package br.com.signal.signal_analytics_ai_service.analytics.dto.client;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletClientResponse {

    private UUID id;
    private UUID ownerId;
    private String ownerType;
    private BigDecimal balance;
    private BigDecimal pendingBalance;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}