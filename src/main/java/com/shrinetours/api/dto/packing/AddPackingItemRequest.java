package com.shrinetours.api.dto.packing;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddPackingItemRequest(
        @NotBlank(message = "Category id is required")
        String category_id,
        @NotBlank(message = "Item name is required")
        String name,
        @NotNull(message = "Quantity is required")
        Integer quantity
) {
}
