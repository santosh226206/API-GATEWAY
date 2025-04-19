package com.layp.GateWayService.filter;

import com.layp.GateWayService.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Gateway filter for JWT-based authentication
 * Implements Spring Cloud Gateway's filter mechanism to secure API endpoints
 * Validates JWT tokens and enriches requests with user information
 */
@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Constructor initializing the filter with configuration class
     * Required by Spring Cloud Gateway's filter factory mechanism
     */
    public AuthenticationFilter() {
        super(Config.class);
    }

    /**
     * Main filter method that processes each request
     * Business logic:
     * 1. Checks if the request needs authentication
     * 2. Validates JWT token from Authorization header
     * 3. Extracts user details and adds them to request headers
     * 4. Allows or denies access based on token validity
     *
     * Technical implementation:
     * - Uses reactive programming (Project Reactor)
     * - Modifies request headers using ServerWebExchange
     * - Handles JWT validation through JwtUtil
     *
     * @param config Filter configuration
     * @return GatewayFilter instance
     */
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            if (isSecured(exchange)) {
                if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }

                String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
                String userNameHeader=exchange.getRequest().getHeaders().get("x-userName").get(0);
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    authHeader = authHeader.substring(7);
                }
                try {
                    jwtUtil.validateToken(authHeader);
                    
                    // Add user details to headers for downstream services
                    String username = jwtUtil.extractUsername(authHeader);
                    String role = jwtUtil.extractRole(authHeader);
                    if(!userNameHeader.equals(username)){
                        throw new Exception("Invalid user Token");
                    }
                    exchange.getRequest().mutate()
                        .header("X-Auth-User", username)
                        .header("X-Auth-Role", role)
                        .build();
                        
                } catch (Exception e) {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }
            }
            return chain.filter(exchange);
        };
    }

    /**
     * Determines if a request needs authentication
     * Business rule: Skip authentication for login endpoint
     * All other endpoints require authentication
     *
     * @param exchange ServerWebExchange containing request details
     * @return boolean indicating if request needs authentication
     */
    private boolean isSecured(ServerWebExchange exchange) {
        String path = exchange.getRequest().getURI().getPath();
        return !path.contains("/auth/login"); // Skip authentication for login endpoint
    }

    /**
     * Configuration class for the filter
     * Currently empty but can be extended for future configuration options
     * Required by Spring Cloud Gateway's filter factory pattern
     */
    public static class Config {
        // Configuration properties if needed
    }
} 