package br.com.signal.signal_payment_service.payment.messaging;

import br.com.signal.signal_payment_service.payment.messaging.event.PaymentRequestedEvent;
import br.com.signal.signal_payment_service.payment.service.PaymentProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentRequestedListener {

    private final PaymentProcessingService paymentProcessingService;

    @RabbitListener(queues = "${offpay.rabbit.payment-requested-queue}")
    public void listen(PaymentRequestedEvent event) {
        log.info("PaymentRequestedEvent received for order {}", event.orderId());
        paymentProcessingService.process(event);
    }
}