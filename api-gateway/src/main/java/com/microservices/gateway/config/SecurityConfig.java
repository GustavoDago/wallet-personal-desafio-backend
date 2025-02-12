package com.microservices.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsConfigurationSource;


import java.util.List;

@EnableWebFluxSecurity
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF (usually safe for APIs with JWT)
                .authorizeExchange(exchanges -> exchanges
                        // Permit access to /api-usuario/register without authentication
                        .pathMatchers(HttpMethod.POST, "/api-usuario/register").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api-usuario/login").permitAll()
                        // Permit other public endpoints if needed
                        .pathMatchers("/public/**").permitAll()
                        // Require authentication for all other requests
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> {}) // Configure JWT resource server (defaults are usually fine)
                );
        return http.build();
    }


}
