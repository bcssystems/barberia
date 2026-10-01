package com.bcsystems.barberia_api.controller;

import com.bcsystems.barberia_api.dto.CarritoItemDTO;
import com.bcsystems.barberia_api.service.CarritoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/carrito")
public class CarritoController {

    /** El carrito del POS lo usa quien cobra, por eso VENTAS_CREAR tambien da acceso. */
    private static final String LECTURA = "hasAnyAuthority('CARRITO_VER','CARRITO_EDITAR','VENTAS_CREAR')";
    private static final String EDICION = "hasAnyAuthority('CARRITO_EDITAR','VENTAS_CREAR')";

    private final CarritoService carritoService;

    public CarritoController(CarritoService carritoService) {
        this.carritoService = carritoService;
    }

    @GetMapping("/{idCaja}")
    @PreAuthorize(LECTURA)
    public ResponseEntity<List<CarritoItemDTO>> findByCaja(@PathVariable Integer idCaja) {
        return ResponseEntity.ok(carritoService.findByCaja(idCaja));
    }

    @PostMapping
    @PreAuthorize(EDICION)
    public ResponseEntity<List<CarritoItemDTO>> agregar(@RequestBody Map<String, Object> body) {
        List<CarritoItemDTO> carrito = carritoService.agregar(
                intValue(body, "idCaja"),
                strValue(body, "tipo"),
                intValue(body, "idReferencia"),
                body.get("cantidad") != null ? intValue(body, "cantidad") : 1,
                body.get("precioUnitario") != null ? dblValue(body, "precioUnitario") : null);
        return ResponseEntity.status(HttpStatus.CREATED).body(carrito);
    }

    @PutMapping
    @PreAuthorize(EDICION)
    public ResponseEntity<List<CarritoItemDTO>> actualizar(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(carritoService.actualizar(
                intValue(body, "idCaja"),
                strValue(body, "tipo"),
                intValue(body, "idReferencia"),
                intValue(body, "cantidad")));
    }

    @DeleteMapping("/{idCaja}/{tipo}/{idReferencia}")
    @PreAuthorize(EDICION)
    public ResponseEntity<Void> quitar(@PathVariable Integer idCaja,
                                       @PathVariable String tipo,
                                       @PathVariable Integer idReferencia) {
        carritoService.quitar(idCaja, tipo, idReferencia);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{idCaja}")
    @PreAuthorize(EDICION)
    public ResponseEntity<Void> limpiar(@PathVariable Integer idCaja) {
        carritoService.limpiar(idCaja);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{idCaja}")
    @PreAuthorize(EDICION)
    public ResponseEntity<List<CarritoItemDTO>> sincronizar(@PathVariable Integer idCaja,
                                                             @RequestBody List<CarritoService.LineaRecibida> lineas) {
        return ResponseEntity.ok(carritoService.sincronizar(idCaja, lineas));
    }

    private Integer intValue(Map<String, Object> body, String key) {
        Object value = body.get(key);
        if (value == null) {
            throw new RuntimeException("El campo '" + key + "' es obligatorio");
        }
        return Integer.valueOf(value.toString());
    }

    private Double dblValue(Map<String, Object> body, String key) {
        return Double.valueOf(body.get(key).toString());
    }

    private String strValue(Map<String, Object> body, String key) {
        Object value = body.get(key);
        if (value == null) {
            throw new RuntimeException("El campo '" + key + "' es obligatorio");
        }
        return value.toString();
    }

}
