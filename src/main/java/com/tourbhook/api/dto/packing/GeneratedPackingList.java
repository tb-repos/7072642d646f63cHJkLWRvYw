package com.tourbhook.api.dto.packing;

import java.util.List;

public record GeneratedPackingList(
        String source,
        List<GeneratedPackingCategory> categories
) {
}