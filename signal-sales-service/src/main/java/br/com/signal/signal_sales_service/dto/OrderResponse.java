package br.com.signal.signal_sales_service.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

    private UUID id;
    private String localOrderId;
    private UUID storeId;
    private UUID customerId;
    private UUID sellerId;
    private String deviceId;
    private String orderStatus;
    private String paymentStatus;
    private String syncStatus;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private LocalDateTime offlineCreatedAt;
    private List<OrderItemResponse> items;
}