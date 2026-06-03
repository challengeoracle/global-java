package br.com.signal.signal_analytics_ai_service.ai.knowledge;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PdfKnowledgeIngestionService {

    private static final String SOURCE_NAME = "offpay-rules-pdf";
    private static final String SOURCE_TITLE = "Regras operacionais do OffPay";

    private final KnowledgeChunkRepository repository;

    @Value("${knowledge.pdf.path}")
    private String pdfPath;

    @PostConstruct
    public void ingestPdfIfNeeded() {
        if (repository.countBySourceName(SOURCE_NAME) > 0) {
            return;
        }

        var reader = new PagePdfDocumentReader(pdfPath);
        List<Document> documents = reader.get();

        int chunkIndex = 0;

        for (Document document : documents) {
            for (String content : splitIntoChunks(document.getFormattedContent())) {
                if (content.isBlank()) {
                    continue;
                }

                KnowledgeChunk chunk = new KnowledgeChunk();
                chunk.setSourceName(SOURCE_NAME);
                chunk.setTitle(SOURCE_TITLE);
                chunk.setChunkIndex(chunkIndex++);
                chunk.setContent(content.trim());
                chunk.setNormalizedContent(normalize(content));
                repository.save(chunk);
            }
        }
    }

    static String normalize(String value) {
        if (value == null) {
            return "";
        }

        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^a-zA-Z0-9\\s]", " ")
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ")
                .trim();
    }

    private List<String> splitIntoChunks(String content) {
        if (content == null || content.isBlank()) {
            return List.of();
        }

        String[] rawChunks = content.split("(\\r?\\n){2,}");
        List<String> chunks = new ArrayList<>();

        for (String rawChunk : rawChunks) {
            String normalizedChunk = rawChunk == null ? "" : rawChunk.trim();

            if (normalizedChunk.length() >= 40) {
                chunks.add(normalizedChunk);
            }
        }

        if (chunks.isEmpty()) {
            return List.of(content.trim());
        }

        return chunks;
    }
}
