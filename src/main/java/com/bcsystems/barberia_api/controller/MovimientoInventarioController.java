package com.bcsystems.barberia_api.controller;

import com.bcsystems.barberia_api.domain.en.TipoMovimiento;
import com.bcsystems.barberia_api.dto.MovimientoInventarioDTO;
import com.bcsystems.barberia_api.service.MovimientoInventarioService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventario")
public class MovimientoInventarioController {

    private final MovimientoInventarioService movimientoService;

    public MovimientoInventarioController(MovimientoInventarioService movimientoService) {
        this.movimientoService = movimientoService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('INVENTARIO_VER')")
    public ResponseEntity<Page<MovimientoInventarioDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(movimientoService.findAll(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('INVENTARIO_VER')")
    public ResponseEntity<MovimientoInventarioDTO> findById(@PathVariable Integer id) {
        return movimientoService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('INVENTARIO_CREAR')")
    public ResponseEntity<MovimientoInventarioDTO> create(@RequestBody MovimientoInventarioDTO dto) {
        MovimientoInventarioDTO saved = movimientoService.save(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('INVENTARIO_ELIMINAR')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        movimientoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/producto/{idProducto}")
    @PreAuthorize("hasAuthority('INVENTARIO_VER')")
    public ResponseEntity<Page<MovimientoInventarioDTO>> findByProducto(
            @PathVariable Integer idProducto,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(movimientoService.findByProducto(idProducto, pageable));
    }

    @GetMapping("/tipo/{tipo}")
    @PreAuthorize("hasAuthority('INVENTARIO_VER')")
    public ResponseEntity<Page<MovimientoInventarioDTO>> findByTipoMovimiento(
            @PathVariable TipoMovimiento tipo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(movimientoService.findByTipoMovimiento(tipo, pageable));
    }
}