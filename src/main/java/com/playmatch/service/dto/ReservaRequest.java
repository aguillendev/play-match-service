package com.playmatch.service.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class ReservaRequest {

    @NotNull
    private Long canchaId;

    private Long jugadorId;

    @NotNull
    private LocalDateTime inicio;

    @NotNull
    private LocalDateTime fin;

    public Long getCanchaId() {
        return canchaId;
    }

    public void setCanchaId(Long canchaId) {
        this.canchaId = canchaId;
    }

    public Long getJugadorId() {
        return jugadorId;
    }

    public void setJugadorId(Long jugadorId) {
        this.jugadorId = jugadorId;
    }

    public LocalDateTime getInicio() {
        return inicio;
    }

    public void setInicio(LocalDateTime inicio) {
        this.inicio = inicio;
    }

    public LocalDateTime getFin() {
        return fin;
    }

    public void setFin(LocalDateTime fin) {
        this.fin = fin;
    }
}
