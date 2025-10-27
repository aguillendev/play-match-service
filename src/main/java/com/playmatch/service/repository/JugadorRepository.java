package com.playmatch.service.repository;

import com.playmatch.service.entity.Jugador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JugadorRepository extends JpaRepository<Jugador, Long> {
    Optional<Jugador> findByUsuarioId(Long usuarioId);
}
