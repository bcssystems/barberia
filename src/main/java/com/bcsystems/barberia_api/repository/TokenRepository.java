package com.bcsystems.barberia_api.repository;

import com.bcsystems.barberia_api.domain.Token;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Integer> {

    Optional<Token> findByToken(String token);

    Optional<Token> findByRefreshToken(String refreshToken);

    List<Token> findByUsuarioIdUsuarioAndRevokedFalseAndExpiredFalse(Integer idUsuario);

    void deleteByUsuarioIdUsuario(Integer idUsuario);

}
