package com.playmatch.service.service;

import com.playmatch.service.dto.CanchaRequest;
import com.playmatch.service.dto.CanchaResponse;
import com.playmatch.service.dto.HorarioIntervalDTO;
import com.playmatch.service.entity.AdministradorCancha;
import com.playmatch.service.entity.Cancha;
import com.playmatch.service.entity.CanchaHorario;
import com.playmatch.service.entity.Role;
import com.playmatch.service.exception.NotFoundException;
import com.playmatch.service.repository.AdministradorCanchaRepository;
import com.playmatch.service.repository.CanchaRepository;
import com.playmatch.service.repository.ReservaRepository;
import com.playmatch.service.security.UserPrincipal;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CanchaService {

    private final CanchaRepository canchaRepository;
    private final AdministradorCanchaRepository administradorCanchaRepository;
    private final ReservaRepository reservaRepository;

    public CanchaService(CanchaRepository canchaRepository, AdministradorCanchaRepository administradorCanchaRepository, ReservaRepository reservaRepository) {
        this.canchaRepository = canchaRepository;
        this.administradorCanchaRepository = administradorCanchaRepository;
        this.reservaRepository = reservaRepository;
    }

    @Transactional
    public CanchaResponse crearCancha(CanchaRequest request) {
        UserPrincipal principal = getAuthenticatedPrincipal();
        if (principal.getRole() != Role.ADMINISTRADOR_CANCHA) {
            throw new AccessDeniedException("Solo un usuario con rol administrador de cancha puede crear canchas");
        }
        AdministradorCancha administradorCancha = administradorCanchaRepository.findByUsuarioId(principal.getUsuarioId())
                .orElseThrow(() -> new AccessDeniedException("No se encontró un administrador de cancha asociado al usuario autenticado"));
        Cancha cancha = new Cancha();
        cancha.setNombre(request.getNombre());
        cancha.setDireccion(request.getDireccion());
        cancha.setLatitud(request.getLatitud());
        cancha.setLongitud(request.getLongitud());
        cancha.setPrecioHora(request.getPrecioHora());
        cancha.setHorarioApertura(request.getHorarioApertura());
        cancha.setHorarioCierre(request.getHorarioCierre());
        cancha.setTipo(request.getTipo());
        applyHorarios(cancha, request.getHorarios());
        cancha.setAdministradorCancha(administradorCancha);
        Cancha guardada = canchaRepository.save(cancha);
        return toResponse(guardada);
    }

    private UserPrincipal getAuthenticatedPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new AccessDeniedException("Usuario no autenticado");
        }
        return principal;
    }

    @Transactional(readOnly = true)
    public List<CanchaResponse> buscarDisponibles(double latitud, double longitud, double radioKm) {
        return canchaRepository.findAll().stream()
                .filter(cancha -> distanciaKm(latitud, longitud, cancha.getLatitud(), cancha.getLongitud()) <= radioKm)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CanchaResponse actualizarHorarios(Long canchaId, CanchaRequest request) {
        Cancha cancha = canchaRepository.findById(canchaId)
                .orElseThrow(() -> new NotFoundException("Cancha no encontrada"));
        UserPrincipal principal = getAuthenticatedPrincipal();
        if (principal.getRole() != Role.ADMINISTRADOR_CANCHA) {
            throw new AccessDeniedException("Solo un usuario con rol administrador de cancha puede actualizar canchas");
        }
        AdministradorCancha administradorCancha = administradorCanchaRepository.findByUsuarioId(principal.getUsuarioId())
                .orElseThrow(() -> new AccessDeniedException("No se encontró un administrador de cancha asociado al usuario autenticado"));
        if (!cancha.getAdministradorCancha().getId().equals(administradorCancha.getId())) {
            throw new AccessDeniedException("No puedes modificar canchas que pertenecen a otro administrador");
        }
        cancha.setHorarioApertura(request.getHorarioApertura());
        cancha.setHorarioCierre(request.getHorarioCierre());
        applyHorarios(cancha, request.getHorarios());
        return toResponse(canchaRepository.save(cancha));
    }

    @Transactional(readOnly = true)
    public List<CanchaResponse> listarTodas() {
        return canchaRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CanchaResponse actualizarCancha(Long canchaId, CanchaRequest request) {
        Cancha cancha = canchaRepository.findById(canchaId)
                .orElseThrow(() -> new NotFoundException("Cancha no encontrada"));
        UserPrincipal principal = getAuthenticatedPrincipal();
        if (principal.getRole() != Role.ADMINISTRADOR_CANCHA) {
            throw new AccessDeniedException("Solo un usuario con rol administrador de cancha puede actualizar canchas");
        }
        AdministradorCancha administradorCancha = administradorCanchaRepository.findByUsuarioId(principal.getUsuarioId())
                .orElseThrow(() -> new AccessDeniedException("No se encontró un administrador de cancha asociado al usuario autenticado"));
        if (!cancha.getAdministradorCancha().getId().equals(administradorCancha.getId())) {
            throw new AccessDeniedException("No puedes modificar canchas que pertenecen a otro administrador");
        }
        cancha.setNombre(request.getNombre());
        cancha.setDireccion(request.getDireccion());
        cancha.setLatitud(request.getLatitud());
        cancha.setLongitud(request.getLongitud());
        cancha.setPrecioHora(request.getPrecioHora());
        cancha.setHorarioApertura(request.getHorarioApertura());
        cancha.setHorarioCierre(request.getHorarioCierre());
        cancha.setTipo(request.getTipo());
        applyHorarios(cancha, request.getHorarios());
        return toResponse(canchaRepository.save(cancha));
    }

    @Transactional
    public void eliminarCancha(Long canchaId) {
        Cancha cancha = canchaRepository.findById(canchaId)
                .orElseThrow(() -> new NotFoundException("Cancha no encontrada"));
        UserPrincipal principal = getAuthenticatedPrincipal();
        if (principal.getRole() != Role.ADMINISTRADOR_CANCHA) {
            throw new AccessDeniedException("Solo un usuario con rol administrador de cancha puede eliminar canchas");
        }
        AdministradorCancha administradorCancha = administradorCanchaRepository.findByUsuarioId(principal.getUsuarioId())
                .orElseThrow(() -> new AccessDeniedException("No se encontró un administrador de cancha asociado al usuario autenticado"));
        if (!cancha.getAdministradorCancha().getId().equals(administradorCancha.getId())) {
            throw new AccessDeniedException("No puedes eliminar canchas que pertenecen a otro administrador");
        }
        if (reservaRepository.existsByCancha(cancha)) {
            throw new AccessDeniedException("No se puede eliminar: la cancha tiene reservas asociadas");
        }
        canchaRepository.delete(cancha);
    }

    private double distanciaKm(double lat1, double lon1, double lat2, double lon2) {
        double radioTierra = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return radioTierra * c;
    }

    private CanchaResponse toResponse(Cancha cancha) {
        CanchaResponse response = new CanchaResponse();
        response.setId(cancha.getId());
        response.setNombre(cancha.getNombre());
        response.setDireccion(cancha.getDireccion());
        response.setLatitud(cancha.getLatitud());
        response.setLongitud(cancha.getLongitud());
        response.setPrecioHora(cancha.getPrecioHora());
        response.setHorarioApertura(cancha.getHorarioApertura());
        response.setHorarioCierre(cancha.getHorarioCierre());
        response.setTipo(cancha.getTipo());
        if (cancha.getHorarios() != null) {
            response.setHorarios(cancha.getHorarios().stream()
                    .sorted((a,b) -> a.getInicio().compareTo(b.getInicio()))
                    .map(h -> {
                HorarioIntervalDTO dto = new HorarioIntervalDTO();
                dto.setInicio(h.getInicio());
                dto.setFin(h.getFin());
                return dto;
            }).collect(Collectors.toList()));
        }
        boolean futuras = reservaRepository.existsByCanchaAndInicioAfter(cancha, LocalDateTime.now());
        response.setTieneReservasFuturas(futuras);
        return response;
    }

    private void applyHorarios(Cancha cancha, List<HorarioIntervalDTO> horarios) {
        Set<CanchaHorario> actuales = cancha.getHorarios();
        actuales.clear();
        if (horarios != null) {
            for (HorarioIntervalDTO dto : horarios) {
                if (dto.getInicio() == null || dto.getFin() == null) continue;
                CanchaHorario h = new CanchaHorario();
                h.setCancha(cancha);
                h.setInicio(dto.getInicio());
                h.setFin(dto.getFin());
                actuales.add(h);
            }
        }
    }
}
