package com.example.portfolio.services;

import com.example.portfolio.models.ExchangeItem;
import com.example.portfolio.models.User;
import com.example.portfolio.repositories.ExchangeItemRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Service for Coinbase OAuth integration and API calls
 * Handles OAuth flow, token management, and fetching account data
 */
@Service
public class CoinbaseService {

    @Value("${coinbase.client-id}")
    private String clientId;

    @Value("${coinbase.client-secret}")
    private String clientSecret;

    @Value("${coinbase.redirect-uri}")
    private String redirectUri;

    private final ExchangeItemRepository exchangeItemRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CoinbaseService(ExchangeItemRepository exchangeItemRepository) {
        this.exchangeItemRepository = exchangeItemRepository;
    }

    // ---------------------------
    // 1. OAuth Flow
    // ---------------------------

    /**
     * Generate OAuth authorization URL for user to connect Coinbase
     */
    public String getAuthorizationUrl(String state) {
        String scope = "wallet:accounts:read,wallet:transactions:read,wallet:buys:read,wallet:sells:read";

        return String.format(
            "https://www.coinbase.com/oauth/authorize?response_type=code&client_id=%s&redirect_uri=%s&state=%s&scope=%s",
            clientId,
            redirectUri,
            state,
            scope
        );
    }

    /**
     * Exchange authorization code for access token
     */
    public ExchangeItem exchangeCodeForToken(String code, User user) throws Exception {
        String url = "https://api.coinbase.com/oauth/token";

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("grant_type", "authorization_code");
        requestBody.put("code", code);
        requestBody.put("client_id", clientId);
        requestBody.put("client_secret", clientSecret);
        requestBody.put("redirect_uri", redirectUri);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Failed to exchange code for token");
        }

        JsonNode json = objectMapper.readTree(response.getBody());
        String accessToken = json.get("access_token").asText();
        String refreshToken = json.get("refresh_token").asText();
        int expiresIn = json.get("expires_in").asInt(); // seconds

        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(expiresIn);

        // Check if user already has Coinbase connected
        Optional<ExchangeItem> existing = exchangeItemRepository.findByOwner_IdAndExchange(user.getId(), "coinbase");
        if (existing.isPresent()) {
            // Update existing connection
            ExchangeItem item = existing.get();
            item.setAccessToken(accessToken);
            item.setRefreshToken(refreshToken);
            item.setTokenExpiresAt(expiresAt);
            return exchangeItemRepository.save(item);
        } else {
            // Create new connection
            ExchangeItem item = new ExchangeItem("coinbase", accessToken, refreshToken, expiresAt, user);
            item.setConnectionName("Coinbase");
            return exchangeItemRepository.save(item);
        }
    }

    /**
     * Refresh an expired access token using refresh token
     */
    public void refreshAccessToken(ExchangeItem item) throws Exception {
        String url = "https://api.coinbase.com/oauth/token";

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("grant_type", "refresh_token");
        requestBody.put("refresh_token", item.getRefreshToken());
        requestBody.put("client_id", clientId);
        requestBody.put("client_secret", clientSecret);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Failed to refresh access token");
        }

        JsonNode json = objectMapper.readTree(response.getBody());
        String accessToken = json.get("access_token").asText();
        int expiresIn = json.get("expires_in").asInt();

        item.setAccessToken(accessToken);
        item.setTokenExpiresAt(LocalDateTime.now().plusSeconds(expiresIn));
        exchangeItemRepository.save(item);
    }

    /**
     * Ensure access token is valid, refresh if needed
     */
    private void ensureValidToken(ExchangeItem item) throws Exception {
        if (item.getTokenExpiresAt() == null || LocalDateTime.now().isAfter(item.getTokenExpiresAt().minusMinutes(5))) {
            refreshAccessToken(item);
        }
    }

    // ---------------------------
    // 2. Coinbase API Calls
    // ---------------------------

    /**
     * Get all crypto accounts from Coinbase
     */
    public List<Map<String, Object>> getAccounts(ExchangeItem item) throws Exception {
        ensureValidToken(item);

        String url = "https://api.coinbase.com/v2/accounts";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(item.getAccessToken());

        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Failed to fetch Coinbase accounts");
        }

        JsonNode json = objectMapper.readTree(response.getBody());
        JsonNode data = json.get("data");

        List<Map<String, Object>> accounts = new ArrayList<>();
        if (data.isArray()) {
            for (JsonNode account : data) {
                Map<String, Object> accountData = new HashMap<>();
                accountData.put("id", account.get("id").asText());
                accountData.put("name", account.get("name").asText());
                accountData.put("currency", account.get("currency").asText());

                JsonNode balance = account.get("balance");
                accountData.put("balance", new BigDecimal(balance.get("amount").asText()));
                accountData.put("balanceCurrency", balance.get("currency").asText());

                // Get native balance (USD value)
                if (account.has("native_balance")) {
                    JsonNode nativeBalance = account.get("native_balance");
                    accountData.put("balanceUsd", new BigDecimal(nativeBalance.get("amount").asText()));
                } else {
                    accountData.put("balanceUsd", BigDecimal.ZERO);
                }

                accountData.put("type", account.get("type").asText());

                accounts.add(accountData);
            }
        }

        return accounts;
    }

    /**
     * Get transactions for a specific account
     */
    public List<Map<String, Object>> getTransactions(ExchangeItem item, String accountId) throws Exception {
        ensureValidToken(item);

        String url = String.format("https://api.coinbase.com/v2/accounts/%s/transactions", accountId);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(item.getAccessToken());

        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Failed to fetch transactions");
        }

        JsonNode json = objectMapper.readTree(response.getBody());
        JsonNode data = json.get("data");

        List<Map<String, Object>> transactions = new ArrayList<>();
        if (data.isArray()) {
            for (JsonNode tx : data) {
                Map<String, Object> txData = new HashMap<>();
                txData.put("id", tx.get("id").asText());
                txData.put("type", tx.get("type").asText());
                txData.put("status", tx.get("status").asText());

                JsonNode amount = tx.get("amount");
                txData.put("amount", new BigDecimal(amount.get("amount").asText()));
                txData.put("currency", amount.get("currency").asText());

                if (tx.has("native_amount")) {
                    JsonNode nativeAmount = tx.get("native_amount");
                    txData.put("amountUsd", new BigDecimal(nativeAmount.get("amount").asText()));
                }

                txData.put("createdAt", tx.get("created_at").asText());
                if (tx.has("description")) {
                    txData.put("description", tx.get("description").asText());
                }

                transactions.add(txData);
            }
        }

        return transactions;
    }

    /**
     * Disconnect Coinbase account
     */
    public void disconnectExchange(Integer itemId, Integer userId) {
        Optional<ExchangeItem> item = exchangeItemRepository.findById(itemId);
        if (item.isPresent() && item.get().getOwner().getId().equals(userId)) {
            exchangeItemRepository.delete(item.get());
        } else {
            throw new IllegalArgumentException("Exchange connection not found or unauthorized");
        }
    }

    /**
     * Get all exchange connections for user
     */
    public List<ExchangeItem> getExchangeItems(User user) {
        return exchangeItemRepository.findByOwner(user);
    }
}
