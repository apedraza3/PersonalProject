package com.example.portfolio.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionUpdateDto {
    // Optional: allow moving a txn to a different account. If you don't want that,
    // remove accountId and its logic in the service.
    private Integer accountId;

    @NotNull
    private LocalDate date;

    @Size(max = 255)
    private String description;

    @NotNull
    @Digits(integer = 16, fraction = 2)
    private BigDecimal amount;

    @Size(max = 120)
    private String category;

    // getters/setters
    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}