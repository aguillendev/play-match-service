package com.playmatch.service.service;

import com.playmatch.service.dto.AuthResponse;
import com.playmatch.service.dto.LoginRequest;
import com.playmatch.service.dto.RegisterRequest;
import com.playmatch.service.entity.AdministradorCancha;
import com.playmatch.service.entity.Jugador;
import com.playmatch.service.entity.Role;
import com.playmatch.service.entity.Usuario;
import com.playmatch.service.exception.BadRequestException;
import com.playmatch.service.repository.AdministradorCanchaRepository;
import com.playmatch.service.repository.JugadorRepository;
import com.playmatch.service.repository.UsuarioRepository;
import com.playmatch.service.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final JugadorRepository jugadorRepository;
    private final AdministradorCanchaRepository administradorCanchaRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UsuarioRepository usuarioRepository,
                       JugadorRepository jugadorRepository,
                       AdministradorCanchaRepository administradorCanchaRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider jwtTokenProvider) {
        this.usuarioRepository = usuarioRepository;
        this.jugadorRepository = jugadorRepository;
        this.administradorCanchaRepository = administradorCanchaRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("El email ya está registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setEmail(request.getEmail());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setRole(request.getRole());
        usuarioRepository.save(usuario);

        if (request.getRole() == Role.JUGADOR) {
            Jugador jugador = new Jugador();
            jugador.setNombre(request.getNombre());
            jugador.setTelefono(request.getTelefono());
            jugador.setUsuario(usuario);
            usuario.setJugador(jugador);
            jugadorRepository.save(jugador);
        } else if (request.getRole() == Role.ADMINISTRADOR_CANCHA) {
            AdministradorCancha administradorCancha = new AdministradorCancha();
            administradorCancha.setNombre(request.getNombre());
            administradorCancha.setTelefono(request.getTelefono());
            administradorCancha.setUsuario(usuario);
            usuario.setAdministradorCancha(administradorCancha);
            administradorCanchaRepository.save(administradorCancha);
        } else {
            throw new BadRequestException("Rol no soportado");
        }
        String token = jwtTokenProvider.generateToken(usuario.getEmail(), usuario.getRole());
        return new AuthResponse(token, usuario.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        String email = authentication.getName();
        Role role = usuarioRepository.findByEmail(email)
                .map(Usuario::getRole)
                .orElseThrow(() -> new BadRequestException("Usuario no encontrado"));
        String token = jwtTokenProvider.generateToken(email, role);
        return new AuthResponse(token, role.name());
    }
}
