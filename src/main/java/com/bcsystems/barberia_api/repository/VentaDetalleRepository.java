package com.bcsystems.barberia_api.repository;

import com.bcsystems.barberia_api.domain.VentaDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VentaDetalleRepository extends JpaRepository<VentaDetalle, Integer> {

    @Query("SELECT vd.producto.idProducto, vd.producto.nombre, SUM(vd.cantidad), SUM(vd.precio * vd.cantidad) " +
           "FROM VentaDetalle vd WHERE vd.venta.fecha BETWEEN :start AND :end AND vd.producto IS NOT NULL " +
           "AND vd.venta.fechaCorte IS NULL " +
           "GROUP BY vd.producto.idProducto, vd.producto.nombre " +
           "ORDER BY SUM(vd.cantidad) DESC")
    List<Object[]> findProductosMasVendidosBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT SUM(vd.precio * vd.cantidad) FROM VentaDetalle vd WHERE vd.servicio IS NOT NULL AND vd.venta.fecha BETWEEN :start AND :end AND vd.venta.fechaCorte IS NULL")
    Double sumServiciosByFechaBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT SUM(vd.precio * vd.cantidad) FROM VentaDetalle vd WHERE vd.producto IS NOT NULL AND vd.venta.fecha BETWEEN :start AND :end AND vd.venta.fechaCorte IS NULL")
    Double sumTotalProductosByFechaBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT SUM(vd.producto.precioCompra * vd.cantidad) FROM VentaDetalle vd WHERE vd.producto IS NOT NULL AND vd.producto.precioCompra IS NOT NULL AND vd.venta.fecha BETWEEN :start AND :end AND vd.venta.fechaCorte IS NULL")
    Double sumCostoProductosByFechaBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(vd.montoComision), 0) FROM VentaDetalle vd WHERE vd.venta.fecha BETWEEN :start AND :end AND vd.venta.fechaCorte IS NULL")
    Double sumComisionesByFechaBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
