package br.com.signal.signal_analytics_ai_service.ai.service;

import br.com.signal.signal_analytics_ai_service.ai.dto.request.InsightAskRequest;
import br.com.signal.signal_analytics_ai_service.ai.dto.response.InsightAskResponse;
import br.com.signal.signal_analytics_ai_service.analytics.dto.response.AnalyticsSummaryResponse;
import br.com.signal.signal_analytics_ai_service.analytics.service.AnalyticsSummaryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class InsightAiService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final AnalyticsSummaryService analyticsSummaryService;

    @Value("classpath:knowledge/offpay-rules.md")
    private Resource offpayRulesResource;

    @Value("${spring.ai.openai.chat.options.model}")
    private String model;

    public InsightAskResponse ask(String authorization, InsightAskRequest request) {
        AnalyticsSummaryResponse summary = analyticsSummaryService.getMySummary(authorization);

        String rules = readRules();
        String summaryJson = toJson(summary);

        String answer = chatClient.prompt()
                .system("""
                        Você é o OffPay Insights, um assistente de análise operacional.
                        
                        Use apenas os dados fornecidos no contexto.
                        Não invente números, pedidos, produtos, lojas ou valores.
                        Responda sempre em português do Brasil.
                        Seja claro, direto e útil.
                        
                        Regras do domínio:
                        %s
                        """.formatted(rules))
                .user("""
                        Pergunta do usuário:
                        %s
                        
                        Contexto operacional em JSON:
                        %s
                        
                        Gere uma resposta curta e útil com base no contexto.
                        """.formatted(request.getQuestion(), summaryJson))
                .call()
                .content();

        return InsightAskResponse.builder()
                .answer(answer)
                .source("analytics_summary")
                .model(model)
                .build();
    }

    private String readRules() {
        try {
            return offpayRulesResource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException ex) {
            return """
                    O OffPay é uma plataforma offline-first.
                    Responda em português do Brasil.
                    Não invente dados.
                    Use apenas o contexto fornecido.
                    """;
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            return "{}";
        }
    }
}