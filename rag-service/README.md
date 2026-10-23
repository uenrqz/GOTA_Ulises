# Servicio RAG de GOTA

Servicio Spring Boot para el chatbot público de GOTA. Está aislado
intencionalmente del monolito de CodeIgniter/MySQL y utiliza una base de datos
PostgreSQL independiente con `pgvector`.

## Diagrama de la arquitectura RAG

La integración actual funciona de la siguiente manera:

```mermaid
flowchart LR
    U[Usuario] --> D[Dashboard público<br/>CodeIgniter]
    D -->|POST /api/rag/chat| S[Servicio RAG<br/>Spring Boot :8081]
    S -->|/api/chat| O[Ollama local<br/>llama3.2 :11434]
    O --> S
    S -->|Respuesta| D

    S -.->|Esquema preparado<br/>para documentos y chunks| P[(PostgreSQL<br/>gota_rag :5433)]
    P -.->|pgvector: recuperación<br/>semántica pendiente| S
```

### Estado actual del flujo

1. El usuario escribe una pregunta en el widget del dashboard.
2. El dashboard envía la pregunta al endpoint `POST /api/rag/chat` de Spring Boot.
3. Spring Boot construye el prompt y lo envía a Ollama.
4. Ollama genera la respuesta usando el modelo local `llama3.2`.
5. Spring Boot devuelve la respuesta al dashboard.
6. PostgreSQL contiene el esquema inicial para documentos y fragmentos, pero la
   ingesta de documentos, los embeddings y la recuperación mediante `pgvector`
   todavía deben conectarse al flujo de conversación.

Por tanto, la implementación actual corresponde a:

```text
Pregunta → Spring Boot → Ollama/llama3.2 → Respuesta
```

El flujo RAG completo que se habilitará posteriormente será:

```text
Documentos → Fragmentos → Embeddings → pgvector
                                      ↓
Pregunta → Búsqueda semántica → Contexto → Ollama → Respuesta fundamentada
```

## Requisitos

- Java 21
- Maven 3.9+
- PostgreSQL con la extensión `vector`
- Ollama instalado y ejecutándose localmente
- Modelo `llama3.2` descargado

## Ejecución local

1. Crea una base de datos llamada `gota_rag` y habilita `pgvector`.
2. Confirma que Ollama está ejecutándose y que el modelo está disponible:

```bash
ollama list
ollama run llama3.2
```

3. Configura las variables de la base de datos tomando como referencia
   `.env.example`.
4. Inicia el servicio:

```bash
mvn spring-boot:run
```

Flyway crea las tablas del RAG al iniciar. De forma predeterminada, el
servicio queda disponible en `http://localhost:8081`.

## API

`POST /api/rag/chat` es el contrato interno que utilizará el BFF de Next.js.
El adaptador de Ollama se encuentra en
`src/main/java/com/gota/rag/infrastructure/ollama/OllamaClient.java` y utiliza
el endpoint local `/api/chat` de Ollama. No se necesita una clave de API del
LLM.

```json
{
  "message": "¿Cómo puedo reportar una fuga?",
  "conversationId": "optional-client-conversation-id"
}
```

Actualmente, el endpoint envía directamente a Ollama la pregunta del usuario.
Todavía no implementa el flujo RAG completo: la recuperación mediante pgvector
y los embeddings deben conectarse antes de habilitar respuestas para
producción. Hasta entonces, las respuestas no deben considerarse fundamentadas
en documentos de GOTA.

Comprobación de estado: `GET /actuator/health`.
