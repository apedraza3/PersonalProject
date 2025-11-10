package com.example.portfolio.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.example.portfolio.dto.TransactionResponseDto; // <-- IMPORTANT
import com.example.portfolio.services.TransactionService;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final TransactionService transactionService;

    public AccountController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<List<TransactionResponseDto>> getTransactionsForAccount(
            @PathVariable("id") Integer accountId) {
        List<TransactionResponseDto> transactions = transactionService.getTransactionsForAccount(accountId);
        return ResponseEntity.ok(transactions);
    }
}