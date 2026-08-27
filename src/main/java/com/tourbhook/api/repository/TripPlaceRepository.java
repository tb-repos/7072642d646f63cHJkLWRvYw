package com.tourbhook.api.repository;

import com.tourbhook.api.entity.PackingList;
import com.tourbhook.api.entity.Trip;
import com.tourbhook.api.entity.TripPlace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TripPlaceRepository extends JpaRepository<TripPlace, String> {

    List<TripPlace> findByTrip(Trip trip);

    List<TripPlace> findByTrip_Id(String tripId);

    boolean existsByTrip_IdAndPlace_Id(String tripId, String placeId);

    void deleteByTrip_IdAndPlace_Id(String tripId, String placeId);

    Optional<TripPlace> findByTrip_IdAndPlace_Id(String tripId, String placeId);

    int countByTrip_Id(String tripId);

	Optional<PackingList> findByTripIdAndPlaceId(String tripId, String placeId);
}