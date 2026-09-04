package com.tourbhook.api.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.tourbhook.api.dto.packing.AddPackingCategoryItemRequest;
import com.tourbhook.api.dto.packing.AddPackingCategoryRequest;
import com.tourbhook.api.dto.packing.AddPackingItemRequest;
import com.tourbhook.api.dto.packing.PackingCategoryResponse;
import com.tourbhook.api.dto.packing.PackingItemResponse;
import com.tourbhook.api.dto.packing.PackingResponse;
import com.tourbhook.api.dto.packing.PackingTemplateResponse;
import com.tourbhook.api.dto.packing.TogglePackingCategoryRequest;
import com.tourbhook.api.dto.packing.TogglePackingItemRequest;
import com.tourbhook.api.dto.packing.UpdatePackingItemQuantityRequest;
import com.tourbhook.api.dto.packing.UpdatePackingTransportsRequest;
import com.tourbhook.api.entity.PackingCategory;
import com.tourbhook.api.entity.PackingItem;
import com.tourbhook.api.entity.PackingList;
import com.tourbhook.api.entity.Trip;
import com.tourbhook.api.repository.PackingCategoryRepository;
import com.tourbhook.api.repository.PackingListRepository;
import com.tourbhook.api.repository.exception.ResourceNotFoundException;
import com.tourbhook.api.service.PackingService;
import com.tourbhook.api.service.TripService;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import com.tourbhook.api.dto.packing.GeneratedPackingCategory;
import com.tourbhook.api.dto.packing.GeneratedPackingList;
import com.tourbhook.api.dto.weather.WeatherResponse;
import com.tourbhook.api.entity.Activity;
import com.tourbhook.api.entity.Itinerary;
import com.tourbhook.api.repository.ItineraryRepository;
import com.tourbhook.api.service.PackingRecommendationService;
import com.tourbhook.api.service.WeatherService;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PackingServiceImpl implements PackingService {

    private final PackingListRepository packingListRepository;
    private final PackingCategoryRepository packingCategoryRepository;
    private final ItineraryRepository itineraryRepository;
    private final TripService tripService;
    private final WeatherService weatherService;
    private final PackingRecommendationService packingRecommendationService;

    @Override
    public PackingResponse getPacking(String tripId) {
        PackingList packingList = getPackingList(tripId);
        ensureRecommendationsAreCurrent(packingList, false);
        return mapPacking(packingList);
    }

    @Override
    public PackingResponse regeneratePacking(String tripId) {
        PackingList packingList = getPackingList(tripId);
        ensureRecommendationsAreCurrent(packingList, true);

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

    private void ensureRecommendationsAreCurrent(PackingList packingList, boolean force) {
        Trip trip = packingList.getTrip();
        List<String> activityTitles = collectActivityTitles(trip);
        String currentSignature = computeItinerarySignature(activityTitles);

        boolean alreadyCurrent = !force && currentSignature.equals(packingList.getItinerarySignature());
        if (alreadyCurrent) {
            return;
        }

        WeatherResponse weather = safelyFetchWeather(trip);
        GeneratedPackingList generated = packingRecommendationService.generate(
                trip.getCity(), trip.getDays(), weather, activityTitles);

        applyGeneratedCategories(packingList, generated);
        packingList.setItinerarySignature(currentSignature);
        packingList.setPackingSource(generated.source());
        packingList.setPackingGeneratedAt(Instant.now());
        packingListRepository.save(packingList);

        log.info("Packing recommendations regenerated for trip {} (source={}, {} categories)",
                trip.getId(), generated.source(), generated.categories().size());
    }

    private List<String> collectActivityTitles(Trip trip) {
        return itineraryRepository.findByTrip(trip)
                .map(Itinerary::getDays)
                .orElseGet(List::of)
                .stream()
                .flatMap(day -> day.getActivities().stream())
                .map(Activity::getTitle)
                .filter(title -> title != null && !title.isBlank())
                .toList();
    }

    private WeatherResponse safelyFetchWeather(Trip trip) {
        try {
            return weatherService.getWeather(trip.getCity(), trip.getStartDate());
        } catch (Exception e) {
            log.warn("Weather lookup failed for trip {} (city={}): {}", trip.getId(), trip.getCity(), e.getMessage());
            return null;
        }
    }

    private String computeItinerarySignature(List<String> activityTitles) {
        String joined = String.join("|", activityTitles);
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(joined.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            return String.valueOf(joined.hashCode());
        }
    }

    private void applyGeneratedCategories(PackingList packingList, GeneratedPackingList generated) {
        packingList.getCategories().removeIf(category -> Boolean.TRUE.equals(category.getAiGenerated()));

        for (GeneratedPackingCategory generatedCategory : generated.categories()) {
            List<PackingItem> items = generatedCategory.items().stream()
                    .map(name -> PackingItem.builder().name(name).checked(false).quantity(1).build())
                    .collect(java.util.stream.Collectors.toCollection(ArrayList::new));

            packingList.getCategories().add(
                    PackingCategory.builder()
                            .name(generatedCategory.name())
                            .icon(generatedCategory.icon())
                            .templateOnly(false)
                            .checked(false)
                            .aiGenerated(true)
                            .items(items)
                            .build()
            );
        }
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
                        .toList(),
                packingList.getPackingSource(),
                packingList.getPackingGeneratedAt()
        );
    }
}
