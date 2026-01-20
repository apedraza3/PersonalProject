# Crypto Wallet Integration - Implementation Summary

## =Ë Project Goal

Add cryptocurrency wallet tracking to your finance tracker app, allowing users to:
1. **Option 1** (In Progress): Track wallet balances via public addresses (read-only, no private keys)
2. **Option 3** (Future): Connect exchange accounts (Coinbase, Binance) via OAuth

---

##  What's Been Completed

### **1. Database Models (Entities)**

#### **CryptoWallet Entity**
- **Location**: `src/main/java/com/example/portfolio/models/CryptoWallet.java`
- **Purpose**: Stores user's cryptocurrency wallet addresses
- **Key Fields**:
  - `walletAddress` - Public wallet address (e.g., `0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb`)
  - `blockchain` - Type of blockchain (`ethereum`, `bitcoin`, `solana`, `polygon`)
  - `walletName` - User-friendly name (e.g., "My Main Wallet")
  - `owner` - Link to User who owns the wallet
  - `createdAt`, `updatedAt` - Timestamps

**Security Note**: Only public addresses are stored - NO private keys!

#### **CryptoTransaction Entity**
- **Location**: `src/main/java/com/example/portfolio/models/CryptoTransaction.java`
- **Purpose**: Stores blockchain transactions synced from APIs
- **Key Fields**:
  - `txHash` - Unique blockchain transaction hash
  - `wallet` - Link to CryptoWallet
  - `date` - Transaction timestamp
  - `fromAddress`, `toAddress` - Sender/receiver
  - `amount` - Transaction amount (DECIMAL 30,18 for precision)
  - `token` - Token type (`ETH`, `BTC`, `USDC`, etc.)
  - `type` - Transaction type (`send`, `receive`, `swap`)
  - `gasFee` - Transaction fee
  - `blockNumber`, `confirmations` - Blockchain metadata

### **2. Database Repositories**

#### **CryptoWalletRepository**
- **Location**: `src/main/java/com/example/portfolio/repositories/CryptoWalletRepository.java`
- **Methods**:
  - `findByOwner(User)` - Get all wallets for user
  - `findByOwner_Id(Integer)` - Get wallets by user ID
  - `findByOwner_IdAndBlockchain(Integer, String)` - Filter by blockchain
  - `findByWalletAddressAndOwner_Id(String, Integer)` - Find specific wallet
  - `existsByWalletAddressAndOwner_Id(String, Integer)` - Check for duplicates
  - `deleteByOwner_Id(Integer)` - Cleanup on user deletion

#### **CryptoTransactionRepository**
- **Location**: `src/main/java/com/example/portfolio/repositories/CryptoTransactionRepository.java`
- **Methods**:
  - `findByTxHash(String)` - Find transaction by hash (idempotent sync)
  - `findByWallet(CryptoWallet)` - Get all transactions for wallet
  - `findByWalletOrderByDateDesc(CryptoWallet)` - Get sorted transactions
  - `findByUserId(Integer)` - Get all crypto transactions for user
  - `existsByTxHash(String)` - Prevent duplicate syncs
  - `deleteByWallet_Id(Integer)` - Cleanup on wallet deletion

### **3. Database Migration**

#### **V6__Add_crypto_tables.sql**
- **Location**: `src/main/resources/db/migration/V6__Add_crypto_tables.sql`
- **Tables Created**:
  1. `crypto_wallets` - Stores wallet addresses
  2. `crypto_transactions` - Stores blockchain transactions
- **Indexes**:
  - `idx_user_id` - Fast user wallet lookups
  - `idx_blockchain` - Filter by blockchain type
  - `idx_wallet_address` - Fast address lookups
  - `idx_tx_hash` - Fast transaction lookups
  - `idx_date` - Sort transactions by date
  - `idx_token` - Filter by token type
- **Constraints**:
  - `unique_wallet_per_user` - Prevent duplicate wallets per user
  - `UNIQUE tx_hash` - Prevent duplicate transactions
  - Foreign keys with `ON DELETE CASCADE` - Automatic cleanup

---

## =§ What's Remaining

### **Phase 1: Option 1 - Read-Only Wallet Tracking**

