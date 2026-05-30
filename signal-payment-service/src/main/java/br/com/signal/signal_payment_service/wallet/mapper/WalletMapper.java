package br.com.signal.signal_payment_service.wallet.mapper;

import br.com.signal.signal_payment_service.wallet.dto.response.WalletResponse;
import br.com.signal.signal_payment_service.wallet.dto.response.WalletTransactionResponse;
import br.com.signal.signal_payment_service.wallet.entity.Wallet;
import br.com.signal.signal_payment_service.wallet.entity.WalletTransaction;
import org.springframework.stereotype.Component;

@Component
public class WalletMapper {

    public WalletResponse toResponse(Wallet wallet) {
        return WalletResponse.builder()
                .id(wallet.getId())
                .ownerId(wallet.getOwnerId())
                .ownerType(wallet.getOwnerType())
                .balance(wallet.getBalance())
                .pendingBalance(wallet.getPendingBalance())
                .createdAt(wallet.getCreatedAt())
                .updatedAt(wallet.getUpdatedAt())
                .build();
    }

    public WalletTransactionResponse toTransactionResponse(WalletTransaction transaction) {
        return WalletTransactionResponse.builder()
                .id(transaction.getId())
                .walletId(transaction.getWallet().getId())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .description(transaction.getDescription())
                .referenceId(transaction.getReferenceId())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}