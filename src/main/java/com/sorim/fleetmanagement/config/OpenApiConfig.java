package com.sorim.fleetmanagement.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

    @Bean
    public OpenAPI fleetManagementOpenAPI() {
        Server localServer = new Server();
        localServer.setUrl("http://localhost:8080");
        localServer.setDescription("Local development server");

        Server productionServer = new Server();
        productionServer.setUrl("https://fleet-app-568582430679.us-central1.run.app/");
        productionServer.setDescription("Production server");

        Contact contact = new Contact();
        contact.setName("Sorim Fleet Management Team");
        contact.setEmail("support@fleetmanagement.com");
        contact.setUrl("https://fleetmanagement.com");

        License license = new License()
                .name("MIT License")
                .url("https://choosealicense.com/licenses/mit/");

        Info info = new Info()
                .title("Fleet Management API")
                .version("1.0.0")
                .description("""
                        Comprehensive Fleet Management System API for managing vehicles, service records, and user authentication.
                        
                        ## Features
                        - **Vehicle Management**: Create, read, update, and delete vehicles with advanced filtering and search capabilities
                        - **Service Records**: Track vehicle maintenance and service appointments
                        - **Authentication**: Secure JWT-based authentication and user management
                        - **Role-Based Access Control**: Admin and user roles with appropriate permissions
                        
                        ## Authentication
                        Most endpoints require authentication using a JWT Bearer token. Include the token in the Authorization header:
                        `Authorization: Bearer <your-jwt-token>`
                        
                        ## Error Responses
                        All endpoints may return standard error responses with the following structure:
                        ```json
                        {
                          "success": false,
                          "message": "Error description",
                          "data": null
                        }
                        ```
                        
                        Common HTTP status codes:
                        - `200 OK`: Request successful
                        - `201 Created`: Resource created successfully
                        - `400 Bad Request`: Invalid request parameters
                        - `401 Unauthorized`: Authentication required or invalid token
                        - `403 Forbidden`: Insufficient permissions
                        - `404 Not Found`: Resource not found
                        - `500 Internal Server Error`: Server error
                        """)
                .contact(contact)
                .license(license);

        Components components = new Components()
                .addSecuritySchemes(SECURITY_SCHEME_NAME,
                        new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("""
                                        JWT Bearer Token Authentication.
                                        
                                        To authenticate:
                                        1. Use the `/api/v1/auth/login` endpoint to obtain a JWT token
                                        2. Include the token in the Authorization header with the format: `Bearer <token>`
                                        
                                        The token expires after 24 hours and must be refreshed by logging in again.
                                        """));

        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList(SECURITY_SCHEME_NAME);

        return new OpenAPI()
                .info(info)
                .servers(List.of(localServer, productionServer))
                .components(components)
                .addSecurityItem(securityRequirement);
    }
}
