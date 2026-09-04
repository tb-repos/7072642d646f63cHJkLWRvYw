package com.tourbhook.api.service.impl;

import com.tourbhook.api.config.GeminiProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Slf4j
@Component
public class GeminiClient {

    private final RestTemplate restTemplate;
    private final GeminiProperties properties;

    public GeminiClient(RestTemplateBuilder builder, GeminiProperties properties) {
        this.properties = properties;
        this.restTemplate = builder
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(30))
                .build();
    }

    public Optional<String> generateText(String prompt) {
        return generate(prompt, Map.of());
    }

    public Optional<String> generateJson(String prompt) {
        return generate(prompt, Map.of("responseMimeType", "application/json"));
    }

    private Optional<String> generate(String prompt, Map<String, Object> generationConfig) {
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            log.warn("Gemini API key not configured — skipping AI call");
            return Optional.empty();
        }

        String url = UriComponentsBuilder
                .fromHttpUrl(properties.getBaseUrl() + "/" + properties.getModel() + ":generateContent")
                .queryParam("key", properties.getApiKey())
                .toUriString();

        Map<String, Object> requestBody = generationConfig.isEmpty()
                ? Map.of("contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))))
                : Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                "generationConfig", generationConfig
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(
                    url, new HttpEntity<>(requestBody, headers), Map.class);

            return extractText(response);

        } catch (RestClientResponseException e) {
            log.error("Gemini API error: status={}, body={}", e.getRawStatusCode(), e.getResponseBodyAsString(), e);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Gemini API unexpected error: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    private Optional<String> extractText(Map<String, Object> response) {
        if (response == null) {
            return Optional.empty();
        }

        Object candidatesObj = response.get("candidates");
        if (!(candidatesObj instanceof List<?> candidates) || candidates.isEmpty()) {
            log.warn("Gemini response had no candidates: {}", response);
            return Optional.empty();
        }

        if (!(candidates.get(0) instanceof Map<?, ?> firstCandidate)) {
            return Optional.empty();
        }

        Object contentObj = firstCandidate.get("content");
        if (!(contentObj instanceof Map<?, ?> content)) {
            return Optional.empty();
        }

        Object partsObj = content.get("parts");
        if (!(partsObj instanceof List<?> parts) || parts.isEmpty()) {
            return Optional.empty();
        }

        if (!(parts.get(0) instanceof Map<?, ?> firstPart)) {
            return Optional.empty();
        }

        Object text = firstPart.get("text");
        if (text == null || text.toString().isBlank()) {
            return Optional.empty();
        }

        return Optional.of(text.toString().trim());
    }
}