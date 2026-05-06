package com.bcsystems.barberia_api.repository;

import com.bcsystems.barberia_api.domain.MovimientoInventario;
import com.bcsystems.barberia_api.domain.en.TipoMovimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Integer> {
    Page<MovimientoInventario> findByProductoIdProducto(Integer idProducto, Pageable pageable);
    
    @Query("SELECT m FROM MovimientoInventario m WHERE m.fecha BETWEEN :start AND :end")
    Page<MovimientoInventario> findByFechaBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);
    
    Page<MovimientoInventario> findByTipoMovimiento(TipoMovimiento tipoMovimiento, Pageable pageable);
    
    @Query("SELECT SUM(m.producto.precioCompra * m.cantidad) FROM MovimientoInventario m " +
           "WHERE m.tipoMovimiento = 'ENTRADA' " +
           "AND m.fecha BETWEEN :start AND :end AND m.producto.precioCompra IS NOT NULL")
    Double sumComprasByFechaBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}