package com.bcsystems.barberia_api.repository;

import com.bcsystems.barberia_api.domain.VentaPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VentaPagoRepository extends JpaRepository<VentaPago, Integer> {
    List<VentaPago> findByVentaIdVenta(Integer idVenta);
}
