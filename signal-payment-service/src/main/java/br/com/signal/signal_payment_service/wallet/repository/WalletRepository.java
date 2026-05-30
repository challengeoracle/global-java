package br.com.signal.signal_payment_service.wallet.repository;

import br.com.signal.signal_payment_service.wallet.entity.Wallet;
import br.com.signal.signal_payment_service.wallet.enums.WalletOwnerType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    Optional<Wallet> findByOwnerIdAndOwnerType(UUID ownerId, WalletOwnerType ownerType);
}