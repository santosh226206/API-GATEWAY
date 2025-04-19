# API Gateway Service

A Spring Cloud Gateway implementation that serves as the central entry point for the microservices architecture. This gateway handles authentication, routing, and load balancing for the connected services.

## Architecture Overview

```
Client -> API Gateway -> Microservices (USER-SERVICE, HOTEL-SERVICE, RATING-SERVICE)
```

## Features

- JWT-based Authentication
- Centralized Routing
- Load Balancing
- Service Discovery (Eureka)
- Role-based Access Control
- Secure Communication
- Comprehensive Error Handling
- Logging and Monitoring

## Technical Stack

- Java 17
- Spring Boot 3.4.3
- Spring Cloud Gateway
- Spring Cloud Netflix Eureka Client
- JWT (JSON Web Tokens)
- Project Reactor (Reactive Programming)

## Service Routes

```
/auth/** -> Handled locally by API Gateway (public)
/layp/users/** -> USER-SERVICE (protected)
/layp/hotels/** -> HOTEL-SERVICE (protected)
/layp/ratings/** -> RATING-SERVICE (protected)
```

## Authentication Flow

1. Client sends login request to API Gateway
2. Gateway forwards to USER-SERVICE for validation
3. On success, Gateway generates JWT token
4. Token returned to client
5. Client uses token for subsequent requests

```mermaid
sequenceDiagram
    Client->>API Gateway: POST /auth/login
    API Gateway->>USER-SERVICE: Validate credentials
    USER-SERVICE-->>API Gateway: Return user details + role
    API Gateway->>API Gateway: Generate JWT token
    API Gateway-->>Client: Return JWT token
```

## Protected API Flow

```mermaid
sequenceDiagram
    Client->>API Gateway: Request with JWT token
    API Gateway->>API Gateway: Validate token
    API Gateway->>Microservice: Forward request + user info
    Microservice-->>API Gateway: Response
    API Gateway-->>Client: Forward response
```

## Components

### 1. JwtUtil
- Handles JWT operations
- Token generation and validation
- User information extraction
- Security implementations

### 2. AuthenticationFilter
- Request interception
- Token validation
- User context management
- Security flow handling

### 3. AuthController
- Login request handling
- USER-SERVICE communication
- Token generation management
- Authentication response handling

### 4. ApiGatewayConfig
- Route configuration
- Load balancer setup
- Filter application
- Service discovery management

## API Usage

### 1. Authentication

```bash
# Login Request
POST http://localhost:8084/auth/login
Content-Type: application/json

{
    "username": "johndoe",
    "password": "password123"
}

# Response
{
    "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### 2. Protected API Access

```bash
# Example Request
GET http://localhost:8084/layp/users/getAllUser
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

## Security Features

- JWT-based authentication
- Token expiration (1 hour default)
- Role-based access control
- Secure inter-service communication

## Error Handling

| Error Type | HTTP Status | Description |
|------------|-------------|-------------|
| Authentication Failure | 401 | Invalid or expired token |
| Invalid Route | 404 | Requested resource not found |
| Service Error | 500 | Internal server error |

## Service Integration

### USER-SERVICE
- User management
- Authentication
- User data operations

### HOTEL-SERVICE
- Hotel management
- Hotel-related operations

### RATING-SERVICE
- Rating management
- Rating-related operations

## Configuration

### application.yml
```yaml
server:
  port: 8084
spring:
  application:
    name: API-GATEWAY
  cloud:
    gateway:
      discovery:
        locator:
          enabled: false
          lower-case-service-id: true

jwt:
  secret: mysecretkey12345mysecretkey12345mysecretkey12345
  expiration: 3600000 # 1 hour
```

## Setup and Deployment

1. Start Eureka Server
2. Start required microservices (USER-SERVICE, HOTEL-SERVICE, RATING-SERVICE)
3. Start API Gateway
4. Verify service registration in Eureka dashboard (http://localhost:8761)

## Best Practices Implemented

1. Centralized Authentication
2. Service Discovery
3. Load Balancing
4. Secure Communication
5. Separation of Concerns
6. Comprehensive Error Handling
7. Logging and Monitoring

## Development Guidelines

1. All new routes should be added in `ApiGatewayConfig`
2. Protected routes must include authentication filter
3. Maintain proper logging for debugging
4. Follow error handling patterns
5. Update documentation for API changes

## Monitoring and Maintenance

- Access actuator endpoints for metrics
- Monitor service health through Eureka
- Check logs for error tracking
- Regular token validation and cleanup

## Contributing

1. Fork the repository
2. Create feature branch
3. Commit changes
4. Push to branch
5. Create Pull Request

## Contact

[linkedIn: https://www.linkedin.com/in/santosh-kumar-9bbb52203/] 
