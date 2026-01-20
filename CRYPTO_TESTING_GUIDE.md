# Crypto Wallet Integration - Testing Guide

##  Implementation Complete!

**Option 1 (Read-Only Wallet Tracking)** is now fully implemented and ready to test!

---

## <‰ What's Been Built

### **Backend (Complete)**
-  CryptoWallet entity - Stores wallet addresses
-  CryptoTransaction entity - Stores blockchain transactions
-  CryptoWalletRepository - Database queries
-  CryptoTransactionRepository - Transaction queries
-  Database migration V6 - Creates crypto tables
-  CryptoService - Blockchain API integration (Ethereum, Bitcoin, Polygon)
-  CryptoController - REST API endpoints
-  UserService updated - Crypto wallet deletion on user delete

### **Frontend (Complete)**
-  Crypto Wallets section on dashboard
-  Add Wallet modal with form
-  Wallet cards with balances (crypto + USD)
-  Sync transactions button
-  Delete wallet button
-  Crypto Transactions table
-  All JavaScript functions with CSRF protection

### **Configuration (Complete)**
-  application.properties - API key configuration
-  .env.example - Template for API keys

---

## =€ How to Test

### **Step 1: Get API Keys (Optional but Recommended)**

#### **Etherscan API (for Ethereum)**
1. Go to https://etherscan.io/register
2. Create a free account
3. Go to https://etherscan.io/myapikey
4. Click "Add" to create a new API key
5. Copy your API key

**Add to your `.env` file:**
```bash
ETHERSCAN_API_KEY=your_etherscan_api_key_here
```

**Note:** Without an API key, you'll hit rate limits quickly (5 calls/sec ’ 1 call/5sec). The free tier with a key gives you 5 calls/second.

#### **CoinGecko API (for Prices)**
- Free tier doesn't need an API key
- Prices will work automatically

---

### **Step 2: Restart Your Application**

The migration will run automatically on startup:

```bash
# Stop your current app (Ctrl+C)

# Restart
mvn spring-boot:run
```

**Look for this in the logs:**
```
Migrating schema `portfolio` to version "6 - Add crypto tables"
Successfully applied 1 migration to schema `portfolio`
```

---

### **Step 3: Test Adding a Wallet**

#### **Option A: Use a Real Ethereum Address**

Find any Ethereum address on Etherscan to test with. Here are some examples:

**Vitalik Buterin's Address (Ethereum founder)**:
```
0xd8dA6BF26964aF9D7eEd9e03E53415D37aA96045
```

**USDC Contract Address** (has lots of transactions):
```
0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48
```

**Steps:**
1. Open your app: http://localhost:8080
2. Login to your account
3. Scroll to "Crypto Wallets" section
4. Click "Add Wallet"
5. Fill in the form:
   - **Blockchain**: Ethereum
   - **Wallet Address**: `0xd8dA6BF26964aF9D7eEd9e03E53415D37aA96045`
   - **Wallet Name**: `Vitalik's Wallet`
6. Click "Add Wallet"

**Expected Result:**
-  "Wallet added successfully!" alert
-  Wallet card appears with current balance
-  Shows ETH balance and USD value

#### **Option B: Bitcoin Address**

**Satoshi Nakamoto's Address** (first Bitcoin address):
```
1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa
```

**Steps:**
1. Click "Add Wallet"
2. Select "Bitcoin"
3. Enter address: `1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa`
4. Name: `Satoshi's Wallet`
5. Click "Add Wallet"

**Expected Result:**
-  Shows BTC balance

---

### **Step 4: Sync Transactions**

1. Click the "Sync" button on any wallet card
2. Wait a few seconds

**Expected Result:**
-  "Synced X new transactions" alert
-  Transactions appear in "Crypto Transactions" table
-  Shows tx hash, date, type, amount, token

