package com.playmatch.service.dto;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

import com.playmatch.service.entity.Deporte;

public class CanchaResponse {
    private Long id;
    private String nombre;
    private String direccion;
    private double latitud;
    private double longitud;
    private BigDecimal precioHora;
    private LocalTime horarioApertura;
    private LocalTime horarioCierre;
    private Deporte tipo;
    private List<HorarioIntervalDTO> horarios;
    private boolean tieneReservasFuturas;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
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

    public boolean isTieneReservasFuturas() {
        return tieneReservasFuturas;
    }

    public void setTieneReservasFuturas(boolean tieneReservasFuturas) {
        this.tieneReservasFuturas = tieneReservasFuturas;
    }
}
