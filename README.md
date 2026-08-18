# Campus Complaint Management System (CCMS)

CCMS is a full-stack Campus Complaint Management System with a React frontend and a Spring Boot backend. Students can register, log in, and raise complaints. Faculty can review department complaints, and admins can manage complaints and faculty accounts.

## Project structure

```text
ccms/
├── ccms-backend/          # Spring Boot 3 backend API
│   ├── src/main/java/com/ccms/
│   ├── src/main/resources/application.properties
│   ├── Dockerfile
│   └── pom.xml
├── client/                # React + Vite frontend
│   ├── src/
│   ├── Dockerfile
│   ├── nginx.conf
│   ├── package.json
│   └── vite.config.js
└── docker-compose.yml     # Frontend + backend + MySQL
```

## Tech stack

| Layer | Technology |
|---|---|
| Frontend | React 19, Vite, Tailwind CSS, Axios, React Router |
| Backend | Java 17, Spring Boot 3.3, Spring Web, Spring Security, Spring Data JPA |
| Database | MySQL 8 |
| Auth | JWT |
| Email | Spring Mail / SMTP |
| Containers | Docker, Docker Compose, Nginx |

## Features

- Student registration and login
- JWT-based authentication
- Role-based access for students, faculty, and admins
- Raise and view personal complaints
- Faculty department complaint review
- Admin complaint management
- Faculty account creation, listing, and deletion
- Password reset email flow
- Automatic admin seeding on backend startup
- Dockerized frontend, backend, and MySQL database

## Prerequisites

Install these before running the project locally:

- Java 17
- Maven 3.9+
- Node.js 20+ or 22+
- npm
- MySQL 8, if running without Docker
- Docker and Docker Compose, if running with containers

## Quick start with Docker

From the project root:

```bash
docker-compose up --build
```

The services will be available at:

```text
Frontend: http://localhost:3000
Backend:  http://localhost:8082
Health:   http://localhost:8082/health
MySQL:    localhost:3306
```

Docker Compose starts three services:

| Service | Purpose | Port |
|---|---|---|
| `frontend` | Builds the Vite app and serves it with Nginx | `3000` |
| `backend` | Runs the Spring Boot API | `8082` |
| `mysql` | Stores users and complaints | `3306` |

To stop the containers:

```bash
docker-compose down
```

To stop containers and remove the MySQL volume:

```bash
docker-compose down -v
```

## Docker files

This project uses separate Dockerfiles:

- `ccms-backend/Dockerfile`
- `client/Dockerfile`

This is the recommended setup because the backend and frontend have different build tools and runtimes. The backend is a Java application, while the frontend is static HTML/CSS/JS served by Nginx.

A single Dockerfile for both is possible, but it is usually less flexible. It would place the frontend and backend in one container, which makes scaling, restarting, logging, and deployment harder. For this project, separate images managed by one `docker-compose.yml` is cleaner.

## Environment variables

### Backend

The backend reads configuration from environment variables in `ccms-backend/src/main/resources/application.properties`.

| Variable | Default | Purpose |
|---|---|---|
| `PORT` | `8082` | Backend server port |
| `MYSQL_URL` | Local MySQL JDBC URL | MySQL connection string |
| `MYSQL_USER` | `complaint_user` | MySQL username |
| `MYSQL_PASSWORD` | `Complaint@123` | MySQL password |
| `JPA_DDL_AUTO` | `update` | Hibernate schema mode |
| `JPA_SHOW_SQL` | `true` | Enables SQL logging |
| `JWT_SECRET` | `super_secret_change_me` | JWT signing secret |
| `JWT_EXPIRATION_MS` | `604800000` | JWT expiration time in milliseconds |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173,https://campus-client.onrender.com` | Allowed frontend origins |
| `FRONTEND_URL` | `http://localhost:5173` | Frontend URL used in password reset links |
| `MAIL_HOST` | `smtp.gmail.com` | SMTP host |
| `MAIL_PORT` | `587` | SMTP port |
| `EMAIL_USER` | Project default | SMTP username |
| `EMAIL_PASS` | Project default | SMTP password/app password |
| `SEED_ADMIN1_EMAIL` | `admin1@rgukt.ac.in` | First seeded admin email |
| `SEED_ADMIN1_PASSWORD` | `admin@123` | First seeded admin password |
| `SEED_ADMIN2_EMAIL` | `admin2@rgukt.ac.in` | Second seeded admin email |
| `SEED_ADMIN2_PASSWORD` | `Admin@123` | Second seeded admin password |

For real deployment, change all default passwords and secrets.

### Frontend

The frontend uses Vite environment variables:

| Variable | Local value | Purpose |
|---|---|---|
| `VITE_API_URL` | `http://localhost:8082/api` | Backend API base URL |
| `VITE_FRONTEND_URL` | `http://localhost:5173` or `http://localhost:3000` | Frontend URL for reset password links |

For Docker, these values are passed as build arguments in `docker-compose.yml`.

## Run locally without Docker

### 1. Start MySQL

Create a database and user that match the backend defaults:

