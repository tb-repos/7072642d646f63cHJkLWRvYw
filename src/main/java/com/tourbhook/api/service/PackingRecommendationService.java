package com.tourbhook.api.service;

import com.tourbhook.api.dto.packing.GeneratedPackingList;
import com.tourbhook.api.dto.weather.WeatherResponse;

import java.util.List;

public interface PackingRecommendationService {

    GeneratedPackingList generate(String city, int days, WeatherResponse weather, List<String> activityTitles);
}