package com.shrinetours.api.service;

import com.shrinetours.api.dto.trip.*;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public interface TripService {
    List<TripSummaryResponse> getTrips();
    TripSummaryResponse getTripById(String id);
    TripSummaryResponse createTrip(CreateTripRequest request);
    GeneratedTripResponse generateTrip(GenerateTripRequest request);
    TripDetailResponse getTripDetail(String id);
    TripDetailResponse updateTrip(String id, UpdateTripRequest request);
    void deleteTrip(String id);
    void addPlaceToTrip(String tripId, String placeId);
	void removePlaceFromTrip(String trip_id,String place_id);
	
   
}
