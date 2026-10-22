# GOTA RAG service

Spring Boot service for the public GOTA chatbot. It is intentionally isolated
from the CodeIgniter/MySQL monolith and is designed to use a separate
PostgreSQL database with `pgvector`.

## Requirements

- Java 21
- Maven 3.9+
- PostgreSQL with the `vector` extension
- A Gemini API key from Google AI Studio

## Run locally

1. Create a database named `gota_rag` and enable `pgvector`.
2. Set `GEMINI_API_KEY` in the IntelliJ run configuration, your shell, or an
   ignored local `.env` file. Never commit the key.
3. Set the database variables from `.env.example`.
4. Start the service:

```bash
mvn spring-boot:run
```

Flyway creates the RAG tables on startup. The service listens on
`http://localhost:8081` by default.

## API

`POST /api/rag/chat` is the internal contract that the Next.js BFF will call.
The Gemini adapter is located in
`src/main/java/com/gota/rag/infrastructure/gemini/GeminiClient.java`; it
creates the SDK client in `GeminiConfig` and reads the key from
`GEMINI_API_KEY`.

```json
{
  "message": "¿Cómo puedo reportar una fuga?",
  "conversationId": "optional-client-conversation-id"
}
```

The current endpoint calls Gemini directly with the user prompt. It is not yet
the complete RAG flow: pgvector retrieval and embeddings still need to be
connected before enabling production answers. Until then, do not treat the
response as grounded in GOTA documents.

Health check: `GET /actuator/health`.
