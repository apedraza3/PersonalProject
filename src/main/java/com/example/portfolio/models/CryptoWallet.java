package com.example.portfolio.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Represents a cryptocurrency wallet address owned by a user.
 * Stores public wallet addresses for read-only balance and transaction tracking.
 * No private keys are stored - completely safe!
 */
@Entity
@Table(name = "crypto_wallets")
public class CryptoWallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "wallet_address", nullable = false, length = 255)
    private String walletAddress;  // Public address (e.g., "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb")

    @Column(name = "blockchain", nullable = false, length = 50)
    private String blockchain;  // "ethereum", "bitcoin", "solana", "polygon", etc.

    @Column(name = "wallet_name", length = 255)
    private String walletName;  // User-friendly name (e.g., "My Main Wallet")

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public CryptoWallet() {
    }

    public CryptoWallet(String walletAddress, String blockchain, String walletName, User owner) {
        this.walletAddress = walletAddress;
        this.blockchain = blockchain;
        this.walletName = walletName;
        this.owner = owner;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getWalletAddress() {
        return walletAddress;
    }

    public void setWalletAddress(String walletAddress) {
        this.walletAddress = walletAddress;
    }

    public String getBlockchain() {
        return blockchain;
    }

    public void setBlockchain(String blockchain) {
        this.blockchain = blockchain;
    }

    public String getWalletName() {
        return walletName;
    }

    public void setWalletName(String walletName) {
        this.walletName = walletName;
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

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
