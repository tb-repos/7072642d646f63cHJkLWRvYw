package com.shrinetours.api.dto.packing;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdatePackingTransportsRequest(
        @NotNull(message = "Selected transports are required")
        List<String> selected_transports
) {
}
