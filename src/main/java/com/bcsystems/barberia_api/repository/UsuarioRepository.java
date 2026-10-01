package com.bcsystems.barberia_api.repository;

import com.bcsystems.barberia_api.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByUsuarioIgnoreCase(String usuario);

    boolean existsByUsuarioIgnoreCase(String usuario);

    List<Usuario> findByActivoTrueOrderByNombreAsc();

}
