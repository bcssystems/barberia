package com.bcsystems.barberia_api.service;

import com.bcsystems.barberia_api.domain.Caja;
import com.bcsystems.barberia_api.domain.Cita;
import com.bcsystems.barberia_api.domain.CitaDetails;
import com.bcsystems.barberia_api.domain.MovimientoCaja;
import com.bcsystems.barberia_api.domain.Producto;
import com.bcsystems.barberia_api.domain.Servicio;
import com.bcsystems.barberia_api.domain.Venta;
import com.bcsystems.barberia_api.domain.VentaDetalle;
import com.bcsystems.barberia_api.dto.VentaDetalleDTO;
import com.bcsystems.barberia_api.dto.VentaDTO;
import com.bcsystems.barberia_api.repository.CajaRepository;
import com.bcsystems.barberia_api.repository.CitaRepository;
import com.bcsystems.barberia_api.repository.MovimientoCajaRepository;
import com.bcsystems.barberia_api.repository.ProductoRepository;
import com.bcsystems.barberia_api.repository.ServicioRepository;
import com.bcsystems.barberia_api.repository.VentaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
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

    public VentaService(VentaRepository ventaRepository, CitaRepository citaRepository,
                        ProductoRepository productoRepository, ServicioRepository servicioRepository,
                        CajaRepository cajaRepository, MovimientoCajaRepository movimientoCajaRepository) {
        this.ventaRepository = ventaRepository;
        this.citaRepository = citaRepository;
        this.productoRepository = productoRepository;
        this.servicioRepository = servicioRepository;
        this.cajaRepository = cajaRepository;
        this.movimientoCajaRepository = movimientoCajaRepository;
    }

    @Transactional(readOnly = true)
    public Page<VentaDTO> findAll(Pageable pageable) {
        return ventaRepository.findAll(pageable).map(this::toDTO);
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
            MovimientoCaja mc = new MovimientoCaja();
            mc.setCaja(saved.getCaja());
            mc.setTipo("CONTADO".equals(saved.getTipoVenta()) ? "VENTA_CONTADO" : "VENTA_CREDITO");
            mc.setMonto(saved.getTotal());
            mc.setVenta(saved);
            movimientoCajaRepository.save(mc);
        }

        return toDTO(saved);
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
            MovimientoCaja mc = new MovimientoCaja();
            mc.setCaja(venta.getCaja());
            mc.setTipo("EGRESO");
            mc.setMonto(venta.getTotal());
            mc.setMotivo("Cancelacion venta #" + id);
            mc.setVenta(venta);
            movimientoCajaRepository.save(mc);
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
        dto.setNombreEmpleado(venta.getCita() != null && venta.getCita().getEmpleado() != null ? venta.getCita().getEmpleado().getNombre() : null);
        dto.setFecha(venta.getFecha());
        dto.setTipoVenta(venta.getTipoVenta());
        dto.setSubtotal(venta.getSubtotal());
        dto.setDescuento(venta.getDescuento());
        dto.setTotal(venta.getTotal());
        dto.setCancelada(venta.getCancelada());
        dto.setFechaCorte(venta.getFechaCorte());
        dto.setDetalles(detallesDTO);
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

        if (dto.getIdCaja() != null) {
            Caja caja = cajaRepository.findById(dto.getIdCaja())
                    .orElseThrow(() -> new RuntimeException("Caja no encontrada"));
            if (!"ABIERTA".equals(caja.getEstado())) {
                throw new RuntimeException("La caja no esta abierta");
            }
            venta.setCaja(caja);
        }

        venta.setTipoVenta(dto.getTipoVenta() != null ? dto.getTipoVenta() : "CONTADO");
        venta.setSubtotal(null);
        venta.setDescuento(dto.getDescuento() != null ? dto.getDescuento() : 0.0);
        venta.setCancelada(dto.getCancelada() != null ? dto.getCancelada() : false);

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
