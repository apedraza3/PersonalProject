package com.example.portfolio.services;

import org.springframework.stereotype.Service;
import com.example.portfolio.repositories.AccountRepository;
import java.time.LocalDateTime;
import com.example.portfolio.models.Account;
import com.example.portfolio.models.User;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepo;

    public AccountService(AccountRepository accountRepo) {
        this.accountRepo = accountRepo;
    }

    // Get all accounts for a user
    @Transactional(readOnly = true)
    public List<Account> getAccountsForUser(User user) {
        return accountRepo.findByUserId(user.getId());
    }

    public Account createAccount(User user, String accountName) {
        Account newAccount = new Account();
        newAccount.setUser(user);
        newAccount.setAccountName(accountName);
        newAccount.setCreatedAt(LocalDateTime.now());
        newAccount.setUpdatedAt(LocalDateTime.now());
        return accountRepo.save(newAccount);
    }

    public java.util.List<Account> getAccountsForUser(Integer userId) {
        return accountRepo.findByUserId(userId);
    }
}
