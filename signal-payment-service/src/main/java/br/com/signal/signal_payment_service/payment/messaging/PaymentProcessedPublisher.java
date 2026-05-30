package br.com.signal.signal_payment_service.payment.messaging;

import br.com.signal.signal_payment_service.payment.messaging.event.PaymentProcessedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentProcessedPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${offpay.rabbit.payment-exchange}")
    private String paymentExchange;

    @Value("${offpay.rabbit.payment-processed-routing-key}")
    private String paymentProcessedRoutingKey;

    public void publish(PaymentProcessedEvent event) {
        try {
            rabbitTemplate.convertAndSend(paymentExchange, paymentProcessedRoutingKey, event);
            log.info("PaymentProcessedEvent published for order {}", event.orderId());
        } catch (Exception ex) {
            log.error("Failed to publish PaymentProcessedEvent for order {}", event.orderId(), ex);
        }
    }
}