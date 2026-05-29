package br.com.signal.signal_sales_service.service;

import br.com.signal.signal_sales_service.client.AuthClient;
import br.com.signal.signal_sales_service.dto.*;
import br.com.signal.signal_sales_service.entity.OrderSyncLog;
import br.com.signal.signal_sales_service.entity.Product;
import br.com.signal.signal_sales_service.entity.SalesOrder;
import br.com.signal.signal_sales_service.entity.SalesOrderItem;
import br.com.signal.signal_sales_service.entity.enums.OrderStatus;
import br.com.signal.signal_sales_service.entity.enums.PaymentStatus;
import br.com.signal.signal_sales_service.entity.enums.SyncStatus;
import br.com.signal.signal_sales_service.exception.BadRequestException;
import br.com.signal.signal_sales_service.exception.NotFoundException;
import br.com.signal.signal_sales_service.messaging.OrderEventPublisher;
import br.com.signal.signal_sales_service.repository.OrderSyncLogRepository;
import br.com.signal.signal_sales_service.repository.ProductRepository;
import br.com.signal.signal_sales_service.repository.SalesOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final AuthClient authClient;
    private final ProductRepository productRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final OrderSyncLogRepository orderSyncLogRepository;
    private final OrderEventPublisher orderEventPublisher;

    @Transactional
    public OrderResponse createOnlineOrder(CreateOrderRequest request, String authorization) {
        AuthUserResponse authUser = authClient.me(authorization);

        UUID storeId = resolveStoreId(request.getStoreId(), authUser);
        UUID customerId = "CUSTOMER".equals(authUser.getRole()) ? authUser.getId() : null;
        UUID sellerId = "SELLER".equals(authUser.getRole()) ? authUser.getId() : null;

        SalesOrder order = createOrder(
                null,
                storeId,
                customerId,
                sellerId,
                request.getDeviceId(),
                SyncStatus.ONLINE,
                null,
                request.getItems()
        );

        orderEventPublisher.publishPaymentRequested(order);

        return toResponse(order);
    }

    @Transactional
    public OrderSyncResponse syncOfflineOrders(OrderSyncRequest request, String authorization) {
        AuthUserResponse authUser = authClient.me(authorization);

        if (!"SELLER".equals(authUser.getRole())) {
            throw new BadRequestException("Only sellers can sync offline orders");
        }

        if (authUser.getStoreId() == null) {
            throw new BadRequestException("Seller does not have a store");
        }

        LocalDateTime syncedAt = LocalDateTime.now();

        List<OfflineOrderRequest> uniqueOrders = request.getOrders()
                .stream()
                .collect(Collectors.toMap(
                        OfflineOrderRequest::getLocalOrderId,
                        order -> order,
                        (first, duplicate) -> first,
                        LinkedHashMap::new
                ))
                .values()
                .stream()
                .toList();

        List<OrderSyncItemResponse> results = uniqueOrders
                .stream()
                .map(orderRequest -> syncSingleOfflineOrder(
                        orderRequest,
                        request.getDeviceId(),
                        authUser,
                        syncedAt
                ))
                .toList();

        return OrderSyncResponse.builder()
                .storeId(authUser.getStoreId())
                .syncedAt(syncedAt)
                .results(results)
                .build();
    }

    public OrderResponse findById(UUID id) {
        SalesOrder order = salesOrderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        return toResponse(order);
    }

    public List<OrderResponse> findByStore(UUID storeId) {
        return salesOrderRepository.findByStoreIdOrderByCreatedAtDesc(storeId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<OrderResponse> findByCustomer(UUID customerId) {
        return salesOrderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<OrderResponse> findMyOrders(String authorization) {
        AuthUserResponse authUser = authClient.me(authorization);

        if ("SELLER".equals(authUser.getRole())) {
            if (authUser.getStoreId() == null) {
                throw new BadRequestException("Seller does not have a store");
            }

            return findByStore(authUser.getStoreId());
        }

        if ("CUSTOMER".equals(authUser.getRole())) {
            return findByCustomer(authUser.getId());
        }

        throw new BadRequestException("Invalid user role");
    }

    @Transactional
    public OrderResponse updatePaymentStatus(UUID orderId, PaymentStatus paymentStatus) {
        SalesOrder order = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        order.setPaymentStatus(paymentStatus);
        order.setUpdatedAt(LocalDateTime.now());

        salesOrderRepository.save(order);

        return toResponse(order);
    }

    private OrderSyncItemResponse syncSingleOfflineOrder(
            OfflineOrderRequest orderRequest,
            String deviceId,
            AuthUserResponse authUser,
            LocalDateTime syncedAt
    ) {
        try {
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

                return toDuplicateSyncResponse(orderRequest.getLocalOrderId(), existing);
            }

            SalesOrder order = createOrder(
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

            return OrderSyncItemResponse.builder()
                    .localOrderId(orderRequest.getLocalOrderId())
                    .orderId(order.getId())
                    .status("APPLIED")
                    .message("Offline order synced successfully")
                    .orderStatus(order.getOrderStatus().name())
                    .paymentStatus(order.getPaymentStatus().name())
                    .syncStatus(order.getSyncStatus().name())
                    .totalAmount(order.getTotalAmount())
                    .build();
        } catch (DataIntegrityViolationException ex) {
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

                return toDuplicateSyncResponse(orderRequest.getLocalOrderId(), existing);
            }

            createOrderSyncLog(
                    authUser.getStoreId(),
                    null,
                    orderRequest.getLocalOrderId(),
                    deviceId,
                    "REJECTED",
                    "Order could not be synced because of a database constraint",
                    syncedAt
            );

            return OrderSyncItemResponse.builder()
                    .localOrderId(orderRequest.getLocalOrderId())
                    .status("REJECTED")
                    .message("Order could not be synced because of a database constraint")
                    .build();
        } catch (RuntimeException ex) {
            createOrderSyncLog(
                    authUser.getStoreId(),
                    null,
                    orderRequest.getLocalOrderId(),
                    deviceId,
                    "REJECTED",
                    ex.getMessage(),
                    syncedAt
            );

            return OrderSyncItemResponse.builder()
                    .localOrderId(orderRequest.getLocalOrderId())
                    .status("REJECTED")
                    .message(ex.getMessage())
                    .build();
        }
    }

    private SalesOrder findExistingOfflineOrder(String localOrderId) {
        if (localOrderId == null || localOrderId.isBlank()) {
            return null;
        }

        return salesOrderRepository.findByLocalOrderId(localOrderId).orElse(null);
    }

    private OrderSyncItemResponse toDuplicateSyncResponse(String localOrderId, SalesOrder existing) {
        return OrderSyncItemResponse.builder()
                .localOrderId(localOrderId)
                .orderId(existing.getId())
                .status("DUPLICATE")
                .message("Order already synced")
                .orderStatus(existing.getOrderStatus().name())
                .paymentStatus(existing.getPaymentStatus().name())
                .syncStatus(existing.getSyncStatus().name())
                .totalAmount(existing.getTotalAmount())
                .build();
    }

    private SalesOrder createOrder(
            String localOrderId,
            UUID storeId,
            UUID customerId,
            UUID sellerId,
            String deviceId,
            SyncStatus syncStatus,
            LocalDateTime offlineCreatedAt,
            List<OrderItemRequest> itemRequests
    ) {
        if (itemRequests == null || itemRequests.isEmpty()) {
            throw new BadRequestException("Order must have at least one item");
        }

        SalesOrder order = SalesOrder.builder()
                .localOrderId(localOrderId)
                .storeId(storeId)
                .customerId(customerId)
                .sellerId(sellerId)
                .deviceId(deviceId)
                .orderStatus(OrderStatus.CREATED)
                .paymentStatus(PaymentStatus.PENDING_PAYMENT)
                .syncStatus(syncStatus)
                .totalAmount(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .offlineCreatedAt(offlineCreatedAt)
                .build();

        List<SalesOrderItem> items = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : itemRequests) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new NotFoundException("Product not found"));

            if (!product.getStoreId().equals(storeId)) {
                throw new BadRequestException("Product does not belong to this store");
            }

            if (!Boolean.TRUE.equals(product.getActive())) {
                throw new BadRequestException("Product is inactive");
            }

            if (itemRequest.getQuantity() == null || itemRequest.getQuantity() <= 0) {
                throw new BadRequestException("Quantity must be greater than zero");
            }

            if (product.getStockQuantity() < itemRequest.getQuantity()) {
                throw new BadRequestException("Insufficient stock for product " + product.getName());
            }

            BigDecimal unitPrice = itemRequest.getUnitPrice() != null
                    ? itemRequest.getUnitPrice()
                    : product.getPrice();

            if (unitPrice.signum() <= 0) {
                throw new BadRequestException("Unit price must be greater than zero");
            }

            BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

            product.setStockQuantity(product.getStockQuantity() - itemRequest.getQuantity());
            product.setUpdatedAt(LocalDateTime.now());

            SalesOrderItem orderItem = SalesOrderItem.builder()
                    .order(order)
                    .product(product)
                    .productName(product.getName())
                    .unitPrice(unitPrice)
                    .quantity(itemRequest.getQuantity())
                    .totalPrice(itemTotal)
                    .build();

            items.add(orderItem);
            totalAmount = totalAmount.add(itemTotal);
        }

        order.setItems(items);
        order.setTotalAmount(totalAmount);

        return salesOrderRepository.saveAndFlush(order);
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

    private UUID resolveStoreId(UUID requestStoreId, AuthUserResponse authUser) {
        if ("SELLER".equals(authUser.getRole())) {
            if (authUser.getStoreId() == null) {
                throw new BadRequestException("Seller does not have a store");
            }

            return authUser.getStoreId();
        }

        if (requestStoreId == null) {
            throw new BadRequestException("Store id is required");
        }

        return requestStoreId;
    }

    private OrderResponse toResponse(SalesOrder order) {
        return OrderResponse.builder()
                .id(order.getId())
                .localOrderId(order.getLocalOrderId())
                .storeId(order.getStoreId())
                .customerId(order.getCustomerId())
                .sellerId(order.getSellerId())
                .deviceId(order.getDeviceId())
                .orderStatus(order.getOrderStatus().name())
                .paymentStatus(order.getPaymentStatus().name())
                .syncStatus(order.getSyncStatus().name())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .offlineCreatedAt(order.getOfflineCreatedAt())
                .items(
                        order.getItems()
                                .stream()
                                .map(this::toItemResponse)
                                .toList()
                )
                .build();
    }

    private OrderItemResponse toItemResponse(SalesOrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProductName())
                .unitPrice(item.getUnitPrice())
                .quantity(item.getQuantity())
                .totalPrice(item.getTotalPrice())
                .build();
    }
}