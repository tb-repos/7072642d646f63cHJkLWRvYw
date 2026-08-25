package com.shrinetours.api.service;

import com.shrinetours.api.dto.packing.*;

import java.util.List;

public interface PackingService {
    PackingResponse getPacking(String tripId);
    List<PackingTemplateResponse> getCategories();
    PackingResponse addItem(String tripId, AddPackingItemRequest request);
    PackingResponse toggleItem(String tripId, String itemId, TogglePackingItemRequest request);
    PackingResponse addCategory(String tripId, AddPackingCategoryRequest request);
    PackingResponse addCategoryItem(String tripId, String categoryId, AddPackingCategoryItemRequest request);
    PackingResponse updateItemQuantity(String tripId, String itemId, UpdatePackingItemQuantityRequest request);
    PackingResponse toggleCategory(String tripId, String categoryId, TogglePackingCategoryRequest request);
    PackingResponse updateTransports(String tripId, UpdatePackingTransportsRequest request);
    PackingResponse deleteItem(String tripId, String itemId);
}
