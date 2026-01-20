<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard - FinanceTracker</title>
    <script src="https://cdn.plaid.com/link/v2/stable/link-initialize.js"></script>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
            background: #f5f7fa;
            color: #333;
        }

        .navbar {
            background: white;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
            padding: 15px 0;
        }

        .navbar-content {
            max-width: 1200px;
            margin: 0 auto;
            padding: 0 20px;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .logo {
            font-size: 24px;
            font-weight: 700;
            color: #667eea;
        }

        .user-menu {
            display: flex;
            align-items: center;
            gap: 20px;
        }

        .user-email {
            color: #666;
            font-size: 14px;
        }

        .btn-logout {
            padding: 8px 20px;
            background: #f56565;
            color: white;
            border: none;
            border-radius: 6px;
            cursor: pointer;
            font-size: 14px;
            font-weight: 500;
        }

        .btn-logout:hover {
            background: #e53e3e;
        }

        .container {
            max-width: 1200px;
            margin: 40px auto;
            padding: 0 20px;
        }

        .dashboard-header {
            margin-bottom: 30px;
        }

        .dashboard-header h1 {
            font-size: 32px;
            margin-bottom: 10px;
        }

        .dashboard-header p {
            color: #666;
        }

        .cards-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
            gap: 20px;
            margin-bottom: 30px;
        }

        .card {
            background: white;
            border-radius: 12px;
            padding: 24px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
        }

        .card h3 {
            font-size: 16px;
            color: #666;
            margin-bottom: 10px;
            font-weight: 500;
        }

        .card-value {
            font-size: 32px;
            font-weight: 700;
            color: #333;
            margin-bottom: 5px;
        }

        .card-label {
            font-size: 14px;
            color: #999;
        }

        .section {
            background: white;
            border-radius: 12px;
            padding: 24px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
            margin-bottom: 20px;
        }

        .section-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 20px;
        }

        .section-header h2 {
            font-size: 20px;
        }

        .btn {
            padding: 10px 20px;
            background: #667eea;
            color: white;
            border: none;
            border-radius: 6px;
            cursor: pointer;
            font-size: 14px;
            font-weight: 500;
        }

        .btn:hover {
            background: #5568d3;
        }

        .account-list {
            display: grid;
            gap: 15px;
        }

        .account-item {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 15px;
            border: 1px solid #e0e0e0;
            border-radius: 8px;
        }

        .account-info h4 {
            font-size: 16px;
            margin-bottom: 5px;
        }

        .account-info p {
            font-size: 14px;
            color: #666;
        }

        .account-balance {
            font-size: 20px;
            font-weight: 700;
            color: #667eea;
        }

        .transactions-list {
            display: grid;
            gap: 10px;
        }

        .transaction-item {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 12px;
            border-bottom: 1px solid #f0f0f0;
        }

        .transaction-item:last-child {
            border-bottom: none;
        }

        .transaction-info h4 {
            font-size: 15px;
            margin-bottom: 3px;
        }

        .transaction-info p {
            font-size: 13px;
            color: #999;
        }

        .transaction-amount {
            font-size: 16px;
            font-weight: 600;
        }

        .transaction-amount.positive {
            color: #48bb78;
        }

        .transaction-amount.negative {
            color: #f56565;
        }

        .empty-state {
            text-align: center;
            padding: 40px 20px;
            color: #999;
        }

        .empty-state-icon {
            font-size: 48px;
            margin-bottom: 15px;
        }

        .loading {
            text-align: center;
            padding: 20px;
            color: #999;
        }

        @media (max-width: 768px) {
            .cards-grid {
                grid-template-columns: 1fr;
            }

            .account-item {
                flex-direction: column;
                text-align: center;
                gap: 10px;
            }
        }
    </style>
