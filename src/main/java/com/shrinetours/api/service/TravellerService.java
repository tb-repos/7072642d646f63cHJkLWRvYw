package com.shrinetours.api.service;

import com.shrinetours.api.dto.traveller.TravellerResponse;
import com.shrinetours.api.dto.traveller.UpdateTravellerRequest;

public interface TravellerService {
    TravellerResponse updateTravellers(UpdateTravellerRequest request);
    TravellerResponse getTravellers(String tripId);
}
