package br.com.signal.signal_analytics_ai_service.analytics.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopProductResponse {

    private UUID productId;
    private String productName;
    private Integer quantitySold;
    private BigDecimal totalAmount;
}