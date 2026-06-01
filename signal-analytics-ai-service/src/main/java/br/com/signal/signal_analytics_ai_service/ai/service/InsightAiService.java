package br.com.signal.signal_analytics_ai_service.ai.service;

import br.com.signal.signal_analytics_ai_service.ai.dto.request.InsightAskRequest;
import br.com.signal.signal_analytics_ai_service.ai.dto.response.InsightAskResponse;
import br.com.signal.signal_analytics_ai_service.ai.knowledge.KnowledgeService;
import br.com.signal.signal_analytics_ai_service.analytics.dto.response.AnalyticsSummaryResponse;
import br.com.signal.signal_analytics_ai_service.analytics.service.AnalyticsSummaryService;
import br.com.signal.signal_analytics_ai_service.shared.dto.response.AuthUserResponse;
import br.com.signal.signal_analytics_ai_service.shared.service.AuthIdentityService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InsightAiService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final AnalyticsSummaryService analyticsSummaryService;
    private final AuthIdentityService authIdentityService;
    private final KnowledgeService knowledgeService;

    @Value("${spring.ai.openai.chat.options.model}")
    private String model;

    public InsightAskResponse ask(String authorization, InsightAskRequest request) {
        AuthUserResponse authUser = authIdentityService.requireCustomerOrSeller(authorization);
        AnalyticsSummaryResponse summary = analyticsSummaryService.getMySummary(authorization);

        String knowledgeContext = knowledgeService.buildKnowledgeContext(authUser, request.getQuestion());
        String summaryJson = toJson(summary);

        String answer = chatClient.prompt()
                .system("""
                        Você é o OffPay Insights, um assistente de análise operacional e financeira.
                        
                        Siga rigorosamente as regras abaixo.
                        
                        Regras selecionadas:
                        %s
                        """.formatted(knowledgeContext))
                .user("""
                        Perfil do usuário:
                        - Nome: %s
                        - Papel: %s
                        - Loja: %s
                        
                        Pergunta do usuário:
                        %s
                        
                        Contexto operacional em JSON:
                        %s
                        
                        Responda com base somente nesse contexto.
                        Não invente valores.
                        Seja breve, claro e útil.
                        """.formatted(
                        authUser.getName(),
                        authUser.getRole(),
                        authUser.getStoreName(),
                        request.getQuestion(),
                        summaryJson
                ))
                .call()
                .content();

        return InsightAskResponse.builder()
                .answer(answer)
                .source("analytics_summary_with_runtime_knowledge")
                .model(model)
                .build();
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            return "{}";
        }
    }
}