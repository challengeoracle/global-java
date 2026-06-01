package br.com.signal.signal_payment_service.payment.messaging.outbox;

public enum PaymentProcessedOutboxStatus {
    PENDING,
    FAILED,
    SENT
}
