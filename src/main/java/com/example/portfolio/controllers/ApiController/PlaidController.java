package com.example.portfolio.controllers.ApiController;

import com.example.portfolio.models.PlaidItem;
import com.example.portfolio.models.User;
import com.example.portfolio.security.CurrentUser;
import com.example.portfolio.services.PlaidService;
import com.example.portfolio.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/plaid")
public class PlaidController {

    private final PlaidService plaidService;
    private final CurrentUser currentUser;
    private final UserService userService;

    public PlaidController(PlaidService plaidService, CurrentUser currentUser, UserService userService) {
        this.plaidService = plaidService;
        this.currentUser = currentUser;
        this.userService = userService;
    }

    // ---------------------------
    // 1. Create Plaid Link Token
    // ---------------------------
    @PostMapping("/link-token")
    public ResponseEntity<?> createLinkToken() {
        try {
            String email = currentUser.email();
            if (email == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("error", "Not authenticated"));
            }

            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found for email: " + email));

            String linkToken = plaidService.createLinkToken(user);

            return ResponseEntity.ok(Map.of("link_token", linkToken));
        } catch (IOException e) {
            System.err.println("❌ Plaid Link Token Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Failed to create Plaid link token: " + e.getMessage()));
        } catch (Exception e) {
            System.err.println("❌ Unexpected Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Unexpected error: " + e.getMessage()));
        }
    }

    // ---------------------------
    // 2. Exchange Public Token
    // ---------------------------
    @PostMapping("/exchange")
    public ResponseEntity<?> exchangePublicToken(@RequestBody Map<String, String> body) throws IOException {
        String email = currentUser.email();
        if (email == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Not authenticated"));
        }

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found for email: " + email));

        String publicToken = body.get("public_token");
        if (publicToken == null || publicToken.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "public_token is required"));
        }

        PlaidItem item = plaidService.exchangePublicToken(user, publicToken);

        return ResponseEntity.ok(Map.of(
                "item_id", item.getItemId(),
                "plaid_item_id", item.getId()));
    }

    // ---------------------------
    // 3. Sync accounts from Plaid into our DB
    // ---------------------------
    @PostMapping("/accounts/sync")
    public ResponseEntity<?> syncAccounts() throws IOException {
        String email = currentUser.email();
        if (email == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Not authenticated"));
        }

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found for email: " + email));

        var accounts = plaidService.syncAccountsForUser(user);

        return ResponseEntity.ok(accounts);
    }

    // ---------------------------
    // 4. Sync transactions from Plaid into our DB
    // ---------------------------
    @PostMapping("/transactions/sync")
    public ResponseEntity<?> syncTransactions(@RequestBody(required = false) Map<String, String> body) throws IOException {
        String email = currentUser.email();
        if (email == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Not authenticated"));
        }

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found for email: " + email));

        // Default to last 30 days if no dates provided
        java.time.LocalDate endDate = java.time.LocalDate.now();
        java.time.LocalDate startDate = endDate.minusDays(30);

        // Allow custom date range if provided
        if (body != null) {
            if (body.containsKey("start_date")) {
                startDate = java.time.LocalDate.parse(body.get("start_date"));
            }
            if (body.containsKey("end_date")) {
                endDate = java.time.LocalDate.parse(body.get("end_date"));
            }
        }

        var transactions = plaidService.syncTransactionsForUser(user, startDate, endDate);

        return ResponseEntity.ok(Map.of(
                "count", transactions.size(),
                "transactions", transactions));
    }
}