package br.com.signal.signal_payment_service.wallet.dto.response;

import br.com.signal.signal_payment_service.wallet.enums.WalletOwnerType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletResponse {

    private UUID id;
    private UUID ownerId;
    private WalletOwnerType ownerType;
    private BigDecimal balance;
    private BigDecimal pendingBalance;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}