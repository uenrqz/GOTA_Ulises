package com.gota.rag.config;

import com.google.genai.Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiConfig {

    @Bean("geminiSdkClient")
    Client geminiSdkClient(@Value("${app.gemini.api-key}") String apiKey) {
        if (apiKey.isBlank()) {
            throw new IllegalStateException(
                    "GEMINI_API_KEY must be configured before starting the RAG service"
            );
        }

        return Client.builder().apiKey(apiKey).build();
    }
}
