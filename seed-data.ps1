# Seed data - Crear jugadores y reservas
$baseUrl = "http://localhost:8080/api"
$timestamp = (Get-Date).ToString("HHmmss")

# Jugadores
$jugadores = @(
    @{ nombre="Carlos Rodriguez"; telefono="+56912345671"; email="carlos.rod.$timestamp@email.com"; password="Pass123!" },
    @{ nombre="Maria Gonzalez"; telefono="+56912345672"; email="maria.gon.$timestamp@email.com"; password="Pass123!" },
    @{ nombre="Juan Perez"; telefono="+56912345673"; email="juan.per.$timestamp@email.com"; password="Pass123!" },
    @{ nombre="Ana Martinez"; telefono="+56912345674"; email="ana.mar.$timestamp@email.com"; password="Pass123!" },
    @{ nombre="Diego Silva"; telefono="+56912345675"; email="diego.sil.$timestamp@email.com"; password="Pass123!" },
    @{ nombre="Sofia Fernandez"; telefono="+56912345676"; email="sofia.fer.$timestamp@email.com"; password="Pass123!" },
    @{ nombre="Luis Torres"; telefono="+56912345677"; email="luis.tor.$timestamp@email.com"; password="Pass123!" },
    @{ nombre="Valentina Rojas"; telefono="+56912345678"; email="vale.roj.$timestamp@email.com"; password="Pass123!" }
)

Write-Host "=== CREANDO JUGADORES ===" -ForegroundColor Cyan
$tokens = @()

foreach ($j in $jugadores) {
    Write-Host "Registrando: $($j.nombre)" -ForegroundColor Yellow
    
    $registerBody = @{
        nombre = $j.nombre
        telefono = $j.telefono
        email = $j.email
        password = $j.password
        role = "JUGADOR"
    } | ConvertTo-Json
    
    try {
        $resp = Invoke-RestMethod -Uri "$baseUrl/auth/register" -Method Post -Body $registerBody -ContentType "application/json"
        $tokens += @{ nombre=$j.nombre; token=$resp.token }
        Write-Host "  OK - Token obtenido" -ForegroundColor Green
    } catch {
        Write-Host "  ERROR: $_" -ForegroundColor Red
    }
}

Write-Host "`n=== OBTENIENDO CANCHAS ===" -ForegroundColor Cyan

# Obtener canchas (sin auth)
$canchas = @()
try {
    $canchas = Invoke-RestMethod -Uri "$baseUrl/canchas" -Method Get -ContentType "application/json"
    Write-Host "Canchas encontradas: $($canchas.Count)" -ForegroundColor Green
    
    if ($canchas.Count -eq 0) {
        Write-Host "ERROR: No hay canchas disponibles!" -ForegroundColor Red
        exit
    }
    
    $canchas | ForEach-Object { Write-Host "  - ID: $($_.id) - $($_.nombre)" -ForegroundColor Gray }
} catch {
    Write-Host "ERROR obteniendo canchas: $_" -ForegroundColor Red
    exit
}

Write-Host "`n=== CREANDO RESERVAS ===" -ForegroundColor Cyan
$reservasOk = 0
$fechaBase = Get-Date

foreach ($tokenInfo in $tokens) {
    $numReservas = Get-Random -Minimum 1 -Maximum 3
    
    for ($i = 0; $i -lt $numReservas; $i++) {
        $cancha = $canchas[(Get-Random -Maximum $canchas.Count)]
        $dias = Get-Random -Minimum 1 -Maximum 15
        $hora = Get-Random -Minimum 8 -Maximum 21
        
        $inicio = $fechaBase.AddDays($dias).Date.AddHours($hora)
        $fin = $inicio.AddHours(1)
        
        $reservaBody = @{
            canchaId = $cancha.id
            inicio = $inicio.ToString("yyyy-MM-ddTHH:mm:ss")
            fin = $fin.ToString("yyyy-MM-ddTHH:mm:ss")
        } | ConvertTo-Json
        
        Write-Host "$($tokenInfo.nombre) -> $($cancha.nombre) el $($inicio.ToString('dd/MM HH:mm'))" -ForegroundColor Yellow
        
        try {
            $headers = @{
                "Authorization" = "Bearer $($tokenInfo.token)"
                "Content-Type" = "application/json"
            }
            $resp = Invoke-RestMethod -Uri "$baseUrl/reservas" -Method Post -Headers $headers -Body $reservaBody
            Write-Host "  OK - ID: $($resp.id)" -ForegroundColor Green
            $reservasOk++
        } catch {
            Write-Host "  ERROR: $_" -ForegroundColor Red
        }
    }
}

Write-Host "`n=== RESUMEN ===" -ForegroundColor Cyan
Write-Host "Jugadores: $($tokens.Count)" -ForegroundColor Green
Write-Host "Reservas: $reservasOk" -ForegroundColor Green
Write-Host "COMPLETADO!" -ForegroundColor Green
