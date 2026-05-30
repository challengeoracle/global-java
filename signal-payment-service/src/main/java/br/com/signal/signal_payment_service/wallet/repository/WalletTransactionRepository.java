package br.com.signal.signal_payment_service.wallet.repository;

import br.com.signal.signal_payment_service.wallet.entity.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, UUID> {

    List<WalletTransaction> findByWalletIdOrderByCreatedAtDesc(UUID walletId);
}