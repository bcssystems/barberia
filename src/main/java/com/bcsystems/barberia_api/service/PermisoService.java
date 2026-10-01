package com.bcsystems.barberia_api.service;

import com.bcsystems.barberia_api.domain.Permiso;
import com.bcsystems.barberia_api.dto.PermisoDTO;
import com.bcsystems.barberia_api.repository.PermisoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PermisoService {

    private final PermisoRepository permisoRepository;

    public PermisoService(PermisoRepository permisoRepository) {
        this.permisoRepository = permisoRepository;
    }

    @Transactional(readOnly = true)
    public List<PermisoDTO> findAll() {
        return permisoRepository.findAll().stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<PermisoDTO> findActivos() {
        return permisoRepository.findByActivoTrueOrderByModuloAscNombreAsc().stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public Map<String, List<PermisoDTO>> findAgrupadosPorModulo() {
        return permisoRepository.findByActivoTrueOrderByModuloAscNombreAsc().stream()
                .map(this::toDTO)
                .collect(Collectors.groupingBy(PermisoDTO::getModulo, java.util.LinkedHashMap::new, Collectors.toList()));
    }

    @Transactional
    public PermisoDTO updateActivo(Integer id, Boolean activo) {
        Permiso permiso = permisoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Permiso no encontrado"));
        permiso.setActivo(activo);
        return toDTO(permisoRepository.save(permiso));
    }

    private PermisoDTO toDTO(Permiso permiso) {
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
