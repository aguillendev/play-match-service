package com.playmatch.service.repository;

import com.playmatch.service.entity.Cancha;
import com.playmatch.service.entity.Jugador;
import com.playmatch.service.entity.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    @Query("SELECT r FROM Reserva r WHERE r.cancha = :cancha AND r.inicio < :fin AND r.fin > :inicio")
    List<Reserva> findOverlapping(@Param("cancha") Cancha cancha,
                                  @Param("inicio") LocalDateTime inicio,
                                  @Param("fin") LocalDateTime fin);

    boolean existsByCancha(Cancha cancha);

    boolean existsByCanchaAndInicioAfter(Cancha cancha, java.time.LocalDateTime inicio);

    List<Reserva> findByJugador(Jugador jugador);

    List<Reserva> findByCancha(Cancha cancha);
}
