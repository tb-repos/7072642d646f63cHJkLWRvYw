package com.tourbhook.api.service.impl;

import com.tourbhook.api.dto.traveller.TravellerResponse;
import com.tourbhook.api.dto.traveller.UpdateTravellerRequest;
import com.tourbhook.api.entity.Trip;
import com.tourbhook.api.repository.TripRepository;
import com.tourbhook.api.service.TravellerService;
import com.tourbhook.api.service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TravellerServiceImpl implements TravellerService {

    private final TripService tripService;
    private final TripRepository tripRepository;

    @Override
    public TravellerResponse updateTravellers(UpdateTravellerRequest request) {
        Trip trip = ((TripServiceImpl) tripService).getOwnedTrip(request.trip_id());
        trip.setAdults(request.adults());
        trip.setKids(request.kids());
        tripRepository.save(trip);
        return new TravellerResponse(trip.getId(), trip.getAdults(), trip.getKids());
    }

    @Override
    @Transactional(readOnly = true)
    public TravellerResponse getTravellers(String tripId) {
        Trip trip = ((TripServiceImpl) tripService).getOwnedTrip(tripId);
        return new TravellerResponse(trip.getId(), trip.getAdults(), trip.getKids());
    }
}
