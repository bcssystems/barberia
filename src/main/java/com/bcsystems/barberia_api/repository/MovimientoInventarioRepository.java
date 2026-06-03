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
import java.util.List;

@Repository
public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Integer> {
    Page<MovimientoInventario> findByProductoIdProducto(Integer idProducto, Pageable pageable);
    
    @Query("SELECT m FROM MovimientoInventario m WHERE m.fecha BETWEEN :start AND :end")
    Page<MovimientoInventario> findByFechaBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);

    @Query("SELECT m FROM MovimientoInventario m WHERE m.fecha BETWEEN :start AND :end AND m.fechaCorte IS NULL")
    List<MovimientoInventario> findAllByFechaBetweenAndFechaCorteIsNull(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT m FROM MovimientoInventario m WHERE m.fecha BETWEEN :start AND :end AND m.fechaCorte IS NOT NULL")
    List<MovimientoInventario> findAllByFechaBetweenAndFechaCorteIsNotNull(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    Page<MovimientoInventario> findByTipoMovimiento(TipoMovimiento tipoMovimiento, Pageable pageable);
    
    @Query("SELECT SUM(m.producto.precioCompra * m.cantidad) FROM MovimientoInventario m " +
           "WHERE m.tipoMovimiento = 'ENTRADA' " +
           "AND m.fecha BETWEEN :start AND :end AND m.producto.precioCompra IS NOT NULL " +
           "AND m.pagoCaja = true AND m.fechaCorte IS NULL")
    Double sumComprasByFechaBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT SUM(m.producto.precioCompra * m.cantidad) FROM MovimientoInventario m " +
           "WHERE m.tipoMovimiento = 'ENTRADA' " +
           "AND m.fecha BETWEEN :start AND :end AND m.producto.precioCompra IS NOT NULL " +
           "AND (m.pagoCaja IS NULL OR m.pagoCaja = false)")
    Double sumComprasNoCajaByFechaBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
