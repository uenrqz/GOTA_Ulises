package com.gota.rag.application;

import com.gota.rag.infrastructure.ollama.OllamaClient;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class DocumentIngestionService {

    private static final int CHUNK_SIZE = 1000;
    private static final int CHUNK_OVERLAP = 150;

    private final JdbcTemplate jdbcTemplate;
    private final OllamaClient ollamaClient;

    public DocumentIngestionService(JdbcTemplate jdbcTemplate, OllamaClient ollamaClient) {
        this.jdbcTemplate = jdbcTemplate;
        this.ollamaClient = ollamaClient;
    }

    @Transactional
    public UUID ingest(String title, String source, String content) {
        UUID documentId = UUID.randomUUID();
        jdbcTemplate.update(
                """
                INSERT INTO rag_documents (id, title, source, content)
                VALUES (?, ?, ?, ?)
                """,
                documentId, title, source, content
        );

        List<String> chunks = splitIntoChunks(content);
        for (int index = 0; index < chunks.size(); index++) {
            List<Double> embedding = ollamaClient.embed(chunks.get(index));
            jdbcTemplate.update(
                    """
                    INSERT INTO rag_chunks (id, document_id, chunk_index, content, embedding)
                    VALUES (?, ?, ?, ?, CAST(? AS vector))
                    """,
                    UUID.randomUUID(), documentId, index, chunks.get(index), toVectorLiteral(embedding)
            );
        }
        return documentId;
    }

    private List<String> splitIntoChunks(String content) {
        int step = CHUNK_SIZE - CHUNK_OVERLAP;
        return java.util.stream.IntStream.iterate(0, start -> start < content.length(), start -> start + step)
                .mapToObj(start -> content.substring(start, Math.min(start + CHUNK_SIZE, content.length())).trim())
                .filter(chunk -> !chunk.isBlank())
                .toList();
    }

    private String toVectorLiteral(List<Double> embedding) {
        return embedding.stream()
                .map(String::valueOf)
                .collect(java.util.stream.Collectors.joining(",", "[", "]"));
    }
}
