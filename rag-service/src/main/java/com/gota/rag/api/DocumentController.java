package com.gota.rag.api;

import com.gota.rag.application.DocumentIngestionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/rag/documents")
public class DocumentController {

    private final DocumentIngestionService ingestionService;

    public DocumentController(DocumentIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping
    public ResponseEntity<DocumentResponse> ingest(@Valid @RequestBody DocumentRequest request) {
        UUID documentId = ingestionService.ingest(request.title(), request.source(), request.content());
        return ResponseEntity.ok(new DocumentResponse(documentId));
    }

    public record DocumentRequest(
            @NotBlank @Size(max = 255) String title,
            @NotBlank @Size(max = 500) String source,
            @NotBlank @Size(max = 100_000) String content
    ) {
    }

    public record DocumentResponse(UUID documentId) {
    }
}
