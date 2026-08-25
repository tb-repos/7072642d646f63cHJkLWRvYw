package com.shrinetours.api.service.impl;

import com.shrinetours.api.service.AnalyticsService;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    @Override
    public void track(String eventName, Map<String, Object> payload) {
        // no-op for now
        // later you can connect BigQuery here
    }
}