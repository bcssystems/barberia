package com.bcsystems.barberia_api.service;

import com.bcsystems.barberia_api.domain.Corte;
import com.bcsystems.barberia_api.domain.MovimientoInventario;
import com.bcsystems.barberia_api.domain.Venta;
import com.bcsystems.barberia_api.dto.CorteCompletoDTO;
import com.bcsystems.barberia_api.repository.CorteRepository;
import com.bcsystems.barberia_api.repository.MovimientoInventarioRepository;
import com.bcsystems.barberia_api.repository.VentaRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CorteService {

    private final CorteRepository corteRepository;
    private final VentaRepository ventaRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;

    @Autowired
    public CorteService(CorteRepository corteRepository,
                        VentaRepository ventaRepository,
                        MovimientoInventarioRepository movimientoInventarioRepository) {
        this.corteRepository = corteRepository;
        this.ventaRepository = ventaRepository;
        this.movimientoInventarioRepository = movimientoInventarioRepository;
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
        corte.setTotalFondoCaja(dto.getTotalFondoCaja() != null ? dto.getTotalFondoCaja() : 0.0);
        corte.setTotalIngresosCaja(dto.getTotalIngresosCaja() != null ? dto.getTotalIngresosCaja() : 0.0);
        corte.setTotalEgresosCaja(dto.getTotalEgresosCaja() != null ? dto.getTotalEgresosCaja() : 0.0);
        corte.setSaldoEsperado(dto.getSaldoEsperado() != null ? dto.getSaldoEsperado() : 0.0);
        corte.setSaldoFinal(dto.getSaldoFinal() != null ? dto.getSaldoFinal() : 0.0);
        corte.setTotalEfectivo(dto.getTotalEfectivo() != null ? dto.getTotalEfectivo() : 0.0);
        corte.setTotalTarjeta(dto.getTotalTarjeta() != null ? dto.getTotalTarjeta() : 0.0);
        corte.setTotalTransferencia(dto.getTotalTransferencia() != null ? dto.getTotalTransferencia() : 0.0);
        corte.setIdCaja(dto.getIdCaja());
        corte.setNombreCaja(dto.getNombreCaja());
        corte.setUsuario(dto.getUsuario());
        corte.setEfectivoOperaciones(dto.getEfectivoOperaciones() != null ? dto.getEfectivoOperaciones() : 0);
        corte.setTarjetaOperaciones(dto.getTarjetaOperaciones() != null ? dto.getTarjetaOperaciones() : 0);
        corte.setTransferenciaOperaciones(dto.getTransferenciaOperaciones() != null ? dto.getTransferenciaOperaciones() : 0);
        corte.setEfectivoReal(dto.getEfectivoReal());
        corte.setTarjetaReal(dto.getTarjetaReal());
        corte.setTransferenciaReal(dto.getTransferenciaReal());
        corte.setTotalReal(dto.getTotalReal());
        corte.setDiferencia(dto.getDiferencia());
        corte.setFechaRegistro(LocalDateTime.now());
        corte.setEstado("ACTIVO");
        Corte saved = corteRepository.save(corte);

        LocalDateTime fechaRegistro = saved.getFechaRegistro();

        // Marcar ventas no corte'd en el rango como consumidas por este corte
        List<Venta> ventas = ventaRepository.findAllByFechaBetweenAndFechaCorteIsNull(dto.getFechaInicio(), dto.getFechaFin());
        for (Venta v : ventas) {
            v.setFechaCorte(fechaRegistro);
        }
        ventaRepository.saveAll(ventas);

        // Marcar movimientos de inventario no corte'd en el rango como consumidos
        List<MovimientoInventario> movimientos = movimientoInventarioRepository.findAllByFechaBetweenAndFechaCorteIsNull(dto.getFechaInicio(), dto.getFechaFin());
        for (MovimientoInventario m : movimientos) {
            m.setFechaCorte(fechaRegistro);
        }
        movimientoInventarioRepository.saveAll(movimientos);

        return saved;
    }

    @Transactional(readOnly = true)
    public List<Corte> findAll() {
        return corteRepository.findAllByOrderByFechaRegistroDesc();
    }

    /**
     * Historial de cortes filtrado y paginado (Caja, Desde, Hasta).
     * Sin filtros devuelve todos en orden de registro descendente.
     */
    @Transactional(readOnly = true)
    public Page<Corte> findAllFiltrados(Integer idCaja, LocalDateTime desde, LocalDateTime hasta, Pageable pageable) {
        Specification<Corte> spec = (root, query, cb) -> {
            List<Predicate> preds = new ArrayList<>();
            if (idCaja != null) {
                preds.add(cb.equal(root.get("idCaja"), idCaja));
            }
            if (desde != null) {
                preds.add(cb.greaterThanOrEqualTo(root.get("fechaRegistro"), desde));
            }
            if (hasta != null) {
                preds.add(cb.lessThanOrEqualTo(root.get("fechaRegistro"), hasta));
            }
            return preds.isEmpty() ? cb.conjunction() : cb.and(preds.toArray(new Predicate[0]));
        };
        return corteRepository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public Corte findById(Integer id) {
        return corteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Corte no encontrado"));
    }

    @Transactional(readOnly = true)
    public Corte findUltimoActivo() {
        return corteRepository.findTopByEstadoOrderByFechaRegistroDesc("ACTIVO")
                .orElse(null);
    }

    @Transactional
    public void cancelarCorte(Integer id) {
        Corte corte = findById(id);

        // Desmarcar ventas que fueron consumidas por este corte
        List<Venta> ventas = ventaRepository.findAllByFechaBetweenAndFechaCorte(corte.getFechaInicio(), corte.getFechaFin(), corte.getFechaRegistro());
        for (Venta v : ventas) {
            v.setFechaCorte(null);
        }
        ventaRepository.saveAll(ventas);

        // Desmarcar movimientos que fueron consumidos por este corte
        List<MovimientoInventario> movimientos = movimientoInventarioRepository.findAllByFechaBetweenAndFechaCorteIsNotNull(corte.getFechaInicio(), corte.getFechaFin());
        for (MovimientoInventario m : movimientos) {
            m.setFechaCorte(null);
        }
        movimientoInventarioRepository.saveAll(movimientos);

        corte.setEstado("CANCELADO");
        corteRepository.save(corte);
    }
}
