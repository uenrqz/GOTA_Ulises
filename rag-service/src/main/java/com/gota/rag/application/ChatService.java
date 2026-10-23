package com.gota.rag.application;

import com.gota.rag.api.ChatController.ChatRequest;
import com.gota.rag.api.ChatController.ChatResponse;
import com.gota.rag.api.ChatController.SourceReference;
import com.gota.rag.infrastructure.ollama.OllamaClient;
import com.gota.rag.infrastructure.rag.RagChunkRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Application boundary for retrieval and generation.
 */
@Service
public class ChatService {

    private final OllamaClient ollamaClient;
    private final RagChunkRepository ragChunkRepository;
    private final OperationalMetricsService operationalMetricsService;

    public ChatService(
            OllamaClient ollamaClient,
            RagChunkRepository ragChunkRepository,
            OperationalMetricsService operationalMetricsService
    ) {
        this.ollamaClient = ollamaClient;
        this.ragChunkRepository = ragChunkRepository;
        this.operationalMetricsService = operationalMetricsService;
    }

    public ChatResponse answer(ChatRequest request) {
        String conversationId = conversationIdOrNew(request.conversationId());
        if (isActiveClientsQuestion(request.message())) {
            int activeClients = operationalMetricsService.countActiveClients();
            return new ChatResponse(
                    activeClients == 1
                            ? "Actualmente tenemos 1 cliente activo."
                            : "Actualmente tenemos %d clientes activos.".formatted(activeClients),
                    List.of(),
                    conversationId
            );
        }
        if (isActiveClientNamesQuestion(request.message())) {
            List<String> names = operationalMetricsService.findActiveClientNames();
            String answer = names.isEmpty()
                    ? "No hay clientes activos registrados actualmente."
                    : "Los clientes activos registrados son: %s.".formatted(String.join(", ", names));
            return new ChatResponse(answer, List.of(), conversationId);
        }
        if (isCurrentTariffQuestion(request.message())) {
            List<OperationalMetricsService.CurrentTariff> tariffs =
                    operationalMetricsService.findCurrentTariffs();
            String answer = tariffs.isEmpty()
                    ? "No hay tarifas vigentes registradas actualmente."
                    : formatTariffs(tariffs);
            return new ChatResponse(answer, List.of(), conversationId);
        }
        List<RagChunkRepository.RagChunk> chunks =
                ragChunkRepository.findSimilar(ollamaClient.embed(request.message()));
        String prompt = """
                Eres el asistente público de GOTA. Responde en español de forma clara y breve.
                Responde únicamente con la información del contexto.
                Si el contexto no contiene la respuesta, indica que no puedes confirmarlo.
                No inventes cifras, tarifas, fechas ni datos de clientes.

                Contexto:
                %s

                Pregunta del usuario:
                %s
                """.formatted(formatContext(chunks), request.message());

        List<SourceReference> sources = chunks.stream()
                .map(chunk -> new SourceReference(
                        chunk.title(), chunk.documentId().toString()))
                .distinct()
                .toList();

        return new ChatResponse(ollamaClient.generate(prompt), sources, conversationId);
    }

    private boolean isActiveClientsQuestion(String message) {
        String normalized = message.toLowerCase(java.util.Locale.ROOT);
        return normalized.contains("cliente")
                && normalized.contains("activo");
    }

    private boolean isActiveClientNamesQuestion(String message) {
        String normalized = message.toLowerCase(java.util.Locale.ROOT);
        return normalized.contains("cliente")
                && (normalized.contains("nombre") || normalized.contains("llama"));
    }

    private boolean isCurrentTariffQuestion(String message) {
        String normalized = message.toLowerCase(java.util.Locale.ROOT);
        return normalized.contains("tarifa")
                || normalized.contains("costo vigente")
                || normalized.contains("precio vigente");
    }

    private String formatTariffs(List<OperationalMetricsService.CurrentTariff> tariffs) {
        return tariffs.stream()
                .map(tariff -> "Servicio %s: tarifa base %s, incluye %s m³ y el exceso cuesta %s por m³"
                        .formatted(
                                tariff.serviceType(),
                                tariff.baseRate(),
                                tariff.includedVolumeM3(),
                                tariff.excessRate()
                        ))
                .collect(Collectors.joining(". ", "Tarifas vigentes: ", "."));
    }

    private String formatContext(List<RagChunkRepository.RagChunk> chunks) {
        if (chunks.isEmpty()) {
            return "No se encontraron documentos relevantes.";
        }
        return chunks.stream()
                .map(chunk -> "- %s (relevancia %.2f):%n%s"
                        .formatted(chunk.title(), chunk.similarity(), chunk.content()))
                .collect(Collectors.joining(System.lineSeparator() + System.lineSeparator()));
    }

    public String conversationIdOrNew(String conversationId) {
        return conversationId == null || conversationId.isBlank()
                ? UUID.randomUUID().toString()
                : conversationId;
    }
}
