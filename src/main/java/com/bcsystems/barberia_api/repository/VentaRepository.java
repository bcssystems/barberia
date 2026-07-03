package com.bcsystems.barberia_api.repository;

import com.bcsystems.barberia_api.domain.Venta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Integer> {
    Page<Venta> findByCitaIdCita(Integer idCita, Pageable pageable);
    
    @Query("SELECT v FROM Venta v WHERE v.fecha BETWEEN :start AND :end")
    Page<Venta> findByFechaBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);
    
    @Query("SELECT v FROM Venta v WHERE v.fecha BETWEEN :start AND :end")
    List<Venta> findAllByFechaBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT v FROM Venta v WHERE v.fecha BETWEEN :start AND :end AND v.fechaCorte IS NULL")
    List<Venta> findAllByFechaBetweenAndFechaCorteIsNull(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT v FROM Venta v WHERE v.fecha BETWEEN :start AND :end AND v.fechaCorte IS NOT NULL AND v.fechaCorte = :fechaCorte")
    List<Venta> findAllByFechaBetweenAndFechaCorte(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("fechaCorte") LocalDateTime fechaCorte);
    
    @Query("SELECT SUM(v.total) FROM Venta v WHERE v.fecha BETWEEN :start AND :end AND v.fechaCorte IS NULL")
    Double sumTotalByFechaBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT SUM(vd.cantidad) FROM VentaDetalle vd WHERE vd.producto.idProducto = :idProducto AND vd.venta.fecha BETWEEN :start AND :end")
    Integer sumCantidadByProductoAndFechaBetween(@Param("idProducto") Integer idProducto, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT SUM(vd.precio * vd.cantidad) FROM VentaDetalle vd WHERE vd.producto.idProducto = :idProducto AND vd.venta.fecha BETWEEN :start AND :end")
    Double sumTotalByProductoAndFechaBetween(@Param("idProducto") Integer idProducto, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    Page<Venta> findByCajaIdCaja(Integer idCaja, Pageable pageable);
}
