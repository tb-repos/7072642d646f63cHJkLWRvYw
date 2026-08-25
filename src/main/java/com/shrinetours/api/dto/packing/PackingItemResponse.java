package com.shrinetours.api.dto.packing;

public record PackingItemResponse(
        String id,
        String name,
        Boolean checked,
        Integer quantity
) {
}
