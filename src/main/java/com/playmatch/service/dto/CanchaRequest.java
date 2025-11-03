package com.playmatch.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

import com.playmatch.service.entity.Deporte;

public class CanchaRequest {

    @NotBlank
    private String nombre;

    @NotBlank
    private String direccion;

    @NotNull
    private Double latitud;

    @NotNull
    private Double longitud;

    @NotNull
    private BigDecimal precioHora;

    private LocalTime horarioApertura;
    private LocalTime horarioCierre;

    @NotNull
    private Deporte tipo;

    private List<HorarioIntervalDTO> horarios;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public Double getLatitud() {
        return latitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }

    public BigDecimal getPrecioHora() {
        return precioHora;
    }

    public void setPrecioHora(BigDecimal precioHora) {
        this.precioHora = precioHora;
    }

    public LocalTime getHorarioApertura() {
        return horarioApertura;
    }

    public void setHorarioApertura(LocalTime horarioApertura) {
        this.horarioApertura = horarioApertura;
    }

    public LocalTime getHorarioCierre() {
        return horarioCierre;
    }

    public void setHorarioCierre(LocalTime horarioCierre) {
        this.horarioCierre = horarioCierre;
    }

    public Deporte getTipo() {
        return tipo;
    }

    public void setTipo(Deporte tipo) {
        this.tipo = tipo;
    }

    public List<HorarioIntervalDTO> getHorarios() {
        return horarios;
    }

    public void setHorarios(List<HorarioIntervalDTO> horarios) {
        this.horarios = horarios;
    }
}
