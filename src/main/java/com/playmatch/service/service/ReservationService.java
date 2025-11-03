package com.playmatch.service.service;

import com.playmatch.service.dto.ReservaRequest;
import com.playmatch.service.dto.ReservaResponse;
import com.playmatch.service.entity.Cancha;
import com.playmatch.service.entity.Jugador;
import com.playmatch.service.entity.Role;
import com.playmatch.service.entity.Reserva;
import com.playmatch.service.exception.BadRequestException;
import com.playmatch.service.exception.NotFoundException;
import com.playmatch.service.repository.CanchaRepository;
import com.playmatch.service.repository.JugadorRepository;
import com.playmatch.service.repository.ReservaRepository;
import com.playmatch.service.security.UserPrincipal;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReservationService {

    private final ReservaRepository reservaRepository;
    private final JugadorRepository jugadorRepository;
    private final CanchaRepository canchaRepository;

    public ReservationService(ReservaRepository reservaRepository,
                              JugadorRepository jugadorRepository,
                              CanchaRepository canchaRepository) {
        this.reservaRepository = reservaRepository;
        this.jugadorRepository = jugadorRepository;
        this.canchaRepository = canchaRepository;
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
        Reserva guardada = reservaRepository.save(reserva);
        ReservaResponse response = new ReservaResponse();
        response.setId(guardada.getId());
        response.setJugadorId(jugador.getId());
        response.setCanchaId(cancha.getId());
        response.setInicio(guardada.getInicio());
        response.setFin(guardada.getFin());
        return response;
    }

    private UserPrincipal getAuthenticatedPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new AccessDeniedException("Usuario no autenticado");
        }
        return principal;
    }
}
