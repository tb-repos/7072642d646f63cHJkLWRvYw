package com.tourbhook.api.repository;

import com.tourbhook.api.entity.Place;
import com.tourbhook.api.entity.Review;
import com.tourbhook.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, String> {

    List<Review> findByPlaceOrderByCreatedAtDesc(Place place);

    Optional<Review> findByUserAndPlace(User user, Place place);

    boolean existsByUserAndPlace(User user, Place place);

    long countByPlace(Place place);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.place = :place")
    Double averageRatingFor(@Param("place") Place place);
}
