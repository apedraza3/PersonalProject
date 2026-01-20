# Coinbase OAuth Integration - Setup Guide

## ✅ Implementation Complete!

**Option 3 (Exchange OAuth Integration)** is now fully implemented! You can now connect your Coinbase account to see your real cryptocurrency balances.

---

## 🎯 What's Been Built

### **Backend (Complete)**
- ✅ ExchangeItem entity - Stores encrypted OAuth tokens
- ✅ ExchangeItemRepository - Database queries
- ✅ Database migration V7 - Creates exchange_items table
- ✅ CoinbaseService - OAuth flow and API integration
- ✅ ExchangeController - REST API endpoints for OAuth
- ✅ UserService updated - Exchange deletion on user delete

### **Frontend (Complete)**
- ✅ "Exchange Connections" section on dashboard
- ✅ "Connect Coinbase" button
- ✅ Automatic account loading after OAuth
- ✅ Display all Coinbase crypto balances (BTC, ETH, etc.)
- ✅ Disconnect exchange button
- ✅ USD value display for all holdings

### **Configuration (Complete)**
- ✅ application.properties - Coinbase OAuth config
- ✅ .env.example - Template for OAuth credentials

---

## 🔧 How to Set Up Coinbase OAuth

### **Step 1: Create a Coinbase OAuth Application**

1. **Go to Coinbase Developer Portal**
   - Visit: https://www.coinbase.com/settings/api
   - Sign in to your Coinbase account

2. **Create a New OAuth2 Application**
   - Click "+ New OAuth2 Application"
   - Fill in the details:
     - **Name**: `My Portfolio Tracker` (or any name)
     - **Description**: `Personal finance tracker`
     - **Redirect URI**: `http://localhost:8080/api/exchange/coinbase/callback`
     - **Website**: `http://localhost:8080` (optional)

3. **Set Permissions**
   - Select these scopes:
     - ✅ `wallet:accounts:read` - Read account balances
     - ✅ `wallet:transactions:read` - Read transaction history
     - ✅ `wallet:buys:read` - Read buy orders
     - ✅ `wallet:sells:read` - Read sell orders

4. **Save and Get Credentials**
   - After creating the app, you'll see:
     - **Client ID**: (example: `1234567890abcdef`)
     - **Client Secret**: (example: `abcdef1234567890` - keep this secret!)

---

### **Step 2: Add Credentials to Your `.env` File**

