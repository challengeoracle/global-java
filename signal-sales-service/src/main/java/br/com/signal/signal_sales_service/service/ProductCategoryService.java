package br.com.signal.signal_sales_service.service;

import br.com.signal.signal_sales_service.client.AuthClient;
import br.com.signal.signal_sales_service.dto.*;
import br.com.signal.signal_sales_service.entity.Product;
import br.com.signal.signal_sales_service.entity.ProductCategory;
import br.com.signal.signal_sales_service.exception.BadRequestException;
import br.com.signal.signal_sales_service.exception.NotFoundException;
import br.com.signal.signal_sales_service.repository.ProductCategoryRepository;
import br.com.signal.signal_sales_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductCategoryService {

    private final ProductCategoryRepository productCategoryRepository;
    private final ProductRepository productRepository;
    private final AuthClient authClient;

    public CategoryResponse create(CreateCategoryRequest request, String authorization) {
        AuthUserResponse authUser = getSeller(authorization);
        String categoryName = request.getName().trim();

        if (productCategoryRepository.existsByStoreIdAndNameIgnoreCaseAndActiveTrue(authUser.getStoreId(), categoryName)) {
            throw new BadRequestException("Category already exists for this store");
        }

        LocalDateTime now = LocalDateTime.now();

        ProductCategory category = ProductCategory.builder()
                .id(UUID.randomUUID())
                .storeId(authUser.getStoreId())
                .name(categoryName)
                .description(request.getDescription())
                .active(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        productCategoryRepository.save(category);

        return toCategoryResponse(category);
    }

    public List<CategoryResponse> findMyCategories(String authorization) {
        AuthUserResponse authUser = getSeller(authorization);

        return productCategoryRepository
                .findByStoreIdAndActiveTrueOrderByNameAsc(authUser.getStoreId())
                .stream()
                .map(this::toCategoryResponse)
                .toList();
    }

    public CategoryWithProductsResponse findMyCategoryById(UUID id, String authorization) {
        AuthUserResponse authUser = getSeller(authorization);
        ProductCategory category = findCategoryForStore(id, authUser.getStoreId());

        List<ProductResponse> products = productRepository
                .findByStoreIdAndCategory_IdAndActiveTrueOrderByNameAsc(authUser.getStoreId(), id)
                .stream()
                .map(this::toProductResponse)
                .toList();

        return CategoryWithProductsResponse.builder()
                .id(category.getId())
                .storeId(category.getStoreId())
                .name(category.getName())
                .description(category.getDescription())
                .active(category.getActive())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .products(products)
                .build();
    }

    public CategoryResponse update(UUID id, UpdateCategoryRequest request, String authorization) {
        AuthUserResponse authUser = getSeller(authorization);
        ProductCategory category = findCategoryForStore(id, authUser.getStoreId());

        String categoryName = request.getName().trim();

        boolean duplicatedName = productCategoryRepository
                .findByStoreIdAndActiveTrueOrderByNameAsc(authUser.getStoreId())
                .stream()
                .anyMatch(existing ->
                        !existing.getId().equals(id)
                                && existing.getName().equalsIgnoreCase(categoryName)
                );

        if (duplicatedName) {
            throw new BadRequestException("Category already exists for this store");
        }

        category.setName(categoryName);
        category.setDescription(request.getDescription());
        category.setUpdatedAt(LocalDateTime.now());

        productCategoryRepository.save(category);

        return toCategoryResponse(category);
    }

    public void deactivate(UUID id, String authorization) {
        AuthUserResponse authUser = getSeller(authorization);
        ProductCategory category = findCategoryForStore(id, authUser.getStoreId());

        List<Product> products = productRepository
                .findByStoreIdAndCategory_IdAndActiveTrueOrderByNameAsc(authUser.getStoreId(), id);

        if (!products.isEmpty()) {
            throw new BadRequestException("Cannot deactivate category with active products");
        }

        category.setActive(false);
        category.setUpdatedAt(LocalDateTime.now());

        productCategoryRepository.save(category);
    }

    private AuthUserResponse getSeller(String authorization) {
        AuthUserResponse authUser = authClient.me(authorization);

        if (!"SELLER".equals(authUser.getRole())) {
            throw new BadRequestException("Only sellers can manage categories");
        }

        if (authUser.getStoreId() == null) {
            throw new BadRequestException("Seller does not have a store");
        }

        return authUser;
    }

    private ProductCategory findCategoryForStore(UUID categoryId, UUID storeId) {
        ProductCategory category = productCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found"));

        if (!storeId.equals(category.getStoreId())) {
            throw new BadRequestException("Category does not belong to seller store");
        }

        if (!Boolean.TRUE.equals(category.getActive())) {
            throw new BadRequestException("Category is inactive");
        }

        return category;
    }

    private CategoryResponse toCategoryResponse(ProductCategory category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .storeId(category.getStoreId())
                .name(category.getName())
                .description(category.getDescription())
                .active(category.getActive())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }

    private ProductResponse toProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .storeId(product.getStoreId())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .active(product.getActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}