package br.com.signal.signal_analytics_ai_service.analytics.controller;

import br.com.signal.signal_analytics_ai_service.analytics.dto.response.AnalyticsSummaryResponse;
import br.com.signal.signal_analytics_ai_service.analytics.service.AnalyticsSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
}