**What's Happening:**
- Calls Etherscan/Blockchain.com API
- Fetches last 100 transactions
- Saves to database (idempotent - won't duplicate)
- Displays in UI

---

### **Step 5: Verify Data Persistence**

1. Refresh the page (F5)
2. Wallets and transactions should still be there

**Check the database:**
```sql
USE portfolio;

-- Check wallets
SELECT * FROM crypto_wallets;

-- Check transactions
SELECT * FROM crypto_transactions LIMIT 10;
```

---

### **Step 6: Test Deletion**

1. Click "Delete" button on a wallet
2. Confirm the deletion
3. Wallet should disappear
4. Associated transactions also deleted (CASCADE)

---

## >ê Full Test Checklist

### **Wallet Management**
- [ ] Can add Ethereum wallet
- [ ] Can add Bitcoin wallet
- [ ] Can add Polygon wallet
- [ ] Can add Solana wallet (address validation)
- [ ] Balance displays correctly
- [ ] USD value displays correctly
- [ ] Wallet name is optional
- [ ] Duplicate wallet prevention works
- [ ] Invalid address is rejected
- [ ] Can delete wallet

### **Transaction Syncing**
- [ ] Sync button works
- [ ] Transactions appear in table
- [ ] Transaction details are accurate
- [ ] No duplicate transactions (run sync twice)
- [ ] Tx hash links to Etherscan
- [ ] Date displays correctly
- [ ] Amount displays with correct decimals

### **UI/UX**
- [ ] Modal opens and closes
- [ ] Form validation works
- [ ] Loading states display
- [ ] Empty states display
- [ ] Error messages display
- [ ] CSRF protection works (no 403 errors)

### **Security**
- [ ] Only public addresses stored
- [ ] No private keys anywhere
- [ ] CSRF tokens work on all endpoints
- [ ] User can only see their own wallets
- [ ] User can only delete their own wallets

---

## = Troubleshooting

### **Problem: "Failed to add wallet: Invalid address"**

**Solution:** Check address format:
- Ethereum: Must start with `0x` and be 42 characters (40 hex chars)
- Bitcoin: Must start with `1`, `3`, or `bc1` and be 26-62 characters
- Make sure you copied the full address

### **Problem: "Failed to load wallets" or Empty balance**

**Possible causes:**
1. **API rate limit** - Wait 5-10 seconds and refresh
2. **No API key** - Add Etherscan API key to `.env`
3. **Network issue** - Check internet connection
4. **Invalid address** - Use a known active wallet

**Check logs:**
```bash
# Look for errors in Spring console
Error fetching Ethereum balance: ...
```

### **Problem: Migration didn't run (tables don't exist)**

**Solution:**
```bash
# Check Flyway status
mysql -u root -p portfolio -e "SELECT * FROM flyway_schema_history;"

# If V6 is missing, restart app
mvn spring-boot:run
```

### **Problem: 403 Forbidden errors**

**Solution:** CSRF token refresh is built into all crypto functions. If you still get 403:
1. Clear browser cookies
2. Logout and login again
3. Try again

### **Problem: "Wallet already added"**

**Solution:** You already have this wallet. Each wallet can only be added once per user.

---

## =Ê Expected API Behavior

### **Etherscan API**

**Without API key:**
-   1 call per 5 seconds (very slow)
- May get rate limited quickly

**With API key (free tier):**
-  5 calls per second
-  Much more reliable

### **Blockchain.com API (Bitcoin)**

-  No API key required
-  200 requests/hour without key
- Publicly accessible

### **CoinGecko API (Prices)**

-  No API key required for free tier
-  10-50 calls/minute
- Very reliable

---

## <¯ Success Criteria

Your crypto integration is working if:

1.  You can add at least one Ethereum wallet
2.  The wallet shows a correct balance
3.  The balance converts to USD correctly
4.  You can sync transactions
5.  Transactions display in the table
6.  You can delete a wallet
7.  Data persists after page refresh
8.  No 403/500 errors in console

---

## =€ What's Next?

### **Enhancements You Can Add**

1. **More Blockchains**:
   - Add Solana support (Solscan API)
   - Add Binance Smart Chain (BscScan API)
   - Add Arbitrum, Optimism, etc.

2. **Better Transaction Display**:
   - Filter by date range
   - Filter by token type
   - Pagination for large transaction lists
   - Search transactions

3. **Portfolio Analytics**:
   - Total portfolio value across all wallets
   - Portfolio breakdown by token
   - Price charts (historical data)
   - Profit/loss tracking

4. **Notifications**:
   - Email alerts for large transactions
   - Balance change notifications
   - Price alerts

5. **Option 3 - Exchange Integration**:
   - Coinbase OAuth
   - Binance API
   - Kraken API
   - Combine CEX + DEX holdings

---

## =Ý Test Wallet Addresses

Here are some public wallet addresses you can use for testing:

### **Ethereum**
```
Vitalik Buterin: 0xd8dA6BF26964aF9D7eEd9e03E53415D37aA96045
USDC Contract: 0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48
Uniswap Router: 0x7a250d5630B4cF539739dF2C5dAcb4c659F2488D
```

### **Bitcoin**
```
Satoshi's Address: 1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa
Pizza Transaction: 1XPTgDRhN8RFnzniWCddobD9iKZatrvH4
```

### **Polygon (MATIC)**
```
Use any Ethereum address - Polygon uses same format
```

---

## =Ú API Documentation

- **Etherscan**: https://docs.etherscan.io/
- **Blockchain.com**: https://www.blockchain.com/explorer/api
- **CoinGecko**: https://docs.coingecko.com/reference/introduction
- **Polygonscan**: https://docs.polygonscan.com/

---

## <“ What You Learned

Through this implementation, you now understand:

1. **Blockchain APIs** - How to query balances and transactions
2. **Public vs Private Keys** - Why public addresses are safe to store
3. **Idempotent Syncing** - Using unique transaction hashes
4. **Address Validation** - Regex patterns for different blockchains
5. **Wei/Satoshi Conversions** - Handling cryptocurrency decimals
6. **Real-time Price APIs** - Converting crypto to fiat
7. **CSRF Protection** - Token refresh for secure API calls

---

##  Next Session Goals

When you continue development:

1. **Test with your own wallet** (if you have one)
2. **Add more blockchains** (Solana, BSC, Avalanche)
3. **Improve error handling** (better user feedback)
4. **Add loading spinners** (better UX)
5. **Implement Option 3** (Exchange OAuth integration)

---

<‰ **Congratulations!** Your crypto wallet tracking is now live!

Try it out and let me know if you run into any issues!
