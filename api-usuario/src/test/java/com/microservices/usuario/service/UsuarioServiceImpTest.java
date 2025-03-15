package com.microservices.usuario.service;

import com.microservices.usuario.entity.Usuario;
import com.microservices.usuario.exception.NotApprovedTransaction;
import com.microservices.usuario.exception.ResourceBadRequestException;
import com.microservices.usuario.exception.RestException;
import com.microservices.usuario.records.NewUserRecord;
import com.microservices.usuario.records.UserWithToken;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class) // Usa Mockito con JUnit 5
class UsuarioServiceImpTest {

    @Mock
    private Keycloak keycloak;

    @Mock // Mockeamos TokenProvider
    private TokenProvider tokenProvider;

    @InjectMocks //Ahora sí usamos InjectMocks.
    private UsuarioServiceImp usuarioService;

    private NewUserRecord validUserRecord;
    private UserRepresentation userRepresentation;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        // Inyecta valores a los campos privados con @Value (como si fuera Spring)
        ReflectionTestUtils.setField(usuarioService, "realm", "test-realm");
        ReflectionTestUtils.setField(usuarioService, "clientId", "test-client");
        ReflectionTestUtils.setField(usuarioService, "clientSecret", "test-secret");


        validUserRecord = new NewUserRecord("test@example.com", "password", "Test", "User", "1234567890", "12345678");

        userRepresentation = new UserRepresentation();
        userRepresentation.setId("test-user-id");
        userRepresentation.setUsername(validUserRecord.email());
        userRepresentation.setEmail(validUserRecord.email());
        userRepresentation.setFirstName(validUserRecord.firstName());
        userRepresentation.setLastName(validUserRecord.lastName());
        userRepresentation.singleAttribute("phone", validUserRecord.phone());
        userRepresentation.singleAttribute("dni", validUserRecord.dni());
        userRepresentation.setEnabled(true);

