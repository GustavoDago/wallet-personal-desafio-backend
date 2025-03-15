package com.microservices.usuario.service;

import org.keycloak.representations.AccessTokenResponse;

public interface TokenProvider {
    AccessTokenResponse getToken(String username, String password);

}
