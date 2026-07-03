package com.bcsystems.barberia_api.service;

import com.bcsystems.barberia_api.domain.Cita;
import com.bcsystems.barberia_api.domain.Empleado;
import com.bcsystems.barberia_api.domain.PagoComision;
import com.bcsystems.barberia_api.domain.Servicio;
import com.bcsystems.barberia_api.domain.Venta;
import com.bcsystems.barberia_api.domain.VentaDetalle;
import com.bcsystems.barberia_api.domain.en.EstadoPago;
import com.bcsystems.barberia_api.dto.CorteCompletoDTO;
import com.bcsystems.barberia_api.dto.CorteComisionDTO;
import com.bcsystems.barberia_api.dto.DesgloseComisionDTO;
import com.bcsystems.barberia_api.dto.DetalleComisionDTO;
import com.bcsystems.barberia_api.dto.PagoComisionDTO;
import com.bcsystems.barberia_api.dto.ResumenComisionEmpleadoDTO;
import com.bcsystems.barberia_api.repository.CitaRepository;
import com.bcsystems.barberia_api.repository.EmpleadoRepository;
import com.bcsystems.barberia_api.repository.MovimientoInventarioRepository;
import com.bcsystems.barberia_api.repository.PagoComisionRepository;
import com.bcsystems.barberia_api.repository.ServicioRepository;
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
    private final ServicioRepository servicioRepository;

    public ComisionService(PagoComisionRepository pagoComisionRepository,
                           EmpleadoRepository empleadoRepository,
                           VentaRepository ventaRepository,
                           VentaDetalleRepository ventaDetalleRepository,
                           CitaRepository citaRepository,
                           MovimientoInventarioRepository movimientoInventarioRepository,
                           ServicioRepository servicioRepository) {
        this.pagoComisionRepository = pagoComisionRepository;
        this.empleadoRepository = empleadoRepository;
        this.ventaRepository = ventaRepository;
        this.ventaDetalleRepository = ventaDetalleRepository;
        this.citaRepository = citaRepository;
        this.movimientoInventarioRepository = movimientoInventarioRepository;
        this.servicioRepository = servicioRepository;
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
                .orElseThrow(() -> new RuntimeException("Pago de comision no encontrado"));
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

        Map<Integer, List<DetalleComisionDTO>> detallesPendientesPorEmp = new HashMap<>();
        Map<Integer, List<DetalleComisionDTO>> detallesPagadosPorEmp = new HashMap<>();

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
            Servicio servicio = vd.getServicio();
            int cantidad = vd.getCantidad() != null ? vd.getCantidad() : 1;
            double comision = servicio.getComision() * cantidad;

            DetalleComisionDTO detalle = new DetalleComisionDTO();
            detalle.setIdVentaDetalle(vd.getIdVentaDetalle());
            detalle.setIdVenta(venta.getIdVenta());
            detalle.setIdServicio(servicio.getIdServicio());
            detalle.setNombreServicio(servicio.getNombre());
            detalle.setPrecioServicio(vd.getPrecio());
            detalle.setMontoComision(comision);
            detalle.setFechaVenta(fechaVenta);
            detalle.setPagada(vd.getComisionPagada() != null && vd.getComisionPagada());

            if (vd.getComisionPagada() != null && vd.getComisionPagada()) {
                detallesPagadosPorEmp.computeIfAbsent(empId, k -> new ArrayList<>()).add(detalle);
            } else {
                detallesPendientesPorEmp.computeIfAbsent(empId, k -> new ArrayList<>()).add(detalle);
            }
        }

        List<Empleado> empleados = empleadoRepository.findAll();
        List<ResumenComisionEmpleadoDTO> resumenList = new ArrayList<>();
        double totalComisionesPendientes = 0;
        double totalPagadas = 0;

        for (Empleado emp : empleados) {
            if (emp.getStatus() != 1) continue;
            if (emp.getCobraComision() == null || !emp.getCobraComision()) continue;

            List<DetalleComisionDTO> pendientes = detallesPendientesPorEmp.get(emp.getIdEmpleado());
            List<DetalleComisionDTO> pagados = detallesPagadosPorEmp.get(emp.getIdEmpleado());

            double montoPendiente = pendientes != null ? pendientes.stream().mapToDouble(DetalleComisionDTO::getMontoComision).sum() : 0;
            double montoPagado = pagados != null ? pagados.stream().mapToDouble(DetalleComisionDTO::getMontoComision).sum() : 0;
            double totalServiciosVal = (pendientes != null ? pendientes.stream().mapToDouble(d -> d.getPrecioServicio()).sum() : 0)
                                     + (pagados != null ? pagados.stream().mapToDouble(d -> d.getPrecioServicio()).sum() : 0);

            if (montoPendiente == 0 && montoPagado == 0) continue;

            totalComisionesPendientes += montoPendiente;
            totalPagadas += montoPagado;

            ResumenComisionEmpleadoDTO resumen = new ResumenComisionEmpleadoDTO();
            resumen.setIdEmpleado(emp.getIdEmpleado());
            resumen.setNombreEmpleado(emp.getNombre());
            resumen.setTotalVentasServicios(totalServiciosVal);
            resumen.setMontoComision(montoPendiente + montoPagado);
            resumen.setTienePagoPendiente(montoPendiente > 0);

            List<DetalleComisionDTO> desglose = new ArrayList<>();
            if (pendientes != null) desglose.addAll(pendientes);
            if (pagados != null) desglose.addAll(pagados);
            resumen.setDesglose(desglose);

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

        Double totalVentasServicios = ventaDetalleRepository.sumServiciosByFechaBetween(fechaInicio, fechaFin);
        Double totalVentasProductos = ventaDetalleRepository.sumTotalProductosByFechaBetween(fechaInicio, fechaFin);
        totalVentasServicios = totalVentasServicios != null ? totalVentasServicios : 0.0;
        totalVentasProductos = totalVentasProductos != null ? totalVentasProductos : 0.0;

        corte.setTotalVentasServicios(totalVentasServicios);
        corte.setTotalVentasProductos(totalVentasProductos);
        corte.setTotalVentas(totalVentasServicios + totalVentasProductos);

        Double costoProductosVendidos = ventaDetalleRepository.sumCostoProductosByFechaBetween(fechaInicio, fechaFin);
        costoProductosVendidos = costoProductosVendidos != null ? costoProductosVendidos : 0.0;
        corte.setCostoProductosVendidos(costoProductosVendidos);

        Double gastosInventario = movimientoInventarioRepository.sumComprasByFechaBetween(fechaInicio, fechaFin);
        gastosInventario = gastosInventario != null ? gastosInventario : 0.0;
        corte.setGastosInventario(gastosInventario);

        Double totalCostos = costoProductosVendidos + gastosInventario;
        corte.setTotalCostos(totalCostos);

        Double utilidadBruta = (totalVentasServicios + totalVentasProductos) - totalCostos;
        corte.setUtilidadBruta(utilidadBruta);

        List<VentaDetalle> todosDetalles = ventaDetalleRepository.findAll();

        Map<Integer, List<DetalleComisionDTO>> detallesPendientesPorEmp = new HashMap<>();
        Map<Integer, List<DetalleComisionDTO>> detallesPagadosPorEmp = new HashMap<>();

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
            Servicio servicio = vd.getServicio();
            int cantidad = vd.getCantidad() != null ? vd.getCantidad() : 1;
            double comision = servicio.getComision() * cantidad;

            DetalleComisionDTO detalle = new DetalleComisionDTO();
            detalle.setIdVentaDetalle(vd.getIdVentaDetalle());
            detalle.setIdVenta(venta.getIdVenta());
            detalle.setIdServicio(servicio.getIdServicio());
            detalle.setNombreServicio(servicio.getNombre());
            detalle.setPrecioServicio(vd.getPrecio());
            detalle.setMontoComision(comision);
            detalle.setFechaVenta(fechaVenta);
            detalle.setPagada(vd.getComisionPagada() != null && vd.getComisionPagada());

            if (vd.getComisionPagada() != null && vd.getComisionPagada()) {
                detallesPagadosPorEmp.computeIfAbsent(empId, k -> new ArrayList<>()).add(detalle);
            } else {
                detallesPendientesPorEmp.computeIfAbsent(empId, k -> new ArrayList<>()).add(detalle);
            }
        }

        List<Empleado> empleados = empleadoRepository.findAll();
        List<ResumenComisionEmpleadoDTO> resumenList = new ArrayList<>();
        double totalComisionesPendientes = 0;
        double totalPagadas = 0;

        for (Empleado emp : empleados) {
            if (emp.getStatus() != 1) continue;
            if (emp.getCobraComision() == null || !emp.getCobraComision()) continue;

            List<DetalleComisionDTO> pendientes = detallesPendientesPorEmp.get(emp.getIdEmpleado());
            List<DetalleComisionDTO> pagados = detallesPagadosPorEmp.get(emp.getIdEmpleado());

            double montoPendiente = pendientes != null ? pendientes.stream().mapToDouble(DetalleComisionDTO::getMontoComision).sum() : 0;
            double montoPagado = pagados != null ? pagados.stream().mapToDouble(DetalleComisionDTO::getMontoComision).sum() : 0;
            double totalServiciosVal = (pendientes != null ? pendientes.stream().mapToDouble(d -> d.getPrecioServicio()).sum() : 0)
                                     + (pagados != null ? pagados.stream().mapToDouble(d -> d.getPrecioServicio()).sum() : 0);

            if (montoPendiente == 0 && montoPagado == 0) continue;

            totalComisionesPendientes += montoPendiente;
            totalPagadas += montoPagado;

            ResumenComisionEmpleadoDTO resumen = new ResumenComisionEmpleadoDTO();
            resumen.setIdEmpleado(emp.getIdEmpleado());
            resumen.setNombreEmpleado(emp.getNombre());
            resumen.setTotalVentasServicios(totalServiciosVal);
            resumen.setMontoComision(montoPendiente + montoPagado);
            resumen.setTienePagoPendiente(montoPendiente > 0);

            List<DetalleComisionDTO> desglose = new ArrayList<>();
            if (pendientes != null) desglose.addAll(pendientes);
            if (pagados != null) desglose.addAll(pagados);
            resumen.setDesglose(desglose);

            resumenList.add(resumen);
        }

        corte.setComisionesPorEmpleado(resumenList);
        corte.setTotalComisionesPendientes(totalComisionesPendientes);
        corte.setTotalComisionesPagadas(totalPagadas);

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

            if (emp.getCobraComision() == null || !emp.getCobraComision()) continue;

            List<VentaDetalle> todosDetalles = ventaDetalleRepository.findAll();
            double totalComisionEmpleado = 0;

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

                Servicio servicio = vd.getServicio();
                int cantidad = vd.getCantidad() != null ? vd.getCantidad() : 1;
                double comision = servicio.getComision() * cantidad;

                vd.setMontoComision(servicio.getComision());
                vd.setComisionPagada(true);
                ventaDetalleRepository.save(vd);

                totalComisionEmpleado += comision;
            }

            if (totalComisionEmpleado == 0) continue;

            PagoComision pago = new PagoComision();
            pago.setEmpleado(emp);
            pago.setMontoComision(totalComisionEmpleado);
            pago.setFechaCorteInicio(fechaCorteInicio);
            pago.setFechaCorteFin(fechaCorteFin);
            pago.setFechaPago(LocalDateTime.now());
            pago.setEstado(EstadoPago.PAGADA);
            pago.setDeleted(false);

            pagoComisionRepository.save(pago);
            totalPagado += totalComisionEmpleado;
        }

        PagoComisionDTO result = new PagoComisionDTO();
        result.setMontoComision(totalPagado);
        result.setEstado(EstadoPago.PAGADA);
        return result;
    }

    @Transactional(readOnly = true)
    public DesgloseComisionDTO generarDesglose(Integer idEmpleado, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        Empleado emp = empleadoRepository.findById(idEmpleado)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

        List<VentaDetalle> todosDetalles = ventaDetalleRepository.findAll();
        List<DetalleComisionDTO> detalles = new ArrayList<>();

        for (VentaDetalle vd : todosDetalles) {
            if (vd.getServicio() == null) continue;
            if (vd.getVenta() == null) continue;

            Venta venta = vd.getVenta();
            if (venta.getCita() == null) continue;

            Cita cita = venta.getCita();
            if (cita.getEmpleado() == null) continue;
            if (!cita.getEmpleado().getIdEmpleado().equals(idEmpleado)) continue;

            LocalDateTime fechaVenta = venta.getFecha();
            if (fechaVenta != null && (fechaVenta.isBefore(fechaInicio) || fechaVenta.isAfter(fechaFin))) continue;

            Servicio servicio = vd.getServicio();
            int cantidad = vd.getCantidad() != null ? vd.getCantidad() : 1;
            double comision = servicio.getComision() * cantidad;

            DetalleComisionDTO detalle = new DetalleComisionDTO();
            detalle.setIdVentaDetalle(vd.getIdVentaDetalle());
            detalle.setIdVenta(venta.getIdVenta());
            detalle.setIdServicio(servicio.getIdServicio());
            detalle.setNombreServicio(servicio.getNombre());
            detalle.setPrecioServicio(vd.getPrecio());
            detalle.setMontoComision(comision);
            detalle.setFechaVenta(fechaVenta);
            detalle.setPagada(vd.getComisionPagada() != null && vd.getComisionPagada());

            detalles.add(detalle);
        }

        double totalComision = detalles.stream().mapToDouble(DetalleComisionDTO::getMontoComision).sum();

        DesgloseComisionDTO desglose = new DesgloseComisionDTO();
        desglose.setIdEmpleado(emp.getIdEmpleado());
        desglose.setNombreEmpleado(emp.getNombre());
        desglose.setDetalles(detalles);
        desglose.setTotalComision(totalComision);

        return desglose;
    }

    @Transactional
    public PagoComisionDTO update(Integer id, PagoComisionDTO dto) {
        PagoComision pago = pagoComisionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pago de comision no encontrado"));

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
                .orElseThrow(() -> new RuntimeException("Pago de comision no encontrado"));
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
