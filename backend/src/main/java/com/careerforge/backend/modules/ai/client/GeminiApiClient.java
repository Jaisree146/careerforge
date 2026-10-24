package com.careerforge.backend.modules.ai.client;

import com.careerforge.backend.modules.ai.config.GeminiConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class GeminiApiClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiApiClient.class);

    private final GeminiConfig config;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public GeminiApiClient(GeminiConfig config, RestClient geminiRestClient, ObjectMapper objectMapper) {
        this.config = config;
        this.restClient = geminiRestClient;
        this.objectMapper = objectMapper;
    }

    public Optional<String> generateJsonContent(String prompt) {
        if (!config.isConfigured()) {
            log.info("Gemini API key is not configured or using default template. Using smart fallback.");
            return Optional.empty();
        }

        try {
            Map<String, Object> part = Map.of("text", prompt);
            Map<String, Object> content = Map.of("parts", List.of(part));

            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("temperature", 0.3);
            generationConfig.put("responseMimeType", "application/json");

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(content),
                    "generationConfig", generationConfig
            );

            String uri = String.format("/models/%s:generateContent?key=%s", config.getModel(), config.getApiKey());

            String rawResponse = restClient.post()
                    .uri(uri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            if (rawResponse == null || rawResponse.isBlank()) {
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(rawResponse);
            JsonNode textNode = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");

            if (!textNode.isMissingNode() && !textNode.asText().isBlank()) {
                String responseText = textNode.asText().trim();
                return Optional.of(responseText);
            }
        } catch (Exception e) {
            log.warn("Gemini API call failed: {}. Falling back to rule-based AI engine.", e.getMessage());
        }

        return Optional.empty();
    }
}
