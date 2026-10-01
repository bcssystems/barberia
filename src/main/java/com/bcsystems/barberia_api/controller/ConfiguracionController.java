package com.bcsystems.barberia_api.controller;

import com.bcsystems.barberia_api.dto.ConfiguracionDTO;
import com.bcsystems.barberia_api.service.ConfiguracionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/configuracion")
public class ConfiguracionController {

    /** Lectura: ademas de quien administra configuracion, la necesita el ticket de venta y el corte de caja. */
    private static final String LECTURA =
            "hasAnyAuthority('CONFIGURACION_VER','CONFIGURACION_EDITAR','VENTAS_VER','VENTAS_CREAR','CAJA_VER','CAJA_CORTE')";

    private final ConfiguracionService configuracionService;

    public ConfiguracionController(ConfiguracionService configuracionService) {
        this.configuracionService = configuracionService;
    }

    @GetMapping
    @PreAuthorize(LECTURA)
    public ResponseEntity<List<ConfiguracionDTO>> findAll() {
        return ResponseEntity.ok(configuracionService.findAll());
    }

    @GetMapping("/{clave}")
    @PreAuthorize(LECTURA)
    public ResponseEntity<ConfiguracionDTO> findByClave(@PathVariable String clave) {
        return ResponseEntity.ok(configuracionService.findByClave(clave));
    }

    @PutMapping("/{clave}")
    @PreAuthorize("hasAuthority('CONFIGURACION_EDITAR')")
    public ResponseEntity<ConfiguracionDTO> update(@PathVariable String clave, @RequestBody ConfiguracionDTO dto) {
        return ResponseEntity.ok(configuracionService.update(clave, dto));
    }
}
