package com.bcsystems.barberia_api.controller;

import com.bcsystems.barberia_api.dto.ConfiguracionDTO;
import com.bcsystems.barberia_api.service.ConfiguracionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/configuracion")
public class ConfiguracionController {

    private final ConfiguracionService configuracionService;

    public ConfiguracionController(ConfiguracionService configuracionService) {
        this.configuracionService = configuracionService;
    }

    @GetMapping
    public ResponseEntity<List<ConfiguracionDTO>> findAll() {
        return ResponseEntity.ok(configuracionService.findAll());
    }

    @GetMapping("/{clave}")
    public ResponseEntity<ConfiguracionDTO> findByClave(@PathVariable String clave) {
        return ResponseEntity.ok(configuracionService.findByClave(clave));
    }

    @PutMapping("/{clave}")
    public ResponseEntity<ConfiguracionDTO> update(@PathVariable String clave, @RequestBody ConfiguracionDTO dto) {
        return ResponseEntity.ok(configuracionService.update(clave, dto));
    }
}
