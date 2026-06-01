package br.com.signal.signal_sales_service.catalog.service;

import br.com.signal.signal_sales_service.catalog.dto.request.CreateProductRequest;
import br.com.signal.signal_sales_service.catalog.dto.response.ProductResponse;
import br.com.signal.signal_sales_service.catalog.entity.Product;
import br.com.signal.signal_sales_service.catalog.entity.ProductCategory;
import br.com.signal.signal_sales_service.catalog.mapper.ProductMapper;
import br.com.signal.signal_sales_service.catalog.repository.ProductCategoryRepository;
import br.com.signal.signal_sales_service.catalog.repository.ProductRepository;
import br.com.signal.signal_sales_service.catalog.dto.request.UpdateProductRequest;
import br.com.signal.signal_sales_service.shared.dto.response.AuthUserResponse;
import br.com.signal.signal_sales_service.shared.dto.response.PageResponse;
import br.com.signal.signal_sales_service.shared.exception.BadRequestException;
import br.com.signal.signal_sales_service.shared.exception.NotFoundException;
import br.com.signal.signal_sales_service.shared.service.AuthIdentityService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final AuthIdentityService authIdentityService;

    @Transactional
    @CacheEvict(cacheNames = {"catalogByStore", "productById", "productsByStore"}, allEntries = true)
    public ProductResponse create(
            CreateProductRequest request,
            String authorization
    ) {
        AuthUserResponse authUser = authIdentityService.requireSeller(authorization);

        ProductCategory category = productCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category not found"));

        validateCategoryForSellerStore(category, authUser.getStoreId());

        if (productRepository.existsByStoreIdAndNameIgnoreCaseAndActiveTrue(
                authUser.getStoreId(),
                request.getName()
        )) {
            throw new BadRequestException("Product already exists for this store");
        }

        Product product = Product.builder()
                .id(UUID.randomUUID())
                .storeId(authUser.getStoreId())
                .category(category)
                .name(request.getName().trim())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        productRepository.save(product);

        return ProductMapper.toResponse(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findAllActive() {
        return productRepository.findByActiveTrueOrderByNameAsc()
                .stream()
                .map(ProductMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> findAllActivePage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return PageResponse.from(
                productRepository.findByActiveTrueOrderByNameAsc(pageable).map(ProductMapper::toResponse)
        );
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "productsByStore", key = "#storeId")
    public List<ProductResponse> findByStore(UUID storeId) {
        return productRepository.findByStoreIdAndActiveTrueOrderByNameAsc(storeId)
                .stream()
                .map(ProductMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> findByStorePage(UUID storeId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return PageResponse.from(
                productRepository.findByStoreIdAndActiveTrueOrderByNameAsc(storeId, pageable).map(ProductMapper::toResponse)
        );
    }

    @Transactional(readOnly = true)
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
                .map(ProductMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findByCategory(UUID categoryId) {
        return productRepository.findByCategory_IdAndActiveTrueOrderByNameAsc(categoryId)
                .stream()
                .map(ProductMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> findByCategoryPage(UUID categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return PageResponse.from(
                productRepository.findByCategory_IdAndActiveTrueOrderByNameAsc(categoryId, pageable).map(ProductMapper::toResponse)
        );
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> findByStoreAndCategoryPage(UUID storeId, UUID categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return PageResponse.from(
                productRepository.findByStoreIdAndCategory_IdAndActiveTrueOrderByNameAsc(storeId, categoryId, pageable)
                        .map(ProductMapper::toResponse)
        );
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "productById", key = "#id")
    public ProductResponse findById(UUID id) {
        Product product = findProductEntityById(id);

        return ProductMapper.toResponse(product);
    }

    @Transactional
    @CacheEvict(cacheNames = {"catalogByStore", "productById", "productsByStore"}, allEntries = true)
    public ProductResponse update(
            UUID id,
            UpdateProductRequest request,
            String authorization
    ) {
        AuthUserResponse authUser = authIdentityService.requireSeller(authorization);

        Product product = findProductEntityById(id);

        validateProductForSellerStore(product, authUser.getStoreId());

        ProductCategory category = productCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category not found"));

        validateCategoryForSellerStore(category, authUser.getStoreId());

        product.setCategory(category);
        product.setName(request.getName().trim());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setUpdatedAt(LocalDateTime.now());

        productRepository.save(product);

        return ProductMapper.toResponse(product);
    }

    @Transactional
    @CacheEvict(cacheNames = {"catalogByStore", "productById", "productsByStore"}, allEntries = true)
    public void deactivate(
            UUID id,
            String authorization
    ) {
        AuthUserResponse authUser = authIdentityService.requireSeller(authorization);

        Product product = findProductEntityById(id);

        validateProductForSellerStore(product, authUser.getStoreId());

        product.setActive(false);
        product.setUpdatedAt(LocalDateTime.now());

        productRepository.save(product);
    }

    private void validateCategoryForSellerStore(
            ProductCategory category,
            UUID storeId
    ) {
        if (!storeId.equals(category.getStoreId())) {
            throw new BadRequestException("Category does not belong to seller store");
        }

        if (!Boolean.TRUE.equals(category.getActive())) {
            throw new BadRequestException("Category is inactive");
        }
    }

    private void validateProductForSellerStore(
            Product product,
            UUID storeId
    ) {
        if (!storeId.equals(product.getStoreId())) {
            throw new BadRequestException("Product does not belong to seller store");
        }

        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new BadRequestException("Product is inactive");
        }
    }

    private Product findProductEntityById(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));
    }
}
