package br.com.signal.signal_payment_service.payment.messaging;

import br.com.signal.signal_payment_service.payment.messaging.event.PaymentProcessedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentProcessedPublisher {

    private final PaymentProcessedOutboxService paymentProcessedOutboxService;

    public void publish(PaymentProcessedEvent event) {
        paymentProcessedOutboxService.enqueue(event);
        log.info("PaymentProcessedEvent queued for order {}", event.orderId());
    }
}
