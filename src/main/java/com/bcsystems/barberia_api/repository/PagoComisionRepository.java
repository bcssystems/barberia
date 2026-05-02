package com.bcsystems.barberia_api.repository;

import com.bcsystems.barberia_api.domain.PagoComision;
import com.bcsystems.barberia_api.domain.en.EstadoPago;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PagoComisionRepository extends JpaRepository<PagoComision, Integer> {

    Page<PagoComision> findByDeletedFalse(Pageable pageable);

    List<PagoComision> findByEmpleadoIdEmpleadoAndDeletedFalse(Integer idEmpleado);

    List<PagoComision> findByEstadoAndDeletedFalse(EstadoPago estado);

    List<PagoComision> findByEmpleadoIdEmpleadoAndEstadoAndDeletedFalse(Integer idEmpleado, EstadoPago estado);

    @Query("SELECT SUM(pc.montoComision) FROM PagoComision pc WHERE pc.estado = 'PAGADA' AND pc.deleted = false AND pc.fechaPago BETWEEN :inicio AND :fin")
    Double sumComisionesPagadas(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    @Query("SELECT SUM(pc.montoComision) FROM PagoComision pc WHERE pc.estado = 'PENDIENTE' AND pc.deleted = false")
    Double sumComisionesPendientes();
}
