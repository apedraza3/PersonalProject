-- Add cursor column to plaid_items for Plaid's cursor-based sync
-- This enables incremental transaction syncing and handles modifications/removals

ALTER TABLE plaid_items
    ADD COLUMN transaction_cursor TEXT COMMENT 'Plaid transactions/sync cursor for incremental updates';

-- Note: NULL cursor means "never synced" - first sync will fetch all available transactions
-- After first sync, the cursor will be populated and subsequent syncs will be incremental
