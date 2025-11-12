package com.playmatch.service.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class ReservaResponse {
    private Long id;
    private Long canchaId;
    private String canchaNombre;
    private String canchaDeporte;
    private String cliente;  // Nombre del jugador
    private String estado;   // pendiente, confirmada, cancelada
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private Double monto;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCanchaId() {
        return canchaId;
    }

    public void setCanchaId(Long canchaId) {
        this.canchaId = canchaId;
    }

    public String getCanchaNombre() {
        return canchaNombre;
    }

    public void setCanchaNombre(String canchaNombre) {
        this.canchaNombre = canchaNombre;
    }

    public String getCanchaDeporte() {
        return canchaDeporte;
    }

    public void setCanchaDeporte(String canchaDeporte) {
        this.canchaDeporte = canchaDeporte;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }

    public Double getMonto() {
        return monto;
    }

    public void setMonto(Double monto) {
        this.monto = monto;
    }
}
