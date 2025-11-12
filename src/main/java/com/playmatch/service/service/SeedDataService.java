package com.playmatch.service.service;

import com.playmatch.service.dto.SeedReservasRequest;
import com.playmatch.service.dto.SeedReservasResponse;
import com.playmatch.service.entity.AdministradorCancha;
import com.playmatch.service.entity.Cancha;
import com.playmatch.service.entity.Jugador;
import com.playmatch.service.entity.Reserva;
import com.playmatch.service.entity.Role;
import com.playmatch.service.entity.Usuario;
import com.playmatch.service.exception.NotFoundException;
import com.playmatch.service.repository.AdministradorCanchaRepository;
import com.playmatch.service.repository.CanchaRepository;
import com.playmatch.service.repository.JugadorRepository;
import com.playmatch.service.repository.ReservaRepository;
import com.playmatch.service.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class SeedDataService {

    private final AdministradorCanchaRepository administradorCanchaRepository;
    private final CanchaRepository canchaRepository;
    private final JugadorRepository jugadorRepository;
    private final ReservaRepository reservaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final Random random = new Random();

    // Nombres aleatorios para jugadores
    private static final String[] NOMBRES = {
        "Juan", "María", "Carlos", "Ana", "Luis", "Laura", "Pedro", "Sofia",
        "Diego", "Valentina", "Jorge", "Camila", "Ricardo", "Daniela", "Fernando",
        "Isabella", "Miguel", "Lucía", "Andrés", "Martina", "Pablo", "Emma",
        "Sebastián", "Olivia", "Mateo", "Victoria", "Alejandro", "Gabriela"
    };

    private static final String[] APELLIDOS = {
        "García", "Rodríguez", "Martínez", "López", "González", "Pérez", "Sánchez",
        "Ramírez", "Torres", "Flores", "Rivera", "Gómez", "Díaz", "Cruz", "Morales",
        "Reyes", "Gutiérrez", "Ortiz", "Mendoza", "Silva", "Castro", "Vargas"
    };

    private static final Reserva.EstadoReserva[] ESTADOS = {
        Reserva.EstadoReserva.CONFIRMADA,
        Reserva.EstadoReserva.CONFIRMADA,
        Reserva.EstadoReserva.CONFIRMADA,
        Reserva.EstadoReserva.PENDIENTE,
        Reserva.EstadoReserva.CANCELADA
    };

    public SeedDataService(AdministradorCanchaRepository administradorCanchaRepository,
                          CanchaRepository canchaRepository,
                          JugadorRepository jugadorRepository,
                          ReservaRepository reservaRepository,
                          UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder) {
        this.administradorCanchaRepository = administradorCanchaRepository;
        this.canchaRepository = canchaRepository;
        this.jugadorRepository = jugadorRepository;
        this.reservaRepository = reservaRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public SeedReservasResponse generarReservasParaAdministrador(SeedReservasRequest request) {
        // Buscar administrador por nombre
        AdministradorCancha administrador = administradorCanchaRepository
                .findByNombreIgnoreCase(request.getNombreAdministrador())
                .orElseThrow(() -> new NotFoundException(
                    "No se encontró un administrador con el nombre: " + request.getNombreAdministrador()));

        // Obtener todas las canchas del administrador
        List<Cancha> canchas = new ArrayList<>(administrador.getCanchas());
        if (canchas.isEmpty()) {
            throw new NotFoundException("El administrador " + request.getNombreAdministrador() + 
                                       " no tiene canchas registradas");
        }

        // Definir rango de fechas (por defecto: últimos 30 días hasta 30 días en el futuro)
        LocalDate fechaDesde = request.getFechaDesde() != null ? 
            request.getFechaDesde() : LocalDate.now().minusDays(30);
        LocalDate fechaHasta = request.getFechaHasta() != null ? 
            request.getFechaHasta() : LocalDate.now().plusDays(30);

        // Crear jugadores aleatorios si es necesario
        List<Jugador> jugadoresExistentes = jugadorRepository.findAll();
        int jugadoresNecesarios = Math.max(5, request.getCantidadReservas() / 3);
        List<Jugador> jugadoresCreados = new ArrayList<>();
        
        if (jugadoresExistentes.size() < jugadoresNecesarios) {
            int cantidadACrear = jugadoresNecesarios - jugadoresExistentes.size();
            jugadoresCreados = crearJugadoresAleatorios(cantidadACrear);
            jugadoresExistentes.addAll(jugadoresCreados);
        }

        // Generar reservas
        List<Reserva> reservasCreadas = new ArrayList<>();
        int intentos = 0;
        int maxIntentos = request.getCantidadReservas() * 10;

        while (reservasCreadas.size() < request.getCantidadReservas() && intentos < maxIntentos) {
            intentos++;
            
            try {
                Reserva reserva = generarReservaAleatoria(
                    canchas, 
                    jugadoresExistentes, 
                    fechaDesde, 
                    fechaHasta
                );
                
                if (reserva != null) {
                    reservasCreadas.add(reserva);
                }
            } catch (Exception e) {
                // Ignorar conflictos y continuar intentando
            }
        }

        // Preparar respuesta
        List<String> nombresCancha = canchas.stream()
                .map(Cancha::getNombre)
                .collect(Collectors.toList());

        String mensaje = String.format(
            "Se generaron %d reservas aleatorias para las canchas del administrador %s",
            reservasCreadas.size(),
            administrador.getNombre()
        );

        return new SeedReservasResponse(
            mensaje,
            reservasCreadas.size(),
            jugadoresCreados.size(),
            nombresCancha,
            administrador.getNombre()
        );
    }

    private List<Jugador> crearJugadoresAleatorios(int cantidad) {
        List<Jugador> jugadores = new ArrayList<>();
        
        for (int i = 0; i < cantidad; i++) {
            String nombre = NOMBRES[random.nextInt(NOMBRES.length)] + " " + 
                           APELLIDOS[random.nextInt(APELLIDOS.length)];
            String nombreUsuario = nombre.toLowerCase().replace(" ", "") + random.nextInt(1000);
            String email = nombreUsuario + "@example.com";
            
            // Verificar si el email ya existe
            if (usuarioRepository.findByEmail(email).isPresent()) {
                continue;
            }

            Usuario usuario = new Usuario();
            usuario.setEmail(email);
            usuario.setPassword(passwordEncoder.encode("password123"));
            usuario.setRole(Role.JUGADOR);
            usuario = usuarioRepository.save(usuario);

            Jugador jugador = new Jugador();
            jugador.setNombre(nombre);
            jugador.setTelefono("+591 7" + (10000000 + random.nextInt(90000000)));
            jugador.setUsuario(usuario);
            jugador = jugadorRepository.save(jugador);
            
            jugadores.add(jugador);
        }
        
        return jugadores;
    }

    private Reserva generarReservaAleatoria(List<Cancha> canchas, List<Jugador> jugadores,
                                            LocalDate fechaDesde, LocalDate fechaHasta) {
        // Seleccionar cancha aleatoria
        Cancha cancha = canchas.get(random.nextInt(canchas.size()));
        
        // Seleccionar jugador aleatorio
        Jugador jugador = jugadores.get(random.nextInt(jugadores.size()));
        
        // Generar fecha aleatoria en el rango
        long diasDiferencia = fechaHasta.toEpochDay() - fechaDesde.toEpochDay();
        LocalDate fecha = fechaDesde.plusDays(random.nextInt((int) diasDiferencia + 1));
        
        // Generar hora de inicio aleatoria (entre 8:00 y 20:00)
        int horaInicio = 8 + random.nextInt(13); // 8 a 20
        int minutosInicio = random.nextBoolean() ? 0 : 30;
        LocalTime horaInicioTime = LocalTime.of(horaInicio, minutosInicio);
        
        // Duración aleatoria (1 o 2 horas)
        int duracion = random.nextBoolean() ? 1 : 2;
        LocalTime horaFinTime = horaInicioTime.plusHours(duracion);
        
        LocalDateTime inicio = LocalDateTime.of(fecha, horaInicioTime);
        LocalDateTime fin = LocalDateTime.of(fecha, horaFinTime);
        
        // Verificar que no haya solapamiento
        List<Reserva> solapadas = reservaRepository.findOverlapping(cancha, inicio, fin);
        if (!solapadas.isEmpty()) {
            return null; // Hay conflicto, retornar null
        }
        
        // Crear reserva
        Reserva reserva = new Reserva();
        reserva.setCancha(cancha);
        reserva.setJugador(jugador);
        reserva.setInicio(inicio);
        reserva.setFin(fin);
        reserva.setEstado(ESTADOS[random.nextInt(ESTADOS.length)]);
        
        // Calcular monto
        double precioHora = cancha.getPrecioHora().doubleValue();
        double monto = precioHora * duracion;
        reserva.setMonto(monto);
        
        return reservaRepository.save(reserva);
    }
}
