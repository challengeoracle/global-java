package br.com.signal.signal_sales_service.sync.service;

import br.com.signal.signal_sales_service.order.entity.SalesOrder;
import br.com.signal.signal_sales_service.order.messaging.OrderEventPublisher;
import br.com.signal.signal_sales_service.order.repository.SalesOrderRepository;
import br.com.signal.signal_sales_service.order.service.OrderService;
import br.com.signal.signal_sales_service.shared.dto.response.AuthUserResponse;
import br.com.signal.signal_sales_service.shared.service.AuthIdentityService;
import br.com.signal.signal_sales_service.sync.dto.request.OfflineOrderRequest;
import br.com.signal.signal_sales_service.sync.dto.request.OrderSyncRequest;
import br.com.signal.signal_sales_service.sync.dto.response.OrderSyncResponse;
import br.com.signal.signal_sales_service.sync.dto.response.SyncItemResponse;
import br.com.signal.signal_sales_service.sync.entity.OrderSyncLog;
import br.com.signal.signal_sales_service.sync.mapper.SyncItemResponseMapper;
import br.com.signal.signal_sales_service.sync.repository.OrderSyncLogRepository;
import br.com.signal.signal_sales_service.order.entity.enums.SyncStatus;
import lombok.RequiredArgsConstructor;
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
public class OrderSyncService {

    private final AuthIdentityService authIdentityService;
    private final SalesOrderRepository salesOrderRepository;
    private final OrderSyncLogRepository orderSyncLogRepository;
    private final OrderEventPublisher orderEventPublisher;
    private final OrderService orderService;
    private final PlatformTransactionManager transactionManager;

    public OrderSyncResponse syncOfflineOrders(OrderSyncRequest request, String authorization) {
        AuthUserResponse authUser = authIdentityService.requireSeller(authorization);

        if (request.getOrders() == null || request.getOrders().isEmpty()) {
            return OrderSyncResponse.builder()
                    .storeId(authUser.getStoreId())
                    .syncedAt(LocalDateTime.now())
                    .results(List.of())
                    .build();
        }

        LocalDateTime syncedAt = LocalDateTime.now();
        String deviceId = request.getDeviceId();
        List<SyncItemResponse> results = new ArrayList<>();

        List<OfflineOrderRequest> uniqueOrders = request.getOrders()
                .stream()
                .filter(order -> {
                    if (order.getLocalOrderId() == null || order.getLocalOrderId().isBlank()) {
                        results.add(rejectMissingLocalOrderId(deviceId, authUser, syncedAt));
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toMap(
                        OfflineOrderRequest::getLocalOrderId,
                        order -> order,
                        (first, duplicate) -> first,
                        LinkedHashMap::new
                ))
                .values()
                .stream()
                .toList();

        uniqueOrders.stream()
                .map(orderRequest -> syncSingleOfflineOrderSafely(
                        orderRequest,
                        deviceId,
                        authUser,
                        syncedAt
                ))
                .forEach(results::add);

        return OrderSyncResponse.builder()
                .storeId(authUser.getStoreId())
                .syncedAt(syncedAt)
                .results(results)
                .build();
    }

    private SyncItemResponse syncSingleOfflineOrderSafely(
            OfflineOrderRequest orderRequest,
            String deviceId,
            AuthUserResponse authUser,
            LocalDateTime syncedAt
    ) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        try {
            return transactionTemplate.execute(status ->
                    syncSingleOfflineOrderTransactional(orderRequest, deviceId, authUser, syncedAt)
            );
        } catch (RuntimeException ex) {
            if (isUniqueConstraintViolation(ex)) {
                return transactionTemplate.execute(status ->
                        handleDuplicateAfterRollback(orderRequest, deviceId, authUser, syncedAt)
                );
            }

            return transactionTemplate.execute(status ->
                    handleRejectedAfterRollback(orderRequest, deviceId, authUser, syncedAt, ex.getMessage())
            );
        }
    }

    private SyncItemResponse rejectMissingLocalOrderId(
            String deviceId,
            AuthUserResponse authUser,
            LocalDateTime syncedAt
    ) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        String message = "localOrderId is required for offline sync";

        return transactionTemplate.execute(status -> {
            createOrderSyncLog(
                    authUser.getStoreId(),
                    null,
                    null,
                    deviceId,
                    "REJECTED",
                    message,
                    syncedAt
            );

            return SyncItemResponseMapper.forOrder(
                    null,
                    null,
                    "REJECTED",
                    message,
                    null,
                    syncedAt
            );
        });
    }

