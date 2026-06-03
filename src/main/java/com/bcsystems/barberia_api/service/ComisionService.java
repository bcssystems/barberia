package com.bcsystems.barberia_api.service;

import com.bcsystems.barberia_api.domain.Cita;
import com.bcsystems.barberia_api.domain.Empleado;
import com.bcsystems.barberia_api.domain.PagoComision;
import com.bcsystems.barberia_api.domain.Venta;
import com.bcsystems.barberia_api.domain.VentaDetalle;
import com.bcsystems.barberia_api.domain.en.EstadoPago;
import com.bcsystems.barberia_api.dto.CorteCompletoDTO;
import com.bcsystems.barberia_api.dto.CorteComisionDTO;
import com.bcsystems.barberia_api.dto.PagoComisionDTO;
import com.bcsystems.barberia_api.dto.ResumenComisionEmpleadoDTO;
import com.bcsystems.barberia_api.repository.CitaRepository;
import com.bcsystems.barberia_api.repository.EmpleadoRepository;
import com.bcsystems.barberia_api.repository.MovimientoInventarioRepository;
import com.bcsystems.barberia_api.repository.PagoComisionRepository;
import com.bcsystems.barberia_api.repository.VentaDetalleRepository;
import com.bcsystems.barberia_api.repository.VentaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ComisionService {

    private final PagoComisionRepository pagoComisionRepository;
    private final EmpleadoRepository empleadoRepository;
    private final VentaRepository ventaRepository;
    private final VentaDetalleRepository ventaDetalleRepository;
    private final CitaRepository citaRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;

    public ComisionService(PagoComisionRepository pagoComisionRepository,
                           EmpleadoRepository empleadoRepository,
                           VentaRepository ventaRepository,
                           VentaDetalleRepository ventaDetalleRepository,
                           CitaRepository citaRepository,
                           MovimientoInventarioRepository movimientoInventarioRepository) {
        this.pagoComisionRepository = pagoComisionRepository;
        this.empleadoRepository = empleadoRepository;
        this.ventaRepository = ventaRepository;
        this.ventaDetalleRepository = ventaDetalleRepository;
        this.citaRepository = citaRepository;
        this.movimientoInventarioRepository = movimientoInventarioRepository;
    }

    @Transactional(readOnly = true)
    public Page<PagoComisionDTO> findAllPaginated(Pageable pageable) {
        return pagoComisionRepository.findByDeletedFalse(pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<PagoComisionDTO> findAll() {
        return pagoComisionRepository.findByDeletedFalse(Pageable.unpaged()).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PagoComisionDTO findById(Integer id) {
        return pagoComisionRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Pago de comisión no encontrado"));
    }

    @Transactional(readOnly = true)
    public List<PagoComisionDTO> findByEmpleado(Integer idEmpleado) {
        return pagoComisionRepository.findByEmpleadoIdEmpleadoAndDeletedFalse(idEmpleado).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PagoComisionDTO> findPendientes() {
        return pagoComisionRepository.findByEstadoAndDeletedFalse(EstadoPago.PENDIENTE).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CorteComisionDTO generarCorte(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        CorteComisionDTO corte = new CorteComisionDTO();
        corte.setFechaInicio(fechaInicio);
        corte.setFechaFin(fechaFin);

        List<VentaDetalle> todosDetalles = ventaDetalleRepository.findAll();

        Map<Integer, Double> ventasPorEmpleado = new HashMap<>();
        Map<Integer, Double> ventasPagadasPorEmpleado = new HashMap<>();

        for (VentaDetalle vd : todosDetalles) {
            if (vd.getServicio() == null) continue;
            if (vd.getVenta() == null) continue;

            Venta venta = vd.getVenta();
            if (venta.getFechaCorte() != null) continue;
            if (venta.getCita() == null) continue;

            Cita cita = venta.getCita();
            if (!"COMPLETADA".equals(cita.getEstado().name())) continue;
            if (cita.getEmpleado() == null) continue;

            LocalDateTime fechaVenta = venta.getFecha();
            if (fechaVenta != null && (fechaVenta.isBefore(fechaInicio) || fechaVenta.isAfter(fechaFin))) continue;

            int empId = cita.getEmpleado().getIdEmpleado();
            double subtotal = vd.getPrecio() * (vd.getCantidad() != null ? vd.getCantidad() : 1);

            if (vd.getComisionPagada() != null && vd.getComisionPagada()) {
                ventasPagadasPorEmpleado.merge(empId, subtotal, Double::sum);
            } else {
                ventasPorEmpleado.merge(empId, subtotal, Double::sum);
            }
        }

        List<Empleado> empleados = empleadoRepository.findAll();
        List<ResumenComisionEmpleadoDTO> resumenList = new ArrayList<>();
        double totalComisionesPendientes = 0;
        double totalPagadas = 0;

        for (Empleado emp : empleados) {
            if (emp.getStatus() != 1) continue;

            Double porcentaje = emp.getPorcentajeComision() != null ? emp.getPorcentajeComision() : 0;

            Double totalVentasPendientes = ventasPorEmpleado.get(emp.getIdEmpleado());
            Double totalVentasPagadas = ventasPagadasPorEmpleado.get(emp.getIdEmpleado());
            boolean tienePendiente = totalVentasPendientes != null && totalVentasPendientes > 0;
            boolean tienePagado = totalVentasPagadas != null && totalVentasPagadas > 0;

            if (!tienePendiente && !tienePagado) continue;

            Double totalServicios = (totalVentasPendientes != null ? totalVentasPendientes : 0)
                                  + (totalVentasPagadas != null ? totalVentasPagadas : 0);
            Double montoComisionPendiente = tienePendiente ? totalVentasPendientes * (porcentaje / 100.0) : 0;
            Double montoComisionPagado = tienePagado ? totalVentasPagadas * (porcentaje / 100.0) : 0;

            totalComisionesPendientes += montoComisionPendiente;
            totalPagadas += montoComisionPagado;

            ResumenComisionEmpleadoDTO resumen = new ResumenComisionEmpleadoDTO();
            resumen.setIdEmpleado(emp.getIdEmpleado());
            resumen.setNombreEmpleado(emp.getNombre());
            resumen.setTotalVentasServicios(totalServicios);
            resumen.setPorcentajeComision(porcentaje);
            resumen.setMontoComision(montoComisionPendiente + montoComisionPagado);
            resumen.setTienePagoPendiente(tienePendiente);
            resumenList.add(resumen);
        }

        corte.setComisionesPorEmpleado(resumenList);
        corte.setTotalComisionesPendientes(totalComisionesPendientes);
        corte.setTotalComisionesPagadas(totalPagadas);

        return corte;
    }

    @Transactional(readOnly = true)
    public CorteCompletoDTO generarCorteCompleto(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        CorteCompletoDTO corte = new CorteCompletoDTO();
        corte.setFechaInicio(fechaInicio);
        corte.setFechaFin(fechaFin);

        // Calcular ventas de servicios y productos
        Double totalVentasServicios = ventaDetalleRepository.sumServiciosByFechaBetween(fechaInicio, fechaFin);
        Double totalVentasProductos = ventaDetalleRepository.sumTotalProductosByFechaBetween(fechaInicio, fechaFin);
        totalVentasServicios = totalVentasServicios != null ? totalVentasServicios : 0.0;
        totalVentasProductos = totalVentasProductos != null ? totalVentasProductos : 0.0;

        corte.setTotalVentasServicios(totalVentasServicios);
        corte.setTotalVentasProductos(totalVentasProductos);
        corte.setTotalVentas(totalVentasServicios + totalVentasProductos);

        // Calcular costo de productos vendidos (precio de compra)
        Double costoProductosVendidos = ventaDetalleRepository.sumCostoProductosByFechaBetween(fechaInicio, fechaFin);
        costoProductosVendidos = costoProductosVendidos != null ? costoProductosVendidos : 0.0;
        corte.setCostoProductosVendidos(costoProductosVendidos);

        // Calcular gastos de inventario (entradas de mercancía nueva)
        Double gastosInventario = movimientoInventarioRepository.sumComprasByFechaBetween(fechaInicio, fechaFin);
        gastosInventario = gastosInventario != null ? gastosInventario : 0.0;
        corte.setGastosInventario(gastosInventario);

        Double totalCostos = costoProductosVendidos + gastosInventario;
        corte.setTotalCostos(totalCostos);

        // Utilidad bruta
        Double utilidadBruta = (totalVentasServicios + totalVentasProductos) - totalCostos;
        corte.setUtilidadBruta(utilidadBruta);

        // Calcular comisiones por empleado desde detalles de venta no cortados
        List<VentaDetalle> todosDetalles = ventaDetalleRepository.findAll();
        Map<Integer, Double> ventasPorEmpleado = new HashMap<>();
        Map<Integer, Double> ventasPagadasPorEmpleado = new HashMap<>();

        for (VentaDetalle vd : todosDetalles) {
            if (vd.getServicio() == null) continue;
            if (vd.getVenta() == null) continue;

            Venta venta = vd.getVenta();
            if (venta.getFechaCorte() != null) continue;
            if (venta.getCita() == null) continue;

            Cita cita = venta.getCita();
            if (!"COMPLETADA".equals(cita.getEstado().name())) continue;
            if (cita.getEmpleado() == null) continue;

            LocalDateTime fechaVenta = venta.getFecha();
            if (fechaVenta != null && (fechaVenta.isBefore(fechaInicio) || fechaVenta.isAfter(fechaFin))) continue;

            int empId = cita.getEmpleado().getIdEmpleado();
            double subtotal = vd.getPrecio() * (vd.getCantidad() != null ? vd.getCantidad() : 1);

            if (vd.getComisionPagada() != null && vd.getComisionPagada()) {
                ventasPagadasPorEmpleado.merge(empId, subtotal, Double::sum);
            } else {
                ventasPorEmpleado.merge(empId, subtotal, Double::sum);
            }
        }

        List<Empleado> empleados = empleadoRepository.findAll();
        List<ResumenComisionEmpleadoDTO> resumenList = new ArrayList<>();
        double totalComisionesPendientes = 0;
        double totalPagadas = 0;

        for (Empleado emp : empleados) {
            if (emp.getStatus() != 1) continue;

            Double porcentaje = emp.getPorcentajeComision() != null ? emp.getPorcentajeComision() : 0;

            Double totalVentasPendientes = ventasPorEmpleado.get(emp.getIdEmpleado());
            Double totalVentasPagadas = ventasPagadasPorEmpleado.get(emp.getIdEmpleado());
            boolean tienePendiente = totalVentasPendientes != null && totalVentasPendientes > 0;
            boolean tienePagado = totalVentasPagadas != null && totalVentasPagadas > 0;

            if (!tienePendiente && !tienePagado) continue;

            Double totalServicios = (totalVentasPendientes != null ? totalVentasPendientes : 0)
                                  + (totalVentasPagadas != null ? totalVentasPagadas : 0);
            Double montoComisionPendiente = tienePendiente ? totalVentasPendientes * (porcentaje / 100.0) : 0;
            Double montoComisionPagado = tienePagado ? totalVentasPagadas * (porcentaje / 100.0) : 0;

            totalComisionesPendientes += montoComisionPendiente;
            totalPagadas += montoComisionPagado;

            ResumenComisionEmpleadoDTO resumen = new ResumenComisionEmpleadoDTO();
            resumen.setIdEmpleado(emp.getIdEmpleado());
            resumen.setNombreEmpleado(emp.getNombre());
            resumen.setTotalVentasServicios(totalServicios);
            resumen.setPorcentajeComision(porcentaje);
            resumen.setMontoComision(montoComisionPendiente + montoComisionPagado);
            resumen.setTienePagoPendiente(tienePendiente);
            resumenList.add(resumen);
        }

        corte.setComisionesPorEmpleado(resumenList);
        corte.setTotalComisionesPendientes(totalComisionesPendientes);
        corte.setTotalComisionesPagadas(totalPagadas);

        // Utilidad neta (después de comisiones)
        Double utilidadNeta = utilidadBruta - totalComisionesPendientes - totalPagadas;
        corte.setUtilidadNeta(utilidadNeta);

        return corte;
    }

    @Transactional
    public PagoComisionDTO pagarComisiones(List<Integer> idEmpleados, LocalDateTime fechaCorteInicio, LocalDateTime fechaCorteFin) {
        Double totalPagado = 0.0;

        for (Integer idEmpleado : idEmpleados) {
            Empleado emp = empleadoRepository.findById(idEmpleado)
                    .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

            List<VentaDetalle> todosDetalles = ventaDetalleRepository.findAll();
            double totalVentas = 0;

            for (VentaDetalle vd : todosDetalles) {
                if (vd.getServicio() == null) continue;
                if (vd.getVenta() == null) continue;
                if (vd.getComisionPagada() != null && vd.getComisionPagada()) continue;

                Venta venta = vd.getVenta();
                if (venta.getCita() == null) continue;

                Cita cita = venta.getCita();
                if (!"COMPLETADA".equals(cita.getEstado().name())) continue;
                if (cita.getEmpleado() == null) continue;
                if (!cita.getEmpleado().getIdEmpleado().equals(idEmpleado)) continue;

                LocalDateTime fechaVenta = venta.getFecha();
                if (fechaVenta != null && (fechaVenta.isBefore(fechaCorteInicio) || fechaVenta.isAfter(fechaCorteFin))) continue;

                double subtotal = vd.getPrecio() * (vd.getCantidad() != null ? vd.getCantidad() : 1);
                totalVentas += subtotal;

                vd.setComisionPagada(true);
                ventaDetalleRepository.save(vd);
            }

            if (totalVentas == 0) continue;

            Double porcentaje = emp.getPorcentajeComision() != null ? emp.getPorcentajeComision() : 0;
            Double montoComision = totalVentas * (porcentaje / 100.0);

            PagoComision pago = new PagoComision();
            pago.setEmpleado(emp);
            pago.setMontoComision(montoComision);
            pago.setFechaCorteInicio(fechaCorteInicio);
            pago.setFechaCorteFin(fechaCorteFin);
            pago.setFechaPago(LocalDateTime.now());
            pago.setEstado(EstadoPago.PAGADA);
            pago.setDeleted(false);

            pagoComisionRepository.save(pago);
            totalPagado += montoComision;
        }

        PagoComisionDTO result = new PagoComisionDTO();
        result.setMontoComision(totalPagado);
        result.setEstado(EstadoPago.PAGADA);
        return result;
    }

    @Transactional
    public PagoComisionDTO update(Integer id, PagoComisionDTO dto) {
        PagoComision pago = pagoComisionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pago de comisión no encontrado"));

        if (dto.getMontoComision() != null) {
            pago.setMontoComision(dto.getMontoComision());
        }
        if (dto.getEstado() != null) {
            pago.setEstado(dto.getEstado());
        }
        if (dto.getFechaCorteInicio() != null) {
            pago.setFechaCorteInicio(dto.getFechaCorteInicio());
        }
        if (dto.getFechaCorteFin() != null) {
            pago.setFechaCorteFin(dto.getFechaCorteFin());
        }

        return toDTO(pagoComisionRepository.save(pago));
    }

    @Transactional
    public void softDelete(Integer id) {
        PagoComision pago = pagoComisionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pago de comisión no encontrado"));
        pago.setDeleted(true);
        pagoComisionRepository.save(pago);
    }

    private PagoComisionDTO toDTO(PagoComision pc) {
        PagoComisionDTO dto = new PagoComisionDTO();
        dto.setIdPagoComision(pc.getIdPagoComision());
        dto.setIdEmpleado(pc.getEmpleado() != null ? pc.getEmpleado().getIdEmpleado() : null);
        dto.setNombreEmpleado(pc.getEmpleado() != null ? pc.getEmpleado().getNombre() : null);
        dto.setMontoComision(pc.getMontoComision());
        dto.setFechaCorteInicio(pc.getFechaCorteInicio());
        dto.setFechaCorteFin(pc.getFechaCorteFin());
        dto.setFechaPago(pc.getFechaPago());
        dto.setEstado(pc.getEstado());
        dto.setDeleted(pc.getDeleted());
        return dto;
    }
}
