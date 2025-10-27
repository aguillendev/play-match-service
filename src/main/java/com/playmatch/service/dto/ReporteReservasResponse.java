package com.playmatch.service.dto;

import java.time.LocalDate;

public class ReporteReservasResponse {
    private LocalDate fecha;
    private long totalReservas;
    private long totalJugadores;

    public ReporteReservasResponse(LocalDate fecha, long totalReservas, long totalJugadores) {
        this.fecha = fecha;
        this.totalReservas = totalReservas;
        this.totalJugadores = totalJugadores;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public long getTotalReservas() {
        return totalReservas;
    }

    public long getTotalJugadores() {
        return totalJugadores;
    }
}
