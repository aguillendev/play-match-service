package com.playmatch.service.controller;

import com.playmatch.service.dto.ReservaRequest;
import com.playmatch.service.dto.ReservaResponse;
import com.playmatch.service.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    private final ReservationService reservationService;

    public ReservaController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    @PreAuthorize("hasRole('JUGADOR')")
    @Operation(summary = "Crear una reserva de cancha")
    public ResponseEntity<ReservaResponse> crear(@Validated @RequestBody ReservaRequest request) {
        return ResponseEntity.ok(reservationService.crearReserva(request));
    }
}
