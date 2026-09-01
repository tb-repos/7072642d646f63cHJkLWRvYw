package com.tourbhook.api.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tourbhook.api.dto.packing.GeneratedPackingCategory;
import com.tourbhook.api.dto.packing.GeneratedPackingList;
import com.tourbhook.api.dto.weather.WeatherResponse;
import com.tourbhook.api.service.PackingRecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PackingRecommendationServiceImpl implements PackingRecommendationService {

    private static final String SOURCE_AI = "AI";
    private static final String SOURCE_STATIC_FALLBACK = "STATIC_FALLBACK";

    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;

    @Override
    public GeneratedPackingList generate(String city, int days, WeatherResponse weather, List<String> activityTitles) {
        String prompt = buildPrompt(city, days, weather, activityTitles);

        Optional<String> aiResponse = geminiClient.generateJson(prompt);
        if (aiResponse.isEmpty()) {
            log.info("Packing AI call returned nothing for city={}, falling back to static checklist", city);
            return staticFallback();
        }

        List<GeneratedPackingCategory> parsed = parseAndValidate(aiResponse.get());
        if (parsed == null) {
            log.warn("Packing AI response for city={} failed validation, falling back to static checklist", city);
            return staticFallback();
        }

        return new GeneratedPackingList(SOURCE_AI, parsed);
    }

    private String buildPrompt(String city, int days, WeatherResponse weather, List<String> activityTitles) {
        String climateDescription = weather == null
                ? "Climate data is unavailable — pack for a mix of general conditions."
                : "Expected conditions: %s, %d-%d°C, %d%% chance of precipitation, %d%% humidity."
                .formatted(weather.condition(), weather.minTemp(), weather.maxTemp(),
                        weather.precipitationChance(), weather.humidity());

        String activitiesDescription = activityTitles.isEmpty()
                ? "No itinerary has been planned yet — recommend a general-purpose list for the destination and season."
                : "Planned activities: " + String.join(", ", activityTitles) + ".";

        return """
                You are a travel packing assistant. Generate a packing checklist for this trip:

                Destination: %s
                Trip length: %d day(s)
                %s
                %s

                Group items into logical categories (e.g. Clothing, Toiletries, Documents & Money, \
                Electronics, Health & Safety, Activity-Specific Gear). If any planned activity implies \
                specialized gear (e.g. hiking, snorkeling, formal events), include a category for it.

                Respond with ONLY a JSON array, no other text, in exactly this shape:
                [
                  { "name": "Category name", "icon": "single emoji", "items": ["item 1", "item 2"] }
                ]
                """.formatted(city, days, climateDescription, activitiesDescription);
    }

    private List<GeneratedPackingCategory> parseAndValidate(String rawJson) {
        try {
            List<GeneratedPackingCategory> categories = objectMapper.readValue(
                    rawJson, new TypeReference<List<GeneratedPackingCategory>>() {
                    });

            if (categories.isEmpty()) {
                return null;
            }

            boolean allValid = categories.stream().allMatch(category ->
                    category.name() != null && !category.name().isBlank()
                            && category.items() != null && !category.items().isEmpty());

            return allValid ? categories : null;

        } catch (Exception e) {
            log.warn("Failed to parse Gemini packing response as JSON: {}", e.getMessage());
            return null;
        }
    }

    private GeneratedPackingList staticFallback() {
        return new GeneratedPackingList(SOURCE_STATIC_FALLBACK, List.of(
                new GeneratedPackingCategory("Clothing", "\uD83D\uDC55", List.of(
                        "T-shirts", "Comfortable pants/shorts", "Underwear & socks",
                        "Light jacket or sweater", "Sleepwear", "Comfortable walking shoes")),
                new GeneratedPackingCategory("Toiletries", "\uD83E\uDDF4", List.of(
                        "Toothbrush & toothpaste", "Shampoo & soap", "Sunscreen",
                        "Deodorant", "Basic first-aid kit")),
                new GeneratedPackingCategory("Documents & Money", "\uD83D\uDCC4", List.of(
                        "ID / passport", "Travel tickets & bookings", "Cash & cards",
                        "Travel insurance details")),
                new GeneratedPackingCategory("Electronics", "\uD83D\uDD0C", List.of(
                        "Phone & charger", "Power bank", "Universal adapter (if applicable)")),
                new GeneratedPackingCategory("Health & Safety", "\uD83E\uDE79", List.of(
                        "Prescription medication", "Hand sanitizer", "Face masks", "Water bottle"))
        ));
    }
}