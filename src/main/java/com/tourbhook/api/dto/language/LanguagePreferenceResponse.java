package com.tourbhook.api.dto.language;

public record LanguagePreferenceResponse(
        String code,
        String englishName,
        String nativeName
) {
}