package com.playmatch.service.service;

import com.playmatch.service.dto.ReservaRequest;
import com.playmatch.service.dto.ReservaResponse;
import com.playmatch.service.entity.AdministradorCancha;
import com.playmatch.service.entity.Cancha;
import com.playmatch.service.entity.Jugador;
import com.playmatch.service.entity.Role;
import com.playmatch.service.entity.Reserva;
import com.playmatch.service.exception.BadRequestException;
import com.playmatch.service.exception.NotFoundException;
import com.playmatch.service.repository.AdministradorCanchaRepository;
import com.playmatch.service.repository.CanchaRepository;
import com.playmatch.service.repository.JugadorRepository;
import com.playmatch.service.repository.ReservaRepository;
import com.playmatch.service.security.UserPrincipal;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
public class ReservationService {

    private final ReservaRepository reservaRepository;
    private final JugadorRepository jugadorRepository;
    private final CanchaRepository canchaRepository;
    private final AdministradorCanchaRepository administradorCanchaRepository;

    public ReservationService(ReservaRepository reservaRepository,
                              JugadorRepository jugadorRepository,
                              CanchaRepository canchaRepository,
                              AdministradorCanchaRepository administradorCanchaRepository) {
        this.reservaRepository = reservaRepository;
        this.jugadorRepository = jugadorRepository;
        this.canchaRepository = canchaRepository;
        this.administradorCanchaRepository = administradorCanchaRepository;
    }

    @Transactional
    public ReservaResponse crearReserva(ReservaRequest request) {
        UserPrincipal principal = getAuthenticatedPrincipal();
        if (principal.getRole() != Role.JUGADOR) {
            throw new AccessDeniedException("Solo un usuario con rol jugador puede crear reservas");
        }
        Jugador jugador = jugadorRepository.findByUsuarioId(principal.getUsuarioId())
                .orElseThrow(() -> new AccessDeniedException("No se encontró un jugador asociado al usuario autenticado"));
        if (request.getJugadorId() != null && !jugador.getId().equals(request.getJugadorId())) {
            throw new AccessDeniedException("No puedes crear una reserva para otro jugador");
        }
        Cancha cancha = canchaRepository.findById(request.getCanchaId())
                .orElseThrow(() -> new NotFoundException("Cancha no encontrada"));

        if (!request.getFin().isAfter(request.getInicio())) {
            throw new BadRequestException("La hora de fin debe ser posterior a la de inicio");
        }

        // Validar que la reserva cae dentro de los horarios habilitados
        java.time.LocalTime inicioTime = request.getInicio().toLocalTime();
        java.time.LocalTime finTime = request.getFin().toLocalTime();
        boolean permitido;
        if (cancha.getHorarios() != null && !cancha.getHorarios().isEmpty()) {
            permitido = cancha.getHorarios().stream().anyMatch(h ->
                    !inicioTime.isBefore(h.getInicio()) && !finTime.isAfter(h.getFin()));
        } else {
            // Fallback a ventana unica de apertura/cierre si no hay intervalos cargados
            permitido = (cancha.getHorarioApertura() == null || !inicioTime.isBefore(cancha.getHorarioApertura()))
                    && (cancha.getHorarioCierre() == null || !finTime.isAfter(cancha.getHorarioCierre()));
        }
        if (!permitido) {
            throw new BadRequestException("La reserva no esta dentro de los horarios disponibles de la cancha");
        }

        List<Reserva> existentes = reservaRepository.findOverlapping(cancha, request.getInicio(), request.getFin());
        if (!existentes.isEmpty()) {
            throw new BadRequestException("La cancha ya está reservada para el horario solicitado");
        }

        Reserva reserva = new Reserva();
        reserva.setJugador(jugador);
        reserva.setCancha(cancha);
        reserva.setInicio(request.getInicio());
        reserva.setFin(request.getFin());
        reserva.setEstado(Reserva.EstadoReserva.PENDIENTE);
        
        // Calcular monto basado en horas y precio de la cancha
        long horas = java.time.Duration.between(request.getInicio(), request.getFin()).toHours();
        if (horas == 0) horas = 1; // Mínimo 1 hora
        double montoTotal = cancha.getPrecioHora().doubleValue() * horas;
        reserva.setMonto(montoTotal);
        
        Reserva guardada = reservaRepository.save(reserva);
        return toResponse(guardada);
    }

    @Transactional(readOnly = true)
    public ReservaResponse obtenerReserva(Long id) {
        UserPrincipal principal = getAuthenticatedPrincipal();
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Reserva no encontrada"));

        // Validar que el usuario solo pueda obtener sus propias reservas
        if (!reserva.getJugador().getUsuario().getId().equals(principal.getUsuarioId())) {
            throw new AccessDeniedException("No tienes permiso para acceder a esta reserva");
        }

        return toResponse(reserva);
    }

