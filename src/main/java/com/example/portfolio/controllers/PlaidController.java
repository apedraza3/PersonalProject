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
    public ResponseEntity<Map<String, String>> createLinkToken(
            @RequestHeader("Authorization") String authHeader) throws IOException {

        // remove "Bearer "
        String rawToken = authHeader.substring(7);

        // get email from JWT
        String email = jwtUtil.getSubject(rawToken);

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
    public ResponseEntity<Map<String, Object>> exchangePublicToken(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> body) throws IOException {

        String rawToken = authHeader.substring(7);
        String email = jwtUtil.getSubject(rawToken);

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
            @RequestHeader("Authorization") String authHeader) throws IOException {

        String rawToken = authHeader.substring(7);
        String email = jwtUtil.getSubject(rawToken);

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found for email: " + email));

        var accounts = plaidService.syncAccountsForUser(user);

        return ResponseEntity.ok(accounts);
    }
}