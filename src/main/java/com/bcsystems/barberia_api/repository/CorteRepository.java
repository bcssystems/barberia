package com.bcsystems.barberia_api.repository;

import com.bcsystems.barberia_api.domain.Corte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CorteRepository extends JpaRepository<Corte, Integer>, JpaSpecificationExecutor<Corte> {
    Optional<Corte> findTopByEstadoOrderByFechaRegistroDesc(String estado);

    List<Corte> findAllByOrderByFechaRegistroDesc();
}
