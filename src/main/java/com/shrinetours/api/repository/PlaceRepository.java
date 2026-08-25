package com.shrinetours.api.repository;

import com.shrinetours.api.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlaceRepository extends JpaRepository<Place, String> {

    List<Place> findByCityIgnoreCase(String city);

    List<Place> findByCityIgnoreCaseAndSuggestedTrue(String city);

    Optional<Place> findByGooglePlaceId(String googlePlaceId);

    Optional<Place> findFirstByNameIgnoreCaseAndCityIgnoreCase(String name, String city);

    boolean existsByCityIgnoreCase(String city);

    boolean existsByCityIgnoreCaseAndSuggestedTrue(String city);

    @Query("""
        SELECT p FROM Place p
        WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(COALESCE(p.city, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(COALESCE(p.category, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(COALESCE(p.address, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
        ORDER BY p.suggested DESC, p.rating DESC, p.reviewsCount DESC
    """)
    List<Place> searchByKeyword(@Param("keyword") String keyword);
}