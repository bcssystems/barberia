package com.bcsystems.barberia_api.controller;

import com.bcsystems.barberia_api.dto.CajaDTO;
import com.bcsystems.barberia_api.dto.CortePreviewDTO;
import com.bcsystems.barberia_api.dto.MovimientoCajaDTO;
import com.bcsystems.barberia_api.service.CajaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cajas")
public class CajaController {

    private final CajaService cajaService;

    public CajaController(CajaService cajaService) {
        this.cajaService = cajaService;
    }

    @GetMapping
    public ResponseEntity<List<CajaDTO>> findAll() {
        return ResponseEntity.ok(cajaService.findAll());
    }

    @GetMapping("/abiertas")
    public ResponseEntity<List<CajaDTO>> findAbiertas() {
        return ResponseEntity.ok(cajaService.findAbiertas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CajaDTO> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(cajaService.findById(id));
    }

    @GetMapping("/fondo-default")
    public ResponseEntity<Double> getFondoDefault() {
        return ResponseEntity.ok(cajaService.getFondoDefault());
    }

    @PostMapping
    public ResponseEntity<CajaDTO> create(@RequestBody CajaDTO dto) {
        CajaDTO saved = cajaService.save(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PostMapping("/{id}/apertura")
    public ResponseEntity<CajaDTO> apertura(@PathVariable Integer id, @RequestBody Map<String, Double> body) {
        Double saldoInicial = body.get("saldoInicial");
        return ResponseEntity.ok(cajaService.apertura(id, saldoInicial));
    }

    @PostMapping("/{id}/cierre")
    public ResponseEntity<CajaDTO> cierre(@PathVariable Integer id) {
        return ResponseEntity.ok(cajaService.cierre(id));
    }

    @PostMapping("/{id}/ingresos")
    public ResponseEntity<MovimientoCajaDTO> ingresarEfectivo(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        Double monto = (Double) body.get("monto");
        String motivo = (String) body.get("motivo");
        return ResponseEntity.ok(cajaService.ingresarEfectivo(id, monto, motivo));
    }

    @PostMapping("/{id}/egresos")
    public ResponseEntity<MovimientoCajaDTO> retirarEfectivo(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        Double monto = (Double) body.get("monto");
        String motivo = (String) body.get("motivo");
        return ResponseEntity.ok(cajaService.retirarEfectivo(id, monto, motivo));
    }

    @GetMapping("/{id}/movimientos")
    public ResponseEntity<List<MovimientoCajaDTO>> getMovimientos(@PathVariable Integer id) {
        return ResponseEntity.ok(cajaService.getMovimientos(id));
    }

    @GetMapping("/{id}/corte-preview")
    public ResponseEntity<CortePreviewDTO> previewCorte(@PathVariable Integer id) {
        return ResponseEntity.ok(cajaService.previewCorte(id));
    }
}