    private boolean isUniqueConstraintViolation(Throwable ex) {
        for (Throwable current = ex; current != null; current = current.getCause()) {
            if (current instanceof DataIntegrityViolationException) {
                return true;
            }

            String message = current.getMessage();
            if (message != null && (message.contains("ORA-00001") || message.contains("unique constraint"))) {
                return true;
            }
        }

        return false;
    }

    private SyncItemResponse syncSingleOfflineOrderTransactional(
            OfflineOrderRequest orderRequest,
            String deviceId,
            AuthUserResponse authUser,
            LocalDateTime syncedAt
    ) {
        SalesOrder existing = findExistingOfflineOrder(orderRequest.getLocalOrderId());

        if (existing != null) {
            createOrderSyncLog(
                    authUser.getStoreId(),
                    existing,
                    orderRequest.getLocalOrderId(),
                    deviceId,
                    "DUPLICATE",
                    "Order already synced",
                    syncedAt
            );

            return toDuplicateSyncResponse(orderRequest.getLocalOrderId(), existing, syncedAt);
        }

        SalesOrder order = orderService.createOrder(
                orderRequest.getLocalOrderId(),
                authUser.getStoreId(),
                orderRequest.getCustomerId(),
                authUser.getId(),
                deviceId,
                SyncStatus.OFFLINE_SYNCED,
                orderRequest.getOfflineCreatedAt(),
                orderRequest.getItems()
        );

        createOrderSyncLog(
                authUser.getStoreId(),
                order,
                orderRequest.getLocalOrderId(),
                deviceId,
                "APPLIED",
                "Offline order synced successfully",
                syncedAt
        );

        orderEventPublisher.publishPaymentRequested(order);

        return SyncItemResponseMapper.forOrder(
                orderRequest.getLocalOrderId(),
                order.getId(),
                "APPLIED",
                "Offline order synced successfully",
                order,
                syncedAt
        );
    }

    private SyncItemResponse handleDuplicateAfterRollback(
            OfflineOrderRequest orderRequest,
            String deviceId,
            AuthUserResponse authUser,
            LocalDateTime syncedAt
    ) {
        SalesOrder existing = findExistingOfflineOrder(orderRequest.getLocalOrderId());

        if (existing != null) {
            createOrderSyncLog(
                    authUser.getStoreId(),
                    existing,
                    orderRequest.getLocalOrderId(),
                    deviceId,
                    "DUPLICATE",
                    "Order already synced",
                    syncedAt
            );

            return toDuplicateSyncResponse(orderRequest.getLocalOrderId(), existing, syncedAt);
        }

        return handleRejectedAfterRollback(
                orderRequest,
                deviceId,
                authUser,
                syncedAt,
                "Order could not be synced because of a database constraint"
        );
    }

    private SyncItemResponse handleRejectedAfterRollback(
            OfflineOrderRequest orderRequest,
            String deviceId,
            AuthUserResponse authUser,
            LocalDateTime syncedAt,
            String message
    ) {
        String safeMessage = message != null ? message : "Order rejected";

        createOrderSyncLog(
                authUser.getStoreId(),
                null,
                orderRequest.getLocalOrderId(),
                deviceId,
                "REJECTED",
                safeMessage,
                syncedAt
        );

        return SyncItemResponseMapper.forOrder(
                orderRequest.getLocalOrderId(),
                null,
                "REJECTED",
                safeMessage,
                null,
                syncedAt
        );
    }

    private SalesOrder findExistingOfflineOrder(String localOrderId) {
        if (localOrderId == null || localOrderId.isBlank()) {
            return null;
        }

        return salesOrderRepository.findByLocalOrderId(localOrderId).orElse(null);
    }

    private SyncItemResponse toDuplicateSyncResponse(String localOrderId, SalesOrder existing, LocalDateTime syncedAt) {
        return SyncItemResponseMapper.forOrder(
                localOrderId,
                existing.getId(),
                "DUPLICATE",
                "Order already synced",
                existing,
                syncedAt
        );
    }

    private void createOrderSyncLog(
            UUID storeId,
            SalesOrder order,
            String localOrderId,
            String deviceId,
            String status,
            String message,
            LocalDateTime syncedAt
    ) {
        OrderSyncLog log = OrderSyncLog.builder()
                .storeId(storeId)
                .order(order)
                .localOrderId(localOrderId)
                .deviceId(deviceId)
                .status(status)
                .message(message != null && message.length() > 255 ? message.substring(0, 255) : message)
                .syncedAt(syncedAt)
                .build();

        orderSyncLogRepository.save(log);
    }
}
