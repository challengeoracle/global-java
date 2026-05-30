package br.com.signal.signal_payment_service.payment.messaging.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentProcessedEvent(
        UUID orderId,
        String localOrderId,
        UUID transactionId,
        UUID storeId,
        BigDecimal amount,
        String status,
        String failureReason,
        LocalDateTime processedAt
) {
}