#### **1. CryptoService (Backend Logic)**
**File to create**: `src/main/java/com/example/portfolio/services/CryptoService.java`

**What it needs to do**:
- Validate wallet addresses (Ethereum, Bitcoin format validation)
- Call blockchain APIs to fetch balances:
  - Etherscan API for Ethereum
  - Blockchain.com API for Bitcoin
  - Solscan API for Solana
- Call blockchain APIs to fetch transactions
- Sync transactions to database (idempotent - prevent duplicates using `txHash`)
- Calculate USD values using price APIs (CoinGecko/CoinMarketCap)

**Key Methods Needed**:
```java
// Validate address format
public boolean isValidEthereumAddress(String address);
public boolean isValidBitcoinAddress(String address);

// Fetch balance from blockchain
public BigDecimal getEthereumBalance(String address);
public BigDecimal getBitcoinBalance(String address);

// Fetch transactions from blockchain
public List<CryptoTransaction> syncEthereumTransactions(CryptoWallet wallet);

// Get USD price
public BigDecimal getTokenPrice(String symbol);
```

**APIs to integrate**:
- **Etherscan** (Ethereum): https://api.etherscan.io/api
  - API Key required (free tier available)
  - Endpoints: `/api?module=account&action=balance`, `/api?module=account&action=txlist`
- **Blockchain.com** (Bitcoin): https://blockchain.info/
  - No API key needed for basic queries
  - Endpoints: `/balance`, `/rawaddr/{address}`
- **CoinGecko** (Prices): https://api.coingecko.com/api/v3/
  - No API key for free tier
  - Endpoint: `/simple/price?ids=ethereum&vs_currencies=usd`

#### **2. CryptoController (REST API)**
**File to create**: `src/main/java/com/example/portfolio/controllers/ApiController/CryptoController.java`

**Endpoints to implement**:
```java
// Add wallet
POST /api/crypto/wallets
Body: { "walletAddress": "0x...", "blockchain": "ethereum", "walletName": "My Wallet" }

// Get all wallets for current user
GET /api/crypto/wallets
Returns: [{ "id": 1, "address": "0x...", "blockchain": "ethereum", "balance": "1.5", "balanceUsd": "4500" }]

// Get specific wallet with transactions
GET /api/crypto/wallets/{id}
Returns: { "wallet": {...}, "transactions": [...], "balance": "1.5" }

// Delete wallet
DELETE /api/crypto/wallets/{id}

// Sync transactions for wallet
POST /api/crypto/wallets/{id}/sync
Returns: { "synced": 25, "newTransactions": 5 }

// Get all crypto transactions for user
GET /api/crypto/transactions
Returns: [{ "txHash": "0x...", "date": "2024-01-15", "amount": "0.5", "token": "ETH" }]
```

#### **3. Frontend UI (Dashboard)**
**File to modify**: `src/main/resources/META-INF/resources/WEB-INF/views/dashboard.jsp`

**UI Components to add**:

1. **Crypto Wallets Section**:
```html
<div class="section">
    <h3>Crypto Wallets</h3>
    <button onclick="showAddWalletModal()">Add Wallet</button>
    <div id="crypto-wallets-container">
        <!-- Will show wallet cards with balances -->
    </div>
</div>
```

2. **Add Wallet Modal**:
```html
<div id="add-wallet-modal" class="modal">
    <form onsubmit="addWallet(event)">
        <input type="text" name="walletAddress" placeholder="0x... or bc1..." required>
        <select name="blockchain">
            <option value="ethereum">Ethereum</option>
            <option value="bitcoin">Bitcoin</option>
            <option value="solana">Solana</option>
            <option value="polygon">Polygon</option>
        </select>
        <input type="text" name="walletName" placeholder="My Main Wallet">
        <button type="submit">Add Wallet</button>
    </form>
</div>
```

3. **JavaScript Functions**:
```javascript
async function loadCryptoWallets() {
    // Fetch from /api/crypto/wallets
    // Display wallet cards with balances
}

async function addWallet(event) {
    // POST to /api/crypto/wallets
    // Refresh wallet list
}

async function syncWalletTransactions(walletId) {
    // POST to /api/crypto/wallets/{id}/sync
    // Reload transactions
}

async function loadCryptoTransactions() {
    // Fetch from /api/crypto/transactions
    // Display in table
}
```

