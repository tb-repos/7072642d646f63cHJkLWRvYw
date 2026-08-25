package com.shrinetours.api.service;

import java.util.Map;

public interface AnalyticsService {
    void track(String eventName, Map<String, Object> payload);
}