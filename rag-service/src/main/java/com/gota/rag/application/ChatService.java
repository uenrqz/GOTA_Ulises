package com.gota.rag.application;

import com.gota.rag.api.ChatController.ChatRequest;
import com.gota.rag.api.ChatController.ChatResponse;
import com.gota.rag.infrastructure.ollama.OllamaClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Application boundary for retrieval and generation.
 */
@Service
public class ChatService {

    private final OllamaClient ollamaClient;

    public ChatService(OllamaClient ollamaClient) {
        this.ollamaClient = ollamaClient;
    }

    public ChatResponse answer(ChatRequest request) {
        String conversationId = conversationIdOrNew(request.conversationId());
        String prompt = """
                Eres el asistente público de GOTA. Responde en español de forma clara y breve.
                Si no tienes información suficiente en el contexto proporcionado, indica que
                no puedes confirmarlo y recomienda consultar los canales oficiales.

                Pregunta del usuario:
                %s
                """.formatted(request.message());

        return new ChatResponse(ollamaClient.generate(prompt), List.of(), conversationId);
    }

    public String conversationIdOrNew(String conversationId) {
        return conversationId == null || conversationId.isBlank()
                ? UUID.randomUUID().toString()
                : conversationId;
    }
}
