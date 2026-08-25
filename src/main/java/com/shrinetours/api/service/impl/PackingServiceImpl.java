package com.shrinetours.api.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shrinetours.api.dto.packing.AddPackingCategoryItemRequest;
import com.shrinetours.api.dto.packing.AddPackingCategoryRequest;
import com.shrinetours.api.dto.packing.AddPackingItemRequest;
import com.shrinetours.api.dto.packing.PackingCategoryResponse;
import com.shrinetours.api.dto.packing.PackingItemResponse;
import com.shrinetours.api.dto.packing.PackingResponse;
import com.shrinetours.api.dto.packing.PackingTemplateResponse;
import com.shrinetours.api.dto.packing.TogglePackingCategoryRequest;
import com.shrinetours.api.dto.packing.TogglePackingItemRequest;
import com.shrinetours.api.dto.packing.UpdatePackingItemQuantityRequest;
import com.shrinetours.api.dto.packing.UpdatePackingTransportsRequest;
import com.shrinetours.api.entity.PackingCategory;
import com.shrinetours.api.entity.PackingItem;
import com.shrinetours.api.entity.PackingList;
import com.shrinetours.api.entity.Trip;
import com.shrinetours.api.repository.PackingCategoryRepository;
import com.shrinetours.api.repository.PackingListRepository;
import com.shrinetours.api.repository.exception.ResourceNotFoundException;
import com.shrinetours.api.service.PackingService;
import com.shrinetours.api.service.TripService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PackingServiceImpl implements PackingService {

    private final PackingListRepository packingListRepository;
    private final PackingCategoryRepository packingCategoryRepository;
    private final TripService tripService;

    @Override
    @Transactional(readOnly = true)
    public PackingResponse getPacking(String tripId) {
        PackingList packingList = getPackingList(tripId);
        return mapPacking(packingList);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PackingTemplateResponse> getCategories() {
        return packingCategoryRepository.findByTemplateOnlyTrue().stream()
                .map(c -> new PackingTemplateResponse(c.getId(), c.getName(), c.getIcon(), c.getItems().stream().map(PackingItem::getName).toList()))
                .toList();
    }

    @Override
    public PackingResponse addItem(String tripId, AddPackingItemRequest request) {
        PackingList packingList = getPackingList(tripId);
        PackingCategory category = getCategory(packingList, request.category_id());
        category.getItems().add(PackingItem.builder().name(request.name().trim()).quantity(request.quantity()).checked(false).build());
        packingListRepository.save(packingList);
        return mapPacking(packingList);
    }

    @Override
    public PackingResponse toggleItem(String tripId, String itemId, TogglePackingItemRequest request) {
        PackingList packingList = getPackingList(tripId);
        PackingItem item = getItem(packingList, itemId);

        if (request.is_checked() != null) item.setChecked(request.is_checked());
        if (request.quantity() != null) item.setQuantity(request.quantity());
        refreshCategoryStates(packingList);
        packingListRepository.save(packingList);
        return mapPacking(packingList);
    }

    @Override
    public PackingResponse addCategory(String tripId, AddPackingCategoryRequest request) {
        PackingList packingList = getPackingList(tripId);
        packingList.getCategories().add(PackingCategory.builder()
                .name(request.name().trim())
                .icon(request.icon())
                .templateOnly(false)
                .checked(false)
                .items(new ArrayList<>())
                .build());
        packingListRepository.save(packingList);
        return mapPacking(packingList);
    }

    @Override
    public PackingResponse addCategoryItem(String tripId, String categoryId, AddPackingCategoryItemRequest request) {
        PackingList packingList = getPackingList(tripId);
        PackingCategory category = getCategory(packingList, categoryId);
        category.getItems().add(PackingItem.builder().name(request.name().trim()).quantity(request.quantity()).checked(false).build());
        category.setChecked(false);
        packingListRepository.save(packingList);
        return mapPacking(packingList);
    }

    @Override
    public PackingResponse updateItemQuantity(String tripId, String itemId, UpdatePackingItemQuantityRequest request) {
        PackingList packingList = getPackingList(tripId);
        PackingItem item = getItem(packingList, itemId);
        item.setQuantity(request.quantity());
        packingListRepository.save(packingList);
        return mapPacking(packingList);
    }

    @Override
    public PackingResponse toggleCategory(String tripId, String categoryId, TogglePackingCategoryRequest request) {
        PackingList packingList = getPackingList(tripId);
        PackingCategory category = getCategory(packingList, categoryId);
        category.setChecked(request.checked());
        for (PackingItem item : category.getItems()) {
            item.setChecked(request.checked());
        }
        packingListRepository.save(packingList);
        return mapPacking(packingList);
    }

    @Override
    public PackingResponse updateTransports(String tripId, UpdatePackingTransportsRequest request) {
        PackingList packingList = getPackingList(tripId);
        List<String> transports = request.selected_transports() == null ? List.of() : request.selected_transports().stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
        packingList.setSelectedTransports(new ArrayList<>(transports));
        packingListRepository.save(packingList);
        return mapPacking(packingList);
    }

    @Override
    public PackingResponse deleteItem(String tripId, String itemId) {
        PackingList packingList = getPackingList(tripId);
        boolean removed = false;
        for (PackingCategory category : packingList.getCategories()) {
            removed = category.getItems().removeIf(item -> item.getId().equals(itemId)) || removed;
        }
        if (!removed) {
            throw new ResourceNotFoundException("Packing item not found");
        }
        refreshCategoryStates(packingList);
        packingListRepository.save(packingList);
        return mapPacking(packingList);
    }

    private PackingCategory getCategory(PackingList packingList, String categoryId) {
        return packingList.getCategories().stream()
                .filter(c -> c.getId().equals(categoryId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }

    private PackingItem getItem(PackingList packingList, String itemId) {
        return packingList.getCategories().stream()
                .flatMap(c -> c.getItems().stream())
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Packing item not found"));
    }

    private void refreshCategoryStates(PackingList packingList) {
        for (PackingCategory category : packingList.getCategories()) {
            boolean checked = !category.getItems().isEmpty() && category.getItems().stream().allMatch(item -> Boolean.TRUE.equals(item.getChecked()));
            category.setChecked(checked);
        }
    }

    private PackingList getPackingList(String tripId) {
        Trip trip = ((TripServiceImpl) tripService).getOwnedTrip(tripId);

        return packingListRepository.findByTrip(trip)
                .orElseGet(() -> packingListRepository.save(
                        PackingList.builder()
                                .trip(trip)
                                .selectedTransports(new ArrayList<>(List.of()))
                                .categories(new ArrayList<>())
                                .build()
                ));
    }

    private PackingResponse mapPacking(PackingList packingList) {
        return new PackingResponse(
                packingList.getTrip().getId(),
                packingList.getSelectedTransports(),
                packingList.getCategories().stream()
                        .map(c -> new PackingCategoryResponse(
                                c.getId(),
                                c.getName(),
                                c.getIcon(),
                                c.getChecked(),
                                c.getItems().stream().map(i -> new PackingItemResponse(i.getId(), i.getName(), i.getChecked(), i.getQuantity())).toList()
                        ))
                        .toList()
        );
    }
}
