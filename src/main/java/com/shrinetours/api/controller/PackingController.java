package com.shrinetours.api.controller;

import com.shrinetours.api.dto.common.ApiResponse;
import com.shrinetours.api.dto.packing.*;
import com.shrinetours.api.service.PackingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/packing")
@RequiredArgsConstructor
public class PackingController {

	private final PackingService packingService;

	@GetMapping
	public ResponseEntity<ApiResponse<PackingResponse>> getPacking(@RequestParam("trip_id") String tripId) {
		PackingResponse response = packingService.getPacking(tripId);
		return ResponseEntity
				.ok(ApiResponse.<PackingResponse>builder().success(true).message("Packing list fetched successfully")
						.data(response).timestamp(LocalDateTime.now()).path("/api/v1/packing").build());
	}

	@GetMapping("/categories")
	public ResponseEntity<ApiResponse<List<PackingTemplateResponse>>> getCategories() {
		List<PackingTemplateResponse> response = packingService.getCategories();
		return ResponseEntity.ok(ApiResponse.<List<PackingTemplateResponse>>builder().success(true)
				.message("Packing categories fetched successfully").data(response).timestamp(LocalDateTime.now())
				.path("/api/v1/packing/categories").build());
	}

	@PostMapping("/{tripId}/add-item")
	public ResponseEntity<ApiResponse<PackingResponse>> addItem(@PathVariable String tripId,
			@Valid @RequestBody AddPackingItemRequest request) {
		PackingResponse response = packingService.addItem(tripId, request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.<PackingResponse>builder().success(true).message("Packing item added successfully")
						.data(response).timestamp(LocalDateTime.now()).path("/api/v1/packing/" + tripId + "/add-item")
						.build());
	}

	@PutMapping("/{tripId}/toggle-item/{itemId}")
	public ResponseEntity<ApiResponse<PackingResponse>> toggleItem(@PathVariable String tripId,
			@PathVariable String itemId, @RequestBody TogglePackingItemRequest request) {
		PackingResponse response = packingService.toggleItem(tripId, itemId, request);
		return ResponseEntity.ok(ApiResponse.<PackingResponse>builder().success(true)
				.message("Packing item updated successfully").data(response).timestamp(LocalDateTime.now())
				.path("/api/v1/packing/" + tripId + "/toggle-item/" + itemId).build());
	}

	@PostMapping("/{tripId}/Addcategories")
	public ResponseEntity<ApiResponse<PackingResponse>> addCategory(@PathVariable String tripId,
			@Valid @RequestBody AddPackingCategoryRequest request) {
		PackingResponse response = packingService.addCategory(tripId, request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.<PackingResponse>builder().success(true)
						.message("Packing category added successfully").data(response).timestamp(LocalDateTime.now())
						.path("/api/v1/packing/" + tripId + "/categories").build());
	}

	@PostMapping("/{tripId}/categories/{categoryId}/additems")
	public ResponseEntity<ApiResponse<PackingResponse>> addCategoryItem(@PathVariable String tripId,
			@PathVariable String categoryId, @Valid @RequestBody AddPackingCategoryItemRequest request) {
		PackingResponse response = packingService.addCategoryItem(tripId, categoryId, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<PackingResponse>builder().success(true)
				.message("Packing category item added successfully").data(response).timestamp(LocalDateTime.now())
				.path("/api/v1/packing/" + tripId + "/categories/" + categoryId + "/items").build());
	}

	@PutMapping("/{tripId}/items/{itemId}/quantity")
	public ResponseEntity<ApiResponse<PackingResponse>> updateItemQuantity(@PathVariable String tripId,
			@PathVariable String itemId, @Valid @RequestBody UpdatePackingItemQuantityRequest request) {
		PackingResponse response = packingService.updateItemQuantity(tripId, itemId, request);
		return ResponseEntity.ok(ApiResponse.<PackingResponse>builder().success(true)
				.message("Packing item quantity updated successfully").data(response).timestamp(LocalDateTime.now())
				.path("/api/v1/packing/" + tripId + "/items/" + itemId + "/quantity").build());
	}

	@PutMapping("/{tripId}/categories/{categoryId}/toggle")
	public ResponseEntity<ApiResponse<PackingResponse>> toggleCategory(@PathVariable String tripId,
			@PathVariable String categoryId, @Valid @RequestBody TogglePackingCategoryRequest request) {
		PackingResponse response = packingService.toggleCategory(tripId, categoryId, request);
		return ResponseEntity.ok(ApiResponse.<PackingResponse>builder().success(true)
				.message("Packing category updated successfully").data(response).timestamp(LocalDateTime.now())
				.path("/api/v1/packing/" + tripId + "/categories/" + categoryId + "/toggle").build());
	}

	@PutMapping("/{tripId}/transports")
	public ResponseEntity<ApiResponse<PackingResponse>> updateTransports(@PathVariable String tripId,
			@Valid @RequestBody UpdatePackingTransportsRequest request) {
		PackingResponse response = packingService.updateTransports(tripId, request);
		return ResponseEntity.ok(ApiResponse.<PackingResponse>builder()
				.success(true)
				.message("Selected transports updated successfully")
				.data(response)
				.timestamp(LocalDateTime.now())
				.path("/api/v1/packing/" + tripId + "/transports")
				.build());
	}

	@DeleteMapping("/{tripId}/items/{itemId}")
	public ResponseEntity<ApiResponse<PackingResponse>> deleteItem(@PathVariable String tripId,
			@PathVariable String itemId) {
		PackingResponse response = packingService.deleteItem(tripId, itemId);
		return ResponseEntity.ok(ApiResponse.<PackingResponse>builder().success(true)
				.message("Packing item deleted successfully").data(response).timestamp(LocalDateTime.now())
				.path("/api/v1/packing/" + tripId + "/items/" + itemId).build());
	}
}
