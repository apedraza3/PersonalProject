package com.example.portfolio.models;

import com.example.portfolio.security.EncryptedStringConverter;
import jakarta.persistence.*;

@Entity
@Table(name = "plaid_items")
public class PlaidItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String itemId;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(length = 1000) // Encrypted data is longer than plaintext
    private String accessToken;

    private String institutionName;

    @Column(name = "transaction_cursor", columnDefinition = "TEXT")
    private String transactionCursor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User owner;

    public PlaidItem() {
    }

    public PlaidItem(String itemId, String accessToken, String institutionName, User owner) {
        this.itemId = itemId;
        this.accessToken = accessToken;
        this.institutionName = institutionName;
        this.owner = owner;
    }

    // getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getTransactionCursor() {
        return transactionCursor;
    }

    public void setTransactionCursor(String transactionCursor) {
        this.transactionCursor = transactionCursor;
    }
}