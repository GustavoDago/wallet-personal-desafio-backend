package com.microservices.usuario.service;

import com.microservices.usuario.exception.RestException;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

public class KeycloakTokenProvider implements TokenProvider{
    @Value("${app.keycloak.realm}") private String realm;
    @Value("${app.keycloak.admin.clientId}") private String clientId;
    @Value("${app.keycloak.admin.clientSecret}") private String clientSecret;

    @Autowired
    private RestTemplate restTemplate;
    @Override
    public AccessTokenResponse getToken(String username, String password) {
        String tokenUrl = "http://localhost:9081/realms/" + realm + "/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("grant_type", "password");
        body.add("username", username);
        body.add("password", password);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<AccessTokenResponse> response = restTemplate.exchange(
                tokenUrl,
                HttpMethod.POST,
                entity,
                AccessTokenResponse.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new RestException(HttpStatus.BAD_REQUEST, "Error al obtener el token: " + response.getStatusCode());
        }
    }
}
