# Flyway Migration Setup

## First Time Setup (Existing Database)

If you're adding Flyway to an existing database, you have two options:

### Option 1: Clean Start (Recommended for Development)
If you don't need the existing data:

```sql
-- Connect to MySQL
mysql -u root -p

-- Drop and recreate the database
DROP DATABASE IF EXISTS portfolio;
CREATE DATABASE portfolio;
```

Then start the application - Flyway will create all tables from scratch.

### Option 2: Baseline Existing Database
If you want to keep existing data:

```sql
-- Connect to your database
mysql -u root -p portfolio

-- Manually create Flyway's schema history table
CREATE TABLE IF NOT EXISTS flyway_schema_history (
    installed_rank INT NOT NULL,
    version VARCHAR(50),
    description VARCHAR(200) NOT NULL,
    type VARCHAR(20) NOT NULL,
    script VARCHAR(1000) NOT NULL,
    checksum INT,
    installed_by VARCHAR(100) NOT NULL,
    installed_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    execution_time INT NOT NULL,
    success BOOLEAN NOT NULL,
    PRIMARY KEY (installed_rank)
);

-- Mark current schema as baseline (version 0)
INSERT INTO flyway_schema_history (
    installed_rank, version, description, type, script,
    checksum, installed_by, installed_on, execution_time, success
) VALUES (
    1, '0', '<< Flyway Baseline >>', 'BASELINE', '<< Flyway Baseline >>',
    NULL, 'manual', NOW(), 0, 1
);

-- Mark V1 as already applied (since tables exist)
INSERT INTO flyway_schema_history (
    installed_rank, version, description, type, script,
    checksum, installed_by, installed_on, execution_time, success
) VALUES (
    2, '1', 'Initial schema', 'SQL', 'V1__Initial_schema.sql',
    NULL, 'manual', NOW(), 0, 1
);
```

Then start the application.

## Troubleshooting

### Error: "Table already exists"
This means Flyway is trying to create tables that already exist. Use Option 2 above.

### Error: "Flyway failed to initialize"
Check that:
1. Database connection is working
2. User has CREATE TABLE permissions
3. `spring.flyway.baseline-on-migrate=true` is set in application.properties

### Verify Flyway Status
After starting the app, check what migrations have been applied:

```sql
SELECT * FROM flyway_schema_history ORDER BY installed_rank;
```

## Future Migrations

To add new migrations, create new SQL files in `src/main/resources/db/migration/`:

- `V2__Add_new_column.sql`
- `V3__Create_new_table.sql`
- etc.

Naming convention: `V{version}__{description}.sql`

Example:
```sql
-- V2__Add_user_timezone.sql
ALTER TABLE Users ADD COLUMN timezone VARCHAR(50) DEFAULT 'UTC';
```

## Production Deployment

1. Always test migrations on a backup/staging database first
2. Set `spring.flyway.baseline-on-migrate=false` in production
3. Set `spring.flyway.validate-on-migrate=true` in production
4. Consider using `spring.flyway.clean-disabled=true` to prevent accidental data loss
