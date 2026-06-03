package com.bcsystems.barberia_api.service;

import com.bcsystems.barberia_api.domain.Empleado;
import com.bcsystems.barberia_api.dto.EmpleadoDTO;
import com.bcsystems.barberia_api.repository.EmpleadoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class EmpleadoService {

    private final EmpleadoRepository empleadoRepository;

    public EmpleadoService(EmpleadoRepository empleadoRepository) {
        this.empleadoRepository = empleadoRepository;
    }

    @Transactional(readOnly = true)
    public Page<EmpleadoDTO> findAll(Pageable pageable) {
        return empleadoRepository.findAll(pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Optional<EmpleadoDTO> findById(Integer id) {
        return empleadoRepository.findById(id).map(this::toDTO);
    }

    @Transactional
    public EmpleadoDTO save(EmpleadoDTO dto) {
        if (empleadoRepository.existsByNombre(dto.getNombre())) {
            throw new RuntimeException("Ya existe un empleado con ese nombre");
        }
        if (empleadoRepository.existsByTelefono(dto.getTelefono())) {
            throw new RuntimeException("Ya existe un empleado con ese teléfono");
        }
        Empleado empleado = toEntity(dto);
        Empleado saved = empleadoRepository.save(empleado);
        return toDTO(saved);
    }

    @Transactional
    public EmpleadoDTO update(Integer id, EmpleadoDTO dto) {
        Empleado empleado = empleadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));
        if (!empleado.getNombre().equalsIgnoreCase(dto.getNombre()) && empleadoRepository.existsByNombre(dto.getNombre())) {
            throw new RuntimeException("Ya existe un empleado con ese nombre");
        }
        if (!empleado.getTelefono().equals(dto.getTelefono()) && empleadoRepository.existsByTelefono(dto.getTelefono())) {
            throw new RuntimeException("Ya existe un empleado con ese teléfono");
        }
        empleado.setNombre(dto.getNombre());
        empleado.setTelefono(dto.getTelefono());
        empleado.setPorcentajeComision(dto.getPorcentajeComision());
        return toDTO(empleadoRepository.save(empleado));
    }

    @Transactional
    public void delete(Integer id) {
        if (!empleadoRepository.existsById(id)) {
            throw new RuntimeException("Empleado no encontrado");
        }
        empleadoRepository.deleteById(id);
    }

    private EmpleadoDTO toDTO(Empleado empleado) {
        return new EmpleadoDTO(
                empleado.getIdEmpleado(),
                empleado.getNombre(),
                empleado.getTelefono(),
                empleado.getPorcentajeComision(),
                empleado.getStatus()
        );
    }

    private Empleado toEntity(EmpleadoDTO dto) {
        Empleado empleado = new Empleado();
        empleado.setNombre(dto.getNombre());
        empleado.setTelefono(dto.getTelefono());
        empleado.setPorcentajeComision(dto.getPorcentajeComision());
        return empleado;
    }
}