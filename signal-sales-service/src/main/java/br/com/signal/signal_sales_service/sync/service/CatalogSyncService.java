package br.com.signal.signal_sales_service.sync.service;

import br.com.signal.signal_sales_service.catalog.entity.Product;
import br.com.signal.signal_sales_service.catalog.entity.ProductCategory;
import br.com.signal.signal_sales_service.shared.dto.response.AuthUserResponse;
import br.com.signal.signal_sales_service.shared.service.AuthIdentityService;
import br.com.signal.signal_sales_service.sync.dto.request.CatalogSyncItemRequest;
import br.com.signal.signal_sales_service.sync.dto.request.CatalogSyncRequest;
import br.com.signal.signal_sales_service.sync.dto.response.CatalogSyncResponse;
import br.com.signal.signal_sales_service.sync.dto.response.SyncItemResponse;
import br.com.signal.signal_sales_service.sync.mapper.SyncItemResponseMapper;
import br.com.signal.signal_sales_service.shared.exception.NotFoundException;
import br.com.signal.signal_sales_service.sync.entity.CatalogSyncLog;
import br.com.signal.signal_sales_service.sync.entity.enums.CatalogSyncOperation;
import br.com.signal.signal_sales_service.shared.exception.BadRequestException;
import br.com.signal.signal_sales_service.sync.repository.CatalogSyncLogRepository;
import br.com.signal.signal_sales_service.catalog.repository.ProductCategoryRepository;
import br.com.signal.signal_sales_service.catalog.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CatalogSyncService {

    private final AuthIdentityService authIdentityService;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductRepository productRepository;
    private final CatalogSyncLogRepository catalogSyncLogRepository;
    private final PlatformTransactionManager transactionManager;

    @CacheEvict(cacheNames = {"catalogByStore", "productById", "productsByStore"}, allEntries = true)
    public CatalogSyncResponse syncCatalog(CatalogSyncRequest request, String authorization) {
        AuthUserResponse authUser = authIdentityService.requireSeller(authorization);

        if (request.getChanges() == null || request.getChanges().isEmpty()) {
            return CatalogSyncResponse.builder()
                    .storeId(authUser.getStoreId())
                    .syncedAt(LocalDateTime.now())
                    .results(List.of())
                    .build();
        }

        LocalDateTime syncedAt = LocalDateTime.now();
        List<SyncItemResponse> results = new ArrayList<>();

        List<CatalogSyncItemRequest> uniqueChanges = request.getChanges()
                .stream()
                .filter(item -> {
                    if (item.getOperationId() == null || item.getOperationId().isBlank()) {
                        results.add(rejectMissingOperationId(authUser, syncedAt));
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toMap(
                        CatalogSyncItemRequest::getOperationId,
                        item -> item,
                        (first, duplicate) -> first,
                        LinkedHashMap::new
                ))
                .values()
                .stream()
                .toList();

        uniqueChanges.stream()
                .map(item -> processSyncItemSafely(
                        authUser.getStoreId(),
                        item,
                        syncedAt
                ))
                .forEach(results::add);

        return CatalogSyncResponse.builder()
                .storeId(authUser.getStoreId())
                .syncedAt(syncedAt)
                .results(results)
                .build();
    }

    private SyncItemResponse processSyncItemSafely(
            UUID storeId,
            CatalogSyncItemRequest item,
            LocalDateTime syncedAt
    ) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        try {
            return transactionTemplate.execute(status ->
                    processSyncItemTransactional(storeId, item, syncedAt)
            );
        } catch (RuntimeException ex) {
            if (isUniqueConstraintViolation(ex)) {
                return transactionTemplate.execute(status ->
                        handleDuplicateAfterRollback(storeId, item, syncedAt)
                );
            }

            return transactionTemplate.execute(status ->
                    handleRejectedAfterRollback(storeId, item, syncedAt, ex.getMessage())
            );
        }
    }

    private SyncItemResponse rejectMissingOperationId(
            AuthUserResponse authUser,
            LocalDateTime syncedAt
    ) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        String message = "operationId is required for catalog sync";

        return transactionTemplate.execute(status -> {
            createLog(
                    authUser.getStoreId(),
                    null,
                    null,
                    null,
                    syncedAt,
                    "REJECTED",
                    message
            );

            return SyncItemResponseMapper.forCatalog(
                    null,
                    null,
                    "REJECTED",
                    message,
                    null,
                    null,
                    null,
                    null,
                    syncedAt
            );
        });
    }

    private SyncItemResponse buildCatalogItemResponse(
            CatalogSyncItemRequest item,
            UUID remoteId,
            String status,
            String message,
            Product product,
            ProductCategory category,
            Integer stockQuantity,
            LocalDateTime syncedAt
    ) {
        return SyncItemResponseMapper.forCatalog(
                item != null ? item.getOperationId() : null,
                remoteId,
                status,
                message,
                item != null ? item.getOperation() : null,
                product,
                category,
                stockQuantity,
                syncedAt
        );
    }

    private SyncItemResponse processSyncItemTransactional(
            UUID storeId,
            CatalogSyncItemRequest item,
            LocalDateTime syncedAt
    ) {
        CatalogSyncLog existing = findExistingOperation(item.getOperationId());

        if (existing != null) {
            return toDuplicateSyncResponse(item.getOperationId(), existing, syncedAt);
        }

        SyncItemResponse response = switch (item.getOperation()) {
            case CATEGORY_CREATE -> createCategoryFromSync(storeId, item, syncedAt);
            case CATEGORY_UPDATE -> updateCategoryFromSync(storeId, item, syncedAt);
            case CATEGORY_DEACTIVATE -> deactivateCategoryFromSync(storeId, item, syncedAt);
            case PRODUCT_CREATE -> createProductFromSync(storeId, item, syncedAt);
            case PRODUCT_UPDATE -> updateProductFromSync(storeId, item, syncedAt);
            case PRODUCT_DEACTIVATE -> deactivateProductFromSync(storeId, item, syncedAt);
            case STOCK_UPDATE -> updateStockFromSync(storeId, item, syncedAt);
        };

        return response;
    }

    private SyncItemResponse handleDuplicateAfterRollback(
            UUID storeId,
            CatalogSyncItemRequest item,
            LocalDateTime syncedAt
    ) {
        CatalogSyncLog existing = findExistingOperation(item.getOperationId());

        if (existing != null) {
            return toDuplicateSyncResponse(item.getOperationId(), existing, syncedAt);
        }

        return handleRejectedAfterRollback(
                storeId,
                item,
                syncedAt,
                "Operation could not be synced because of a database constraint"
        );
    }

    private SyncItemResponse handleRejectedAfterRollback(
            UUID storeId,
            CatalogSyncItemRequest item,
            LocalDateTime syncedAt,
            String message
    ) {
        String safeMessage = message != null ? message : "Operation rejected";

        createLogFromItem(
                storeId,
                item,
                null,
                syncedAt,
                "REJECTED",
                safeMessage
        );

        return buildCatalogItemResponse(
                item,
                item.getProductId() != null ? item.getProductId() : item.getCategoryId(),
                "REJECTED",
                safeMessage,
                null,
                null,
                null,
                syncedAt
        );
    }

    private CatalogSyncLog findExistingOperation(String operationId) {
        if (operationId == null || operationId.isBlank()) {
            return null;
        }

        return catalogSyncLogRepository.findByOperationId(operationId).orElse(null);
    }

    private SyncItemResponse toDuplicateSyncResponse(
            String operationId,
            CatalogSyncLog existing,
            LocalDateTime syncedAt
    ) {
        CatalogSyncLog resolvedExisting = resolveDuplicateProductCreateLog(existing);

        return SyncItemResponseMapper.forCatalogFromLog(
                operationId,
                resolvedExisting,
                "DUPLICATE",
                "Operation already processed",
                syncedAt
        );
    }

    private CatalogSyncLog resolveDuplicateProductCreateLog(CatalogSyncLog existing) {
        if (existing.getProduct() != null || existing.getOperation() != CatalogSyncOperation.PRODUCT_CREATE) {
            return existing;
        }

        if (existing.getCategoryId() == null || existing.getProductName() == null || existing.getProductName().isBlank()) {
            return existing;
        }

        return productRepository
                .findByStoreIdAndCategory_IdAndNameIgnoreCaseAndActiveTrue(
                        existing.getStoreId(),
                        existing.getCategoryId(),
                        existing.getProductName().trim()
                )
                .map(product -> {
                    existing.setProduct(product);
                    existing.setStockQuantity(product.getStockQuantity());
                    return catalogSyncLogRepository.saveAndFlush(existing);
                })
                .orElse(existing);
    }

    private SyncItemResponse createCategoryFromSync(
            UUID storeId,
            CatalogSyncItemRequest item,
            LocalDateTime syncedAt
    ) {
        if (item.getCategoryId() == null) {
            return rejectItem(storeId, item, syncedAt, "Category id is required to create category");
        }

        if (item.getName() == null || item.getName().isBlank()) {
            return rejectItem(storeId, item, syncedAt, "Category name is required");
        }

        String categoryName = item.getName().trim();

        if (productCategoryRepository.existsByStoreIdAndNameIgnoreCaseAndActiveTrue(storeId, categoryName)) {
            return logAndReturn(
                    storeId, item, null, syncedAt,
                    "DUPLICATE", "Category already exists for this store",
                    buildCatalogItemResponse(
                            item,
                            item.getCategoryId(),
                            "DUPLICATE",
                            "Category already exists for this store",
                            null,
                            null,
                            null,
                            syncedAt
                    )
            );
        }

        ProductCategory category = ProductCategory.builder()
                .id(item.getCategoryId())
                .storeId(storeId)
                .name(categoryName)
                .description(item.getDescription())
                .active(true)
                .createdAt(syncedAt)
                .updatedAt(syncedAt)
                .build();

        productCategoryRepository.save(category);

        return logAndReturn(
                storeId, item, null, syncedAt,
                "APPLIED", "Category created successfully",
                buildCatalogItemResponse(
                        item,
                        category.getId(),
                        "APPLIED",
                        "Category created successfully",
                        null,
                        category,
                        null,
                        syncedAt
                )
        );
    }

    private SyncItemResponse updateCategoryFromSync(
            UUID storeId,
            CatalogSyncItemRequest item,
            LocalDateTime syncedAt
    ) {
        ProductCategory category = findCategoryForStore(item.getCategoryId(), storeId);

        if (item.getName() != null && !item.getName().isBlank()) {
            category.setName(item.getName().trim());
        }

        if (item.getDescription() != null) {
            category.setDescription(item.getDescription());
        }

        category.setUpdatedAt(syncedAt);
        productCategoryRepository.save(category);

        return logAndReturn(
                storeId, item, null, syncedAt,
                "APPLIED", "Category updated successfully",
                buildCatalogItemResponse(
                        item,
                        category.getId(),
                        "APPLIED",
                        "Category updated successfully",
                        null,
                        category,
                        null,
                        syncedAt
                )
        );
    }

    private SyncItemResponse deactivateCategoryFromSync(
            UUID storeId,
            CatalogSyncItemRequest item,
            LocalDateTime syncedAt
    ) {
        ProductCategory category = findCategoryForStore(item.getCategoryId(), storeId);

        List<Product> activeProducts = productRepository
                .findByStoreIdAndCategory_IdAndActiveTrueOrderByNameAsc(storeId, category.getId());

        if (!activeProducts.isEmpty()) {
            return logAndReturn(
                    storeId, item, null, syncedAt,
                    "REJECTED", "Cannot deactivate category with active products",
                    buildCatalogItemResponse(
                            item,
                            category.getId(),
                            "REJECTED",
                            "Cannot deactivate category with active products",
                            null,
                            category,
                            null,
                            syncedAt
                    )
            );
        }

        category.setActive(false);
        category.setUpdatedAt(syncedAt);
        productCategoryRepository.save(category);

        return logAndReturn(
                storeId, item, null, syncedAt,
                "APPLIED", "Category deactivated successfully",
                buildCatalogItemResponse(
                        item,
                        category.getId(),
                        "APPLIED",
                        "Category deactivated successfully",
                        null,
                        category,
                        null,
                        syncedAt
                )
        );
    }

    private SyncItemResponse createProductFromSync(
            UUID storeId,
            CatalogSyncItemRequest item,
            LocalDateTime syncedAt
    ) {
        String validationError = validateCreateProductItem(item);
        if (validationError != null) {
            return rejectItem(storeId, item, syncedAt, validationError);
        }

        ProductCategory category = findCategoryForStore(item.getCategoryId(), storeId);

        if (productRepository.existsByStoreIdAndNameIgnoreCaseAndActiveTrue(storeId, item.getName())) {
            return logAndReturn(
                    storeId, item, null, syncedAt,
                    "REJECTED", "Product already exists for this store",
                    buildCatalogItemResponse(
                            item,
                            category.getId(),
                            "REJECTED",
                            "Product already exists for this store",
                            null,
                            category,
                            null,
                            syncedAt
                    )
            );
        }

        Product product = Product.builder()
                .id(item.getProductId())
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

        Product savedProduct = productRepository.saveAndFlush(product);

        return logAndReturn(
                storeId, item, savedProduct, syncedAt,
                "APPLIED", "Product created successfully",
                buildCatalogItemResponse(
                        item,
                        savedProduct.getId(),
                        "APPLIED",
                        "Product created successfully",
                        savedProduct,
                        category,
                        savedProduct.getStockQuantity(),
                        syncedAt
                )
        );
    }

    private SyncItemResponse updateProductFromSync(
            UUID storeId,
            CatalogSyncItemRequest item,
            LocalDateTime syncedAt
    ) {
        Product product = findProductForStore(item.getProductId(), storeId);

        if (item.getCategoryId() != null) {
            ProductCategory category = findCategoryForStore(item.getCategoryId(), storeId);
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
                return rejectItem(storeId, item, syncedAt, "Price must be greater than zero");
            }

            product.setPrice(item.getPrice());
        }

        if (item.getStockQuantity() != null) {
            if (item.getStockQuantity() < 0) {
                return rejectItem(storeId, item, syncedAt, "Stock quantity cannot be negative");
            }

            product.setStockQuantity(item.getStockQuantity());
        }

        product.setUpdatedAt(syncedAt);
        productRepository.save(product);

        return logAndReturn(
                storeId, item, product, syncedAt,
                "APPLIED", "Product updated successfully",
                buildCatalogItemResponse(
                        item,
                        product.getId(),
                        "APPLIED",
                        "Product updated successfully",
                        product,
                        null,
                        product.getStockQuantity(),
                        syncedAt
                )
        );
    }

    private SyncItemResponse deactivateProductFromSync(
            UUID storeId,
            CatalogSyncItemRequest item,
            LocalDateTime syncedAt
    ) {
        Product product = findProductForStore(item.getProductId(), storeId);

        product.setActive(false);
        product.setUpdatedAt(syncedAt);
        productRepository.save(product);

        return logAndReturn(
                storeId, item, product, syncedAt,
                "APPLIED", "Product deactivated successfully",
                buildCatalogItemResponse(
                        item,
                        product.getId(),
                        "APPLIED",
                        "Product deactivated successfully",
                        product,
                        null,
                        product.getStockQuantity(),
                        syncedAt
                )
        );
    }

    private SyncItemResponse updateStockFromSync(
            UUID storeId,
            CatalogSyncItemRequest item,
            LocalDateTime syncedAt
    ) {
        Product product = findProductForStore(item.getProductId(), storeId);

        if (!Boolean.TRUE.equals(product.getActive())) {
            return rejectItem(storeId, item, syncedAt, "Product is inactive");
        }

        if (item.getQuantityDelta() == null) {
            return rejectItem(storeId, item, syncedAt, "Quantity delta is required for stock update");
        }

        int currentStock = product.getStockQuantity();
        int newStock = currentStock + item.getQuantityDelta();

        if (newStock < 0) {
            return logAndReturn(
                    storeId, item, product, syncedAt,
                    "REJECTED", "Stock quantity cannot be negative",
                    buildCatalogItemResponse(
                            item,
                            product.getId(),
                            "REJECTED",
                            "Stock quantity cannot be negative",
                            product,
                            null,
                            currentStock,
                            syncedAt
                    )
            );
        }

        product.setStockQuantity(newStock);
        product.setUpdatedAt(syncedAt);
        productRepository.save(product);

        return logAndReturn(
                storeId, item, product, syncedAt,
                "APPLIED", "Stock updated successfully",
                buildCatalogItemResponse(
                        item,
                        product.getId(),
                        "APPLIED",
                        "Stock updated successfully",
                        product,
                        null,
                        newStock,
                        syncedAt
                )
        );
    }

    private SyncItemResponse rejectItem(
            UUID storeId,
            CatalogSyncItemRequest item,
            LocalDateTime syncedAt,
            String message
    ) {
        return logAndReturn(
                storeId, item, null, syncedAt,
                "REJECTED", message,
                buildCatalogItemResponse(
                        item,
                        item.getProductId() != null ? item.getProductId() : item.getCategoryId(),
                        "REJECTED",
                        message,
                        null,
                        null,
                        null,
                        syncedAt
                )
        );
    }

    private SyncItemResponse logAndReturn(
            UUID storeId,
            CatalogSyncItemRequest item,
            Product product,
            LocalDateTime syncedAt,
            String status,
            String message,
            SyncItemResponse response
    ) {
        createLogFromItem(storeId, item, product, syncedAt, status, message);
        return response;
    }

    private ProductCategory findCategoryForStore(UUID categoryId, UUID storeId) {
        if (categoryId == null) {
            throw new BadRequestException("Category id is required");
        }

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

    private String validateCreateProductItem(CatalogSyncItemRequest item) {
        if (item.getCategoryId() == null) {
            return "Category id is required to create product";
        }

        if (item.getName() == null || item.getName().isBlank()) {
            return "Product name is required to create product";
        }

        if (item.getPrice() == null || item.getPrice().signum() <= 0) {
            return "Valid price is required to create product";
        }

        if (item.getStockQuantity() == null || item.getStockQuantity() < 0) {
            return "Valid stock quantity is required";
        }

        return null;
    }

    private void createLogFromItem(
            UUID storeId,
            CatalogSyncItemRequest item,
            Product product,
            LocalDateTime syncedAt,
            String status,
            String message
    ) {
        createLog(
                storeId,
                item != null ? item.getOperationId() : null,
                item,
                product,
                syncedAt,
                status,
                message
        );
    }

    private void createLog(
            UUID storeId,
            String operationId,
            CatalogSyncItemRequest item,
            Product product,
            LocalDateTime syncedAt,
            String status,
            String message
    ) {
        CatalogSyncLog log = CatalogSyncLog.builder()
                .storeId(storeId)
                .product(product)
                .operationId(operationId)
                .operation(item != null ? item.getOperation() : null)
                .quantityDelta(item != null ? item.getQuantityDelta() : null)
                .productName(item != null ? item.getName() : null)
                .categoryId(item != null ? item.getCategoryId() : null)
                .price(item != null ? item.getPrice() : null)
                .stockQuantity(product != null ? product.getStockQuantity() : (item != null ? item.getStockQuantity() : null))
                .localUpdatedAt(item != null ? item.getLocalUpdatedAt() : syncedAt)
                .syncedAt(syncedAt)
                .status(status)
                .message(message != null && message.length() > 255 ? message.substring(0, 255) : message)
                .build();

        catalogSyncLogRepository.save(log);
    }

    private boolean isUniqueConstraintViolation(Throwable ex) {
        for (Throwable current = ex; current != null; current = current.getCause()) {
            if (current instanceof DataIntegrityViolationException) {
                return true;
            }

            String errorMessage = current.getMessage();
            if (errorMessage != null && (errorMessage.contains("ORA-00001") || errorMessage.contains("unique constraint"))) {
                return true;
            }
        }

        return false;
    }
}
