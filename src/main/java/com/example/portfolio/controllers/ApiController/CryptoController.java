package com.example.portfolio.controllers.ApiController;

import com.example.portfolio.models.CryptoTransaction;
import com.example.portfolio.models.CryptoWallet;
import com.example.portfolio.models.User;
import com.example.portfolio.security.CurrentUser;
import com.example.portfolio.services.CryptoService;
import com.example.portfolio.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST API for cryptocurrency wallet tracking.
 * Provides endpoints for adding wallets, fetching balances, and syncing transactions.
 */
@RestController
@RequestMapping("/api/crypto")
public class CryptoController {

    private final CryptoService cryptoService;
    private final CurrentUser currentUser;
    private final UserService userService;

    public CryptoController(CryptoService cryptoService, CurrentUser currentUser, UserService userService) {
        this.cryptoService = cryptoService;
        this.currentUser = currentUser;
        this.userService = userService;
    }

    // ---------------------------
    // 1. Wallet Management
    // ---------------------------

    /**
     * Add a new cryptocurrency wallet
     * POST /api/crypto/wallets
     */
    @PostMapping("/wallets")
    public ResponseEntity<?> addWallet(@RequestBody Map<String, String> request) {
        String email = currentUser.email();
        if (email == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String walletAddress = request.get("walletAddress");
        String blockchain = request.get("blockchain");
        String walletName = request.get("walletName");

        // Validate inputs
        if (walletAddress == null || walletAddress.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Wallet address is required"));
        }
        if (blockchain == null || blockchain.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Blockchain is required"));
        }

        try {
            CryptoWallet wallet = cryptoService.addWallet(user, walletAddress, blockchain, walletName);

            // Get current balance
            BigDecimal balance = cryptoService.getWalletBalance(wallet);
            BigDecimal balanceUsd = BigDecimal.ZERO;

            // Get USD value
            String token = switch (blockchain.toLowerCase()) {
                case "ethereum" -> "ETH";
                case "bitcoin" -> "BTC";
                case "polygon" -> "MATIC";
                default -> "ETH";
            };
            BigDecimal price = cryptoService.getTokenPrice(token);
            if (price.compareTo(BigDecimal.ZERO) > 0) {
                balanceUsd = balance.multiply(price);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("id", wallet.getId());
            response.put("walletAddress", wallet.getWalletAddress());
            response.put("blockchain", wallet.getBlockchain());
            response.put("walletName", wallet.getWalletName());
            response.put("balance", balance.toString());
            response.put("balanceUsd", balanceUsd.toString());
            response.put("token", token);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("Error adding wallet: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Failed to add wallet: " + e.getMessage()));
        }
    }

    /**
     * Get all wallets for current user with balances
     * GET /api/crypto/wallets
     */
    @GetMapping("/wallets")
    public ResponseEntity<?> getWallets() {
        String email = currentUser.email();
        if (email == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            List<CryptoWallet> wallets = cryptoService.getWalletsForUser(user);

            // Enrich with balance data
            List<Map<String, Object>> walletsWithBalances = wallets.stream().map(wallet -> {
                Map<String, Object> data = new HashMap<>();
                data.put("id", wallet.getId());
                data.put("walletAddress", wallet.getWalletAddress());
                data.put("blockchain", wallet.getBlockchain());
                data.put("walletName", wallet.getWalletName());
                data.put("createdAt", wallet.getCreatedAt().toString());

                // Get balance
                BigDecimal balance = cryptoService.getWalletBalance(wallet);
                data.put("balance", balance.toString());

                // Get token symbol
                String token = switch (wallet.getBlockchain().toLowerCase()) {
                    case "ethereum" -> "ETH";
                    case "bitcoin" -> "BTC";
                    case "polygon" -> "MATIC";
                    case "solana" -> "SOL";
                    default -> "ETH";
                };
                data.put("token", token);

                // Get USD value
                BigDecimal price = cryptoService.getTokenPrice(token);
                BigDecimal balanceUsd = balance.multiply(price);
                data.put("balanceUsd", balanceUsd.toString());

                return data;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(walletsWithBalances);

        } catch (Exception e) {
            System.err.println("Error fetching wallets: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Failed to fetch wallets"));
        }
    }

    /**
     * Get specific wallet with details
     * GET /api/crypto/wallets/{id}
     */
    @GetMapping("/wallets/{id}")
    public ResponseEntity<?> getWallet(@PathVariable Integer id) {
        String email = currentUser.email();
        if (email == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            CryptoWallet wallet = cryptoService.getWalletsForUser(user).stream()
                    .filter(w -> w.getId().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));

            Map<String, Object> data = new HashMap<>();
            data.put("id", wallet.getId());
            data.put("walletAddress", wallet.getWalletAddress());
            data.put("blockchain", wallet.getBlockchain());
            data.put("walletName", wallet.getWalletName());

            BigDecimal balance = cryptoService.getWalletBalance(wallet);
            data.put("balance", balance.toString());

            return ResponseEntity.ok(data);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("Error fetching wallet: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Failed to fetch wallet"));
        }
    }

    /**
     * Delete a wallet
     * DELETE /api/crypto/wallets/{id}
     */
    @DeleteMapping("/wallets/{id}")
    public ResponseEntity<?> deleteWallet(@PathVariable Integer id) {
        String email = currentUser.email();
        if (email == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            cryptoService.deleteWallet(id, user.getId());
            return ResponseEntity.ok(Map.of("success", true));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("Error deleting wallet: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Failed to delete wallet"));
        }
    }

    // ---------------------------
    // 2. Transaction Syncing
    // ---------------------------

    /**
     * Sync transactions for a wallet from blockchain
     * POST /api/crypto/wallets/{id}/sync
     */
    @PostMapping("/wallets/{id}/sync")
    public ResponseEntity<?> syncWalletTransactions(@PathVariable Integer id) {
        String email = currentUser.email();
        if (email == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            CryptoWallet wallet = cryptoService.getWalletsForUser(user).stream()
                    .filter(w -> w.getId().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));

            List<CryptoTransaction> synced = cryptoService.syncTransactions(wallet);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "synced", synced.size(),
                    "message", "Synced " + synced.size() + " new transactions"
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("Error syncing transactions: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Failed to sync transactions"));
        }
    }

    /**
     * Get all crypto transactions for current user
     * GET /api/crypto/transactions
     */
    @GetMapping("/transactions")
    public ResponseEntity<?> getTransactions() {
        String email = currentUser.email();
        if (email == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            List<CryptoTransaction> transactions = cryptoService.getTransactionsForUser(user.getId());

            List<Map<String, Object>> formatted = transactions.stream().map(tx -> {
                Map<String, Object> data = new HashMap<>();
                data.put("id", tx.getId());
                data.put("txHash", tx.getTxHash());
                data.put("date", tx.getDate().toString());
                data.put("fromAddress", tx.getFromAddress());
                data.put("toAddress", tx.getToAddress());
                data.put("amount", tx.getAmount().toString());
                data.put("token", tx.getToken());
                data.put("type", tx.getType());
                if (tx.getGasFee() != null) {
                    data.put("gasFee", tx.getGasFee().toString());
                }
                if (tx.getBlockNumber() != null) {
                    data.put("blockNumber", tx.getBlockNumber());
                }
                return data;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(formatted);

        } catch (Exception e) {
            System.err.println("Error fetching transactions: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Failed to fetch transactions"));
        }
    }
}
