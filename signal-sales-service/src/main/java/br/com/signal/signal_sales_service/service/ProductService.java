package br.com.signal.signal_sales_service.service;

import br.com.signal.signal_sales_service.client.AuthClient;
import br.com.signal.signal_sales_service.dto.AuthUserResponse;
import br.com.signal.signal_sales_service.dto.CreateProductRequest;
import br.com.signal.signal_sales_service.dto.ProductResponse;
import br.com.signal.signal_sales_service.dto.UpdateProductRequest;
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
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final AuthClient authClient;

    public ProductResponse create(
            CreateProductRequest request,
            String authorization
    ) {
        AuthUserResponse authUser = authClient.me(authorization);

        if (!"SELLER".equals(authUser.getRole())) {
            throw new BadRequestException("Only sellers can create products");
        }

        if (authUser.getStoreId() == null) {
            throw new BadRequestException("Seller does not have a store");
        }

        ProductCategory category = productCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category not found"));

        if (!Boolean.TRUE.equals(category.getActive())) {
            throw new BadRequestException("Category is inactive");
        }

        if (productRepository.existsByStoreIdAndNameIgnoreCaseAndActiveTrue(
                authUser.getStoreId(),
                request.getName()
        )) {
            throw new BadRequestException("Product already exists for this store");
        }

        Product product = Product.builder()
                .storeId(authUser.getStoreId())
                .category(category)
                .name(request.getName().trim())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        productRepository.save(product);

        return toResponse(product);
    }

    public List<ProductResponse> findAllActive() {
        return productRepository.findByActiveTrueOrderByNameAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ProductResponse> findByStore(UUID storeId) {
        return productRepository.findByStoreIdAndActiveTrueOrderByNameAsc(storeId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ProductResponse> findByStoreAndCategory(
            UUID storeId,
            UUID categoryId
    ) {
        return productRepository
                .findByStoreIdAndCategory_IdAndActiveTrueOrderByNameAsc(
                        storeId,
                        categoryId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ProductResponse findById(UUID id) {
        Product product = findProductEntityById(id);

        return toResponse(product);
    }

    public ProductResponse update(
            UUID id,
            UpdateProductRequest request
    ) {
        Product product = findProductEntityById(id);

        ProductCategory category = productCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category not found"));

        if (!Boolean.TRUE.equals(category.getActive())) {
            throw new BadRequestException("Category is inactive");
        }

        product.setCategory(category);
        product.setName(request.getName().trim());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setUpdatedAt(LocalDateTime.now());

        productRepository.save(product);

        return toResponse(product);
    }

    public void deactivate(UUID id) {
        Product product = findProductEntityById(id);

        product.setActive(false);
        product.setUpdatedAt(LocalDateTime.now());

        productRepository.save(product);
    }

    private Product findProductEntityById(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));
    }

    public List<ProductResponse> findByCategory(UUID categoryId) {
        return productRepository.findByCategory_IdAndActiveTrueOrderByNameAsc(categoryId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private ProductResponse toResponse(Product product) {
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