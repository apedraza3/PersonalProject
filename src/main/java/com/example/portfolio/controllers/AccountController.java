package com.example.portfolio.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import com.example.portfolio.dto.TransactionResponseDto;
import com.example.portfolio.models.Account;
import com.example.portfolio.models.User;
import com.example.portfolio.repositories.AccountRepository;
import com.example.portfolio.security.CurrentUser;
import com.example.portfolio.services.TransactionService;
import com.example.portfolio.services.UserService;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final TransactionService transactionService;
    private final AccountRepository accountRepository;
    private final CurrentUser currentUser;
    private final UserService userService;

    public AccountController(TransactionService transactionService,
                           AccountRepository accountRepository,
                           CurrentUser currentUser,
                           UserService userService) {
        this.transactionService = transactionService;
        this.accountRepository = accountRepository;
        this.currentUser = currentUser;
        this.userService = userService;
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<?> getTransactionsForAccount(
            @PathVariable("id") Integer accountId) {

        // Get current authenticated user
        String email = currentUser.email();
        if (email == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Not authenticated"));
        }

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify account ownership
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        if (!account.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403)
                    .body(Map.of("error", "Access denied: you do not own this account"));
        }

        List<TransactionResponseDto> transactions = transactionService.getTransactionsForAccount(accountId);
        return ResponseEntity.ok(transactions);
    }
}