package com.example.portfolio.mappers;

import com.example.portfolio.dto.TransactionResponseDto;
import com.example.portfolio.models.Transaction;

public class TransactionMapper {
    public static TransactionResponseDto toResponseDto(Transaction t) {
        TransactionResponseDto dto = new TransactionResponseDto();
        dto.setId(t.getId());
        dto.setAccountId(t.getAccount() != null ? t.getAccount().getId() : null);
        dto.setDate(t.getDate());
        dto.setDescription(t.getDescription());
        dto.setAmount(t.getAmount());
        dto.setCategory(t.getCategory());
        dto.setCreatedAt(t.getCreatedAt());
        dto.setUpdatedAt(t.getUpdatedAt());
        return dto;
    }
}