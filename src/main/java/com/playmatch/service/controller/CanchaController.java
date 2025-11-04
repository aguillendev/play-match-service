package com.playmatch.service.controller;

import com.playmatch.service.dto.CanchaRequest;
import com.playmatch.service.dto.CanchaResponse;
import com.playmatch.service.service.CanchaService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/canchas")
public class CanchaController {

    private final CanchaService canchaService;

    public CanchaController(CanchaService canchaService) {
        this.canchaService = canchaService;
    }

    @GetMapping("/disponibles")
    @Operation(summary = "Buscar canchas disponibles por ubicación")
    public ResponseEntity<List<CanchaResponse>> disponibles(@RequestParam double lat,
                                                            @RequestParam double lng,
                                                            @RequestParam double radioKm) {
        return ResponseEntity.ok(canchaService.buscarDisponibles(lat, lng, radioKm));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR_CANCHA')")
    @Operation(summary = "Crear una cancha para un administrador de cancha")
    public ResponseEntity<CanchaResponse> crear(@Validated @RequestBody CanchaRequest request) {
        return ResponseEntity.ok(canchaService.crearCancha(request));
    }

    @PutMapping("/{id}/horarios")
    @Operation(summary = "Actualizar horarios de una cancha")
    public ResponseEntity<CanchaResponse> actualizarHorarios(@PathVariable("id") Long id,
                                                             @Validated @RequestBody CanchaRequest request) {
        return ResponseEntity.ok(canchaService.actualizarHorarios(id, request));
    }

    @GetMapping
    @Operation(summary = "Listar todas las canchas")
    public ResponseEntity<List<CanchaResponse>> listarTodas() {
        return ResponseEntity.ok(canchaService.listarTodas());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR_CANCHA')")
    @Operation(summary = "Actualizar datos completos de una cancha")
    public ResponseEntity<CanchaResponse> actualizar(@PathVariable("id") Long id,
                                                     @Validated @RequestBody CanchaRequest request) {
        return ResponseEntity.ok(canchaService.actualizarCancha(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR_CANCHA')")
    @Operation(summary = "Eliminar una cancha (si no tiene reservas)")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Long id) {
        canchaService.eliminarCancha(id);
        return ResponseEntity.noContent().build();
    }
}
