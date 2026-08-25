//package com.shrinetours.api.service;
//
//import com.shrinetours.api.dto.place.AddPlaceToTripRequest;
//import com.shrinetours.api.dto.place.PlaceResponse;
//
//import jakarta.validation.Valid;
//
//import java.util.List;
//import java.util.Map;
//
//public interface PlaceService {
//    List<PlaceResponse> getPlaces(String city);
//    List<PlaceResponse> search(String q);
//    List<PlaceResponse> suggested();
//    PlaceResponse getPlace(String id);
//    void addToTrip(AddPlaceToTripRequest request);
//	void removeFromTrip( AddPlaceToTripRequest request);
//	Map<String, Object> getNearbyGooglePlaces(String city, String type, double radius);
//}

package com.shrinetours.api.service;

import com.shrinetours.api.dto.place.AddPlaceToTripRequest;
import com.shrinetours.api.dto.place.PlaceResponse;

import java.util.List;

public interface PlaceService {
    List<PlaceResponse> getPlaces(String city);
    List<PlaceResponse> getSuggestedPlaces(String city);
    List<PlaceResponse> search(String q);
    List<PlaceResponse> suggested();
    PlaceResponse getPlace(String id);
    void addToTrip(AddPlaceToTripRequest request);
    void removeFromTrip(AddPlaceToTripRequest request);
}