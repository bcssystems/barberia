package com.bcsystems.barberia_api.controller;

import com.bcsystems.barberia_api.domain.Corte;
import com.bcsystems.barberia_api.dto.CorteCompletoDTO;
import com.bcsystems.barberia_api.service.CorteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cortes")
public class CorteController {

    private final CorteService corteService;

    public CorteController(CorteService corteService) {
        this.corteService = corteService;
    }

    @PostMapping
    public ResponseEntity<Corte> guardarCorte(@RequestBody CorteCompletoDTO dto) {
        return ResponseEntity.ok(corteService.guardarCorte(dto));
    }

    @GetMapping
    public ResponseEntity<List<Corte>> findAll() {
        return ResponseEntity.ok(corteService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Corte> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(corteService.findById(id));
    }

    @GetMapping("/ultimo")
    public ResponseEntity<?> findUltimo() {
        Corte corte = corteService.findUltimoActivo();
        if (corte == null) {
            return ResponseEntity.ok(java.util.Map.of("message", "No hay cortes previos"));
        }
        return ResponseEntity.ok(corte);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelarCorte(@PathVariable Integer id) {
        corteService.cancelarCorte(id);
        return ResponseEntity.ok().build();
    }
}
