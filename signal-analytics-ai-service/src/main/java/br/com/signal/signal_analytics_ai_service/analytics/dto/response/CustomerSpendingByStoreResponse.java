package br.com.signal.signal_analytics_ai_service.analytics.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerSpendingByStoreResponse {

    private UUID storeId;
    private String storeName;
    private Integer purchases;
    private BigDecimal totalSpent;
    private LocalDateTime lastPurchaseAt;
}
