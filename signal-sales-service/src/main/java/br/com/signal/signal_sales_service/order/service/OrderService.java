package br.com.signal.signal_sales_service.order.service;

import br.com.signal.signal_sales_service.catalog.entity.Product;
import br.com.signal.signal_sales_service.catalog.repository.ProductRepository;
import br.com.signal.signal_sales_service.order.dto.request.CreateOrderRequest;
import br.com.signal.signal_sales_service.order.dto.request.OrderItemRequest;
import br.com.signal.signal_sales_service.order.dto.response.OrderResponse;
import br.com.signal.signal_sales_service.order.entity.SalesOrder;
import br.com.signal.signal_sales_service.order.entity.SalesOrderItem;
import br.com.signal.signal_sales_service.order.entity.enums.OrderStatus;
import br.com.signal.signal_sales_service.order.entity.enums.PaymentStatus;
import br.com.signal.signal_sales_service.order.entity.enums.SyncStatus;
import br.com.signal.signal_sales_service.order.mapper.OrderMapper;
import br.com.signal.signal_sales_service.order.messaging.OrderEventPublisher;
import br.com.signal.signal_sales_service.order.repository.SalesOrderRepository;
import br.com.signal.signal_sales_service.shared.dto.response.AuthUserResponse;
import br.com.signal.signal_sales_service.shared.exception.BadRequestException;
import br.com.signal.signal_sales_service.shared.exception.ForbiddenException;
import br.com.signal.signal_sales_service.shared.exception.NotFoundException;
import br.com.signal.signal_sales_service.shared.service.AuthIdentityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final AuthIdentityService authIdentityService;
    private final ProductRepository productRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final OrderEventPublisher orderEventPublisher;

    @Transactional
    public OrderResponse createOnlineOrder(CreateOrderRequest request, String authorization) {
        AuthUserResponse authUser = authIdentityService.requireCustomer(authorization);

        if (request.getStoreId() == null) {
            throw new BadRequestException("Store id is required");
        }

        SalesOrder order = createOrder(
                null,
                request.getStoreId(),
                authUser.getId(),
                null,
                request.getDeviceId(),
                SyncStatus.ONLINE,
                null,
                request.getItems()
        );

        orderEventPublisher.publishPaymentRequested(order);

        return OrderMapper.toResponse(order);
    }

    public OrderResponse findById(UUID id, String authorization) {
        AuthUserResponse authUser = authIdentityService.me(authorization);
        SalesOrder order = salesOrderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        assertOrderAccess(authUser, order);

        return OrderMapper.toResponse(order);
    }

    public List<OrderResponse> findByStore(UUID storeId, String authorization) {
        AuthUserResponse authUser = authIdentityService.requireSeller(authorization);

        if (!authUser.getStoreId().equals(storeId)) {
            throw new ForbiddenException("Cannot access orders from another store");
        }

        return salesOrderRepository.findByStoreIdOrderByCreatedAtDesc(storeId)
                .stream()
                .map(OrderMapper::toResponse)
                .toList();
    }

    public List<OrderResponse> findByCustomer(UUID customerId, String authorization) {
        AuthUserResponse authUser = authIdentityService.requireCustomer(authorization);

        if (!authUser.getId().equals(customerId)) {
            throw new ForbiddenException("Cannot access orders from another customer");
        }

        return salesOrderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(OrderMapper::toResponse)
                .toList();
    }

    public List<OrderResponse> findMyOrders(String authorization) {
        AuthUserResponse authUser = authIdentityService.me(authorization);

        if ("SELLER".equals(authUser.getRole())) {
            if (authUser.getStoreId() == null) {
                throw new BadRequestException("Seller does not have a store");
            }

            return salesOrderRepository.findByStoreIdOrderByCreatedAtDesc(authUser.getStoreId())
                    .stream()
                    .map(OrderMapper::toResponse)
                    .toList();
        }

        if ("CUSTOMER".equals(authUser.getRole())) {
            return salesOrderRepository.findByCustomerIdOrderByCreatedAtDesc(authUser.getId())
                    .stream()
                    .map(OrderMapper::toResponse)
                    .toList();
        }

        throw new ForbiddenException("Invalid user role");
    }

    @Transactional
    public OrderResponse updatePaymentStatus(UUID orderId, PaymentStatus paymentStatus) {
        SalesOrder order = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        order.setPaymentStatus(paymentStatus);
        order.setUpdatedAt(LocalDateTime.now());

        salesOrderRepository.save(order);

        return OrderMapper.toResponse(order);
    }

    @Transactional
    public SalesOrder createOrder(
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

    private void assertOrderAccess(AuthUserResponse authUser, SalesOrder order) {
        if ("SELLER".equals(authUser.getRole())) {
            if (authUser.getStoreId() == null || !authUser.getStoreId().equals(order.getStoreId())) {
                throw new ForbiddenException("Cannot access orders from another store");
            }
            return;
        }

        if ("CUSTOMER".equals(authUser.getRole())) {
            if (order.getCustomerId() == null || !authUser.getId().equals(order.getCustomerId())) {
                throw new ForbiddenException("Cannot access orders from another customer");
            }
            return;
        }

        throw new ForbiddenException("Invalid user role");
    }
}
