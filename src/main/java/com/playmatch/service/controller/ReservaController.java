package com.playmatch.service.controller;

import com.playmatch.service.dto.ReservaRequest;
import com.playmatch.service.dto.ReservaResponse;
import com.playmatch.service.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

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

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('JUGADOR')")
    @Operation(summary = "Obtener una reserva por ID")
    public ResponseEntity<ReservaResponse> obtener(@PathVariable("id") Long id) {
        return ResponseEntity.ok(reservationService.obtenerReserva(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('JUGADOR')")
    @Operation(summary = "Listar las reservas del usuario autenticado con filtros y ordenamiento")
    public ResponseEntity<List<ReservaResponse>> listarMisReservas(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) Long canchaId,
            @RequestParam(defaultValue = "fecha") String ordenarPor,
            @RequestParam(defaultValue = "desc") String direccion) {
        return ResponseEntity.ok(reservationService.listarMisReservas(
                estado, fechaDesde, fechaHasta, canchaId, ordenarPor, direccion));
    }

    @GetMapping("/canchas/{canchaId}")
    @Operation(summary = "Listar todas las reservas de una cancha específica con filtros y ordenamiento")
    public ResponseEntity<List<ReservaResponse>> listarReservasPorCancha(
            @PathVariable("canchaId") Long canchaId,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) String cliente,
            @RequestParam(defaultValue = "fecha") String ordenarPor,
            @RequestParam(defaultValue = "desc") String direccion) {
        return ResponseEntity.ok(reservationService.listarReservasPorCancha(
                canchaId, estado, fechaDesde, fechaHasta, cliente, ordenarPor, direccion));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('JUGADOR')")
    @Operation(summary = "Cancelar una reserva")
    public ResponseEntity<Void> cancelar(@PathVariable("id") Long id) {
        reservationService.cancelarReserva(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/confirmar")
    @Operation(summary = "Confirmar una reserva (solo dueño de la cancha)")
    public ResponseEntity<ReservaResponse> confirmar(@PathVariable("id") Long id) {
        return ResponseEntity.ok(reservationService.confirmarReserva(id));
    }

    @PostMapping("/{id}/rechazar")
    @Operation(summary = "Rechazar una reserva (solo dueño de la cancha)")
    public ResponseEntity<ReservaResponse> rechazar(@PathVariable("id") Long id) {
        return ResponseEntity.ok(reservationService.rechazarReserva(id));
    }
}
