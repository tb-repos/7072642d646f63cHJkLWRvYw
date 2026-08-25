package com.shrinetours.api.dto.weather;

import java.time.LocalDate;

public record WeatherResponse(
        String city,
        LocalDate date,
        String condition,
        int minTemp,
        int maxTemp,
        String summary,
        int precipitationChance,
        String wind,
        int humidity
) {
}