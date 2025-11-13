package com.playmatch.service.controller;

import com.playmatch.service.dto.ReporteReservasResponse;
import com.playmatch.service.entity.Usuario;
import com.playmatch.service.repository.UsuarioRepository;
import com.playmatch.service.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteService reporteService;
    private final UsuarioRepository usuarioRepository;

    public ReporteController(ReporteService reporteService, UsuarioRepository usuarioRepository) {
        this.reporteService = reporteService;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/reservas")
    @Operation(summary = "Reporte de reservas por periodo")
    public ResponseEntity<List<ReporteReservasResponse>> reservas(
            Authentication authentication,
            @RequestParam(name = "administradorCanchaId", required = false) Long administradorCanchaId,
            @RequestParam(name = "canchaId", required = false) Long canchaId,
            @RequestParam(name = "periodo", defaultValue = "mes") String periodo) {
        
        // Si se proporciona canchaId, obtener reporte de esa cancha específica
        if (canchaId != null) {
            return ResponseEntity.ok(reporteService.reporteReservasPorCancha(canchaId, periodo));
        }
        
        // Si se proporciona administradorCanchaId, usarlo
        Long adminId = administradorCanchaId;
        
        // Si no se proporciona administradorCanchaId, obtenerlo del usuario autenticado
        if (adminId == null && authentication != null) {
            String email = authentication.getName();
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
            adminId = usuario.getId();
        }
        
        if (adminId != null) {
            return ResponseEntity.ok(reporteService.reporteReservas(adminId, periodo));
        } else {
            throw new IllegalArgumentException("No se pudo determinar el administrador");
        }
    }
}
