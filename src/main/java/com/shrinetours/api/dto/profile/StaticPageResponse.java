package com.shrinetours.api.dto.profile;

public record StaticPageResponse(
        String slug,
        String title,
        String content
) {
}
