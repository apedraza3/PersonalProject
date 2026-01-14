package com.example.portfolio.controllers.ApiController;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import com.example.portfolio.dto.TransactionResponseDto;
import com.example.portfolio.models.User;
import com.example.portfolio.security.CurrentUser;
import com.example.portfolio.services.TransactionService;
import com.example.portfolio.services.UserService;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final CurrentUser currentUser;
    private final UserService userService;

    public TransactionController(
            TransactionService transactionService,
            CurrentUser currentUser,
            UserService userService) {
        this.transactionService = transactionService;
        this.currentUser = currentUser;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<?> getCurrentUserTransactions(
            @RequestParam(required = false, defaultValue = "30") Integer days) {

        // Get current authenticated user
        String email = currentUser.email();
        if (email == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Not authenticated"));
        }

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get all transactions for this user's accounts
        List<TransactionResponseDto> transactions = transactionService.getTransactionsForUser(user.getId(), days);

        return ResponseEntity.ok(transactions);
    }
}
