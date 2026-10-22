package com.gota.rag.infrastructure.gemini;

import com.google.genai.Client;
import com.google.genai.gaos.models.interactions.CreateModelInteraction;
import com.google.genai.gaos.models.interactions.InteractionsInput;
import com.google.genai.gaos.models.interactions.Interaction;
import com.google.genai.gaos.models.interactions.Model;
import com.google.genai.gaos.models.operations.CreateInteractionRequestBody;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GeminiClient {

    private final Client client;
    private final String modelName;

    public GeminiClient(
            Client client,
            @Value("${app.gemini.model}") String modelName
    ) {
        this.client = client;
        this.modelName = modelName;
    }

    public String generate(String prompt) {
        CreateModelInteraction params = CreateModelInteraction.builder()
                .model(Model.of(modelName))
                .input(InteractionsInput.of(prompt))
                .build();

        Interaction interaction = client.interactions
                .create(CreateInteractionRequestBody.of(params))
                .interaction()
                .orElseThrow(() -> new GeminiClientException("Gemini returned no interaction"));

        return interaction.outputText()
                .orElseThrow(() -> new GeminiClientException("Gemini returned no text"));
    }
}
