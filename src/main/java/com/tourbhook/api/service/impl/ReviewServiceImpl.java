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
import com.tourbhook.api.service.ReviewService;
import com.tourbhook.api.dto.moderation.ModerationResult;
import com.tourbhook.api.repository.exception.ContentModerationException;
import com.tourbhook.api.service.AccountEnforcementService;
import com.tourbhook.api.service.ModerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private static final String MODERATION_BLOCK_MESSAGE =
            "Your account has been permanently blocked for violating content guidelines.";

    private final ReviewRepository reviewRepository;
    private final PlaceRepository placeRepository;
    private final AuthenticatedUserService authenticatedUserService;
    private final ModerationService moderationService;
    private final AccountEnforcementService accountEnforcementService;

    @Override
    public ReviewResponse submitReview(String placeId, SubmitReviewRequest request) {
        User user = authenticatedUserService.getCurrentUser();
        Place place = findPlace(placeId);
        enforceModeration(user, request.comment());

        if (reviewRepository.existsByUserAndPlace(user, place)) {
            throw new DuplicateResourceException(
                    "You have already reviewed this destination. Update your existing review instead.");
        }

        Review review = reviewRepository.save(
                Review.builder()
                        .user(user)
                        .place(place)
                        .rating(request.rating())
                        .comment(normalizeComment(request.comment()))
                        .build()
        );

        recomputePlaceRating(place);
        log.info("User {} submitted a review for place {}", user.getId(), place.getId());
        return mapReview(review);
    }

    @Override
    public ReviewResponse updateReview(String placeId, SubmitReviewRequest request) {
        User user = authenticatedUserService.getCurrentUser();
        Place place = findPlace(placeId);
        enforceModeration(user, request.comment());

        Review review = reviewRepository.findByUserAndPlace(user, place)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "You haven't reviewed this destination yet. Submit a review first."));

        review.setRating(request.rating());
        review.setComment(normalizeComment(request.comment()));
        reviewRepository.save(review);

        recomputePlaceRating(place);
        log.info("User {} updated their review for place {}", user.getId(), place.getId());
        return mapReview(review);
    }

    @Override
    public void deleteReview(String placeId) {
        User user = authenticatedUserService.getCurrentUser();
        Place place = findPlace(placeId);

        Review review = reviewRepository.findByUserAndPlace(user, place)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        reviewRepository.delete(review);
        recomputePlaceRating(place);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsForPlace(String placeId) {
        Place place = findPlace(placeId);
        return reviewRepository.findByPlaceOrderByCreatedAtDesc(place)
                .stream()
                .map(this::mapReview)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewResponse getMyReview(String placeId) {
        User user = authenticatedUserService.getCurrentUser();
        Place place = findPlace(placeId);

        return reviewRepository.findByUserAndPlace(user, place)
                .map(this::mapReview)
                .orElseThrow(() -> new ResourceNotFoundException("You haven't reviewed this destination yet"));
    }

    private void recomputePlaceRating(Place place) {
        Double average = reviewRepository.averageRatingFor(place);
        long count = reviewRepository.countByPlace(place);

        place.setRating(average == null
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(average).setScale(2, RoundingMode.HALF_UP));
        place.setReviewsCount(Math.toIntExact(count));
        placeRepository.save(place);
    }

    private Place findPlace(String placeId) {
        return placeRepository.findById(placeId)
                .orElseThrow(() -> new ResourceNotFoundException("Destination not found"));
    }

    private void enforceModeration(User user, String comment) {
        ModerationResult result = moderationService.checkText(comment);
        if (result.flagged()) {
            accountEnforcementService.blockPermanently(user, result.reason());
            throw new ContentModerationException(MODERATION_BLOCK_MESSAGE);
        }
    }

    private String normalizeComment(String comment) {
        return comment == null || comment.isBlank() ? null : comment.trim();
    }

    private ReviewResponse mapReview(Review review) {
        User author = review.getUser();
        return new ReviewResponse(
                review.getId(),
                review.getPlace().getId(),
                author.getId(),
                author.getName(),
                author.getAvatarUrl(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}
