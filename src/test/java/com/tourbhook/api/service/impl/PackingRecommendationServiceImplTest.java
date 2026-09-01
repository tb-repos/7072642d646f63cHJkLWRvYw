package com.tourbhook.api.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tourbhook.api.dto.packing.GeneratedPackingList;
import com.tourbhook.api.dto.weather.WeatherResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PackingRecommendationServiceImplTest {

    @Mock
    private GeminiClient geminiClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private PackingRecommendationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PackingRecommendationServiceImpl(geminiClient, objectMapper);
    }

    private WeatherResponse sampleWeather() {
        return new WeatherResponse("Munnar", LocalDate.now(), "Rainy", 16, 22, "ok", 70, "10 km/h", 85);
    }

    @Test
    void generate_whenGeminiReturnsValidJson_returnsAiSourcedList() {
        String validJson = """
                [
                  {"name": "Clothing", "icon": "shirt", "items": ["Rain jacket", "Trekking boots"]},
                  {"name": "Gear", "icon": "bag", "items": ["Trekking poles"]}
                ]
                """;
        when(geminiClient.generateJson(anyString())).thenReturn(Optional.of(validJson));

        GeneratedPackingList result = service.generate("Munnar", 4, sampleWeather(), List.of("Trekking in Munnar hills"));

        assertThat(result.source()).isEqualTo("AI");
        assertThat(result.categories()).hasSize(2);
        assertThat(result.categories().get(0).name()).isEqualTo("Clothing");
        assertThat(result.categories().get(0).items()).contains("Rain jacket", "Trekking boots");
    }

    @Test
    void generate_whenGeminiCallFails_fallsBackToStaticChecklist() {
        when(geminiClient.generateJson(anyString())).thenReturn(Optional.empty());

        GeneratedPackingList result = service.generate("Munnar", 4, sampleWeather(), List.of());

        assertThat(result.source()).isEqualTo("STATIC_FALLBACK");
        assertThat(result.categories()).isNotEmpty();
        assertThat(result.categories()).allSatisfy(category -> assertThat(category.items()).isNotEmpty());
    }

    @Test
    void generate_whenGeminiReturnsMalformedJson_fallsBackToStaticChecklist() {
        when(geminiClient.generateJson(anyString())).thenReturn(Optional.of("this is not json at all"));

        GeneratedPackingList result = service.generate("Munnar", 4, sampleWeather(), List.of());

        assertThat(result.source()).isEqualTo("STATIC_FALLBACK");
    }

    @Test
    void generate_whenGeminiReturnsEmptyArray_fallsBackToStaticChecklist() {
        when(geminiClient.generateJson(anyString())).thenReturn(Optional.of("[]"));

        GeneratedPackingList result = service.generate("Munnar", 4, sampleWeather(), List.of());

        assertThat(result.source()).isEqualTo("STATIC_FALLBACK");
    }

    @Test
    void generate_whenGeminiReturnsCategoryWithNoItems_fallsBackToStaticChecklist() {
        String invalidJson = "[{\"name\": \"Clothing\", \"icon\": \"shirt\", \"items\": []}]";
        when(geminiClient.generateJson(anyString())).thenReturn(Optional.of(invalidJson));

        GeneratedPackingList result = service.generate("Munnar", 4, sampleWeather(), List.of());

        assertThat(result.source()).isEqualTo("STATIC_FALLBACK");
    }

    @Test
    void generate_whenGeminiReturnsCategoryWithBlankName_fallsBackToStaticChecklist() {
        String invalidJson = "[{\"name\": \"  \", \"icon\": \"shirt\", \"items\": [\"Something\"]}]";
        when(geminiClient.generateJson(anyString())).thenReturn(Optional.of(invalidJson));

        GeneratedPackingList result = service.generate("Munnar", 4, sampleWeather(), List.of());

        assertThat(result.source()).isEqualTo("STATIC_FALLBACK");
    }

    @Test
    void generate_withNullWeather_stillCallsGeminiAndDoesNotThrow() {
        when(geminiClient.generateJson(anyString())).thenReturn(Optional.empty());

        GeneratedPackingList result = service.generate("Munnar", 4, null, List.of("City walking tour"));

        assertThat(result.source()).isEqualTo("STATIC_FALLBACK");
    }
}