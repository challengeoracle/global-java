package br.com.signal.signal_sales_service.order.messaging.outbox;

public enum PaymentEventOutboxStatus {
    PENDING,
    FAILED,
    SENT
}
