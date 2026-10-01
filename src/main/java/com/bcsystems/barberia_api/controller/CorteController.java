package com.bcsystems.barberia_api.controller;

import com.bcsystems.barberia_api.domain.Corte;
import com.bcsystems.barberia_api.dto.CorteDTO;
import com.bcsystems.barberia_api.service.CorteService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Los cortes se generan siempre por el servidor a traves de
 * POST /api/cajas/{id}/cierre. Por eso no se expone ningun endpoint
 * de escritura aqui: un corte guardado con importes enviados por el
 * cliente permitiria falsear la utilidad y el efectivo del turno.
 */
@RestController
@RequestMapping("/api/cortes")
public class CorteController {

    private final CorteService corteService;

    public CorteController(CorteService corteService) {
        this.corteService = corteService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('CAJA_CORTE','COMISIONES_VER')")
    public ResponseEntity<Page<CorteDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer idCaja,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fechaRegistro"));
        Page<CorteDTO> resultado = corteService.findAllFiltrados(
                        idCaja, inicioDelDia(desde), finDelDia(hasta), pageable)
                .map(CorteDTO::from);
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('CAJA_CORTE','COMISIONES_VER')")
    public ResponseEntity<CorteDTO> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(CorteDTO.from(corteService.findById(id)));
    }

    @GetMapping("/ultimo")
    @PreAuthorize("hasAnyAuthority('CAJA_CORTE','COMISIONES_VER')")
    public ResponseEntity<?> findUltimo() {
        Corte corte = corteService.findUltimoActivo();
        if (corte == null) {
            return ResponseEntity.ok(Map.of("message", "No hay cortes previos"));
        }
        return ResponseEntity.ok(CorteDTO.from(corte));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CAJA_CORTE')")
    public ResponseEntity<Void> cancelarCorte(@PathVariable Integer id) {
        corteService.cancelarCorte(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Los cortes se crean unicamente cerrando la caja. Si se intenta
     * guardar uno directo por API se responde 405 y no se persiste nada.
     */
    @RequestMapping(value = "", method = {RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<Map<String, String>> escrituraNoPermitida() {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(Map.of(
                        "message", "Los cortes se generan cerrando la caja: POST /api/cajas/{id}/cierre",
                        "error", "Corte no editable por API"));
    }

    private LocalDateTime inicioDelDia(LocalDate fecha) {
        return fecha == null ? null : fecha.atStartOfDay();
    }

    private LocalDateTime finDelDia(LocalDate fecha) {
        return fecha == null ? null : fecha.atTime(23, 59, 59);
    }
}
