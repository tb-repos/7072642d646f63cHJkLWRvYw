package com.shrinetours.api.dto.packing;

import jakarta.validation.constraints.NotBlank;

public record AddPackingCategoryRequest(
        @NotBlank(message = "Category name is required")
        String name,
        String icon
) {
}
