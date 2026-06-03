package br.com.signal.signal_payment_service.payment.messaging;

import br.com.signal.signal_payment_service.payment.messaging.event.PaymentProcessedEvent;
import br.com.signal.signal_payment_service.payment.messaging.outbox.PaymentProcessedOutbox;
import br.com.signal.signal_payment_service.payment.messaging.outbox.PaymentProcessedOutboxRepository;
import br.com.signal.signal_payment_service.payment.messaging.outbox.PaymentProcessedOutboxStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentProcessedOutboxDispatcher {

    private static final int MAX_ERROR_LENGTH = 255;
    private static final int RETRY_BASE_SECONDS = 15;
    private static final int RETRY_MAX_SECONDS = 300;

    private final PaymentProcessedOutboxRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${offpay.rabbit.payment-exchange}")
    private String paymentExchange;

    @Value("${offpay.rabbit.payment-processed-routing-key}")
    private String paymentProcessedRoutingKey;

    @Transactional
    @Scheduled(initialDelay = 15000, fixedDelay = 15000)
    public void retryPendingEvents() {
        for (UUID pendingId : outboxRepository.findRetryable(LocalDateTime.now()).stream().map(PaymentProcessedOutbox::getId).toList()) {
            dispatchSingle(pendingId);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void dispatchSingle(UUID outboxId) {
        outboxRepository.findByIdForUpdate(outboxId).ifPresent(outbox -> {
            if (outbox.getStatus() == PaymentProcessedOutboxStatus.SENT) {
                return;
            }

            try {
                PaymentProcessedEvent event = objectMapper.readValue(outbox.getPayloadJson(), PaymentProcessedEvent.class);
                rabbitTemplate.convertAndSend(paymentExchange, paymentProcessedRoutingKey, event);

                outbox.setStatus(PaymentProcessedOutboxStatus.SENT);
                outbox.setSentAt(LocalDateTime.now());
                outbox.setNextAttemptAt(LocalDateTime.now());
                outbox.setLastError(null);
                outboxRepository.save(outbox);
                log.info("PaymentProcessedEvent delivered for order {}", outbox.getOrderId());
            } catch (AmqpException | JsonProcessingException ex) {
                int attempts = (outbox.getAttempts() == null ? 0 : outbox.getAttempts()) + 1;
                int retryDelaySeconds = Math.min(RETRY_BASE_SECONDS * (1 << Math.min(attempts - 1, 4)), RETRY_MAX_SECONDS);
                outbox.setAttempts(attempts);
                outbox.setStatus(PaymentProcessedOutboxStatus.FAILED);
                outbox.setNextAttemptAt(LocalDateTime.now().plusSeconds(retryDelaySeconds));
                outbox.setLastError(truncateError(ex.getMessage()));
                outboxRepository.save(outbox);
                log.warn("PaymentProcessedEvent delivery failed for order {}. attempt={}", outbox.getOrderId(), attempts, ex);
            }
        });
    }

    private String truncateError(String message) {
        if (message == null || message.length() <= MAX_ERROR_LENGTH) {
            return message;
        }
        return message.substring(0, MAX_ERROR_LENGTH);
    }
}
