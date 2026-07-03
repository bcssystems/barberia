package com.bcsystems.barberia_api.service;

import com.bcsystems.barberia_api.domain.Servicio;
import com.bcsystems.barberia_api.dto.ServicioDTO;
import com.bcsystems.barberia_api.repository.ServicioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ServicioService {

    private final ServicioRepository servicioRepository;

    public ServicioService(ServicioRepository servicioRepository) {
        this.servicioRepository = servicioRepository;
    }

    @Transactional(readOnly = true)
    public Page<ServicioDTO> findAll(Pageable pageable) {
        return servicioRepository.findByStatus(1, pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Optional<ServicioDTO> findById(Integer id) {
        return servicioRepository.findById(id).map(this::toDTO);
    }

    @Transactional
    public ServicioDTO save(ServicioDTO dto) {
        if (servicioRepository.existsByNombre(dto.getNombre())) {
            throw new RuntimeException("Ya existe un servicio con ese nombre");
        }
        Servicio servicio = toEntity(dto);
        Servicio saved = servicioRepository.save(servicio);
        return toDTO(saved);
    }

    @Transactional
    public ServicioDTO update(Integer id, ServicioDTO dto) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));
        if (!servicio.getNombre().equalsIgnoreCase(dto.getNombre()) && servicioRepository.existsByNombre(dto.getNombre())) {
            throw new RuntimeException("Ya existe un servicio con ese nombre");
        }
        servicio.setNombre(dto.getNombre());
        servicio.setDescripcion(dto.getDescripcion());
        servicio.setDuracionMinutos(dto.getDuracionMinutos());
        servicio.setPrecio(dto.getPrecio());
        servicio.setComision(dto.getComision() != null ? dto.getComision() : 0.0);
        if (dto.getStatus() != null) servicio.setStatus(dto.getStatus());
        return toDTO(servicioRepository.save(servicio));
    }

    @Transactional
    public void delete(Integer id) {
        if (!servicioRepository.existsById(id)) {
            throw new RuntimeException("Servicio no encontrado");
        }
        servicioRepository.deleteById(id);
    }

    private ServicioDTO toDTO(Servicio servicio) {
        ServicioDTO dto = new ServicioDTO();
        dto.setIdServicio(servicio.getIdServicio());
        dto.setNombre(servicio.getNombre());
        dto.setDescripcion(servicio.getDescripcion());
        dto.setDuracionMinutos(servicio.getDuracionMinutos());
        dto.setPrecio(servicio.getPrecio());
        dto.setComision(servicio.getComision());
        dto.setStatus(servicio.getStatus());
        return dto;
    }

    private Servicio toEntity(ServicioDTO dto) {
        Servicio servicio = new Servicio();
        servicio.setNombre(dto.getNombre());
        servicio.setDescripcion(dto.getDescripcion());
        servicio.setDuracionMinutos(dto.getDuracionMinutos());
        servicio.setPrecio(dto.getPrecio());
        servicio.setComision(dto.getComision() != null ? dto.getComision() : 0.0);
        return servicio;
    }
}
