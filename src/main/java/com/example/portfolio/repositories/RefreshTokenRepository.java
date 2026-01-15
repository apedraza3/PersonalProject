package com.example.portfolio.repositories;

import com.example.portfolio.models.RefreshToken;
import com.example.portfolio.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUser(User user);

    void deleteByExpiresAtBefore(LocalDateTime date);

    void deleteByUserId(Integer userId);
}
