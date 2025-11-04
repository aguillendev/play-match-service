# Script para probar la lógica de reportes con reservas confirmadas

$baseUrl = "http://localhost:8080/api"

Write-Host "=== PRUEBA DE LÓGICA DE REPORTES ===" -ForegroundColor Cyan
Write-Host ""

# Paso 1: Crear un administrador de cancha y obtener su token
Write-Host "1. Registrando administrador de cancha..." -ForegroundColor Yellow
$timestamp = (Get-Date).ToString("HHmmss")
$adminBody = @{
    nombre = "Admin Test"
    telefono = "+56999999999"
    email = "admin.$timestamp@test.com"
    password = "Admin123!"
    role = "ADMINISTRADOR_CANCHA"
} | ConvertTo-Json

try {
    $adminResp = Invoke-RestMethod -Uri "$baseUrl/auth/register" -Method Post -Body $adminBody -ContentType "application/json"
    $adminToken = $adminResp.token
    $adminId = $adminResp.usuario.id
    Write-Host "   OK - Admin ID: $adminId" -ForegroundColor Green
} catch {
    Write-Host "   ERROR: $_" -ForegroundColor Red
    exit
}

# Paso 2: Crear una cancha
Write-Host "`n2. Creando cancha de prueba..." -ForegroundColor Yellow
$canchaBody = @{
    nombre = "Cancha Test Reportes"
    direccion = "Calle Test 123"
    latitud = -33.4489
    longitud = -70.6693
    precioHora = 25000
    horarioApertura = "08:00:00"
    horarioCierre = "22:00:00"
    tipo = "FUTBOL"
} | ConvertTo-Json

try {
    $headers = @{
        "Authorization" = "Bearer $adminToken"
        "Content-Type" = "application/json"
    }
    $canchaResp = Invoke-RestMethod -Uri "$baseUrl/canchas" -Method Post -Headers $headers -Body $canchaBody
    $canchaId = $canchaResp.id
    Write-Host "   OK - Cancha ID: $canchaId" -ForegroundColor Green
} catch {
    Write-Host "   ERROR: $_" -ForegroundColor Red
    exit
}

# Paso 3: Crear jugadores y hacer reservas
Write-Host "`n3. Creando jugadores y reservas..." -ForegroundColor Yellow
$jugadores = @()
$reservas = @()

for ($i = 1; $i -le 5; $i++) {
    $jugadorBody = @{
        nombre = "Jugador Test $i"
        telefono = "+56900000$i$i$i"
        email = "jugador$i.$timestamp@test.com"
        password = "Test123!"
        role = "JUGADOR"
    } | ConvertTo-Json
    
    try {
        $jugadorResp = Invoke-RestMethod -Uri "$baseUrl/auth/register" -Method Post -Body $jugadorBody -ContentType "application/json"
        $jugadores += @{ nombre = "Jugador $i"; token = $jugadorResp.token }
        Write-Host "   - Jugador $i registrado" -ForegroundColor Gray
    } catch {
        Write-Host "   - ERROR registrando jugador $i" -ForegroundColor Red
    }
}

# Crear reservas: algunas pasadas, algunas futuras
Write-Host "`n4. Creando reservas (pasadas y futuras)..." -ForegroundColor Yellow
$ahora = Get-Date
$reservasData = @(
    @{ dias = -2; hora = 10; nombre = "Reserva Pasada 1 (hace 2 días)" },
    @{ dias = -1; hora = 14; nombre = "Reserva Pasada 2 (ayer)" },
    @{ dias = 0; hora = ($ahora.Hour - 2); nombre = "Reserva Pasada 3 (hoy hace 2h)" },
    @{ dias = 0; hora = ($ahora.Hour + 2); nombre = "Reserva Futura 1 (hoy en 2h)" },
    @{ dias = 1; hora = 16; nombre = "Reserva Futura 2 (mañana)" }
)

