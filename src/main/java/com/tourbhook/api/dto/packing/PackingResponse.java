package com.tourbhook.api.dto.packing;

import java.util.List;
import java.time.Instant;

public record PackingResponse(
        String tripId,
        List<String> selectedTransports,
        List<PackingCategoryResponse> categories,
        String source,
        Instant generatedAt

) {
}
