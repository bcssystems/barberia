package com.bcsystems.barberia_api.controller;

import com.bcsystems.barberia_api.dto.UsuarioDTO;
import com.bcsystems.barberia_api.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    /** Quien administra roles necesita leer los usuarios para asignarlos. */
    private static final String LECTURA = "hasAnyAuthority('USUARIOS_VER','USUARIOS_EDITAR','ROLES_CREAR','ROLES_EDITAR')";

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    @PreAuthorize(LECTURA)
    public ResponseEntity<List<UsuarioDTO>> findAll() {
        return ResponseEntity.ok(usuarioService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize(LECTURA)
    public ResponseEntity<UsuarioDTO> findById(@PathVariable Integer id) {
        return usuarioService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USUARIOS_CREAR')")
    public ResponseEntity<UsuarioDTO> create(@RequestBody UsuarioDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.save(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USUARIOS_EDITAR')")
    public ResponseEntity<UsuarioDTO> update(@PathVariable Integer id, @RequestBody UsuarioDTO dto) {
        return ResponseEntity.ok(usuarioService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USUARIOS_ELIMINAR')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        usuarioService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UsuarioDTO> cambiarPassword(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(usuarioService.cambiarPassword(
                id, body.get("passwordActual"), body.get("passwordNuevo")));
    }

}
