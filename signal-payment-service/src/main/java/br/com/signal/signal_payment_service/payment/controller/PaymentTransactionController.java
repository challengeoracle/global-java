package br.com.signal.signal_payment_service.payment.controller;

import br.com.signal.signal_payment_service.payment.dto.response.PaymentTransactionResponse;
import br.com.signal.signal_payment_service.payment.service.PaymentTransactionQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/payment/transactions")
@RequiredArgsConstructor
public class PaymentTransactionController {

    private final PaymentTransactionQueryService paymentTransactionQueryService;

    @GetMapping("/me")
    public List<PaymentTransactionResponse> findMyTransactions(
            @RequestHeader("Authorization") String authorization
    ) {
        return paymentTransactionQueryService.findMyTransactions(authorization);
    }

    @GetMapping("/order/{orderId}")
    public PaymentTransactionResponse findByOrderId(
            @RequestHeader("Authorization") String authorization,
            @PathVariable UUID orderId
    ) {
        return paymentTransactionQueryService.findByOrderId(authorization, orderId);
    }
}