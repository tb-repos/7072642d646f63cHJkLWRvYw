//////package com.shrinetours.api.service.impl;
//////
//////import lombok.extern.slf4j.Slf4j;
//////import org.springframework.beans.factory.annotation.Value;
//////import org.springframework.http.*;
//////import org.springframework.stereotype.Component;
//////import org.springframework.web.client.RestTemplate;
//////import org.springframework.web.util.UriComponentsBuilder;
//////
//////import java.util.Map;
//////
//////@Slf4j
//////@Component
//////public class GoogleMapsClient {
//////
//////    private final RestTemplate restTemplate = new RestTemplate();
//////
//////    @Value("${google.maps.api-key}")
//////    private String apiKey;
//////
//////    public String getApiKey() {
//////        return apiKey;
//////    }
//////
//////    public Map<String, Object> textSearch(String query) {
//////        String url = UriComponentsBuilder
//////                .fromHttpUrl("https://maps.googleapis.com/maps/api/place/textsearch/json")
//////                .queryParam("query", query)
//////                .queryParam("key", apiKey)
//////                .toUriString();
//////
//////        log.info("Google Text Search URL: {}", url.replace(apiKey, "****"));
//////
//////        ResponseEntity<Map> response = restTemplate.exchange(
//////                url,
//////                HttpMethod.GET,
//////                new HttpEntity<>(new HttpHeaders()),
//////                Map.class
//////        );
//////
//////        Map<String, Object> body = response.getBody();
//////        log.info("Google Text Search response status: {}", body != null ? body.get("status") : "null");
//////
//////        return body;
//////    }
//////
//////    public Map<String, Object> geocodeCity(String city) {
//////        String url = UriComponentsBuilder
//////                .fromHttpUrl("https://maps.googleapis.com/maps/api/geocode/json")
//////                .queryParam("address", city)
//////                .queryParam("key", apiKey)
//////                .toUriString();
//////
//////        log.info("Google Geocode URL: {}", url.replace(apiKey, "****"));
//////
//////        ResponseEntity<Map> response = restTemplate.exchange(
//////                url,
//////                HttpMethod.GET,
//////                new HttpEntity<>(new HttpHeaders()),
//////                Map.class
//////        );
//////
//////        Map<String, Object> body = response.getBody();
//////        log.info("Google Geocode response status: {}", body != null ? body.get("status") : "null");
//////
//////        return body;
//////    }
//////
//////    public Map<String, Object> getCurrentWeather(double lat, double lng) {
//////        String url = UriComponentsBuilder
//////                .fromHttpUrl("https://weather.googleapis.com/v1/currentConditions:lookup")
//////                .queryParam("key", apiKey)
//////                .queryParam("location.latitude", lat)
//////                .queryParam("location.longitude", lng)
//////                .toUriString();
//////
//////        log.info("Google Weather URL: {}", url.replace(apiKey, "****"));
//////
//////        ResponseEntity<Map> response = restTemplate.exchange(
//////                url,
//////                HttpMethod.GET,
//////                new HttpEntity<>(new HttpHeaders()),
//////                Map.class
//////        );
//////
//////        return response.getBody();
//////    }
//////}
////package com.shrinetours.api.service.impl;
////
////import lombok.extern.slf4j.Slf4j;
////import org.springframework.beans.factory.annotation.Value;
////import org.springframework.http.*;
////import org.springframework.stereotype.Component;
////import org.springframework.web.client.RestTemplate;
////import org.springframework.web.util.UriComponentsBuilder;
////
////import java.util.Collections;
////import java.util.Map;
////
////@Slf4j
////@Component
////public class GoogleMapsClient {
////
////    private final RestTemplate restTemplate = new RestTemplate();
////
////    @Value("${google.maps.api-key:}")
////    private String apiKey;
////
////    public String getApiKey() {
////        return apiKey;
////    }
////
////    public Map<String, Object> textSearch(String query) {
////        String url = UriComponentsBuilder
////                .fromHttpUrl("https://maps.googleapis.com/maps/api/place/textsearch/json")
////                .queryParam("query", query)
////                .queryParam("key", apiKey)
////                .toUriString();
////
////        log.info("Google Text Search URL: {}", safeUrl(url));
////
////        ResponseEntity<Map> response = restTemplate.exchange(
////                url,
////                HttpMethod.GET,
////                new HttpEntity<>(new HttpHeaders()),
////                Map.class
////        );
////
////        Map<String, Object> body = response.getBody();
////        log.info("Google Text Search response: {}", body);
////        return body != null ? body : Collections.emptyMap();
////    }
////
////    public Map<String, Object> geocodeCity(String city) {
////        String normalizedCity = city == null ? "" : city.trim();
////
////        if (!normalizedCity.toLowerCase().contains("india")) {
////            normalizedCity = normalizedCity + ", india";
////        }
////
////        String url = UriComponentsBuilder
////                .fromHttpUrl("https://maps.googleapis.com/maps/api/geocode/json")
////                .queryParam("address", normalizedCity)
////                .queryParam("key", apiKey)
////                .toUriString();
////
////        log.info("Google Geocode URL: {}", safeUrl(url));
////
////        ResponseEntity<Map> response = restTemplate.exchange(
////                url,
////                HttpMethod.GET,
////                new HttpEntity<>(new HttpHeaders()),
////                Map.class
////        );
////
////        Map<String, Object> body = response.getBody();
////        log.info("Google Geocode full response: {}", body);
////
////        return body != null ? body : Collections.emptyMap();
////    }
////
////    public Map<String, Object> getCurrentWeather(double lat, double lng) {
////        String url = UriComponentsBuilder
////                .fromHttpUrl("https://weather.googleapis.com/v1/currentConditions:lookup")
////                .queryParam("key", apiKey)
////                .queryParam("location.latitude", lat)
////                .queryParam("location.longitude", lng)
////                .toUriString();
////
////        log.info("Google Weather URL: {}", safeUrl(url));
////
////        ResponseEntity<Map> response = restTemplate.exchange(
////                url,
////                HttpMethod.GET,
////                new HttpEntity<>(new HttpHeaders()),
////                Map.class
////        );
////
////        Map<String, Object> body = response.getBody();
////        log.info("Google Weather full response: {}", body);
////
////        return body != null ? body : Collections.emptyMap();
////    }
////
////    private String safeUrl(String url) {
////        return apiKey == null || apiKey.isBlank() ? url : url.replace(apiKey, "****");
////    }
////}
////package com.shrinetours.api.service.impl;
////
////import lombok.extern.slf4j.Slf4j;
////import org.springframework.beans.factory.annotation.Value;
////import org.springframework.http.HttpEntity;
////import org.springframework.http.HttpHeaders;
////import org.springframework.http.HttpMethod;
////import org.springframework.http.ResponseEntity;
////import org.springframework.stereotype.Component;
////import org.springframework.web.client.RestTemplate;
////import org.springframework.web.util.UriComponentsBuilder;
////
////import java.util.Collections;
////import java.util.Map;
////
////@Slf4j
////@Component
////public class GoogleMapsClient {
////
////    private final RestTemplate restTemplate = new RestTemplate();
////
////    @Value("${google.maps.api-key:}")
////    private String apiKey;
////
////    public String getApiKey() {
////        return apiKey;
////    }
////
////    public Map<String, Object> textSearch(String query) {
////        String url = UriComponentsBuilder
////                .fromHttpUrl("https://maps.googleapis.com/maps/api/place/textsearch/json")
////                .queryParam("query", query)
////                .queryParam("key", apiKey)
////                .toUriString();
////
////        log.info("Google Text Search URL: {}", safeUrl(url));
////
////        ResponseEntity<Map> response = restTemplate.exchange(
////                url,
////                HttpMethod.GET,
////                new HttpEntity<>(new HttpHeaders()),
////                Map.class
////        );
////
////        Map<String, Object> body = response.getBody();
////        log.info("Google Text Search response: {}", body);
////
////        return body != null ? body : Collections.emptyMap();
////    }
////
////    public Map<String, Object> geocodeCity(String city) {
////        String normalizedCity = city == null ? "" : city.trim();
////
////        if (!normalizedCity.toLowerCase().contains("india")) {
////            normalizedCity = normalizedCity + ", india";
////        }
////
////        String url = UriComponentsBuilder
////                .fromHttpUrl("https://maps.googleapis.com/maps/api/geocode/json")
////                .queryParam("address", normalizedCity)
////                .queryParam("key", apiKey)
////                .toUriString();
////
////        log.info("Google Geocode URL: {}", safeUrl(url));
////
////        ResponseEntity<Map> response = restTemplate.exchange(
////                url,
////                HttpMethod.GET,
////                new HttpEntity<>(new HttpHeaders()),
////                Map.class
////        );
////
////        Map<String, Object> body = response.getBody();
////        log.info("Google Geocode full response: {}", body);
////
////        return body != null ? body : Collections.emptyMap();
////    }
////
////    public Map<String, Object> getCurrentWeather(double lat, double lng) {
////        String url = UriComponentsBuilder
////                .fromHttpUrl("https://weather.googleapis.com/v1/currentConditions:lookup")
////                .queryParam("key", apiKey)
////                .queryParam("location.latitude", lat)
////                .queryParam("location.longitude", lng)
////                .toUriString();
////
////        log.info("Google Weather URL: {}", safeUrl(url));
////
////        ResponseEntity<Map> response = restTemplate.exchange(
////                url,
////                HttpMethod.GET,
////                new HttpEntity<>(new HttpHeaders()),
////                Map.class
////        );
////
////        Map<String, Object> body = response.getBody();
////        log.info("Google Weather full response: {}", body);
////
////        return body != null ? body : Collections.emptyMap();
////    }
////
////    private String safeUrl(String url) {
////        return apiKey == null || apiKey.isBlank() ? url : url.replace(apiKey, "****");
////    }
////}//
//
//package com.shrinetours.api.service.impl;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.web.client.RestTemplateBuilder;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Component;
//import org.springframework.web.client.RestClientResponseException;
//import org.springframework.web.client.RestTemplate;
//import org.springframework.web.util.UriComponentsBuilder;
//
//import java.time.Duration;
//import java.util.Collections;
//import java.util.Map;
//
//@Slf4j
//@Component
//public class GoogleMapsClient {
//
//    private final RestTemplate restTemplate;
//
//    @Value("${google.maps.api-key:}")
//    private String apiKey;
//
//    public GoogleMapsClient(RestTemplateBuilder builder) {
//        this.restTemplate = builder
//                .setConnectTimeout(Duration.ofSeconds(10))
//                .setReadTimeout(Duration.ofSeconds(15))
//                .build();
//
//        log.info("GoogleMapsClient initialized with timeout-enabled RestTemplate");
//    }
//
//    public String getApiKey() {
//        return apiKey;
//    }
//
//    public Map<String, Object> textSearch(String query) {
//        String url = UriComponentsBuilder
//                .fromHttpUrl("https://maps.googleapis.com/maps/api/place/textsearch/json")
//                .queryParam("query", query)
//                .queryParam("key", apiKey)
//                .toUriString();
//
//        log.info("Google Text Search URL: {}", safeUrl(url));
//
//        try {
//            ResponseEntity<Map> response = restTemplate.exchange(
//                    url,
//                    HttpMethod.GET,
//                    new HttpEntity<>(new HttpHeaders()),
//                    Map.class
//            );
//
//            Map<String, Object> body = response.getBody();
//            log.info("Google Text Search response: {}", body);
//            return body != null ? body : Collections.emptyMap();
//
//        } catch (RestClientResponseException e) {
//            log.error("Google Text Search API error: status={}, body={}",
//                    e.getRawStatusCode(), e.getResponseBodyAsString(), e);
//            return Collections.emptyMap();
//        } catch (Exception e) {
//            log.error("Google Text Search unexpected error: {}", e.getMessage(), e);
//            return Collections.emptyMap();
//        }
//    }
//
//    public Map<String, Object> geocodeCity(String city) {
//        String normalizedCity = city == null ? "" : city.trim();
//
//        if (normalizedCity.isBlank()) {
//            log.warn("Geocode called with blank city");
//            return Collections.emptyMap();
//        }
//
//        if (!normalizedCity.toLowerCase().contains("india")) {
//            normalizedCity = normalizedCity + ", india";
//        }
//
//        String url = UriComponentsBuilder
//                .fromHttpUrl("https://maps.googleapis.com/maps/api/geocode/json")
//                .queryParam("address", normalizedCity)
//                .queryParam("key", apiKey)
//                .toUriString();
//
//        log.info("Google Geocode URL: {}", safeUrl(url));
//
//        try {
//            ResponseEntity<Map> response = restTemplate.exchange(
//                    url,
//                    HttpMethod.GET,
//                    new HttpEntity<>(new HttpHeaders()),
//                    Map.class
//            );
//
//            Map<String, Object> body = response.getBody();
//            log.info("Google Geocode full response: {}", body);
//            return body != null ? body : Collections.emptyMap();
//
//        } catch (RestClientResponseException e) {
//            log.error("Google Geocode API error: status={}, body={}",
//                    e.getRawStatusCode(), e.getResponseBodyAsString(), e);
//            return Collections.emptyMap();
//        } catch (Exception e) {
//            log.error("Google Geocode unexpected error: {}", e.getMessage(), e);
//            return Collections.emptyMap();
//        }
//    }
//
//    public Map<String, Object> getCurrentWeather(double lat, double lng) {
//        log.info("ENTERED getCurrentWeather method");
//
//        String url = UriComponentsBuilder
//                .fromHttpUrl("https://weather.googleapis.com/v1/currentConditions:lookup")
//                .queryParam("key", apiKey)
//                .queryParam("location.latitude", lat)
//                .queryParam("location.longitude", lng)
//                .toUriString();
//
//        log.info("Google Weather URL: {}", safeUrl(url));
//
//        try {
//            ResponseEntity<Map> response = restTemplate.exchange(
//                    url,
//                    HttpMethod.GET,
//                    new HttpEntity<>(new HttpHeaders()),
//                    Map.class
//            );
//
//            Map<String, Object> body = response.getBody();
//            log.info("Google Weather full response: {}", body);
//            return body != null ? body : Collections.emptyMap();
//
//        } catch (RestClientResponseException e) {
//            log.error("Google Weather API error: status={}, body={}",
//                    e.getRawStatusCode(), e.getResponseBodyAsString(), e);
//            return Collections.emptyMap();
//        } catch (Exception e) {
//            log.error("Google Weather unexpected error: {}", e.getMessage(), e);
//            return Collections.emptyMap();
//        }
//    }
//
//    private String safeUrl(String url) {
//        return apiKey == null || apiKey.isBlank() ? url : url.replace(apiKey, "****");
//    }
//}

