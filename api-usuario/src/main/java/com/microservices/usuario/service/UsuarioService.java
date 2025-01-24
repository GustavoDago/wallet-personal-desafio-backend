package com.microservices.usuario.service;

import com.microservices.usuario.entity.Usuario;
import com.microservices.usuario.exception.NotApprovedTransaction;
import com.microservices.usuario.exception.ResourceBadRequestException;
import com.microservices.usuario.records.NewUserRecord;
import com.microservices.usuario.records.UserWithToken;
import org.keycloak.admin.client.resource.UserResource;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UsuarioService {

    UserWithToken crearUsuario(NewUserRecord usuario) throws ResourceBadRequestException, NotApprovedTransaction;
    UserResource getUser(String userId);
    void eliminarUsuario(String userId);
    ResponseEntity<Map<String, Object>> updateUser(String id, Map<String, Object> data, String token);
    String login(String email, String password) throws ResourceBadRequestException;


    Usuario obtenerUsuarioPorId(String id);
}
