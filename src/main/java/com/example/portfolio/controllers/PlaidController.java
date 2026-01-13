package com.example.portfolio.controllers;

import com.example.portfolio.models.PlaidItem;
import com.example.portfolio.models.User;
import com.example.portfolio.security.JwtUtil;
import com.example.portfolio.services.PlaidService;
import com.example.portfolio.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/plaid")
public class PlaidController {

    private final PlaidService plaidService;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    public PlaidController(PlaidService plaidService, JwtUtil jwtUtil, UserService userService) {
        this.plaidService = plaidService;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    // ---------------------------
    // 1. Create Plaid Link Token
    // ---------------------------
    @PostMapping("/link-token")
    public ResponseEntity<?> createLinkToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader) throws IOException {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid or missing Authorization header"));
        }

        // remove "Bearer "
        String rawToken = authHeader.substring(7).trim();

        // get email from JWT
        String email;
        try {
            email = jwtUtil.getSubject(rawToken);
        } catch (Exception e) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Invalid or expired token"));
        }

        // find user by email
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found for email: " + email));

        // ask Plaid for a link token
        String linkToken = plaidService.createLinkToken(user);

        return ResponseEntity.ok(Map.of("link_token", linkToken));
    }

    // ---------------------------
    // 2. Exchange Public Token
    // ---------------------------
    @PostMapping("/exchange-public-token")
    public ResponseEntity<?> exchangePublicToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, String> body) throws IOException {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid or missing Authorization header"));
        }

        String rawToken = authHeader.substring(7).trim();
        String email;
        try {
            email = jwtUtil.getSubject(rawToken);
        } catch (Exception e) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Invalid or expired token"));
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
    public ResponseEntity<?> syncAccounts(
            @RequestHeader(value = "Authorization", required = false) String authHeader) throws IOException {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid or missing Authorization header"));
        }

        String rawToken = authHeader.substring(7).trim();
        String email;
        try {
            email = jwtUtil.getSubject(rawToken);
        } catch (Exception e) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Invalid or expired token"));
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
    public ResponseEntity<?> syncTransactions(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) Map<String, String> body) throws IOException {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid or missing Authorization header"));
        }

        String rawToken = authHeader.substring(7).trim();
        String email;
        try {
            email = jwtUtil.getSubject(rawToken);
        } catch (Exception e) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Invalid or expired token"));
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
                "transactions", transactions
        ));
    }
}