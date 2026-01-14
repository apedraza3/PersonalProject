package com.example.portfolio.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.portfolio.dto.TransactionCreateDto;
import com.example.portfolio.dto.TransactionUpdateDto;
import com.example.portfolio.dto.TransactionResponseDto;
import com.example.portfolio.mappers.TransactionMapper;
import com.example.portfolio.repositories.TransactionRepository;
import com.example.portfolio.repositories.AccountRepository;
import com.example.portfolio.models.Transaction;
import com.example.portfolio.models.Account;
import com.example.portfolio.models.User;

@Service
@Transactional
public class TransactionService {

    private final TransactionRepository txRepo;
    private final AccountRepository accountRepo;

    public TransactionService(TransactionRepository txRepo, AccountRepository accountRepo) {
        this.txRepo = txRepo;
        this.accountRepo = accountRepo;
    }

    // Helper method to verify ownership
    private void verifyAccountOwnership(Account account, User user) {
        if (!account.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Access denied: you do not own this account");
        }
    }

    // CREATE
    public TransactionResponseDto create(TransactionCreateDto dto, User currentUser) {
        Account account = accountRepo.findById(dto.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        // Verify ownership
        verifyAccountOwnership(account, currentUser);

        Transaction tx = new Transaction();
        tx.setAccount(account);
        tx.setDate(dto.getDate());
        tx.setDescription(dto.getDescription());
        tx.setAmount(dto.getAmount());
        tx.setCategory(dto.getCategory());
        tx.setCreatedAt(java.time.LocalDateTime.now());
        tx.setUpdatedAt(java.time.LocalDateTime.now());

        Transaction saved = txRepo.save(tx);
        return TransactionMapper.toResponseDto(saved);
    }

    // UPDATE
    public TransactionResponseDto update(Integer id, TransactionUpdateDto dto, User currentUser) {
        Transaction tx = txRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

        // Verify ownership of current account
        verifyAccountOwnership(tx.getAccount(), currentUser);

        // Optional: allow changing account (but verify ownership of new account too)
        if (dto.getAccountId() != null && !dto.getAccountId().equals(tx.getAccount().getId())) {
            Account newAccount = accountRepo.findById(dto.getAccountId())
                    .orElseThrow(() -> new IllegalArgumentException("Account not found"));

            // Verify ownership of new account
            verifyAccountOwnership(newAccount, currentUser);
            tx.setAccount(newAccount);
        }

        tx.setDate(dto.getDate());
        tx.setDescription(dto.getDescription());
        tx.setAmount(dto.getAmount());
        tx.setCategory(dto.getCategory());
        tx.setUpdatedAt(java.time.LocalDateTime.now());

        Transaction saved = txRepo.save(tx);
        return TransactionMapper.toResponseDto(saved);
    }

    // GET ONE
    public TransactionResponseDto getOne(Integer id, User currentUser) {
        Transaction tx = txRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

        // Verify ownership
        verifyAccountOwnership(tx.getAccount(), currentUser);

        return TransactionMapper.toResponseDto(tx);
    }

    // GET ALL FOR ACCOUNT (used in AccountController)
    public List<TransactionResponseDto> getTransactionsForAccount(Integer accountId) {
        List<Transaction> txs = txRepo.findByAccountId(accountId);
        return txs.stream().map(TransactionMapper::toResponseDto).toList();
    }

    // GET ALL FOR USER (across all their accounts)
    public List<TransactionResponseDto> getTransactionsForUser(Integer userId, Integer days) {
        // Get all accounts for this user
        List<Account> userAccounts = accountRepo.findByUserId(userId);

        // Get all transactions for these accounts
        List<Transaction> allTransactions = userAccounts.stream()
                .flatMap(account -> txRepo.findByAccountId(account.getId()).stream())
                .toList();

        // Convert to DTOs
        return allTransactions.stream()
                .map(TransactionMapper::toResponseDto)
                .toList();
    }

    // DELETE
    public void delete(Integer id, User currentUser) {
        Transaction tx = txRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

        // Verify ownership
        verifyAccountOwnership(tx.getAccount(), currentUser);

        txRepo.deleteById(id);
    }
}