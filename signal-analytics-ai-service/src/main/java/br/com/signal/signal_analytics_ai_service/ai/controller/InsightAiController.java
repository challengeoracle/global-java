package br.com.signal.signal_analytics_ai_service.ai.controller;

import br.com.signal.signal_analytics_ai_service.ai.dto.request.InsightAskRequest;
import br.com.signal.signal_analytics_ai_service.ai.dto.response.InsightAskResponse;
import br.com.signal.signal_analytics_ai_service.ai.service.InsightAiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai/insights")
@RequiredArgsConstructor
public class InsightAiController {

    private final InsightAiService insightAiService;

    @PostMapping("/ask")
    public InsightAskResponse ask(
            @RequestHeader("Authorization") String authorization,
            @RequestBody @Valid InsightAskRequest request
    ) {
        return insightAiService.ask(authorization, request);
    }
}