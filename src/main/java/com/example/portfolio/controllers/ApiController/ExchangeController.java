package com.example.portfolio.controllers.ApiController;

import com.example.portfolio.models.ExchangeItem;
import com.example.portfolio.models.User;
import com.example.portfolio.security.CurrentUser;
import com.example.portfolio.services.CoinbaseService;
import com.example.portfolio.services.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.*;

/**
 * REST API for cryptocurrency exchange OAuth integration
 * Handles OAuth flow for Coinbase and other exchanges
 */
@RestController
@RequestMapping("/api/exchange")
public class ExchangeController {

    private final CoinbaseService coinbaseService;
    private final CurrentUser currentUser;
    private final UserService userService;

    public ExchangeController(CoinbaseService coinbaseService, CurrentUser currentUser, UserService userService) {
        this.coinbaseService = coinbaseService;
        this.currentUser = currentUser;
        this.userService = userService;
    }

    // ---------------------------
    // 1. OAuth Flow
    // ---------------------------

    /**
     * Initiate Coinbase OAuth flow
     * GET /api/exchange/coinbase/connect
     */
    @GetMapping("/coinbase/connect")
    public RedirectView connectCoinbase(HttpSession session) {
        String email = currentUser.email();
        if (email == null) {
            return new RedirectView("/login");
        }

        // Generate random state for CSRF protection
        String state = UUID.randomUUID().toString();
        session.setAttribute("coinbase_oauth_state", state);

        String authUrl = coinbaseService.getAuthorizationUrl(state);
        return new RedirectView(authUrl);
    }

    /**
     * OAuth callback from Coinbase
     * GET /api/exchange/coinbase/callback
     */
    @GetMapping("/coinbase/callback")
    public RedirectView coinbaseCallback(
            @RequestParam String code,
            @RequestParam String state,
            HttpSession session
    ) {
        String email = currentUser.email();
        if (email == null) {
            return new RedirectView("/login?error=not_authenticated");
        }

        // Verify state for CSRF protection
        String savedState = (String) session.getAttribute("coinbase_oauth_state");
        if (savedState == null || !savedState.equals(state)) {
            return new RedirectView("/dashboard?error=invalid_state");
        }

        try {
            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            coinbaseService.exchangeCodeForToken(code, user);

            // Clean up session
            session.removeAttribute("coinbase_oauth_state");

            return new RedirectView("/dashboard?success=coinbase_connected");

        } catch (Exception e) {
            System.err.println("Error connecting Coinbase: " + e.getMessage());
            e.printStackTrace();
            return new RedirectView("/dashboard?error=coinbase_connection_failed");
        }
    }

    // ---------------------------
    // 2. Exchange Data
    // ---------------------------

    /**
     * Get all connected exchanges for current user
     * GET /api/exchange/connections
     */
    @GetMapping("/connections")
    public ResponseEntity<?> getConnections() {
        String email = currentUser.email();
        if (email == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        try {
            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<ExchangeItem> items = coinbaseService.getExchangeItems(user);

            List<Map<String, Object>> connections = items.stream().map(item -> {
                Map<String, Object> data = new HashMap<>();
                data.put("id", item.getId());
                data.put("exchange", item.getExchange());
                data.put("connectionName", item.getConnectionName());
                data.put("createdAt", item.getCreatedAt().toString());
                return data;
            }).toList();

            return ResponseEntity.ok(connections);

        } catch (Exception e) {
            System.err.println("Error fetching connections: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Failed to fetch connections"));
        }
    }

    /**
     * Get Coinbase accounts with balances
     * GET /api/exchange/coinbase/accounts
     */
    @GetMapping("/coinbase/accounts")
    public ResponseEntity<?> getCoinbaseAccounts() {
        String email = currentUser.email();
        if (email == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        try {
            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Optional<ExchangeItem> item = coinbaseService.getExchangeItems(user).stream()
                    .filter(i -> i.getExchange().equals("coinbase"))
                    .findFirst();

            if (item.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("error", "Coinbase not connected"));
            }

            List<Map<String, Object>> accounts = coinbaseService.getAccounts(item.get());
            return ResponseEntity.ok(accounts);

        } catch (Exception e) {
            System.err.println("Error fetching Coinbase accounts: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Failed to fetch accounts: " + e.getMessage()));
        }
    }

    /**
     * Get transactions for a Coinbase account
     * GET /api/exchange/coinbase/accounts/{accountId}/transactions
     */
    @GetMapping("/coinbase/accounts/{accountId}/transactions")
    public ResponseEntity<?> getCoinbaseTransactions(@PathVariable String accountId) {
        String email = currentUser.email();
        if (email == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        try {
            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Optional<ExchangeItem> item = coinbaseService.getExchangeItems(user).stream()
                    .filter(i -> i.getExchange().equals("coinbase"))
                    .findFirst();

            if (item.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("error", "Coinbase not connected"));
            }

            List<Map<String, Object>> transactions = coinbaseService.getTransactions(item.get(), accountId);
            return ResponseEntity.ok(transactions);

        } catch (Exception e) {
            System.err.println("Error fetching Coinbase transactions: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Failed to fetch transactions"));
        }
    }

    /**
     * Disconnect an exchange
     * DELETE /api/exchange/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> disconnectExchange(@PathVariable Integer id) {
        String email = currentUser.email();
        if (email == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        try {
            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            coinbaseService.disconnectExchange(id, user.getId());
            return ResponseEntity.ok(Map.of("success", true));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("Error disconnecting exchange: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Failed to disconnect exchange"));
        }
    }
}
