package com.shrinetours.api.dto.packing;

import java.util.List;

public record PackingCategoryResponse(
        String id,
        String name,
        String icon,
        Boolean checked,
        List<PackingItemResponse> items
) {
}
