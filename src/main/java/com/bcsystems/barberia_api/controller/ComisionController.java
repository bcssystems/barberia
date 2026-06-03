package com.bcsystems.barberia_api.controller;

import com.bcsystems.barberia_api.dto.CorteCompletoDTO;
import com.bcsystems.barberia_api.dto.CorteComisionDTO;
import com.bcsystems.barberia_api.dto.PagoComisionDTO;
import com.bcsystems.barberia_api.service.ComisionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comisiones")
public class ComisionController {

    private final ComisionService comisionService;

    public ComisionController(ComisionService comisionService) {
        this.comisionService = comisionService;
    }

    @GetMapping
    public ResponseEntity<Page<PagoComisionDTO>> findAll(Pageable pageable) {
        return ResponseEntity.ok(comisionService.findAllPaginated(pageable));
    }

    @GetMapping("/all")
    public ResponseEntity<List<PagoComisionDTO>> findAllList() {
        return ResponseEntity.ok(comisionService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PagoComisionDTO> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(comisionService.findById(id));
    }

    @GetMapping("/empleado/{idEmpleado}")
    public ResponseEntity<List<PagoComisionDTO>> findByEmpleado(@PathVariable Integer idEmpleado) {
        return ResponseEntity.ok(comisionService.findByEmpleado(idEmpleado));
    }

    @GetMapping("/pendientes")
    public ResponseEntity<List<PagoComisionDTO>> findPendientes() {
        return ResponseEntity.ok(comisionService.findPendientes());
    }

    @GetMapping("/corte")
    public ResponseEntity<CorteComisionDTO> generarCorte(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        return ResponseEntity.ok()
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .header("Expires", "0")
                .body(comisionService.generarCorte(inicio, fin));
    }

    @GetMapping("/corte-completo")
    public ResponseEntity<CorteCompletoDTO> generarCorteCompleto(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        return ResponseEntity.ok()
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .header("Expires", "0")
                .body(comisionService.generarCorteCompleto(inicio, fin));
    }

    @PostMapping("/pagar")
    public ResponseEntity<PagoComisionDTO> pagarComisiones(
            @RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<Integer> idEmpleados = (List<Integer>) request.get("idEmpleados");
        String fechaInicioStr = (String) request.get("fechaInicio");
        String fechaFinStr = (String) request.get("fechaFin");

        LocalDateTime fechaInicio = LocalDateTime.parse(fechaInicioStr);
        LocalDateTime fechaFin = LocalDateTime.parse(fechaFinStr);

        return ResponseEntity.ok(comisionService.pagarComisiones(idEmpleados, fechaInicio, fechaFin));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PagoComisionDTO> update(@PathVariable Integer id, @RequestBody PagoComisionDTO dto) {
        return ResponseEntity.ok(comisionService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable Integer id) {
        comisionService.softDelete(id);
        return ResponseEntity.ok().build();
    }
}
