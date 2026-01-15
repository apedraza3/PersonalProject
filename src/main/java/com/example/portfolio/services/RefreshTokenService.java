package com.example.portfolio.services;

import com.example.portfolio.models.RefreshToken;
import com.example.portfolio.models.User;
import com.example.portfolio.repositories.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
     * Hash a token using SHA-256
     * This is secure for refresh tokens as they have high entropy (UUID-based)
     */
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));

            // Convert to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Create a new refresh token for the user
     * Returns a TokenPair with both the plaintext token (to send to client)
     * and the hashed token (stored in DB)
     */
    public RefreshToken createRefreshToken(User user) {
        // Generate a secure random token (plaintext)
        String plaintextToken = UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();

        // Hash it before storing
        String hashedToken = hashToken(plaintextToken);

        LocalDateTime expiresAt = LocalDateTime.now().plusDays(refreshTokenTtlDays);

        // Store the HASHED token in the database
        RefreshToken refreshToken = new RefreshToken(hashedToken, user, expiresAt);
        RefreshToken saved = refreshTokenRepository.save(refreshToken);

        // IMPORTANT: We need to return the plaintext token to send to the client
        // But the entity has the hashed version. We'll temporarily store plaintext in the entity
        // just for returning to the controller
        saved.setToken(plaintextToken);  // Temporarily set plaintext for return

        return saved;
    }

    /**
     * Find a refresh token by its value (hashes the token before lookup)
     */
    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByToken(String plaintextToken) {
        String hashedToken = hashToken(plaintextToken);
        return refreshTokenRepository.findByToken(hashedToken);
    }

    /**
     * Verify and get refresh token (hashes the token before lookup)
     * Returns the token if it's valid, empty if invalid/expired/revoked
     */
    @Transactional(readOnly = true)
    public Optional<RefreshToken> verifyRefreshToken(String plaintextToken) {
        String hashedToken = hashToken(plaintextToken);
        return refreshTokenRepository.findByToken(hashedToken)
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
