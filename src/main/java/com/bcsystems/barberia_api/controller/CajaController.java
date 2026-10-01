package com.bcsystems.barberia_api.controller;

import com.bcsystems.barberia_api.dto.CajaDTO;
import com.bcsystems.barberia_api.dto.CortePreviewDTO;
import com.bcsystems.barberia_api.dto.MovimientoCajaDTO;
import com.bcsystems.barberia_api.service.CajaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cajas")
public class CajaController {

    /** Quien puede ver cajas: cualquiera con una accion de caja o que venda en POS. */
    private static final String LECTURA = "hasAnyAuthority('CAJA_VER','CAJA_APERTURA','CAJA_CIERRE','CAJA_INGRESOS','CAJA_EGRESOS','CAJA_CORTE','VENTAS_CREAR')";

    private final CajaService cajaService;

    public CajaController(CajaService cajaService) {
        this.cajaService = cajaService;
    }

    @GetMapping
    @PreAuthorize(LECTURA)
    public ResponseEntity<List<CajaDTO>> findAll() {
        return ResponseEntity.ok(cajaService.findAll());
    }

    @GetMapping("/abiertas")
    @PreAuthorize(LECTURA)
    public ResponseEntity<List<CajaDTO>> findAbiertas() {
        return ResponseEntity.ok(cajaService.findAbiertas());
    }

    @GetMapping("/{id}")
    @PreAuthorize(LECTURA)
    public ResponseEntity<CajaDTO> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(cajaService.findById(id));
    }

    @GetMapping("/fondo-default")
    @PreAuthorize(LECTURA)
    public ResponseEntity<Double> getFondoDefault() {
        return ResponseEntity.ok(cajaService.getFondoDefault());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CAJA_APERTURA')")
    public ResponseEntity<CajaDTO> create(@RequestBody CajaDTO dto) {
        CajaDTO saved = cajaService.save(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PostMapping("/{id}/apertura")
    @PreAuthorize("hasAuthority('CAJA_APERTURA')")
    public ResponseEntity<CajaDTO> apertura(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        Double saldoInicial = body.get("saldoInicial") != null ? ((Number) body.get("saldoInicial")).doubleValue() : 0.0;
        return ResponseEntity.ok(cajaService.apertura(id, saldoInicial));
    }

    @PostMapping("/{id}/cierre")
    @PreAuthorize("hasAuthority('CAJA_CIERRE')")
    public ResponseEntity<CajaDTO> cierre(@PathVariable Integer id,
                                          @RequestBody(required = false) Map<String, Object> body) {
        Map<String, Double> conteo = null;
        if (body != null && body.get("conteo") instanceof Map<?, ?> mapaConteo) {
            conteo = new java.util.HashMap<>();
            for (Map.Entry<?, ?> entry : mapaConteo.entrySet()) {
                if (entry.getValue() instanceof Number numero) {
                    conteo.put(String.valueOf(entry.getKey()), numero.doubleValue());
                }
            }
        }
        return ResponseEntity.ok(cajaService.cierre(id, conteo, usuarioActual()));
    }

    /** Usuario autenticado que quedara registrado en el corte. */
    private String usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null) {
            return null;
        }
        return auth.getName();
    }

    @PostMapping("/{id}/ingresos")
    @PreAuthorize("hasAuthority('CAJA_INGRESOS')")
    public ResponseEntity<MovimientoCajaDTO> ingresarEfectivo(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        Double monto = ((Number) body.get("monto")).doubleValue();
        String motivo = (String) body.get("motivo");
        return ResponseEntity.ok(cajaService.ingresarEfectivo(id, monto, motivo));
    }

    @PostMapping("/{id}/egresos")
    @PreAuthorize("hasAuthority('CAJA_EGRESOS')")
    public ResponseEntity<MovimientoCajaDTO> retirarEfectivo(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        Double monto = ((Number) body.get("monto")).doubleValue();
        String motivo = (String) body.get("motivo");
        return ResponseEntity.ok(cajaService.retirarEfectivo(id, monto, motivo));
    }

    @GetMapping("/{id}/movimientos")
    @PreAuthorize(LECTURA)
    public ResponseEntity<List<MovimientoCajaDTO>> getMovimientos(@PathVariable Integer id) {
        return ResponseEntity.ok(cajaService.getMovimientos(id));
    }

    @GetMapping("/{id}/corte-preview")
    @PreAuthorize("hasAuthority('CAJA_CORTE')")
    public ResponseEntity<CortePreviewDTO> previewCorte(@PathVariable Integer id) {
        return ResponseEntity.ok(cajaService.previewCorte(id));
    }
}
