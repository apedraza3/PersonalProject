package com.example.portfolio.services;

import com.example.portfolio.models.CryptoTransaction;
import com.example.portfolio.models.CryptoWallet;
import com.example.portfolio.models.User;
import com.example.portfolio.repositories.CryptoTransactionRepository;
import com.example.portfolio.repositories.CryptoWalletRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Service for cryptocurrency wallet tracking via blockchain APIs.
 * Supports Ethereum, Bitcoin, and other blockchains.
 * Uses public APIs (Etherscan, Blockchain.com, CoinGecko) for read-only data.
 */
@Service
public class CryptoService {

    private final CryptoWalletRepository cryptoWalletRepository;
    private final CryptoTransactionRepository cryptoTransactionRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${crypto.etherscan.api-key:}")
    private String etherscanApiKey;

    @Value("${crypto.coingecko.api-key:}")
    private String coingeckoApiKey;

    // Regex patterns for address validation
    private static final Pattern ETHEREUM_ADDRESS_PATTERN = Pattern.compile("^0x[a-fA-F0-9]{40}$");
    private static final Pattern BITCOIN_ADDRESS_PATTERN = Pattern.compile("^(1|3|bc1)[a-zA-Z0-9]{25,62}$");

    public CryptoService(CryptoWalletRepository cryptoWalletRepository,
                        CryptoTransactionRepository cryptoTransactionRepository) {
        this.cryptoWalletRepository = cryptoWalletRepository;
        this.cryptoTransactionRepository = cryptoTransactionRepository;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    // ---------------------------
    // 1. Wallet Management
    // ---------------------------

    /**
     * Add a new wallet for a user
     */
    public CryptoWallet addWallet(User user, String walletAddress, String blockchain, String walletName) {
        // Validate address format
        if (!isValidAddress(walletAddress, blockchain)) {
            throw new IllegalArgumentException("Invalid " + blockchain + " address format");
        }

        // Check if wallet already exists for this user
        if (cryptoWalletRepository.existsByWalletAddressAndOwner_Id(walletAddress, user.getId())) {
            throw new IllegalArgumentException("Wallet already added");
        }

        // Create and save wallet
        CryptoWallet wallet = new CryptoWallet(walletAddress, blockchain, walletName, user);
        return cryptoWalletRepository.save(wallet);
    }

    /**
     * Get all wallets for a user with current balances
     */
    public List<CryptoWallet> getWalletsForUser(User user) {
        return cryptoWalletRepository.findByOwner(user);
    }

    /**
     * Delete a wallet
     */
    public void deleteWallet(Integer walletId, Integer userId) {
        CryptoWallet wallet = cryptoWalletRepository.findById(walletId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));

        // Verify ownership
        if (!wallet.getOwner().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized");
        }

        cryptoWalletRepository.delete(wallet);
    }

    // ---------------------------
    // 2. Address Validation
    // ---------------------------

    public boolean isValidAddress(String address, String blockchain) {
        if (address == null || address.isEmpty()) {
            return false;
        }

        return switch (blockchain.toLowerCase()) {
            case "ethereum", "polygon", "arbitrum", "optimism" -> isValidEthereumAddress(address);
            case "bitcoin" -> isValidBitcoinAddress(address);
            case "solana" -> isValidSolanaAddress(address);
            default -> false;
        };
    }

    private boolean isValidEthereumAddress(String address) {
        return ETHEREUM_ADDRESS_PATTERN.matcher(address).matches();
    }

    private boolean isValidBitcoinAddress(String address) {
        return BITCOIN_ADDRESS_PATTERN.matcher(address).matches();
    }

    private boolean isValidSolanaAddress(String address) {
        // Solana addresses are base58, 32-44 characters
        return address.matches("^[1-9A-HJ-NP-Za-km-z]{32,44}$");
    }

    // ---------------------------
    // 3. Balance Fetching
    // ---------------------------

    /**
     * Get balance for a wallet from blockchain
     */
    public BigDecimal getWalletBalance(CryptoWallet wallet) {
        return switch (wallet.getBlockchain().toLowerCase()) {
            case "ethereum" -> getEthereumBalance(wallet.getWalletAddress());
            case "bitcoin" -> getBitcoinBalance(wallet.getWalletAddress());
            case "polygon" -> getPolygonBalance(wallet.getWalletAddress());
            default -> BigDecimal.ZERO;
        };
    }

