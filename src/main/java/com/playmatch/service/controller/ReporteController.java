package com.playmatch.service.controller;

import com.playmatch.service.dto.ReporteReservasResponse;
import com.playmatch.service.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/reservas")
    @Operation(summary = "Reporte de reservas por periodo")
    public ResponseEntity<List<ReporteReservasResponse>> reservas(
            @RequestParam(name = "administradorCanchaId", required = false) Long administradorCanchaId,
            @RequestParam(name = "canchaId", required = false) Long canchaId,
            @RequestParam(name = "periodo", defaultValue = "mes") String periodo) {
        // Soporta ambos parámetros: administradorCanchaId (todas las canchas del administrador) o canchaId (una cancha específica)
        if (canchaId != null) {
            return ResponseEntity.ok(reporteService.reporteReservasPorCancha(canchaId, periodo));
        } else if (administradorCanchaId != null) {
            return ResponseEntity.ok(reporteService.reporteReservas(administradorCanchaId, periodo));
        } else {
            throw new IllegalArgumentException("Debe proporcionar administradorCanchaId o canchaId");
        }
    }
}
