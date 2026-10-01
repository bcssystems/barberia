package com.bcsystems.barberia_api.controller;

import com.bcsystems.barberia_api.domain.en.EstadoCita;
import com.bcsystems.barberia_api.dto.CitaDTO;
import com.bcsystems.barberia_api.service.CitaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

    private final CitaService citaService;

    public CitaController(CitaService citaService) {
        this.citaService = citaService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('CITAS_VER','CITAS_CREAR','CITAS_EDITAR')")
    public ResponseEntity<Page<CitaDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(citaService.findAll(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('CITAS_VER','CITAS_CREAR','CITAS_EDITAR')")
    public ResponseEntity<CitaDTO> findById(@PathVariable Integer id) {
        return citaService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CITAS_CREAR')")
    public ResponseEntity<CitaDTO> create(@RequestBody CitaDTO dto) {
        CitaDTO saved = citaService.save(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CITAS_EDITAR')")
    public ResponseEntity<CitaDTO> update(@PathVariable Integer id, @RequestBody CitaDTO dto) {
        return ResponseEntity.ok(citaService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CITAS_ELIMINAR')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        citaService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/empleado/{idEmpleado}")
    @PreAuthorize("hasAnyAuthority('CITAS_VER','CITAS_CREAR','CITAS_EDITAR')")
    public ResponseEntity<Page<CitaDTO>> findByEmpleado(
            @PathVariable Integer idEmpleado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(citaService.findByEmpleado(idEmpleado, pageable));
    }

    @GetMapping("/cliente/{idCliente}")
    @PreAuthorize("hasAnyAuthority('CITAS_VER','CITAS_CREAR','CITAS_EDITAR')")
    public ResponseEntity<Page<CitaDTO>> findByCliente(
            @PathVariable Integer idCliente,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(citaService.findByCliente(idCliente, pageable));
    }

    @GetMapping("/estado/{estado}")
    @PreAuthorize("hasAnyAuthority('CITAS_VER','CITAS_CREAR','CITAS_EDITAR')")
    public ResponseEntity<Page<CitaDTO>> findByEstado(
            @PathVariable EstadoCita estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(citaService.findByEstado(estado, pageable));
    }
}