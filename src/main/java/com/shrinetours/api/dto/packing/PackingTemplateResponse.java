package com.shrinetours.api.dto.packing;

import java.util.List;

public record PackingTemplateResponse(
        String id,
        String name,
        String icon,
        List<String> items
) {
}
