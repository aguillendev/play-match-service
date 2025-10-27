package com.playmatch.service.service;

import com.playmatch.service.dto.PartidoRequest;
import com.playmatch.service.dto.PartidoResponse;
import com.playmatch.service.entity.Cancha;
import com.playmatch.service.entity.Jugador;
import com.playmatch.service.entity.Partido;
import com.playmatch.service.exception.BadRequestException;
import com.playmatch.service.exception.NotFoundException;
import com.playmatch.service.repository.CanchaRepository;
import com.playmatch.service.repository.JugadorRepository;
import com.playmatch.service.repository.PartidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PartidoService {

    private final PartidoRepository partidoRepository;
    private final JugadorRepository jugadorRepository;
    private final CanchaRepository canchaRepository;

    public PartidoService(PartidoRepository partidoRepository,
                          JugadorRepository jugadorRepository,
                          CanchaRepository canchaRepository) {
        this.partidoRepository = partidoRepository;
        this.jugadorRepository = jugadorRepository;
        this.canchaRepository = canchaRepository;
    }

    @Transactional
    public PartidoResponse publicarPartido(PartidoRequest request) {
        Jugador organizador = jugadorRepository.findById(request.getOrganizadorId())
                .orElseThrow(() -> new NotFoundException("Organizador no encontrado"));
        Cancha cancha = canchaRepository.findById(request.getCanchaId())
                .orElseThrow(() -> new NotFoundException("Cancha no encontrada"));

        Partido partido = new Partido();
        partido.setTitulo(request.getTitulo());
        partido.setDescripcion(request.getDescripcion());
        partido.setOrganizador(organizador);
        partido.setCancha(cancha);
        partido.setFecha(request.getFecha());
        partido.setCupo(request.getCupo());
        partido.setPublicado(true);
        Partido guardado = partidoRepository.save(partido);
        return toResponse(guardado);
    }

    @Transactional(readOnly = true)
    public List<PartidoResponse> buscarPartidosAbiertos() {
        return partidoRepository.findByPublicadoTrue().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PartidoResponse unirseAPartido(Long partidoId, Long jugadorId) {
        Partido partido = partidoRepository.findById(partidoId)
                .orElseThrow(() -> new NotFoundException("Partido no encontrado"));
        Jugador jugador = jugadorRepository.findById(jugadorId)
                .orElseThrow(() -> new NotFoundException("Jugador no encontrado"));

        if (!partido.isPublicado()) {
            throw new BadRequestException("El partido no está abierto");
        }
        if (partido.getJugadores().size() >= partido.getCupo()) {
            throw new BadRequestException("El partido alcanzó el cupo máximo");
        }
        if (partido.getJugadores().contains(jugador)) {
            throw new BadRequestException("El jugador ya está inscrito en el partido");
        }
        partido.getJugadores().add(jugador);
        Partido actualizado = partidoRepository.save(partido);
        return toResponse(actualizado);
    }

    private PartidoResponse toResponse(Partido partido) {
        PartidoResponse response = new PartidoResponse();
        response.setId(partido.getId());
        response.setTitulo(partido.getTitulo());
        response.setDescripcion(partido.getDescripcion());
        response.setFecha(partido.getFecha());
        response.setCupo(partido.getCupo());
        response.setCanchaId(partido.getCancha().getId());
        response.setOrganizadorId(partido.getOrganizador().getId());
        Set<Long> jugadores = partido.getJugadores().stream()
                .map(Jugador::getId)
                .collect(Collectors.toSet());
        response.setJugadores(jugadores);
        return response;
    }
}
