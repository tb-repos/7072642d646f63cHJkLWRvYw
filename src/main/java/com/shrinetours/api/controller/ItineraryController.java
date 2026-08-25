package com.shrinetours.api.controller;

import com.shrinetours.api.dto.common.ApiResponse;
import com.shrinetours.api.dto.itinerary.*;
import com.shrinetours.api.service.ItineraryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/itineraries")
@RequiredArgsConstructor
public class ItineraryController {

    private final ItineraryService itineraryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ItineraryRefResponse>>> getItineraries() {
        List<ItineraryRefResponse> response = itineraryService.getItineraries();
        return ResponseEntity.ok(
                ApiResponse.<List<ItineraryRefResponse>>builder()
                        .success(true)
                        .message("Itineraries fetched successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now())
                        .path("/api/v1/itineraries")
                        .build()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ItineraryResponse>> getItinerary(@PathVariable String id) {
        ItineraryResponse response = itineraryService.getItinerary(id);
        return ResponseEntity.ok(
                ApiResponse.<ItineraryResponse>builder()
                        .success(true)
                        .message("Itinerary fetched successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now())
                        .path("/api/v1/itineraries/" + id)
                        .build()
        );
    }

    @GetMapping("/{id}/activities")
    public ResponseEntity<ApiResponse<List<ActivityResponse>>> getActivities(@PathVariable String id) {
        List<ActivityResponse> response = itineraryService.getActivities(id);
        return ResponseEntity.ok(
                ApiResponse.<List<ActivityResponse>>builder()
                        .success(true)
                        .message("Activities fetched successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now())
                        .path("/api/v1/itineraries/" + id + "/activities")
                        .build()
        );
    }

    @GetMapping("/activities/{activityId}")
    public ResponseEntity<ApiResponse<ActivityResponse>> getActivity(@PathVariable String activityId) {
        ActivityResponse response = itineraryService.getActivity(activityId);
        return ResponseEntity.ok(
                ApiResponse.<ActivityResponse>builder()
                        .success(true)
                        .message("Activity fetched successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now())
                        .path("/api/v1/itineraries/activities/" + activityId)
                        .build()
        );
    }

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<ItineraryResponse>> generate(
            @Valid @RequestBody GenerateItineraryRequest request
    ) {
        ItineraryResponse response = itineraryService.generateItinerary(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        ApiResponse.<ItineraryResponse>builder()
                                .success(true)
                                .message("Itinerary generated successfully")
                                .data(response)
                                .timestamp(LocalDateTime.now())
                                .path("/api/v1/itineraries/generate")
                                .build()
                );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ItineraryResponse>> updateItinerary(
            @PathVariable String id,
            @Valid @RequestBody UpdateItineraryRequest request
    ) {
        ItineraryResponse response = itineraryService.updateItinerary(id, request);
        return ResponseEntity.ok(
                ApiResponse.<ItineraryResponse>builder()
                        .success(true)
                        .message("Itinerary updated successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now())
                        .path("/api/v1/itineraries/" + id)
                        .build()
        );
    }

    @PutMapping("/{id}/update-day")
    public ResponseEntity<ApiResponse<ItineraryResponse>> updateDay(
            @PathVariable String id,
            @Valid @RequestBody UpdateDayRequest request
    ) {
        ItineraryResponse response = itineraryService.updateDay(id, request);
        return ResponseEntity.ok(
                ApiResponse.<ItineraryResponse>builder()
                        .success(true)
                        .message("Day updated successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now())
                        .path("/api/v1/itineraries/" + id + "/update-day")
                        .build()
        );
    }

    @PostMapping("/{id}/add-activity")
    public ResponseEntity<ApiResponse<ActivityResponse>> addActivity(
            @PathVariable String id,
            @Valid @RequestBody AddActivityRequest request
    ) {
        ActivityResponse response = itineraryService.addActivity(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        ApiResponse.<ActivityResponse>builder()
                                .success(true)
                                .message("Activity added successfully")
                                .data(response)
                                .timestamp(LocalDateTime.now())
                                .path("/api/v1/itineraries/" + id + "/add-activity")
                                .build()
                );
    }

    @DeleteMapping("/remove-activity/{activityId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> removeActivity(@PathVariable String activityId) {
        itineraryService.removeActivity(activityId);
        return ResponseEntity.ok(
                ApiResponse.<Map<String, Object>>builder()
                        .success(true)
                        .message("Activity removed successfully")
                        .data(Map.of("ok", true))
                        .timestamp(LocalDateTime.now())
                        .path("/api/v1/itineraries/remove-activity/" + activityId)
                        .build()
        );
    }

    @PostMapping("/{id}/reoptimize")
    public ResponseEntity<ApiResponse<ItineraryResponse>> reoptimize(
            @PathVariable String id,
            @RequestBody ReoptimizeRequest request
    ) {
        ItineraryResponse response = itineraryService.reoptimize(id, request);
        return ResponseEntity.ok(
                ApiResponse.<ItineraryResponse>builder()
                        .success(true)
                        .message("Itinerary reoptimized successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now())
                        .path("/api/v1/itineraries/" + id + "/reoptimize")
                        .build()
        );
    }
}