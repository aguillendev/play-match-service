package com.playmatch.service.controller;

import com.playmatch.service.dto.SeedReservasRequest;
import com.playmatch.service.dto.SeedReservasResponse;
import com.playmatch.service.service.SeedDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/seed")
@Tag(name = "Seed Data", description = "Endpoints para generar datos de prueba")
public class SeedDataController {

    private final SeedDataService seedDataService;

    public SeedDataController(SeedDataService seedDataService) {
        this.seedDataService = seedDataService;
    }

    @PostMapping("/reservas")
    @Operation(summary = "Generar reservas aleatorias para un administrador de cancha",
               description = "Crea reservas con jugadores aleatorios para todas las canchas " +
                            "de un administrador específico. Los jugadores se crean automáticamente si no existen.")
    public ResponseEntity<SeedReservasResponse> generarReservas(
            @Validated @RequestBody SeedReservasRequest request) {
        SeedReservasResponse response = seedDataService.generarReservasParaAdministrador(request);
        return ResponseEntity.ok(response);
    }
}
