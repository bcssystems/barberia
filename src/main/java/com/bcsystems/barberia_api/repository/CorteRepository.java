package com.bcsystems.barberia_api.repository;

import com.bcsystems.barberia_api.domain.Corte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CorteRepository extends JpaRepository<Corte, Integer> {
    Optional<Corte> findTopByEstadoOrderByFechaRegistroDesc(String estado);
}