    @Transactional(readOnly = true)
    public List<ReservaResponse> listarMisReservas(String estado, LocalDate fechaDesde, LocalDate fechaHasta, 
                                                     Long canchaId, String ordenarPor, String direccion) {
        UserPrincipal principal = getAuthenticatedPrincipal();
        if (principal.getRole() != Role.JUGADOR) {
            throw new AccessDeniedException("Solo los jugadores pueden listar sus reservas");
        }
        Jugador jugador = jugadorRepository.findByUsuarioId(principal.getUsuarioId())
                .orElseThrow(() -> new AccessDeniedException("No se encontró un jugador asociado al usuario autenticado"));

        Stream<Reserva> reservasStream = reservaRepository.findByJugador(jugador).stream();
        
        // Aplicar filtros
        reservasStream = aplicarFiltros(reservasStream, estado, fechaDesde, fechaHasta, canchaId, null);
        
        // Convertir a response
        List<ReservaResponse> reservas = reservasStream.map(this::toResponse).toList();
        
        // Aplicar ordenamiento
        return aplicarOrdenamiento(reservas, ordenarPor, direccion);
    }

    @Transactional(readOnly = true)
    public List<ReservaResponse> listarReservasPorCancha(Long canchaId, String estado, LocalDate fechaDesde, 
                                                          LocalDate fechaHasta, String cliente, String ordenarPor, String direccion) {
        Cancha cancha = canchaRepository.findById(canchaId)
                .orElseThrow(() -> new NotFoundException("Cancha no encontrada"));

        Stream<Reserva> reservasStream = reservaRepository.findByCancha(cancha).stream();
        
        // Aplicar filtros
        reservasStream = aplicarFiltros(reservasStream, estado, fechaDesde, fechaHasta, null, cliente);
        
        // Convertir a response
        List<ReservaResponse> reservas = reservasStream.map(this::toResponse).toList();
        
        // Aplicar ordenamiento
        return aplicarOrdenamiento(reservas, ordenarPor, direccion);
    }

    @Transactional(readOnly = true)
    public List<ReservaResponse> listarReservasDelAdministrador(String estado, LocalDate fechaDesde, 
                                                                 LocalDate fechaHasta, Long canchaId, String cliente,
                                                                 String ordenarPor, String direccion) {
        UserPrincipal principal = getAuthenticatedPrincipal();
        if (principal.getRole() != Role.ADMINISTRADOR_CANCHA) {
            throw new AccessDeniedException("Solo los administradores de cancha pueden listar reservas de sus canchas");
        }
        
        // Buscar el administrador asociado al usuario autenticado
        AdministradorCancha administrador = administradorCanchaRepository.findByUsuarioId(principal.getUsuarioId())
                .orElseThrow(() -> new NotFoundException("No se encontró un administrador asociado al usuario"));
        
        // Obtener todas las reservas de las canchas del administrador
        Stream<Reserva> reservasStream = reservaRepository.findByAdministradorCanchaId(administrador.getId()).stream();
        
        // Aplicar filtros
        reservasStream = aplicarFiltros(reservasStream, estado, fechaDesde, fechaHasta, canchaId, cliente);
        
        // Convertir a response
        List<ReservaResponse> reservas = reservasStream.map(this::toResponse).toList();
        
        // Aplicar ordenamiento
        return aplicarOrdenamiento(reservas, ordenarPor, direccion);
    }
    
    private Stream<Reserva> aplicarFiltros(Stream<Reserva> stream, String estado, LocalDate fechaDesde, 
                                            LocalDate fechaHasta, Long canchaId, String cliente) {
        // Filtro por estado
        if (estado != null && !estado.isBlank()) {
            String estadoUpper = estado.toUpperCase();
            stream = stream.filter(r -> r.getEstado().name().equals(estadoUpper));
        }
        
        // Filtro por fecha desde
        if (fechaDesde != null) {
            LocalDateTime fechaDesdeTime = fechaDesde.atStartOfDay();
            stream = stream.filter(r -> !r.getInicio().isBefore(fechaDesdeTime));
        }
        
        // Filtro por fecha hasta
        if (fechaHasta != null) {
            LocalDateTime fechaHastaTime = fechaHasta.atTime(LocalTime.MAX);
            stream = stream.filter(r -> !r.getInicio().isAfter(fechaHastaTime));
        }
        
        // Filtro por cancha (para mis reservas)
        if (canchaId != null) {
            stream = stream.filter(r -> r.getCancha().getId().equals(canchaId));
        }
        
        // Filtro por cliente (para reservas de cancha)
        if (cliente != null && !cliente.isBlank()) {
            String clienteLower = cliente.toLowerCase();
            stream = stream.filter(r -> r.getJugador().getNombre().toLowerCase().contains(clienteLower));
        }
        
        return stream;
    }
    
