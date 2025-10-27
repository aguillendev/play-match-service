package com.playmatch.service.service;

import com.playmatch.service.dto.ReporteReservasResponse;
import com.playmatch.service.entity.Dueno;
import com.playmatch.service.entity.Reserva;
import com.playmatch.service.exception.NotFoundException;
import com.playmatch.service.repository.DuenoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReporteService {

    private final DuenoRepository duenoRepository;

    public ReporteService(DuenoRepository duenoRepository) {
        this.duenoRepository = duenoRepository;
    }

    @Transactional(readOnly = true)
    public List<ReporteReservasResponse> reporteReservas(Long duenoId) {
        Dueno dueno = duenoRepository.findById(duenoId)
                .orElseThrow(() -> new NotFoundException("Dueño no encontrado"));

        Map<LocalDate, List<Reserva>> reservasPorDia = dueno.getCanchas().stream()
                .flatMap(cancha -> cancha.getReservas().stream())
                .collect(Collectors.groupingBy(reserva -> reserva.getInicio().toLocalDate()));

        return reservasPorDia.entrySet().stream()
                .map(entry -> new ReporteReservasResponse(
                        entry.getKey(),
                        entry.getValue().size(),
                        entry.getValue().stream()
                                .map(reserva -> reserva.getJugador().getId())
                                .distinct()
                                .count()))
                .sorted((a, b) -> b.getFecha().compareTo(a.getFecha()))
                .collect(Collectors.toList());
    }
}
