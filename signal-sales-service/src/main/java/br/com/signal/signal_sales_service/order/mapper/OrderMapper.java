package br.com.signal.signal_sales_service.order.mapper;

import br.com.signal.signal_sales_service.order.dto.response.OrderItemResponse;
import br.com.signal.signal_sales_service.order.dto.response.OrderResponse;
import br.com.signal.signal_sales_service.order.entity.SalesOrder;
import br.com.signal.signal_sales_service.order.entity.SalesOrderItem;

public final class OrderMapper {

    private OrderMapper() {
    }

    public static OrderResponse toResponse(SalesOrder order) {
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
                .items(order.getItems().stream().map(OrderMapper::toItemResponse).toList())
                .build();
    }

    public static OrderItemResponse toItemResponse(SalesOrderItem item) {
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