#### **4. Configuration (API Keys)**
**File to modify**: `src/main/resources/application.properties`

```properties
# Blockchain API Configuration
crypto.etherscan.api-key=${ETHERSCAN_API_KEY:}
crypto.blockcypher.api-key=${BLOCKCYPHER_API_KEY:}
crypto.coingecko.api-key=${COINGECKO_API_KEY:}

# API rate limits
crypto.api.rate-limit-per-minute=5
```

**File to modify**: `.env.example`
```bash
# Blockchain API Keys (Optional but recommended for higher rate limits)
ETHERSCAN_API_KEY=your_etherscan_api_key
BLOCKCYPHER_API_KEY=your_blockcypher_api_key
COINGECKO_API_KEY=your_coingecko_api_key
```

#### **5. Update UserService**
**File to modify**: `src/main/java/com/example/portfolio/services/UserService.java`

Add crypto wallet deletion to `deleteUserAndAllData()`:
```java
// Delete crypto wallets and transactions (cascade handles transactions)
cryptoWalletRepository.deleteByOwner_Id(userId);
```

---

### **Phase 2: Option 3 - Exchange OAuth Integration**

This phase adds integration with centralized exchanges (Coinbase, Binance) using OAuth.

#### **Files to Create**:

1. **ExchangeItem Entity**
   - Similar to PlaidItem
   - Stores encrypted OAuth access tokens
   - Fields: `exchange`, `accessToken`, `refreshToken`, `user`

2. **ExchangeItemRepository**
   - Standard JPA repository

3. **CoinbaseService**
   - OAuth flow implementation
   - Methods: `getAuthorizationUrl()`, `exchangeCode()`, `getAccounts()`, `getTransactions()`

4. **ExchangeController**
   - Endpoints for OAuth flow and data fetching

5. **Database Migration V7**
   - Create `exchange_items` table

6. **Frontend Integration**
   - "Connect Coinbase" button
   - OAuth callback handler
   - Display exchange accounts alongside wallets

---

## =' How to Continue Implementation

### **Step 1: Run the Migration**
```bash
# Restart your Spring Boot app
mvn spring-boot:run
```

Flyway will automatically create the `crypto_wallets` and `crypto_transactions` tables.

### **Step 2: Get API Keys**

**Etherscan (Ethereum)**:
1. Go to https://etherscan.io/register
2. Create free account
3. Go to https://etherscan.io/myapikey
4. Create API key
5. Add to `.env`: `ETHERSCAN_API_KEY=your_key_here`

**BlockCypher (Bitcoin)**:
1. Go to https://www.blockcypher.com/
2. Get free API key (optional - 200 requests/hour without key)
3. Add to `.env` if using: `BLOCKCYPHER_API_KEY=your_key_here`

**CoinGecko (Prices)**:
1. Free tier doesn't need API key
2. For higher limits: https://www.coingecko.com/en/api/pricing

### **Step 3: Implement CryptoService**

Create the service with blockchain API integration. Start with Ethereum support:

```java
@Service
public class CryptoService {
    @Value("${crypto.etherscan.api-key}")
    private String etherscanApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    // Implement methods here
}
```

### **Step 4: Implement CryptoController**

Create REST API endpoints for the frontend to call.

### **Step 5: Add Frontend UI**

Update `dashboard.jsp` with crypto wallet section.

### **Step 6: Test**

1. Add a wallet address (use a real Ethereum address from Etherscan)
2. Verify it fetches balance
3. Sync transactions
4. View transactions in UI

---

## =Ê Database Schema Overview

```
users
     crypto_wallets (1 user ’ many wallets)
           crypto_transactions (1 wallet ’ many transactions)

     exchange_items (1 user ’ many exchanges) [Phase 2]
```

**Relationships**:
- User ’ CryptoWallet: One-to-Many
- CryptoWallet ’ CryptoTransaction: One-to-Many
- User ’ ExchangeItem: One-to-Many (Phase 2)

---

## = Security Considerations

### **What's Safe**:
 Storing public wallet addresses - These are meant to be public
 Storing transaction hashes - Public blockchain data
 Fetching balances via APIs - Read-only operations

