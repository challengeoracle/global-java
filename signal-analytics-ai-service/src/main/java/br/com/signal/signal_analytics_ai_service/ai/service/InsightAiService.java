package br.com.signal.signal_analytics_ai_service.ai.service;

import br.com.signal.signal_analytics_ai_service.ai.dto.request.InsightAskRequest;
import br.com.signal.signal_analytics_ai_service.ai.dto.response.InsightAskResponse;
import br.com.signal.signal_analytics_ai_service.ai.knowledge.KnowledgeRetrievalService;
import br.com.signal.signal_analytics_ai_service.ai.knowledge.KnowledgeSnippet;
import br.com.signal.signal_analytics_ai_service.ai.tool.InsightRuntimeTools;
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

import java.util.List;

@Service
@RequiredArgsConstructor
public class InsightAiService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final AnalyticsSummaryService analyticsSummaryService;
    private final AuthIdentityService authIdentityService;
    private final KnowledgeRetrievalService knowledgeRetrievalService;

    @Value("${spring.ai.openai.chat.options.model}")
    private String model;

    public InsightAskResponse ask(String authorization, InsightAskRequest request) {
        AuthUserResponse authUser = authIdentityService.requireCustomerOrSeller(authorization);
        AnalyticsSummaryResponse summary = analyticsSummaryService.getMySummary(authorization);
        List<KnowledgeSnippet> snippets = knowledgeRetrievalService.retrieve(authUser, request.getQuestion());
        InsightRuntimeTools runtimeTools = new InsightRuntimeTools(
                authorization,
                authUser,
                analyticsSummaryService,
                knowledgeRetrievalService
        );

        String knowledgeContext = knowledgeRetrievalService.renderContext(snippets);
        String summaryJson = toJson(summary);

        String answer = chatClient.prompt()
                .system("""
                        Voce e o OffPay Insights, um assistente de analise operacional e financeira.
                        Use as ferramentas disponiveis quando precisar confirmar indicadores, regras ou fontes.
                        Responda apenas com base nos dados e nas regras recuperadas.

                        Contexto recuperado por retrieval:
                        %s
                        """.formatted(knowledgeContext))
                .user("""
                        Perfil do usuario:
                        - Nome: %s
                        - Papel: %s
                        - Loja: %s

                        Pergunta do usuario:
                        %s

                        Contexto operacional em JSON:
                        %s

                        Responda com base somente nesse contexto.
                        Se faltar certeza, use uma ferramenta antes de responder.
                        Nao invente valores.
                        Seja breve, claro e util.
                        """.formatted(
                        authUser.getName(),
                        authUser.getRole(),
                        authUser.getStoreName(),
                        request.getQuestion(),
                        summaryJson
                ))
                .tools(runtimeTools)
                .call()
                .content();

        return InsightAskResponse.builder()
                .answer(answer)
                .source("spring_ai_runtime_retrieval_tools")
                .model(model)
                .sources(snippets.stream()
                        .map(snippet -> snippet.source() + ":" + snippet.title())
                        .toList())
                .capabilities(List.of("RAG", "TOOLING", "MCP"))
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
