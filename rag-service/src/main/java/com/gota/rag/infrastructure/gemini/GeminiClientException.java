package com.gota.rag.infrastructure.gemini;

public class GeminiClientException extends RuntimeException {

    public GeminiClientException(String message) {
        super(message);
    }
}
