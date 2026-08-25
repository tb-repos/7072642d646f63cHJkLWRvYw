package com.shrinetours.api.repository;

import com.shrinetours.api.entity.Itinerary;
import com.shrinetours.api.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface ItineraryRepository extends JpaRepository<Itinerary, String> {
    Optional<Itinerary> findByTrip(Trip trip);
    List<Itinerary> findAllByOrderByCityAsc();
    

}
