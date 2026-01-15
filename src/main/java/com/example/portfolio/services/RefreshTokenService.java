package com.example.portfolio.services;

import com.example.portfolio.models.RefreshToken;
import com.example.portfolio.models.User;
import com.example.portfolio.repositories.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.refresh-token.ttl-days:7}")
    private int refreshTokenTtlDays;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    /**
     * Create a new refresh token for the user
     */
    public RefreshToken createRefreshToken(User user) {
        // Generate a secure random token
        String tokenValue = UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();

        LocalDateTime expiresAt = LocalDateTime.now().plusDays(refreshTokenTtlDays);

        RefreshToken refreshToken = new RefreshToken(tokenValue, user, expiresAt);
        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Find a refresh token by its value
     */
    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    /**
     * Verify and get refresh token
     * Returns the token if it's valid, empty if invalid/expired/revoked
     */
    @Transactional(readOnly = true)
    public Optional<RefreshToken> verifyRefreshToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .filter(RefreshToken::isValid);
    }

    /**
     * Revoke a refresh token
     */
    public void revokeToken(RefreshToken token) {
        token.setRevoked(true);
        refreshTokenRepository.save(token);
    }

    /**
     * Revoke all refresh tokens for a user (useful for logout from all devices)
     */
    public void revokeAllUserTokens(Integer userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    /**
     * Delete expired refresh tokens (cleanup task)
     */
    public void deleteExpiredTokens() {
        refreshTokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }
}
