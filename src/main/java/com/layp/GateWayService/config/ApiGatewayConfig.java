package com.layp.GateWayService.config;

import com.layp.GateWayService.filter.AuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * API Gateway Configuration
 * Configures routing and authentication for the microservices architecture
 * Implements the Gateway pattern to provide a single entry point for all client requests
 */
@Configuration
public class ApiGatewayConfig {

    @Autowired
    private AuthenticationFilter authFilter;

    /**
     * Configures WebClient with load balancing support
     * Used for service-to-service communication
     * Enables client-side load balancing through Eureka
     *
     * @return Load-balanced WebClient.Builder instance
     */
    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    /**
     * Configures API routes and their filters
     * Business logic:
     * 1. Routes requests to appropriate microservices
     * 2. Applies authentication to protected endpoints
     * 3. Handles path rewriting for clean URLs
     *
     * Technical implementation:
     * - Uses Spring Cloud Gateway's RouteLocator
     * - Implements load balancing with "lb://" scheme
     * - Applies JWT authentication filter
     * - Configures path rewriting for each service
     *
     * Route structure:
     * - /auth/** -> USER-SERVICE (public)
     * - /layp/users/** -> USER-SERVICE (protected)
     * - /layp/hotels/** -> HOTEL-SERVICE (protected)
     * - /layp/ratings/** -> RATING-SERVICE (protected)
     *
     * @param builder RouteLocatorBuilder instance
     * @return Configured RouteLocator
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Auth endpoint - no authentication required
                .route(r -> r.path("/auth/**")
                        .uri("lb://USER-SERVICE"))
                // Protected routes with authentication
                .route(r -> r.path("/layp/users/**")
                        .filters(f -> f.filter(authFilter.apply(new AuthenticationFilter.Config()))
                                .rewritePath("/layp/users/(?<segment>.*)", "/${segment}"))
                        .uri("lb://USER-SERVICE"))
                .route(r -> r.path("/layp/hotels/**")
                        .filters(f -> f.filter(authFilter.apply(new AuthenticationFilter.Config()))
                                .rewritePath("/layp/hotels/(?<segment>.*)", "/${segment}"))
                        .uri("lb://HOTEL-SERVICE"))
                .route(r -> r.path("/layp/ratings/**")
                        .filters(f -> f.filter(authFilter.apply(new AuthenticationFilter.Config()))
                                .rewritePath("/layp/ratings/(?<segment>.*)", "/${segment}"))
                        .uri("lb://RATING-SERVICE"))
                .build();
    }
}

