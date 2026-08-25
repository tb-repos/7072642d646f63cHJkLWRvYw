package com.shrinetours.api.controller;

import com.shrinetours.api.dto.common.ApiResponse;
import com.shrinetours.api.dto.trip.*;
import com.shrinetours.api.service.TripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
public class TripController {

	private final TripService tripService;

	@GetMapping
	public ResponseEntity<ApiResponse<List<TripSummaryResponse>>> getTrips() {
		List<TripSummaryResponse> response = tripService.getTrips();
		return ResponseEntity
				.ok(ApiResponse.<List<TripSummaryResponse>>builder().success(true).message("Trips fetched successfully")
						.data(response).timestamp(LocalDateTime.now()).path("/api/v1/trips").build());
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<TripDetailResponse>> getTripById(@PathVariable String id) {
		TripDetailResponse response = tripService.getTripDetail(id);
		return ResponseEntity
				.ok(ApiResponse.<TripDetailResponse>builder().success(true).message("Trip fetched successfully")
						.data(response).timestamp(LocalDateTime.now()).path("/api/v1/trips/" + id).build());
	}

	@PostMapping
	public ResponseEntity<ApiResponse<TripSummaryResponse>> createTrip(@Valid @RequestBody CreateTripRequest request) {
		TripSummaryResponse response = tripService.createTrip(request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.<TripSummaryResponse>builder().success(true).message("Trip created successfully")
						.data(response).timestamp(LocalDateTime.now()).path("/api/v1/trips").build());
	}

	@PostMapping("/generate")
	public ResponseEntity<ApiResponse<GeneratedTripResponse>> generateTrip(
			@Valid @RequestBody GenerateTripRequest request) {
		GeneratedTripResponse response = tripService.generateTrip(request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.<GeneratedTripResponse>builder().success(true)
						.message("Itinerary generated successfully").data(response).timestamp(LocalDateTime.now())
						.path("/api/v1/trips/generate").build());
	}

	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<TripDetailResponse>> updateTrip(@PathVariable String id,
			@RequestBody UpdateTripRequest request) {
		TripDetailResponse response = tripService.updateTrip(id, request);
		return ResponseEntity
				.ok(ApiResponse.<TripDetailResponse>builder().success(true).message("Trip updated successfully")
						.data(response).timestamp(LocalDateTime.now()).path("/api/v1/trips/" + id).build());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<Map<String, Object>>> deleteTrip(@PathVariable String id) {
		tripService.deleteTrip(id);
		return ResponseEntity
				.ok(ApiResponse.<Map<String, Object>>builder().success(true).message("Trip deleted successfully")
						.data(Map.of("ok", true)).timestamp(LocalDateTime.now()).path("/api/v1/trips/" + id).build());
	}
}