### **What to NEVER Do**:
L Store private keys - NEVER, EVER store private keys in database
L Ask for seed phrases - Don't request or store recovery phrases
L Sign transactions server-side - User must sign with their own wallet

### **Exchange OAuth Tokens** (Phase 2):
-  Encrypt access tokens with AES-256-GCM (like Plaid)
-  Use refresh tokens to get new access tokens
-  Store with `@Convert(converter = EncryptedStringConverter.class)`

---

## <¯ Testing Checklist

### **Option 1 - Wallet Tracking**:
- [ ] Migration creates tables successfully
- [ ] Can add Ethereum wallet address
- [ ] Fetches correct balance from Etherscan
- [ ] Can sync transactions from blockchain
- [ ] Transactions display in UI
- [ ] Can delete wallet
- [ ] Duplicate wallets are prevented
- [ ] Duplicate transactions are prevented (txHash unique)
- [ ] Works with multiple blockchains (Ethereum, Bitcoin)

### **Option 3 - Exchange OAuth** (Future):
- [ ] OAuth flow works with Coinbase
- [ ] Access tokens are encrypted in database
- [ ] Can fetch exchange balances
- [ ] Can sync exchange transactions
- [ ] Refresh token renewal works

---

## =Ú API Documentation Links

**Etherscan (Ethereum)**:
- Docs: https://docs.etherscan.io/
- Get Balance: https://docs.etherscan.io/api-endpoints/accounts#get-ether-balance-for-a-single-address
- Get Transactions: https://docs.etherscan.io/api-endpoints/accounts#get-a-list-of-normal-transactions-by-address

**Blockchain.com (Bitcoin)**:
- Docs: https://www.blockchain.com/explorer/api
- Get Balance: https://blockchain.info/balance?active=address
- Get Transactions: https://blockchain.info/rawaddr/address

**CoinGecko (Prices)**:
- Docs: https://docs.coingecko.com/reference/introduction
- Get Price: https://api.coingecko.com/api/v3/simple/price

**Coinbase API** (Phase 2):
- OAuth: https://docs.cloud.coinbase.com/sign-in-with-coinbase/docs/welcome
- Accounts: https://docs.cloud.coinbase.com/sign-in-with-coinbase/docs/api-accounts

---

## =€ Quick Start Commands

```bash
# 1. Add API keys to .env
echo "ETHERSCAN_API_KEY=your_key" >> .env

# 2. Restart app to run migration
mvn spring-boot:run

# 3. Verify tables created
mysql -u root -p portfolio -e "SHOW TABLES LIKE 'crypto%';"

# 4. Continue implementation with CryptoService
```

---

## =Ý Notes

- **Why two phases?** Option 1 is simpler (no OAuth) and teaches blockchain basics. Option 3 adds complexity but supports centralized exchanges.
- **Why Ethereum first?** Most popular blockchain for DeFi, widely used, good API documentation.
- **Rate limits**: Free tiers have limits. Etherscan: 5 calls/sec, CoinGecko: 10-50 calls/min.
- **Real-time updates**: Consider WebSocket for live balance updates in future.
- **Multi-chain**: Architecture supports any blockchain - just add new service methods.

---

## <“ Learning Resources

- **Ethereum Basics**: https://ethereum.org/en/developers/docs/
- **Bitcoin Basics**: https://developer.bitcoin.org/
- **Web3 Integration**: https://web3js.readthedocs.io/
- **OAuth 2.0**: https://oauth.net/2/

---

##  Current Status

**Completed**:
-  Database models (CryptoWallet, CryptoTransaction)
-  Repositories (CryptoWalletRepository, CryptoTransactionRepository)
-  Database migration (V6__Add_crypto_tables.sql)

**Next Steps**:
1. Create CryptoService (blockchain API integration)
2. Create CryptoController (REST endpoints)
3. Add UI to dashboard.jsp
4. Get API keys and test

**Estimated Time Remaining**:
- CryptoService: ~1-2 hours
- CryptoController: ~30 min
- Frontend UI: ~1 hour
- Testing: ~30 min

**Total**: 3-4 hours to complete Option 1

---

Good luck with the implementation! When you're ready to continue, just say "Let's continue with CryptoService" and I'll help you build it! =€
