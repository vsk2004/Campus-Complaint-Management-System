# CCMS Backend — Spring Boot

This is a Spring Boot 3 / Java 17 backend for the Campus Complaint Management System.
It uses MySQL through Spring Data JPA and is designed to keep the existing React frontend
API contract unchanged.

## ⚠️ Rotate your credentials

The uploaded project's `server/.env` contained live secrets committed in plain text:

- any old MongoDB Atlas connection string with username/password
- a Gmail address + app password (`EMAIL_USER` / `EMAIL_PASS`)
- a SendGrid API key

**None of these were copied into this project.** Since they were sitting in a zip file,
treat them as compromised and rotate all three (change the old Mongo user's password, revoke/
regenerate the Gmail app password, and revoke/regenerate the SendGrid key) before deploying
this anywhere.

## What changed structurally

| Node/Express | Spring Boot |
|---|---|
| `server.js` | `CcmsApplication.java` + `config/SecurityConfig.java` |
| `models/*.js` (Mongoose schemas) | `model/*.java` (Spring Data JPA entities) |
| `middleware/authMiddleware.js` | `security/JwtAuthFilter.java` + `security/JwtService.java` |
| `middleware/roles.js` / `authorize()` | `hasRole(...)` rules in `SecurityConfig` + `@PreAuthorize` |
| `controllers/*.js` | `service/*.java` (business logic) + `controller/*.java` (HTTP layer) |
| `routes/*.js` | `controller/*.java` `@RequestMapping`s |
| `@sendgrid/mail` + `nodemailer` | `service/EmailService.java` (Spring Mail / JavaMailSender) |
| `seeder/adminSeeder.js` (manual script) | `seed/AdminSeeder.java` (`CommandLineRunner`, runs automatically & idempotently on boot) |

The MySQL schema is created/updated by Hibernate by default: `users` and `complaints` tables.
JSON response shapes were kept **byte-compatible with the existing React frontend**,
including two quirks worth knowing about:

- `/api/auth/login` returns `user.id` (not `_id`), matching the original controller.
- Faculty listings and complaint responses return `_id`, matching Mongoose's default
  `toJSON()` output, since that's what `ManageFaculty.jsx`, `AllComplaints.jsx`, etc. read.

## Preserved behavior (including a couple of pre-existing quirks)

I ported the *active* logic exactly (the original files had a lot of commented-out history —
I only converted the code that was actually running), including:

- `GET /api/complaints` only allows roles `"admin"` or `"staff"`. The `User` role enum is
  actually `student | faculty | admin` — `"staff"` is never assigned to anyone — so today
  this endpoint is effectively **admin-only**. That's exactly how the original behaved; I
  left it as-is rather than guessing whether you meant `"faculty"`. Change the role set in
  `ComplaintService.ALL_COMPLAINTS_ROLES` if you want faculty to see all complaints too.
- `createFaculty`'s response no longer includes the bcrypt password hash (the original did
  leak it in the JSON body). The frontend never reads that field, so this is a pure security
  fix with no behavioral impact.
- Email sending failures are logged and swallowed rather than failing the request, matching
  the original's per-`sgMail.send()` try/catch.

## Configuration

All configuration is environment-variable driven (see `src/main/resources/application.properties`
for the full list and defaults):

| Env var | Purpose |
|---|---|
| `PORT` | HTTP port (default 8082) |
| `MYSQL_URL` | JDBC connection string |
| `MYSQL_USER`, `MYSQL_PASSWORD` | MySQL credentials |
| `JPA_DDL_AUTO` | Hibernate schema mode (default `update`) |
| `JPA_SHOW_SQL` | Log SQL statements (default `true`) |
| `JWT_SECRET` | HMAC signing secret for JWTs |
| `JWT_EXPIRATION_MS` | Token lifetime in ms (default 7 days, same as before) |
| `CORS_ALLOWED_ORIGINS` | Comma-separated list of allowed origins |
| `FRONTEND_URL` | Used to build the password-reset link |
| `MAIL_HOST`, `MAIL_PORT`, `EMAIL_USER`, `EMAIL_PASS` | SMTP credentials for password-reset / complaint notification emails |
| `SEED_ADMIN1_EMAIL`, `SEED_ADMIN1_PASSWORD`, `SEED_ADMIN2_EMAIL`, `SEED_ADMIN2_PASSWORD` | Default admin accounts seeded on startup |

Email sending was switched from SendGrid's HTTP API to standard SMTP via Spring Mail, since
that's the idiomatic Spring Boot approach and works with the same Gmail account
(`EMAIL_USER`/`EMAIL_PASS`) the project already had configured for its nodemailer code path.
If you'd rather keep using SendGrid, swap `EmailService` to call SendGrid's Java SDK/HTTP API
instead — the rest of the app only depends on `EmailService.sendHtml(...)`.

## Running locally

```bash
export MYSQL_URL="jdbc:mysql://localhost:3306/complaint?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
export MYSQL_USER="complaint_user"
export MYSQL_PASSWORD="Complaint@123"
export JWT_SECRET="some-long-random-string"
export EMAIL_USER="you@gmail.com"
export EMAIL_PASS="your-gmail-app-password"
export FRONTEND_URL="http://localhost:5173"

mvn spring-boot:run
```

The API will be available at `http://localhost:8082/api/...`
server, and `GET /health` still returns `{ "ok": true }`.

## Serving the React frontend from this backend

The original `server.js` served `client/build` as static files with a catch-all route back
to `index.html`. `config/SpaWebConfig.java` replicates that: build the React app and copy the
contents of `client/build` (or `client/dist`) into `src/main/resources/static/` before
packaging, and Spring Boot will serve it the same way. If you deploy the frontend separately
(its own static host / CDN), you can delete `SpaWebConfig.java` — it's not required for the
API to work.

## Building a deployable jar

```bash
mvn clean package
java -jar target/ccms-backend.jar
```
