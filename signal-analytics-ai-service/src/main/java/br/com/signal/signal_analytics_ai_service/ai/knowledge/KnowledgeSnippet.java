package br.com.signal.signal_analytics_ai_service.ai.knowledge;

public record KnowledgeSnippet(
        String source,
        String title,
        String content,
        int score
) {
}
