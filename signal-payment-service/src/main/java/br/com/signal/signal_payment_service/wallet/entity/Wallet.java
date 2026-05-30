package br.com.signal.signal_payment_service.wallet.entity;

import br.com.signal.signal_payment_service.wallet.enums.WalletOwnerType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "TB_WALLETS",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UK_WALLETS_OWNER",
                        columnNames = {"OWNER_ID", "OWNER_TYPE"}
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "OWNER_ID", nullable = false)
    private UUID ownerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "OWNER_TYPE", nullable = false, length = 20)
    private WalletOwnerType ownerType;

    @Column(name = "BALANCE", nullable = false, precision = 15, scale = 2)
    private BigDecimal balance;

    @Column(name = "PENDING_BALANCE", nullable = false, precision = 15, scale = 2)
    private BigDecimal pendingBalance;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        if (balance == null) {
            balance = BigDecimal.ZERO;
        }

        if (pendingBalance == null) {
            pendingBalance = BigDecimal.ZERO;
        }

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}