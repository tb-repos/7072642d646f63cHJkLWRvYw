package com.tourbhook.api.controller;

import com.tourbhook.api.dto.common.ApiResponse;
import com.tourbhook.api.dto.weather.WeatherResponse;
import com.tourbhook.api.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/weather")
@RequiredArgsConstructor
public class WeatherController {

	private final WeatherService weatherService;

	@GetMapping
	public ResponseEntity<ApiResponse<WeatherResponse>> getWeather(@RequestParam String city,
			@RequestParam(required = false) LocalDate date) {
		WeatherResponse response = weatherService.getWeather(city, date != null ? date : LocalDate.now());
		return ResponseEntity
				.ok(ApiResponse.<WeatherResponse>builder().success(true).message("Weather fetched successfully")
						.data(response).timestamp(LocalDateTime.now()).path("/api/v1/weather").build());
	}
}
