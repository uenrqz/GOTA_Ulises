package com.gota.rag.infrastructure.ollama;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Objects;

@Component
public class OllamaClient {

    private final RestClient client;
    private final String modelName;
    private final String embeddingModelName;

    public OllamaClient(
            @Value("${app.ollama.base-url}") String baseUrl,
            @Value("${app.ollama.model}") String modelName,
            @Value("${app.ollama.embedding-model}") String embeddingModelName
    ) {
        this.client = RestClient.builder()
                .baseUrl(Objects.requireNonNull(baseUrl))
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.modelName = Objects.requireNonNull(modelName);
        this.embeddingModelName = Objects.requireNonNull(embeddingModelName);
    }

    public List<Double> embed(String content) {
        try {
            EmbeddingResponse response = client.post()
                    .uri("/api/embeddings")
                    .body(new EmbeddingRequest(embeddingModelName, content))
                    .retrieve()
                    .body(EmbeddingResponse.class);

            if (response == null || response.embedding() == null || response.embedding().isEmpty()) {
                throw new OllamaClientException("Ollama returned no embedding");
            }

            return response.embedding();
        } catch (RestClientResponseException exception) {
            throw new OllamaClientException(
                    "Ollama rejected the embedding request (HTTP %s): %s"
                            .formatted(exception.getStatusCode().value(), exception.getResponseBodyAsString()),
                    exception
            );
        }
    }

    public String generate(String prompt) {
        try {
            ChatResponse response = client.post()
                    .uri("/api/chat")
                    .body(new ChatRequest(
                            modelName,
                            List.of(new Message("user", prompt)),
                            false
                    ))
                    .retrieve()
                    .body(ChatResponse.class);

            if (response == null || response.message() == null
                    || response.message().content() == null
                    || response.message().content().isBlank()) {
                throw new OllamaClientException("Ollama returned no answer");
            }

            return response.message().content();
        } catch (RestClientResponseException exception) {
            throw new OllamaClientException(
                    "Ollama rejected the request (HTTP %s): %s"
                            .formatted(exception.getStatusCode().value(), exception.getResponseBodyAsString()),
                    exception
            );
        }
    }

    private record ChatRequest(String model, List<Message> messages, boolean stream) {
    }

    private record Message(String role, String content) {
    }

    private record EmbeddingRequest(String model, String prompt) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record EmbeddingResponse(List<Double> embedding) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ChatResponse(Message message) {
    }
}
