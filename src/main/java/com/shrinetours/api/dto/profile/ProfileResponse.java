package com.shrinetours.api.dto.profile;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProfileResponse(
        String id,
        String name,
        String email,
        String phone,
        LocalDate dob,
        String avatarUrl,
        String level,
        BigDecimal levelProgress,
        Integer tripsCompleted,
        boolean premium
) {
}
