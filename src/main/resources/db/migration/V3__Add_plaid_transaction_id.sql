-- Add plaid_transaction_id column to prevent duplicate transactions
-- This enables idempotent transaction syncing from Plaid

ALTER TABLE Transactions
    ADD COLUMN plaid_transaction_id VARCHAR(255) UNIQUE;

-- Add index for faster lookups during sync
CREATE INDEX idx_plaid_transaction_id ON Transactions(plaid_transaction_id);

-- Note: Existing transactions will have NULL plaid_transaction_id
-- They can be re-synced to populate this field, or left as-is
