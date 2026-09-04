package com.tourbhook.api.controller;

import com.tourbhook.api.dto.common.ApiResponse;
import com.tourbhook.api.dto.routing.ComputeRouteRequest;
import com.tourbhook.api.dto.routing.RouteResponse;
import com.tourbhook.api.service.RoutingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/routing")
@RequiredArgsConstructor
public class RoutingController {

    private static final String PATH = "/api/v1/routing/route";

    private final RoutingService routingService;

    @PostMapping("/route")
    public ResponseEntity<ApiResponse<RouteResponse>> computeRoute(@Valid @RequestBody ComputeRouteRequest request) {
        RouteResponse response = routingService.computeRoute(request);
        return ResponseEntity.ok(
                ApiResponse.<RouteResponse>builder()
                        .success(true)
                        .message("Route computed successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now())
                        .path(PATH)
                        .build()
        );
    }
}