    private List<ReservaResponse> aplicarOrdenamiento(List<ReservaResponse> reservas, String ordenarPor, String direccion) {
        Comparator<ReservaResponse> comparator = switch (ordenarPor.toLowerCase()) {
            case "fecha" -> Comparator.comparing(r -> r.getFecha());
            case "hora", "horainicio" -> Comparator.comparing(r -> r.getHoraInicio());
            case "horafin" -> Comparator.comparing(r -> r.getHoraFin());
            case "estado" -> Comparator.comparing(r -> r.getEstado());
            case "cliente" -> Comparator.comparing(r -> r.getCliente());
            case "monto" -> Comparator.comparing(r -> r.getMonto() != null ? r.getMonto() : 0.0);
            case "cancha", "canchaid" -> Comparator.comparing(r -> r.getCanchaId());
            default -> Comparator.comparing(r -> r.getFecha()); // Por defecto ordenar por fecha
        };
        
        // Aplicar dirección (asc o desc)
        if ("desc".equalsIgnoreCase(direccion)) {
            comparator = comparator.reversed();
        }
        
        return reservas.stream().sorted(comparator).toList();
    }

    @Transactional
    public void cancelarReserva(Long id) {
        UserPrincipal principal = getAuthenticatedPrincipal();
        if (principal.getRole() != Role.JUGADOR) {
            throw new AccessDeniedException("Solo los jugadores pueden cancelar reservas");
        }
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Reserva no encontrada"));

        // Validar que el usuario solo pueda cancelar sus propias reservas
        if (!reserva.getJugador().getUsuario().getId().equals(principal.getUsuarioId())) {
            throw new AccessDeniedException("No tienes permiso para cancelar esta reserva");
        }

        reservaRepository.deleteById(id);
    }

    @Transactional
    public ReservaResponse confirmarReserva(Long id) {
        UserPrincipal principal = getAuthenticatedPrincipal();
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Reserva no encontrada"));

        // Validar que el usuario sea el dueño de la cancha
        if (!reserva.getCancha().getAdministradorCancha().getUsuario().getId().equals(principal.getUsuarioId())) {
            throw new AccessDeniedException("Solo el administrador de la cancha puede confirmar reservas");
        }

        if (reserva.getEstado() == Reserva.EstadoReserva.CONFIRMADA) {
            throw new BadRequestException("La reserva ya está confirmada");
        }

        if (reserva.getEstado() == Reserva.EstadoReserva.CANCELADA) {
            throw new BadRequestException("No se puede confirmar una reserva cancelada");
        }

        reserva.setEstado(Reserva.EstadoReserva.CONFIRMADA);
        Reserva actualizada = reservaRepository.save(reserva);
        return toResponse(actualizada);
    }

    @Transactional
    public ReservaResponse rechazarReserva(Long id) {
        UserPrincipal principal = getAuthenticatedPrincipal();
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Reserva no encontrada"));

        // Validar que el usuario sea el dueño de la cancha
        if (!reserva.getCancha().getAdministradorCancha().getUsuario().getId().equals(principal.getUsuarioId())) {
            throw new AccessDeniedException("Solo el administrador de la cancha puede rechazar reservas");
        }

        if (reserva.getEstado() == Reserva.EstadoReserva.CONFIRMADA) {
            throw new BadRequestException("No se puede rechazar una reserva ya confirmada");
        }

        if (reserva.getEstado() == Reserva.EstadoReserva.CANCELADA) {
            throw new BadRequestException("La reserva ya está cancelada");
        }

        reserva.setEstado(Reserva.EstadoReserva.CANCELADA);
        Reserva actualizada = reservaRepository.save(reserva);
        return toResponse(actualizada);
    }

    private UserPrincipal getAuthenticatedPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new AccessDeniedException("Usuario no autenticado");
        }
        return principal;
    }

    private ReservaResponse toResponse(Reserva reserva) {
        ReservaResponse response = new ReservaResponse();
        response.setId(reserva.getId());
        response.setCanchaId(reserva.getCancha().getId());
        response.setCanchaNombre(reserva.getCancha().getNombre());
        response.setCanchaDeporte(reserva.getCancha().getTipo() != null ? reserva.getCancha().getTipo().name() : null);
        response.setCliente(reserva.getJugador().getNombre());
        response.setEstado(reserva.getEstado().name().toLowerCase());
        response.setFecha(reserva.getInicio().toLocalDate());
        response.setHoraInicio(reserva.getInicio().toLocalTime());
        response.setHoraFin(reserva.getFin().toLocalTime());
        response.setMonto(reserva.getMonto());
        return response;
    }
}
