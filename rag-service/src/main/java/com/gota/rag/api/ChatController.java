package com.gota.rag.api;

import com.gota.rag.application.ChatService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rag")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        return ResponseEntity.ok(chatService.answer(request));
    }

    public record ChatRequest(
            @NotBlank(message = "message is required")
            @Size(max = 2000, message = "message must not exceed 2000 characters")
            String message,
            @Size(max = 100, message = "conversationId must not exceed 100 characters")
            String conversationId
    ) {
    }

    public record ChatResponse(
            String answer,
            List<SourceReference> sources,
            String conversationId
    ) {
    }

    public record SourceReference(String title, String documentId) {
    }
}
