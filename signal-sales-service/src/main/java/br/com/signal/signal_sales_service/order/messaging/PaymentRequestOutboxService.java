package br.com.signal.signal_sales_service.order.messaging;

import br.com.signal.signal_sales_service.order.entity.SalesOrder;
import br.com.signal.signal_sales_service.order.messaging.event.PaymentRequestedEvent;
import br.com.signal.signal_sales_service.order.messaging.outbox.PaymentEventOutbox;
import br.com.signal.signal_sales_service.order.messaging.outbox.PaymentEventOutboxRepository;
import br.com.signal.signal_sales_service.order.messaging.outbox.PaymentEventOutboxStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentRequestOutboxService {

    private static final String PAYMENT_REQUESTED_EVENT_TYPE = "PAYMENT_REQUESTED";

    private final PaymentEventOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final PaymentRequestOutboxDispatcher paymentRequestOutboxDispatcher;

    @Transactional
    public void enqueuePaymentRequested(SalesOrder order) {
        PaymentRequestedEvent event = buildEvent(order);
        PaymentEventOutbox outbox = outboxRepository.findByOrderId(order.getId())
                .orElseGet(() -> createOutbox(order.getId(), serialize(event)));

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    paymentRequestOutboxDispatcher.dispatchSingle(outbox.getId());
                }
            });
        } else {
            paymentRequestOutboxDispatcher.dispatchSingle(outbox.getId());
        }
    }

    private PaymentEventOutbox createOutbox(UUID orderId, String payloadJson) {
        LocalDateTime now = LocalDateTime.now();

        try {
            return outboxRepository.save(
                    PaymentEventOutbox.builder()
                            .orderId(orderId)
                            .eventType(PAYMENT_REQUESTED_EVENT_TYPE)
                            .payloadJson(payloadJson)
                            .status(PaymentEventOutboxStatus.PENDING)
                            .attempts(0)
                            .nextAttemptAt(now)
                            .createdAt(now)
                            .updatedAt(now)
                            .build()
            );
        } catch (DataIntegrityViolationException ex) {
            return outboxRepository.findByOrderId(orderId).orElseThrow(() -> ex);
        }
    }

    private PaymentRequestedEvent buildEvent(SalesOrder order) {
        return PaymentRequestedEvent.builder()
                .orderId(order.getId())
                .localOrderId(order.getLocalOrderId())
                .storeId(order.getStoreId())
                .customerId(order.getCustomerId())
                .sellerId(order.getSellerId())
                .totalAmount(order.getTotalAmount())
                .paymentStatus(order.getPaymentStatus().name())
                .syncStatus(order.getSyncStatus().name())
                .createdAt(order.getCreatedAt())
                .build();
    }

    private String serialize(PaymentRequestedEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize payment event", ex);
        }
    }
}
