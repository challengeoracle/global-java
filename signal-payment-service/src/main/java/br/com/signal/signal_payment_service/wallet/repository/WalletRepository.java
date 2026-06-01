package br.com.signal.signal_payment_service.wallet.repository;

import br.com.signal.signal_payment_service.wallet.entity.Wallet;
import br.com.signal.signal_payment_service.wallet.enums.WalletOwnerType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    Optional<Wallet> findByOwnerIdAndOwnerType(UUID ownerId, WalletOwnerType ownerType);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from Wallet w where w.ownerId = :ownerId and w.ownerType = :ownerType")
    Optional<Wallet> findByOwnerIdAndOwnerTypeForUpdate(@Param("ownerId") UUID ownerId, @Param("ownerType") WalletOwnerType ownerType);
}
