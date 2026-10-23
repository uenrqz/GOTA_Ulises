package com.gota.rag.application;

import com.gota.rag.api.ChatController.ChatRequest;
import com.gota.rag.api.ChatController.ChatResponse;
import com.gota.rag.infrastructure.ollama.OllamaClient;
import com.gota.rag.infrastructure.rag.RagChunkRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private OllamaClient ollamaClient;

    @Mock
    private RagChunkRepository ragChunkRepository;

    @Mock
    private OperationalMetricsService operationalMetricsService;

    @Test
    void respondeCantidadDeClientesSinConsultarElModelo() {
        ChatService service = new ChatService(ollamaClient, ragChunkRepository, operationalMetricsService);
        when(operationalMetricsService.countActiveClients()).thenReturn(1);

        ChatResponse response = service.answer(
                new ChatRequest("¿Cuántos clientes activos tenemos?", null)
        );

        assertThat(response.answer()).isEqualTo("Actualmente tenemos 1 cliente activo.");
        verify(ollamaClient, never()).generate(org.mockito.ArgumentMatchers.anyString());
        verify(ragChunkRepository, never()).findSimilar(org.mockito.ArgumentMatchers.anyList());
    }

    @Test
    void informaCuandoNoHayTarifasVigentes() {
        ChatService service = new ChatService(ollamaClient, ragChunkRepository, operationalMetricsService);
        when(operationalMetricsService.findCurrentTariffs()).thenReturn(List.of());

        ChatResponse response = service.answer(
                new ChatRequest("¿Cuál es el costo vigente?", null)
        );

        assertThat(response.answer()).isEqualTo("No hay tarifas vigentes registradas actualmente.");
        verify(ollamaClient, never()).generate(org.mockito.ArgumentMatchers.anyString());
    }
}