        usuario = new Usuario();
        usuario.setId("user-id");
        usuario.setEmail(validUserRecord.email());
        usuario.setFirstName(validUserRecord.firstName());
        usuario.setLastName(validUserRecord.lastName());
        usuario.setPhone(validUserRecord.phone());
        usuario.setDni(validUserRecord.dni());
    }

    @Test
    void testCrearUsuario_Success() throws Exception {
        // Configurar el mock de Keycloak
        UsersResource usersResource = mock(UsersResource.class);
        RealmResource realmResource = mock(RealmResource.class);
        when(keycloak.realm("test-realm")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);

        Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(HttpStatus.CREATED.value());
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);

        //Simulo que keycloak me retorna el usuario.
        when(usersResource.searchByUsername(validUserRecord.email(), true)).thenReturn(List.of(userRepresentation));

        // ---  Mockear el metodo getToken DENTRO de UsuarioServiceImp ---
        // 1. Crear un *spy* de UsuarioServiceImp.  Un spy es un mock *parcial*. Ya no lo instanciamos aquí, sino con el @Spy.
        // UsuarioServiceImp usuarioServiceSpy = Mockito.spy(usuarioService);

        // Configurar el mock de TokenProvider
        AccessTokenResponse accessTokenResponse = new AccessTokenResponse();
        accessTokenResponse.setToken("fake-token");
        when(tokenProvider.getToken(anyString(), anyString())).thenReturn(accessTokenResponse);


        // Llamar al metodo del servicio *usando el SPY*
        UserWithToken result = usuarioService.crearUsuario(validUserRecord); // Usamos el SPY

        // Verificar los resultados
        assertNotNull(result);
        assertEquals(validUserRecord.email(), result.user().getEmail());
        assertEquals("fake-token", result.accessToken());
    }



    @Test
    void testCrearUsuario_IncompleteData() { //Test para datos incompletos.
        NewUserRecord incompleteUser = new NewUserRecord("", "", "", "", "", ""); // Usuario incompleto.
        //Se espera un ResourceBadRequestException.
        assertThrows(ResourceBadRequestException.class, () -> {
            usuarioService.crearUsuario(incompleteUser); //Llamo directamente al método, no necesito configurar mocks en este caso.
        });
    }


    @Test
    void testCrearUsuario_KeycloakError() throws Exception {
        // Configurar mocks de Keycloak
        UsersResource usersResource = mock(UsersResource.class);
        RealmResource realmResource = mock(RealmResource.class);
        when(keycloak.realm("test-realm")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);

        // Simular un error de Keycloak (por ejemplo, un 409 Conflict)
        Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(HttpStatus.CONFLICT.value()); // Simulo status 409
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);


        // Verificar que se lanza la excepción correcta
        assertThrows(NotApprovedTransaction.class, () -> {
            usuarioService.crearUsuario(validUserRecord);
        });
    }



    @Test
    void testObtenerUsuarioPorId() {

        UsersResource usersResource = mock(UsersResource.class);
        RealmResource realmResource = mock(RealmResource.class);
        UserResource userResource = mock(UserResource.class);

        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get(anyString())).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(userRepresentation);

        Usuario result = usuarioService.obtenerUsuarioPorId("test-user-id");
        // assertEquals(usuario, result); // NO compares objetos completos
        assertEquals("test-user-id", result.getId());          // Compara campo por campo
        assertEquals(validUserRecord.email(), result.getEmail());
        assertEquals(validUserRecord.firstName(), result.getFirstName());
        assertEquals(validUserRecord.lastName(), result.getLastName());
        assertEquals(validUserRecord.phone(), result.getPhone());
        assertEquals(validUserRecord.dni(), result.getDni());

    }


    @Test
    void testObtenerUsuarioPorId_NotFound() {
        // Configurar el mock para que devuelva un usuario null (simulando que no se encuentra)
        UsersResource usersResource = mock(UsersResource.class);
        RealmResource realmResource = mock(RealmResource.class);
        UserResource userResource = mock(UserResource.class); //No se encuentra el usuario.

        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get(anyString())).thenReturn(userResource);
        when(userResource.toRepresentation()).thenThrow(jakarta.ws.rs.NotFoundException.class); //Simulo una excepcion.

        // Llamar al método y verificar que devuelva null
        assertThrows(jakarta.ws.rs.NotFoundException.class, ()-> {
            usuarioService.obtenerUsuarioPorId("non-existent-id");
        });
    }



    @Test
    void testObtenerUserName_success() {
        UsersResource usersResource = mock(UsersResource.class);
        RealmResource realmResource = mock(RealmResource.class);
        UserResource userResource = mock(UserResource.class);

        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get(anyString())).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(userRepresentation);

        String userName = usuarioService.obtenerUserName("test-user-id");
        assertEquals(validUserRecord.email(), userName); //Comparo el nombre de usuario retornado, con el del record.

    }

    @Test
    void testUpdateUser_success() throws Exception {

        //Datos de prueba
        String userId = "existing-user-id";
        Map<String, Object> updateData = Map.of(
                "firstName", "NewFirstName",
                "lastName", "NewLastName",
                "phone", "9999999999"
        );

        //Mocks
        UsersResource usersResource = mock(UsersResource.class);
        RealmResource realmResource = mock(RealmResource.class);
        UserResource userResource = mock(UserResource.class);
        // Usamos un *spy* de UserRepresentation, en lugar de un mock completo
        UserRepresentation existingUser = Mockito.spy(new UserRepresentation());

        //Comportamiento.
        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get(userId)).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(existingUser);
        doNothing().when(userResource).update(any(UserRepresentation.class));


        // Mockear getAttributes() para que devuelva un mapa mutable
        Map<String, List<String>> initialAttributes = new HashMap<>();
        initialAttributes.put("phone", List.of("11111111")); // Valor inicial
        initialAttributes.put("dni", List.of("12345678"));   // Valor inicial
        when(existingUser.getAttributes()).thenReturn(initialAttributes);
        existingUser.setEmail("test@example.com");


        //Llamar al metodo.
        ResponseEntity<Map<String, Object>> response = usuarioService.updateUser(userId, updateData, "fake-token");

        //Verificaciones
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());


        Map<String, Object> responseBody = response.getBody();
        //Ahora sí podemos verificar contra los getters.
        assertEquals("NewFirstName", existingUser.getFirstName()); //Verifico usando el getter.
        assertEquals("NewLastName", existingUser.getLastName());
        assertEquals("test@example.com", existingUser.getEmail());
        //Verificamos en el mapa, y en los atributos.
        Map<String, List<String>> updatedAttributes = existingUser.getAttributes();
        assertNotNull(updatedAttributes);
        assertEquals(List.of("9999999999"), updatedAttributes.get("phone"));

        verify(userResource).update(any(UserRepresentation.class));
    }





    @Test
    void testUpdateUser_invalidData() {

        String userId = "existing-user-id";
        Map<String, Object> updateData = Map.of("firstName", 123); // Tipo incorrecto (debería ser String)

        // Llamar al método y verificar la respuesta
        ResponseEntity<Map<String, Object>> response = usuarioService.updateUser(userId, updateData, "fake-token");

        // Verificar el código de estado y el mensaje de error
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("error"));
        // Ajusta este valor según el mensaje de error real que esperas
        assertEquals("El campo 'firstName' debe ser una cadena de texto.", response.getBody().get("error"));
    }

    @Test
    void testEliminarUsuario_Success() {
        // Configurar mocks
        UsersResource usersResource = mock(UsersResource.class);
        RealmResource realmResource = mock(RealmResource.class);

        // Configurar comportamiento
        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);


        // Llamar al método
        usuarioService.eliminarUsuario("user-id");

        // Verificar que se llamó al método delete() de UsersResource
        verify(usersResource).delete("user-id"); //Verificamos que se llamó con el id correcto.
    }

    @Test
    void testLogin_success() throws Exception{
        String email = "test@example.com";
        String password = "password";

        AccessTokenResponse accessTokenResponse = new AccessTokenResponse();
        accessTokenResponse.setToken("fake-token");

        when(tokenProvider.getToken(anyString(), anyString())).thenReturn(accessTokenResponse);

        String result = usuarioService.login(email, password);
        assertEquals("fake-token", result);
        verify(tokenProvider).getToken(eq(email), eq(password)); //Verificamos que se llamó al método, con los argumentos correctos.
    }


    @Test
    void testLogin_InvalidCredentials() throws Exception {
        String email = "test@example.com";
        String password = "wrong_password";

        // Mockear la llamada a tokenProvider.getToken para que lance la excepción
        when(tokenProvider.getToken(eq(email), eq(password)))
                .thenThrow(new RestException(HttpStatus.NOT_FOUND, "Credenciales inválidas"));

        //Usamos assertThrows para verificar que se lanza la excepción correcta.
        assertThrows(RestException.class, () -> {
            usuarioService.login(email, password);
        });
    }
}