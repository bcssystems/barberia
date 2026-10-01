package com.bcsystems.barberia_api.controller;

import com.bcsystems.barberia_api.dto.ServicioDTO;
import com.bcsystems.barberia_api.service.ServicioService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/servicios")
public class ServicioController {

    private final ServicioService servicioService;

    public ServicioController(ServicioService servicioService) {
        this.servicioService = servicioService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('SERVICIOS_VER','SERVICIOS_CREAR','SERVICIOS_EDITAR')")
    public ResponseEntity<Page<ServicioDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(servicioService.findAll(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SERVICIOS_VER','SERVICIOS_CREAR','SERVICIOS_EDITAR')")
    public ResponseEntity<ServicioDTO> findById(@PathVariable Integer id) {
        return servicioService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SERVICIOS_CREAR')")
    public ResponseEntity<ServicioDTO> create(@RequestBody ServicioDTO dto) {
        ServicioDTO saved = servicioService.save(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SERVICIOS_EDITAR')")
    public ResponseEntity<ServicioDTO> update(@PathVariable Integer id, @RequestBody ServicioDTO dto) {
        return ResponseEntity.ok(servicioService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SERVICIOS_ELIMINAR')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        servicioService.delete(id);
        return ResponseEntity.noContent().build();
    }
}