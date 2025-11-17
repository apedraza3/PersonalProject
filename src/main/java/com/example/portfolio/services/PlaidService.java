package com.example.portfolio.services;

import com.example.portfolio.models.Account;
import com.example.portfolio.models.PlaidItem;
import com.example.portfolio.models.User;
import com.example.portfolio.repositories.AccountRepository;
import com.example.portfolio.repositories.PlaidItemRepository;
import com.plaid.client.request.PlaidApi;
import com.plaid.client.model.LinkTokenCreateRequest;
import com.plaid.client.model.LinkTokenCreateRequestUser;
import com.plaid.client.model.LinkTokenCreateResponse;
import com.plaid.client.model.Products;
import com.plaid.client.model.CountryCode;
import com.plaid.client.model.ItemPublicTokenExchangeRequest;
import com.plaid.client.model.ItemPublicTokenExchangeResponse;
import com.plaid.client.model.AccountBase;
import com.plaid.client.model.AccountsGetRequest;
import com.plaid.client.model.AccountsGetResponse;
import org.springframework.stereotype.Service;
import retrofit2.Response;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PlaidService {

    private final PlaidApi plaidApi;
    private final PlaidItemRepository plaidItemRepository;
    private final AccountRepository accountRepository;

    public PlaidService(
            PlaidApi plaidApi,
            PlaidItemRepository plaidItemRepository,
            AccountRepository accountRepository) {
        this.plaidApi = plaidApi;
        this.plaidItemRepository = plaidItemRepository;
        this.accountRepository = accountRepository;
    }

    // ---------------------------
    // 1. Create Link Token
    // ---------------------------
    public String createLinkToken(User user) throws IOException {
        LinkTokenCreateRequestUser linkUser = new LinkTokenCreateRequestUser()
                .clientUserId(String.valueOf(user.getId()));

        LinkTokenCreateRequest request = new LinkTokenCreateRequest()
                .clientName("My Finance App")
                .language("en")
                .products(List.of(Products.TRANSACTIONS))
                .countryCodes(List.of(CountryCode.US))
                .user(linkUser);

        Response<LinkTokenCreateResponse> response = plaidApi.linkTokenCreate(request).execute();

        if (!response.isSuccessful() || response.body() == null) {
            throw new IOException("Plaid error creating link token: " +
                    (response.errorBody() != null ? response.errorBody().string() : "unknown"));
        }

        return response.body().getLinkToken();
    }

    // ---------------------------
    // 2. Exchange Public Token
    // ---------------------------
    public PlaidItem exchangePublicToken(User user, String publicToken) throws IOException {
        ItemPublicTokenExchangeRequest request = new ItemPublicTokenExchangeRequest().publicToken(publicToken);

        Response<ItemPublicTokenExchangeResponse> response = plaidApi.itemPublicTokenExchange(request).execute();

        if (!response.isSuccessful() || response.body() == null) {
            throw new IOException("Plaid error exchanging public token: " +
                    (response.errorBody() != null ? response.errorBody().string() : "unknown"));
        }

        String accessToken = response.body().getAccessToken();
        String itemId = response.body().getItemId();

        PlaidItem item = new PlaidItem(itemId, accessToken, null, user);
        return plaidItemRepository.save(item);
    }

    // ---------------------------
    // 3. Sync accounts for a user
    // ---------------------------
    public List<Account> syncAccountsForUser(User user) throws IOException {
        // requires: List<PlaidItem> findByOwnerId(Integer ownerId) in
        // PlaidItemRepository
        List<PlaidItem> items = plaidItemRepository.findByOwnerId(user.getId());
        List<Account> result = new ArrayList<>();

        for (PlaidItem item : items) {
            AccountsGetRequest request = new AccountsGetRequest()
                    .accessToken(item.getAccessToken());

            Response<AccountsGetResponse> response = plaidApi.accountsGet(request).execute();

            if (!response.isSuccessful() || response.body() == null) {
                throw new IOException("Error fetching accounts from Plaid: " +
                        (response.errorBody() != null ? response.errorBody().string() : "unknown"));
            }

            for (AccountBase pa : response.body().getAccounts()) {
                // For now we just create new Account rows for each Plaid account
                Account acc = new Account();

                // Map Plaid -> your fields
                acc.setAccountName(pa.getName());
                acc.setInstitutionString(item.getInstitutionName()); // may be null for now

                // Use subtype if present, else type
                if (pa.getSubtype() != null) {
                    acc.setAccountType(pa.getSubtype().toString());
                } else if (pa.getType() != null) {
                    acc.setAccountType(pa.getType().toString());
                }

                if (pa.getBalances() != null && pa.getBalances().getCurrent() != null) {
                    acc.setBalance(BigDecimal.valueOf(
                            pa.getBalances().getCurrent().doubleValue()));
                }

                acc.setUser(user);
                acc.setCreatedAt(LocalDateTime.now());
                acc.setUpdatedAt(LocalDateTime.now());

                result.add(accountRepository.save(acc));
            }
        }

        return result;
    }
}