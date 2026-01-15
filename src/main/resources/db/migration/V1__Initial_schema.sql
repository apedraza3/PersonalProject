-- Initial database schema for FinanceTracker application
-- This migration creates all tables for users, accounts, transactions, and Plaid integration

-- Users table
CREATE TABLE IF NOT EXISTS Users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Plaid Items table (stores encrypted Plaid access tokens)
CREATE TABLE IF NOT EXISTS plaid_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    item_id VARCHAR(255),
    access_token VARCHAR(1000),  -- Encrypted, needs more space than plaintext
    institution_name VARCHAR(255),
    user_id INT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Accounts table
CREATE TABLE IF NOT EXISTS Accounts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    account_name VARCHAR(255),
    institution_string VARCHAR(255),
    account_type VARCHAR(100),
    balance DECIMAL(19, 4),
    plaid_account_id VARCHAR(255) UNIQUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    User_id INT NOT NULL,
    FOREIGN KEY (User_id) REFERENCES Users(id) ON DELETE CASCADE,
    INDEX idx_user_id (User_id),
    INDEX idx_plaid_account_id (plaid_account_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Transactions table
CREATE TABLE IF NOT EXISTS Transactions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    date DATE NOT NULL,
    description VARCHAR(500),
    amount DECIMAL(19, 4) NOT NULL,
    category VARCHAR(255),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    account_id INT NOT NULL,
    FOREIGN KEY (account_id) REFERENCES Accounts(id) ON DELETE CASCADE,
    INDEX idx_account_id (account_id),
    INDEX idx_date (date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
