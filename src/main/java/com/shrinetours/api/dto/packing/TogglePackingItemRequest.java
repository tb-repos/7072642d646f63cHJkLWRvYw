package com.shrinetours.api.dto.packing;

public record TogglePackingItemRequest(
        Boolean is_checked,
        Integer quantity
) {
}
