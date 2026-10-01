package com.bcsystems.barberia_api.service;

import com.bcsystems.barberia_api.domain.Permiso;
import com.bcsystems.barberia_api.domain.Rol;
import com.bcsystems.barberia_api.dto.PermisoDTO;
import com.bcsystems.barberia_api.dto.RolDTO;
import com.bcsystems.barberia_api.repository.PermisoRepository;
import com.bcsystems.barberia_api.repository.RolRepository;
import com.bcsystems.barberia_api.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RolService {

    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;
    private final UsuarioRepository usuarioRepository;

    public RolService(RolRepository rolRepository,
                      PermisoRepository permisoRepository,
                      UsuarioRepository usuarioRepository) {
        this.rolRepository = rolRepository;
        this.permisoRepository = permisoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<RolDTO> findAll() {
        return rolRepository.findAll().stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<RolDTO> findActivos() {
        return rolRepository.findByActivoTrueOrderByNombreAsc().stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public RolDTO findById(Integer id) {
        return rolRepository.findById(id).map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
    }

    @Transactional
    public RolDTO save(RolDTO dto) {
        if (dto.getNombre() == null || dto.getNombre().isBlank()) {
            throw new RuntimeException("El nombre del rol es obligatorio");
        }
        rolRepository.findByNombreIgnoreCase(dto.getNombre()).ifPresent(existing -> {
            throw new RuntimeException("Ya existe un rol con ese nombre");
        });

        Rol rol = new Rol();
        rol.setNombre(dto.getNombre().trim().toUpperCase());
        rol.setDescripcion(dto.getDescripcion());
        rol.setEsSistema(false);
        rol.setActivo(dto.getActivo() != null ? dto.getActivo() : true);
        rol.setPermisos(resolverPermisos(dto.getPermisos()));
        return toDTO(rolRepository.save(rol));
    }

    @Transactional
    public RolDTO update(Integer id, RolDTO dto) {
        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

        if (Boolean.TRUE.equals(rol.getEsSistema()) && dto.getNombre() != null
                && !dto.getNombre().trim().equalsIgnoreCase(rol.getNombre())) {
            throw new RuntimeException("No se puede renombrar un rol del sistema");
        }
        if (dto.getNombre() != null && !dto.getNombre().isBlank()
                && !dto.getNombre().equalsIgnoreCase(rol.getNombre())) {
            rolRepository.findByNombreIgnoreCase(dto.getNombre()).ifPresent(existing -> {
                if (!existing.getIdRol().equals(id)) {
                    throw new RuntimeException("Ya existe un rol con ese nombre");
                }
            });
            rol.setNombre(dto.getNombre().trim().toUpperCase());
        }
        if (dto.getDescripcion() != null) {
            rol.setDescripcion(dto.getDescripcion());
        }
        if (dto.getActivo() != null) {
            if (Boolean.FALSE.equals(dto.getActivo()) && Boolean.TRUE.equals(rol.getEsSistema())) {
                throw new RuntimeException("No se puede desactivar un rol del sistema");
            }
            rol.setActivo(dto.getActivo());
        }
        if (dto.getPermisos() != null) {
            rol.setPermisos(resolverPermisos(dto.getPermisos()));
        }
        return toDTO(rolRepository.save(rol));
    }

    @Transactional
    public void delete(Integer id) {
        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
        if (Boolean.TRUE.equals(rol.getEsSistema())) {
            throw new RuntimeException("No se puede eliminar un rol del sistema");
        }
        if (usuarioRepository.findAll().stream().anyMatch(u -> u.getRol() != null && u.getRol().getIdRol().equals(id))) {
            throw new RuntimeException("No se puede eliminar un rol que tiene usuarios asignados");
        }
        rolRepository.delete(rol);
    }

    private List<Permiso> resolverPermisos(List<PermisoDTO> solicitados) {
        if (solicitados == null) {
            return new ArrayList<>();
        }
        Set<Integer> ids = solicitados.stream()
                .map(PermisoDTO::getIdPermiso)
                .filter(id -> id != null)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return permisoRepository.findAllById(ids).stream()
                .filter(p -> Boolean.TRUE.equals(p.getActivo()))
                .toList();
    }

    public RolDTO toDTO(Rol rol) {
        RolDTO dto = new RolDTO();
        dto.setIdRol(rol.getIdRol());
        dto.setNombre(rol.getNombre());
        dto.setDescripcion(rol.getDescripcion());
        dto.setEsSistema(rol.getEsSistema());
        dto.setActivo(rol.getActivo());
        dto.setPermisos(rol.getPermisos() == null
                ? new ArrayList<>()
                : rol.getPermisos().stream()
                        .sorted((a, b) -> {
                            int mod = String.valueOf(a.getModulo()).compareToIgnoreCase(String.valueOf(b.getModulo()));
                            return mod != 0 ? mod : a.getClave().compareToIgnoreCase(b.getClave());
                        })
                        .map(this::permisoToDTO)
                        .toList());
        return dto;
    }

    private PermisoDTO permisoToDTO(Permiso permiso) {
        PermisoDTO dto = new PermisoDTO();
        dto.setIdPermiso(permiso.getIdPermiso());
        dto.setClave(permiso.getClave());
        dto.setNombre(permiso.getNombre());
        dto.setDescripcion(permiso.getDescripcion());
        dto.setModulo(permiso.getModulo());
        dto.setActivo(permiso.getActivo());
        return dto;
    }

}
