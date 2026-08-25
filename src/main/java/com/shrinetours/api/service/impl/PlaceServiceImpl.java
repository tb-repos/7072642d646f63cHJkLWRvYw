//
//package com.shrinetours.api.service.impl;
//
//import com.shrinetours.api.dto.place.AddPlaceToTripRequest;
//import com.shrinetours.api.dto.place.PlaceResponse;
//import com.shrinetours.api.entity.Place;
//import com.shrinetours.api.repository.PlaceRepository;
//import com.shrinetours.api.repository.exception.BadRequestException;
//import com.shrinetours.api.repository.exception.ResourceNotFoundException;
//import com.shrinetours.api.service.PlaceService;
//import com.shrinetours.api.service.TripService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.math.BigDecimal;
//import java.util.ArrayList;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Locale;
//import java.util.Map;
//import java.util.Objects;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//@Transactional
//public class PlaceServiceImpl implements PlaceService {
//
//    private final PlaceRepository placeRepository;
//    private final TripService tripService;
//    private final GoogleMapsClient googleMapsClient;
//
//    @Override
//    public List<PlaceResponse> getPlaces(String city) {
//        if (city == null || city.isBlank()) {
//            return placeRepository.findAll().stream()
//                    .map(this::mapPlace)
//                    .toList();
//        }
//
//        String normalizedCity = city.trim();
//
//        List<Place> dbPlaces = placeRepository.findByCityIgnoreCase(normalizedCity);
//
//        if (dbPlaces.isEmpty()) {
//            fetchAndSavePlacesFromGoogle(normalizedCity, false, 20);
//            dbPlaces = placeRepository.findByCityIgnoreCase(normalizedCity);
//        }
//
//        return dbPlaces.stream()
//                .map(this::mapPlace)
//                .toList();
//    }
//
//    @Override
//    public List<PlaceResponse> getSuggestedPlaces(String city) {
//        if (city == null || city.isBlank()) {
//            return placeRepository.findAll().stream()
//                    .filter(Place::isSuggested)
//                    .map(this::mapPlace)
//                    .toList();
//        }
//
//        String normalizedCity = city.trim();
//
//        List<Place> suggestedPlaces = placeRepository.findByCityIgnoreCaseAndSuggestedTrue(normalizedCity);
//
//        if (suggestedPlaces.isEmpty()) {
//            fetchAndSavePlacesFromGoogle(normalizedCity, true, 10);
//            suggestedPlaces = placeRepository.findByCityIgnoreCaseAndSuggestedTrue(normalizedCity);
//        }
//
//        return suggestedPlaces.stream()
//                .map(this::mapPlace)
//                .toList();
//    }
//
//    @Override
//    public List<PlaceResponse> search(String q) {
//        if (q == null || q.isBlank()) {
//            return List.of();
//        }
//
//        String keyword = q.trim();
//
//        List<Place> dbResults = placeRepository.searchByKeyword(keyword);
//        if (!dbResults.isEmpty()) {
//            return dbResults.stream()
//                    .map(this::mapPlace)
//                    .toList();
//        }
//
//        Map<String, Object> googleResponse = googleMapsClient.textSearch(keyword);
//        validateGoogleResponse(keyword, googleResponse);
//
//        List<Map<String, Object>> results = castList(googleResponse.get("results"));
//        if (results == null || results.isEmpty()) {
//            return List.of();
//        }
//
//        List<Place> savedPlaces = new ArrayList<>();
//
//        for (Map<String, Object> item : results) {
//            try {
//                Place saved = saveOrUpdateGooglePlace(item, inferCityFromQuery(keyword), false, keyword);
//                savedPlaces.add(saved);
//            } catch (Exception e) {
//                log.error("Failed to save Google place for query '{}': {}", keyword, e.getMessage(), e);
//            }
//        }
//
//        return savedPlaces.stream()
//                .filter(Objects::nonNull)
//                .collect(java.util.stream.Collectors.toMap(
//                        Place::getId,
//                        p -> p,
//                        (a, b) -> a,
//                        LinkedHashMap::new
//                ))
//                .values()
//                .stream()
//                .map(this::mapPlace)
//                .toList();
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public List<PlaceResponse> suggested() {
//        return placeRepository.findAll().stream()
//                .filter(Place::isSuggested)
//                .map(this::mapPlace)
//                .toList();
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public PlaceResponse getPlace(String id) {
//        return placeRepository.findById(id)
//                .map(this::mapPlace)
//                .orElseThrow(() -> new ResourceNotFoundException("Place not found"));
//    }
//
//    @Override
//    public void addToTrip(AddPlaceToTripRequest request) {
//        tripService.addPlaceToTrip(request.trip_id(), request.place_id());
//    }
//
//    @Override
//    public void removeFromTrip(AddPlaceToTripRequest request) {
//        tripService.removePlaceFromTrip(request.trip_id(), request.place_id());
//    }
//
//    private void fetchAndSavePlacesFromGoogle(String city, boolean suggested, int limit) {
//        String query = suggested
//                ? "top sights in " + city
//                : "tourist attractions in " + city;
//
//        Map<String, Object> googleResponse = googleMapsClient.textSearch(query);
//        validateGoogleResponse(query, googleResponse);
//
//        List<Map<String, Object>> results = castList(googleResponse.get("results"));
//        if (results == null || results.isEmpty()) {
//            return;
//        }
//
//        int count = 0;
//        for (Map<String, Object> item : results) {
//            if (limit > 0 && count >= limit) {
//                break;
//            }
//
//            try {
//                saveOrUpdateGooglePlace(item, city, suggested, query);
//                count++;
//            } catch (Exception e) {
//                log.error("Failed saving place for city '{}': {}", city, e.getMessage(), e);
//            }
//        }
//    }
//
//    private Place saveOrUpdateGooglePlace(
//            Map<String, Object> googlePlace,
//            String fallbackCity,
//            boolean suggested,
//            String sourceQuery
//    ) {
//        String googlePlaceId = readString(googlePlace.get("place_id"));
//        if (googlePlaceId == null || googlePlaceId.isBlank()) {
//            throw new IllegalArgumentException("Google place_id missing");
//        }
//
//        String name = safeString(readString(googlePlace.get("name")), "Unknown Place");
//        String address = readString(googlePlace.get("formatted_address"));
//        String imageUrl = buildPhotoUrl(googlePlace.get("photos"));
//
//        // YAHAN YE LINE USE KARNI HAI
//        String city = safeString(fallbackCity, "Unknown");
//
//        List<?> types = googlePlace.get("types") instanceof List<?> t ? t : List.of();
//        String category = !types.isEmpty()
//                ? String.valueOf(types.get(0)).replace("_", " ")
//                : "Point of Interest";
//
//        Map<String, Object> geometry = castMap(googlePlace.get("geometry"));
//        Map<String, Object> location = geometry != null ? castMap(geometry.get("location")) : null;
//
//        Double latitude = location != null ? toDoubleOrNull(location.get("lat")) : null;
//        Double longitude = location != null ? toDoubleOrNull(location.get("lng")) : null;
//
//        BigDecimal rating = toBigDecimalOrZero(googlePlace.get("rating"));
//        Integer reviewsCount = toIntOrZero(googlePlace.get("user_ratings_total"));
//
//        Place place = placeRepository.findByGooglePlaceId(googlePlaceId)
//                .orElseGet(() -> placeRepository
//                        .findFirstByNameIgnoreCaseAndCityIgnoreCase(name, city)
//                        .orElseGet(Place::new));
//
//        place.setGooglePlaceId(googlePlaceId);
//        place.setName(name);
//        place.setCity(city);
//        place.setCategory(category);
//        place.setAddress(address);
//        place.setImageUrl(imageUrl);
//        place.setLatitude(latitude);
//        place.setLongitude(longitude);
//        place.setTypicalDuration(defaultDurationByCategory(category));
//        place.setRating(rating);
//        place.setReviewsCount(reviewsCount);
//        place.setVerified(true);
//        place.setSuggested(place.isSuggested() || suggested);
//        place.setSourceQuery(sourceQuery);
//
//        return placeRepository.save(place);
//    }
//
//    private void validateGoogleResponse(String query, Map<String, Object> googleResponse) {
//        if (googleResponse == null || googleResponse.isEmpty()) {
//            throw new BadRequestException("Google Places API returned empty response");
//        }
//
//        String status = readString(googleResponse.get("status"));
//        String errorMessage = readString(googleResponse.get("error_message"));
//
//        if ("ZERO_RESULTS".equalsIgnoreCase(status)) {
//            return;
//        }
//
//        if (status != null && !"OK".equalsIgnoreCase(status)) {
//            throw new BadRequestException(
//                    "Google Places API failed for query '" + query + "' with status: " + status +
//                            (errorMessage != null ? " (" + errorMessage + ")" : "")
//            );
//        }
//    }
//
//    private PlaceResponse mapPlace(Place place) {
//        return new PlaceResponse(
//                place.getId(),
//                place.getName(),
//                place.getCategory(),
//                place.getImageUrl(),
//                place.getTypicalDuration(),
//                place.getLatitude(),
//                place.getLongitude(),
//                place.getRating(),
//                place.getReviewsCount(),
//                place.isVerified()
//        );
//    }
//
//    private String buildPhotoUrl(Object photosObj) {
//        if (!(photosObj instanceof List<?> photos) || photos.isEmpty()) {
//            return "https://via.placeholder.com/400x300?text=No+Image";
//        }
//
//        Object firstPhotoObj = photos.get(0);
//        if (!(firstPhotoObj instanceof Map<?, ?> firstPhoto)) {
//            return "https://via.placeholder.com/400x300?text=No+Image";
//        }
//
//        Object refObj = firstPhoto.get("photo_reference");
//        if (refObj == null) {
//            return "https://via.placeholder.com/400x300?text=No+Image";
//        }
//
//        return "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photo_reference="
//                + refObj + "&key=" + googleMapsClient.getApiKey();
//    }
//
//    private String inferCityFromQuery(String query) {
//        if (query == null || query.isBlank()) return "Unknown";
//
//        String lower = query.toLowerCase(Locale.ROOT);
//
//        if (lower.contains(" in ")) {
//            return query.substring(lower.lastIndexOf(" in ") + 4).trim();
//        }
//
//        return "Unknown";
//    }
//
//    private String extractCityFromAddress(String address, String fallbackCity) {
//        if (address == null || address.isBlank()) {
//            return fallbackCity;
//        }
//
//        String[] parts = address.split(",");
//        if (parts.length >= 2) {
//            return parts[parts.length - 2].trim();
//        }
//
//        return fallbackCity;
//    }
//
//    @SuppressWarnings("unchecked")
//    private List<Map<String, Object>> castList(Object obj) {
//        return (List<Map<String, Object>>) obj;
//    }
//
//    @SuppressWarnings("unchecked")
//    private Map<String, Object> castMap(Object obj) {
//        return (Map<String, Object>) obj;
//    }
//
//    private String readString(Object obj) {
//        return obj == null ? null : obj.toString();
//    }
//
//    private Double toDoubleOrNull(Object obj) {
//        return obj instanceof Number n ? n.doubleValue() : null;
//    }
//
//    private BigDecimal toBigDecimalOrZero(Object obj) {
//        return obj instanceof Number n ? BigDecimal.valueOf(n.doubleValue()) : BigDecimal.ZERO;
//    }
//
//    private Integer toIntOrZero(Object obj) {
//        return obj instanceof Number n ? n.intValue() : 0;
//    }
//
//    private String safeString(String value, String fallback) {
//        return (value == null || value.isBlank()) ? fallback : value.trim();
//    }
//
//    private String defaultDurationByCategory(String category) {
//        if (category == null) return "2-3 hours";
//
//        String c = category.toLowerCase(Locale.ROOT);
//
//        if (c.contains("restaurant") || c.contains("cafe") || c.contains("food")) {
//            return "1-2 hours";
//        }
//        if (c.contains("park") || c.contains("garden")) {
//            return "2-3 hours";
//        }
//        if (c.contains("museum") || c.contains("art") || c.contains("gallery")) {
//            return "2-3 hours";
//        }
//        if (c.contains("temple") || c.contains("mosque") || c.contains("church")) {
//            return "1-2 hours";
//        }
//
//        return "2-3 hours";
//    }
//}

package com.shrinetours.api.service.impl;

import com.shrinetours.api.dto.place.AddPlaceToTripRequest;
import com.shrinetours.api.dto.place.PlaceResponse;
import com.shrinetours.api.entity.Place;
import com.shrinetours.api.repository.PlaceRepository;
import com.shrinetours.api.repository.exception.BadRequestException;
import com.shrinetours.api.repository.exception.ResourceNotFoundException;
import com.shrinetours.api.service.PlaceService;
import com.shrinetours.api.service.TripService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PlaceServiceImpl implements PlaceService {

    private static final int MIN_DB_RESULTS = 10;
    private static final int TARGET_RESULTS = 30;
    private static final int NEARBY_RADIUS_METERS = 12000;

    private final PlaceRepository placeRepository;
    private final TripService tripService;
    private final GoogleMapsClient googleMapsClient;

    @Override
    public List<PlaceResponse> getPlaces(String city) {
        if (city == null || city.isBlank()) {
            return placeRepository.findAll().stream()
                    .map(this::mapPlace)
                    .toList();
        }

        String normalizedCity = city.trim();
        List<Place> dbPlaces = placeRepository.findByCityIgnoreCase(normalizedCity);

        if (dbPlaces.size() < MIN_DB_RESULTS) {
            fetchAndSavePlacesFromGoogle(normalizedCity, false, TARGET_RESULTS);
            dbPlaces = placeRepository.findByCityIgnoreCase(normalizedCity);
        }

        return dbPlaces.stream()
                .filter(Objects::nonNull)
                .map(this::mapPlace)
                .toList();
    }

    @Override
    public List<PlaceResponse> getSuggestedPlaces(String city) {
        if (city == null || city.isBlank()) {
            return placeRepository.findAll().stream()
                    .filter(Place::isSuggested)
                    .map(this::mapPlace)
                    .toList();
        }

        String normalizedCity = city.trim();
        List<Place> suggestedPlaces = placeRepository.findByCityIgnoreCaseAndSuggestedTrue(normalizedCity);

        if (suggestedPlaces.size() < MIN_DB_RESULTS) {
            fetchAndSavePlacesFromGoogle(normalizedCity, true, TARGET_RESULTS);
            suggestedPlaces = placeRepository.findByCityIgnoreCaseAndSuggestedTrue(normalizedCity);
        }

        return suggestedPlaces.stream()
                .filter(Objects::nonNull)
                .map(this::mapPlace)
                .toList();
    }

    @Override
    public List<PlaceResponse> search(String q) {
        if (q == null || q.isBlank()) {
            return List.of();
        }

        String keyword = q.trim();

        List<Place> dbResults = placeRepository.searchByKeyword(keyword);
        if (!dbResults.isEmpty()) {
            return dbResults.stream()
                    .filter(Objects::nonNull)
                    .map(this::mapPlace)
                    .toList();
        }

        Map<String, Object> googleResponse = googleMapsClient.textSearch(keyword);
        validateGoogleResponse(keyword, googleResponse);

        List<Map<String, Object>> results = castList(googleResponse.get("results"));
        if (results == null || results.isEmpty()) {
            return List.of();
        }

        String inferredCity = inferCityFromQuery(keyword);
        List<Place> savedPlaces = new ArrayList<>();

        for (Map<String, Object> item : results) {
            try {
                Place saved = saveOrUpdateGooglePlace(item, inferredCity, false, keyword);
                if (saved != null) {
                    savedPlaces.add(saved);
                }
            } catch (Exception e) {
                log.error("Failed to save Google place for query '{}': {}", keyword, e.getMessage(), e);
            }
        }

        return savedPlaces.stream()
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toMap(
                        Place::getId,
                        p -> p,
                        (a, b) -> a,
                        LinkedHashMap::new
                ))
                .values()
                .stream()
                .map(this::mapPlace)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlaceResponse> suggested() {
        return placeRepository.findAll().stream()
                .filter(Place::isSuggested)
                .map(this::mapPlace)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PlaceResponse getPlace(String id) {
        return placeRepository.findById(id)
                .map(this::mapPlace)
                .orElseThrow(() -> new ResourceNotFoundException("Place not found"));
    }

    @Override
    public void addToTrip(AddPlaceToTripRequest request) {
        tripService.addPlaceToTrip(request.trip_id(), request.place_id());
    }

    @Override
    public void removeFromTrip(AddPlaceToTripRequest request) {
        tripService.removePlaceFromTrip(request.trip_id(), request.place_id());
    }

    private void fetchAndSavePlacesFromGoogle(String city, boolean suggested, int targetLimit) {
        String normalizedCity = safeString(city, "Unknown");
        List<Map<String, Object>> mergedResults = fetchMergedPlaces(normalizedCity, suggested);

        if (mergedResults.isEmpty()) {
            log.warn("No Google places found for city={}", normalizedCity);
            return;
        }

        int count = 0;
        for (Map<String, Object> item : mergedResults) {
            if (targetLimit > 0 && count >= targetLimit) {
                break;
            }

            try {
                saveOrUpdateGooglePlace(item, normalizedCity, suggested, buildSourceQuery(normalizedCity, suggested));
                count++;
            } catch (Exception e) {
                log.error("Failed saving place for city '{}': {}", normalizedCity, e.getMessage(), e);
            }
        }

        log.info("Saved/updated {} places for city={}", count, normalizedCity);
    }

    private List<Map<String, Object>> fetchMergedPlaces(String city, boolean suggested) {
        Set<String> seenPlaceIds = new LinkedHashSet<>();
        List<Map<String, Object>> merged = new ArrayList<>();

        List<String> queries = suggested
                ? List.of(
                        "top sights in " + city,
                        "best places to visit in " + city,
                        "famous tourist attractions in " + city
                )
                : List.of(
                        "tourist attractions in " + city,
                        "places to visit in " + city,
                        "best attractions in " + city
                );

        Double lat = null;
        Double lng = null;

        try {
            Map<String, Object> coordinates = googleMapsClient.geocodeCity(city);
            if (coordinates != null) {
                lat = toDoubleOrNull(coordinates.get("lat"));
                lng = toDoubleOrNull(coordinates.get("lng"));
            }
        } catch (Exception e) {
            log.warn("Failed to geocode city '{}': {}", city, e.getMessage());
        }

        for (String query : queries) {
            try {
                Map<String, Object> response = googleMapsClient.textSearch(query);
                validateGoogleResponse(query, response);

                List<Map<String, Object>> results = castList(response.get("results"));
                addUniquePlaces(results, merged, seenPlaceIds);
            } catch (Exception e) {
                log.warn("Text search failed for query '{}': {}", query, e.getMessage());
            }
        }

        if (lat != null && lng != null) {
            List<String> nearbyTypes = List.of(
                    "tourist_attraction",
                    "hindu_temple",
                    "museum",
                    "park",
                    "amusement_park",
                    "art_gallery"
            );

            for (String type : nearbyTypes) {
                try {
                    Map<String, Object> nearbyResponse =
                            googleMapsClient.nearbySearch(lat, lng, NEARBY_RADIUS_METERS, type);

                    validateGoogleResponse("nearbySearch:" + type + ":" + city, nearbyResponse);

                    List<Map<String, Object>> results = castList(nearbyResponse.get("results"));
                    addUniquePlaces(results, merged, seenPlaceIds);
                } catch (Exception e) {
                    log.warn("Nearby search failed for city '{}', type '{}': {}", city, type, e.getMessage());
                }
            }
        }

        log.info("Merged Google places count for city '{}': {}", city, merged.size());
        return merged;
    }

    private void addUniquePlaces(
            List<Map<String, Object>> source,
            List<Map<String, Object>> target,
            Set<String> seenPlaceIds
    ) {
        if (source == null || source.isEmpty()) {
            return;
        }

        for (Map<String, Object> item : source) {
            if (item == null) {
                continue;
            }

            String placeId = readString(item.get("place_id"));
            if (placeId == null || placeId.isBlank()) {
                continue;
            }

            if (seenPlaceIds.add(placeId)) {
                target.add(item);
            }
        }
    }

    private Place saveOrUpdateGooglePlace(
            Map<String, Object> googlePlace,
            String fallbackCity,
            boolean suggested,
            String sourceQuery
    ) {
        String googlePlaceId = readString(googlePlace.get("place_id"));
        if (googlePlaceId == null || googlePlaceId.isBlank()) {
            throw new IllegalArgumentException("Google place_id missing");
        }

        String name = safeString(readString(googlePlace.get("name")), "Unknown Place");
        String address = readString(googlePlace.get("formatted_address"));
        String imageUrl = buildPhotoUrl(googlePlace.get("photos"));
        String city = safeString(fallbackCity, "Unknown");

        List<?> types = googlePlace.get("types") instanceof List<?> t ? t : List.of();
        String category = !types.isEmpty()
                ? String.valueOf(types.get(0)).replace("_", " ")
                : "Point of Interest";

        Map<String, Object> geometry = castMap(googlePlace.get("geometry"));
        Map<String, Object> location = geometry != null ? castMap(geometry.get("location")) : null;

        Double latitude = location != null ? toDoubleOrNull(location.get("lat")) : null;
        Double longitude = location != null ? toDoubleOrNull(location.get("lng")) : null;

        BigDecimal rating = toBigDecimalOrZero(googlePlace.get("rating"));
        Integer reviewsCount = toIntOrZero(googlePlace.get("user_ratings_total"));

        Place place = placeRepository.findByGooglePlaceId(googlePlaceId)
                .orElseGet(() -> placeRepository
                        .findFirstByNameIgnoreCaseAndCityIgnoreCase(name, city)
                        .orElseGet(Place::new));

        place.setGooglePlaceId(googlePlaceId);
        place.setName(name);
        place.setCity(city);
        place.setCategory(category);
        place.setAddress(address);
        place.setImageUrl(imageUrl);
        place.setLatitude(latitude);
        place.setLongitude(longitude);
        place.setTypicalDuration(defaultDurationByCategory(category));
        place.setRating(rating);
        place.setReviewsCount(reviewsCount);
        place.setVerified(true);
        place.setSuggested(place.isSuggested() || suggested);
        place.setSourceQuery(sourceQuery);

        return placeRepository.save(place);
    }

    private void validateGoogleResponse(String query, Map<String, Object> googleResponse) {
        if (googleResponse == null || googleResponse.isEmpty()) {
            throw new BadRequestException("Google Places API returned empty response");
        }

        String status = readString(googleResponse.get("status"));
        String errorMessage = readString(googleResponse.get("error_message"));

        if ("ZERO_RESULTS".equalsIgnoreCase(status)) {
            return;
        }

        if (status != null && !"OK".equalsIgnoreCase(status)) {
            throw new BadRequestException(
                    "Google Places API failed for query '" + query + "' with status: " + status +
                            (errorMessage != null ? " (" + errorMessage + ")" : "")
            );
        }
    }

    private PlaceResponse mapPlace(Place place) {
        return new PlaceResponse(
                place.getId(),
                place.getName(),
                place.getCategory(),
                place.getImageUrl(),
                place.getTypicalDuration(),
                place.getLatitude(),
                place.getLongitude(),
                place.getRating(),
                place.getReviewsCount(),
                place.isVerified()
        );
    }

    private String buildPhotoUrl(Object photosObj) {
        if (!(photosObj instanceof List<?> photos) || photos.isEmpty()) {
            return "https://via.placeholder.com/400x300?text=No+Image";
        }

        Object firstPhotoObj = photos.get(0);
        if (!(firstPhotoObj instanceof Map<?, ?> firstPhoto)) {
            return "https://via.placeholder.com/400x300?text=No+Image";
        }

        Object refObj = firstPhoto.get("photo_reference");
        if (refObj == null) {
            return "https://via.placeholder.com/400x300?text=No+Image";
        }

        return "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photo_reference="
                + refObj + "&key=" + googleMapsClient.getApiKey();
    }

    private String inferCityFromQuery(String query) {
        if (query == null || query.isBlank()) {
            return "Unknown";
        }

        String lower = query.toLowerCase(Locale.ROOT);
        if (lower.contains(" in ")) {
            return query.substring(lower.lastIndexOf(" in ") + 4).trim();
        }

        return "Unknown";
    }

    private String buildSourceQuery(String city, boolean suggested) {
        return suggested ? "top sights in " + city : "tourist attractions in " + city;
    }

    private String defaultDurationByCategory(String category) {
        if (category == null) {
            return "2-3 hours";
        }

        String c = category.toLowerCase(Locale.ROOT);

        if (c.contains("restaurant") || c.contains("cafe") || c.contains("food")) {
            return "1-2 hours";
        }
        if (c.contains("park") || c.contains("garden")) {
            return "2-3 hours";
        }
        if (c.contains("museum") || c.contains("art") || c.contains("gallery")) {
            return "2-3 hours";
        }
        if (c.contains("temple") || c.contains("mosque") || c.contains("church")) {
            return "1-2 hours";
        }

        return "2-3 hours";
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> castList(Object obj) {
        return (List<Map<String, Object>>) obj;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> castMap(Object obj) {
        return (Map<String, Object>) obj;
    }

    private String readString(Object obj) {
        return obj == null ? null : obj.toString();
    }

    private Double toDoubleOrNull(Object obj) {
        return obj instanceof Number n ? n.doubleValue() : null;
    }

    private BigDecimal toBigDecimalOrZero(Object obj) {
        return obj instanceof Number n ? BigDecimal.valueOf(n.doubleValue()) : BigDecimal.ZERO;
    }

    private Integer toIntOrZero(Object obj) {
        return obj instanceof Number n ? n.intValue() : 0;
    }

    private String safeString(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value.trim();
    }
}