package br.com.signal.signal_sales_service.order.messaging;

import br.com.signal.signal_sales_service.order.entity.SalesOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventPublisher {

    private final PaymentRequestOutboxService paymentRequestOutboxService;

    public void publishPaymentRequested(SalesOrder order) {
        paymentRequestOutboxService.enqueuePaymentRequested(order);
        log.info("PaymentRequestedEvent queued for order {}", order.getId());
    }
}
