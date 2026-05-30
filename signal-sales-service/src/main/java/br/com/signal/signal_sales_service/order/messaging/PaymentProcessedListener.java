package br.com.signal.signal_sales_service.order.messaging;

import br.com.signal.signal_sales_service.order.entity.enums.PaymentStatus;
import br.com.signal.signal_sales_service.order.messaging.event.PaymentProcessedEvent;
import br.com.signal.signal_sales_service.order.service.OrderService;
import br.com.signal.signal_sales_service.shared.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentProcessedListener {

    private final OrderService orderService;

    @RabbitListener(queues = RabbitMQConfig.SALES_PAYMENT_PROCESSED_QUEUE)
    public void listen(PaymentProcessedEvent event) {
        log.info("PaymentProcessedEvent received for order {} with status {}", event.getOrderId(), event.getStatus());

        if (event.getOrderId() == null) {
            log.warn("Ignoring PaymentProcessedEvent without orderId");
            return;
        }

        PaymentStatus paymentStatus = mapPaymentStatus(event.getStatus());

        orderService.updatePaymentStatus(event.getOrderId(), paymentStatus);

        log.info("Order {} paymentStatus updated to {}", event.getOrderId(), paymentStatus);
    }

    private PaymentStatus mapPaymentStatus(String status) {
        if ("APPROVED".equalsIgnoreCase(status)) {
            return PaymentStatus.PAID;
        }

        if ("REJECTED".equalsIgnoreCase(status)) {
            return PaymentStatus.REJECTED;
        }

        log.warn("Unknown payment status received: {}. Falling back to REJECTED", status);
        return PaymentStatus.REJECTED;
    }
}