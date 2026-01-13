<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard - FinanceTracker</title>
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
                <button class="btn" onclick="syncAccounts()">Sync Accounts</button>
            </div>
            <div id="accounts-container" class="loading">Loading accounts...</div>
        </div>

        <div class="section">
            <div class="section-header">
                <h2>Recent Transactions</h2>
                <button class="btn" onclick="syncTransactions()">Sync Transactions</button>
            </div>
            <div id="transactions-container" class="loading">Loading transactions...</div>
        </div>
    </div>

    <script>
        // Check authentication
        const token = localStorage.getItem('jwt');
        if (!token) {
            window.location.href = '/auth';
        }

        // Fetch user info
        async function loadUserInfo() {
            try {
                const response = await fetch('/api/users/me', {
                    headers: {
                        'Authorization': 'Bearer ' + token
                    }
                });

                if (!response.ok) {
                    throw new Error('Unauthorized');
                }

                const user = await response.json();
                document.getElementById('user-email').textContent = user.email;
            } catch (error) {
                console.error('Failed to load user info:', error);
                logout();
            }
        }

        // Load accounts
        async function loadAccounts() {
            try {
                const response = await fetch('/api/users/me', {
                    headers: {
                        'Authorization': 'Bearer ' + token
                    }
                });

                const user = await response.json();
                const userId = user.id;

                const accountsResponse = await fetch(`/api/users/\${userId}`, {
                    headers: {
                        'Authorization': 'Bearer ' + token
                    }
                });

                if (!accountsResponse.ok) {
                    throw new Error('Failed to fetch accounts');
                }

                const userData = await accountsResponse.json();
                const accounts = userData.accounts || [];

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

                    container.innerHTML = `<div class="account-list">${accountsHTML}</div>`;

                    document.getElementById('total-accounts').textContent = accounts.length;
                    document.getElementById('total-balance').textContent = `$${totalBalance.toFixed(2)}`;
                }
            } catch (error) {
                console.error('Failed to load accounts:', error);
                document.getElementById('accounts-container').innerHTML = '<div class="empty-state">Failed to load accounts</div>';
            }
        }

        // Load transactions
        async function loadTransactions() {
            // Note: You'll need to implement an endpoint to get transactions for a user
            // For now, showing empty state
            const container = document.getElementById('transactions-container');
            container.innerHTML = `
                <div class="empty-state">
                    <div class="empty-state-icon">📊</div>
                    <p>No transactions yet. Sync your accounts to see transactions!</p>
                </div>
            `;
        }

        async function syncAccounts() {
            const button = event.target;
            button.disabled = true;
            button.textContent = 'Syncing...';

            try {
                const response = await fetch('/api/plaid/accounts/sync', {
                    method: 'POST',
                    headers: {
                        'Authorization': 'Bearer ' + token
                    }
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

        async function syncTransactions() {
            const button = event.target;
            button.disabled = true;
            button.textContent = 'Syncing...';

            try {
                const response = await fetch('/api/plaid/transactions/sync', {
                    method: 'POST',
                    headers: {
                        'Authorization': 'Bearer ' + token,
                        'Content-Type': 'application/json'
                    }
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

        function logout() {
            localStorage.removeItem('jwt');
            window.location.href = '/auth';
        }

        // Load data on page load
        loadUserInfo();
        loadAccounts();
        loadTransactions();
    </script>
</body>
</html>
