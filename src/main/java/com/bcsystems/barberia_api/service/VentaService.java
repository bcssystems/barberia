package com.bcsystems.barberia_api.service;

import com.bcsystems.barberia_api.domain.Caja;
import com.bcsystems.barberia_api.domain.Cita;
import com.bcsystems.barberia_api.domain.CitaDetails;
import com.bcsystems.barberia_api.domain.Empleado;
import com.bcsystems.barberia_api.domain.MovimientoCaja;
import com.bcsystems.barberia_api.domain.Producto;
import com.bcsystems.barberia_api.domain.Servicio;
import com.bcsystems.barberia_api.domain.Venta;
import com.bcsystems.barberia_api.domain.VentaDetalle;
import com.bcsystems.barberia_api.domain.VentaPago;
import com.bcsystems.barberia_api.domain.en.MetodoPago;
import com.bcsystems.barberia_api.dto.VentaDetalleDTO;
import com.bcsystems.barberia_api.dto.VentaDTO;
import com.bcsystems.barberia_api.dto.VentaPagoDTO;
import com.bcsystems.barberia_api.repository.CajaRepository;
import com.bcsystems.barberia_api.repository.CarritoItemRepository;
import com.bcsystems.barberia_api.repository.CitaRepository;
import com.bcsystems.barberia_api.repository.EmpleadoRepository;
import com.bcsystems.barberia_api.repository.MovimientoCajaRepository;
import com.bcsystems.barberia_api.repository.ProductoRepository;
import com.bcsystems.barberia_api.repository.ServicioRepository;
import com.bcsystems.barberia_api.repository.VentaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VentaService {

    private final VentaRepository ventaRepository;
    private final CitaRepository citaRepository;
    private final ProductoRepository productoRepository;
    private final ServicioRepository servicioRepository;
    private final CajaRepository cajaRepository;
    private final MovimientoCajaRepository movimientoCajaRepository;
    private final CarritoItemRepository carritoItemRepository;
    private final EmpleadoRepository empleadoRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public VentaService(VentaRepository ventaRepository, CitaRepository citaRepository,
                        ProductoRepository productoRepository, ServicioRepository servicioRepository,
                        CajaRepository cajaRepository, MovimientoCajaRepository movimientoCajaRepository,
                        CarritoItemRepository carritoItemRepository,
                        EmpleadoRepository empleadoRepository) {
        this.ventaRepository = ventaRepository;
        this.citaRepository = citaRepository;
        this.productoRepository = productoRepository;
        this.servicioRepository = servicioRepository;
        this.cajaRepository = cajaRepository;
        this.movimientoCajaRepository = movimientoCajaRepository;
        this.carritoItemRepository = carritoItemRepository;
        this.empleadoRepository = empleadoRepository;
    }

    @Transactional(readOnly = true)
    public Page<VentaDTO> findAll(Pageable pageable) {
        return ventaRepository.findAll(pageable).map(this::toDTO);
    }

    /** Consulta paginada con los filtros del historial de ventas. */
    @Transactional(readOnly = true)
    public Page<VentaDTO> findAllFiltradas(Integer idEmpleado, Integer idCaja, Boolean cancelada,
                                           LocalDateTime desde, LocalDateTime hasta, Pageable pageable) {
        Specification<Venta> spec = specVentas(idEmpleado, idCaja, cancelada, desde, hasta);
        return ventaRepository.findAll(spec, pageable).map(this::toDTO);
    }

    /** Totales del conjunto filtrado completo (no solo la pagina actual). */
    @Transactional(readOnly = true)
    public Map<String, Object> resumenFiltrado(Integer idEmpleado, Integer idCaja, Boolean cancelada,
                                               LocalDateTime desde, LocalDateTime hasta) {
        Specification<Venta> spec = specVentas(idEmpleado, idCaja, cancelada, desde, hasta);

        long total = ventaRepository.count(spec);
        long activas = ventaRepository.count(spec.and((root, query, cb) -> predNoCancelada(cb, root)));
        long canceladas = ventaRepository.count(spec.and((root, query, cb) -> cb.isTrue(root.get("cancelada"))));

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Double> cq = cb.createQuery(Double.class);
        Root<Venta> root = cq.from(Venta.class);
        List<Predicate> preds = predVentas(cb, root, idEmpleado, idCaja, cancelada, desde, hasta);
        preds.add(predNoCancelada(cb, root));
        cq.select(cb.sum(root.get("total")));
        cq.where(preds.toArray(new Predicate[0]));
        Double monto = entityManager.createQuery(cq).getSingleResult();

        Map<String, Object> resumen = new HashMap<>();
        resumen.put("total", total);
        resumen.put("activas", activas);
        resumen.put("canceladas", canceladas);
        resumen.put("montoTotal", monto != null ? Math.round(monto * 100.0) / 100.0 : 0.0);
        return resumen;
    }

    private Specification<Venta> specVentas(Integer idEmpleado, Integer idCaja, Boolean cancelada,
                                            LocalDateTime desde, LocalDateTime hasta) {
        return (root, query, cb) -> cb.and(
                predVentas(cb, root, idEmpleado, idCaja, cancelada, desde, hasta)
                        .toArray(new Predicate[0]));
    }

    private List<Predicate> predVentas(CriteriaBuilder cb, Root<Venta> root, Integer idEmpleado,
                                       Integer idCaja, Boolean cancelada,
                                       LocalDateTime desde, LocalDateTime hasta) {
        List<Predicate> preds = new ArrayList<>();

        if (idEmpleado != null) {
            // El barbero puede venir de la venta (POS) o de la cita asociada
            preds.add(cb.or(
                    cb.equal(root.get("empleado").get("idEmpleado"), idEmpleado),
                    cb.equal(root.join("cita", JoinType.LEFT).get("empleado").get("idEmpleado"), idEmpleado)));
        }
        if (idCaja != null) {
            preds.add(cb.equal(root.get("caja").get("idCaja"), idCaja));
        }
        if (cancelada != null) {
            preds.add(cancelada ? cb.isTrue(root.get("cancelada")) : predNoCancelada(cb, root));
        }
        if (desde != null) {
            preds.add(cb.greaterThanOrEqualTo(root.get("fecha"), desde));
        }
        if (hasta != null) {
            preds.add(cb.lessThanOrEqualTo(root.get("fecha"), hasta));
        }
        return preds;
    }

    private Predicate predNoCancelada(CriteriaBuilder cb, Root<Venta> root) {
        return cb.or(cb.isNull(root.get("cancelada")), cb.isFalse(root.get("cancelada")));
    }

    @Transactional(readOnly = true)
    public Optional<VentaDTO> findById(Integer id) {
        return ventaRepository.findById(id).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<VentaDTO> findByCaja(Integer idCaja, Pageable pageable) {
        return ventaRepository.findByCajaIdCaja(idCaja, pageable).map(this::toDTO);
    }

    @Transactional
    public VentaDTO save(VentaDTO dto) {
        Venta venta = toEntity(dto);

        if (venta.getCita() != null && (venta.getDetalles() == null || venta.getDetalles().isEmpty())) {
            Cita cita = venta.getCita();
            if (cita.getDetalles() != null && !cita.getDetalles().isEmpty()) {
                List<VentaDetalle> detallesFromCita = new ArrayList<>();
                for (CitaDetails citaDetail : cita.getDetalles()) {
                    VentaDetalle vd = new VentaDetalle();
                    vd.setVenta(venta);
                    vd.setServicio(citaDetail.getServicio());
                    vd.setCantidad(1);
                    vd.setPrecio(citaDetail.getPrecio());
                    vd.setComisionPagada(false);
                    if (citaDetail.getServicio() != null) {
                        vd.setMontoComision(citaDetail.getServicio().getComision());
                    }
                    detallesFromCita.add(vd);
                }
                venta.setDetalles(detallesFromCita);
            }
        }

        if (venta.getDetalles() != null) {
            for (VentaDetalle vd : venta.getDetalles()) {
                if (vd.getServicio() != null) {
                    vd.setMontoComision(vd.getServicio().getComision());
                }
            }
        }

        double totalCalculado = calcularTotal(venta);
        venta.setSubtotal(totalCalculado);
        if (venta.getDescuento() != null && venta.getDescuento() > 0) {
            totalCalculado -= venta.getDescuento();
        }
        venta.setTotal(Math.max(0, totalCalculado));

        if (venta.getDetalles() != null) {
            for (VentaDetalle vd : venta.getDetalles()) {
                if (vd.getProducto() != null) {
                    Producto p = vd.getProducto();
                    if (p.getStock() < vd.getCantidad()) {
                        throw new RuntimeException("Stock insuficiente para " + p.getNombre()
                                + ". Disponible: " + p.getStock());
                    }
                }
            }
        }

        Venta saved = ventaRepository.save(venta);
        actualizarStock(saved);

        if (saved.getCaja() != null && saved.getTotal() != null && saved.getTotal() > 0) {
            registrarPagosYCaja(saved);
        }

        if (saved.getCaja() != null) {
            carritoItemRepository.limpiarCarrito(saved.getCaja().getIdCaja());
        }

        return toDTO(saved);
    }

    /**
     * Guarda los pagos de la venta y genera un MovimientoCaja por metodo de pago.
     * El saldo de la caja (efectivo fisico) solo se ve afectado por lo pagado en EFECTIVO.
     */
    private void registrarPagosYCaja(Venta saved) {
        List<VentaPago> pagos = saved.getPagos();
        if (pagos == null || pagos.isEmpty()) {
            // Sin detalle de pagos: se asume efectivo por el total
            MovimientoCaja mc = new MovimientoCaja();
            mc.setCaja(saved.getCaja());
            mc.setTipo("VENTA_EFECTIVO");
            mc.setMonto(saved.getTotal());
            mc.setMotivo("Venta #" + saved.getIdVenta());
            mc.setVenta(saved);
            movimientoCajaRepository.save(mc);
            sumarSaldoCaja(saved, saved.getTotal());
            return;
        }

        for (VentaPago pago : pagos) {
            if (pago.getMonto() == null || pago.getMonto() <= 0) continue;
            MovimientoCaja mc = new MovimientoCaja();
            mc.setCaja(saved.getCaja());
            mc.setTipo("VENTA_" + pago.getMetodoPago().name());
            mc.setMonto(pago.getMonto());
            mc.setMotivo("Venta #" + saved.getIdVenta() + " - " + pago.getMetodoPago().name()
                    + (pago.getReferencia() != null && !pago.getReferencia().isBlank()
                    ? " (ref: " + pago.getReferencia() + ")" : ""));
            mc.setVenta(saved);
            movimientoCajaRepository.save(mc);

            if (pago.getMetodoPago() == MetodoPago.EFECTIVO) {
                sumarSaldoCaja(saved, pago.getMonto());
            }
        }

        // El sobrante es cambio: sale del cajon
        double totalPagos = pagos.stream()
                .filter(p -> p.getMonto() != null && p.getMonto() > 0)
                .mapToDouble(VentaPago::getMonto).sum();
        double totalEfectivo = pagos.stream()
                .filter(p -> p.getMetodoPago() == MetodoPago.EFECTIVO && p.getMonto() != null && p.getMonto() > 0)
                .mapToDouble(VentaPago::getMonto).sum();
        double cambio = totalPagos - saved.getTotal();

        if (cambio > 0.001) {
            if (cambio > totalEfectivo + 0.001) {
                throw new RuntimeException("El pago en efectivo no cubre el cambio de la venta");
            }
            MovimientoCaja mcCambio = new MovimientoCaja();
            mcCambio.setCaja(saved.getCaja());
            mcCambio.setTipo("CAMBIO");
            mcCambio.setMonto(cambio);
            mcCambio.setMotivo("Cambio venta #" + saved.getIdVenta());
            mcCambio.setVenta(saved);
            movimientoCajaRepository.save(mcCambio);
            sumarSaldoCaja(saved, -cambio);
        }
    }

    /** El efectivo cobrado entra al saldo fisico de la caja. */
    private void sumarSaldoCaja(Venta saved, Double monto) {
        Caja caja = cajaRepository.findById(saved.getCaja().getIdCaja()).orElse(null);
        if (caja == null || monto == null) return;
        caja.setSaldoActual(caja.getSaldoActual() + monto);
        cajaRepository.save(caja);
    }

    @Transactional
    public VentaDTO update(Integer id, VentaDTO dto) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));

        if (dto.getIdCita() != null) {
            Cita cita = citaRepository.findById(dto.getIdCita()).orElse(null);
            venta.setCita(cita);
        }

        return toDTO(ventaRepository.save(venta));
    }

    @Transactional
    public void delete(Integer id) {
        if (!ventaRepository.existsById(id)) {
            throw new RuntimeException("Venta no encontrada");
        }
        ventaRepository.deleteById(id);
    }

    @Transactional
    public VentaDTO cancelarVenta(Integer id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));

        venta.setCancelada(true);

        if (venta.getDetalles() != null) {
            for (VentaDetalle detalle : venta.getDetalles()) {
                if (detalle.getProducto() != null) {
                    Producto producto = detalle.getProducto();
                    producto.setStock(producto.getStock() + detalle.getCantidad());
                    productoRepository.save(producto);
                }
            }
        }

        if (venta.getCaja() != null && venta.getTotal() != null && venta.getTotal() > 0) {
            // Reversión desmenuzada por metodo de pago: solo el efectivo sale del cajon
            List<VentaPago> pagos = venta.getPagos();
            boolean hayPagos = pagos != null && !pagos.isEmpty();
            double totalEfectivo = 0;

            if (hayPagos) {
                for (VentaPago pago : pagos) {
                    if (pago.getMonto() == null || pago.getMonto() <= 0) continue;
                    MovimientoCaja mc = new MovimientoCaja();
                    mc.setCaja(venta.getCaja());
                    mc.setTipo("ANULACION_" + pago.getMetodoPago().name());
                    mc.setMonto(pago.getMonto());
                    mc.setMotivo("Cancelacion venta #" + id + " - " + pago.getMetodoPago().name());
                    mc.setVenta(venta);
                    movimientoCajaRepository.save(mc);
                    if (pago.getMetodoPago() == MetodoPago.EFECTIVO) {
                        totalEfectivo += pago.getMonto();
                    }
                }
            } else {
                MovimientoCaja mc = new MovimientoCaja();
                mc.setCaja(venta.getCaja());
                mc.setTipo("ANULACION_EFECTIVO");
                mc.setMonto(venta.getTotal());
                mc.setMotivo("Cancelacion venta #" + id);
                mc.setVenta(venta);
                movimientoCajaRepository.save(mc);
                totalEfectivo = venta.getTotal();
            }

            if (totalEfectivo > 0) {
                Caja caja = cajaRepository.findById(venta.getCaja().getIdCaja()).orElse(null);
                if (caja != null) {
                    caja.setSaldoActual(Math.max(0, caja.getSaldoActual() - totalEfectivo));
                    cajaRepository.save(caja);
                }
            }
        }

        return toDTO(ventaRepository.save(venta));
    }

    @Transactional(readOnly = true)
    public Page<VentaDTO> findByFechaBetween(java.time.LocalDateTime start,
                                               java.time.LocalDateTime end, Pageable pageable) {
        return ventaRepository.findByFechaBetween(start, end, pageable).map(this::toDTO);
    }

    private double calcularTotal(Venta venta) {
        if (venta.getDetalles() == null) return 0.0;
        return venta.getDetalles().stream()
                .mapToDouble(d -> d.getPrecio() * (d.getCantidad() != null ? d.getCantidad() : 1))
                .sum();
    }

    /** Total esperado de la venta segun el DTO (para validar los pagos). */
    private double totalConDescuento(VentaDTO dto) {
        if (dto.getDetalles() == null) return 0.0;
        double subtotal = dto.getDetalles().stream()
                .mapToDouble(d -> {
                    Double precio = d.getPrecio() != null ? d.getPrecio() : 0;
                    int cant = d.getCantidad() != null ? d.getCantidad() : 1;
                    return precio * cant;
                })
                .sum();
        double descuento = dto.getDescuento() != null ? dto.getDescuento() : 0.0;
        return Math.max(0, subtotal - descuento);
    }

    private void actualizarStock(Venta venta) {
        if (venta.getDetalles() == null) return;
        for (VentaDetalle detalle : venta.getDetalles()) {
            if (detalle.getProducto() != null) {
                Producto producto = detalle.getProducto();
                int nuevaCantidad = producto.getStock() - detalle.getCantidad();
                producto.setStock(Math.max(0, nuevaCantidad));
                productoRepository.save(producto);
            }
        }
    }

    private VentaDTO toDTO(Venta venta) {
        List<VentaDetalleDTO> detallesDTO = new ArrayList<>();
        if (venta.getDetalles() != null) {
            detallesDTO = venta.getDetalles().stream()
                    .map(this::toDetalleDTO)
                    .collect(Collectors.toList());
        }

        VentaDTO dto = new VentaDTO();
        dto.setIdVenta(venta.getIdVenta());
        dto.setIdCita(venta.getCita() != null ? venta.getCita().getIdCita() : null);
        dto.setIdCaja(venta.getCaja() != null ? venta.getCaja().getIdCaja() : null);
        dto.setNombreCaja(venta.getCaja() != null ? venta.getCaja().getNombre() : null);
        dto.setNombreCliente(venta.getCita() != null && venta.getCita().getCliente() != null ? venta.getCita().getCliente().getNombre() : null);
        Empleado empleadoVenta = venta.getEmpleado() != null ? venta.getEmpleado()
                : (venta.getCita() != null ? venta.getCita().getEmpleado() : null);
        dto.setIdEmpleado(empleadoVenta != null ? empleadoVenta.getIdEmpleado() : null);
        dto.setNombreEmpleado(empleadoVenta != null ? empleadoVenta.getNombre() : null);
        dto.setFecha(venta.getFecha());
        dto.setTipoVenta(venta.getTipoVenta());
        dto.setSubtotal(venta.getSubtotal());
        dto.setDescuento(venta.getDescuento());
        dto.setTotal(venta.getTotal());
        dto.setCancelada(venta.getCancelada());
        dto.setFechaCorte(venta.getFechaCorte());
        dto.setDetalles(detallesDTO);

        List<VentaPagoDTO> pagosDTO = new ArrayList<>();
        if (venta.getPagos() != null) {
            pagosDTO = venta.getPagos().stream()
                    .map(this::toPagoDTO)
                    .collect(Collectors.toList());
        }
        dto.setPagos(pagosDTO);
        return dto;
    }

    private VentaPagoDTO toPagoDTO(VentaPago pago) {
        VentaPagoDTO dto = new VentaPagoDTO();
        dto.setIdVentaPago(pago.getIdVentaPago());
        dto.setIdVenta(pago.getVenta() != null ? pago.getVenta().getIdVenta() : null);
        dto.setMetodoPago(pago.getMetodoPago());
        dto.setMonto(pago.getMonto());
        dto.setReferencia(pago.getReferencia());
        dto.setFecha(pago.getFecha());
        return dto;
    }

    private VentaDetalleDTO toDetalleDTO(VentaDetalle detalle) {
        VentaDetalleDTO dto = new VentaDetalleDTO();
        dto.setIdVentaDetalle(detalle.getIdVentaDetalle());
        dto.setIdVenta(detalle.getVenta() != null ? detalle.getVenta().getIdVenta() : null);
        dto.setIdProducto(detalle.getProducto() != null ? detalle.getProducto().getIdProducto() : null);
        dto.setNombreProducto(detalle.getProducto() != null ? detalle.getProducto().getNombre() : null);
        dto.setSkuProducto(detalle.getProducto() != null ? detalle.getProducto().getSku() : null);
        dto.setIdServicio(detalle.getServicio() != null ? detalle.getServicio().getIdServicio() : null);
        dto.setNombreServicio(detalle.getServicio() != null ? detalle.getServicio().getNombre() : null);
        dto.setCantidad(detalle.getCantidad());
        dto.setPrecio(detalle.getPrecio());
        dto.setMontoComision(detalle.getMontoComision());
        dto.setComisionPagada(detalle.getComisionPagada());
        return dto;
    }

    private Venta toEntity(VentaDTO dto) {
        Venta venta = new Venta();

        if (dto.getIdCita() != null) {
            Cita cita = citaRepository.findById(dto.getIdCita()).orElse(null);
            venta.setCita(cita);
        }

        if (dto.getIdEmpleado() != null) {
            venta.setEmpleado(empleadoRepository.findById(dto.getIdEmpleado()).orElse(null));
        } else if (venta.getCita() != null && venta.getCita().getEmpleado() != null) {
            venta.setEmpleado(venta.getCita().getEmpleado());
        }

        if (dto.getIdCaja() != null) {
            Caja caja = cajaRepository.findById(dto.getIdCaja())
                    .orElseThrow(() -> new RuntimeException("Caja no encontrada"));
            if (!"ABIERTA".equals(caja.getEstado())) {
                throw new RuntimeException("La caja no esta abierta");
            }
            venta.setCaja(caja);
        }

        venta.setTipoVenta("CONTADO");
        venta.setSubtotal(null);
        venta.setDescuento(dto.getDescuento() != null ? dto.getDescuento() : 0.0);
        venta.setCancelada(dto.getCancelada() != null ? dto.getCancelada() : false);

        if (dto.getPagos() != null && !dto.getPagos().isEmpty()) {
            List<VentaPago> pagos = new ArrayList<>();
            double suma = 0;
            for (VentaPagoDTO pagoDTO : dto.getPagos()) {
                if (pagoDTO.getMonto() == null || pagoDTO.getMonto() <= 0) continue;
                if (pagoDTO.getMetodoPago() == null) continue;
                VentaPago pago = new VentaPago();
                pago.setVenta(venta);
                pago.setMetodoPago(pagoDTO.getMetodoPago());
                pago.setMonto(pagoDTO.getMonto());
                pago.setReferencia(pagoDTO.getReferencia() != null && !pagoDTO.getReferencia().isBlank()
                        ? pagoDTO.getReferencia().trim() : null);
                pago.setFecha(LocalDateTime.now());
                pagos.add(pago);
                suma += pagoDTO.getMonto();
            }
            if (!pagos.isEmpty()) {
                double totalEsperado = totalConDescuento(dto);
                if (suma + 0.01 < totalEsperado) {
                    throw new RuntimeException("La suma de los pagos es menor al total de la venta");
                }
                venta.setPagos(pagos);
            }
        }

        if (dto.getDetalles() != null && !dto.getDetalles().isEmpty()) {
            List<VentaDetalle> detalles = new ArrayList<>();
            for (VentaDetalleDTO detailDTO : dto.getDetalles()) {
                VentaDetalle detalle = new VentaDetalle();
                detalle.setVenta(venta);
                detalle.setCantidad(detailDTO.getCantidad() != null ? detailDTO.getCantidad() : 1);
                detalle.setPrecio(detailDTO.getPrecio());

                if (detailDTO.getIdProducto() != null) {
                    Producto producto = productoRepository.findById(detailDTO.getIdProducto())
                            .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
                    detalle.setProducto(producto);
                }
                if (detailDTO.getIdServicio() != null) {
                    Servicio servicio = servicioRepository.findById(detailDTO.getIdServicio())
                            .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));
                    detalle.setServicio(servicio);
                    detalle.setMontoComision(servicio.getComision());
                }
                detalles.add(detalle);
            }
            venta.setDetalles(detalles);
        }

        return venta;
    }
}
