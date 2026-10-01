package com.bcsystems.barberia_api.controller;

import com.bcsystems.barberia_api.dto.EmpleadoDTO;
import com.bcsystems.barberia_api.service.EmpleadoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/empleados")
public class EmpleadoController {

    private final EmpleadoService empleadoService;

    public EmpleadoController(EmpleadoService empleadoService) {
        this.empleadoService = empleadoService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('EMPLEADOS_VER','EMPLEADOS_CREAR','EMPLEADOS_EDITAR')")
    public ResponseEntity<Page<EmpleadoDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(empleadoService.findAll(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('EMPLEADOS_VER','EMPLEADOS_CREAR','EMPLEADOS_EDITAR')")
    public ResponseEntity<EmpleadoDTO> findById(@PathVariable Integer id) {
        return empleadoService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('EMPLEADOS_CREAR')")
    public ResponseEntity<EmpleadoDTO> create(@RequestBody EmpleadoDTO dto) {
        EmpleadoDTO saved = empleadoService.save(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('EMPLEADOS_EDITAR')")
    public ResponseEntity<EmpleadoDTO> update(@PathVariable Integer id, @RequestBody EmpleadoDTO dto) {
        return ResponseEntity.ok(empleadoService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('EMPLEADOS_ELIMINAR')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        empleadoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}