package com.gota.rag.api;

import com.gota.rag.infrastructure.ollama.OllamaClientException;
import org.springframework.dao.DataAccessException;
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

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiError> handleDatabaseError(DataAccessException exception) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ApiError("RAG_DATABASE_ERROR", "No fue posible consultar la base de conocimiento."));
    }

    public record ApiError(String code, String message) {
    }
}
