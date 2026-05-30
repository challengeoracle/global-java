package br.com.signal.signal_payment_service.wallet.controller;

import br.com.signal.signal_payment_service.wallet.dto.request.DepositRequest;
import br.com.signal.signal_payment_service.wallet.dto.response.WalletResponse;
import br.com.signal.signal_payment_service.wallet.dto.response.WalletTransactionResponse;
import br.com.signal.signal_payment_service.wallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/me")
    public WalletResponse findMyWallet(
            @RequestHeader("Authorization") String authorization
    ) {
        return walletService.findMyWallet(authorization);
    }

    @PostMapping("/deposit")
    public WalletResponse deposit(
            @RequestHeader("Authorization") String authorization,
            @RequestBody @Valid DepositRequest request
    ) {
        return walletService.deposit(authorization, request);
    }

    @GetMapping("/transactions/me")
    public List<WalletTransactionResponse> findMyTransactions(
            @RequestHeader("Authorization") String authorization
    ) {
        return walletService.findMyTransactions(authorization);
    }
}