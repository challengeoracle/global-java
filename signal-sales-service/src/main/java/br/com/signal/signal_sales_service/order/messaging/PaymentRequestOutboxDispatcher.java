package br.com.signal.signal_sales_service.order.messaging;

import br.com.signal.signal_sales_service.order.messaging.event.PaymentRequestedEvent;
import br.com.signal.signal_sales_service.order.messaging.outbox.PaymentEventOutbox;
import br.com.signal.signal_sales_service.order.messaging.outbox.PaymentEventOutboxRepository;
import br.com.signal.signal_sales_service.order.messaging.outbox.PaymentEventOutboxStatus;
import br.com.signal.signal_sales_service.shared.config.RabbitMQConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentRequestOutboxDispatcher {

    private static final int MAX_ERROR_LENGTH = 255;
    private static final int RETRY_BASE_SECONDS = 15;
    private static final int RETRY_MAX_SECONDS = 300;

    private final PaymentEventOutboxRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Transactional
    @Scheduled(initialDelay = 15000, fixedDelay = 15000)
    public void retryPendingEvents() {
        for (UUID pendingId : outboxRepository.findRetryable(LocalDateTime.now()).stream().map(PaymentEventOutbox::getId).toList()) {
            dispatchSingle(pendingId);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void dispatchSingle(UUID outboxId) {
        outboxRepository.findByIdForUpdate(outboxId).ifPresent(outbox -> {
            if (outbox.getStatus() == PaymentEventOutboxStatus.SENT) {
                return;
            }

            try {
                PaymentRequestedEvent event = objectMapper.readValue(outbox.getPayloadJson(), PaymentRequestedEvent.class);

                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.SALES_EXCHANGE,
                        RabbitMQConfig.PAYMENT_REQUESTED_ROUTING_KEY,
                        event
                );

                outbox.setStatus(PaymentEventOutboxStatus.SENT);
                outbox.setSentAt(LocalDateTime.now());
                outbox.setNextAttemptAt(LocalDateTime.now());
                outbox.setLastError(null);
                outboxRepository.save(outbox);
                log.info("PaymentRequestedEvent delivered for order {}", outbox.getOrderId());
            } catch (AmqpException | JsonProcessingException ex) {
                int attempts = (outbox.getAttempts() == null ? 0 : outbox.getAttempts()) + 1;
                int retryDelaySeconds = Math.min(RETRY_BASE_SECONDS * (1 << Math.min(attempts - 1, 4)), RETRY_MAX_SECONDS);

                outbox.setAttempts(attempts);
                outbox.setStatus(PaymentEventOutboxStatus.FAILED);
                outbox.setNextAttemptAt(LocalDateTime.now().plusSeconds(retryDelaySeconds));
                outbox.setLastError(truncateError(ex.getMessage()));
                outboxRepository.save(outbox);
                log.warn("PaymentRequestedEvent delivery failed for order {}. attempt={}", outbox.getOrderId(), attempts, ex);
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
