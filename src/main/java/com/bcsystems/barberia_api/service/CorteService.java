package com.bcsystems.barberia_api.service;

import com.bcsystems.barberia_api.domain.Corte;
import com.bcsystems.barberia_api.dto.CorteCompletoDTO;
import com.bcsystems.barberia_api.repository.CorteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CorteService {

    private final CorteRepository corteRepository;

    @Autowired
    public CorteService(CorteRepository corteRepository) {
        this.corteRepository = corteRepository;
    }

    @Transactional
    public Corte guardarCorte(CorteCompletoDTO dto) {
        Corte corte = new Corte();
        corte.setFechaInicio(dto.getFechaInicio());
        corte.setFechaFin(dto.getFechaFin());
        corte.setTotalVentasServicios(dto.getTotalVentasServicios());
        corte.setTotalVentasProductos(dto.getTotalVentasProductos());
        corte.setTotalVentas(dto.getTotalVentas());
        corte.setCostoProductosVendidos(dto.getCostoProductosVendidos());
        corte.setGastosInventario(dto.getGastosInventario());
        corte.setTotalCostos(dto.getTotalCostos());
        corte.setUtilidadBruta(dto.getUtilidadBruta());
        corte.setTotalComisionesPendientes(dto.getTotalComisionesPendientes());
        corte.setTotalComisionesPagadas(dto.getTotalComisionesPagadas());
        corte.setUtilidadNeta(dto.getUtilidadNeta());
        corte.setFechaRegistro(LocalDateTime.now());
        corte.setEstado("ACTIVO");
        return corteRepository.save(corte);
    }

    @Transactional(readOnly = true)
    public List<Corte> findAll() {
        return corteRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Corte findById(Integer id) {
        return corteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Corte no encontrado"));
    }

    @Transactional
    public void cancelarCorte(Integer id) {
        Corte corte = findById(id);
        corte.setEstado("CANCELADO");
        corteRepository.save(corte);
    }
}
