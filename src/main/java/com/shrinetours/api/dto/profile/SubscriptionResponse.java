package com.shrinetours.api.dto.profile;

import java.time.LocalDate;
import java.util.List;

public record SubscriptionResponse(
        String plan,
        String status,
        LocalDate renewsAt,
        List<String> features
) {
}
