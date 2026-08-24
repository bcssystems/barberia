package com.bcsystems.barberia_api.service;

import com.bcsystems.barberia_api.domain.Configuracion;
import com.bcsystems.barberia_api.dto.ConfiguracionDTO;
import com.bcsystems.barberia_api.repository.ConfiguracionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConfiguracionService {

    private final ConfiguracionRepository configuracionRepository;

    public ConfiguracionService(ConfiguracionRepository configuracionRepository) {
        this.configuracionRepository = configuracionRepository;

        if (!configuracionRepository.existsByClave("fondo_caja_default")) {
            Configuracion c = new Configuracion();
            c.setClave("fondo_caja_default");
            c.setValor("500");
            configuracionRepository.save(c);
        }
    }

    @Transactional(readOnly = true)
    public List<ConfiguracionDTO> findAll() {
        return configuracionRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ConfiguracionDTO findByClave(String clave) {
        return configuracionRepository.findByClave(clave).map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Configuracion no encontrada: " + clave));
    }

    @Transactional
    public ConfiguracionDTO update(String clave, ConfiguracionDTO dto) {
        Configuracion config = configuracionRepository.findByClave(clave)
                .orElseGet(() -> {
                    Configuracion c = new Configuracion();
                    c.setClave(clave);
                    return c;
                });
        config.setValor(dto.getValor());
        return toDTO(configuracionRepository.save(config));
    }

    private ConfiguracionDTO toDTO(Configuracion c) {
        ConfiguracionDTO dto = new ConfiguracionDTO();
        dto.setIdConfiguracion(c.getIdConfiguracion());
        dto.setClave(c.getClave());
        dto.setValor(c.getValor());
        return dto;
    }
}
