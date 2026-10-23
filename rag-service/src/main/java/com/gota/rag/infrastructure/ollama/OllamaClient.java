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

    public OllamaClient(
            @Value("${app.ollama.base-url}") String baseUrl,
            @Value("${app.ollama.model}") String modelName
    ) {
        this.client = RestClient.builder()
                .baseUrl(Objects.requireNonNull(baseUrl))
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.modelName = Objects.requireNonNull(modelName);
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ChatResponse(Message message) {
    }
}
