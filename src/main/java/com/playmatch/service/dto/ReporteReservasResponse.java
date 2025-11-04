package com.playmatch.service.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class ReporteReservasResponse {
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private long totalReservas;
    private long totalJugadores;
    private double recaudacion;

    public ReporteReservasResponse(LocalDate fecha, long totalReservas, long totalJugadores) {
        this.fecha = fecha;
        this.totalReservas = totalReservas;
        this.totalJugadores = totalJugadores;
        this.recaudacion = 0.0;
    }

    public ReporteReservasResponse(LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, long totalReservas, double recaudacion) {
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.totalReservas = totalReservas;
        this.recaudacion = recaudacion;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public long getTotalReservas() {
        return totalReservas;
    }

    public long getTotalJugadores() {
        return totalJugadores;
    }

    public double getRecaudacion() {
        return recaudacion;
    }
}
