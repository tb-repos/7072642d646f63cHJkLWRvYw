package com.shrinetours.api.dto.packing;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record TogglePackingCategoryRequest(
        @JsonProperty("checked")
        @NotNull(message = "Checked value is required")
        Boolean checked
) {
}
