package com.tourbhook.api.dto.language;

public record SupportedLanguageResponse(
        String code,
        String englishName,
        String nativeName,
        int tier
) {
}