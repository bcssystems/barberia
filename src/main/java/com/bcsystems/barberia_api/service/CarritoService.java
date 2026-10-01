package com.bcsystems.barberia_api.service;

import com.bcsystems.barberia_api.domain.CarritoItem;
import com.bcsystems.barberia_api.domain.Caja;
import com.bcsystems.barberia_api.domain.Producto;
import com.bcsystems.barberia_api.domain.Servicio;
import com.bcsystems.barberia_api.dto.CarritoItemDTO;
import com.bcsystems.barberia_api.repository.CajaRepository;
import com.bcsystems.barberia_api.repository.CarritoItemRepository;
import com.bcsystems.barberia_api.repository.ProductoRepository;
import com.bcsystems.barberia_api.repository.ServicioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CarritoService {

    public static final String TIPO_PRODUCTO = "PRODUCTO";
    public static final String TIPO_SERVICIO = "SERVICIO";

    private final CarritoItemRepository carritoItemRepository;
    private final CajaRepository cajaRepository;
    private final ProductoRepository productoRepository;
    private final ServicioRepository servicioRepository;

    public CarritoService(CarritoItemRepository carritoItemRepository,
                          CajaRepository cajaRepository,
                          ProductoRepository productoRepository,
                          ServicioRepository servicioRepository) {
        this.carritoItemRepository = carritoItemRepository;
        this.cajaRepository = cajaRepository;
        this.productoRepository = productoRepository;
        this.servicioRepository = servicioRepository;
    }

    @Transactional(readOnly = true)
    public List<CarritoItemDTO> findByCaja(Integer idCaja) {
        return carritoItemRepository.findByCajaIdCajaOrderByFechaAgregadoAsc(idCaja).stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public List<CarritoItemDTO> agregar(Integer idCaja, String tipo, Integer idReferencia, Integer cantidad, Double precioUnitario) {
        Caja caja = obtenerCaja(idCaja);
        String tipoNormalizado = normalizarTipo(tipo);
        int cantidadValidada = cantidad != null && cantidad > 0 ? cantidad : 1;

        Double precio = precioUnitario != null ? precioUnitario : precioDeCatalogo(tipoNormalizado, idReferencia);

        CarritoItem item = carritoItemRepository
                .findByCajaIdCajaAndTipoAndIdReferencia(caja.getIdCaja(), tipoNormalizado, idReferencia)
                .orElseGet(() -> {
                    CarritoItem nuevo = new CarritoItem();
                    nuevo.setCaja(caja);
                    nuevo.setTipo(tipoNormalizado);
                    nuevo.setIdReferencia(idReferencia);
                    nuevo.setCantidad(0);
                    nuevo.setPrecioUnitario(0.0);
                    nuevo.setFechaAgregado(LocalDateTime.now());
                    return nuevo;
                });

        item.setCantidad(item.getCantidad() + cantidadValidada);
        item.setPrecioUnitario(precio);
        item.setFechaAgregado(LocalDateTime.now());

        carritoItemRepository.save(item);
        return findByCaja(caja.getIdCaja());
    }

    @Transactional
    public List<CarritoItemDTO> actualizar(Integer idCaja, String tipo, Integer idReferencia, Integer cantidad) {
        obtenerCaja(idCaja);
        String tipoNormalizado = normalizarTipo(tipo);

        CarritoItem item = carritoItemRepository
                .findByCajaIdCajaAndTipoAndIdReferencia(idCaja, tipoNormalizado, idReferencia)
                .orElseThrow(() -> new RuntimeException("Esa línea no está en el carrito"));

        if (cantidad == null || cantidad <= 0) {
            carritoItemRepository.delete(item);
            return findByCaja(idCaja);
        }

        item.setCantidad(cantidad);
        item.setFechaAgregado(LocalDateTime.now());
        carritoItemRepository.save(item);
        return findByCaja(idCaja);
    }

    @Transactional
    public void quitar(Integer idCaja, String tipo, Integer idReferencia) {
        carritoItemRepository
                .findByCajaIdCajaAndTipoAndIdReferencia(idCaja, normalizarTipo(tipo), idReferencia)
                .ifPresent(carritoItemRepository::delete);
    }

    @Transactional
    public void limpiar(Integer idCaja) {
        carritoItemRepository.limpiarCarrito(idCaja);
    }

    @Transactional
    public List<CarritoItemDTO> sincronizar(Integer idCaja, List<LineaRecibida> lineas) {
        obtenerCaja(idCaja);
        carritoItemRepository.limpiarCarrito(idCaja);

        if (lineas != null) {
            for (LineaRecibida linea : lineas) {
                if (linea == null || linea.idReferencia() == null) {
                    continue;
                }
                agregar(idCaja, linea.tipo(), linea.idReferencia(), linea.cantidad(), linea.precioUnitario());
            }
        }
        return findByCaja(idCaja);
    }

    private Caja obtenerCaja(Integer idCaja) {
        if (idCaja == null) {
            throw new RuntimeException("Debes tener una caja abierta");
        }
        return cajaRepository.findById(idCaja)
                .orElseThrow(() -> new RuntimeException("Caja no encontrada"));
    }

    private String normalizarTipo(String tipo) {
        if (tipo == null || tipo.isBlank()) {
            throw new RuntimeException("Debes indicar el tipo de línea");
        }
        String normalizado = tipo.trim().toUpperCase();
        if (!TIPO_PRODUCTO.equals(normalizado) && !TIPO_SERVICIO.equals(normalizado)) {
            throw new RuntimeException("Tipo de línea inválido: " + tipo);
        }
        return normalizado;
    }

    private Double precioDeCatalogo(String tipo, Integer idReferencia) {
        if (TIPO_PRODUCTO.equals(tipo)) {
            Producto producto = productoRepository.findById(idReferencia)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
            return producto.getPrecioVenta() != null ? producto.getPrecioVenta() : 0.0;
        }
        Servicio servicio = servicioRepository.findById(idReferencia)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));
        return servicio.getPrecio() != null ? servicio.getPrecio() : 0.0;
    }

    private CarritoItemDTO toDTO(CarritoItem item) {
        CarritoItemDTO dto = new CarritoItemDTO();
        dto.setIdCarritoItem(item.getIdCarritoItem());
        dto.setIdCaja(item.getCaja() != null ? item.getCaja().getIdCaja() : null);
        dto.setTipo(item.getTipo());
        dto.setIdReferencia(item.getIdReferencia());
        dto.setNombre(nombreDeCatalogo(item.getTipo(), item.getIdReferencia()));
        dto.setCantidad(item.getCantidad());
        dto.setPrecioUnitario(item.getPrecioUnitario());
        dto.setSubtotal(item.getCantidad() * item.getPrecioUnitario());
        return dto;
    }

    private String nombreDeCatalogo(String tipo, Integer idReferencia) {
        if (TIPO_PRODUCTO.equals(tipo)) {
            return productoRepository.findById(idReferencia).map(Producto::getNombre).orElse("Producto");
        }
        return servicioRepository.findById(idReferencia).map(Servicio::getNombre).orElse("Servicio");
    }

    public record LineaRecibida(String tipo, Integer idReferencia, Integer cantidad, Double precioUnitario) {
    }

}
