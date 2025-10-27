package com.playmatch.service.repository;

import com.playmatch.service.entity.Partido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PartidoRepository extends JpaRepository<Partido, Long> {
    List<Partido> findByPublicadoTrue();
}
