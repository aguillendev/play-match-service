package com.playmatch.service.dto;

import java.time.LocalTime;

public class HorarioIntervalDTO {
    private LocalTime inicio;
    private LocalTime fin;

    public LocalTime getInicio() {
        return inicio;
    }

    public void setInicio(LocalTime inicio) {
        this.inicio = inicio;
    }

    public LocalTime getFin() {
        return fin;
    }

    public void setFin(LocalTime fin) {
        this.fin = fin;
    }
}

