package br.com.signal.signal_analytics_ai_service.analytics.controller;

import br.com.signal.signal_analytics_ai_service.analytics.dto.response.*;
import br.com.signal.signal_analytics_ai_service.analytics.service.AnalyticsSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsSummaryService analyticsSummaryService;

    @GetMapping("/me/summary")
    public AnalyticsSummaryResponse getMySummary(
            @RequestHeader("Authorization") String authorization
    ) {
        return analyticsSummaryService.getMySummary(authorization);
    }

    @GetMapping("/seller/summary")
    public SellerSummaryResponse getSellerSummary(
            @RequestHeader("Authorization") String authorization
    ) {
        return analyticsSummaryService.getSellerSummary(authorization);
    }

    @GetMapping("/customer/summary")
    public CustomerSummaryResponse getCustomerSummary(
            @RequestHeader("Authorization") String authorization
    ) {
        return analyticsSummaryService.getCustomerSummary(authorization);
    }

    @GetMapping("/seller/top-products")
    public List<TopProductResponse> getSellerTopProducts(
            @RequestHeader("Authorization") String authorization
    ) {
        return analyticsSummaryService.getSellerTopProducts(authorization);
    }

    @GetMapping("/customer/spending")
    public CustomerSpendingResponse getCustomerSpending(
            @RequestHeader("Authorization") String authorization
    ) {
        return analyticsSummaryService.getCustomerSpending(authorization);
    }
}