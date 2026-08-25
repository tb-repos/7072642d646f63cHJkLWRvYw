package com.shrinetours.api.dto.packing;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AddPackingCategoryItemRequest(
        @NotBlank(message = "Item name is required")
        String name,
        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be greater than 0")
        Integer quantity
) {
}
