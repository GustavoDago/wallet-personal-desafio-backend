package com.microservices.usuario.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {


    private final JwtAuthConverter jwtAuthConverter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity)throws Exception{
        httpSecurity
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/public/**", "/login","/register").permitAll()
                        .requestMatchers(HttpMethod.GET, "/public/**", "/ping", "/userName/{id}").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/public/**", "/users/{id}").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/public/**").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/public/**", "/users/{id}/send-verification-email", "/users/forgot-password").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/configuration/**", "/swagger-ui/**",
                                "/swagger-resources/**", "/swagger-ui.html", "/webjars/**", "/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/users/{userId}").hasAuthority("SCOPE_profile") // Or another appropriate scope/role
                        .anyRequest().authenticated() // Todos los demás requests requieren autenticación
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthConverter)
                        )
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf().disable(); // Deshabilitar CSRF si no se utiliza

        return httpSecurity.build();
    }


}
