package com.tourbhook.api.service.impl;

import com.tourbhook.api.dto.routing.ComputeRouteRequest;
import com.tourbhook.api.dto.routing.RouteResponse;
import com.tourbhook.api.entity.Place;
import com.tourbhook.api.repository.PlaceRepository;
import com.tourbhook.api.repository.exception.BadRequestException;
import com.tourbhook.api.repository.exception.ExternalServiceException;
import com.tourbhook.api.repository.exception.ResourceNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoutingServiceImplTest {

    @Mock
    private PlaceRepository placeRepository;
    @Mock
    private GoogleMapsClient googleMapsClient;

    private RoutingServiceImpl routingService;

    private Place templeA;
    private Place marketB;
    private Place fortC;

    @BeforeEach
    void setUp() {
        routingService = new RoutingServiceImpl(placeRepository, googleMapsClient);

        templeA = Place.builder().id("place-a").name("Meenakshi Temple").latitude(9.9195).longitude(78.1193).build();
        marketB = Place.builder().id("place-b").name("Puthu Mandapam Market").latitude(9.9186).longitude(78.1198).build();
        fortC = Place.builder().id("place-c").name("Thirumalai Nayak Palace").latitude(9.9151).longitude(78.1206).build();

        lenient().when(googleMapsClient.getApiKey()).thenReturn("test-api-key");
    }

    @Test
    void computeRoute_happyPath_returnsOrderedLegsAndTotals() {
        when(placeRepository.findAllById(List.of("place-a", "place-b", "place-c")))
                .thenReturn(List.of(fortC, templeA, marketB));

        when(googleMapsClient.getDirections(
                "9.9195,78.1193", "9.9151,78.1206", "9.9186,78.1198", "driving"))
                .thenReturn(googleDirectionsResponse());

        ComputeRouteRequest request = new ComputeRouteRequest(List.of("place-a", "place-b", "place-c"), "driving");
        RouteResponse response = routingService.computeRoute(request);

        assertThat(response.placeIdsInOrder()).containsExactly("place-a", "place-b", "place-c");
        assertThat(response.legs()).hasSize(2);
        assertThat(response.legs().get(0).fromPlaceId()).isEqualTo("place-a");
        assertThat(response.legs().get(0).toPlaceId()).isEqualTo("place-b");
        assertThat(response.legs().get(0).distanceMeters()).isEqualTo(500);
        assertThat(response.legs().get(0).durationSeconds()).isEqualTo(300);
        assertThat(response.legs().get(1).distanceMeters()).isEqualTo(700);
        assertThat(response.totalDistanceMeters()).isEqualTo(1200);
        assertThat(response.totalDurationSeconds()).isEqualTo(700);
        assertThat(response.overviewPolyline()).isEqualTo("encoded_polyline_string");
        assertThat(response.travelMode()).isEqualTo("driving");
    }

    @Test
    void computeRoute_defaultsToDrivingWhenTravelModeOmitted() {
        when(placeRepository.findAllById(List.of("place-a", "place-c")))
                .thenReturn(List.of(templeA, fortC));
        when(googleMapsClient.getDirections(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.eq("driving")))
                .thenReturn(googleDirectionsResponseSingleLeg());

        ComputeRouteRequest request = new ComputeRouteRequest(List.of("place-a", "place-c"), null);
        RouteResponse response = routingService.computeRoute(request);

        assertThat(response.travelMode()).isEqualTo("driving");
    }

    @Test
    void computeRoute_withInvalidTravelMode_throwsBadRequest() {
        ComputeRouteRequest request = new ComputeRouteRequest(List.of("place-a", "place-b"), "teleport");

        assertThatThrownBy(() -> routingService.computeRoute(request))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void computeRoute_whenApiKeyNotConfigured_throwsExternalServiceException() {
        when(googleMapsClient.getApiKey()).thenReturn("");
        ComputeRouteRequest request = new ComputeRouteRequest(List.of("place-a", "place-b"), "driving");

        assertThatThrownBy(() -> routingService.computeRoute(request))
                .isInstanceOf(ExternalServiceException.class);
    }

    @Test
    void computeRoute_whenPlaceMissing_throwsResourceNotFound() {
        when(placeRepository.findAllById(List.of("place-a", "unknown-place")))
                .thenReturn(List.of(templeA));

        ComputeRouteRequest request = new ComputeRouteRequest(List.of("place-a", "unknown-place"), "driving");

        assertThatThrownBy(() -> routingService.computeRoute(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void computeRoute_whenPlaceHasNoCoordinates_throwsBadRequest() {
        Place noCoords = Place.builder().id("place-d").name("Unmapped Spot").build();
        when(placeRepository.findAllById(List.of("place-a", "place-d")))
                .thenReturn(List.of(templeA, noCoords));

        ComputeRouteRequest request = new ComputeRouteRequest(List.of("place-a", "place-d"), "driving");

        assertThatThrownBy(() -> routingService.computeRoute(request))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void computeRoute_whenGoogleReturnsNonOkStatus_throwsExternalServiceException() {
        when(placeRepository.findAllById(List.of("place-a", "place-c")))
                .thenReturn(List.of(templeA, fortC));
        when(googleMapsClient.getDirections(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Map.of("status", "ZERO_RESULTS"));

        ComputeRouteRequest request = new ComputeRouteRequest(List.of("place-a", "place-c"), "driving");

        assertThatThrownBy(() -> routingService.computeRoute(request))
                .isInstanceOf(ExternalServiceException.class);
    }

    private Map<String, Object> googleDirectionsResponse() {
        Map<String, Object> leg1 = Map.of(
                "distance", Map.of("value", 500, "text", "0.5 km"),
                "duration", Map.of("value", 300, "text", "5 mins")
        );
        Map<String, Object> leg2 = Map.of(
                "distance", Map.of("value", 700, "text", "0.7 km"),
                "duration", Map.of("value", 400, "text", "7 mins")
        );
        Map<String, Object> route = Map.of(
                "legs", List.of(leg1, leg2),
                "overview_polyline", Map.of("points", "encoded_polyline_string")
        );
        return Map.of("status", "OK", "routes", List.of(route));
    }

    private Map<String, Object> googleDirectionsResponseSingleLeg() {
        Map<String, Object> leg = Map.of(
                "distance", Map.of("value", 1000, "text", "1 km"),
                "duration", Map.of("value", 600, "text", "10 mins")
        );
        Map<String, Object> route = Map.of(
                "legs", List.of(leg),
                "overview_polyline", Map.of("points", "another_encoded_polyline")
        );
        return Map.of("status", "OK", "routes", List.of(route));
    }
}