package com.example.portfolio.models;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a cryptocurrency transaction from blockchain data.
 * Synced from blockchain APIs (Etherscan, etc.) for wallet tracking.
 */
@Entity
@Table(name = "crypto_transactions")
public class CryptoTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "tx_hash", unique = true, length = 255)
    private String txHash;  // Blockchain transaction hash (unique identifier)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id")
    private CryptoWallet wallet;

    @Column(name = "date", nullable = false)
    private LocalDateTime date;

    @Column(name = "from_address", length = 255)
    private String fromAddress;

    @Column(name = "to_address", length = 255)
    private String toAddress;

    @Column(name = "amount", precision = 30, scale = 18)
    private BigDecimal amount;  // Crypto has many decimals (e.g., 0.000000000000000001 ETH)

    @Column(name = "token", length = 50)
    private String token;  // "ETH", "BTC", "USDC", etc.

    @Column(name = "type", length = 50)
    private String type;  // "send", "receive", "swap", "contract_interaction"

    @Column(name = "gas_fee", precision = 30, scale = 18)
    private BigDecimal gasFee;  // Transaction fee

    @Column(name = "block_number")
    private Long blockNumber;

    @Column(name = "confirmations")
    private Integer confirmations;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Constructors
    public CryptoTransaction() {
    }

    public CryptoTransaction(String txHash, CryptoWallet wallet, LocalDateTime date,
                           String fromAddress, String toAddress, BigDecimal amount,
                           String token, String type) {
        this.txHash = txHash;
        this.wallet = wallet;
        this.date = date;
        this.fromAddress = fromAddress;
        this.toAddress = toAddress;
        this.amount = amount;
        this.token = token;
        this.type = type;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public CryptoWallet getWallet() {
        return wallet;
    }

    public void setWallet(CryptoWallet wallet) {
        this.wallet = wallet;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public String getToAddress() {
        return toAddress;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getGasFee() {
        return gasFee;
    }

    public void setGasFee(BigDecimal gasFee) {
        this.gasFee = gasFee;
    }

    public Long getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(Long blockNumber) {
        this.blockNumber = blockNumber;
    }

    public Integer getConfirmations() {
        return confirmations;
    }

    public void setConfirmations(Integer confirmations) {
        this.confirmations = confirmations;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
