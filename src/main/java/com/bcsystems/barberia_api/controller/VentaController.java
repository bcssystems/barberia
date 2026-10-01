package com.bcsystems.barberia_api.controller;

import com.bcsystems.barberia_api.dto.VentaDTO;
import com.bcsystems.barberia_api.service.VentaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    /** Lectura de ventas: ver el historial o poder cobrar en POS. */
    private static final String LECTURA = "hasAnyAuthority('VENTAS_VER','VENTAS_CREAR','VENTAS_CANCELAR')";

    private final VentaService ventaService;

    public VentaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    @GetMapping
    @PreAuthorize(LECTURA)
    public ResponseEntity<Page<VentaDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer idEmpleado,
            @RequestParam(required = false) Integer idCaja,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        Pageable pageable = PageRequest.of(page, size);

        if (idEmpleado == null && idCaja == null && isSinFiltros(estado) && desde == null && hasta == null) {
            return ResponseEntity.ok(ventaService.findAll(pageable));
        }

        return ResponseEntity.ok(ventaService.findAllFiltradas(
                idEmpleado, idCaja, parseEstado(estado), inicioDelDia(desde), finDelDia(hasta), pageable));
    }

    @GetMapping("/resumen")
    @PreAuthorize(LECTURA)
    public ResponseEntity<Map<String, Object>> resumen(
            @RequestParam(required = false) Integer idEmpleado,
            @RequestParam(required = false) Integer idCaja,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(ventaService.resumenFiltrado(
                idEmpleado, idCaja, parseEstado(estado), inicioDelDia(desde), finDelDia(hasta)));
    }

    private boolean isSinFiltros(String estado) {
        return estado == null || estado.isBlank() || "TODAS".equalsIgnoreCase(estado);
    }

    /** Convierte el filtro de estado textual a la bandera cancelada. */
    private Boolean parseEstado(String estado) {
        if (estado == null || estado.isBlank() || "TODAS".equalsIgnoreCase(estado)) return null;
        if ("CANCELADAS".equalsIgnoreCase(estado)) return Boolean.TRUE;
        if ("ACTIVAS".equalsIgnoreCase(estado)) return Boolean.FALSE;
        return null;
    }

    private LocalDateTime inicioDelDia(LocalDate fecha) {
        return fecha == null ? null : fecha.atStartOfDay();
    }

    private LocalDateTime finDelDia(LocalDate fecha) {
        return fecha == null ? null : fecha.atTime(23, 59, 59);
    }

    @GetMapping("/{id}")
    @PreAuthorize(LECTURA)
    public ResponseEntity<VentaDTO> findById(@PathVariable Integer id) {
        return ventaService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('VENTAS_CREAR')")
    public ResponseEntity<VentaDTO> create(@RequestBody VentaDTO dto) {
        VentaDTO saved = ventaService.save(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('VENTAS_CREAR')")
    public ResponseEntity<VentaDTO> update(@PathVariable Integer id, @RequestBody VentaDTO dto) {
        return ResponseEntity.ok(ventaService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('VENTAS_CANCELAR')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        ventaService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasAuthority('VENTAS_CANCELAR')")
    public ResponseEntity<VentaDTO> cancelar(@PathVariable Integer id) {
        return ResponseEntity.ok(ventaService.cancelarVenta(id));
    }

    @GetMapping("/fecha")
    @PreAuthorize(LECTURA)
    public ResponseEntity<Page<VentaDTO>> findByFechaBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ventaService.findByFechaBetween(start, end, pageable));
    }
}