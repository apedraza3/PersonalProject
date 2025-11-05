package com.example.portfolio.controllers;

import org.springframework.web.bind.annotation.*;
import com.example.portfolio.services.TransactionService;
import com.example.portfolio.models.Transaction;
import org.springframework.http.ResponseEntity;
import java.util.List;

@RestController
@RequestMapping("/accounts")
public class AccountController {
    private final TransactionService transactionService;

    public AccountController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<List<Transaction>> getTransactionsForAccount(@PathVariable("id") Integer accountId) {
        List<Transaction> transactions = transactionService.getTransactionsForAccount(accountId);
        return ResponseEntity.ok(transactions); // This can be an empty list if the account has no transactions
    }

}
