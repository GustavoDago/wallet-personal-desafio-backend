package Cards.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;

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
                        .requestMatchers(HttpMethod.POST, "/users/{userId}/cards").permitAll()
                        .requestMatchers(HttpMethod.GET, "/users/{userId}/cards/{cardId}", "/users/{userId}/cards").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/users/{userId}/cards/{cardId}").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/public/**", "/users/{id}/send-verification-email", "/users/forgot-password").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/configuration/**", "/swagger-ui/**",
                                "/swagger-resources/**", "/swagger-ui.html", "/webjars/**", "/api-docs/**").permitAll()
                        .anyRequest().authenticated() // Todos los demás requests requieren autenticación
                )
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(Arrays.asList("http://localhost:3500")); // Replace with your frontend URL
                    config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                    config.setAllowedHeaders(Arrays.asList("*")); // You might want to be more specific here in production
                    return config;
                }))
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
