package com.microservices.usuario.service;

import com.microservices.usuario.entity.Usuario;
import com.microservices.usuario.exception.NotApprovedTransaction;
import com.microservices.usuario.exception.ResourceBadRequestException;
import com.microservices.usuario.exception.RestException;
import com.microservices.usuario.records.NewUserRecord;
import com.microservices.usuario.records.UserWithToken;
import jakarta.ws.rs.core.Response;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class UsuarioServiceImp implements UsuarioService{


    @Value("${app.keycloak.realm}")
    private String realm;

    @Value("${app.keycloak.admin.clientId}")
    private String clientId;

    @Value(("${app.keycloak.admin.clientSecret}"))
    private String clientSecret;

    @Autowired
    private final Keycloak keycloak;

    public UsuarioServiceImp(Keycloak keycloak) {
        this.keycloak = keycloak;
    }



    @Override
    public UserWithToken crearUsuario(NewUserRecord newUserRecord) throws ResourceBadRequestException, NotApprovedTransaction {
        if (validar(newUserRecord)) throw new ResourceBadRequestException("datos incompletos");

        UserRepresentation userRepresentation = getUserRepresentation(newUserRecord);

        UsersResource usersResource = getUsersResource();

        Response response = usersResource.create(userRepresentation);

        log.info("Status Code: {}", response.getStatus());

        if (response.getStatus() != HttpStatus.CREATED.value()) {
            throw new NotApprovedTransaction("Error al crear el usuario en Keycloak");
        }

        log.info("Nuevo usuario creado");

        UserRepresentation userCreated = getUserRepresentation(newUserRecord, usersResource);

        Usuario user = convertToUser(userCreated);
        // Obtener el accessToken para el usuario recién creado
        String accessToken = getToken(newUserRecord.email(),newUserRecord.password()).getToken();

        return new UserWithToken(user, accessToken);
    }

    @Override
    public UserResource getUserResource(String userId) {
        UsersResource usersResource = getUsersResource();
        return usersResource.get(userId);
    }

    private AccessTokenResponse getToken(String email, String password) {
        RestTemplate restTemplate = new RestTemplate();
        String tokenUrl = "http://localhost:9081/realms/" + realm + "/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);  // Necesario si el cliente está configurado con secreto
        body.add("grant_type", "password");
        body.add("username", email);
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
    public Usuario convertToUser(UserRepresentation userRepresentation) {
        Usuario user = new Usuario();
        user.setId(userRepresentation.getId());
        user.setEmail(userRepresentation.getEmail());
        user.setFirstName(userRepresentation.getFirstName());
        user.setLastName(userRepresentation.getLastName());
        user.setPhone(userRepresentation.getAttributes() != null
                ? userRepresentation.getAttributes().get("phone").get(0) : null);
        user.setDni(userRepresentation.getAttributes() != null
                ? userRepresentation.getAttributes().get("dni").get(0) : null);
        return user;
    }

    private UsersResource getUsersResource(){
        return keycloak.realm(realm).users();
    }

    private UserRepresentation getUserRepresentation(NewUserRecord newUserRecord, UsersResource usersResource) {
        List<UserRepresentation> userRepresentations = usersResource.searchByUsername(newUserRecord.email(), true);
        if (userRepresentations.isEmpty()) {
            throw new RestException(HttpStatus.NOT_FOUND, "No se encontró el usuario creado");
        }
        return  userRepresentations.get(0);
    }

    private static UserRepresentation getUserRepresentation(NewUserRecord newUserRecord) {
        UserRepresentation user= new UserRepresentation();
        user.setEnabled(true);
        user.setFirstName(newUserRecord.firstName());
        user.setLastName(newUserRecord.lastName());
        user.setUsername(newUserRecord.email());
        user.setEmail(newUserRecord.email());
        user.setEmailVerified(true);
        user.singleAttribute("phone", newUserRecord.phone());
        user.singleAttribute("dni", newUserRecord.dni());
        CredentialRepresentation credentialRepresentation=new CredentialRepresentation();
        credentialRepresentation.setValue(newUserRecord.password());
        credentialRepresentation.setType(CredentialRepresentation.PASSWORD);

        user.setCredentials(List.of(credentialRepresentation));
        return user;
    }
    private boolean validar(NewUserRecord nur) {
        String email = nur.email();
        int indexAt = email.indexOf('@');

        // Verifica si hay caracteres antes del @
        boolean hasValidEmail = indexAt > 0; // Debe haber al menos un carácter antes del @

        return (email.length() < 5 || nur.password().isBlank()
                || nur.firstName().isBlank() || nur.lastName().isBlank() || !hasValidEmail);
    }





    @Override
    public void eliminarUsuario(String userId) {
        UsersResource usersResource = getUsersResource();
        usersResource.delete(userId);
    }

    @Override
    public ResponseEntity<Map<String, Object>> updateUser(String id, Map<String, Object> data, String token) {
        log.info("Iniciando la actualización del usuario con ID: {}", id);
        try {
            validateUpdateData(data);
            UserRepresentation user = fetchUser(id);
            updateUserRepresentation(user, data);
            performKeycloakUpdate(id, user);
            log.debug("Construyendo respuesta exitosa para la actualización del usuario con ID: {}", id);
            return buildSuccessResponse(id, user);

        } catch (ResourceBadRequestException e) {
            log.warn("Error de validación al actualizar el usuario: {}", e.getMessage());
            return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());

        } catch (RestException e) {
            log.error("Error al interactuar con Keycloak: {}", e.getMessage());
            return buildErrorResponse(e.getStatus(), e.getMessage());

        } catch (Exception e) {
            log.error("Error inesperado al actualizar el usuario con ID: {}: {}", id, e.getMessage(), e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error inesperado al actualizar el usuario");
        } finally {
            log.info("Finalizando el proceso de actualización del usuario con ID: {}", id);
        }
    }

    private void validateUpdateData(Map<String, Object> data) throws ResourceBadRequestException {
        log.debug("Validando datos de actualización: {}", data);
        if (data == null || data.isEmpty()) {
            throw new ResourceBadRequestException("Los datos de actualización no pueden estar vacíos.");
        }
        // Validaciones específicas de tipo y contenido
        data.forEach((key, value) -> {
            if (value != null) {
                if (List.of("email", "firstName", "lastName", "phone", "dni").contains(key) && !(value instanceof String)) {
                    try {
                        throw new ResourceBadRequestException(String.format("El campo '%s' debe ser una cadena de texto.", key));
                    } catch (ResourceBadRequestException e) {
                        throw new RuntimeException(e);
                    }
                }
                // Puedes agregar validaciones de formato aquí si es necesario (ej. regex para email, teléfono)
            }
        });
    }

    private UserRepresentation fetchUser(String id) throws RestException {
        log.debug("Obteniendo la representación del usuario de Keycloak con ID: {}", id);
        try {
            return keycloak.realm(realm).users().get(id).toRepresentation();
        } catch (jakarta.ws.rs.NotFoundException e) {
            log.warn("Usuario no encontrado en Keycloak con ID: {}", id);
            throw new RestException(HttpStatus.NOT_FOUND, "Usuario no encontrado.");
        } catch (Exception e) {
            log.error("Error al obtener la representación del usuario con ID: {}: {}", id, e.getMessage());
            throw new RestException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al obtener información del usuario en Keycloak.");
        }
    }

    private void updateUserRepresentation(UserRepresentation user, Map<String, Object> data) {
        log.debug("Actualizando la representación del usuario con los datos: {}", data);
        if (data.containsKey("email")) {
            user.setEmail((String) data.get("email"));
        }
        if (data.containsKey("firstName")) {
            user.setFirstName((String) data.get("firstName"));
        }
        if (data.containsKey("lastName")) {
            user.setLastName((String) data.get("lastName"));
        }

        // Actualizar atributos personalizados de forma más clara y segura
        Map<String, List<String>> attributes = user.getAttributes() == null ? new HashMap() : user.getAttributes();
        updateAttribute(attributes, "phone", data.get("phone"));
        updateAttribute(attributes, "dni", data.get("dni"));
        user.setAttributes(attributes);

        log.debug("Representación del usuario actualizada: {}", user);
    }

    private void updateAttribute(Map<String, List<String>> attributes, String key, Object value) {
        if (value != null) {
            attributes.put(key, List.of(value.toString()));
        }
    }

    private void performKeycloakUpdate(String id, UserRepresentation user) throws RestException {
        log.debug("Realizando la actualización del usuario en Keycloak con ID: {}", id);
        try {
            keycloak.realm(realm).users().get(id).update(user);
            log.info("Usuario con ID: {} actualizado exitosamente en Keycloak.", id);
        } catch (jakarta.ws.rs.NotFoundException e) {
            log.warn("Intento de actualizar usuario no encontrado en Keycloak con ID: {}", id);
            throw new RestException(HttpStatus.NOT_FOUND, "Usuario no encontrado en Keycloak.");
        } catch (Exception e) {
            log.error("Error al actualizar el usuario con ID: {} en Keycloak: {}", id, e.getMessage());
            throw new RestException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al actualizar el usuario en Keycloak.");
        }
    }

    private ResponseEntity<Map<String, Object>> buildSuccessResponse(String id, UserRepresentation user) {

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("id", id);
        responseBody.put("email", user.getEmail());
        responseBody.put("firstName", user.getFirstName());
        responseBody.put("lastName", user.getLastName());
        responseBody.put("attributes", user.getAttributes());
        return ResponseEntity.ok(responseBody);
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String message) {
        log.debug("Construyendo respuesta de error con status: {} y mensaje: {}", status, message);
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        return ResponseEntity.status(status).body(errorResponse);
    }

    @Override
    public String login(String email, String password) throws ResourceBadRequestException {
        // Validar parámetros
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            throw new ResourceBadRequestException("El nombre de usuario y la contraseña son obligatorios");
        }

        try {
            // Obtener el token de acceso
            AccessTokenResponse tokenResponse = getToken(email, password);

            // Si se obtiene un token, la autenticación es exitosa
            if (tokenResponse != null && tokenResponse.getToken() != null) {
                return tokenResponse.getToken();
            } else {
                throw new RestException(HttpStatus.NOT_FOUND, "Credenciales inválidas");
            }
        } catch (HttpClientErrorException e) {
            throw new RestException(HttpStatus.BAD_REQUEST, "Error de autenticación: " + e.getMessage());
        } catch (Exception e) {
            throw new RestException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al autenticar al usuario");
        }

    }

    @Override
    public Usuario obtenerUsuarioPorId(String id) {
        // 1. Get UserRepresentation from Keycloak
        UserResource userResource = getUserResource(id);
        UserRepresentation userRepresentation = userResource.toRepresentation();

        // 2. Create a Map with the required data
        Usuario userData = new Usuario();
        userData.setId(userRepresentation.getId());
        userData.setEmail(userRepresentation.getEmail());
        userData.setFirstName(userRepresentation.getFirstName());
        userData.setLastName(userRepresentation.getLastName());
        if (userRepresentation.getAttributes() != null) {
            List<String> phone = userRepresentation.getAttributes().get("phone");
            if (phone != null && !phone.isEmpty()) {
                userData.setPhone(phone.get(0));
            }
            List<String> dni = userRepresentation.getAttributes().get("dni");
            if (dni != null && !dni.isEmpty()) {
                userData.setDni(dni.get(0));
            }
        }

        return userData;
    }



    @Override
    public String obtenerUserName(String id) {
            String respuesta = "";
            UserRepresentation userRepresentation = getUser(id).toRepresentation();
            if (userRepresentation.isEnabled()){
                respuesta = userRepresentation.getUsername();
            }
            return respuesta;
    }

    @Override
    public UserResource getUser(String userId) {
        UsersResource usersResource = getUsersResource();
        return usersResource.get(userId);
    }
}
