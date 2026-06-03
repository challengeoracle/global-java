package br.com.signal.signal_analytics_ai_service.analytics.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsChartResponse {

    private String role;
    private String period;
    private Integer totalOrders;
    private BigDecimal totalAmount;
    private List<AnalyticsChartPointResponse> points;
}
