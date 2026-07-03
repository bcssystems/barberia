package com.bcsystems.barberia_api.repository;

import com.bcsystems.barberia_api.domain.Caja;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CajaRepository extends JpaRepository<Caja, Integer> {
    List<Caja> findByEstado(String estado);
}
