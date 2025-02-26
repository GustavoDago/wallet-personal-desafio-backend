

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