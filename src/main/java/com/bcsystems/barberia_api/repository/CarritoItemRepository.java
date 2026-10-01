package com.bcsystems.barberia_api.repository;

import com.bcsystems.barberia_api.domain.CarritoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface CarritoItemRepository extends JpaRepository<CarritoItem, Integer> {

    List<CarritoItem> findByCajaIdCajaOrderByFechaAgregadoAsc(Integer idCaja);

    Optional<CarritoItem> findByCajaIdCajaAndTipoAndIdReferencia(Integer idCaja, String tipo, Integer idReferencia);

    void deleteByCajaIdCaja(Integer idCaja);

    @Transactional
    @Modifying
    @Query("DELETE FROM CarritoItem c WHERE c.caja.idCaja = :idCaja")
    void limpiarCarrito(@Param("idCaja") Integer idCaja);

}