package com.shrinetours.api.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class GoogleMapsClient {

    private final RestTemplate restTemplate;

    @Value("${google.maps.api-key:}")
    private String apiKey;

    public GoogleMapsClient(RestTemplateBuilder builder) {
        this.restTemplate = builder
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(15))
                .build();

        log.info("GoogleMapsClient initialized with timeout-enabled RestTemplate");
    }

    public String getApiKey() {
        return apiKey;
    }

    public Map<String, Object> textSearch(String query) {
        String url = UriComponentsBuilder
                .fromHttpUrl("https://maps.googleapis.com/maps/api/place/textsearch/json")
                .queryParam("query", query)
                .queryParam("key", apiKey)
                .toUriString();

        log.info("Google Text Search URL: {}", safeUrl(url));

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(new HttpHeaders()),
                    Map.class
            );

            Map<String, Object> body = response.getBody();
            log.info("Google Text Search response: {}", body);
            return body != null ? body : Collections.emptyMap();

        } catch (RestClientResponseException e) {
            log.error("Google Text Search API error: status={}, body={}",
                    e.getRawStatusCode(), e.getResponseBodyAsString(), e);
            return Collections.emptyMap();
        } catch (Exception e) {
            log.error("Google Text Search unexpected error: {}", e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    public Map<String, Object> nearbySearch(Double lat, Double lng, int radius, String type) {
        if (lat == null || lng == null) {
            log.warn("Nearby search called with null coordinates");
            return Collections.emptyMap();
        }

        String url = UriComponentsBuilder
                .fromHttpUrl("https://maps.googleapis.com/maps/api/place/nearbysearch/json")
                .queryParam("location", lat + "," + lng)
                .queryParam("radius", radius)
                .queryParam("type", type)
                .queryParam("key", apiKey)
                .toUriString();

        log.info("Google Nearby Search URL: {}", safeUrl(url));

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(new HttpHeaders()),
                    Map.class
            );

            Map<String, Object> body = response.getBody();
            log.info("Google Nearby Search response: {}", body);
            return body != null ? body : Collections.emptyMap();

        } catch (RestClientResponseException e) {
            log.error("Google Nearby Search API error: status={}, body={}",
                    e.getRawStatusCode(), e.getResponseBodyAsString(), e);
            return Collections.emptyMap();
        } catch (Exception e) {
            log.error("Google Nearby Search unexpected error: {}", e.getMessage(), e);
            return Collections.emptyMap();
        }
    }
    public Map<String, Object> geocodeCityRaw(String city) {
        String normalizedCity = city == null ? "" : city.trim();

        if (normalizedCity.isBlank()) {
            log.warn("Geocode called with blank city");
            return Collections.emptyMap();
        }

        if (!normalizedCity.toLowerCase().contains("india")) {
            normalizedCity = normalizedCity + ", india";
        }

        String url = UriComponentsBuilder
                .fromHttpUrl("https://maps.googleapis.com/maps/api/geocode/json")
                .queryParam("address", normalizedCity)
                .queryParam("key", apiKey)
                .toUriString();

        log.info("Google Geocode RAW URL: {}", safeUrl(url));

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(new HttpHeaders()),
                    Map.class
            );

            Map<String, Object> body = response.getBody();
            log.info("Google Geocode RAW response: {}", body);
            return body != null ? body : Collections.emptyMap();

        } catch (RestClientResponseException e) {
            log.error("Google Geocode RAW API error: status={}, body={}",
                    e.getRawStatusCode(), e.getResponseBodyAsString(), e);
            return Collections.emptyMap();
        } catch (Exception e) {
            log.error("Google Geocode RAW unexpected error: {}", e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    public Map<String, Object> geocodeCity(String city) {
        String normalizedCity = city == null ? "" : city.trim();

        if (normalizedCity.isBlank()) {
            log.warn("Geocode called with blank city");
            return Collections.emptyMap();
        }

        if (!normalizedCity.toLowerCase().contains("india")) {
            normalizedCity = normalizedCity + ", india";
        }

        String url = UriComponentsBuilder
                .fromHttpUrl("https://maps.googleapis.com/maps/api/geocode/json")
                .queryParam("address", normalizedCity)
                .queryParam("key", apiKey)
                .toUriString();

        log.info("Google Geocode URL: {}", safeUrl(url));

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(new HttpHeaders()),
                    Map.class
            );

            Map<String, Object> body = response.getBody();
            log.info("Google Geocode full response: {}", body);

            if (body == null || body.isEmpty()) {
                return Collections.emptyMap();
            }

            Object resultsObj = body.get("results");
            if (!(resultsObj instanceof List<?> results) || results.isEmpty()) {
                return Collections.emptyMap();
            }

            Object firstObj = results.get(0);
            if (!(firstObj instanceof Map<?, ?> firstResult)) {
                return Collections.emptyMap();
            }

            Object geometryObj = firstResult.get("geometry");
            if (!(geometryObj instanceof Map<?, ?> geometry)) {
                return Collections.emptyMap();
            }

            Object locationObj = geometry.get("location");
            if (!(locationObj instanceof Map<?, ?> location)) {
                return Collections.emptyMap();
            }

            Object latObj = location.get("lat");
            Object lngObj = location.get("lng");

            if (!(latObj instanceof Number latNum) || !(lngObj instanceof Number lngNum)) {
                return Collections.emptyMap();
            }

            Map<String, Object> coordinates = new HashMap<>();
            coordinates.put("lat", latNum.doubleValue());
            coordinates.put("lng", lngNum.doubleValue());
            return coordinates;

        } catch (RestClientResponseException e) {
            log.error("Google Geocode API error: status={}, body={}",
                    e.getRawStatusCode(), e.getResponseBodyAsString(), e);
            return Collections.emptyMap();
        } catch (Exception e) {
            log.error("Google Geocode unexpected error: {}", e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    public Map<String, Object> getCurrentWeather(double lat, double lng) {
        String url = UriComponentsBuilder
                .fromHttpUrl("https://weather.googleapis.com/v1/currentConditions:lookup")
                .queryParam("key", apiKey)
                .queryParam("location.latitude", lat)
                .queryParam("location.longitude", lng)
                .toUriString();

        log.info("Google Weather URL: {}", safeUrl(url));

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(new HttpHeaders()),
                    Map.class
            );

            Map<String, Object> body = response.getBody();
            log.info("Google Weather full response: {}", body);
            return body != null ? body : Collections.emptyMap();

        } catch (RestClientResponseException e) {
            log.error("Google Weather API error: status={}, body={}",
                    e.getRawStatusCode(), e.getResponseBodyAsString(), e);
            return Collections.emptyMap();
        } catch (Exception e) {
            log.error("Google Weather unexpected error: {}", e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    private String safeUrl(String url) {
        return apiKey == null || apiKey.isBlank() ? url : url.replace(apiKey, "****");
    }
}