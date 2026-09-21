# Fleet Management Backend - Docker Setup Guide

This guide explains how to run the Fleet Management backend server as a Docker container.

## Prerequisites

- Docker Desktop installed and running
- Docker Compose (for running PostgreSQL database)
- Basic knowledge of Docker commands

## Quick Start

### 1. Clone the Repository

```bash
git clone <repository-url>
cd fleetmanagement
```

### 2. Set Up Environment Variables

For local development with Docker Compose, use the provided `.env.local` file:

```bash
# .env.local is already configured for local development
# No changes needed unless you want to customize credentials
```

The `.env.local` file is pre-configured with:
- Local PostgreSQL connection (`postgres:5432/fleet`)
- Default database credentials
- JWT configuration for development

**Note:** The `.env` file (if present) is used for production/GCP configurations and is ignored by Docker Compose. Use `.env.local` for local development.

### 3. Start the Application Stack

Start both PostgreSQL and the backend with a single command:

```bash
docker-compose up -d
```

This will:
- Build the backend Docker image (if not already built)
- Start PostgreSQL on port 5435
- Start the backend on port 8080
- Connect the backend to the PostgreSQL database
- Wait for the database to be healthy before starting the backend

Verify everything is running:

```bash
docker-compose ps
```

### 4. Verify the Backend is Running

Check the container status:

```bash
docker ps
```

Test the health endpoint:

```bash
curl http://localhost:8080/actuator/health
```

Access the API documentation (Swagger UI):

```
http://localhost:8080/swagger-ui/index.html
```

## API Endpoints

Once running, the backend will be available at:

- **Base URL:** `http://localhost:8080`
- **Health Check:** `http://localhost:8080/actuator/health`
- **API Documentation:** `http://localhost:8080/swagger-ui/index.html`
- **OpenAPI Spec:** `http://localhost:8080/v3/api-docs`

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_URL` | JDBC connection string | `jdbc:postgresql://localhost:5435/fleet` |
| `DB_USERNAME` | Database username | `admin` |
| `DB_PASSWORD` | Database password | `Qwerty@123` |
| `JWT_SECRET` | Secret key for JWT tokens | (see .env.sample) |
| `JWT_EXPIRATION_MS` | JWT token expiration in ms | `86400000` (24 hours) |
| `SERVER_PORT` | Server port | `8080` |

## Common Docker Commands

### View Logs

```bash
# For backend container
docker logs fleet-backend

# For database container
docker logs fleet-postgres

# Follow logs in real-time
docker logs -f fleet-backend
```

### Stop Containers

```bash
# Stop backend
docker stop fleet-backend

# Stop database
docker-compose stop postgres

# Stop all services
docker-compose down
```

### Restart Containers

```bash
docker restart fleet-backend
```

### Remove Containers

```bash
# Remove backend container
docker rm fleet-backend

# Remove all containers and volumes
docker-compose down -v
```

### Rebuild the Image

After making code changes:

```bash
docker build -t fleetmanagement-backend:latest .
docker stop fleet-backend
docker rm fleet-backend
docker run -d --name fleet-backend --network fleet-network -p 8080:8080 --env-file .env fleetmanagement-backend:latest
```

## Troubleshooting

### Container won't start

Check the logs:
```bash
docker logs fleet-backend
```

Common issues:
- Database not running: Start PostgreSQL with `docker-compose up -d postgres`
- Port conflict: Ensure port 8080 is not in use
- Environment variables missing: Verify `.env` file exists and is properly formatted

### Can't connect to database

- Ensure PostgreSQL container is running: `docker-compose ps`
- Check database hostname in `DB_URL` (use `postgres` when running in Docker network)
- Verify database credentials match between `.env` and PostgreSQL container

### Health check failing

The application may still be starting up. Wait 30-60 seconds after container start before checking health.

### View container details

```bash
docker inspect fleet-backend
```

## Development Workflow

1. Make code changes
2. Rebuild the Docker image: `docker build -t fleetmanagement-backend:latest .`
3. Restart the container: `docker restart fleet-backend`
4. Test changes via Swagger UI or your frontend application

## Network Configuration

The application uses a custom Docker network `fleet-network` to communicate with the PostgreSQL database. Both containers must be on the same network.

## Security Notes

- Never commit `.env` files to version control
- Use strong passwords in production
- Change the default JWT secret in production
- Consider using Docker secrets for sensitive data in production

## Support

For issues or questions, contact the backend development team.
