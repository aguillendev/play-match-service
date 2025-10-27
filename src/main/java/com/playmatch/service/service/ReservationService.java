package com.playmatch.service.service;

import com.playmatch.service.dto.ReservaRequest;
import com.playmatch.service.dto.ReservaResponse;
import com.playmatch.service.entity.Cancha;
import com.playmatch.service.entity.Jugador;
import com.playmatch.service.entity.Reserva;
import com.playmatch.service.exception.BadRequestException;
import com.playmatch.service.exception.NotFoundException;
import com.playmatch.service.repository.CanchaRepository;
import com.playmatch.service.repository.JugadorRepository;
import com.playmatch.service.repository.ReservaRepository;
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
        Jugador jugador = jugadorRepository.findById(request.getJugadorId())
                .orElseThrow(() -> new NotFoundException("Jugador no encontrado"));
        Cancha cancha = canchaRepository.findById(request.getCanchaId())
                .orElseThrow(() -> new NotFoundException("Cancha no encontrada"));

        if (!request.getFin().isAfter(request.getInicio())) {
            throw new BadRequestException("La hora de fin debe ser posterior a la de inicio");
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
}
