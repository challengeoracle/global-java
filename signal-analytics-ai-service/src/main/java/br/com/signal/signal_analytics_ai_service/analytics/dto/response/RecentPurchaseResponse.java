package br.com.signal.signal_analytics_ai_service.analytics.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecentPurchaseResponse {

    private UUID orderId;
    private String localOrderId;
    private UUID storeId;
    private String storeName;
    private LocalDateTime purchasedAt;
    private BigDecimal totalAmount;
    private String paymentStatus;
    private String orderStatus;
    private String syncStatus;
    private List<String> productNames;
}
