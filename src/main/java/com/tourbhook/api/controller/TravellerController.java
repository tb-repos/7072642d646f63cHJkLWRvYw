package com.tourbhook.api.controller;

import com.tourbhook.api.dto.common.ApiResponse;
import com.tourbhook.api.dto.traveller.TravellerResponse;
import com.tourbhook.api.dto.traveller.UpdateTravellerRequest;
import com.tourbhook.api.service.TravellerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/travellers")
@RequiredArgsConstructor
public class TravellerController {

	private final TravellerService travellerService;

	@PostMapping("/update")
	public ResponseEntity<ApiResponse<TravellerResponse>> update(@Valid @RequestBody UpdateTravellerRequest request) {
		TravellerResponse response = travellerService.updateTravellers(request);
		return ResponseEntity
				.ok(ApiResponse.<TravellerResponse>builder().success(true).message("Travellers updated successfully")
						.data(response).timestamp(LocalDateTime.now()).path("/api/v1/travellers/update").build());
	}

	@GetMapping("/{tripId}")
	public ResponseEntity<ApiResponse<TravellerResponse>> get(@PathVariable String tripId) {
		TravellerResponse response = travellerService.getTravellers(tripId);
		return ResponseEntity
				.ok(ApiResponse.<TravellerResponse>builder().success(true).message("Travellers fetched successfully")
						.data(response).timestamp(LocalDateTime.now()).path("/api/v1/travellers/" + tripId).build());
	}
}
