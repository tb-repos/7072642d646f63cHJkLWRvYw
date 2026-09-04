package com.tourbhook.api.service;

import com.tourbhook.api.dto.weather.WeatherResponse;

import java.time.LocalDate;

public interface WeatherService {
    WeatherResponse getWeather(String city, LocalDate date);
}
