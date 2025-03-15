package com.layp.GateWayService.config;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiGatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(r -> r.path("/layp/users/**") // Match incoming path
                        .filters(f -> f.rewritePath("/layp/users/(?<segment>.*)", "/${segment}"))
                        .uri("lb://USER-SERVICE"))
                .route(r -> r.path("/layp/hotels/**") // Match incoming path
                        .filters(f -> f.rewritePath("/layp/hotels/(?<segment>.*)", "/${segment}"))
                        .uri("lb://HOTEL-SERVICE"))
                .route(r -> r.path("/layp/ratings/**") // Match incoming path
                        .filters(f -> f.rewritePath("/layp/ratings/(?<segment>.*)", "/${segment}"))
                        .uri("lb://RATING-SERVICE"))
                .build();
    }
}

