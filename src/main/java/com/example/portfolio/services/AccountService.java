package com.example.portfolio.services;

import org.springframework.stereotype.Service;
import com.example.portfolio.repositories.AccountRepository;
import java.time.LocalDateTime;
import com.example.portfolio.models.Account;
import com.example.portfolio.models.User;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account createAccount(User user, String accountName) {
        Account newAccount = new Account();
        newAccount.setUser(user);
        newAccount.setAccountName(accountName);
        newAccount.setCreatedAt(LocalDateTime.now());
        newAccount.setUpdatedAt(LocalDateTime.now());
        return accountRepository.save(newAccount);
    }

    public java.util.List<Account> getAccountsForUser(Integer userId) {
        return accountRepository.findByUserId(userId);
    }
}
