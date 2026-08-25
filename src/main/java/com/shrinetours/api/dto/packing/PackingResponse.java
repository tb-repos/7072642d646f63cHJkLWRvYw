package com.shrinetours.api.dto.packing;

import java.util.List;

public record PackingResponse(
        String tripId,
        List<String> selectedTransports,
        List<PackingCategoryResponse> categories
) {
}
