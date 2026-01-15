-- Hash refresh tokens for better security
-- Change token column to store hashed values (SHA-256 = 64 hex chars)
-- The unique constraint remains to prevent duplicate hashes

ALTER TABLE refresh_tokens
    MODIFY COLUMN token VARCHAR(500) NOT NULL UNIQUE COMMENT 'Hashed refresh token (SHA-256)';

-- Note: Existing tokens will need to be revoked and re-issued as hashed
-- Consider running: DELETE FROM refresh_tokens; after this migration
-- Users will need to log in again to get new hashed refresh tokens
