-- Add cryptocurrency wallet and transaction tracking
-- Supports read-only wallet tracking via blockchain APIs (Etherscan, etc.)

-- Crypto Wallets table (stores public wallet addresses - no private keys!)
CREATE TABLE IF NOT EXISTS crypto_wallets (
    id INT AUTO_INCREMENT PRIMARY KEY,
    wallet_address VARCHAR(255) NOT NULL COMMENT 'Public wallet address (safe to store)',
    blockchain VARCHAR(50) NOT NULL COMMENT 'ethereum, bitcoin, solana, polygon, etc.',
    wallet_name VARCHAR(255) COMMENT 'User-friendly name for the wallet',
    user_id INT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_blockchain (blockchain),
    INDEX idx_wallet_address (wallet_address),
    UNIQUE KEY unique_wallet_per_user (wallet_address, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Crypto Transactions table (synced from blockchain APIs)
CREATE TABLE IF NOT EXISTS crypto_transactions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    tx_hash VARCHAR(255) UNIQUE COMMENT 'Blockchain transaction hash (unique identifier)',
    wallet_id INT,
    date DATETIME NOT NULL COMMENT 'Transaction timestamp',
    from_address VARCHAR(255) COMMENT 'Sender address',
    to_address VARCHAR(255) COMMENT 'Receiver address',
    amount DECIMAL(30, 18) COMMENT 'Transaction amount (supports many decimals)',
    token VARCHAR(50) COMMENT 'ETH, BTC, USDC, etc.',
    type VARCHAR(50) COMMENT 'send, receive, swap, contract_interaction',
    gas_fee DECIMAL(30, 18) COMMENT 'Transaction fee paid',
    block_number BIGINT COMMENT 'Block number on blockchain',
    confirmations INT COMMENT 'Number of confirmations',
    created_at DATETIME NOT NULL COMMENT 'When synced to our database',
    FOREIGN KEY (wallet_id) REFERENCES crypto_wallets(id) ON DELETE CASCADE,
    INDEX idx_wallet_id (wallet_id),
    INDEX idx_date (date),
    INDEX idx_tx_hash (tx_hash),
    INDEX idx_token (token)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
