# API de Reservas - Filtros y Ordenamiento

## Endpoints actualizados

### 1. GET /api/reservas - Mis Reservas (Jugador)

Lista las reservas del jugador autenticado con opciones de filtrado y ordenamiento.

#### Parámetros de Query (todos opcionales):

| Parámetro | Tipo | Descripción | Ejemplo |
|-----------|------|-------------|---------|
| `estado` | String | Filtrar por estado: PENDIENTE, CONFIRMADA, CANCELADA | `?estado=CONFIRMADA` |
| `fechaDesde` | Date (ISO) | Filtrar reservas desde esta fecha (inclusive) | `?fechaDesde=2025-11-01` |
| `fechaHasta` | Date (ISO) | Filtrar reservas hasta esta fecha (inclusive) | `?fechaHasta=2025-11-30` |
| `canchaId` | Long | Filtrar solo reservas de una cancha específica | `?canchaId=1` |
| `ordenarPor` | String | Campo por el cual ordenar | `?ordenarPor=fecha` |
| `direccion` | String | Dirección de ordenamiento: `asc` o `desc` | `?direccion=desc` |

#### Ejemplos de uso:

```bash
# Listar todas mis reservas ordenadas por fecha descendente (más recientes primero)
GET /api/reservas?ordenarPor=fecha&direccion=desc

# Solo mis reservas confirmadas
GET /api/reservas?estado=CONFIRMADA

# Reservas del mes de noviembre 2025
GET /api/reservas?fechaDesde=2025-11-01&fechaHasta=2025-11-30

# Reservas de una cancha específica ordenadas por hora
GET /api/reservas?canchaId=2&ordenarPor=hora&direccion=asc

# Reservas confirmadas del mes actual ordenadas por monto
GET /api/reservas?estado=CONFIRMADA&fechaDesde=2025-11-01&fechaHasta=2025-11-30&ordenarPor=monto&direccion=desc

# Combinación: reservas pendientes de esta semana en cancha 1
GET /api/reservas?estado=PENDIENTE&fechaDesde=2025-11-03&fechaHasta=2025-11-09&canchaId=1
```

---

### 2. GET /api/reservas/canchas/{canchaId} - Reservas por Cancha

Lista todas las reservas de una cancha específica (para administradores o consulta pública).

#### Parámetros de Query (todos opcionales):

| Parámetro | Tipo | Descripción | Ejemplo |
|-----------|------|-------------|---------|
| `estado` | String | Filtrar por estado: PENDIENTE, CONFIRMADA, CANCELADA | `?estado=PENDIENTE` |
| `fechaDesde` | Date (ISO) | Filtrar reservas desde esta fecha (inclusive) | `?fechaDesde=2025-11-01` |
| `fechaHasta` | Date (ISO) | Filtrar reservas hasta esta fecha (inclusive) | `?fechaHasta=2025-11-30` |
| `cliente` | String | Buscar por nombre del cliente (búsqueda parcial) | `?cliente=Juan` |
| `ordenarPor` | String | Campo por el cual ordenar | `?ordenarPor=fecha` |
| `direccion` | String | Dirección de ordenamiento: `asc` o `desc` | `?direccion=asc` |

#### Ejemplos de uso:

```bash
# Todas las reservas de la cancha 1 ordenadas por fecha
GET /api/reservas/canchas/1?ordenarPor=fecha&direccion=desc

# Reservas pendientes de aprobación
GET /api/reservas/canchas/1?estado=PENDIENTE&ordenarPor=fecha&direccion=asc

# Reservas de un cliente específico
GET /api/reservas/canchas/1?cliente=Carlos

# Reservas confirmadas del día de hoy
GET /api/reservas/canchas/1?estado=CONFIRMADA&fechaDesde=2025-11-03&fechaHasta=2025-11-03

# Reservas de la próxima semana ordenadas por hora de inicio
GET /api/reservas/canchas/1?fechaDesde=2025-11-04&fechaHasta=2025-11-10&ordenarPor=hora&direccion=asc

# Todas las reservas de "María" ordenadas por monto descendente
GET /api/reservas/canchas/1?cliente=María&ordenarPor=monto&direccion=desc
```

---

## Campos de Ordenamiento disponibles

| Campo | Alias aceptados | Descripción |
|-------|----------------|-------------|
| `fecha` | `fecha` | Fecha de la reserva (por defecto) |
| `hora` | `hora`, `horainicio` | Hora de inicio de la reserva |
| `horafin` | `horafin` | Hora de finalización |
| `estado` | `estado` | Estado de la reserva (alfabético) |
| `cliente` | `cliente` | Nombre del cliente (alfabético) |
| `monto` | `monto` | Monto de la reserva (numérico) |
| `cancha` | `cancha`, `canchaid` | ID de la cancha |

---

## Estados de Reserva

- **PENDIENTE** - Reserva creada, esperando confirmación del administrador
- **CONFIRMADA** - Reserva aprobada por el administrador
- **CANCELADA** - Reserva rechazada o cancelada

---

## Formato de Fechas

Las fechas deben enviarse en formato ISO 8601: `YYYY-MM-DD`

Ejemplos:
- `2025-11-03` (3 de noviembre de 2025)
- `2025-12-31` (31 de diciembre de 2025)

---

## Ejemplos de respuesta

```json
[
  {
    "id": 1,
    "canchaId": 1,
    "cliente": "Carlos Rodriguez",
    "estado": "confirmada",
    "fecha": "2025-11-11",
    "horaInicio": "09:00:00",
    "horaFin": "10:00:00",
    "monto": 25000.0
  },
  {
    "id": 2,
    "canchaId": 1,
    "cliente": "Maria Gonzalez",
    "estado": "pendiente",
    "fecha": "2025-11-14",
    "horaInicio": "14:00:00",
    "horaFin": "15:00:00",
    "monto": 25000.0
  }
]
```

---

## Casos de Uso Comunes

### Para Jugadores:

1. **Ver mis próximas reservas**
   ```
   GET /api/reservas?fechaDesde=2025-11-03&ordenarPor=fecha&direccion=asc
   ```

2. **Ver historial de reservas confirmadas**
   ```
   GET /api/reservas?estado=CONFIRMADA&ordenarPor=fecha&direccion=desc
   ```

3. **Reservas pendientes de confirmación**
   ```
   GET /api/reservas?estado=PENDIENTE
   ```

### Para Administradores de Cancha:

1. **Ver reservas pendientes de aprobación**
   ```
   GET /api/reservas/canchas/1?estado=PENDIENTE&ordenarPor=fecha&direccion=asc
   ```

2. **Agenda del día**
   ```
   GET /api/reservas/canchas/1?fechaDesde=2025-11-03&fechaHasta=2025-11-03&ordenarPor=hora&direccion=asc
   ```

3. **Buscar reservas de un cliente**
   ```
   GET /api/reservas/canchas/1?cliente=Juan
   ```

4. **Ingresos confirmados del mes**
   ```
   GET /api/reservas/canchas/1?estado=CONFIRMADA&fechaDesde=2025-11-01&fechaHasta=2025-11-30&ordenarPor=monto&direccion=desc
   ```