Open your `.env` file (create it from `.env.example` if you haven't) and add:

```bash
# Coinbase OAuth
COINBASE_CLIENT_ID=your_client_id_here
COINBASE_CLIENT_SECRET=your_client_secret_here
COINBASE_REDIRECT_URI=http://localhost:8080/api/exchange/coinbase/callback
```

**Example:**
```bash
COINBASE_CLIENT_ID=1234567890abcdef
COINBASE_CLIENT_SECRET=abcdef1234567890fedcba0987654321
COINBASE_REDIRECT_URI=http://localhost:8080/api/exchange/coinbase/callback
```

---

### **Step 3: Restart Your Application**

The migration will run automatically on startup:

```bash
# Stop your current app (Ctrl+C)

# Restart
mvn spring-boot:run
```

**Look for this in the logs:**
```
Migrating schema `portfolio` to version "7 - Add exchange items table"
Successfully applied 1 migration to schema `portfolio`
```

---

### **Step 4: Connect Your Coinbase Account**

1. **Open your app**: http://localhost:8080
2. **Login** to your portfolio tracker account
3. **Scroll to "Exchange Connections"** section
4. **Click "Connect Coinbase"** button
5. **You'll be redirected to Coinbase**
   - Login to your Coinbase account (if not already logged in)
   - Review the permissions requested
   - Click "Authorize" to grant access
6. **You'll be redirected back** to your dashboard
7. **See your real Coinbase balances!**

---

## 📊 What You'll See

### **Exchange Connections Section:**
```
┌─────────────────────────────────────────────┐
│ Exchange Connections     [Connect Coinbase] │
├─────────────────────────────────────────────┤
│ Coinbase                      [Disconnect]  │
│ Connected: 1/20/2026                        │
│                                             │
│ ┌─────────┐ ┌─────────┐ ┌─────────┐       │
│ │ BTC     │ │ ETH     │ │ USDC    │       │
│ │ 0.12345 │ │ 2.45678 │ │ 1000.00 │       │
│ │ $5,432  │ │ $6,789  │ │ $1,000  │       │
│ └─────────┘ └─────────┘ └─────────┘       │
└─────────────────────────────────────────────┘
```

---

## 🔐 Security Features

### **What's Encrypted:**
- ✅ **OAuth Access Token** - Encrypted with AES-256-GCM
- ✅ **OAuth Refresh Token** - Encrypted with AES-256-GCM
- ✅ **Automatic Token Refresh** - Tokens are refreshed before expiry

### **What's Stored:**
- Exchange name (e.g., "coinbase")
- Encrypted access token
- Encrypted refresh token
- Token expiration time
- Connection name (optional)
- User ID (foreign key)

### **What's NOT Stored:**
- ❌ Your Coinbase password
- ❌ Your private keys
- ❌ Any sensitive credentials in plaintext

---

## 🧪 Testing

### **Test Checklist:**

- [ ] Can click "Connect Coinbase" button
- [ ] Redirected to Coinbase OAuth page
- [ ] Can authorize the app
- [ ] Redirected back to dashboard
- [ ] See "Exchange Connections" section
- [ ] See all Coinbase crypto accounts
- [ ] See correct balances for each crypto
- [ ] See USD values for each balance
- [ ] Only accounts with balance > 0 are shown
- [ ] Can disconnect exchange
- [ ] Data persists after page refresh

---

## 🔄 OAuth Flow Diagram

```
1. User clicks "Connect Coinbase"
   ↓
2. Redirected to Coinbase with:
   - Client ID
   - Redirect URI
   - Requested scopes
   - CSRF state token
   ↓
3. User authorizes on Coinbase
   ↓
4. Coinbase redirects back with:
   - Authorization code
   - State token
   ↓
5. Backend exchanges code for tokens:
   - Access token (encrypted & stored)
   - Refresh token (encrypted & stored)
   ↓
6. Redirect to dashboard
   ↓
7. Frontend loads Coinbase accounts:
   - GET /api/exchange/coinbase/accounts
   - Displays balances
```

---

## 🛠️ API Endpoints

### **OAuth Flow:**
```
GET  /api/exchange/coinbase/connect
  → Initiates OAuth flow
  → Redirects to Coinbase

GET  /api/exchange/coinbase/callback?code=xxx&state=xxx
  → OAuth callback from Coinbase
  → Exchanges code for tokens
  → Redirects to dashboard
```

### **Data Fetching:**
```
GET  /api/exchange/connections
  → Get all connected exchanges for user
  → Returns: [{ id, exchange, connectionName, createdAt }]

GET  /api/exchange/coinbase/accounts
  → Get all Coinbase crypto accounts
  → Returns: [{ currency, balance, balanceUsd }]

GET  /api/exchange/coinbase/accounts/{accountId}/transactions
  → Get transactions for specific account
  → Returns: [{ type, amount, currency, createdAt }]
```

### **Disconnection:**
```
DELETE /api/exchange/{id}
  → Disconnect exchange
  → Deletes encrypted tokens from database
```

---

## ⚠️ Troubleshooting

### **Problem: "Invalid redirect_uri"**

**Cause:** The redirect URI in your Coinbase app settings doesn't match the one in your `.env` file.

**Solution:**
1. Check Coinbase app settings: https://www.coinbase.com/settings/api
2. Ensure redirect URI is: `http://localhost:8080/api/exchange/coinbase/callback`
3. Update `.env` file to match exactly
4. Restart your app

---

### **Problem: "Invalid client_id or client_secret"**

**Cause:** Credentials in `.env` don't match your Coinbase app.

**Solution:**
1. Go to Coinbase developer portal
2. Copy **Client ID** and **Client Secret** exactly
3. Paste into `.env` file (no quotes needed)
4. Restart your app

---

### **Problem: "Failed to fetch Coinbase accounts"**

**Possible Causes:**
1. **Access token expired** - Should auto-refresh, check logs
2. **Insufficient permissions** - Re-authorize with correct scopes
3. **Network issue** - Check internet connection

**Solution:**
- Check Spring console for error messages
- Try disconnecting and reconnecting Coinbase
- Ensure you granted all requested permissions

---

### **Problem: 403 Forbidden errors**

**Solution:**
- CSRF protection is built in
- Clear browser cookies
- Logout and login again

---

### **Problem: Database migration failed**

**Solution:**
```bash
# Check Flyway status
mysql -u root -p portfolio -e "SELECT * FROM flyway_schema_history;"

# If V7 is missing, restart app
mvn spring-boot:run
```

---

## 🎓 How It Works

### **OAuth 2.0 Flow:**
OAuth 2.0 is an authorization framework that allows third-party applications to access user data without exposing passwords.

**Key Components:**
- **Authorization Server**: Coinbase's OAuth server
- **Resource Server**: Coinbase's API servers
- **Client**: Your portfolio app
- **Resource Owner**: You (the Coinbase user)

**Token Types:**
- **Access Token**: Short-lived (2 hours), used to make API requests
- **Refresh Token**: Long-lived (90 days), used to get new access tokens

### **Why This is Secure:**
1. Your Coinbase password is never shared with the app
2. Tokens are encrypted in the database
3. Tokens can be revoked at any time from Coinbase settings
4. Limited permissions (read-only access)
5. CSRF protection with state parameter

---

## 🔮 What's Next?

### **Additional Exchanges:**
You can add more exchanges using the same pattern:

1. **Binance OAuth**
   - API: https://binance-docs.github.io/apidocs/
   - OAuth: Similar flow to Coinbase

2. **Kraken OAuth**
   - API: https://docs.kraken.com/rest/
   - OAuth flow available

3. **Gemini OAuth**
   - API: https://docs.gemini.com/rest-api/

### **Enhanced Features:**
- Portfolio analytics (total value, allocation %)
- Transaction history from exchanges
- Buy/sell order tracking
- Price alerts
- Historical performance charts

---

## 📚 API Documentation

### **Coinbase API:**
- Main Docs: https://docs.cloud.coinbase.com/
- OAuth Guide: https://docs.cloud.coinbase.com/sign-in-with-coinbase/docs/welcome
- API Reference: https://docs.cloud.coinbase.com/sign-in-with-coinbase/docs/api-users

### **Coinbase Scopes:**
- `wallet:accounts:read` - View account balances
- `wallet:transactions:read` - View transaction history
- `wallet:buys:read` - View buy orders
- `wallet:sells:read` - View sell orders
- `wallet:deposits:read` - View deposits
- `wallet:withdrawals:read` - View withdrawals

---

## ✨ Success!

Your Coinbase integration is now complete! You can:
- ✅ Connect your real Coinbase account
- ✅ See your actual cryptocurrency balances
- ✅ View USD values for all holdings
- ✅ Keep your Coinbase wallet and exchange balances in one place

No more manual wallet address tracking - your real exchange balances are now automatically synced! 🎉

---

## 📝 Notes

- **Rate Limits**: Coinbase has rate limits (15,000 requests/hour for OAuth apps)
- **Token Expiry**: Access tokens expire after 2 hours (auto-refreshed)
- **Refresh Tokens**: Valid for 90 days (need to re-authorize after)
- **Production**: For production, update redirect URI to your production domain
- **Privacy**: Only you can see your exchange connections and balances

---

## 🚀 Next Steps

1. **Test the integration** with your Coinbase account
2. **Add more exchanges** (Binance, Kraken, etc.)
3. **Build portfolio analytics** to see total holdings
4. **Add transaction tracking** to see buy/sell history
5. **Implement Option 2** (self-custody wallet signing) if needed

Happy trading! 📈
