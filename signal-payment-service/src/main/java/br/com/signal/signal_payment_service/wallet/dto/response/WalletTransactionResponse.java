package br.com.signal.signal_payment_service.wallet.dto.response;

import br.com.signal.signal_payment_service.wallet.enums.WalletTransactionType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletTransactionResponse {

    private UUID id;
    private UUID walletId;
    private WalletTransactionType type;
    private BigDecimal amount;
    private String description;
    private String referenceId;
    private LocalDateTime createdAt;
}