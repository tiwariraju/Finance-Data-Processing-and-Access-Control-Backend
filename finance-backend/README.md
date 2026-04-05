# Finance Backend

## 1. Project overview and tech stack

REST API for **finance data processing** and **role-based access control**. It provides JWT authentication, user administration, financial record CRUD with soft delete, and dashboard aggregations.

| Layer | Technology |
|--------|------------|
| Runtime | Java 17+ |
| Framework | Spring Boot 3.2.x |
| Security | Spring Security 6.x, JWT (jjwt 0.12.x) |
| Persistence | Spring Data JPA, Hibernate, MySQL 8.x |
| Build | Maven |
| Utilities | Lombok, ModelMapper, Jakarta Validation |

Architecture: **controller → service (interface + impl) → repository → entity**, with DTOs for all API payloads and a global exception handler for consistent error JSON.

---

## 2. Prerequisites

- **JDK 17** or newer  
- **MySQL 8.x** (server running, user with DDL/DML rights)  
- **Maven 3.9+** (or use the Maven Wrapper if you add one)

---

## 3. Setup (step by step)

1. **Clone or copy** this repository and open the `finance-backend` folder.

2. **Create the database** (MySQL client or GUI):

   ```sql
   CREATE DATABASE finance_db;
   ```

3. **Configure** `src/main/resources/application.properties`:

   - `spring.datasource.username` / `spring.datasource.password` — your MySQL credentials  
   - `app.jwt.secret` — **at least 32 bytes** in UTF-8 (a long random string is fine for development)  
   - Optional: `server.port` (default `8080`)

4. **Run the application**:

   ```bash
   mvn spring-boot:run
   ```

5. **Base URL**: `http://localhost:8080`

On first startup, **`DataInitializer`** creates a default admin (if missing) and **sample financial records** when no non-deleted records exist.

| Field | Value |
|--------|--------|
| Email | `admin@finance.com` |
| Password | `admin123` |
| Role | `ADMIN` |

---

## 4. JWT usage

After `POST /api/auth/login` or `POST /api/auth/register`, send the token on every protected request:

```http
Authorization: Bearer <your_jwt_token>
```

Example (curl):

```bash
curl -s http://localhost:8080/api/dashboard/summary ^
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

---

## 5. API reference

All successful payloads are wrapped as:

```json
{ "success": true, "message": "optional", "data": { } }
```

Errors:

```json
{ "success": false, "message": "...", "errors": { "field": "message" } }
```

(`errors` appears for validation failures.)

### 5.1 Authentication

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | `/api/auth/register` | Public | Register and receive JWT |
| POST | `/api/auth/login` | Public | Login and receive JWT |

**Register — request**

```json
POST /api/auth/register
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "role": "ADMIN"
}
```

**Register — response (201)**

```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "email": "john@example.com",
    "role": "ADMIN",
    "name": "John Doe"
  }
}
```

**Login — request**

```json
POST /api/auth/login
Content-Type: application/json

{
  "email": "admin@finance.com",
  "password": "admin123"
}
```

**Login — response (200)**

```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "email": "admin@finance.com",
    "role": "ADMIN",
    "name": "System Admin"
  }
}
```

### 5.2 User management (admin)

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| GET | `/api/users` | ADMIN | Paginated users (`page`, `size`, `sortBy`, `sortDir`) |
| GET | `/api/users/{id}` | ADMIN | User by id |
| PATCH | `/api/users/{id}/role` | ADMIN | Update role |
| PATCH | `/api/users/{id}/status` | ADMIN | Set `ACTIVE` / `INACTIVE` |
| DELETE | `/api/users/{id}` | ADMIN | Hard delete user |

**PATCH role**

```json
PATCH /api/users/2/role
Authorization: Bearer <admin_token>
Content-Type: application/json

{ "role": "ANALYST" }
```

**PATCH status**

```json
PATCH /api/users/2/status
Authorization: Bearer <admin_token>
Content-Type: application/json

