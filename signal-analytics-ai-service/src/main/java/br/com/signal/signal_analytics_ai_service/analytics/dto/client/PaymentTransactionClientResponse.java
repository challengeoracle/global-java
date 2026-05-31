package br.com.signal.signal_analytics_ai_service.analytics.dto.client;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransactionClientResponse {

    private UUID id;
    private UUID orderId;
    private String localOrderId;
    private UUID customerId;
    private UUID sellerId;
    private UUID storeId;
    private BigDecimal amount;
    private String status;
    private String failureReason;
    private String gatewayReference;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
}