package com.tourbhook.api.service.impl;

import com.tourbhook.api.config.RatingWeightingProperties;
import com.tourbhook.api.entity.Place;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

class PlaceRankingServiceTest {

    private final RatingWeightingProperties properties = new RatingWeightingProperties();
    private final PlaceRankingService rankingService = new PlaceRankingService(properties);

    private Place placeWithRating(String id, String rating) {
        return Place.builder().id(id).name(id).rating(rating == null ? null : new BigDecimal(rating)).build();
    }

    @Test
    void weightFor_atOrAboveThreshold_returnsConfiguredBoost() {
        Place fourPointFive = placeWithRating("p1", "4.5");
        Place fourPointNine = placeWithRating("p2", "4.9");

        assertThat(rankingService.weightFor(fourPointFive)).isEqualTo(properties.getBoostWeight());
        assertThat(rankingService.weightFor(fourPointNine)).isEqualTo(properties.getBoostWeight());
    }

    @Test
    void weightFor_belowThreshold_returnsBaseline() {
        Place fourPointFour = placeWithRating("p1", "4.4");
        assertThat(rankingService.weightFor(fourPointFour)).isEqualTo(1.0);
    }

    @Test
    void weightFor_nullRatingOrNullPlace_returnsBaseline() {
        assertThat(rankingService.weightFor(placeWithRating("p1", null))).isEqualTo(1.0);
        assertThat(rankingService.weightFor(null)).isEqualTo(1.0);
    }

    @Test
    void rank_movesBoostedPlacesAheadOfNonBoosted() {
        Place low = placeWithRating("low", "3.0");
        Place high = placeWithRating("high", "4.7");

        List<Place> ranked = rankingService.rank(List.of(low, high), Function.identity());

        assertThat(ranked).extracting(Place::getId).containsExactly("high", "low");
    }

    @Test
    void rank_isStable_preservesOriginalOrderWithinEachGroup() {
        // Two boosted places and two non-boosted places, deliberately
        // interleaved. "All else equal" means the boosted pair keeps its
        // original relative order, and so does the non-boosted pair.
        Place boostedA = placeWithRating("boostedA", "4.6");
        Place plainA = placeWithRating("plainA", "3.0");
        Place boostedB = placeWithRating("boostedB", "4.8");
        Place plainB = placeWithRating("plainB", "3.5");

        List<Place> input = List.of(boostedA, plainA, boostedB, plainB);
        List<Place> ranked = rankingService.rank(input, Function.identity());

        assertThat(ranked).extracting(Place::getId)
                .containsExactly("boostedA", "boostedB", "plainA", "plainB");
    }

    @Test
    void rank_withNoBoostedPlaces_leavesOrderUnchanged() {
        Place a = placeWithRating("a", "3.0");
        Place b = placeWithRating("b", "4.0");
        Place c = placeWithRating("c", "2.5");

        List<Place> ranked = rankingService.rank(List.of(a, b, c), Function.identity());

        assertThat(ranked).extracting(Place::getId).containsExactly("a", "b", "c");
    }

    @Test
    void threshold_isTunable_loweringItBoostsMorePlaces() {
        RatingWeightingProperties lenient = new RatingWeightingProperties();
        lenient.setThreshold(new BigDecimal("4.0"));
        PlaceRankingService lenientRanking = new PlaceRankingService(lenient);

        Place fourPointTwo = placeWithRating("p1", "4.2");

        // Below the default 4.5 threshold...
        assertThat(rankingService.weightFor(fourPointTwo)).isEqualTo(1.0);
        // ...but boosted once the threshold is tuned down to 4.0.
        assertThat(lenientRanking.weightFor(fourPointTwo)).isEqualTo(lenient.getBoostWeight());
    }

    @Test
    void boostWeight_isTunable_changingItChangesTheScoreNotJustAFlag() {
        RatingWeightingProperties customWeight = new RatingWeightingProperties();
        customWeight.setBoostWeight(10.0);
        PlaceRankingService customRanking = new PlaceRankingService(customWeight);

        Place highRated = placeWithRating("p1", "4.8");

        assertThat(customRanking.weightFor(highRated)).isEqualTo(10.0);
    }
}