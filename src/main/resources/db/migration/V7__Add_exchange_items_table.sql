-- Add exchange OAuth integration for Coinbase, Binance, etc.
-- Stores encrypted OAuth tokens for accessing exchange APIs

CREATE TABLE IF NOT EXISTS exchange_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    exchange VARCHAR(50) NOT NULL COMMENT 'coinbase, binance, kraken, etc.',
    access_token VARCHAR(1000) COMMENT 'Encrypted OAuth access token',
    refresh_token VARCHAR(1000) COMMENT 'Encrypted OAuth refresh token',
    token_expires_at DATETIME COMMENT 'When the access token expires',
    connection_name VARCHAR(255) COMMENT 'User-friendly name for this connection',
    user_id INT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_exchange (exchange),
    UNIQUE KEY unique_exchange_per_user (user_id, exchange) COMMENT 'One connection per exchange per user'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
