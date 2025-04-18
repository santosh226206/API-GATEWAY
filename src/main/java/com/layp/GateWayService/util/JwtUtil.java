package com.layp.GateWayService.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Utility class for handling JSON Web Token (JWT) operations
 * This class provides methods for generating, validating, and extracting information from JWTs
 * Used for securing API endpoints and maintaining user session information
 */
@Component
public class JwtUtil {

    /**
     * Secret key used for signing JWT tokens
     * Loaded from application properties with a default value
     * Must be at least 256 bits long for HS256 algorithm
     */
    @Value("${jwt.secret:mysecretkey12345mysecretkey12345mysecretkey12345}")
    private String secret;

    /**
     * Token expiration time in milliseconds
     * Default is 1 hour (3600000 milliseconds)
     */
    @Value("${jwt.expiration:3600000}")
    private long expirationTime;

    /**
     * Creates a SecretKey instance from the configured secret string
     * Uses HMAC-SHA algorithm for key generation
     * @return SecretKey instance used for JWT signing
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Extracts username from JWT token
     * Business use: Identify the user making the request
     * @param token JWT token string
     * @return username stored in the token's subject claim
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts token expiration date
     * Used for token validation and expiration checking
     * @param token JWT token string
     * @return Date when the token expires
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extracts user role from JWT token
     * Business use: Role-based access control (RBAC)
     * @param token JWT token string
     * @return user's role stored in the token claims
     */
    public String extractRole(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("role", String.class);
    }

    /**
     * Generic claim extraction method
     * Technical utility for extracting any claim from token using a resolver function
     * @param token JWT token string
     * @param claimsResolver function to extract specific claim
     * @return extracted claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extracts all claims from token
     * Technical implementation using JJWT library
     * @param token JWT token string
     * @return Claims object containing all token claims
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Checks if token is expired
     * Business requirement: Ensure security by invalidating old tokens
     * @param token JWT token string
     * @return true if token is expired, false otherwise
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Generates JWT token for authenticated user
     * Business use: Create session token after successful login
     * @param username user's identifier
     * @param role user's role for RBAC
     * @return JWT token string
     */
    public String generateToken(String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        return createToken(claims, username);
    }

    /**
     * Creates JWT token with specified claims
     * Technical implementation using JJWT builder
     * @param claims custom claims to include in token
     * @param subject user identifier (username)
     * @return JWT token string
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validates JWT token
     * Business requirement: Ensure request authenticity
     * Checks both token signature and expiration
     * @param token JWT token string
     * @return true if token is valid, false otherwise
     */
    public Boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
} 