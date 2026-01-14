package com.example.portfolio.mappers;

import com.example.portfolio.dto.AccountResponseDto;
import com.example.portfolio.models.Account;

public class AccountMapper {

    public static AccountResponseDto toResponseDto(Account account) {
        if (account == null) {
            return null;
        }

        AccountResponseDto dto = new AccountResponseDto();
        dto.setId(account.getId());
        dto.setAccountName(account.getAccountName());
        dto.setInstitutionString(account.getInstitutionString());
        dto.setAccountType(account.getAccountType());
        dto.setBalance(account.getBalance());
        dto.setPlaidAccountId(account.getPlaidAccountId());
        dto.setCreatedAt(account.getCreatedAt());
        dto.setUpdatedAt(account.getUpdatedAt());
        return dto;
    }
}
