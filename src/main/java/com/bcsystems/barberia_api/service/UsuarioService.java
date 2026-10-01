package com.bcsystems.barberia_api.service;

import com.bcsystems.barberia_api.domain.Rol;
import com.bcsystems.barberia_api.domain.Usuario;
import com.bcsystems.barberia_api.dto.UsuarioDTO;
import com.bcsystems.barberia_api.repository.RolRepository;
import com.bcsystems.barberia_api.repository.TokenRepository;
import com.bcsystems.barberia_api.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final RolService rolService;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          RolRepository rolRepository,
                          RolService rolService,
                          TokenRepository tokenRepository,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.rolService = rolService;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UsuarioDTO> findAll() {
        return usuarioRepository.findAll().stream()
                .sorted((a, b) -> a.getNombre().compareToIgnoreCase(b.getNombre()))
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<UsuarioDTO> findById(Integer id) {
        return usuarioRepository.findById(id).map(this::toDTO);
    }

    @Transactional
    public UsuarioDTO save(UsuarioDTO dto) {
        validar(dto, null);
        Rol rol = resolverRol(dto.getIdRol());

        Usuario usuario = new Usuario();
        usuario.setNombre(dto.getNombre().trim());
        usuario.setUsuario(dto.getUsuario().trim());
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setRol(rol);
        usuario.setActivo(dto.getActivo() != null ? dto.getActivo() : true);
        return toDTO(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioDTO update(Integer id, UsuarioDTO dto) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        validar(dto, id);

        boolean estabaActivo = Boolean.TRUE.equals(usuario.getActivo());

        usuario.setNombre(dto.getNombre().trim());
        usuario.setUsuario(dto.getUsuario().trim());
        usuario.setRol(resolverRol(dto.getIdRol()));
        if (dto.getActivo() != null) {
            usuario.setActivo(dto.getActivo());
        }
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            if (dto.getPassword().length() < 4) {
                throw new RuntimeException("La contraseña debe tener al menos 4 caracteres");
            }
            usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        Usuario saved = usuarioRepository.save(usuario);
        if (estabaActivo && !Boolean.TRUE.equals(saved.getActivo())) {
            revocarTokens(saved);
        }
        return toDTO(saved);
    }

    @Transactional
    public void delete(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (usuario.getRol() != null && "ADMINISTRADOR".equalsIgnoreCase(usuario.getRol().getNombre())
                && contarAdministradoresActivos() <= 1) {
            throw new RuntimeException("No se puede eliminar el último usuario administrador");
        }

        revocarTokens(usuario);
        tokenRepository.deleteByUsuarioIdUsuario(usuario.getIdUsuario());
        usuarioRepository.delete(usuario);
    }

    @Transactional
    public UsuarioDTO cambiarPassword(Integer id, String passwordActual, String passwordNuevo) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(passwordActual != null ? passwordActual : "", usuario.getPassword())) {
            throw new RuntimeException("La contraseña actual no es correcta");
        }
        if (passwordNuevo == null || passwordNuevo.length() < 4) {
            throw new RuntimeException("La nueva contraseña debe tener al menos 4 caracteres");
        }

        usuario.setPassword(passwordEncoder.encode(passwordNuevo));
        return toDTO(usuarioRepository.save(usuario));
    }

    private void validar(UsuarioDTO dto, Integer idActual) {
        if (dto.getNombre() == null || dto.getNombre().isBlank()) {
            throw new RuntimeException("El nombre es obligatorio");
        }
        if (dto.getUsuario() == null || dto.getUsuario().isBlank()) {
            throw new RuntimeException("El usuario es obligatorio");
        }
        if (idActual == null && (dto.getPassword() == null || dto.getPassword().isBlank())) {
            throw new RuntimeException("La contraseña es obligatoria");
        }
        if (idActual == null && dto.getPassword() != null && dto.getPassword().length() < 4) {
            throw new RuntimeException("La contraseña debe tener al menos 4 caracteres");
        }

        usuarioRepository.findByUsuarioIgnoreCase(dto.getUsuario().trim()).ifPresent(existing -> {
            if (idActual == null || !existing.getIdUsuario().equals(idActual)) {
                throw new RuntimeException("Ese nombre de usuario ya está en uso");
            }
        });
    }

    private Rol resolverRol(Integer idRol) {
        if (idRol == null) {
            throw new RuntimeException("Debes seleccionar un rol");
        }
        return rolRepository.findById(idRol)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
    }

    private long contarAdministradoresActivos() {
        return usuarioRepository.findAll().stream()
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .filter(u -> u.getRol() != null && "ADMINISTRADOR".equalsIgnoreCase(u.getRol().getNombre()))
                .count();
    }

    private void revocarTokens(Usuario usuario) {
        tokenRepository.findByUsuarioIdUsuarioAndRevokedFalseAndExpiredFalse(usuario.getIdUsuario())
                .forEach(t -> {
                    t.setRevoked(true);
                    t.setExpired(true);
                    tokenRepository.save(t);
                });
    }

    public UsuarioDTO toDTO(Usuario usuario) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setIdUsuario(usuario.getIdUsuario());
        dto.setNombre(usuario.getNombre());
        dto.setUsuario(usuario.getUsuario());
        dto.setRol(usuario.getRol() != null ? rolService.toDTO(usuario.getRol()) : null);
        dto.setIdRol(usuario.getRol() != null ? usuario.getRol().getIdRol() : null);
        dto.setActivo(usuario.getActivo());
        dto.setUltimoAcceso(usuario.getUltimoAcceso());
        dto.setFechaCreacion(usuario.getFechaCreacion());
        if (usuario.getRol() != null && usuario.getRol().getPermisos() != null) {
            dto.setPermisos(usuario.getRol().getPermisos().stream()
                    .filter(p -> Boolean.TRUE.equals(p.getActivo()))
                    .map(p -> p.getClave())
                    .sorted()
                    .toList());
        }
        return dto;
    }

}
