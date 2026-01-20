# Docker Setup Guide

This guide explains how to run the Portfolio Finance Tracker using Docker and Docker Compose.

## Prerequisites

- Docker Desktop installed ([Download](https://www.docker.com/products/docker-desktop/))
- Docker Compose (included with Docker Desktop)

## Quick Start

### 1. Create Environment File

Copy the example environment file and fill in your credentials:

```bash
cp .env.example .env
```

Edit `.env` and add your actual values:
- `DB_PASSWORD` - Choose a secure MySQL password
- `APP_JWT_SECRET` - Generate with: `openssl rand -base64 32`
- `APP_ENCRYPTION_KEY` - Generate with: `openssl rand -hex 32`
- `PLAID_CLIENT_ID` - Get from [Plaid Dashboard](https://dashboard.plaid.com/)
- `PLAID_SECRET` - Get from Plaid Dashboard

### 2. Build and Start

```bash
# Build and start all services (MySQL + Spring Boot app)
docker-compose up -d

# View logs
docker-compose logs -f

# View only app logs
docker-compose logs -f app
```

### 3. Access the Application

Once started, the application will be available at:
- **App**: http://localhost:8080
- **MySQL**: localhost:3306

Wait for the health check to pass (you'll see "healthy" status):
```bash
docker-compose ps
```

## Common Commands

```bash
# Stop all services
docker-compose down

# Stop and remove volumes (WARNING: deletes all data)
docker-compose down -v

# Rebuild after code changes
docker-compose up -d --build

# View running containers
docker-compose ps

# Access MySQL shell
docker-compose exec db mysql -u root -p

# Access app container shell
docker-compose exec app bash

# Restart just the app
docker-compose restart app
```

## Architecture

The Docker setup includes:

1. **MySQL Container** (`db`)
   - MySQL 8.0
   - Persistent volume for data
   - Health checks
   - Port 3306 exposed

2. **Spring Boot App Container** (`app`)
   - Multi-stage build (Maven + JRE)
   - Runs as non-root user
   - Auto-restarts on failure
   - Health checks via Spring Actuator
   - Port 8080 exposed

3. **Network**
   - Both containers on `portfolio-network`
   - Containers can communicate via service names

## Database Migrations

Flyway migrations run automatically on application startup. The app waits for MySQL to be healthy before starting.

Migrations applied:
- V1: Initial schema (users, accounts, transactions, plaid_items)
- V2: Refresh tokens table
- V3: Transaction deduplication with plaid_transaction_id
- V4: Hash refresh tokens
- V5: Transaction cursor for incremental sync

## Troubleshooting

### App won't start

**Check logs:**
```bash
docker-compose logs app
```

**Common issues:**
- Missing environment variables in `.env`
- Database not ready (wait for MySQL health check to pass)
- Port 8080 already in use

### Database connection failed

**Check MySQL is healthy:**
```bash
docker-compose ps
```

**Reset database:**
```bash
docker-compose down -v
docker-compose up -d
```

### App is slow to start

The first build takes time (Maven downloads dependencies). Subsequent builds are faster due to Docker layer caching.

### Health check failing

Wait 60 seconds for the app to fully start. Check logs:
```bash
docker-compose logs -f app
```

## Production Deployment

For production:

1. **Update `.env`:**
   ```bash
   SPRING_PROFILES_ACTIVE=prod
   COOKIES_SECURE=true
   PLAID_ENV=production
   ```

2. **Use secrets management** instead of `.env` file

3. **Configure HTTPS** with reverse proxy (nginx/Caddy)

4. **Set strong passwords** for MySQL

5. **Configure backups** for MySQL volume:
   ```bash
   docker-compose exec db mysqldump -u root -p portfolio > backup.sql
   ```

6. **Use Docker secrets** or Kubernetes secrets for sensitive data

## Development

To make code changes:

1. Edit your code
2. Rebuild and restart:
   ```bash
   docker-compose up -d --build
   ```

For faster development, you can also run the app locally with:
```bash
mvn spring-boot:run
```

And only use Docker for MySQL:
```bash
docker-compose up -d db
```

## Cleaning Up

```bash
# Stop and remove containers
docker-compose down

# Remove volumes (deletes all data)
docker-compose down -v

# Remove images
docker-compose down --rmi all

# Full cleanup (containers, volumes, images, networks)
docker-compose down -v --rmi all
```

## Support

For issues, check:
- Application logs: `docker-compose logs app`
- Database logs: `docker-compose logs db`
- Container status: `docker-compose ps`
- Health status: `curl http://localhost:8080/actuator/health`
