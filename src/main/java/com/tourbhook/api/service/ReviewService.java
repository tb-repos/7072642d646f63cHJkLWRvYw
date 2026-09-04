package com.tourbhook.api.service;

import com.tourbhook.api.dto.review.ReviewResponse;
import com.tourbhook.api.dto.review.SubmitReviewRequest;

import java.util.List;

public interface ReviewService {

    ReviewResponse submitReview(String placeId, SubmitReviewRequest request);

    ReviewResponse updateReview(String placeId, SubmitReviewRequest request);

    void deleteReview(String placeId);

    List<ReviewResponse> getReviewsForPlace(String placeId);

    ReviewResponse getMyReview(String placeId);
}
