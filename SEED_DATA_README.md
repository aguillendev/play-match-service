# API de Generación de Datos de Prueba (Seed Data)

Este módulo proporciona endpoints para generar datos de prueba (reservas con jugadores aleatorios) para el sistema PlayMatch.

## Archivos Creados

### DTOs
- **`SeedReservasRequest.java`**: DTO para recibir parámetros de generación
- **`SeedReservasResponse.java`**: DTO con información sobre las reservas generadas

### Servicios
- **`SeedDataService.java`**: Lógica de negocio para generar reservas y jugadores aleatorios

### Controladores
- **`SeedDataController.java`**: Endpoint REST para la generación de datos

### Repositorios Actualizados
- **`AdministradorCanchaRepository.java`**: Agregado método `findByNombreIgnoreCase(String nombre)`

## Endpoint

### POST `/api/seed/reservas`

Genera reservas aleatorias con jugadores ficticios para todas las canchas de un administrador específico.

#### Request Body

```json
{
  "nombreAdministrador": "Diegol",
  "cantidadReservas": 20,
  "fechaDesde": "2025-10-01",
  "fechaHasta": "2025-12-31"
}
```

#### Parámetros

| Campo | Tipo | Requerido | Default | Descripción |
|-------|------|-----------|---------|-------------|
| `nombreAdministrador` | String | Sí | - | Nombre del administrador de canchas |
| `cantidadReservas` | Integer | No | 10 | Cantidad de reservas a generar |
| `fechaDesde` | Date | No | Hoy - 30 días | Fecha inicial del rango |
| `fechaHasta` | Date | No | Hoy + 30 días | Fecha final del rango |

#### Response

```json
{
  "mensaje": "Se generaron 20 reservas aleatorias para las canchas del administrador Diegol",
  "cantidadReservasCreadas": 20,
  "cantidadJugadoresCreados": 8,
  "canchasAfectadas": [
    "Cancha Futbol 5 Central",
    "Cancha de Tenis Norte"
  ],
  "administradorCancha": "Diegol"
}
```

## Características

### Generación de Jugadores
- Si no hay suficientes jugadores en la base de datos, se crean automáticamente
- Los jugadores tienen nombres y apellidos aleatorios de listas predefinidas
- Cada jugador tiene un usuario asociado con email único
- Teléfonos bolivianos generados aleatoriamente (+591 7XXXXXXXX)

### Generación de Reservas
- Distribuidas aleatoriamente entre todas las canchas del administrador
- Fechas dentro del rango especificado
- Horarios entre 8:00 AM y 10:00 PM
- Duración aleatoria: 1 o 2 horas
- Estados aleatorios: CONFIRMADA (60%), PENDIENTE (20%), CANCELADA (20%)
- Validación de solapamiento: no se crean reservas que conflicten con existentes
- Cálculo automático del monto según precio de la cancha y duración

## Ejemplo de Uso con cURL

```bash
curl -X POST http://localhost:8080/api/seed/reservas \
  -H "Content-Type: application/json" \
  -d '{
    "nombreAdministrador": "Diegol",
    "cantidadReservas": 50,
    "fechaDesde": "2025-10-01",
    "fechaHasta": "2025-12-31"
  }'
```

## Ejemplo de Uso con JavaScript (fetch)

```javascript
const response = await fetch('http://localhost:8080/api/seed/reservas', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify({
    nombreAdministrador: 'Diegol',
    cantidadReservas: 30
  })
});

const data = await response.json();
console.log(`Reservas creadas: ${data.cantidadReservasCreadas}`);
```

## Notas Importantes

1. **Búsqueda Case-Insensitive**: El nombre del administrador no distingue mayúsculas/minúsculas
2. **Validación de Conflictos**: El servicio intenta crear las reservas evitando solapamientos
3. **Intentos Limitados**: Si hay muchos conflictos, puede que no se creen todas las reservas solicitadas
4. **Jugadores Reutilizables**: Los jugadores creados quedan disponibles para futuras generaciones
5. **Transaccional**: Todo el proceso se ejecuta en una transacción, asegurando consistencia

## Manejo de Errores

| Código | Descripción |
|--------|-------------|
| 404 | Administrador no encontrado |
| 404 | El administrador no tiene canchas registradas |
| 400 | Parámetros inválidos (validación de @Validated) |

## Swagger/OpenAPI

La documentación interactiva está disponible en:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Busca el tag **"Seed Data"** para encontrar este endpoint.
