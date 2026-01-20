package com.example.portfolio.models;

import com.example.portfolio.security.EncryptedStringConverter;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing a connected cryptocurrency exchange account (Coinbase, Binance, etc.)
 * Stores encrypted OAuth tokens for accessing exchange APIs
 */
@Entity
@Table(name = "exchange_items")
public class ExchangeItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Exchange name (coinbase, binance, kraken, etc.)
     */
    @Column(nullable = false)
    private String exchange;

    /**
     * OAuth access token (encrypted)
     */
    @Convert(converter = EncryptedStringConverter.class)
    @Column(length = 1000) // Encrypted data is longer than plaintext
    private String accessToken;

    /**
     * OAuth refresh token (encrypted)
     */
    @Convert(converter = EncryptedStringConverter.class)
    @Column(length = 1000)
    private String refreshToken;

    /**
     * Access token expiration time
     */
    private LocalDateTime tokenExpiresAt;

    /**
     * User-friendly name for this connection (optional)
     */
    private String connectionName;

    /**
     * User who owns this exchange connection
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public ExchangeItem() {
    }

    public ExchangeItem(String exchange, String accessToken, String refreshToken, LocalDateTime tokenExpiresAt, User owner) {
        this.exchange = exchange;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenExpiresAt = tokenExpiresAt;
        this.owner = owner;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public LocalDateTime getTokenExpiresAt() {
        return tokenExpiresAt;
    }

    public void setTokenExpiresAt(LocalDateTime tokenExpiresAt) {
        this.tokenExpiresAt = tokenExpiresAt;
    }

    public String getConnectionName() {
        return connectionName;
    }

    public void setConnectionName(String connectionName) {
        this.connectionName = connectionName;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
