package com.rh360.rh360.repository;

import com.rh360.rh360.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

    Optional<Token> findByTokenAndActiveTrue(String token);

    Optional<Token> findByToken(String token);

    java.util.List<Token> findByUserIdAndActiveTrue(Long userId);

    void deleteByUserId(Long userId);

    void deleteByExpiresAtBefore(java.time.LocalDateTime dateTime);
}
