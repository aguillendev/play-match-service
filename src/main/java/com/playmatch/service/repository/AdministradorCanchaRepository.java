package com.playmatch.service.repository;

import com.playmatch.service.entity.AdministradorCancha;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdministradorCanchaRepository extends JpaRepository<AdministradorCancha, Long> {
    Optional<AdministradorCancha> findByUsuarioId(Long usuarioId);
}
