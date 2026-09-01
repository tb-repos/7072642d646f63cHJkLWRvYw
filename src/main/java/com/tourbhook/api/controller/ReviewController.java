package com.tourbhook.api.controller;

import com.tourbhook.api.dto.common.ApiResponse;
import com.tourbhook.api.dto.review.ReviewResponse;
import com.tourbhook.api.dto.review.SubmitReviewRequest;
import com.tourbhook.api.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.tourbhook.api.dto.review.ReviewSummaryResponse;
import com.tourbhook.api.service.ReviewSummaryService;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/places/{placeId}/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final ReviewSummaryService reviewSummaryService;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<ReviewSummaryResponse>> getSummary(@PathVariable String placeId) {
        ReviewSummaryResponse response = reviewSummaryService.getSummary(placeId);
        return ResponseEntity.ok(
                ApiResponse.<ReviewSummaryResponse>builder()
                        .success(true)
                        .message("Review summary fetched successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now())
                        .path("/api/v1/places/" + placeId + "/reviews/summary")
                        .build()
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getReviews(@PathVariable String placeId) {
        List<ReviewResponse> response = reviewService.getReviewsForPlace(placeId);
        return ResponseEntity.ok(
                ApiResponse.<List<ReviewResponse>>builder()
                        .success(true)
                        .message("Reviews fetched successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now())
                        .path("/api/v1/places/" + placeId + "/reviews")
                        .build()
        );
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<ReviewResponse>> getMyReview(@PathVariable String placeId) {
        ReviewResponse response = reviewService.getMyReview(placeId);
        return ResponseEntity.ok(
                ApiResponse.<ReviewResponse>builder()
                        .success(true)
                        .message("Review fetched successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now())
                        .path("/api/v1/places/" + placeId + "/reviews/me")
                        .build()
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> submitReview(
            @PathVariable String placeId,
            @Valid @RequestBody SubmitReviewRequest request) {
        ReviewResponse response = reviewService.submitReview(placeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<ReviewResponse>builder()
                        .success(true)
                        .message("Review submitted successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now())
                        .path("/api/v1/places/" + placeId + "/reviews")
                        .build()
        );
    }

    @PutMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            @PathVariable String placeId,
            @Valid @RequestBody SubmitReviewRequest request) {
        ReviewResponse response = reviewService.updateReview(placeId, request);
        return ResponseEntity.ok(
                ApiResponse.<ReviewResponse>builder()
                        .success(true)
                        .message("Review updated successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now())
                        .path("/api/v1/places/" + placeId + "/reviews")
                        .build()
        );
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable String placeId) {
        reviewService.deleteReview(placeId);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Review deleted successfully")
                        .timestamp(LocalDateTime.now())
                        .path("/api/v1/places/" + placeId + "/reviews")
                        .build()
        );
    }
}
