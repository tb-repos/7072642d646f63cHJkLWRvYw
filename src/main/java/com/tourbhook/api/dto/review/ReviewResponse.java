package com.tourbhook.api.dto.review;

import java.time.LocalDateTime;

public record ReviewResponse(
        String id,
        String placeId,
        String userId,
        String userName,
        String userAvatarUrl,
        Integer rating,
        String comment,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}