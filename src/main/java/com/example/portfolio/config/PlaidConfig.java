package com.example.portfolio.config;

import com.plaid.client.ApiClient;
import com.plaid.client.request.PlaidApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class PlaidConfig {

    @Value("${plaid.client-id}")
    private String clientId;

    @Value("${plaid.secret}")
    private String secret;

    // sandbox | development | production
    @Value("${plaid.env:sandbox}")
    private String plaidEnv;

    @Bean
    public PlaidApi plaidApi() {
        // API keys map
        Map<String, String> apiKeys = new HashMap<>();
        apiKeys.put("clientId", clientId);
        apiKeys.put("secret", secret);

        ApiClient apiClient = new ApiClient(apiKeys);

        // Choose environment
        switch (plaidEnv.toLowerCase()) {
            case "sandbox" -> apiClient.setPlaidAdapter(ApiClient.Sandbox);
            case "development" -> apiClient.setPlaidAdapter(ApiClient.Development);
            case "production" -> apiClient.setPlaidAdapter(ApiClient.Production);
            default -> throw new IllegalArgumentException("Unsupported Plaid environment: " + plaidEnv);
        }

        // This is the interface you’ll inject into services/controllers
        return apiClient.createService(PlaidApi.class);
    }
}