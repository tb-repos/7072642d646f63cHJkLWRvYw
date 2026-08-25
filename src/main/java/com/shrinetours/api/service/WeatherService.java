package com.shrinetours.api.service;

import com.shrinetours.api.dto.weather.WeatherResponse;

import java.time.LocalDate;

public interface WeatherService {
    WeatherResponse getWeather(String city, LocalDate date);
}
