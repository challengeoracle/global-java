package br.com.signal.signal_payment_service.payment.messaging;

import br.com.signal.signal_payment_service.payment.messaging.event.PaymentProcessedEvent;
import br.com.signal.signal_payment_service.payment.messaging.outbox.PaymentProcessedOutbox;
import br.com.signal.signal_payment_service.payment.messaging.outbox.PaymentProcessedOutboxRepository;
import br.com.signal.signal_payment_service.payment.messaging.outbox.PaymentProcessedOutboxStatus;
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
public class PaymentProcessedOutboxService {

    private static final String PAYMENT_PROCESSED_EVENT_TYPE = "PAYMENT_PROCESSED";

    private final PaymentProcessedOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final PaymentProcessedOutboxDispatcher paymentProcessedOutboxDispatcher;

    @Transactional
    public void enqueue(PaymentProcessedEvent event) {
        PaymentProcessedOutbox outbox = outboxRepository.findByOrderId(event.orderId())
                .orElseGet(() -> createOutbox(event.orderId(), serialize(event)));

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        paymentProcessedOutboxDispatcher.dispatchSingle(outbox.getId());
                    } catch (RuntimeException ex) {
                        log.warn("Immediate PaymentProcessedEvent dispatch failed for order {}. Scheduled retry will handle it.", outbox.getOrderId(), ex);
                    }
                }
            });
        } else {
            paymentProcessedOutboxDispatcher.dispatchSingle(outbox.getId());
        }
    }

    private PaymentProcessedOutbox createOutbox(UUID orderId, String payloadJson) {
        LocalDateTime now = LocalDateTime.now();

        try {
            return outboxRepository.save(
                    PaymentProcessedOutbox.builder()
                            .orderId(orderId)
                            .eventType(PAYMENT_PROCESSED_EVENT_TYPE)
                            .payloadJson(payloadJson)
                            .status(PaymentProcessedOutboxStatus.PENDING)
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

    private String serialize(PaymentProcessedEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize payment processed event", ex);
        }
    }
}
