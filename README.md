# Play Match Service

Backend construido con Spring Boot para gestionar reservas de canchas, publicación de partidos y reportes para dueños.

## Características principales
- Gestión de usuarios con roles Jugador y Dueño utilizando JWT para autenticación.
- Administración de canchas, reservas y partidos con relaciones JPA.
- Endpoints REST documentados con OpenAPI/Swagger.
- Manejo global de errores mediante `@ControllerAdvice`.
- Observabilidad preparada con OpenTelemetry y logging estructurado.

## Resumen del trabajo realizado
- Se generó una aplicación Spring Boot 3 con Maven que incluye la configuración base y dependencias necesarias.
- Se modelaron las entidades principales (`Jugador`, `Dueño`, `Cancha`, `Reserva`, `Partido`) con repositorios JPA y DTOs de entrada/salida.
- Se implementaron servicios para búsquedas de canchas, reservas y organización de partidos con validaciones básicas.
- Se expusieron controladores REST para autenticación, gestión de reservas, canchas, partidos y reportes.
- Se configuró la seguridad basada en JWT con roles de jugador y dueño, junto con un manejador global de errores.
- Se habilitó la documentación OpenAPI/Swagger y la integración con OpenTelemetry para trazas y logs estructurados.

## Requisitos
- Java 21
- Maven 3.9+
- PostgreSQL 14+

## Ejecución local
```bash
mvn spring-boot:run
```

La documentación interactiva estará disponible en `http://localhost:8080/swagger-ui`.
