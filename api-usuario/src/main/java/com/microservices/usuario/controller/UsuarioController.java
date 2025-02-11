package com.microservices.usuario.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.usuario.entity.Usuario;
import com.microservices.usuario.exception.NotApprovedTransaction;
import com.microservices.usuario.exception.ResourceBadRequestException;
import com.microservices.usuario.records.NewUserRecord;
import com.microservices.usuario.records.UserCredentials;
import com.microservices.usuario.records.UserWithToken;
import com.microservices.usuario.service.UsuarioService;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
@CrossOrigin
public class UsuarioController {
    @Autowired
    private final UsuarioService usuarioService;
    @Autowired
    private ObjectMapper objectMapper;

    @Value("${app.keycloak.realm}")
    private String realm;

    @Value("${app.keycloak.admin.clientId}")
    private String clientId;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }


    @PostMapping("/register")
    public ResponseEntity<?> createUser(@RequestBody NewUserRecord newUserRecord) {


        try {
            UserWithToken userWithToken = usuarioService.crearUsuario(newUserRecord);
            return ResponseEntity.ok(userWithToken);
        } catch (ResourceBadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("error", e.getMessage()));
        } catch (NotApprovedTransaction e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", e.getMessage()));
        }

    }

    @GetMapping("/users/{id}")
    public ResponseEntity<Usuario> obtenerUsuarioPorId(@PathVariable String id, @RequestHeader("Authorization") String  accessToken) {
        Usuario usuario = usuarioService.obtenerUsuarioPorId(id);
        if (usuario != null){
            return new ResponseEntity<>(usuario,HttpStatus.OK);
        }else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/userName/{userId}")
    public ResponseEntity<String > obtenerUserName(@PathVariable String userId, @RequestHeader("Authorization") String  accessToken){
        return ResponseEntity.ok(usuarioService.obtenerUserName(userId));
   }

    // Metodo para recibir la solicitud PATCH y delegar al servicio
    @PatchMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable("id") String id,
            @RequestBody Map<String, Object> data,
            @RequestHeader("Authorization") String token) {

        // Llamamos al servicio para actualizar el usuario
        ResponseEntity<Map<String, Object>> updatedUser = usuarioService.updateUser(id, data, token);

        // Retornamos la respuesta con el usuario actualizado
        return updatedUser;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable String  id) {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody UserCredentials credentials) {
        try {
            String token = usuarioService.login(credentials.email(), credentials.password());

            Map<String, String> response = new HashMap<>();
            response.put("accessToken", token);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", e.getMessage()));
        }
    }


}
