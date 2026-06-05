package br.com.signal.signal_payment_service.wallet.controller;

import br.com.signal.signal_payment_service.wallet.dto.request.DepositRequest;
import br.com.signal.signal_payment_service.wallet.dto.request.SettleWalletRequest;
import br.com.signal.signal_payment_service.wallet.dto.response.WalletResponse;
import br.com.signal.signal_payment_service.wallet.dto.response.WalletTransactionResponse;
import br.com.signal.signal_payment_service.shared.dto.response.PageResponse;
import br.com.signal.signal_payment_service.wallet.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/wallet")
@RequiredArgsConstructor
@Tag(name = "Wallet", description = "Operacoes de carteira, saldo e movimentacoes financeiras.")
@SecurityRequirement(name = "bearerAuth")
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/me")
    @Operation(summary = "Consultar minha carteira", description = "Retorna os dados consolidados da carteira principal do usuario autenticado.")
    public WalletResponse findMyWallet(
            @RequestHeader("Authorization") String authorization
    ) {
        return walletService.findMyWallet(authorization);
    }

    @GetMapping("/personal/me")
    @Operation(summary = "Consultar carteira pessoal", description = "Retorna a carteira pessoal do usuario autenticado.")
    public WalletResponse findMyPersonalWallet(
            @RequestHeader("Authorization") String authorization
    ) {
        return walletService.findMyPersonalWallet(authorization);
    }

    @PostMapping("/deposit")
    @Operation(summary = "Realizar deposito", description = "Adiciona saldo na carteira do usuario autenticado.")
    public WalletResponse deposit(
            @RequestHeader("Authorization") String authorization,
            @RequestBody @Valid DepositRequest request
    ) {
        return walletService.deposit(authorization, request);
    }

    @PostMapping("/settle")
    @Operation(summary = "Liquidar carteira", description = "Executa uma operacao de liquidacao de saldo na carteira.")
    public WalletResponse settle(
            @RequestHeader("Authorization") String authorization,
            @RequestBody @Valid SettleWalletRequest request
    ) {
        return walletService.settle(authorization, request);
    }

    @GetMapping("/transactions/me")
    @Operation(summary = "Listar movimentacoes da carteira", description = "Retorna as movimentacoes da carteira principal do usuario autenticado.")
    public List<WalletTransactionResponse> findMyTransactions(
            @RequestHeader("Authorization") String authorization
    ) {
        return walletService.findMyTransactions(authorization);
    }

    @GetMapping("/transactions/me/page")
    public PageResponse<WalletTransactionResponse> findMyTransactionsPage(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return walletService.findMyTransactionsPage(authorization, page, size);
    }

    @GetMapping("/transactions/personal/me")
    public List<WalletTransactionResponse> findMyPersonalTransactions(
            @RequestHeader("Authorization") String authorization
    ) {
        return walletService.findMyPersonalTransactions(authorization);
    }

    @GetMapping("/transactions/personal/me/page")
    public PageResponse<WalletTransactionResponse> findMyPersonalTransactionsPage(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return walletService.findMyPersonalTransactionsPage(authorization, page, size);
    }
}
