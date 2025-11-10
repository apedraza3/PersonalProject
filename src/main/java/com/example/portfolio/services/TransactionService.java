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

@Service
@Transactional
public class TransactionService {

    private final TransactionRepository txRepo;
    private final AccountRepository accountRepo;

    public TransactionService(TransactionRepository txRepo, AccountRepository accountRepo) {
        this.txRepo = txRepo;
        this.accountRepo = accountRepo;
    }

    // CREATE
    public TransactionResponseDto create(TransactionCreateDto dto) {
        Account account = accountRepo.findById(dto.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

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
    public TransactionResponseDto update(Integer id, TransactionUpdateDto dto) {
        Transaction tx = txRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

        // Optional: allow changing account
        if (dto.getAccountId() != null && !dto.getAccountId().equals(tx.getAccount().getId())) {
            Account account = accountRepo.findById(dto.getAccountId())
                    .orElseThrow(() -> new IllegalArgumentException("Account not found"));
            tx.setAccount(account);
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
    public TransactionResponseDto getOne(Integer id) {
        Transaction tx = txRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

        return TransactionMapper.toResponseDto(tx);
    }

    // GET ALL FOR ACCOUNT (used in AccountController)
    public List<TransactionResponseDto> getTransactionsForAccount(Integer accountId) {
        List<Transaction> txs = txRepo.findByAccountId(accountId);
        return txs.stream().map(TransactionMapper::toResponseDto).toList();
    }

    // DELETE
    public void delete(Integer id) {
        txRepo.deleteById(id);
    }
}