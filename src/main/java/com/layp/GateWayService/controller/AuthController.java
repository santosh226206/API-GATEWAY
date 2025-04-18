package com.layp.GateWayService.controller;

import com.layp.GateWayService.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.HashMap;

/**
 * Authentication Controller
 * Handles user authentication and token generation
 * Acts as a gateway authentication endpoint for the microservices architecture
 */
@RestController
@RequestMapping("/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private WebClient.Builder webClientBuilder;

    /**
     * Handles user login requests
     * Business flow:
     * 1. Receives username/password credentials
     * 2. Validates credentials with USER-SERVICE
     * 3. Generates JWT token for valid users
     * 4. Returns token for successful authentication
     *
     * Technical implementation:
     * - Uses WebClient for reactive service-to-service communication
     * - Implements reactive programming with Project Reactor
     * - Handles errors with proper HTTP status codes
     * - Includes logging for monitoring and debugging
     *
     * @param request AuthRequest containing username and password
     * @return Mono<ResponseEntity> with JWT token or error response
     */
    @PostMapping("/login")
    public Mono<ResponseEntity<Map<String, String>>> login(@RequestBody AuthRequest request) {
        logger.info("Login attempt for user: {}", request.getUsername());
        
        return webClientBuilder.build()
                .post()
                .uri("lb://USER-SERVICE/users/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Map.class)
                .map(userDetails -> {
                    logger.info("User validated successfully: {}", request.getUsername());
                    String token = jwtUtil.generateToken(
                            request.getUsername(),
                            (String) userDetails.get("role")
                    );
                    Map<String, String> response = new HashMap<>();
                    response.put("token", token);
                    return ResponseEntity.ok(response);
                })
                .onErrorResume(WebClientResponseException.class, e -> {
                    logger.error("User service returned error: {} - {}", e.getStatusCode(), e.getMessage());
                    return Mono.just(ResponseEntity.status(e.getStatusCode())
                            .body(new HashMap<>()));
                })
                .onErrorResume(Exception.class, e -> {
                    logger.error("Login failed for user: {}. Error: {}", request.getUsername(), e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new HashMap<>()));
                });
    }
}

/**
 * Data Transfer Object for authentication requests
 * Contains user credentials for login
 */
class AuthRequest {
    private String username;
    private String password;

    // Getters and setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

