

```mermaid
graph TD
    subgraph Cliente [Cliente]
        A[Usuario Final] --> B(Navegador o App)
    end

    B -- HTTPS, JWT --> C[API Gateway]

    subgraph Microservicios
        C -- Enruta, Valida JWT, Feign --> D[API Usuario]
        C -- Enruta, Valida JWT, Feign --> E[API Cuenta]
        C -- Enruta, Valida JWT, Feign --> F[API Transacciones]
        C -- Enruta, Valida JWT, Feign --> G[Servicio Cards]
        D -- Feign --> E
        F -- Feign --> D
        F -- Feign --> E
        G -- Feign --> D
        H[Servidor Configuracion] -- Configuracion --> D
        H -- Configuracion --> E
        H -- Configuracion --> F
        H -- Configuracion --> G
        H -- Configuracion --> C
        I[Servidor Eureka] -- Registro y Descubrimiento --> D
        I -- Registro y Descubrimiento --> E
        I -- Registro y Descubrimiento --> F
        I -- Registro y Descubrimiento --> G
        I -- Registro y Descubrimiento --> C
    end

    subgraph Bases de Datos
        D -- JPA/Hibernate --> J[Base Datos Keycloak - MySQL]
        E -- JPA/Hibernate --> K[Base Datos Cuenta - MySQL]
        F -- JPA/Hibernate --> L[Base Datos Transacciones - MySQL]
        G -- JPA/Hibernate --> M[Base Datos Cards - MySQL]
    end

      subgraph KeycloakContenedor [Contenedor Keycloak]
        D -- Keycloak Admin API --> N[Keycloak]
        N -- JDBC --> J
      end

    subgraph ConfiguracionExterna
      O[Repositorio GitHub Configuracion] --> H
    end

    style C fill:#f9f,stroke:#333,stroke-width:2px
    style D fill:#ccf,stroke:#333,stroke-width:2px
    style E fill:#ccf,stroke:#333,stroke-width:2px
    style F fill:#ccf,stroke:#333,stroke-width:2px
    style G fill:#ccf,stroke:#333,stroke-width:2px
    style H fill:#cff,stroke:#333,stroke-width:2px
    style I fill:#cff,stroke:#333,stroke-width:2px
    style J fill:#ffc,stroke:#333,stroke-width:2px
    style K fill:#ffc,stroke:#333,stroke-width:2px
    style L fill:#ffc,stroke:#333,stroke-width:2px
    style M fill:#ffc,stroke:#333,stroke-width:2px
    style N fill:#fcf,stroke:#333,stroke-width:2px
    style O fill:#fcc,stroke:#333,stroke-width:2px
````

**Explicación del Diagrama y Relaciones:**

1.  **Cliente (Frontend/App):**
    *   Representa la aplicación cliente (navegador web, aplicación móvil) que interactúa con el sistema.
    *   Se comunica con el `api-gateway` a través de HTTPS, enviando peticiones que incluyen un token JWT en el encabezado `Authorization`.

2.  **API Gateway (`api-gateway`):**
    *   **Punto de entrada único:** Todas las peticiones externas pasan por aquí.
    *   **Enrutamiento:** Dirige cada petición al microservicio adecuado según la URL.
    *   **Autenticación y Autorización:**
        *   Valida el token JWT (usando Spring Security y la configuración de Keycloak).
        *   Verifica que el usuario tenga los permisos (roles) necesarios.
    *   **Comunicación con Microservicios:** Utiliza Feign Clients para reenviar las peticiones a los microservicios, incluyendo el token JWT.

3.  **Microservicios (`api-usuario`, `api-cuenta`, `api-transactions`, `Cards`):**
    *   Cada microservicio se encarga de una parte específica de la lógica de negocio.
    *   **Comunicación entre Microservicios (Feign):**
        *   `api-usuario` se comunica con `api-cuenta` (para obtener el nombre al crear una cuenta).
        *   `api-transactions` se comunica con `api-usuario` (para obtener datos del usuario) y con `api-cuenta` (para actualizar saldos).
        *   `Cards` se comunica con `api-usuario` (para verificar la existencia del usuario).
        *   Esta comunicación se realiza a través de Feign Clients (interfaces declarativas que Spring Cloud OpenFeign convierte en clientes REST).  El gateway pasa el token JWT a los microservicios, y estos lo usan en sus llamadas Feign.
    *   **Configuración (Config Server):**  Cada microservicio obtiene su configuración del `config-server`. El `config-server` lee la configuración desde un repositorio Git (GitHub en este caso).
    * **Registro y descubrimiento (Eureka):** Cada microservicio se registra con `eureka-server`, se comunica y es encontrado a través del mismo.

4.  **Bases de Datos:**
    *   Cada microservicio (excepto `api-usuario`) tiene su propia base de datos MySQL.
    *   La interacción con la base de datos se realiza a través de JPA (Java Persistence API) con Hibernate como implementación.  Spring Data JPA simplifica aún más el acceso a datos.
    *   `api-usuario` *no* tiene su propia base de datos.  Gestiona los usuarios *directamente* en Keycloak.

5.  **Keycloak (Contenedor):**
    *   Keycloak se ejecuta en su propio contenedor Docker (según el `docker-compose.yml`).
    *   `api-usuario` utiliza la API de administración de Keycloak (a través de la biblioteca cliente de Keycloak) para crear, actualizar y eliminar usuarios.
    *   Keycloak, a su vez, utiliza la base de datos MySQL (`mysql-kc`) para almacenar su propia información (usuarios, roles, configuraciones, etc.). La conexión entre Keycloak y su base de datos es a través de JDBC.

6. **Configuración externa (GitHub):**
    * El `config-server` lee la configuración de la aplicación desde un repositorio de git.

**Detalles Adicionales Importantes:**

*   **JWT (JSON Web Token):**  El mecanismo principal para la autenticación y autorización.  El cliente obtiene un token JWT al iniciar sesión en `api-usuario` (que interactúa con Keycloak).  Este token se incluye en cada petición posterior, y es validado por el gateway y los microservicios.
*   **Spring Security:**  Se utiliza en el gateway y en cada microservicio para validar el token JWT, extraer la información del usuario (roles, etc.) y aplicar políticas de autorización (por ejemplo, permitir acceso solo a usuarios con ciertos roles).  Las clases `WebSecurityConfig`, `JwtAuthConverter` y `JwtDecoderConfig` en cada microservicio configuran Spring Security.
*   **Feign Clients:**  Simplifican la comunicación entre microservicios.  Se definen interfaces (como `UserFeignClient`, `CuentaFeignClient`) y Spring Cloud OpenFeign se encarga de generar la implementación que realiza las llamadas HTTP.  El token JWT se propaga automáticamente a través de Feign (gracias a la configuración de `RequestInterceptor`).
*   **Docker Compose:** Facilita la ejecución local de la aplicación, creando los contenedores necesarios para Keycloak y la base de datos MySQL.