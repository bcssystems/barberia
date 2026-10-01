package com.bcsystems.barberia_api.controller;

import com.bcsystems.barberia_api.dto.PermisoDTO;
import com.bcsystems.barberia_api.service.PermisoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/permisos")
public class PermisoController {

    /** Quien administra roles necesita leer los permisos para poder asignarlos. */
    private static final String LECTURA = "hasAnyAuthority('PERMISOS_VER','ROLES_CREAR','ROLES_EDITAR')";

    private final PermisoService permisoService;

    public PermisoController(PermisoService permisoService) {
        this.permisoService = permisoService;
    }

    @GetMapping
    @PreAuthorize(LECTURA)
    public ResponseEntity<List<PermisoDTO>> findAll() {
        return ResponseEntity.ok(permisoService.findAll());
    }

    @GetMapping("/activos")
    @PreAuthorize(LECTURA)
    public ResponseEntity<List<PermisoDTO>> findActivos() {
        return ResponseEntity.ok(permisoService.findActivos());
    }

    @GetMapping("/modulos")
    @PreAuthorize(LECTURA)
    public ResponseEntity<Map<String, List<PermisoDTO>>> findPorModulo() {
        return ResponseEntity.ok(permisoService.findAgrupadosPorModulo());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISOS_EDITAR')")
    public ResponseEntity<PermisoDTO> updateActivo(@PathVariable Integer id, @RequestBody Map<String, Boolean> body) {
        return ResponseEntity.ok(permisoService.updateActivo(id, body.get("activo")));
    }

}
