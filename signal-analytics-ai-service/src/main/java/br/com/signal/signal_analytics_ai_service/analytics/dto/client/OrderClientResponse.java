package br.com.signal.signal_analytics_ai_service.analytics.dto.client;

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
public class OrderClientResponse {

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
    private LocalDateTime updatedAt;
    private LocalDateTime offlineCreatedAt;

    private List<OrderItemClientResponse> items;
}