package com.tourbhook.api.service.impl;

import com.tourbhook.api.config.RatingWeightingProperties;
import com.tourbhook.api.entity.Place;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;


@Component
@RequiredArgsConstructor
public class PlaceRankingService {

    private static final double BASELINE_WEIGHT = 1.0;

    private final RatingWeightingProperties properties;

    public double weightFor(Place place) {
        if (place == null || place.getRating() == null) {
            return BASELINE_WEIGHT;
        }
        return place.getRating().compareTo(properties.getThreshold()) >= 0
                ? properties.getBoostWeight()
                : BASELINE_WEIGHT;
    }

    public <T> List<T> rank(List<T> items, Function<T, Place> placeExtractor) {
        return items.stream()
                .sorted(Comparator
                        .comparingDouble((T item) -> weightFor(placeExtractor.apply(item)))
                        .reversed())
                .toList();
    }
}