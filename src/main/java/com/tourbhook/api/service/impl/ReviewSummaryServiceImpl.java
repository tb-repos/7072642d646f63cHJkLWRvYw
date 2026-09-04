package com.tourbhook.api.service.impl;

import com.tourbhook.api.config.ReviewSummaryProperties;
import com.tourbhook.api.dto.review.ReviewSummaryResponse;
import com.tourbhook.api.entity.Place;
import com.tourbhook.api.entity.Review;
import com.tourbhook.api.repository.PlaceRepository;
import com.tourbhook.api.repository.ReviewRepository;
import com.tourbhook.api.repository.exception.ResourceNotFoundException;
import com.tourbhook.api.service.ReviewSummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReviewSummaryServiceImpl implements ReviewSummaryService {

    private final PlaceRepository placeRepository;
    private final ReviewRepository reviewRepository;
    private final GeminiClient geminiClient;
    private final ReviewSummaryProperties properties;

    @Override
    public ReviewSummaryResponse getSummary(String placeId) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new ResourceNotFoundException("Destination not found"));

        int reviewsCount = place.getReviewsCount() == null ? 0 : place.getReviewsCount();
        if (reviewsCount == 0) {
            return new ReviewSummaryResponse(place.getId(), "No reviews yet for this destination.", 0, null);
        }

        if (place.getReviewSummary() == null) {
            generateAndPersist(place);
        }

        return new ReviewSummaryResponse(
                place.getId(), place.getReviewSummary(), reviewsCount, place.getReviewSummaryGeneratedAt());
    }

    @Override
    public void onReviewsChanged(Place place) {
        int currentCount = place.getReviewsCount() == null ? 0 : place.getReviewsCount();

        if (currentCount == 0) {
            clearSummaryIfPresent(place);
            return;
        }

        int lastGeneratedAtCount = place.getReviewsCountAtLastSummary() == null
                ? 0 : place.getReviewsCountAtLastSummary();
        boolean neverGenerated = place.getReviewSummary() == null;
        boolean thresholdCrossed = (currentCount - lastGeneratedAtCount) >= properties.getRegenerationThreshold();

        if (neverGenerated || thresholdCrossed) {
            generateAndPersist(place);
        }
    }

    private void clearSummaryIfPresent(Place place) {
        if (place.getReviewSummary() == null) {
            return;
        }
        place.setReviewSummary(null);
        place.setReviewSummaryGeneratedAt(null);
        place.setReviewsCountAtLastSummary(0);
        placeRepository.save(place);
    }

    private void generateAndPersist(Place place) {
        List<Review> reviews = reviewRepository.findByPlaceOrderByCreatedAtDesc(place);
        String prompt = buildPrompt(place, reviews);

        String summary = geminiClient.generateText(prompt)
                .map(this::flattenToSingleParagraph)
                .filter(text -> !text.isBlank())
                .orElseGet(() -> staticFallbackSummary(place));

        place.setReviewSummary(summary);
        place.setReviewSummaryGeneratedAt(Instant.now());
        place.setReviewsCountAtLastSummary(place.getReviewsCount());
        placeRepository.save(place);

        log.info("Review summary regenerated for place {} ({} reviews)", place.getId(), reviews.size());
    }

    private String buildPrompt(Place place, List<Review> reviews) {
        String reviewLines = reviews.stream()
                .map(review -> "- Rating %d/5: %s".formatted(
                        review.getRating(),
                        (review.getComment() == null || review.getComment().isBlank())
                                ? "(no written comment)"
                                : review.getComment()))
                .collect(Collectors.joining("\n"));

        return """
                Summarize the following visitor reviews for "%s" into ONE concise, readable paragraph. \
                Do not use a list or bullet points in your answer — plain prose only. Highlight what \
                reviewers consistently praise and consistently complain about. Stay neutral and factual; \
                don't invent details that aren't supported by the reviews below.

                Reviews:
                %s
                """.formatted(place.getName(), reviewLines);
    }

    private String flattenToSingleParagraph(String text) {
        String withoutBulletMarkers = text.replaceAll("(?m)^\\s*[-*•]\\s*", "");
        String singleLine = withoutBulletMarkers.replaceAll("\\s*\\n+\\s*", " ").trim();
        return singleLine.replaceAll("\\s{2,}", " ");
    }

    private String staticFallbackSummary(Place place) {
        return ("This destination has %d review(s) with an average rating of %s out of 5. "
                + "AI-generated review insights are temporarily unavailable.")
                .formatted(place.getReviewsCount(), place.getRating());
    }
}