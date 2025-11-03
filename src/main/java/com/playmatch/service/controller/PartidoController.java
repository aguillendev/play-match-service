package com.playmatch.service.controller;

import com.playmatch.service.dto.PartidoRequest;
import com.playmatch.service.dto.PartidoResponse;
import com.playmatch.service.service.PartidoService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/partidos")
public class PartidoController {

    private final PartidoService partidoService;

    public PartidoController(PartidoService partidoService) {
        this.partidoService = partidoService;
    }

    @PostMapping("/publicar")
    @Operation(summary = "Publicar un partido")
    public ResponseEntity<PartidoResponse> publicar(@Validated @RequestBody PartidoRequest request) {
        return ResponseEntity.ok(partidoService.publicarPartido(request));
    }

    @GetMapping("/abiertos")
    @Operation(summary = "Listar partidos abiertos")
    public ResponseEntity<List<PartidoResponse>> abiertos(@RequestParam double lat,
                                                           @RequestParam double lng) {
        // Por ahora retornamos todos los partidos abiertos sin filtrar por ubicación
        return ResponseEntity.ok(partidoService.buscarPartidosAbiertos());
    }

    @PostMapping("/{id}/unirse")
    @Operation(summary = "Unirse a un partido")
    public ResponseEntity<PartidoResponse> unirse(@PathVariable("id") Long id, @RequestParam Long jugadorId) {
        return ResponseEntity.ok(partidoService.unirseAPartido(id, jugadorId));
    }
}
