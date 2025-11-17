# Play Match Service

Backend construido con Spring Boot 3 para gestionar reservas de canchas deportivas. Sistema de administración completo con autenticación JWT, roles de usuario y reportes.

## 🚀 Características principales

### Gestión de Usuarios
- **Autenticación JWT** con roles diferenciados:
  - `JUGADOR`: Puede crear y gestionar sus propias reservas
  - `ADMINISTRADOR_CANCHA`: Gestiona canchas, confirma/rechaza reservas y accede a reportes
- Registro de usuarios con validación de email único
- Endpoints de login y registro con generación de tokens

### Gestión de Canchas
- CRUD completo de canchas deportivas (Fútbol, Pádel, Tenis, Básquet)
- Configuración de horarios disponibles por intervalos
- Precios por hora personalizables
- Filtrado de canchas por tipo de deporte, ubicación y disponibilidad
- Asignación de canchas a administradores

### Sistema de Reservas
- Creación de reservas con validación de disponibilidad
- Estados de reserva: `PENDIENTE`, `CONFIRMADA`, `CANCELADA`
- Validación de solapamiento de horarios
- Confirmación/rechazo de reservas por administradores
- Confirmación masiva de reservas pendientes
- Filtros avanzados:
  - Por estado, fecha, cancha y cliente
  - Ordenamiento por fecha, hora, cliente, estado, monto y cancha
  - Consulta de reservas por jugador o por administrador

### Reportes y Analytics
- Dashboard con métricas de reservas:
  - Total de reservas (confirmadas, pendientes, canceladas)
  - Ingresos totales y promedio por reserva
  - Tasa de ocupación de canchas
- Reportes de reservas por período
- Comparativas de ocupación entre canchas

### Utilidades
- **Seeder de datos**: Generación automática de reservas de prueba
  - Respeta horarios de disponibilidad de canchas
  - Solo genera reservas en horarios redondos (sin :30 minutos)
  - Evita solapamientos
  - Crea jugadores aleatorios si es necesario

## 🛠️ Stack Tecnológico

- **Framework**: Spring Boot 3.5.7
- **Java**: 21
- **Base de datos**: PostgreSQL 18.1
- **ORM**: Hibernate/JPA
- **Seguridad**: Spring Security + JWT
- **Documentación API**: SpringDoc OpenAPI (Swagger)
- **Build Tool**: Maven 3.9+

## 📋 Requisitos

- Java 21
- Maven 3.9+
- PostgreSQL 14+ (puerto 5432)

## ⚙️ Configuración

### Base de datos

Crear una base de datos PostgreSQL:

```sql
CREATE DATABASE playmatch;
```

Configurar credenciales en `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/playmatch
    username: postgres
    password: admin
  jpa:
    hibernate:
      ddl-auto: update
```

## 🚀 Ejecución local

```bash
# Compilar el proyecto
mvn clean install

# Ejecutar la aplicación
mvn spring-boot:run
```

La aplicación estará disponible en `http://localhost:8080`

## 📚 Documentación API

Swagger UI: `http://localhost:8080/swagger-ui.html`

### Endpoints principales

#### Autenticación
- `POST /api/auth/register` - Registro de usuarios
- `POST /api/auth/login` - Login y obtención de JWT token

#### Canchas
- `GET /api/canchas` - Listar canchas con filtros
- `POST /api/canchas` - Crear cancha (Admin)
- `PUT /api/canchas/{id}` - Actualizar cancha (Admin)
- `DELETE /api/canchas/{id}` - Eliminar cancha (Admin)

#### Reservas
- `GET /api/reservas` - Mis reservas (Jugador)
- `GET /api/reservas/administrador` - Todas las reservas (Admin)
- `POST /api/reservas` - Crear reserva (Jugador)
- `POST /api/reservas/{id}/confirmar` - Confirmar reserva (Admin)
- `POST /api/reservas/{id}/rechazar` - Rechazar reserva (Admin)
- `POST /api/reservas/confirmar-todas` - Confirmar todas pendientes (Admin)
- `DELETE /api/reservas/{id}` - Cancelar reserva (Jugador)

#### Reportes
- `GET /api/reportes/dashboard` - Dashboard con métricas (Admin)
- `GET /api/reportes/reservas` - Reporte de reservas por período (Admin)

#### Seeder (Desarrollo)
- `POST /api/seed/reservas?adminNombre={nombre}&cantidadReservas={cantidad}` - Generar reservas de prueba

### Filtros de Reservas

Parámetros de query disponibles:
- `estado`: `pendiente`, `confirmada`, `cancelada`
- `fechaDesde`: Formato ISO (yyyy-MM-dd)
- `fechaHasta`: Formato ISO (yyyy-MM-dd)
- `canchaId`: ID de la cancha
- `cliente`: Nombre del cliente (búsqueda parcial)
- `ordenarPor`: `fecha`, `hora`, `cliente`, `estado`, `monto`, `cancha`
- `direccion`: `asc`, `desc`

Ejemplo:
```
GET /api/reservas/administrador?estado=pendiente&ordenarPor=cancha&direccion=asc
```

## 🔐 Seguridad

### Autenticación JWT

Incluir el token en el header de las peticiones:

```
Authorization: Bearer {token}
```

### Roles y Permisos

- **JUGADOR**:
  - Ver y crear sus propias reservas
  - Cancelar sus reservas
  - Ver canchas disponibles

- **ADMINISTRADOR_CANCHA**:
  - Todo lo anterior
  - Gestionar canchas
  - Confirmar/rechazar reservas
  - Acceder a reportes y dashboard
  - Usar el seeder de datos

## 🧪 Datos de Prueba

Usar el endpoint de seeder para generar datos de prueba:

```bash
curl -X POST "http://localhost:8080/api/seed/reservas?adminNombre=Juan&cantidadReservas=50" \
  -H "Authorization: Bearer {token}"
```

Características del seeder:
- Genera reservas solo en horarios redondos (10:00, 15:00, etc.)
- Respeta los horarios de disponibilidad de cada cancha
- Evita solapamientos
- Crea jugadores aleatorios si es necesario
- Genera reservas en un rango de ±30 días desde hoy

## 📝 Notas de Desarrollo

### Hibernate DDL

El proyecto usa `ddl-auto: update` para desarrollo. Para producción, se recomienda usar Flyway o Liquibase para migraciones controladas.

### CORS

CORS está configurado para permitir peticiones desde `http://localhost:5173` (Vite dev server).

## 🤝 Contribución

Este proyecto es parte del sistema Play Match para gestión de canchas deportivas.
