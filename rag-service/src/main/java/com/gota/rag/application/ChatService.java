package com.gota.rag.application;

import com.gota.rag.api.ChatController.ChatRequest;
import com.gota.rag.api.ChatController.ChatResponse;
import com.gota.rag.api.ChatController.SourceReference;
import com.gota.rag.infrastructure.ollama.OllamaClient;
import com.gota.rag.infrastructure.rag.RagChunkRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Application boundary for retrieval and generation.
 */
@Service
public class ChatService {

    private final OllamaClient ollamaClient;
    private final RagChunkRepository ragChunkRepository;

    public ChatService(OllamaClient ollamaClient, RagChunkRepository ragChunkRepository) {
        this.ollamaClient = ollamaClient;
        this.ragChunkRepository = ragChunkRepository;
    }

    public ChatResponse answer(ChatRequest request) {
        String conversationId = conversationIdOrNew(request.conversationId());
        List<RagChunkRepository.RagChunk> chunks =
                ragChunkRepository.findSimilar(ollamaClient.embed(request.message()));
        String prompt = """
                Eres el asistente público de GOTA. Responde en español de forma clara y breve.
                Responde únicamente con la información del contexto.
                Si el contexto no contiene la respuesta, indica que no puedes confirmarlo.
                No inventes cifras, tarifas, fechas ni datos de clientes.

                Contexto:
                %s

                Pregunta del usuario:
                %s
                """.formatted(formatContext(chunks), request.message());

        List<SourceReference> sources = chunks.stream()
                .map(chunk -> new SourceReference(
                        chunk.title(), chunk.documentId().toString()))
                .distinct()
                .toList();

        return new ChatResponse(ollamaClient.generate(prompt), sources, conversationId);
    }

    private String formatContext(List<RagChunkRepository.RagChunk> chunks) {
        if (chunks.isEmpty()) {
            return "No se encontraron documentos relevantes.";
        }
        return chunks.stream()
                .map(chunk -> "- %s (relevancia %.2f):%n%s"
                        .formatted(chunk.title(), chunk.similarity(), chunk.content()))
                .collect(Collectors.joining(System.lineSeparator() + System.lineSeparator()));
    }

    public String conversationIdOrNew(String conversationId) {
        return conversationId == null || conversationId.isBlank()
                ? UUID.randomUUID().toString()
                : conversationId;
    }
}