```sql
CREATE DATABASE IF NOT EXISTS complaint;
CREATE USER IF NOT EXISTS 'complaint_user'@'%' IDENTIFIED BY 'Complaint@123';
GRANT ALL PRIVILEGES ON complaint.* TO 'complaint_user'@'%';
FLUSH PRIVILEGES;
```

### 2. Run the backend

```bash
cd ccms-backend

export MYSQL_URL="jdbc:mysql://localhost:3306/complaint?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
export MYSQL_USER="complaint_user"
export MYSQL_PASSWORD="Complaint@123"
export JWT_SECRET="change_this_to_a_long_random_secret_at_least_32_chars"
export CORS_ALLOWED_ORIGINS="http://localhost:5173"
export FRONTEND_URL="http://localhost:5173"

mvn spring-boot:run
```

The backend runs at:

```text
http://localhost:8082
```

### 3. Run the frontend

Create or update `client/.env`:

```env
VITE_API_URL=http://localhost:8082/api
VITE_FRONTEND_URL=http://localhost:5173
```

Then start Vite:

```bash
cd client
npm install
npm run dev
```

The frontend runs at:

```text
http://localhost:5173
```

## Build commands

### Backend

```bash
cd ccms-backend
mvn clean package
java -jar target/ccms-backend.jar
```

### Frontend

```bash
cd client
npm run build
npm run preview
```

The Vite production build is generated in:

```text
client/build
```

## API overview

Base API URL:

```text
http://localhost:8082/api
```

Main endpoints:

| Method | Endpoint | Purpose |
|---|---|---|
| `GET` | `/health` | Backend health check |
| `POST` | `/api/auth/register` | Register a student |
| `POST` | `/api/auth/login` | Login and receive JWT |
| `POST` | `/api/auth/faculty` | Create faculty account |
| `GET` | `/api/auth/faculty` | List faculty accounts |
| `DELETE` | `/api/auth/faculty/{id}` | Delete faculty account |
| `POST` | `/api/auth/forgot-password` | Send password reset email |
| `PUT` | `/api/auth/reset-password/{token}` | Reset password |
| `POST` | `/api/complaints` | Create complaint |
| `GET` | `/api/complaints/mine` | View logged-in user's complaints |
| `GET` | `/api/complaints` | View all complaints |
| `PUT` | `/api/complaints/{id}` | Update complaint status |
| `DELETE` | `/api/complaints/{id}` | Delete complaint |
| `GET` | `/api/complaints/department` | View faculty department complaints |
| `PUT` | `/api/complaints/faculty-update/{id}` | Faculty complaint update |
| `GET` | `/api/admin/dashboard` | Admin dashboard data |
| `GET` | `/api/faculty/dashboard` | Faculty dashboard data |
| `GET` | `/api/student` | Student endpoint |
| `POST` | `/api/student` | Student endpoint |

Protected routes require an `Authorization` header:

```text
Authorization: Bearer <jwt-token>
```

## Default admin accounts

The backend seeds admin accounts automatically on startup if they do not already exist.

| Name | Email | Password |
|---|---|---|
| Super Admin | `admin1@rgukt.ac.in` | `admin@123` |
| System Admin | `admin2@rgukt.ac.in` | `Admin@123` |

Change these through environment variables before deploying.

## Security notes

- Do not use default database passwords in production.
- Set a strong `JWT_SECRET`.
- Replace default admin credentials.
- Use environment variables or a secret manager for SMTP credentials.
- Do not commit real email passwords, API keys, database URLs, or JWT secrets.
- Restrict `CORS_ALLOWED_ORIGINS` to your real frontend domain in production.

## Useful commands

Run backend tests:

```bash
cd ccms-backend
mvn test
```

Build frontend:

```bash
cd client
npm run build
```

Validate Docker Compose:

```bash
docker-compose config
```

Rebuild and start everything:

```bash
docker-compose up --build
```

View logs:

```bash
docker-compose logs -f
```

View backend logs only:

```bash
docker-compose logs -f backend
```

## Deployment notes

For deployment, you can either:

1. Deploy frontend and backend separately.
2. Deploy all services with Docker Compose on one server.
3. Serve the built React frontend from Spring Boot by copying `client/build` into `ccms-backend/src/main/resources/static`.

Separate deployment is usually best:

- Frontend on a static host or Nginx container
- Backend as a Spring Boot container
- MySQL as a managed database or dedicated database container

Before production deployment, update:

- `VITE_API_URL`
- `VITE_FRONTEND_URL`
- `CORS_ALLOWED_ORIGINS`
- `FRONTEND_URL`
- `MYSQL_URL`
- `MYSQL_USER`
- `MYSQL_PASSWORD`
- `JWT_SECRET`
- `EMAIL_USER`
- `EMAIL_PASS`
- seeded admin credentials

## Verification status

These commands were verified successfully:

```bash
cd client && npm run build
cd ccms-backend && mvn test
docker-compose config
```

The full Docker build could not be verified in this environment because the local Docker Compose client could not connect to the Docker daemon.
# Campus-Complaint-Management-System
# Campus-Complaint-Management-System
