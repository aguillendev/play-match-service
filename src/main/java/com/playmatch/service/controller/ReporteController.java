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
    public ResponseEntity<List<ReporteReservasResponse>> reservas(@RequestParam Long duenoId,
                                                                  @RequestParam(defaultValue = "mes") String periodo) {
        // Por ahora solo soportamos el periodo mensual y devolvemos el acumulado diario
        return ResponseEntity.ok(reporteService.reporteReservas(duenoId));
    }
}
