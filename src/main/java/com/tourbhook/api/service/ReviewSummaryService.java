package com.tourbhook.api.service;

import com.tourbhook.api.dto.review.ReviewSummaryResponse;
import com.tourbhook.api.entity.Place;

public interface ReviewSummaryService {

    ReviewSummaryResponse getSummary(String placeId);

    void onReviewsChanged(Place place);
}