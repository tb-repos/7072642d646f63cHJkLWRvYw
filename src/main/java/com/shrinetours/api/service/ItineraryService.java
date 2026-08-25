package com.shrinetours.api.service;

import com.shrinetours.api.dto.itinerary.*;

import java.util.List;

public interface ItineraryService {
    ItineraryResponse getItinerary(String id);
    List<ItineraryRefResponse> getItineraries();
    List<ActivityResponse> getActivities(String itineraryId);
    ActivityResponse getActivity(String activityId);
    ItineraryResponse generateItinerary(GenerateItineraryRequest request);

    ItineraryResponse updateItinerary(String itineraryId, UpdateItineraryRequest request);

    ItineraryResponse updateDay(String itineraryId, UpdateDayRequest request);
    ActivityResponse addActivity(String itineraryId, AddActivityRequest request);
    void removeActivity(String activityId);
    ItineraryResponse reoptimize(String itineraryId, ReoptimizeRequest request);
}