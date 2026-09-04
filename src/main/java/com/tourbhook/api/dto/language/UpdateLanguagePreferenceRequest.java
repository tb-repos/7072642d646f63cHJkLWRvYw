package com.tourbhook.api.dto.language;

import jakarta.validation.constraints.NotBlank;

public record UpdateLanguagePreferenceRequest(
        @NotBlank(message = "Language code is required")
        String code
) {
}