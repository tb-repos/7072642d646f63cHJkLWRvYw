package com.tourbhook.api.service.impl;

import com.tourbhook.api.config.ReviewSummaryProperties;
import com.tourbhook.api.dto.review.ReviewSummaryResponse;
import com.tourbhook.api.entity.Place;
import com.tourbhook.api.entity.Review;
import com.tourbhook.api.repository.PlaceRepository;
import com.tourbhook.api.repository.ReviewRepository;
import com.tourbhook.api.repository.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewSummaryServiceImplTest {

    @Mock
    private PlaceRepository placeRepository;
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private GeminiClient geminiClient;

    private ReviewSummaryProperties properties;
    private ReviewSummaryServiceImpl service;

    @BeforeEach
    void setUp() {
        properties = new ReviewSummaryProperties();
        properties.setRegenerationThreshold(10);
        service = new ReviewSummaryServiceImpl(placeRepository, reviewRepository, geminiClient, properties);

        lenient().when(placeRepository.save(any(Place.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private Place placeWith(int reviewsCount, Integer lastSummaryCount, String existingSummary) {
        return Place.builder()
                .id("place-1")
                .name("Meenakshi Temple")
                .rating(new BigDecimal("4.20"))
                .reviewsCount(reviewsCount)
                .reviewsCountAtLastSummary(lastSummaryCount)
                .reviewSummary(existingSummary)
                .build();
    }


    /// getSummary

    @Test
    void getSummary_whenPlaceMissing_throwsResourceNotFound() {
        when(placeRepository.findById("place-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getSummary("place-1"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getSummary_whenNoReviewsYet_returnsCannedMessageWithoutCallingAi() {
        Place place = placeWith(0, 0, null);
        when(placeRepository.findById("place-1")).thenReturn(Optional.of(place));

        ReviewSummaryResponse response = service.getSummary("place-1");

        assertThat(response.summary()).isEqualTo("No reviews yet for this destination.");
        assertThat(response.reviewsCount()).isZero();
        verify(geminiClient, never()).generateText(anyString());
    }

    @Test
    void getSummary_whenNeverGenerated_generatesOnFirstRequestEvenBelowThreshold() {
        Place place = placeWith(2, 0, null); // only 2 reviews, well below threshold of 10
        when(placeRepository.findById("place-1")).thenReturn(Optional.of(place));
        when(reviewRepository.findByPlaceOrderByCreatedAtDesc(place)).thenReturn(List.of(
                review(place, 5, "Absolutely stunning architecture")
        ));
        when(geminiClient.generateText(anyString())).thenReturn(Optional.of("A lovely, well-reviewed temple."));

        ReviewSummaryResponse response = service.getSummary("place-1");

        assertThat(response.summary()).isEqualTo("A lovely, well-reviewed temple.");
        verify(geminiClient).generateText(anyString());
    }

    @Test
    void getSummary_whenAlreadyGeneratedAndBelowThreshold_returnsExistingWithoutRegenerating() {
        Place place = placeWith(5, 5, "Existing summary text");
        when(placeRepository.findById("place-1")).thenReturn(Optional.of(place));

        ReviewSummaryResponse response = service.getSummary("place-1");

        assertThat(response.summary()).isEqualTo("Existing summary text");
        verify(geminiClient, never()).generateText(anyString());
    }

    /// onReviewsChanged

    @Test
    void onReviewsChanged_whenReviewsDropToZero_clearsExistingSummary() {
        Place place = placeWith(0, 8, "Old summary");

        service.onReviewsChanged(place);

        assertThat(place.getReviewSummary()).isNull();
        assertThat(place.getReviewsCountAtLastSummary()).isZero();
        verify(placeRepository).save(place);
    }

    @Test
    void onReviewsChanged_whenAlreadyZeroAndNoSummaryExists_doesNothing() {
        Place place = placeWith(0, 0, null);

        service.onReviewsChanged(place);

        verify(placeRepository, never()).save(any(Place.class));
    }

    @Test
    void onReviewsChanged_belowThreshold_doesNotRegenerate() {
        Place place = placeWith(7, 3, "Existing summary"); // only 4 new reviews since last summary
        properties.setRegenerationThreshold(10);

        service.onReviewsChanged(place);

        verify(geminiClient, never()).generateText(anyString());
        verify(placeRepository, never()).save(any(Place.class));
    }

    @Test
    void onReviewsChanged_thresholdCrossed_regenerates() {
        Place place = placeWith(15, 3, "Old summary"); // 12 new reviews since last summary, threshold 10
        when(reviewRepository.findByPlaceOrderByCreatedAtDesc(place)).thenReturn(List.of(
                review(place, 4, "Good but crowded"),
                review(place, 5, "Loved it")
        ));
        when(geminiClient.generateText(anyString())).thenReturn(Optional.of("Generally well-liked, though crowded at peak times."));

        service.onReviewsChanged(place);

        assertThat(place.getReviewSummary()).isEqualTo("Generally well-liked, though crowded at peak times.");
        assertThat(place.getReviewsCountAtLastSummary()).isEqualTo(15);
        verify(placeRepository).save(place);
    }

    @Test
    void onReviewsChanged_whenGeminiFails_fallsBackToDeterministicSummary() {
        Place place = placeWith(15, 0, null);
        when(reviewRepository.findByPlaceOrderByCreatedAtDesc(place)).thenReturn(List.of());
        when(geminiClient.generateText(anyString())).thenReturn(Optional.empty());

        service.onReviewsChanged(place);

        assertThat(place.getReviewSummary())
                .contains("15 review(s)")
                .contains("4.20")
                .contains("temporarily unavailable");
    }

    @Test
    void onReviewsChanged_flattensBulletedAiResponseIntoOneParagraph() {
        Place place = placeWith(15, 0, null);
        when(reviewRepository.findByPlaceOrderByCreatedAtDesc(place)).thenReturn(List.of());
        when(geminiClient.generateText(anyString())).thenReturn(Optional.of(
                "- Great location\n- Friendly staff\n- A bit expensive"));

        service.onReviewsChanged(place);

        assertThat(place.getReviewSummary()).doesNotContain("\n").doesNotContain("- ");
        assertThat(place.getReviewSummary()).contains("Great location");
    }

    private Review review(Place place, int rating, String comment) {
        return Review.builder().place(place).rating(rating).comment(comment).build();
    }
}