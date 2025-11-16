package com.playmatch.service.service;

import com.playmatch.service.dto.ReporteReservasResponse;
import com.playmatch.service.entity.AdministradorCancha;
import com.playmatch.service.entity.Cancha;
import com.playmatch.service.entity.Reserva;
import com.playmatch.service.exception.NotFoundException;
import com.playmatch.service.repository.AdministradorCanchaRepository;
import com.playmatch.service.repository.CanchaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReporteService {

    private final AdministradorCanchaRepository administradorCanchaRepository;
    private final CanchaRepository canchaRepository;

    public ReporteService(AdministradorCanchaRepository administradorCanchaRepository, CanchaRepository canchaRepository) {
        this.administradorCanchaRepository = administradorCanchaRepository;
        this.canchaRepository = canchaRepository;
    }

    @Transactional(readOnly = true)
    public List<ReporteReservasResponse> reporteReservas(Long administradorCanchaId, String periodo) {
        AdministradorCancha administradorCancha = administradorCanchaRepository.findById(administradorCanchaId)
                .orElseThrow(() -> new NotFoundException("Administrador de cancha no encontrado"));

        LocalDateTime ahora = LocalDateTime.now();

        // Para el período "día", agrupar por hora mostrando todas las reservas del día actual
        if ("dia".equalsIgnoreCase(periodo)) {
            LocalDate hoy = LocalDate.now();
            
            return administradorCancha.getCanchas().stream()
                    .flatMap(cancha -> cancha.getReservas().stream())
                    .filter(reserva -> reserva.getInicio().toLocalDate().equals(hoy))
                    .map(reserva -> {
                        // Solo contar recaudación si está confirmada Y ya finalizó
                        double monto = esReservaRecaudada(reserva, ahora) ? 
                                (reserva.getMonto() != null ? reserva.getMonto() : 0.0) : 0.0;
                        return new ReporteReservasResponse(
                                reserva.getInicio().toLocalDate(),
                                reserva.getInicio().toLocalTime(),
                                reserva.getFin().toLocalTime(),
                                1,
                                monto
                        );
                    })
                    .sorted((a, b) -> {
                        int fechaCompare = a.getFecha().compareTo(b.getFecha());
                        if (fechaCompare != 0) return fechaCompare;
                        return a.getHoraInicio().compareTo(b.getHoraInicio());
                    })
                    .collect(Collectors.toList());
        }

        LocalDate fechaInicio = calcularFechaInicio(periodo);
        
        Map<LocalDate, List<Reserva>> reservasPorDia = administradorCancha.getCanchas().stream()
                .flatMap(cancha -> cancha.getReservas().stream())
                .filter(reserva -> !reserva.getInicio().toLocalDate().isBefore(fechaInicio))
                .collect(Collectors.groupingBy(reserva -> reserva.getInicio().toLocalDate()));

        return reservasPorDia.entrySet().stream()
                .map(entry -> {
                    // Solo contar recaudación de reservas confirmadas que ya finalizaron
                    double totalRecaudado = entry.getValue().stream()
                            .filter(r -> esReservaRecaudada(r, ahora))
                            .mapToDouble(r -> r.getMonto() != null ? r.getMonto() : 0.0)
                            .sum();
                    
                    return new ReporteReservasResponse(
                            entry.getKey(),
                            null,
                            null,
                            entry.getValue().size(),
                            totalRecaudado
                    );
                })
                .sorted((a, b) -> a.getFecha().compareTo(b.getFecha()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReporteReservasResponse> reporteReservasPorCancha(Long canchaId, String periodo) {
        Cancha cancha = canchaRepository.findById(canchaId)
                .orElseThrow(() -> new NotFoundException("Cancha no encontrada"));

        LocalDateTime ahora = LocalDateTime.now();

        // Para el período "día", agrupar por hora mostrando todas las reservas del día actual
        if ("dia".equalsIgnoreCase(periodo)) {
            LocalDate hoy = LocalDate.now();
            
            return cancha.getReservas().stream()
                    .filter(reserva -> reserva.getInicio().toLocalDate().equals(hoy))
                    .map(reserva -> {
                        // Solo contar recaudación si está confirmada Y ya finalizó
                        double monto = esReservaRecaudada(reserva, ahora) ? 
                                (reserva.getMonto() != null ? reserva.getMonto() : 0.0) : 0.0;
                        return new ReporteReservasResponse(
                                reserva.getInicio().toLocalDate(),
                                reserva.getInicio().toLocalTime(),
                                reserva.getFin().toLocalTime(),
                                1,
                                monto
                        );
                    })
                    .sorted((a, b) -> {
                        int fechaCompare = a.getFecha().compareTo(b.getFecha());
                        if (fechaCompare != 0) return fechaCompare;
                        return a.getHoraInicio().compareTo(b.getHoraInicio());
                    })
                    .collect(Collectors.toList());
        }

        // Para semana y mes, agrupar por fecha como antes
        LocalDate fechaInicio = calcularFechaInicio(periodo);

        Map<LocalDate, List<Reserva>> reservasPorDia = cancha.getReservas().stream()
                .filter(reserva -> !reserva.getInicio().toLocalDate().isBefore(fechaInicio))
                .collect(Collectors.groupingBy(reserva -> reserva.getInicio().toLocalDate()));

        return reservasPorDia.entrySet().stream()
                .map(entry -> {
                    // Solo contar recaudación de reservas confirmadas que ya finalizaron
                    double totalRecaudado = entry.getValue().stream()
                            .filter(r -> esReservaRecaudada(r, ahora))
                            .mapToDouble(r -> r.getMonto() != null ? r.getMonto() : 0.0)
                            .sum();
                    
                    return new ReporteReservasResponse(
                            entry.getKey(),
                            null,
                            null,
                            entry.getValue().size(),
                            totalRecaudado
                    );
                })
                .sorted((a, b) -> a.getFecha().compareTo(b.getFecha()))
                .collect(Collectors.toList());
    }

    /**
     * Verifica si una reserva debe contarse como recaudación.
     * Requisitos:
     * 1. Estado CONFIRMADA
     * 2. Fecha/hora de fin ya pasó (partido finalizado y pagado)
     */
    private boolean esReservaRecaudada(Reserva reserva, LocalDateTime ahora) {
        return reserva.getEstado() == Reserva.EstadoReserva.CONFIRMADA 
                && reserva.getFin().isBefore(ahora);
    }

    private LocalDate calcularFechaInicio(String periodo) {
        LocalDate hoy = LocalDate.now();
        
        switch (periodo.toLowerCase()) {
            case "dia":
                return hoy; // Solo el día actual
            case "semana":
                return hoy.minus(7, ChronoUnit.DAYS); // Últimos 7 días
            case "mes":
                return hoy.minus(30, ChronoUnit.DAYS); // Últimos 30 días
            default:
                return hoy.minus(30, ChronoUnit.DAYS);
        }
    }
}
