package com.bcsystems.barberia_api.controller;

import com.bcsystems.barberia_api.dto.RolDTO;
import com.bcsystems.barberia_api.service.RolService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RolController {

    /** Quien administra usuarios necesita leer los roles para asignarlos. */
    private static final String LECTURA = "hasAnyAuthority('ROLES_VER','USUARIOS_VER','USUARIOS_CREAR','USUARIOS_EDITAR')";

    private final RolService rolService;

    public RolController(RolService rolService) {
        this.rolService = rolService;
    }

    @GetMapping
    @PreAuthorize(LECTURA)
    public ResponseEntity<List<RolDTO>> findAll() {
        return ResponseEntity.ok(rolService.findAll());
    }

    @GetMapping("/activos")
    @PreAuthorize(LECTURA)
    public ResponseEntity<List<RolDTO>> findActivos() {
        return ResponseEntity.ok(rolService.findActivos());
    }

    @GetMapping("/{id}")
    @PreAuthorize(LECTURA)
    public ResponseEntity<RolDTO> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(rolService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLES_CREAR')")
    public ResponseEntity<RolDTO> create(@RequestBody RolDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rolService.save(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLES_EDITAR')")
    public ResponseEntity<RolDTO> update(@PathVariable Integer id, @RequestBody RolDTO dto) {
        return ResponseEntity.ok(rolService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLES_ELIMINAR')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        rolService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
