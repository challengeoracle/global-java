package br.com.signal.signal_analytics_ai_service.ai.controller;

import br.com.signal.signal_analytics_ai_service.ai.dto.request.InsightAskRequest;
import br.com.signal.signal_analytics_ai_service.ai.dto.response.InsightAskResponse;
import br.com.signal.signal_analytics_ai_service.ai.service.InsightAiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai/insights")
@RequiredArgsConstructor
@Tag(name = "AI Insights", description = "Perguntas e respostas com IA sobre o contexto operacional do OffPay.")
@SecurityRequirement(name = "bearerAuth")
public class InsightAiController {

    private final InsightAiService insightAiService;

    @PostMapping("/ask")
    @Operation(summary = "Perguntar para a IA", description = "Recebe uma pergunta em linguagem natural e devolve uma resposta baseada no contexto operacional disponível.")
    public InsightAskResponse ask(
            @RequestHeader("Authorization") String authorization,
            @RequestBody @Valid InsightAskRequest request
    ) {
        return insightAiService.ask(authorization, request);
    }
}
