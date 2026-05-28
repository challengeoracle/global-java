package br.com.signal.signal_sales_service.service;

import br.com.signal.signal_sales_service.client.AuthClient;
import br.com.signal.signal_sales_service.dto.*;
import br.com.signal.signal_sales_service.entity.CatalogSyncLog;
import br.com.signal.signal_sales_service.entity.Product;
import br.com.signal.signal_sales_service.entity.ProductCategory;
import br.com.signal.signal_sales_service.entity.enums.CatalogSyncOperation;
import br.com.signal.signal_sales_service.exception.BadRequestException;
import br.com.signal.signal_sales_service.exception.NotFoundException;
import br.com.signal.signal_sales_service.repository.CatalogSyncLogRepository;
import br.com.signal.signal_sales_service.repository.ProductCategoryRepository;
import br.com.signal.signal_sales_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CatalogService {

    private final AuthClient authClient;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductRepository productRepository;
    private final CatalogSyncLogRepository catalogSyncLogRepository;

    public CatalogResponse findCatalogByStore(UUID storeId) {
        List<ProductCategory> categories =
                productCategoryRepository.findByActiveTrueOrderByNameAsc();

        List<CatalogCategoryResponse> categoryResponses = categories.stream()
                .map(category -> {
                    List<CatalogProductResponse> products =
                            productRepository
                                    .findByStoreIdAndCategory_IdAndActiveTrueOrderByNameAsc(
                                            storeId,
                                            category.getId()
                                    )
                                    .stream()
                                    .map(this::toCatalogProductResponse)
                                    .toList();

                    return CatalogCategoryResponse.builder()
                            .id(category.getId())
                            .name(category.getName())
                            .description(category.getDescription())
                            .active(category.getActive())
                            .createdAt(category.getCreatedAt())
                            .products(products)
                            .build();
                })
                .filter(category -> !category.getProducts().isEmpty())
                .toList();

        return CatalogResponse.builder()
                .storeId(storeId)
                .syncedAt(LocalDateTime.now())
                .categories(categoryResponses)
                .build();
    }

    public CatalogSyncResponse syncCatalog(
            CatalogSyncRequest request,
            String authorization
    ) {
        AuthUserResponse authUser = authClient.me(authorization);

        if (!"SELLER".equals(authUser.getRole())) {
            throw new BadRequestException("Only sellers can sync catalog changes");
        }

        if (authUser.getStoreId() == null) {
            throw new BadRequestException("Seller does not have a store");
        }

        LocalDateTime syncedAt = LocalDateTime.now();

        List<CatalogSyncItemResponse> results = request.getChanges()
                .stream()
                .map(item -> processSyncItem(
                        authUser.getStoreId(),
                        request.getDeviceId(),
                        item,
                        syncedAt
                ))
                .toList();

        return CatalogSyncResponse.builder()
                .storeId(authUser.getStoreId())
                .syncedAt(syncedAt)
                .results(results)
                .build();
    }

    private CatalogSyncItemResponse processSyncItem(
            UUID storeId,
            String deviceId,
            CatalogSyncItemRequest item,
            LocalDateTime syncedAt
    ) {
        return switch (item.getOperation()) {
            case PRODUCT_CREATE -> createProductFromSync(storeId, deviceId, item, syncedAt);
            case PRODUCT_UPDATE -> updateProductFromSync(storeId, deviceId, item, syncedAt);
            case PRODUCT_DEACTIVATE -> deactivateProductFromSync(storeId, deviceId, item, syncedAt);
            case STOCK_UPDATE -> updateStockFromSync(storeId, deviceId, item, syncedAt);
        };
    }

    private CatalogSyncItemResponse createProductFromSync(
            UUID storeId,
            String deviceId,
            CatalogSyncItemRequest item,
            LocalDateTime syncedAt
    ) {
        validateCreateProductItem(item);

        ProductCategory category = productCategoryRepository.findById(item.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category not found"));

        if (!Boolean.TRUE.equals(category.getActive())) {
            throw new BadRequestException("Category is inactive");
        }

        if (productRepository.existsByStoreIdAndNameIgnoreCaseAndActiveTrue(
                storeId,
                item.getName()
        )) {
            throw new BadRequestException("Product already exists for this store");
        }

        Product product = Product.builder()
                .storeId(storeId)
                .category(category)
                .name(item.getName().trim())
                .description(item.getDescription())
                .price(item.getPrice())
                .stockQuantity(item.getStockQuantity())
                .active(true)
                .createdAt(syncedAt)
                .updatedAt(syncedAt)
                .build();

        productRepository.save(product);

        createLog(
                storeId,
                deviceId,
                item,
                product,
                syncedAt,
                "APPLIED",
                "Product created successfully"
        );

        return CatalogSyncItemResponse.builder()
                .productId(product.getId())
                .status("APPLIED")
                .message("Product created successfully")
                .currentStockQuantity(product.getStockQuantity())
                .build();
    }

    private CatalogSyncItemResponse updateProductFromSync(
            UUID storeId,
            String deviceId,
            CatalogSyncItemRequest item,
            LocalDateTime syncedAt
    ) {
        Product product = findProductForStore(item.getProductId(), storeId);

        if (item.getCategoryId() != null) {
            ProductCategory category = productCategoryRepository.findById(item.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Category not found"));

            if (!Boolean.TRUE.equals(category.getActive())) {
                throw new BadRequestException("Category is inactive");
            }

            product.setCategory(category);
        }

        if (item.getName() != null && !item.getName().isBlank()) {
            product.setName(item.getName().trim());
        }

        if (item.getDescription() != null) {
            product.setDescription(item.getDescription());
        }

        if (item.getPrice() != null) {
            if (item.getPrice().signum() <= 0) {
                throw new BadRequestException("Price must be greater than zero");
            }

            product.setPrice(item.getPrice());
        }

        if (item.getStockQuantity() != null) {
            if (item.getStockQuantity() < 0) {
                throw new BadRequestException("Stock quantity cannot be negative");
            }

            product.setStockQuantity(item.getStockQuantity());
        }

        product.setUpdatedAt(syncedAt);

        productRepository.save(product);

        createLog(
                storeId,
                deviceId,
                item,
                product,
                syncedAt,
                "APPLIED",
                "Product updated successfully"
        );

        return CatalogSyncItemResponse.builder()
                .productId(product.getId())
                .status("APPLIED")
                .message("Product updated successfully")
                .currentStockQuantity(product.getStockQuantity())
                .build();
    }

    private CatalogSyncItemResponse deactivateProductFromSync(
            UUID storeId,
            String deviceId,
            CatalogSyncItemRequest item,
            LocalDateTime syncedAt
    ) {
        Product product = findProductForStore(item.getProductId(), storeId);

        product.setActive(false);
        product.setUpdatedAt(syncedAt);

        productRepository.save(product);

        createLog(
                storeId,
                deviceId,
                item,
                product,
                syncedAt,
                "APPLIED",
                "Product deactivated successfully"
        );

        return CatalogSyncItemResponse.builder()
                .productId(product.getId())
                .status("APPLIED")
                .message("Product deactivated successfully")
                .currentStockQuantity(product.getStockQuantity())
                .build();
    }

    private CatalogSyncItemResponse updateStockFromSync(
            UUID storeId,
            String deviceId,
            CatalogSyncItemRequest item,
            LocalDateTime syncedAt
    ) {
        Product product = findProductForStore(item.getProductId(), storeId);

        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new BadRequestException("Product is inactive");
        }

        if (item.getQuantityDelta() == null) {
            throw new BadRequestException("Quantity delta is required for stock update");
        }

        int currentStock = product.getStockQuantity();
        int newStock = currentStock + item.getQuantityDelta();

        if (newStock < 0) {
            createLog(
                    storeId,
                    deviceId,
                    item,
                    product,
                    syncedAt,
                    "REJECTED",
                    "Stock quantity cannot be negative"
            );

            return CatalogSyncItemResponse.builder()
                    .productId(product.getId())
                    .status("REJECTED")
                    .message("Stock quantity cannot be negative")
                    .currentStockQuantity(currentStock)
                    .build();
        }

        product.setStockQuantity(newStock);
        product.setUpdatedAt(syncedAt);

        productRepository.save(product);

        createLog(
                storeId,
                deviceId,
                item,
                product,
                syncedAt,
                "APPLIED",
                "Stock updated successfully"
        );

        return CatalogSyncItemResponse.builder()
                .productId(product.getId())
                .status("APPLIED")
                .message("Stock updated successfully")
                .currentStockQuantity(newStock)
                .build();
    }

    private Product findProductForStore(UUID productId, UUID storeId) {
        if (productId == null) {
            throw new BadRequestException("Product id is required");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        if (!product.getStoreId().equals(storeId)) {
            throw new BadRequestException("Product does not belong to seller store");
        }

        return product;
    }

    private void validateCreateProductItem(CatalogSyncItemRequest item) {
        if (item.getCategoryId() == null) {
            throw new BadRequestException("Category id is required to create product");
        }

        if (item.getName() == null || item.getName().isBlank()) {
            throw new BadRequestException("Product name is required to create product");
        }

        if (item.getPrice() == null || item.getPrice().signum() <= 0) {
            throw new BadRequestException("Valid price is required to create product");
        }

        if (item.getStockQuantity() == null || item.getStockQuantity() < 0) {
            throw new BadRequestException("Valid stock quantity is required to create product");
        }
    }

    private void createLog(
            UUID storeId,
            String deviceId,
            CatalogSyncItemRequest item,
            Product product,
            LocalDateTime syncedAt,
            String status,
            String message
    ) {
        CatalogSyncLog log = CatalogSyncLog.builder()
                .storeId(storeId)
                .product(product)
                .deviceId(deviceId)
                .operation(item.getOperation())
                .quantityDelta(item.getQuantityDelta())
                .productName(item.getName())
                .categoryId(item.getCategoryId())
                .price(item.getPrice())
                .stockQuantity(item.getStockQuantity())
                .localUpdatedAt(item.getLocalUpdatedAt())
                .syncedAt(syncedAt)
                .status(status)
                .message(message)
                .build();

        catalogSyncLogRepository.save(log);
    }

    private CatalogProductResponse toCatalogProductResponse(Product product) {
        return CatalogProductResponse.builder()
                .id(product.getId())
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