package com.bcsystems.barberia_api.repository;

import com.bcsystems.barberia_api.domain.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RolRepository extends JpaRepository<Rol, Integer> {

    Optional<Rol> findByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCase(String nombre);

    List<Rol> findByActivoTrueOrderByNombreAsc();

}
