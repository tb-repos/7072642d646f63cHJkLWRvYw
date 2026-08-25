//package com.shrinetours.api.config;
//
//import com.shrinetours.api.entity.*;
//import com.shrinetours.api.repository.*;
//import lombok.RequiredArgsConstructor;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.math.BigDecimal;
//import java.time.Instant;
//import java.time.LocalDate;
//import java.util.ArrayList;
//import java.util.List;
//
//@Component
//@RequiredArgsConstructor
//@Transactional
//public class DataSeeder implements CommandLineRunner {
//
//    private final UserRepository userRepository;
//    private final TripRepository tripRepository;
//    private final ItineraryRepository itineraryRepository;
//    private final PlaceRepository placeRepository;
//    private final PackingListRepository packingListRepository;
//    private final PackingCategoryRepository packingCategoryRepository;
//    private final PaymentMethodRepository paymentMethodRepository;
//    private final SubscriptionRepository subscriptionRepository;
//    private final StaticPageRepository staticPageRepository;
//    private final TripPlaceRepository tripPlaceRepository;
//    private final OtpCodeRepository otpCodeRepository;
//    private final PasswordEncoder passwordEncoder;
//
//    @Override
//    public void run(String... args) {
//        if (userRepository.count() > 0) return;
//
//        User user = userRepository.save(User.builder()
//                .name("John Doe")
//                .email("demo@shrinetours.com")
//                .password(passwordEncoder.encode("password123"))
//                .phone("9876543210")
//                .dob(LocalDate.of(1990, 1, 1))
//                .avatarUrl("https://cdn.shrinetours.local/profile/demo/avatar.png")
//                .level("Explorer")
//                .levelProgress(new BigDecimal("0.75"))
//                .tripsCompleted(5)
//                .premium(true)
//                .deleted(false)
//                .build());
//
//        otpCodeRepository.save(OtpCode.builder()
//                .email(user.getEmail())
//                .code("123456")
//                .expiresAt(Instant.now().plusSeconds(600))
//                .consumed(false)
//                .build());
//
//        seedPlaces();
//        seedPackingTemplates();
//        seedPages();
//
//        Trip trip = tripRepository.save(
//        		Trip.builder()
//                .user(user)
//                .city("Bhopal")
//                .imageUrl("https://images.unsplash.com/photo-1599661046289-e31897846e41?q=80&w=1200&auto=format&fit=crop")
//                .startDate(LocalDate.of(2026, 4, 23))
//                .endDate(LocalDate.of(2026, 4, 26))
//                .placesCount(3)
//                .days(4)
//                .adults(2)
//                .kids(1)
//                .tripStyle("spiritual")
//                .purposeofTravel("pilgrimage")
//                .build());
//
//        List<Place> bhopalPlaces = placeRepository.findByCityIgnoreCase("Bhopal");
//        bhopalPlaces.stream().limit(3).forEach(place ->
//                tripPlaceRepository.save(
//                        TripPlace.builder()
//                                .trip(trip)
//                                .place(place)
//                                .build()
//                )
//        );
//
//        Itinerary itinerary = Itinerary.builder()
//                .trip(trip)
//                .city("Bhopal")
//                .title("Itinerary for Bhopal")
//                .days(new ArrayList<>())
//                .build();
//
//        for (int i = 1; i <= 4; i++) {
//            ItineraryDay day = ItineraryDay.builder()
//                    .dayNumber(i)
//                    .activities(new ArrayList<>(List.of(
//                            Activity.builder()
//                                    .activityTime("08:30 AM")
//                                    .title("Start from Hotel")
//                                    .duration("30 min")
//                                    .cost(BigDecimal.ZERO)
//                                    .icon("car")
//                                    .build(),
//                            Activity.builder()
//                                    .activityTime("10:00 AM")
//                                    .title("Temple visit and local sightseeing")
//                                    .duration("3 hr")
//                                    .cost(BigDecimal.valueOf(500))
//                                    .icon("temple")
//                                    .build()
//                    )))
//                    .build();
//            itinerary.getDays().add(day);
//        }
//
//        itineraryRepository.save(itinerary);
//
//        PackingList packingList = PackingList.builder()
//                .trip(trip)
//                .selectedTransports(new ArrayList<>(List.of("airplane", "car")))
//                .categories(new ArrayList<>())
//                .build();
//
//        for (PackingCategory template : packingCategoryRepository.findByTemplateOnlyTrue()) {
//            PackingCategory category = PackingCategory.builder()
//                    .name(template.getName())
//                    .icon(template.getIcon())
//                    .templateOnly(false)
//                    .items(template.getItems().stream()
//                            .map(item -> PackingItem.builder()
//                                    .name(item.getName())
//                                    .checked("Passport".equalsIgnoreCase(item.getName()))
//                                    .quantity(item.getQuantity())
//                                    .build())
//                            .collect(java.util.stream.Collectors.toCollection(ArrayList::new)))
//                    .build();
//            packingList.getCategories().add(category);
//        }
//
//        packingListRepository.save(packingList);
//
//        paymentMethodRepository.saveAll(List.of(
//                PaymentMethod.builder()
//                        .user(user)
//                        .type("VISA")
//                        .lastFour("4532")
//                        .holderName("JOHN DOE")
//                        .expiry("12/27")
//                        .primaryMethod(true)
//                        .build(),
//                PaymentMethod.builder()
//                        .user(user)
//                        .type("MASTERCARD")
//                        .lastFour("8891")
//                        .holderName("JOHN DOE")
//                        .expiry("09/28")
//                        .primaryMethod(false)
//                        .build()
//        ));
//
//        subscriptionRepository.save(Subscription.builder()
//                .user(user)
//                .plan("premium")
//                .status("active")
//                .renewsAt(LocalDate.of(2026, 12, 1))
//                .features(new ArrayList<>(List.of(
//                        "unlimited_trips",
//                        "packing_assistant",
//                        "priority_support",
//                        "smart_itinerary"
//                )))
//                .build());
//    }
//
//    private void seedPages() {
//        staticPageRepository.saveAll(List.of(
//                StaticPage.builder()
//                        .slug("privacy-policy")
//                        .title("Privacy Policy")
//                        .content("This is a sample privacy policy page for the Shrine Tours mobile application.")
//                        .build(),
//                StaticPage.builder()
//                        .slug("terms-and-conditions")
//                        .title("Terms & Conditions")
//                        .content("This is a sample terms page. Replace with legal copy before production.")
//                        .build(),
//                StaticPage.builder()
//                        .slug("about-us")
//                        .title("About Us")
//                        .content("Shrine Tours helps users build smart pilgrimage and sightseeing plans.")
//                        .build()
//        ));
//    }
//
//    private void seedPlaces() {
//        placeRepository.saveAll(List.of(
//                Place.builder()
//                        .googlePlaceId("seed-bhopal-van-vihar")
//                        .city("Bhopal")
//                        .name("Van Vihar")
//                        .category("Nature")
//                        .address("Van Vihar National Park, Bhopal, Madhya Pradesh, India")
//                        .imageUrl("https://images.unsplash.com/photo-1564507592333-c60657eea523?q=80&w=800&auto=format&fit=crop")
//                        .latitude(23.2307)
//                        .longitude(77.3928)
//                        .typicalDuration("Typically requires 3h")
//                        .rating(BigDecimal.valueOf(4.8))
//                        .reviewsCount(47)
//                        .verified(true)
//                        .suggested(false)
//                        .sourceQuery("seed-data")
//                        .build(),
//
//                Place.builder()
//                        .googlePlaceId("seed-bhopal-sanchi-stupa")
//                        .city("Bhopal")
//                        .name("Sanchi Stupa")
//                        .category("Historical")
//                        .address("Sanchi, Raisen district, Madhya Pradesh, India")
//                        .imageUrl("https://images.unsplash.com/photo-1524499982521-1ffd58dd89ea?q=80&w=800&auto=format&fit=crop")
//                        .latitude(23.4794)
//                        .longitude(77.7397)
//                        .typicalDuration("Typically requires 2h")
//                        .rating(BigDecimal.valueOf(4.7))
//                        .reviewsCount(110)
//                        .verified(true)
//                        .suggested(true)
//                        .sourceQuery("seed-data")
//                        .build(),
//
//                Place.builder()
//                        .googlePlaceId("seed-bhopal-bhojpur-temple")
//                        .city("Bhopal")
//                        .name("Bhojpur Temple")
//                        .category("Religious")
//                        .address("Bhojpur, Madhya Pradesh, India")
//                        .imageUrl("https://images.unsplash.com/photo-1589308078059-be1415eab4c3?q=80&w=800&auto=format&fit=crop")
//                        .latitude(23.1045)
//                        .longitude(77.6086)
//                        .typicalDuration("Typically requires 1h")
//                        .rating(BigDecimal.valueOf(4.5))
//                        .reviewsCount(66)
//                        .verified(true)
//                        .suggested(true)
//                        .sourceQuery("seed-data")
//                        .build(),
//
//                Place.builder()
//                        .googlePlaceId("seed-bhopal-upper-lake-boat-club")
//                        .city("Bhopal")
//                        .name("Upper Lake Boat Club")
//                        .category("Leisure")
//                        .address("Upper Lake, Bhopal, Madhya Pradesh, India")
//                        .imageUrl("https://images.unsplash.com/photo-1506744038136-46273834b3fb?q=80&w=800&auto=format&fit=crop")
//                        .latitude(23.2380)
//                        .longitude(77.3940)
//                        .typicalDuration("Typically requires 2h")
//                        .rating(BigDecimal.valueOf(4.4))
//                        .reviewsCount(95)
//                        .verified(true)
//                        .suggested(true)
//                        .sourceQuery("seed-data")
//                        .build(),
//
//                Place.builder()
//                        .googlePlaceId("seed-varanasi-kashi-vishwanath")
//                        .city("Varanasi")
//                        .name("Kashi Vishwanath Temple")
//                        .category("Religious")
//                        .address("Varanasi, Uttar Pradesh, India")
//                        .imageUrl("https://images.unsplash.com/photo-1621416894569-0f39ed31d247?q=80&w=800&auto=format&fit=crop")
//                        .latitude(25.3109)
//                        .longitude(83.0107)
//                        .typicalDuration("Typically requires 2h")
//                        .rating(BigDecimal.valueOf(4.9))
//                        .reviewsCount(1500)
//                        .verified(true)
//                        .suggested(true)
//                        .sourceQuery("seed-data")
//                        .build(),
//
//                Place.builder()
//                        .googlePlaceId("seed-goa-baga-beach")
//                        .city("Goa")
//                        .name("Baga Beach")
//                        .category("Beach")
//                        .address("Baga, Goa, India")
//                        .imageUrl("https://images.unsplash.com/photo-1507525428034-b723cf961d3e?q=80&w=800&auto=format&fit=crop")
//                        .latitude(15.5553)
//                        .longitude(73.7517)
//                        .typicalDuration("Typically requires 4h")
//                        .rating(BigDecimal.valueOf(4.7))
//                        .reviewsCount(501)
//                        .verified(true)
//                        .suggested(true)
//                        .sourceQuery("seed-data")
//                        .build(),
//                        
//                        
//                Place.builder()
//                        .googlePlaceId("seed-indore-rajwada-palace")
//                        .city("Indore")
//                        .name("Rajwada Palace")
//                        .category("Historical")
//                        .address("Rajwada, Indore, Madhya Pradesh, India")
//                        .imageUrl("https://images.unsplash.com/photo-1564507592333-c60657eea523?q=80&w=800&auto=format&fit=crop")
//                        .latitude(22.7177)
//                        .longitude(75.8545)
//                        .typicalDuration("Typically requires 1h")
//                        .rating(BigDecimal.valueOf(4.5))
//                        .reviewsCount(842)
//                        .verified(true)
//                        .suggested(true)
//                        .sourceQuery("seed-data")
//                        .build(),
//
//                Place.builder()
//                        .googlePlaceId("seed-indore-lal-bagh-palace")
//                        .city("Indore")
//                        .name("Lal Bagh Palace")
//                        .category("Historical")
//                        .address("Nehru Park Road, Indore, Madhya Pradesh, India")
//                        .imageUrl("https://images.unsplash.com/photo-1524499982521-1ffd58dd89ea?q=80&w=800&auto=format&fit=crop")
//                        .latitude(22.7006)
//                        .longitude(75.8610)
//                        .typicalDuration("Typically requires 2h")
//                        .rating(BigDecimal.valueOf(4.6))
//                        .reviewsCount(522)
//                        .verified(true)
//                        .suggested(true)
//                        .sourceQuery("seed-data")
//                        .build(),
//
//                Place.builder()
//                        .googlePlaceId("seed-indore-khajrana-ganesh")
//                        .city("Indore")
//                        .name("Khajrana Ganesh Temple")
//                        .category("Religious")
//                        .address("Khajrana, Indore, Madhya Pradesh, India")
//                        .imageUrl("https://images.unsplash.com/photo-1589308078059-be1415eab4c3?q=80&w=800&auto=format&fit=crop")
//                        .latitude(22.7196)
//                        .longitude(75.9047)
//                        .typicalDuration("Typically requires 1h")
//                        .rating(BigDecimal.valueOf(4.8))
//                        .reviewsCount(2301)
//                        .verified(true)
//                        .suggested(true)
//                        .sourceQuery("seed-data")
//                        .build()
//
//        ));
//    }
//
//    private void seedPackingTemplates() {
//
//        PackingCategory essentials = PackingCategory.builder()
//                .name("Essentials")
//                .icon("luggage")
//                .templateOnly(true)
//                .items(new ArrayList<>(List.of(
//                        PackingItem.builder().name("Passport").checked(false).quantity(1).build(),
//                        PackingItem.builder().name("Wallet").checked(false).quantity(1).build(),
//                        PackingItem.builder().name("Phone Charger").checked(false).quantity(1).build()
//                )))
//                .build();
//
//        PackingCategory airplane = PackingCategory.builder()
//                .name("Airplane")
//                .icon("flight")
//                .templateOnly(true)
//                .items(new ArrayList<>(List.of(
//                        PackingItem.builder().name("Boarding Pass").checked(false).quantity(1).build(),
//                        PackingItem.builder().name("Neck Pillow").checked(false).quantity(1).build(),
//                        PackingItem.builder().name("Earbuds").checked(false).quantity(1).build(),
//                        PackingItem.builder().name("Eye Mask").checked(false).quantity(1).build(),
//                        PackingItem.builder().name("Snacks").checked(false).quantity(1).build()
//                )))
//                .build();
//
//        PackingCategory bus = PackingCategory.builder()
//                .name("Bus")
//                .icon("directions_bus")
//                .templateOnly(true)
//                .items(new ArrayList<>(List.of(
//                        PackingItem.builder().name("Bus Ticket").checked(false).quantity(1).build(),
//                        PackingItem.builder().name("Neck Pillow").checked(false).quantity(1).build(),
//                        PackingItem.builder().name("Headphone").checked(false).quantity(2).build()
//                )))
//                .build();
//
//        PackingCategory hotel = PackingCategory.builder()
//                .name("Hotel")
//                .icon("hotel")
//                .templateOnly(true)
//                .items(new ArrayList<>(List.of(
//                        PackingItem.builder().name("Toiletries").checked(false).quantity(1).build(),
//                        PackingItem.builder().name("Charger").checked(false).quantity(1).build()
//                )))
//                .build();
//
//        PackingCategory international = PackingCategory.builder()
//                .name("International")
//                .icon("public")
//                .templateOnly(true)
//                .items(new ArrayList<>(List.of(
//                        PackingItem.builder().name("Visa Documents").checked(false).quantity(1).build(),
//                        PackingItem.builder().name("Travel Insurance").checked(false).quantity(1).build()
//                )))
//                .build();
//
//        PackingCategory personal = PackingCategory.builder()
//                .name("Personal")
//                .icon("person")
//                .templateOnly(true)
//                .items(new ArrayList<>(List.of(
//                        PackingItem.builder().name("Medication").checked(false).quantity(1).build(),
//                        PackingItem.builder().name("Sunscreen").checked(false).quantity(1).build()
//                )))
//                .build();
//
//        packingCategoryRepository.saveAll(
//                List.of(essentials, airplane, bus, hotel, international, personal)
//        );
//    }
//}