foreach ($reservaData in $reservasData) {
    if ($jugadores.Count -eq 0) { break }
    
    $jugador = $jugadores[(Get-Random -Maximum $jugadores.Count)]
    $inicio = $ahora.AddDays($reservaData.dias).Date.AddHours($reservaData.hora)
    $fin = $inicio.AddHours(1)
    
    $reservaBody = @{
        canchaId = $canchaId
        inicio = $inicio.ToString("yyyy-MM-ddTHH:mm:ss")
        fin = $fin.ToString("yyyy-MM-ddTHH:mm:ss")
    } | ConvertTo-Json
    
    try {
        $jugadorHeaders = @{
            "Authorization" = "Bearer $($jugador.token)"
            "Content-Type" = "application/json"
        }
        $reservaResp = Invoke-RestMethod -Uri "$baseUrl/reservas" -Method Post -Headers $jugadorHeaders -Body $reservaBody
        $reservas += @{ id = $reservaResp.id; nombre = $reservaData.nombre; estado = "PENDIENTE"; pasada = ($reservaData.dias -lt 0 -or ($reservaData.dias -eq 0 -and $reservaData.hora -lt $ahora.Hour)) }
        Write-Host "   - $($reservaData.nombre): ID $($reservaResp.id)" -ForegroundColor Gray
    } catch {
        Write-Host "   - ERROR: $($reservaData.nombre)" -ForegroundColor Red
    }
}

Write-Host "`n5. Confirmando solo las reservas PASADAS..." -ForegroundColor Yellow
Write-Host "   (Las reservas futuras quedan PENDIENTES)" -ForegroundColor Gray

foreach ($reserva in $reservas) {
    if ($reserva.pasada) {
        try {
            $confirmResp = Invoke-RestMethod -Uri "$baseUrl/reservas/$($reserva.id)/confirmar" -Method Post -ContentType "application/json"
            Write-Host "   - Reserva $($reserva.id) CONFIRMADA: $($reserva.nombre)" -ForegroundColor Green
            $reserva.estado = "CONFIRMADA"
        } catch {
            Write-Host "   - ERROR confirmando reserva $($reserva.id)" -ForegroundColor Red
        }
    } else {
        Write-Host "   - Reserva $($reserva.id) PENDIENTE (futura): $($reserva.nombre)" -ForegroundColor Yellow
    }
}

# Paso 6: Consultar reportes
Write-Host "`n6. Consultando reportes..." -ForegroundColor Yellow
Start-Sleep -Seconds 2

try {
    $reporte = Invoke-RestMethod -Uri "$baseUrl/reportes/reservas?canchaId=$canchaId&periodo=mes" -Method Get -ContentType "application/json"
    
    Write-Host "`n=== RESULTADO DEL REPORTE ===" -ForegroundColor Cyan
    Write-Host "Total de días con reservas: $($reporte.Count)" -ForegroundColor White
    
    $totalReservas = 0
    $totalRecaudacion = 0
    
    foreach ($dia in $reporte) {
        $totalReservas += $dia.totalReservas
        $totalRecaudacion += $dia.recaudacion
        $recaudacionStr = if ($dia.recaudacion -gt 0) { "$($dia.recaudacion) (CONFIRMADAS Y PASADAS)" } else { "0 (pendientes o futuras)" }
        Write-Host "  - $($dia.fecha): $($dia.totalReservas) reservas, Recaudación: $recaudacionStr" -ForegroundColor Gray
    }
    
    Write-Host "`nRESUMEN:" -ForegroundColor Cyan
    Write-Host "  Total Reservas: $totalReservas" -ForegroundColor White
    Write-Host "  Total Recaudación: $totalRecaudacion" -ForegroundColor Green
    Write-Host "`nNOTA: Solo se contabiliza recaudación de reservas CONFIRMADAS que ya finalizaron" -ForegroundColor Yellow
    
} catch {
    Write-Host "ERROR consultando reportes: $_" -ForegroundColor Red
}

Write-Host "`n=== PRUEBA COMPLETADA ===" -ForegroundColor Green