    /**
     * Get Ethereum balance from Etherscan API
     */
    public BigDecimal getEthereumBalance(String address) {
        try {
            String url = String.format(
                "https://api.etherscan.io/api?module=account&action=balance&address=%s&tag=latest&apikey=%s",
                address,
                etherscanApiKey
            );

            String response = restTemplate.getForObject(url, String.class);
            JsonNode json = objectMapper.readTree(response);

            if (json.get("status").asText().equals("1")) {
                // Balance is in Wei (1 ETH = 10^18 Wei)
                String balanceWei = json.get("result").asText();
                BigDecimal balance = new BigDecimal(balanceWei);
                return balance.divide(new BigDecimal("1000000000000000000"), 18, RoundingMode.HALF_UP);
            }

            return BigDecimal.ZERO;
        } catch (Exception e) {
            System.err.println("Error fetching Ethereum balance: " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    /**
     * Get Bitcoin balance from Blockchain.com API
     */
    public BigDecimal getBitcoinBalance(String address) {
        try {
            // Try primary API endpoint
            String url = String.format("https://blockchain.info/q/addressbalance/%s", address);
            String response = restTemplate.getForObject(url, String.class);

            // Response is just the balance in Satoshis as plain text
            long balanceSatoshis = Long.parseLong(response.trim());
            BigDecimal balance = new BigDecimal(balanceSatoshis);
            return balance.divide(new BigDecimal("100000000"), 8, RoundingMode.HALF_UP);

        } catch (Exception e) {
            System.err.println("Error fetching Bitcoin balance (primary): " + e.getMessage());

            // Fallback to alternative API
            try {
                String fallbackUrl = String.format("https://blockchain.info/balance?active=%s", address);
                String response = restTemplate.getForObject(fallbackUrl, String.class);
                JsonNode json = objectMapper.readTree(response);

                if (json.has(address)) {
                    long balanceSatoshis = json.get(address).get("final_balance").asLong();
                    BigDecimal balance = new BigDecimal(balanceSatoshis);
                    return balance.divide(new BigDecimal("100000000"), 8, RoundingMode.HALF_UP);
                }
            } catch (Exception fallbackError) {
                System.err.println("Error fetching Bitcoin balance (fallback): " + fallbackError.getMessage());
            }

            return BigDecimal.ZERO;
        }
    }

    /**
     * Get Polygon balance (uses Polygonscan API - same format as Etherscan)
     */
    public BigDecimal getPolygonBalance(String address) {
        try {
            // Note: You'll need a Polygonscan API key for this
            String url = String.format(
                "https://api.polygonscan.com/api?module=account&action=balance&address=%s&tag=latest&apikey=%s",
                address,
                etherscanApiKey // Can reuse or use separate key
            );

            String response = restTemplate.getForObject(url, String.class);
            JsonNode json = objectMapper.readTree(response);

            if (json.get("status").asText().equals("1")) {
                String balanceWei = json.get("result").asText();
                BigDecimal balance = new BigDecimal(balanceWei);
                return balance.divide(new BigDecimal("1000000000000000000"), 18, RoundingMode.HALF_UP);
            }

            return BigDecimal.ZERO;
        } catch (Exception e) {
            System.err.println("Error fetching Polygon balance: " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    // ---------------------------
    // 4. Transaction Syncing
    // ---------------------------

    /**
     * Sync transactions for a wallet from blockchain
     */
    public List<CryptoTransaction> syncTransactions(CryptoWallet wallet) {
        return switch (wallet.getBlockchain().toLowerCase()) {
            case "ethereum" -> syncEthereumTransactions(wallet);
            case "bitcoin" -> syncBitcoinTransactions(wallet);
            case "polygon" -> syncPolygonTransactions(wallet);
            default -> new ArrayList<>();
        };
    }

    /**
     * Sync Ethereum transactions from Etherscan API
     */
    public List<CryptoTransaction> syncEthereumTransactions(CryptoWallet wallet) {
        List<CryptoTransaction> synced = new ArrayList<>();

        try {
            String url = String.format(
                "https://api.etherscan.io/api?module=account&action=txlist&address=%s&startblock=0&endblock=99999999&page=1&offset=100&sort=desc&apikey=%s",
                wallet.getWalletAddress(),
                etherscanApiKey
            );

            String response = restTemplate.getForObject(url, String.class);
            JsonNode json = objectMapper.readTree(response);

            if (json.get("status").asText().equals("1")) {
                JsonNode transactions = json.get("result");

                for (JsonNode tx : transactions) {
                    String txHash = tx.get("hash").asText();

                    // Check if transaction already exists (idempotent sync)
                    if (cryptoTransactionRepository.existsByTxHash(txHash)) {
                        continue; // Skip duplicates
                    }

                    // Parse transaction data
                    long timestamp = tx.get("timeStamp").asLong();
                    LocalDateTime date = LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(timestamp),
                        ZoneId.systemDefault()
                    );

                    String fromAddress = tx.get("from").asText();
                    String toAddress = tx.get("to").asText();

                    // Convert Wei to ETH
                    BigDecimal valueWei = new BigDecimal(tx.get("value").asText());
                    BigDecimal amount = valueWei.divide(new BigDecimal("1000000000000000000"), 18, RoundingMode.HALF_UP);

                    // Determine transaction type
                    String type = fromAddress.equalsIgnoreCase(wallet.getWalletAddress()) ? "send" : "receive";

                    // Gas fee
                    BigDecimal gasUsed = new BigDecimal(tx.get("gasUsed").asText());
                    BigDecimal gasPrice = new BigDecimal(tx.get("gasPrice").asText());
                    BigDecimal gasFeeWei = gasUsed.multiply(gasPrice);
                    BigDecimal gasFee = gasFeeWei.divide(new BigDecimal("1000000000000000000"), 18, RoundingMode.HALF_UP);

                    // Create transaction
                    CryptoTransaction cryptoTx = new CryptoTransaction(
                        txHash, wallet, date, fromAddress, toAddress, amount, "ETH", type
                    );
                    cryptoTx.setGasFee(gasFee);
                    cryptoTx.setBlockNumber(tx.get("blockNumber").asLong());
                    cryptoTx.setConfirmations(tx.has("confirmations") ? tx.get("confirmations").asInt() : null);

                    synced.add(cryptoTransactionRepository.save(cryptoTx));
                }
            }
        } catch (Exception e) {
            System.err.println("Error syncing Ethereum transactions: " + e.getMessage());
            e.printStackTrace();
        }

        return synced;
    }

    /**
     * Sync Bitcoin transactions from Blockchain.com API
     */
    public List<CryptoTransaction> syncBitcoinTransactions(CryptoWallet wallet) {
        List<CryptoTransaction> synced = new ArrayList<>();

        try {
            String url = String.format("https://blockchain.info/rawaddr/%s?limit=50", wallet.getWalletAddress());

            String response = restTemplate.getForObject(url, String.class);
            JsonNode json = objectMapper.readTree(response);

            if (json.has("txs")) {
                JsonNode transactions = json.get("txs");

                for (JsonNode tx : transactions) {
                    String txHash = tx.get("hash").asText();

                    // Check if transaction already exists
                    if (cryptoTransactionRepository.existsByTxHash(txHash)) {
                        continue;
                    }

                    // Parse transaction data
                    long timestamp = tx.get("time").asLong();
                    LocalDateTime date = LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(timestamp),
                        ZoneId.systemDefault()
                    );

                    // Calculate amount and determine type
                    JsonNode inputs = tx.get("inputs");
                    JsonNode outputs = tx.get("out");

                    String fromAddress = inputs.size() > 0 ? inputs.get(0).get("prev_out").get("addr").asText() : "unknown";
                    String toAddress = outputs.size() > 0 ? outputs.get(0).get("addr").asText() : "unknown";

                    long result = tx.has("result") ? tx.get("result").asLong() : 0;
                    BigDecimal amount = new BigDecimal(Math.abs(result)).divide(new BigDecimal("100000000"), 8, RoundingMode.HALF_UP);

                    String type = result < 0 ? "send" : "receive";

                    // Create transaction
                    CryptoTransaction cryptoTx = new CryptoTransaction(
                        txHash, wallet, date, fromAddress, toAddress, amount, "BTC", type
                    );
                    cryptoTx.setBlockNumber(tx.has("block_height") ? tx.get("block_height").asLong() : null);

                    synced.add(cryptoTransactionRepository.save(cryptoTx));
                }
            }
        } catch (Exception e) {
            System.err.println("Error syncing Bitcoin transactions: " + e.getMessage());
            e.printStackTrace();
        }

        return synced;
    }

    /**
     * Sync Polygon transactions (same API format as Ethereum)
     */
    public List<CryptoTransaction> syncPolygonTransactions(CryptoWallet wallet) {
        List<CryptoTransaction> synced = new ArrayList<>();

        try {
            String url = String.format(
                "https://api.polygonscan.com/api?module=account&action=txlist&address=%s&startblock=0&endblock=99999999&page=1&offset=100&sort=desc&apikey=%s",
                wallet.getWalletAddress(),
                etherscanApiKey
            );

            String response = restTemplate.getForObject(url, String.class);
            JsonNode json = objectMapper.readTree(response);

            if (json.get("status").asText().equals("1")) {
                JsonNode transactions = json.get("result");

                for (JsonNode tx : transactions) {
                    String txHash = tx.get("hash").asText();

                    if (cryptoTransactionRepository.existsByTxHash(txHash)) {
                        continue;
                    }

                    long timestamp = tx.get("timeStamp").asLong();
                    LocalDateTime date = LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(timestamp),
                        ZoneId.systemDefault()
                    );

                    String fromAddress = tx.get("from").asText();
                    String toAddress = tx.get("to").asText();

                    BigDecimal valueWei = new BigDecimal(tx.get("value").asText());
                    BigDecimal amount = valueWei.divide(new BigDecimal("1000000000000000000"), 18, RoundingMode.HALF_UP);

                    String type = fromAddress.equalsIgnoreCase(wallet.getWalletAddress()) ? "send" : "receive";

                    BigDecimal gasUsed = new BigDecimal(tx.get("gasUsed").asText());
                    BigDecimal gasPrice = new BigDecimal(tx.get("gasPrice").asText());
                    BigDecimal gasFeeWei = gasUsed.multiply(gasPrice);
                    BigDecimal gasFee = gasFeeWei.divide(new BigDecimal("1000000000000000000"), 18, RoundingMode.HALF_UP);

                    CryptoTransaction cryptoTx = new CryptoTransaction(
                        txHash, wallet, date, fromAddress, toAddress, amount, "MATIC", type
                    );
                    cryptoTx.setGasFee(gasFee);
                    cryptoTx.setBlockNumber(tx.get("blockNumber").asLong());

                    synced.add(cryptoTransactionRepository.save(cryptoTx));
                }
            }
        } catch (Exception e) {
            System.err.println("Error syncing Polygon transactions: " + e.getMessage());
            e.printStackTrace();
        }

        return synced;
    }

    // ---------------------------
    // 5. Price Data
    // ---------------------------

    /**
     * Get current USD price for a token
     */
    public BigDecimal getTokenPrice(String symbol) {
        try {
            // Map token symbols to CoinGecko IDs
            String coinId = switch (symbol.toUpperCase()) {
                case "ETH" -> "ethereum";
                case "BTC" -> "bitcoin";
                case "MATIC" -> "matic-network";
                default -> symbol.toLowerCase();
            };

            String url = String.format(
                "https://api.coingecko.com/api/v3/simple/price?ids=%s&vs_currencies=usd",
                coinId
            );

            String response = restTemplate.getForObject(url, String.class);
            JsonNode json = objectMapper.readTree(response);

            if (json.has(coinId) && json.get(coinId).has("usd")) {
                return new BigDecimal(json.get(coinId).get("usd").asText());
            }

            return BigDecimal.ZERO;
        } catch (Exception e) {
            System.err.println("Error fetching token price: " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    /**
     * Get all transactions for a user across all wallets
     */
    public List<CryptoTransaction> getTransactionsForUser(Integer userId) {
        return cryptoTransactionRepository.findByUserId(userId);
    }
}
