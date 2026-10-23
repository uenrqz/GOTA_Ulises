package com.gota.rag.infrastructure.rag;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class RagChunkRepository {

    private static final int MAX_RESULTS = 5;
    private final JdbcTemplate jdbcTemplate;

    public RagChunkRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<RagChunk> findSimilar(List<Double> embedding) {
        String vector = embedding.stream()
                .map(String::valueOf)
                .collect(java.util.stream.Collectors.joining(",", "[", "]"));

        return jdbcTemplate.query(
                """
                SELECT c.id, c.content, d.id AS document_id, d.title, d.source,
                       1 - (c.embedding <=> CAST(? AS vector)) AS similarity
                FROM rag_chunks c
                JOIN rag_documents d ON d.id = c.document_id
                ORDER BY c.embedding <=> CAST(? AS vector)
                LIMIT ?
                """,
                (resultSet, rowNum) -> new RagChunk(
                        resultSet.getObject("id", UUID.class),
                        resultSet.getString("content"),
                        resultSet.getObject("document_id", UUID.class),
                        resultSet.getString("title"),
                        resultSet.getString("source"),
                        resultSet.getDouble("similarity")
                ),
                vector, vector, MAX_RESULTS
        );
    }

    public record RagChunk(
            UUID id,
            String content,
            UUID documentId,
            String title,
            String source,
            double similarity
    ) {
    }
}
