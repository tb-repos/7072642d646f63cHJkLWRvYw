package com.tourbhook.api.service;

import com.tourbhook.api.dto.routing.ComputeRouteRequest;
import com.tourbhook.api.dto.routing.RouteResponse;

public interface RoutingService {

    RouteResponse computeRoute(ComputeRouteRequest request);
}