package com.microservices.usuario.service;

import com.microservices.usuario.entity.Usuario;
import com.microservices.usuario.exception.NotApprovedTransaction;
import com.microservices.usuario.exception.ResourceBadRequestException;
import com.microservices.usuario.records.NewUserRecord;
import com.microservices.usuario.records.UserWithToken;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface UsuarioService {

    UserWithToken crearUsuario(NewUserRecord usuario) throws ResourceBadRequestException, NotApprovedTransaction;
    UserResource getUserResource(String userId);
    void eliminarUsuario(String userId);
    ResponseEntity<Map<String, Object>> updateUser(String id, Map<String, Object> data, String token);
    String login(String email, String password) throws ResourceBadRequestException;

    Usuario obtenerUsuarioPorId(String id);

    String obtenerUserName(String id);
    UserResource getUser(String userId);
}
