package com.bcsystems.barberia_api.repository;

import com.bcsystems.barberia_api.domain.Configuracion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConfiguracionRepository extends JpaRepository<Configuracion, Integer> {
    Optional<Configuracion> findByClave(String clave);
    boolean existsByClave(String clave);
}
