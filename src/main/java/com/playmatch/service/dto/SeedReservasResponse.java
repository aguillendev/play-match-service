package com.playmatch.service.dto;

import java.util.List;

public class SeedReservasResponse {

    private String mensaje;
    private Integer cantidadReservasCreadas;
    private Integer cantidadJugadoresCreados;
    private List<String> canchasAfectadas;
    private String administradorCancha;

    public SeedReservasResponse() {
    }

    public SeedReservasResponse(String mensaje, Integer cantidadReservasCreadas, 
                                Integer cantidadJugadoresCreados, List<String> canchasAfectadas,
                                String administradorCancha) {
        this.mensaje = mensaje;
        this.cantidadReservasCreadas = cantidadReservasCreadas;
        this.cantidadJugadoresCreados = cantidadJugadoresCreados;
        this.canchasAfectadas = canchasAfectadas;
        this.administradorCancha = administradorCancha;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public Integer getCantidadReservasCreadas() {
        return cantidadReservasCreadas;
    }

    public void setCantidadReservasCreadas(Integer cantidadReservasCreadas) {
        this.cantidadReservasCreadas = cantidadReservasCreadas;
    }

    public Integer getCantidadJugadoresCreados() {
        return cantidadJugadoresCreados;
    }

    public void setCantidadJugadoresCreados(Integer cantidadJugadoresCreados) {
        this.cantidadJugadoresCreados = cantidadJugadoresCreados;
    }

    public List<String> getCanchasAfectadas() {
        return canchasAfectadas;
    }

    public void setCanchasAfectadas(List<String> canchasAfectadas) {
        this.canchasAfectadas = canchasAfectadas;
    }

    public String getAdministradorCancha() {
        return administradorCancha;
    }

    public void setAdministradorCancha(String administradorCancha) {
        this.administradorCancha = administradorCancha;
    }
}
