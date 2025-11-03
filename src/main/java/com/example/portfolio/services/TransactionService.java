package com.example.portfolio.services;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import com.example.portfolio.repositories.TransactionRepository;
import com.example.portfolio.models.Account;
import com.example.portfolio.models.Transaction;

@Service
public class TransactionService {
    private final TransactionRepository txRepo;

    public TransactionService(TransactionRepository txRepo) {
        this.txRepo = txRepo;
    }

    public Transaction createTransaction(Account account, BigDecimal amount, String description) {
        Transaction tx = new Transaction();
        tx.setAccount(account);
        tx.setDescription(description);
        tx.setAmount(amount);
        tx.setCreatedAt(java.time.LocalDateTime.now());
        tx.setUpdatedAt(java.time.LocalDateTime.now());
        return txRepo.save(tx);
    }

    public List<Transaction> getTransactionsForAccount(Integer accountId) {
        return txRepo.findByAccountId(accountId);
    }
}
