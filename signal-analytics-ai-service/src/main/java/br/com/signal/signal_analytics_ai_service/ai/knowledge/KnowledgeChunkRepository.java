package br.com.signal.signal_analytics_ai_service.ai.knowledge;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface KnowledgeChunkRepository extends JpaRepository<KnowledgeChunk, Long> {

    long countBySourceName(String sourceName);

    @Query("""
            SELECT kc
            FROM KnowledgeChunk kc
            WHERE kc.normalizedContent LIKE CONCAT('%', :term, '%')
            ORDER BY kc.chunkIndex ASC
            """)
    List<KnowledgeChunk> searchByTerm(@Param("term") String term, Pageable pageable);

    List<KnowledgeChunk> findTop50BySourceNameOrderByChunkIndexAsc(String sourceName);
}
