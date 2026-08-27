package com.tourbhook.api.service;

import com.tourbhook.api.dto.traveller.TravellerResponse;
import com.tourbhook.api.dto.traveller.UpdateTravellerRequest;

public interface TravellerService {
    TravellerResponse updateTravellers(UpdateTravellerRequest request);
    TravellerResponse getTravellers(String tripId);
}
