package com.bcsystems.barberia_api.repository;

import com.bcsystems.barberia_api.domain.Permiso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PermisoRepository extends JpaRepository<Permiso, Integer> {

    Optional<Permiso> findByClave(String clave);

    List<Permiso> findByActivoTrueOrderByModuloAscNombreAsc();

}
