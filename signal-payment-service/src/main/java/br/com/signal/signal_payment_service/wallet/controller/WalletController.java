package br.com.signal.signal_payment_service.wallet.controller;

import br.com.signal.signal_payment_service.wallet.dto.request.DepositRequest;
import br.com.signal.signal_payment_service.wallet.dto.request.SettleWalletRequest;
import br.com.signal.signal_payment_service.wallet.dto.response.WalletResponse;
import br.com.signal.signal_payment_service.wallet.dto.response.WalletTransactionResponse;
import br.com.signal.signal_payment_service.shared.dto.response.PageResponse;
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

    @GetMapping("/personal/me")
    public WalletResponse findMyPersonalWallet(
            @RequestHeader("Authorization") String authorization
    ) {
        return walletService.findMyPersonalWallet(authorization);
    }

    @PostMapping("/deposit")
    public WalletResponse deposit(
            @RequestHeader("Authorization") String authorization,
            @RequestBody @Valid DepositRequest request
    ) {
        return walletService.deposit(authorization, request);
    }

    @PostMapping("/settle")
    public WalletResponse settle(
            @RequestHeader("Authorization") String authorization,
            @RequestBody @Valid SettleWalletRequest request
    ) {
        return walletService.settle(authorization, request);
    }

    @GetMapping("/transactions/me")
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
