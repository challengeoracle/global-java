package br.com.signal.signal_sales_service.sync.mapper;

import br.com.signal.signal_sales_service.sync.dto.response.SyncItemResponse;
import br.com.signal.signal_sales_service.sync.dto.response.SyncItemStateResponse;
import br.com.signal.signal_sales_service.sync.entity.CatalogSyncLog;
import br.com.signal.signal_sales_service.catalog.entity.Product;
import br.com.signal.signal_sales_service.catalog.entity.ProductCategory;
import br.com.signal.signal_sales_service.order.entity.SalesOrder;
import br.com.signal.signal_sales_service.sync.entity.enums.CatalogSyncOperation;

import java.time.LocalDateTime;
import java.util.UUID;

public final class SyncItemResponseMapper {

    private SyncItemResponseMapper() {
    }

    public static SyncItemResponse forOrder(
            String localId,
            UUID remoteId,
            String status,
            String message,
            SalesOrder order,
            LocalDateTime syncedAt
    ) {
        return SyncItemResponse.builder()
                .localId(localId)
                .remoteId(remoteId)
                .status(status)
                .message(message)
                .currentState(order != null ? orderState(order) : null)
                .syncedAt(syncedAt)
                .build();
    }

    public static SyncItemResponse forCatalog(
            String operationId,
            UUID remoteId,
            String status,
            String message,
            CatalogSyncOperation operation,
            Product product,
            ProductCategory category,
            Integer stockQuantity,
            LocalDateTime syncedAt
    ) {
        return SyncItemResponse.builder()
                .operationId(operationId)
                .remoteId(remoteId)
                .status(status)
                .message(message)
                .currentState(catalogState(operation, product, category, stockQuantity))
                .syncedAt(syncedAt)
                .build();
    }

    public static SyncItemResponse forCatalogFromLog(
            String operationId,
            CatalogSyncLog log,
            String status,
            String message,
            LocalDateTime syncedAt
    ) {
        Product product = log.getProduct();
        UUID categoryId = product != null ? product.getCategory().getId() : log.getCategoryId();

        return SyncItemResponse.builder()
                .operationId(operationId)
                .remoteId(resolveCatalogRemoteId(product, log.getCategoryId()))
                .status(status)
                .message(message)
                .currentState(SyncItemStateResponse.builder()
                        .operation(log.getOperation() != null ? log.getOperation().name() : null)
                        .productId(product != null ? product.getId() : null)
                        .categoryId(categoryId)
                        .stockQuantity(log.getStockQuantity())
                        .active(product != null ? product.getActive() : null)
                        .build())
                .syncedAt(syncedAt)
                .build();
    }

    private static SyncItemStateResponse orderState(SalesOrder order) {
        return SyncItemStateResponse.builder()
                .orderStatus(order.getOrderStatus().name())
                .paymentStatus(order.getPaymentStatus().name())
                .syncStatus(order.getSyncStatus().name())
                .totalAmount(order.getTotalAmount())
                .build();
    }

    private static SyncItemStateResponse catalogState(
            CatalogSyncOperation operation,
            Product product,
            ProductCategory category,
            Integer stockQuantity
    ) {
        SyncItemStateResponse.SyncItemStateResponseBuilder builder = SyncItemStateResponse.builder()
                .operation(operation != null ? operation.name() : null)
                .stockQuantity(stockQuantity);

        if (product != null) {
            builder.productId(product.getId())
                    .categoryId(product.getCategory().getId())
                    .active(product.getActive());
        } else if (category != null) {
            builder.categoryId(category.getId())
                    .active(category.getActive());
        }

        return builder.build();
    }

    private static UUID resolveCatalogRemoteId(Product product, UUID categoryId) {
        if (product != null) {
            return product.getId();
        }

        return categoryId;
    }
}
