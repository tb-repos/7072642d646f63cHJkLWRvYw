package com.tourbhook.api.repository;

import com.tourbhook.api.entity.Itinerary;
import com.tourbhook.api.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface ItineraryRepository extends JpaRepository<Itinerary, String> {
    Optional<Itinerary> findByTrip(Trip trip);
    List<Itinerary> findAllByOrderByCityAsc();
    

}
