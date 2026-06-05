package br.com.signal.signal_payment_service.payment.controller;

import br.com.signal.signal_payment_service.payment.dto.response.PaymentTransactionResponse;
import br.com.signal.signal_payment_service.payment.service.PaymentDebtService;
import br.com.signal.signal_payment_service.payment.service.PaymentTransactionQueryService;
import br.com.signal.signal_payment_service.shared.dto.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/payment/transactions")
@RequiredArgsConstructor
@Tag(name = "Payment Transactions", description = "Consulta e liquidacao de transacoes financeiras do OffPay.")
@SecurityRequirement(name = "bearerAuth")
public class PaymentTransactionController {

    private final PaymentTransactionQueryService paymentTransactionQueryService;
    private final PaymentDebtService paymentDebtService;

    @GetMapping("/me")
    @Operation(summary = "Listar minhas transacoes", description = "Retorna as transacoes do usuario autenticado.")
    public List<PaymentTransactionResponse> findMyTransactions(
            @RequestHeader("Authorization") String authorization
    ) {
        return paymentTransactionQueryService.findMyTransactions(authorization);
    }

    @GetMapping("/me/page")
    public PageResponse<PaymentTransactionResponse> findMyTransactionsPage(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return paymentTransactionQueryService.findMyTransactionsPage(authorization, page, size);
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Consultar transacao por pedido", description = "Retorna a transacao vinculada a um pedido especifico.")
    public PaymentTransactionResponse findByOrderId(
            @RequestHeader("Authorization") String authorization,
            @PathVariable UUID orderId
    ) {
        return paymentTransactionQueryService.findByOrderId(authorization, orderId);
    }

    @PostMapping("/order/{orderId}/settle-debt")
    @Operation(summary = "Liquidar debito do pedido", description = "Realiza a liquidacao do debito pendente associado ao pedido informado.")
    public PaymentTransactionResponse settleCreditDebt(
            @RequestHeader("Authorization") String authorization,
            @PathVariable UUID orderId
    ) {
        return paymentDebtService.settleCreditDebt(authorization, orderId);
    }
}