</head>
<body>
    <nav class="navbar">
        <div class="navbar-content">
            <div class="logo">💰 FinanceTracker</div>
            <div class="user-menu">
                <span class="user-email" id="user-email">Loading...</span>
                <button class="btn-logout" onclick="logout()">Logout</button>
            </div>
        </div>
    </nav>

    <div class="container">
        <div class="dashboard-header">
            <h1>Dashboard</h1>
            <p>Manage your accounts and transactions</p>
        </div>

        <div class="cards-grid">
            <div class="card">
                <h3>Total Accounts</h3>
                <div class="card-value" id="total-accounts">0</div>
                <div class="card-label">Connected accounts</div>
            </div>
            <div class="card">
                <h3>Total Balance</h3>
                <div class="card-value" id="total-balance">$0.00</div>
                <div class="card-label">Across all accounts</div>
            </div>
            <div class="card">
                <h3>Transactions</h3>
                <div class="card-value" id="total-transactions">0</div>
                <div class="card-label">Last 30 days</div>
            </div>
        </div>

        <div class="section">
            <div class="section-header">
                <h2>Your Accounts</h2>
                <button class="btn" onclick="connectBank(event)">Connect Bank</button>
                <button class="btn" onclick="syncAccounts(event)">Sync Accounts</button>
            </div>
            <div id="accounts-container" class="loading">Loading accounts...</div>
        </div>

        <div class="section">
            <div class="section-header">
                <h2>Recent Transactions</h2>
                <button class="btn" onclick="syncTransactions(event)">Sync Transactions</button>
            </div>
            <div id="transactions-container" class="loading">Loading transactions...</div>
        </div>

        <div class="section">
            <div class="section-header">
                <h2>Crypto Wallets</h2>
                <button class="btn" onclick="showAddWalletModal()">Add Wallet</button>
            </div>
            <div id="crypto-wallets-container" class="loading">Loading wallets...</div>
        </div>

        <!-- Exchange Connections - Temporarily disabled until Coinbase OAuth is available
        <div class="section">
            <div class="section-header">
                <h2>Exchange Connections</h2>
                <a href="/api/exchange/coinbase/connect" class="btn">Connect Coinbase</a>
            </div>
            <div id="exchange-connections-container" class="loading">Loading...</div>
        </div>
        -->

        <div class="section">
            <div class="section-header">
                <h2>Crypto Transactions</h2>
            </div>
            <div id="crypto-transactions-container">Loading...</div>
        </div>

        <div class="section">
            <div class="section-header">
                <h2>Account Settings</h2>
            </div>
            <div class="card" style="border: 1px solid #fed7d7;">
                <h3 style="color: #c53030; margin-bottom: 10px;">Danger Zone</h3>
                <p style="color: #666; margin-bottom: 15px; font-size: 14px;">
                    Once you delete your account, there is no going back. This will permanently delete:
                </p>
                <ul style="color: #666; margin-bottom: 20px; font-size: 14px; margin-left: 20px;">
                    <li>Your user account</li>
                    <li>All connected bank accounts</li>
                    <li>All transaction history</li>
                    <li>All Plaid connections</li>
                </ul>
                <button class="btn-logout" onclick="deleteAccount()" style="background: #c53030;">
                    Delete My Account
                </button>
            </div>
        </div>
    </div>

    <script>
        // CSRF Token Helper
        function getCsrfToken() {
            const cookies = document.cookie.split(';');
            for (let cookie of cookies) {
                const [name, value] = cookie.trim().split('=');
                if (name === 'XSRF-TOKEN') {
                    return decodeURIComponent(value);
                }
            }
            return null;
        }

        // Initialize CSRF token on page load
        async function initCsrf() {
            try {
                await fetch('/api/csrf', { credentials: 'include' });
            } catch (error) {
                console.error('Failed to initialize CSRF token:', error);
            }
        }

        async function connectBank(event) {
            const button = event.target;
            button.disabled = true;
            button.textContent = 'Opening...';

            try {
                const csrfToken = getCsrfToken();
                const response = await fetch('/api/plaid/link-token', {
                    method: 'POST',
                    credentials: 'include',
                    headers: csrfToken ? { 'X-XSRF-TOKEN': csrfToken } : {}
                });

                if (!response.ok) {
                    const text = await response.text();
                    console.error('link-token failed:', text);
                    alert('Failed to start Plaid Link. Check server logs.');
                    return;
                }

                const data = await response.json();
                const linkToken = data.link_token;

                if (!linkToken) {
                    console.error('Missing link_token in response:', data);
                    alert('Backend did not return link_token.');
                    return;
                }

                const handler = Plaid.create({
                    token: linkToken,
                    onSuccess: async (public_token, metadata) => {
                        try {
                            // Refresh CSRF token after Plaid popup (cookies may be lost)
                            console.log('🔄 Refreshing CSRF token after Plaid completion...');
                            await fetch('/api/csrf', { credentials: 'include' });

                            // Small delay to ensure cookie is set
                            await new Promise(resolve => setTimeout(resolve, 100));

                            const csrfToken = getCsrfToken();
                            console.log('🔐 CSRF Token:', csrfToken);
                            console.log('🍪 Cookies:', document.cookie);

                            const headers = { 'Content-Type': 'application/json' };
                            if (csrfToken) {
                                headers['X-XSRF-TOKEN'] = csrfToken;
                                console.log('✅ CSRF token added to headers');
                            } else {
                                console.error('❌ No CSRF token found!');
                            }

                            console.log('📤 Sending request to /api/plaid/exchange with headers:', headers);

                            const ex = await fetch('/api/plaid/exchange', {
                                method: 'POST',
                                credentials: 'include',
                                headers: headers,
                                body: JSON.stringify({ public_token: public_token })
                            });

                            console.log('📥 Response status:', ex.status);

                            if (!ex.ok) {
                                const exText = await ex.text();
                                console.error('exchange failed:', exText);
                                alert('Bank connected, but token exchange failed. Check server logs.');
                                return;
                            }

                            alert('Bank connected! Now click Sync Accounts.');
                            // Optional: automatically sync after connecting
                            // await syncAccounts({ target: document.querySelector('button[onclick^="syncAccounts"]') });
                        } catch (err) {
                            console.error('exchange error:', err);
                            alert('Token exchange failed.');
                        }
                    },
                    onExit: (err, metadata) => {
                        if (err) console.error('Plaid onExit error:', err);
                    }
                });

                handler.open();

            } catch (error) {
                console.error('Connect bank error:', error);
                alert('Failed to open Plaid Link.');
            } finally {
                button.disabled = false;
                button.textContent = 'Connect Bank';
            }
        }

        // Fetch user info
        async function loadUserInfo() {
            try {
                const response = await fetch('/api/users/me', {
                    credentials: 'include'  // Include HttpOnly cookie
                });

                if (!response.ok) {
                    // Not authenticated, redirect to login
                    window.location.href = '/auth';
                    return;
                }

                const user = await response.json();
                document.getElementById('user-email').textContent = user.email;
            } catch (error) {
                console.error('Failed to load user info:', error);
                window.location.href = '/auth';
            }
        }

        // Load accounts
        async function loadAccounts() {
            try {
                // Directly fetch accounts for the current user
                const accountsResponse = await fetch('/api/accounts', {
                    credentials: 'include'
                });

                if (!accountsResponse.ok) {
                    throw new Error('Failed to fetch accounts');
                }

                const accounts = await accountsResponse.json();

                const container = document.getElementById('accounts-container');

                if (accounts.length === 0) {
                    container.innerHTML = `
                        <div class="empty-state">
                            <div class="empty-state-icon">🏦</div>
                            <p>No accounts connected yet. Connect your bank account to get started!</p>
                        </div>
                    `;
                } else {
                    let totalBalance = 0;
                    const accountsHTML = accounts.map(account => {
                        const balance = account.balance || 0;
                        totalBalance += parseFloat(balance);
                        return `
                            <div class="account-item">
                                <div class="account-info">
                                    <h4>\${account.accountName || 'Account'}</h4>
                                    <p>\${account.accountType || 'Unknown'} • \${account.institutionString || 'Bank'}</p>
                                </div>
                                <div class="account-balance">$\${parseFloat(balance).toFixed(2)}</div>
                            </div>
                        `;
                    }).join('');

                    container.innerHTML = `<div class="account-list">\${accountsHTML}</div>`;

                    document.getElementById('total-accounts').textContent = accounts.length;
                    document.getElementById('total-balance').textContent = `$\${totalBalance.toFixed(2)}`;
                }
            } catch (error) {
                console.error('Failed to load accounts:', error);
                document.getElementById('accounts-container').innerHTML = '<div class="empty-state">Failed to load accounts</div>';
            }
        }

        // Load transactions
        async function loadTransactions() {
            const container = document.getElementById('transactions-container');

            try {
                const response = await fetch('/api/transactions', {
                    credentials: 'include'
                });

                if (!response.ok) {
                    throw new Error('Failed to load transactions');
                }

                const transactions = await response.json();

                // Update transaction count
                document.getElementById('total-transactions').textContent = transactions.length;

                if (!transactions || transactions.length === 0) {
                    container.innerHTML = `
                        <div class="empty-state">
                            <div class="empty-state-icon">📊</div>
                            <p>No transactions yet. Sync your accounts to see transactions!</p>
                        </div>
                    `;
                    return;
                }

                // Display transactions in a table
                container.innerHTML = `
                    <table class="transactions-table">
                        <thead>
                            <tr>
                                <th>Date</th>
                                <th>Description</th>
                                <th>Category</th>
                                <th>Account</th>
                                <th>Amount</th>
                            </tr>
                        </thead>
                        <tbody>
                            \${transactions.map(t => `
                                <tr>
                                    <td>\${new Date(t.date).toLocaleDateString()}</td>
                                    <td>\${t.description || 'N/A'}</td>
                                    <td>\${t.category || 'Uncategorized'}</td>
                                    <td>\${t.accountName || 'N/A'}</td>
                                    <td class="\${parseFloat(t.amount) < 0 ? 'negative' : 'positive'}">
                                        $\${Math.abs(parseFloat(t.amount)).toFixed(2)}
                                    </td>
                                </tr>
                            `).join('')}
                        </tbody>
                    </table>
                `;
            } catch (error) {
                console.error('Error loading transactions:', error);
                container.innerHTML = `
                    <div class="empty-state">
                        <div class="empty-state-icon">⚠️</div>
                        <p>Failed to load transactions. Please try again.</p>
                    </div>
                `;
            }
        }

        async function syncAccounts(event) {
            const button = event.target;
            button.disabled = true;
            button.textContent = 'Syncing...';

            try {
                // Refresh CSRF token before sync
                await fetch('/api/csrf', { credentials: 'include' });
                await new Promise(resolve => setTimeout(resolve, 100));

                const csrfToken = getCsrfToken();
                const response = await fetch('/api/plaid/accounts/sync', {
                    method: 'POST',
                    credentials: 'include',
                    headers: csrfToken ? { 'X-XSRF-TOKEN': csrfToken } : {}
                });

                if (response.ok) {
                    alert('Accounts synced successfully!');
                    loadAccounts();
                } else {
                    alert('Failed to sync accounts');
                }
            } catch (error) {
                console.error('Sync error:', error);
                alert('Failed to sync accounts');
            } finally {
                button.disabled = false;
                button.textContent = 'Sync Accounts';
            }
        }

        async function syncTransactions(event) {
            const button = event.target;
            button.disabled = true;
            button.textContent = 'Syncing...';

            try {
                // Refresh CSRF token before sync
                await fetch('/api/csrf', { credentials: 'include' });
                await new Promise(resolve => setTimeout(resolve, 100));

                const csrfToken = getCsrfToken();
                const headers = { 'Content-Type': 'application/json' };
                if (csrfToken) headers['X-XSRF-TOKEN'] = csrfToken;

                const response = await fetch('/api/plaid/transactions/sync', {
                    method: 'POST',
                    credentials: 'include',
                    headers: headers
                });

                if (response.ok) {
                    alert('Transactions synced successfully!');
                    loadTransactions();
                } else {
                    alert('Failed to sync transactions');
                }
            } catch (error) {
                console.error('Sync error:', error);
                alert('Failed to sync transactions');
            } finally {
                button.disabled = false;
                button.textContent = 'Sync Transactions';
            }
        }

        async function deleteAccount() {
            // Show confirmation dialog
            const confirmed = confirm(
                'Are you absolutely sure you want to delete your account?\n\n' +
                'This will permanently delete:\n' +
                '• Your user account\n' +
                '• All connected bank accounts\n' +
                '• All transaction history\n' +
                '• All Plaid connections\n\n' +
                'This action CANNOT be undone!'
            );

            if (!confirmed) {
                return;
            }

            // Double confirmation
            const doubleConfirm = confirm(
                'FINAL WARNING: This is your last chance!\n\n' +
                'Type OK in your mind and click OK to proceed with account deletion.'
            );

            if (!doubleConfirm) {
                return;
            }

            try {
                const csrfToken = getCsrfToken();
                const response = await fetch('/api/users/me', {
                    method: 'DELETE',
                    credentials: 'include',
                    headers: csrfToken ? { 'X-XSRF-TOKEN': csrfToken } : {}
                });

                if (response.ok) {
                    alert('Your account has been successfully deleted.');
                    window.location.href = '/auth';
                } else {
                    const error = await response.json();
                    alert('Failed to delete account: ' + (error.error || 'Unknown error'));
                }
            } catch (error) {
                console.error('Delete account error:', error);
                alert('Failed to delete account. Please try again.');
            }
        }

        async function logout() {
            try {
                // Call logout endpoint to clear cookie
                await fetch('/api/auth/logout', {
                    method: 'POST',
                    credentials: 'include'
                });
            } catch (error) {
                console.error('Logout error:', error);
            }
            // Redirect to auth page
            window.location.href = '/auth';
        }

        // ===========================================
        // CRYPTO WALLET FUNCTIONS
        // ===========================================

        // Show/Hide Add Wallet Modal
        function showAddWalletModal() {
            document.getElementById('addWalletModal').style.display = 'flex';
        }

        function hideAddWalletModal() {
            document.getElementById('addWalletModal').style.display = 'none';
            document.getElementById('addWalletForm').reset();
        }

        // Add Wallet
        async function addWallet(event) {
            event.preventDefault();

            const formData = {
                walletAddress: document.getElementById('walletAddress').value,
                blockchain: document.getElementById('blockchain').value,
                walletName: document.getElementById('walletName').value || null
            };

            try {
                // Refresh CSRF token
                await fetch('/api/csrf', { credentials: 'include' });
                await new Promise(resolve => setTimeout(resolve, 100));

                const csrfToken = getCsrfToken();
                const response = await fetch('/api/crypto/wallets', {
                    method: 'POST',
                    credentials: 'include',
                    headers: {
                        'Content-Type': 'application/json',
                        'X-XSRF-TOKEN': csrfToken
                    },
                    body: JSON.stringify(formData)
                });

                if (response.ok) {
                    alert('Wallet added successfully!');
                    hideAddWalletModal();
                    loadCryptoWallets();
                } else {
                    const error = await response.json();
                    alert('Failed to add wallet: ' + (error.error || 'Unknown error'));
                }
            } catch (error) {
                console.error('Error adding wallet:', error);
                alert('Failed to add wallet');
            }
        }

        // Load Crypto Wallets
        async function loadCryptoWallets() {
            const container = document.getElementById('crypto-wallets-container');

            try {
                const response = await fetch('/api/crypto/wallets', {
                    credentials: 'include'
                });

                if (!response.ok) {
                    throw new Error('Failed to load wallets');
                }

                const wallets = await response.json();

                if (!wallets || wallets.length === 0) {
                    container.innerHTML = `
                        <div class="empty-state">
                            <div class="empty-state-icon">💰</div>
                            <p>No crypto wallets yet. Add your first wallet to track your crypto!</p>
                        </div>
                    `;
                    return;
                }

                // Display wallets
                container.innerHTML = wallets.map(wallet => `
                    <div class="card" style="margin-bottom: 15px;">
                        <div style="display: flex; justify-content: space-between; align-items: start;">
                            <div>
                                <h4 style="margin: 0 0 5px 0;">\${wallet.walletName || 'Crypto Wallet'}</h4>
                                <p style="color: #666; font-size: 12px; margin: 0 0 10px 0; word-break: break-all;">\${wallet.walletAddress}</p>
                                <p style="margin: 0; font-size: 14px;">
                                    <span style="background: #e6f3ff; padding: 3px 8px; border-radius: 4px; font-size: 11px; text-transform: uppercase;">\${wallet.blockchain}</span>
                                </p>
                            </div>
                            <div style="text-align: right;">
                                <p style="font-size: 20px; font-weight: bold; margin: 0;">\${parseFloat(wallet.balance).toFixed(6)} \${wallet.token}</p>
                                <p style="color: #666; font-size: 14px; margin: 5px 0;">$\${parseFloat(wallet.balanceUsd).toFixed(2)}</p>
                                <button onclick="syncCryptoTransactions(\${wallet.id})" class="btn" style="font-size: 12px; padding: 5px 10px; margin-top: 10px;">Sync</button>
                                <button onclick="deleteCryptoWallet(\${wallet.id})" style="font-size: 12px; padding: 5px 10px; background: #fed7d7; color: #c53030; border: none; border-radius: 5px; cursor: pointer; margin-top: 5px;">Delete</button>
                            </div>
                        </div>
                    </div>
                `).join('');

            } catch (error) {
                console.error('Error loading wallets:', error);
                container.innerHTML = `
                    <div class="empty-state">
                        <div class="empty-state-icon">⚠️</div>
                        <p>Failed to load wallets. Please try again.</p>
                    </div>
                `;
            }
        }

        // Sync Crypto Transactions
        async function syncCryptoTransactions(walletId) {
            try {
                // Refresh CSRF token
                await fetch('/api/csrf', { credentials: 'include' });
                await new Promise(resolve => setTimeout(resolve, 100));

                const csrfToken = getCsrfToken();
                const response = await fetch(`/api/crypto/wallets/\${walletId}/sync`, {
                    method: 'POST',
                    credentials: 'include',
                    headers: {
                        'X-XSRF-TOKEN': csrfToken
                    }
                });

                if (response.ok) {
                    const result = await response.json();
                    alert(result.message || 'Transactions synced successfully!');
                    loadCryptoTransactions();
                } else {
                    alert('Failed to sync transactions');
                }
            } catch (error) {
                console.error('Error syncing transactions:', error);
                alert('Failed to sync transactions');
            }
        }

        // Delete Crypto Wallet
        async function deleteCryptoWallet(walletId) {
            if (!confirm('Are you sure you want to delete this wallet? This will also delete all associated transactions.')) {
                return;
            }

            try {
                // Refresh CSRF token
                await fetch('/api/csrf', { credentials: 'include' });
                await new Promise(resolve => setTimeout(resolve, 100));

                const csrfToken = getCsrfToken();
                const response = await fetch(`/api/crypto/wallets/\${walletId}`, {
                    method: 'DELETE',
                    credentials: 'include',
                    headers: {
                        'X-XSRF-TOKEN': csrfToken
                    }
                });

                if (response.ok) {
                    alert('Wallet deleted successfully!');
                    loadCryptoWallets();
                    loadCryptoTransactions();
                } else {
                    alert('Failed to delete wallet');
                }
            } catch (error) {
                console.error('Error deleting wallet:', error);
                alert('Failed to delete wallet');
            }
        }

        // Load Crypto Transactions
        async function loadCryptoTransactions() {
            const container = document.getElementById('crypto-transactions-container');

            try {
                const response = await fetch('/api/crypto/transactions', {
                    credentials: 'include'
                });

                if (!response.ok) {
                    throw new Error('Failed to load transactions');
                }

                const transactions = await response.json();

                if (!transactions || transactions.length === 0) {
                    container.innerHTML = `
                        <div class="empty-state">
                            <div class="empty-state-icon">📊</div>
                            <p>No crypto transactions yet. Sync your wallets to see transactions!</p>
                        </div>
                    `;
                    return;
                }

                // Display transactions in table
                container.innerHTML = `
                    <table class="transactions-table">
                        <thead>
                            <tr>
                                <th>Date</th>
                                <th>Type</th>
                                <th>From/To</th>
                                <th>Amount</th>
                                <th>Token</th>
                                <th>TX Hash</th>
                            </tr>
                        </thead>
                        <tbody>
                            \${transactions.slice(0, 50).map(tx => `
                                <tr>
                                    <td>\${new Date(tx.date).toLocaleDateString()}</td>
                                    <td>\${tx.type}</td>
                                    <td style="font-size: 11px; max-width: 150px; overflow: hidden; text-overflow: ellipsis;">\${tx.type === 'send' ? tx.toAddress : tx.fromAddress}</td>
                                    <td>\${parseFloat(tx.amount).toFixed(6)}</td>
                                    <td>\${tx.token}</td>
                                    <td><a href="https://etherscan.io/tx/\${tx.txHash}" target="_blank" style="font-size: 11px;">\${tx.txHash.substring(0, 10)}...</a></td>
                                </tr>
                            `).join('')}
                        </tbody>
                    </table>
                `;

            } catch (error) {
                console.error('Error loading crypto transactions:', error);
                container.innerHTML = `
                    <div class="empty-state">
                        <div class="empty-state-icon">⚠️</div>
                        <p>Failed to load transactions.</p>
                    </div>
                `;
            }
        }

        // ===================================
        // Exchange Connections
        // ===================================

        // Load Exchange Connections
        async function loadExchangeConnections() {
            const container = document.getElementById('exchange-connections-container');
            container.classList.add('loading');

            try {
                const response = await fetch('/api/exchange/connections', {
                    credentials: 'include'
                });

                if (!response.ok) {
                    throw new Error('Failed to fetch exchange connections');
                }

                const connections = await response.json();
                container.classList.remove('loading');

                if (connections.length === 0) {
                    container.innerHTML = `
                        <div class="empty-state">
                            <div class="empty-state-icon">🔗</div>
                            <p>No exchanges connected yet.</p>
                            <p style="color: #666; font-size: 14px;">Click "Connect Coinbase" to link your exchange account.</p>
                        </div>
                    `;
                    return;
                }

                // Display connections and load accounts for each
                container.innerHTML = connections.map(conn => `
                    <div class="card" id="exchange-\${conn.id}">
                        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px;">
                            <div>
                                <h3>\${conn.connectionName || conn.exchange}</h3>
                                <p style="color: #666; font-size: 14px;">Connected: \${new Date(conn.createdAt).toLocaleDateString()}</p>
                            </div>
                            <button class="btn-danger" onclick="disconnectExchange(\${conn.id})">Disconnect</button>
                        </div>
                        <div id="exchange-\${conn.id}-accounts" class="loading">Loading accounts...</div>
                    </div>
                `).join('');

                // Load Coinbase accounts if connected
                const coinbase = connections.find(c => c.exchange === 'coinbase');
                if (coinbase) {
                    loadCoinbaseAccounts(coinbase.id);
                }

            } catch (error) {
                container.classList.remove('loading');
                console.error('Error loading exchange connections:', error);
                container.innerHTML = `
                    <div class="empty-state">
                        <div class="empty-state-icon">⚠️</div>
                        <p>Failed to load exchange connections.</p>
                    </div>
                `;
            }
        }

        // Load Coinbase Accounts
        async function loadCoinbaseAccounts(exchangeId) {
            const container = document.getElementById('exchange-' + exchangeId + '-accounts');

            try {
                const response = await fetch('/api/exchange/coinbase/accounts', {
                    credentials: 'include'
                });

                if (!response.ok) {
                    throw new Error('Failed to fetch Coinbase accounts');
                }

                const accounts = await response.json();
                container.classList.remove('loading');

                if (accounts.length === 0) {
                    container.innerHTML = '<p style="color: #666;">No accounts found.</p>';
                    return;
                }

                // Filter accounts with non-zero balances
                const activeAccounts = accounts.filter(acc => parseFloat(acc.balance) > 0);

                const accountsHtml = activeAccounts.map(acc =>
                    '<div style="background: #f7fafc; padding: 15px; border-radius: 8px;">' +
                        '<div style="font-weight: 600; margin-bottom: 5px;">' + acc.currency + '</div>' +
                        '<div style="font-size: 20px; color: #667eea; margin-bottom: 5px;">' +
                            parseFloat(acc.balance).toFixed(8) +
                        '</div>' +
                        '<div style="color: #666; font-size: 14px;">$' +
                            parseFloat(acc.balanceUsd).toFixed(2) +
                        '</div>' +
                    '</div>'
                ).join('');

                container.innerHTML =
                    '<div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 15px;">' +
                        accountsHtml +
                    '</div>';

            } catch (error) {
                container.classList.remove('loading');
                console.error('Error loading Coinbase accounts:', error);
                container.innerHTML = '<p style="color: #e53e3e;">Failed to load accounts.</p>';
            }
        }

        // Disconnect Exchange
        async function disconnectExchange(exchangeId) {
            if (!confirm('Are you sure you want to disconnect this exchange?')) {
                return;
            }

            try {
                await refreshCsrfToken();

                const response = await fetch('/api/exchange/' + exchangeId, {
                    method: 'DELETE',
                    headers: {
                        'X-XSRF-TOKEN': getCsrfToken()
                    },
                    credentials: 'include'
                });

                if (!response.ok) {
                    throw new Error('Failed to disconnect exchange');
                }

                alert('Exchange disconnected successfully!');
                loadExchangeConnections();

            } catch (error) {
                console.error('Error disconnecting exchange:', error);
                alert('Failed to disconnect exchange.');
            }
        }

        // Load data on page load
        async function initializeDashboard() {
            await initCsrf();  // Initialize CSRF token first
            loadUserInfo();
            loadAccounts();
            loadTransactions();
            loadCryptoWallets();
            loadExchangeConnections();
            loadCryptoTransactions();
        }
        initializeDashboard();
    </script>

    <footer style="text-align: center; padding: 40px 20px; color: #666; font-size: 14px;">
        <a href="/privacy" style="color: #667eea; text-decoration: none; margin: 0 10px;">Privacy Policy</a>
        <span>•</span>
        <a href="/terms" style="color: #667eea; text-decoration: none; margin: 0 10px;">Terms of Service</a>
    </footer>

    <!-- Add Wallet Modal -->
    <div id="addWalletModal" class="modal" style="display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 1000; align-items: center; justify-content: center;">
        <div class="modal-content" style="background: white; padding: 30px; border-radius: 10px; max-width: 500px; width: 90%;">
            <h2>Add Crypto Wallet</h2>
            <form id="addWalletForm" onsubmit="addWallet(event)">
                <div style="margin-bottom: 15px;">
                    <label style="display: block; margin-bottom: 5px; font-weight: 500;">Blockchain:</label>
                    <select id="blockchain" name="blockchain" required style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 5px;">
                        <option value="ethereum">Ethereum</option>
                        <option value="bitcoin">Bitcoin</option>
                        <option value="polygon">Polygon</option>
                        <option value="solana">Solana</option>
                    </select>
                </div>
                <div style="margin-bottom: 15px;">
                    <label style="display: block; margin-bottom: 5px; font-weight: 500;">Wallet Address:</label>
                    <input type="text" id="walletAddress" name="walletAddress" placeholder="0x... or bc1..." required style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 5px;">
                    <small style="color: #666;">Enter your public wallet address (read-only, no private keys needed)</small>
                </div>
                <div style="margin-bottom: 20px;">
                    <label style="display: block; margin-bottom: 5px; font-weight: 500;">Wallet Name (Optional):</label>
                    <input type="text" id="walletName" name="walletName" placeholder="My Main Wallet" style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 5px;">
                </div>
                <div style="display: flex; gap: 10px; justify-content: flex-end;">
                    <button type="button" onclick="hideAddWalletModal()" style="padding: 10px 20px; background: #e2e8f0; border: none; border-radius: 5px; cursor: pointer;">Cancel</button>
                    <button type="submit" style="padding: 10px 20px; background: #667eea; color: white; border: none; border-radius: 5px; cursor: pointer;">Add Wallet</button>
                </div>
            </form>
        </div>
    </div>
</body>
</html>
