package com.gota.rag.api;

import com.gota.rag.infrastructure.ollama.OllamaClientException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(OllamaClientException.class)
    public ResponseEntity<ApiError> handleOllamaError(OllamaClientException exception) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ApiError("LLM_PROVIDER_ERROR", exception.getMessage()));
    }

    public record ApiError(String code, String message) {
    }
}
