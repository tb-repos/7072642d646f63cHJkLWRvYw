package com.tourbhook.api.service.impl;

import com.tourbhook.api.dto.packing.GeneratedPackingCategory;
import com.tourbhook.api.dto.packing.GeneratedPackingList;
import com.tourbhook.api.dto.packing.PackingResponse;
import com.tourbhook.api.dto.weather.WeatherResponse;
import com.tourbhook.api.entity.Activity;
import com.tourbhook.api.entity.Itinerary;
import com.tourbhook.api.entity.ItineraryDay;
import com.tourbhook.api.entity.PackingCategory;
import com.tourbhook.api.entity.PackingList;
import com.tourbhook.api.entity.Trip;
import com.tourbhook.api.repository.ItineraryRepository;
import com.tourbhook.api.repository.PackingCategoryRepository;
import com.tourbhook.api.repository.PackingListRepository;
import com.tourbhook.api.service.PackingRecommendationService;
import com.tourbhook.api.service.WeatherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PackingServiceImplTest {

    @Mock
    private PackingListRepository packingListRepository;
    @Mock
    private PackingCategoryRepository packingCategoryRepository;
    @Mock
    private ItineraryRepository itineraryRepository;
    @Mock
    private TripServiceImpl tripService;
    @Mock
    private WeatherService weatherService;
    @Mock
    private PackingRecommendationService packingRecommendationService;

    @InjectMocks
    private PackingServiceImpl packingService;

    private Trip trip;

    @BeforeEach
    void setUp() {
        trip = Trip.builder()
                .id("trip-1")
                .city("Munnar")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(4))
                .days(4)
                .build();

        lenient().when(tripService.getOwnedTrip("trip-1")).thenReturn(trip);
        lenient().when(packingListRepository.save(any(PackingList.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private PackingList packingListFor(Trip trip) {
        return PackingList.builder()
                .id("packing-1")
                .trip(trip)
                .selectedTransports(new ArrayList<>())
                .categories(new ArrayList<>())
                .build();
    }

    private GeneratedPackingList sampleGenerated(String source) {
        return new GeneratedPackingList(source, List.of(
                new GeneratedPackingCategory("Clothing", "shirt", List.of("Rain jacket"))
        ));
    }

    @Test
    void getPacking_whenNeverGeneratedBefore_generatesAndPersists() {
        PackingList packingList = packingListFor(trip); // itinerarySignature is null
        when(packingListRepository.findByTrip(trip)).thenReturn(Optional.of(packingList));
        when(itineraryRepository.findByTrip(trip)).thenReturn(Optional.empty());
        when(weatherService.getWeather("Munnar", trip.getStartDate()))
                .thenReturn(new WeatherResponse("Munnar", trip.getStartDate(), "Cool", 15, 20, "ok", 40, "5 km/h", 60));
        when(packingRecommendationService.generate(eq("Munnar"), eq(4), any(), any()))
                .thenReturn(sampleGenerated("AI"));

        PackingResponse response = packingService.getPacking("trip-1");

        assertThat(response.source()).isEqualTo("AI");
        assertThat(response.categories()).hasSize(1);
        assertThat(response.categories().get(0).name()).isEqualTo("Clothing");
        verify(packingRecommendationService).generate(eq("Munnar"), eq(4), any(), any());
        verify(packingListRepository).save(packingList);
    }

    @Test
    void getPacking_whenSignatureUnchanged_doesNotRegenerate() {
        PackingList packingList = packingListFor(trip);
        // No itinerary at all -> signature is the hash of an empty activity list, every time.
        when(itineraryRepository.findByTrip(trip)).thenReturn(Optional.empty());
        packingList.setItinerarySignature(sha256Of(""));
        when(packingListRepository.findByTrip(trip)).thenReturn(Optional.of(packingList));

        PackingResponse response = packingService.getPacking("trip-1");

        verify(packingRecommendationService, never()).generate(any(), anyInt(), any(), any());
        assertThat(response.categories()).isEmpty();
    }

    @Test
    void getPacking_whenItineraryActivitiesChange_regenerates() {
        PackingList packingList = packingListFor(trip);
        packingList.setItinerarySignature(sha256Of("")); // matches "no itinerary" state

        Activity hiking = Activity.builder()
                .activityTime("09:00").title("Trekking at Kodaikanal").duration("3 hours")
                .cost(BigDecimal.ZERO).icon("hike").build();
        ItineraryDay day = ItineraryDay.builder().dayNumber(1).activities(List.of(hiking)).build();
        Itinerary itinerary = Itinerary.builder().trip(trip).title("Munnar Trip").city("Munnar")
                .days(List.of(day)).build();

        when(packingListRepository.findByTrip(trip)).thenReturn(Optional.of(packingList));
        when(itineraryRepository.findByTrip(trip)).thenReturn(Optional.of(itinerary));
        when(weatherService.getWeather(anyString(), any())).thenReturn(null);
        when(packingRecommendationService.generate(eq("Munnar"), eq(4), any(), eq(List.of("Trekking at Kodaikanal"))))
                .thenReturn(sampleGenerated("AI"));

        PackingResponse response = packingService.getPacking("trip-1");

        assertThat(response.categories()).hasSize(1);
        verify(packingRecommendationService).generate(eq("Munnar"), eq(4), any(), eq(List.of("Trekking at Kodaikanal")));
    }

    @Test
    void regeneratePacking_forcesRegenerationEvenWhenSignatureAlreadyMatches() {
        PackingList packingList = packingListFor(trip);
        packingList.setItinerarySignature(sha256Of("")); // already up to date by signature
        when(packingListRepository.findByTrip(trip)).thenReturn(Optional.of(packingList));
        when(itineraryRepository.findByTrip(trip)).thenReturn(Optional.empty());
        when(weatherService.getWeather(anyString(), any())).thenReturn(null);
        when(packingRecommendationService.generate(any(), anyInt(), any(), any()))
                .thenReturn(sampleGenerated("AI"));

        packingService.regeneratePacking("trip-1");

        verify(packingRecommendationService, times(1)).generate(any(), anyInt(), any(), any());
    }

    @Test
    void regeneration_preservesUserAddedCategories_onlyReplacesAiGeneratedOnes() {
        PackingList packingList = packingListFor(trip);
        PackingCategory userCategory = PackingCategory.builder()
                .id("cat-user").name("My custom stuff").aiGenerated(false).checked(false)
                .items(new ArrayList<>()).build();
        PackingCategory oldAiCategory = PackingCategory.builder()
                .id("cat-old-ai").name("Old AI category").aiGenerated(true).checked(false)
                .items(new ArrayList<>()).build();
        packingList.getCategories().add(userCategory);
        packingList.getCategories().add(oldAiCategory);
        // itinerarySignature left null so this test always triggers regeneration

        when(packingListRepository.findByTrip(trip)).thenReturn(Optional.of(packingList));
        when(itineraryRepository.findByTrip(trip)).thenReturn(Optional.empty());
        when(weatherService.getWeather(anyString(), any())).thenReturn(null);
        when(packingRecommendationService.generate(any(), anyInt(), any(), any()))
                .thenReturn(sampleGenerated("AI"));

        PackingResponse response = packingService.getPacking("trip-1");

        assertThat(response.categories()).extracting(c -> c.name())
                .contains("My custom stuff", "Clothing")
                .doesNotContain("Old AI category");
    }

    private String sha256Of(String joined) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(joined.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}