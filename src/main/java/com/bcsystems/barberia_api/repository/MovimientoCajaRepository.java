package com.bcsystems.barberia_api.repository;

import com.bcsystems.barberia_api.domain.MovimientoCaja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MovimientoCajaRepository extends JpaRepository<MovimientoCaja, Integer> {
    List<MovimientoCaja> findByCajaIdCajaOrderByFechaDesc(Integer idCaja);

    List<MovimientoCaja> findByCajaIdCajaAndFechaCorteIsNull(Integer idCaja);

    @Query("SELECT COALESCE(SUM(m.monto), 0) FROM MovimientoCaja m WHERE m.caja.idCaja = :idCaja AND m.tipo = :tipo AND m.fechaCorte IS NULL")
    Double sumByCajaAndTipoAndFechaCorteIsNull(@Param("idCaja") Integer idCaja, @Param("tipo") String tipo);
}
