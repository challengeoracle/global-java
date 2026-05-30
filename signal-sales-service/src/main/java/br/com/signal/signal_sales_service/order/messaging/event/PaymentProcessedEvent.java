package br.com.signal.signal_sales_service.order.messaging.event;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentProcessedEvent {

    private UUID orderId;
    private String localOrderId;
    private UUID transactionId;
    private UUID storeId;
    private BigDecimal amount;
    private String status;
    private String failureReason;
    private LocalDateTime processedAt;
}