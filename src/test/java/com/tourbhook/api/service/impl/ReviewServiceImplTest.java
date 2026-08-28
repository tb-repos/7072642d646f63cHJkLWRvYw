package com.tourbhook.api.service.impl;

import com.tourbhook.api.dto.review.ReviewResponse;
import com.tourbhook.api.dto.review.SubmitReviewRequest;
import com.tourbhook.api.entity.Place;
import com.tourbhook.api.entity.Review;
import com.tourbhook.api.entity.User;
import com.tourbhook.api.repository.PlaceRepository;
import com.tourbhook.api.repository.ReviewRepository;
import com.tourbhook.api.repository.exception.DuplicateResourceException;
import com.tourbhook.api.repository.exception.ResourceNotFoundException;
import com.tourbhook.api.service.AuthenticatedUserService;
import com.tourbhook.api.dto.moderation.ModerationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.tourbhook.api.repository.exception.ContentModerationException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import com.tourbhook.api.service.AccountEnforcementService;
import com.tourbhook.api.service.ModerationService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private PlaceRepository placeRepository;
    @Mock
    private AuthenticatedUserService authenticatedUserService;
    @Mock
    private ModerationService moderationService;
    @Mock
    private AccountEnforcementService accountEnforcementService;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private User user;
    private Place place;

    @BeforeEach
    void setUp() {
        user = User.builder().id("user-1").name("Test").avatarUrl("https://cdn/avatar.png").build();
        place = Place.builder().id("place-1").name("Meenakshi Temple").rating(BigDecimal.ZERO).reviewsCount(0).build();

        lenient().when(moderationService.checkText(any())).thenReturn(ModerationResult.clean());
        lenient().when(authenticatedUserService.getCurrentUser()).thenReturn(user);
        when(placeRepository.findById("place-1")).thenReturn(Optional.of(place));
    }

    /// submitReview
    @Test
    void submitReview_whenNoExistingReview_savesAndRecomputesPlaceRating() {
        SubmitReviewRequest request = new SubmitReviewRequest(4, "Beautiful architecture");
        when(reviewRepository.existsByUserAndPlace(user, place)).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(reviewRepository.averageRatingFor(place)).thenReturn(4.0);
        when(reviewRepository.countByPlace(place)).thenReturn(1L);

        ReviewResponse response = reviewService.submitReview("place-1", request);

        assertThat(response.rating()).isEqualTo(4);
        assertThat(response.comment()).isEqualTo("Beautiful architecture");
        assertThat(response.userId()).isEqualTo("user-1");
        assertThat(place.getRating()).isEqualByComparingTo("4.00");
        assertThat(place.getReviewsCount()).isEqualTo(1);
        verify(placeRepository).save(place);
    }

    @Test
    void submitReview_whenReviewAlreadyExists_throwsDuplicateResourceException() {
        SubmitReviewRequest request = new SubmitReviewRequest(5, "Loved it");
        when(reviewRepository.existsByUserAndPlace(user, place)).thenReturn(true);

        assertThatThrownBy(() -> reviewService.submitReview("place-1", request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(reviewRepository, never()).save(any(Review.class));
        verify(placeRepository, never()).save(any(Place.class));
    }

    @Test
    void submitReview_whenPlaceMissing_throwsResourceNotFound() {
        when(placeRepository.findById("place-1")).thenReturn(Optional.empty());
        SubmitReviewRequest request = new SubmitReviewRequest(5, "Loved it");

        assertThatThrownBy(() -> reviewService.submitReview("place-1", request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void submitReview_blankCommentIsStoredAsNull() {
        SubmitReviewRequest request = new SubmitReviewRequest(3, "   ");
        when(reviewRepository.existsByUserAndPlace(user, place)).thenReturn(false);
        when(reviewRepository.averageRatingFor(place)).thenReturn(3.0);
        when(reviewRepository.countByPlace(place)).thenReturn(1L);

        ArgumentCaptor<Review> captor = ArgumentCaptor.forClass(Review.class);
        when(reviewRepository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        reviewService.submitReview("place-1", request);

        assertThat(captor.getValue().getComment()).isNull();
    }

    @Test
    void submitReview_whenModerationFlagsComment_blocksUserAndNeverSaves() {
        SubmitReviewRequest request = new SubmitReviewRequest(3, "this is a scam");
        when(moderationService.checkText("this is a scam"))
                .thenReturn(ModerationResult.flagged("Text matched a banned term"));

        assertThatThrownBy(() -> reviewService.submitReview("place-1", request))
                .isInstanceOf(ContentModerationException.class);

        verify(accountEnforcementService).blockPermanently(user, "Text matched a banned term");
        verify(reviewRepository, never()).existsByUserAndPlace(any(), any());
        verify(reviewRepository, never()).save(any(Review.class));
        verify(placeRepository, never()).save(any(Place.class));
    }

    /// updateReview

    @Test
    void updateReview_whenModerationFlagsComment_blocksUserAndNeverSaves() {
        SubmitReviewRequest request = new SubmitReviewRequest(3, "this is a scam");
        when(moderationService.checkText("this is a scam"))
                .thenReturn(ModerationResult.flagged("Text matched a banned term"));

        assertThatThrownBy(() -> reviewService.updateReview("place-1", request))
                .isInstanceOf(ContentModerationException.class);

        verify(accountEnforcementService).blockPermanently(user, "Text matched a banned term");
        verify(reviewRepository, never()).findByUserAndPlace(any(), any());
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void updateReview_whenReviewExists_updatesRatingAndComment() {
        Review existing = Review.builder().id("rev-1").user(user).place(place).rating(2).comment("Meh").build();
        when(reviewRepository.findByUserAndPlace(user, place)).thenReturn(Optional.of(existing));
        when(reviewRepository.averageRatingFor(place)).thenReturn(5.0);
        when(reviewRepository.countByPlace(place)).thenReturn(1L);

        SubmitReviewRequest request = new SubmitReviewRequest(5, "Changed my mind, it's excellent");
        ReviewResponse response = reviewService.updateReview("place-1", request);

        assertThat(response.rating()).isEqualTo(5);
        assertThat(existing.getRating()).isEqualTo(5);
        verify(reviewRepository).save(existing);
        verify(placeRepository).save(place);
    }

    @Test
    void updateReview_whenNoExistingReview_throwsResourceNotFound() {
        when(reviewRepository.findByUserAndPlace(user, place)).thenReturn(Optional.empty());
        SubmitReviewRequest request = new SubmitReviewRequest(5, "Great");

        assertThatThrownBy(() -> reviewService.updateReview("place-1", request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(reviewRepository, never()).save(any(Review.class));
    }

    /// deleteReview
    @Test
    void deleteReview_removesReviewAndRecomputesRatingToZeroWhenNoneRemain() {
        Review existing = Review.builder().id("rev-1").user(user).place(place).rating(4).build();
        when(reviewRepository.findByUserAndPlace(user, place)).thenReturn(Optional.of(existing));
        when(reviewRepository.averageRatingFor(place)).thenReturn(null);
        when(reviewRepository.countByPlace(place)).thenReturn(0L);

        reviewService.deleteReview("place-1");

        verify(reviewRepository).delete(existing);
        assertThat(place.getRating()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(place.getReviewsCount()).isZero();
    }

    @Test
    void deleteReview_whenNoReviewExists_throwsResourceNotFound() {
        when(reviewRepository.findByUserAndPlace(user, place)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.deleteReview("place-1"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(reviewRepository, never()).delete(any(Review.class));
    }

    /// Reads
    @Test
    void getReviewsForPlace_returnsReviewsOrderedNewestFirst() {
        Review review = Review.builder().id("rev-1").user(user).place(place).rating(5).comment("Amazing").build();
        when(reviewRepository.findByPlaceOrderByCreatedAtDesc(place)).thenReturn(List.of(review));

        List<ReviewResponse> responses = reviewService.getReviewsForPlace("place-1");

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).userName()).isEqualTo("Asha Rao");
    }

    @Test
    void getMyReview_whenNotReviewedYet_throwsResourceNotFound() {
        when(reviewRepository.findByUserAndPlace(user, place)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.getMyReview("place-1"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