{ "status": "INACTIVE" }
```

**GET users (paginated) — example**

`GET /api/users?page=0&size=10&sortBy=id&sortDir=asc`

### 5.3 Financial records

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | `/api/records` | ADMIN | Create record |
| GET | `/api/records` | VIEWER, ANALYST, ADMIN | List + filters + pagination |
| GET | `/api/records/{id}` | VIEWER, ANALYST, ADMIN | Single record |
| PATCH | `/api/records/{id}` | ADMIN | Update record |
| DELETE | `/api/records/{id}` | ADMIN | Soft delete (`isDeleted = true`) |

**Query parameters (GET list)**

| Param | Description |
|--------|-------------|
| `type` | `INCOME` or `EXPENSE` |
| `category` | Exact match (case-insensitive) |
| `startDate`, `endDate` | `yyyy-MM-dd` |
| `search` | Case-insensitive substring in `description` |
| `page`, `size` | Pagination (defaults `0`, `10`) |
| `sortBy` | `date`, `amount`, `category`, `type`, `createdAt`, `id` (invalid values fall back to `date`) |
| `sortDir` | `asc` or `desc` (default `desc`) |

**Create record**

```json
POST /api/records
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "amount": 5000.00,
  "type": "INCOME",
  "category": "Salary",
  "date": "2025-04-01",
  "description": "Monthly salary"
}
```

**Response (201)** — `data` is a `RecordResponse` (`createdBy` is a small user summary).

**GET list response** — `data` is `PagedRecordsResponse` with `content`, `page`, `size`, `totalElements`, `totalPages`.

### 5.4 Dashboard

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| GET | `/api/dashboard/summary` | ANALYST, ADMIN | Totals + record count |
| GET | `/api/dashboard/by-category` | ANALYST, ADMIN | Income/expense per category |
| GET | `/api/dashboard/trends` | ANALYST, ADMIN | `period=monthly` (default) or `yearly` |
| GET | `/api/dashboard/recent` | ANALYST, ADMIN | Recent records (`limit`, default `5`, max `100`) |

**Summary — response**

```json
{
  "success": true,
  "data": {
    "totalIncome": 50000.00,
    "totalExpenses": 30000.00,
    "netBalance": 20000.00,
    "totalRecords": 120
  }
}
```

**By category — response**

```json
{
  "success": true,
  "data": {
    "data": [
      { "category": "Food", "totalIncome": 0, "totalExpense": 900.00 },
      { "category": "Salary", "totalIncome": 20000.00, "totalExpense": 0 }
    ]
  }
}
```

**Trends — request**

`GET /api/dashboard/trends?period=monthly`

**Trends — response**

```json
{
  "success": true,
  "data": {
    "data": [
      { "period": "2025-01", "totalIncome": 20000.00, "totalExpense": 10000.00 }
    ]
  }
}
```

**Recent — request**

`GET /api/dashboard/recent?limit=5`

---

## 6. Role permissions matrix

| Endpoint group | VIEWER | ANALYST | ADMIN |
|----------------|--------|---------|-------|
| `/api/auth/**` | ✓ (public) | ✓ (public) | ✓ (public) |
| `GET /api/records`, `GET /api/records/{id}` | ✓ | ✓ | ✓ |
| `POST/PATCH/DELETE /api/records` | ✗ | ✗ | ✓ |
| `/api/dashboard/**` | ✗ | ✓ | ✓ |
| `/api/users/**` | ✗ | ✗ | ✓ |

---

## 7. HTTP status codes (global handler)

| Situation | Status |
|-----------|--------|
| Resource not found | 404 |
| Validation errors | 400 (`errors` map) |
| Bad login | 401 |
| Missing/invalid JWT on protected routes | 401 (entry point) |
| Insufficient role (`@PreAuthorize`) | 403 |
| Inactive user login | 403 (`DisabledException`) |
| Duplicate email / unique violation | 409 |
| Unexpected server error | 500 |

---

## 8. Assumptions

- **Registration** accepts a `role` in the body as specified; in production you would typically restrict self-registration to `VIEWER` or require an admin to invite users.  
- **JWT secret** in `application.properties` is UTF-8 text; jjwt requires a key length suitable for HS256 (use a long secret in dev; use a strong random secret in production).  
- **Soft-deleted** records are hidden from all queries and dashboard aggregations.  
- **User delete** is a **hard** delete; deleting users does not reassign historical `FinancialRecord` rows (MySQL may enforce FK behavior depending on schema evolution—adjust if you add FK `ON DELETE RESTRICT` issues).  
- **Seed data** runs only when there are **no** non-deleted financial records, so repeated restarts do not duplicate sample rows once data exists.  
- **`data.sql`** is not used for inserts; seeding is entirely in `DataInitializer` so tables exist before inserts (`spring.sql.init.mode=never`).

---

## 9. Design decisions and tradeoffs

- **DTOs + ModelMapper**: Keeps entities off the wire; `RecordResponse` uses a small dedicated mapper (`RecordDtoMapper`) for `createdBy` to avoid brittle nested ModelMapper rules.  
- **Specifications**: Dynamic filters for records stay type-safe and composable; all list paths include `isDeleted = false`.  
- **Dashboard aggregations**: JPQL for type sums and category breakdown; **native SQL** for `DATE_FORMAT` trends so bucketing matches MySQL’s date functions.  
- **JWT filter**: Invalid or malformed tokens clear the security context and rely on the authentication entry point for 401, avoiding duplicate error formats in the filter.  
- **CORS**: Permissive defaults (`*`) for local development; tighten origins in production.  
- **Method security**: `@PreAuthorize` on controllers with `@EnableMethodSecurity` for fine-grained roles alongside a stateless JWT filter chain.

---

## 10. Project layout (reference)

```
finance-backend/
├── src/main/java/com/finance/backend/
│   ├── config/
│   ├── controller/
│   ├── service/ (+ impl/)
│   ├── repository/ (+ specification/)
│   ├── entity/
│   ├── dto/
│   ├── enums/
│   ├── exception/
│   ├── middleware/     (JwtAuthFilter)
│   ├── security/       (SecurityUser)
│   └── util/
├── src/main/resources/
│   ├── application.properties
│   └── data.sql        (placeholder; seeding in Java)
├── pom.xml
└── README.md
```

---

## 11. Build note

If `mvn compile` fails in a restricted environment (e.g. cannot write to `~/.m2`), run Maven on your machine with normal user permissions or configure a local repository directory Maven can write to.
