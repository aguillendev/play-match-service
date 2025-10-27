package com.playmatch.service.dto;

import java.time.LocalDateTime;
import java.util.Set;

public class PartidoResponse {
    private Long id;
    private String titulo;
    private String descripcion;
    private LocalDateTime fecha;
    private Integer cupo;
    private Long canchaId;
    private Long organizadorId;
    private Set<Long> jugadores;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public Integer getCupo() {
        return cupo;
    }

    public void setCupo(Integer cupo) {
        this.cupo = cupo;
    }

    public Long getCanchaId() {
        return canchaId;
    }

    public void setCanchaId(Long canchaId) {
        this.canchaId = canchaId;
    }

    public Long getOrganizadorId() {
        return organizadorId;
    }

    public void setOrganizadorId(Long organizadorId) {
        this.organizadorId = organizadorId;
    }

    public Set<Long> getJugadores() {
        return jugadores;
    }

    public void setJugadores(Set<Long> jugadores) {
        this.jugadores = jugadores;
    }
}
