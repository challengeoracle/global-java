package br.com.signal.signal_sales_service.catalog.service;

import br.com.signal.signal_sales_service.catalog.dto.request.CreateCategoryRequest;
import br.com.signal.signal_sales_service.catalog.dto.request.UpdateCategoryRequest;
import br.com.signal.signal_sales_service.catalog.dto.response.CategoryResponse;
import br.com.signal.signal_sales_service.catalog.dto.response.CategoryWithProductsResponse;
import br.com.signal.signal_sales_service.catalog.dto.response.ProductResponse;
import br.com.signal.signal_sales_service.catalog.mapper.CategoryMapper;
import br.com.signal.signal_sales_service.shared.dto.response.AuthUserResponse;
import br.com.signal.signal_sales_service.shared.service.AuthIdentityService;
import br.com.signal.signal_sales_service.catalog.entity.Product;
import br.com.signal.signal_sales_service.catalog.entity.ProductCategory;
import br.com.signal.signal_sales_service.shared.exception.BadRequestException;
import br.com.signal.signal_sales_service.shared.exception.NotFoundException;
import br.com.signal.signal_sales_service.catalog.repository.ProductCategoryRepository;
import br.com.signal.signal_sales_service.catalog.repository.ProductRepository;
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
    private final AuthIdentityService authIdentityService;

    public CategoryResponse create(CreateCategoryRequest request, String authorization) {
        AuthUserResponse authUser = authIdentityService.requireSeller(authorization);
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

        return CategoryMapper.toResponse(category);
    }

    public List<CategoryResponse> findMyCategories(String authorization) {
        AuthUserResponse authUser = authIdentityService.requireSeller(authorization);

        return productCategoryRepository
                .findByStoreIdAndActiveTrueOrderByNameAsc(authUser.getStoreId())
                .stream()
                .map(CategoryMapper::toResponse)
                .toList();
    }

    public CategoryWithProductsResponse findMyCategoryById(UUID id, String authorization) {
        AuthUserResponse authUser = authIdentityService.requireSeller(authorization);
        ProductCategory category = findCategoryForStore(id, authUser.getStoreId());

        List<ProductResponse> products = CategoryMapper.toProductResponses(
                productRepository.findByStoreIdAndCategory_IdAndActiveTrueOrderByNameAsc(authUser.getStoreId(), id)
        );

        return CategoryMapper.toWithProductsResponse(category, products);
    }

    public CategoryResponse update(UUID id, UpdateCategoryRequest request, String authorization) {
        AuthUserResponse authUser = authIdentityService.requireSeller(authorization);
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

        return CategoryMapper.toResponse(category);
    }

    public void deactivate(UUID id, String authorization) {
        AuthUserResponse authUser = authIdentityService.requireSeller(authorization);
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
}