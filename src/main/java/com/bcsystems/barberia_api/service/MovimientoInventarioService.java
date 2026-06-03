package com.bcsystems.barberia_api.service;

import com.bcsystems.barberia_api.domain.MovimientoInventario;
import com.bcsystems.barberia_api.domain.Producto;
import com.bcsystems.barberia_api.domain.en.TipoMovimiento;
import com.bcsystems.barberia_api.dto.MovimientoInventarioDTO;
import com.bcsystems.barberia_api.repository.MovimientoInventarioRepository;
import com.bcsystems.barberia_api.repository.ProductoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class MovimientoInventarioService {

    private final MovimientoInventarioRepository movimientoRepository;
    private final ProductoRepository productoRepository;

    public MovimientoInventarioService(MovimientoInventarioRepository movimientoRepository,
                                        ProductoRepository productoRepository) {
        this.movimientoRepository = movimientoRepository;
        this.productoRepository = productoRepository;
    }

    @Transactional(readOnly = true)
    public Page<MovimientoInventarioDTO> findAll(Pageable pageable) {
        return movimientoRepository.findAll(pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Optional<MovimientoInventarioDTO> findById(Integer id) {
        return movimientoRepository.findById(id).map(this::toDTO);
    }

    @Transactional
    public MovimientoInventarioDTO save(MovimientoInventarioDTO dto) {
        MovimientoInventario movimiento = toEntity(dto);
        MovimientoInventario saved = movimientoRepository.save(movimiento);
        actualizarStock(movimiento);
        return toDTO(saved);
    }

    @Transactional
    public void delete(Integer id) {
        if (!movimientoRepository.existsById(id)) {
            throw new RuntimeException("Movimiento no encontrado");
        }
        movimientoRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<MovimientoInventarioDTO> findByProducto(Integer idProducto, Pageable pageable) {
        return movimientoRepository.findByProductoIdProducto(idProducto, pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<MovimientoInventarioDTO> findByTipoMovimiento(TipoMovimiento tipo, Pageable pageable) {
        return movimientoRepository.findByTipoMovimiento(tipo, pageable).map(this::toDTO);
    }

    private void actualizarStock(MovimientoInventario movimiento) {
        Producto producto = movimiento.getProducto();
        int nuevoStock;
        if (movimiento.getTipoMovimiento() == TipoMovimiento.ENTRADA) {
            nuevoStock = producto.getStock() + movimiento.getCantidad();
        } else {
            nuevoStock = producto.getStock() - movimiento.getCantidad();
        }
        producto.setStock(Math.max(0, nuevoStock));
        productoRepository.save(producto);
    }

    private MovimientoInventarioDTO toDTO(MovimientoInventario movimiento) {
        MovimientoInventarioDTO dto = new MovimientoInventarioDTO();
        dto.setIdMovimientoInventario(movimiento.getIdMovimientoInventario());
        dto.setIdProducto(movimiento.getProducto() != null ? movimiento.getProducto().getIdProducto() : null);
        dto.setNombreProducto(movimiento.getProducto() != null ? movimiento.getProducto().getNombre() : null);
        dto.setCantidad(movimiento.getCantidad());
        dto.setTipoMovimiento(movimiento.getTipoMovimiento());
        dto.setFecha(movimiento.getFecha());
        dto.setMotivo(movimiento.getMotivo());
        dto.setPagoCaja(movimiento.getPagoCaja());
        return dto;
    }

    private MovimientoInventario toEntity(MovimientoInventarioDTO dto) {
        MovimientoInventario movimiento = new MovimientoInventario();
        
        if (dto.getIdProducto() != null) {
            Producto producto = productoRepository.findById(dto.getIdProducto())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
            movimiento.setProducto(producto);
        }
        movimiento.setCantidad(dto.getCantidad());
        movimiento.setTipoMovimiento(dto.getTipoMovimiento());
        movimiento.setMotivo(dto.getMotivo());
        movimiento.setPagoCaja(dto.getPagoCaja() != null ? dto.getPagoCaja() : false);
        return movimiento;
    }
}