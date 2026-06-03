package br.com.signal.signal_analytics_ai_service.ai.knowledge;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "knowledge_chunk")
@Getter
@Setter
public class KnowledgeChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_name", nullable = false, length = 120)
    private String sourceName;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "chunk_index", nullable = false)
    private Integer chunkIndex;

    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    @Lob
    @Column(name = "normalized_content", nullable = false)
    private String normalizedContent;
}
