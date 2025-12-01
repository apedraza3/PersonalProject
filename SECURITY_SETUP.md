# Security Setup Guide

## CRITICAL: Environment Variables Configuration

Your application now uses environment variables for sensitive configuration. You **MUST** set these up before running the application.

## Step 1: Create Your .env File

1. Copy `.env.example` to `.env`:
   ```bash
   cp .env.example .env
   ```

2. **IMPORTANT**: Add `.env` to your `.gitignore` to prevent committing secrets:
   ```bash
   echo ".env" >> .gitignore
   ```

## Step 2: Generate a Strong JWT Secret

Generate a secure JWT secret key (at least 256 bits):

```bash
# On Linux/Mac:
openssl rand -base64 64

# On Windows (PowerShell):
[Convert]::ToBase64String((1..64 | ForEach-Object { Get-Random -Minimum 0 -Maximum 256 }))

# Or use an online generator (be cautious):
# https://generate-random.org/api-token-generator
```

Copy the generated string and paste it as your `JWT_SECRET` in the `.env` file.

## Step 3: Configure Database Credentials

Update your `.env` file with your actual database credentials:

```env
DB_PASSWORD=your_actual_database_password
```

**IMPORTANT**: If you previously committed your database password to Git, you should:
1. Change your database password immediately
2. Update the `.env` file with the new password
3. Consider using Git history rewriting tools (like `git filter-branch` or `BFG Repo Cleaner`) to remove the old password from Git history

## Step 4: Configure Plaid API Credentials

1. Sign up at https://plaid.com/
2. Get your Client ID and Secret from the Plaid Dashboard
3. Update your `.env` file:

```env
PLAID_CLIENT_ID=your_actual_plaid_client_id
PLAID_SECRET=your_actual_plaid_secret
```

## Step 5: Set Environment Variables

### Option A: Using .env File with Spring Boot

Install the `spring-boot-dotenv` dependency in your `pom.xml`:

```xml
<dependency>
    <groupId>me.paulschwarz</groupId>
    <artifactId>spring-dotenv</artifactId>
    <version>4.0.0</version>
</dependency>
```

### Option B: Set System Environment Variables

#### Windows:
```powershell
# PowerShell
$env:DB_PASSWORD="your_password"
$env:JWT_SECRET="your_jwt_secret"
# ... etc
```

Or set permanently via System Properties > Environment Variables

#### Linux/Mac:
```bash
export DB_PASSWORD="your_password"
export JWT_SECRET="your_jwt_secret"
# ... etc
```

Or add to your `~/.bashrc` or `~/.zshrc`

### Option C: IntelliJ IDEA / IDE Configuration

1. Go to Run > Edit Configurations
2. Select your Spring Boot application
3. Click "Modify options" > "Environment variables"
4. Add all required environment variables

## Step 6: Verify Configuration

Run your application and check for errors. If you see errors like:

```
Could not resolve placeholder 'DB_PASSWORD' in value "${DB_PASSWORD}"
```

This means the environment variable is not set properly.

## Production Deployment

For production environments:

1. **NEVER** commit `.env` files to version control
2. Use proper secrets management:
   - AWS: AWS Secrets Manager or Parameter Store
   - Azure: Azure Key Vault
   - Google Cloud: Secret Manager
   - Heroku: Config Vars
   - Docker: Docker Secrets
   - Kubernetes: Kubernetes Secrets

3. Rotate all secrets that were previously exposed:
   - Change database password
   - Generate new JWT secret
   - Rotate Plaid API credentials if compromised

## Required Environment Variables

| Variable | Required | Description |
|----------|----------|-------------|
| `DB_URL` | No | Database connection URL (has default) |
| `DB_USERNAME` | No | Database username (defaults to 'root') |
| `DB_PASSWORD` | **YES** | Database password (no default for security) |
| `JWT_SECRET` | **YES** | JWT signing secret (no default for security) |
| `JWT_TTL_SECONDS` | No | JWT token lifetime in seconds (defaults to 86400) |
| `PLAID_ENV` | No | Plaid environment (defaults to 'sandbox') |
| `PLAID_CLIENT_ID` | **YES** | Your Plaid client ID |
| `PLAID_SECRET` | **YES** | Your Plaid secret key |
| `PLAID_PRODUCTS` | No | Plaid products to use (has default) |
| `PLAID_COUNTRY_CODES` | No | Country codes (defaults to 'US') |
| `PLAID_REDIRECT_URI` | No | OAuth redirect URI (has default) |

## Troubleshooting

**Problem**: Application fails to start with placeholder errors
- **Solution**: Ensure all required environment variables are set

**Problem**: JWT tokens are invalid after restart
- **Solution**: Make sure `JWT_SECRET` is consistent across restarts

**Problem**: Database connection fails
- **Solution**: Verify `DB_PASSWORD` is correct and database is running

## Security Best Practices

1. ✅ Use strong, unique passwords for database
2. ✅ Generate JWT secrets with at least 256 bits of entropy
3. ✅ Never commit `.env` files to Git
4. ✅ Use different secrets for development, staging, and production
5. ✅ Rotate secrets regularly (every 90 days recommended)
6. ✅ Use secrets management services in production
7. ✅ Limit access to production secrets to only necessary personnel
8. ✅ Enable audit logging for secret access in production
