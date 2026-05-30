package br.com.signal.signal_payment_service.payment.messaging.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentRequestedEvent(
        UUID orderId,
        String localOrderId,
        UUID storeId,
        UUID customerId,
        UUID sellerId,
        BigDecimal totalAmount,
        String paymentStatus,
        String syncStatus,
        LocalDateTime createdAt
) {
}