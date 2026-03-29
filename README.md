# Introduction


# Requirements

This app works with **MySQL**. For simplicity, we recommend using **Docker Compose**:

```bash
docker-compose -f db/docker-compose.yml up -d
```

# DB Schema

The schema is split into individual scripts under `db/`, executed automatically by Docker Compose in order:

| Script | Table | Used by |
|---|---|---|
| `01_init.sql` | `users` | CLI, Java API |
| `02_topics.sql` | `topics` | .NET CIS API |
| `03_ideas.sql` | `ideas` | .NET CIS API |
| `04_votes.sql` | `votes` | .NET CIS API |

# DB Configuration File (For CLI)
You will need a configuration file to connect (example `sd3.xml`):

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE configuration
  PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
  "http://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>
	<environments default="development">
		<environment id="development">
			<transactionManager type="JDBC" />
			<dataSource type="POOLED">
				<property name="driver" value="com.mysql.cj.jdbc.Driver" />
				<property name="url" value="jdbc:mysql://localhost:3307/sd3" />
				<property name="username" value="SOME_USER" />
				<property name="password" value="SOME_PASSWORD" />
			</dataSource>
		</environment>
	</environments>
</configuration>
```

# CLI Usage

Just compile and run this program:

```bash
Users CLI

Usage: users -config=<configuration> [COMMAND]
CRUD on a Users DB
      -config=<configuration>
         Configuration File (xml)
Commands:
  -read    Read Users
  -delete  Delete a User by ID
  -create  Create a new user
  -update  Update an existing user
```

These are common parameters:

_In this examples, sd3.xml is a file that follows the **Db Configuration File**_

### CLI Examples:

* Read users:

```
-config=sd3.xml -read
```

* Create user
```
-config=sd3.xml -create -n javier -l jroca -p pass123
```

* Delete existing user (id = aab5d5fd-70c1-11e5-a4fb-b026b977eb28 )
```
-config=sd3.xml -delete aab5d5fd-70c1-11e5-a4fb-b026b977eb28
```

* Update existing user (id = 3bf71036-e7ef-4890-b79b-91496c14160f)
```
-config=sd3.xml -update -i 3bf71036-e7ef-4890-b79b-91496c14160f -n javier2 -l jroca2 -p pwd321
```



# CIS API Java (cis-api)

A Spring Boot 3 REST API that manages users with JWT authentication.

## Build and Run
```bash
cd cis-api/
mvn clean spring-boot:run
```

## Endpoints (Base Path: `/api/v1`)

| Method   | URL                        | Auth required | Description |
|----------|----------------------------|:---:|-------------|
| `GET`    | `/health`                  | No  | API & Database health check |
| `POST`   | `/auth/login`              | No  | **Login** – returns a JWT token valid for this API and the .NET CIS API |
| `POST`   | `/users`                   | No  | **Create user** – receives a Base64 password, stored as plain text |
| `GET`    | `/users`                   | No  | **List all users** |
| `GET`    | `/users/{id}`              | **Yes** | **Get user by ID** |
| `GET`    | `/users/login/{login}`     | **Yes** | **Get user by login** (response excludes password) |
| `PUT`    | `/users/{id}`              | **Yes** | **Update user** – `name`, `login`, and/or `password` (Base64); partial updates supported |
| `DELETE` | `/users/{id}`              | **Yes** | **Soft delete** – sets `active = false` |
| `GET`    | `/swagger-ui.html`         | No  | Swagger UI interactive documentation |

> **Token usage:** the JWT returned by `POST /auth/login` is shared across both APIs. Include it as `Authorization: Bearer <token>` on all protected endpoints — both here and in the .NET CIS API.

### Request body for `POST /users`

```json
{
  "name": "Full Name",
  "login": "username",
  "password": "BASE64_ENCODED_PASSWORD"
}
```

> The `id` is auto-generated (UUID). For `PUT`, all fields are optional (partial update).

### Response codes

| Code | Meaning |
|------|---------|
| `200` | OK |
| `201` | User created successfully |
| `400` | Bad request (e.g. invalid Base64 password) |
| `404` | User not found |
| `409` | Conflict – user with same `login` already exists |
| `500` | Internal server error |

## Compatibility Note
The API receives the password in **Base64** to avoid plain-text transmission over HTTP. The API **decodes** it before storing in the database to maintain 100% compatibility with the **CLI Legacy**, which reads and writes plain text passwords.

---

# CIS API .NET (cis-api-dotnet)

An ASP.NET Core 10 Web API following Clean Architecture to manage Topics, Ideas, and Votes.

## Build and Run
```bash
cd cis-api-dotnet/
dotnet build
dotnet run
```

## Endpoints

Default port: `http://localhost:5281`

| Method     | URL                     | Auth required | Description                          |
|------------|-------------------------|:---:|--------------------------------------|
| `GET`      | `/api/v1/health`        | No  | API & Database health check          |
| `GET`      | `/api/v1/topics`        | No  | List all topics                      |
| `POST`     | `/api/v1/topics`        | **Yes** | Create a topic                   |
| `PUT`      | `/api/v1/topics/{id}`   | **Yes** | Update a topic                   |
| `DELETE`   | `/api/v1/topics/{id}`   | **Yes** | Delete a topic                   |
| `GET`      | `/api/v1/ideas`         | No  | List all ideas                       |
| `POST`     | `/api/v1/ideas`         | **Yes** | Create an idea                   |
| `PUT`      | `/api/v1/ideas/{id}`    | **Yes** | Update an idea                   |
| `DELETE`   | `/api/v1/ideas/{id}`    | **Yes** | Delete an idea                   |
| `POST`     | `/api/v1/votes`         | **Yes** | Cast a vote                      |
| `DELETE`   | `/api/v1/votes/{id}`    | **Yes** | Remove a vote                    |
| `GET`      | `/swagger`              | No  | Swagger UI interactive documentation |

> **How to authenticate:** call `POST /api/v1/auth/login` on the **Java Users API** to obtain a JWT token, then include it as `Authorization: Bearer <token>` on every protected request.

## Security — JWT Auth Interceptor

All `POST`, `PUT`, and `DELETE` requests require a valid `Authorization` header:

```
Authorization: Bearer <token>
```

The token is obtained from the **CIS Java API** (`POST /api/v1/auth/login`).

### Validation flow

```
Client → .NET API (JwtAuthMiddleware)
           ↓  validates JWT signature & expiry
           ↓  GET /api/v1/users/login/{login}  →  Java Users API
           ↓  checks user exists and is active
        ✓ proceed  /  401 AUTH-401  /  403 AUTH-403
```

### Error responses

| HTTP | Body | Cause |
|------|------|-------|
| `401` | `AUTH-401: Unauthorized access` | Missing / invalid / expired token, or user not found |
| `403` | `AUTH-403: Forbidden - Account Inactive` | Token valid but user account is inactive |

## Configuration
`cis-api-dotnet/appsettings.json` contains two sections to configure:

```json
{
  "ConnectionStrings": {
    "CisDatabase": "Server=...;Port=...;Database=sd3;User=...;Password=..."
  },
  "Jwt": {
    "Secret": "<same secret as the Java API>"
  },
  "UsersApi": {
    "BaseUrl": "http://localhost:8080"
  }
}
```

- **CisDatabase** — shared MySQL database (Topics, Ideas, Votes tables)
- **Jwt.Secret** — must match `jwt.secret` in the Java API's `application.properties`
- **UsersApi.BaseUrl** — base URL of the Java Users API used to validate tokens

---

# Testing & Code Coverage

## CIS API Java (cis-api)

The project includes a robust suite of unit tests for controllers and services.

### Run Tests
```bash
cd cis-api/
mvn clean test
```

### Code Coverage (JaCoCo)
After running the tests, the HTML report is generated at:
`cis-api/target/site/jacoco/index.html`

The project is configured to enforce a minimum of **80% line coverage** for the `jalau.cis.api` package. If the coverage falls below this threshold, the build will fail.

## CIS API .NET (cis-api-dotnet)

Unit tests for `HealthController`, `HealthService`, and `JwtAuthMiddleware` using xUnit and Moq.

### Run Tests
```bash
cd cis-api-dotnet/
dotnet test
```

### Frameworks
| Framework | Role |
|---|---|
| xUnit | Test runner |
| Moq | Mocking service dependencies (`IUserValidationService`, `IHealthService`) |
| EF Core InMemory | In-memory DB for service tests |

### Test coverage
| Test class | Scenarios covered |
|---|---|
| `HealthControllerTests` | DB connected (Healthy), DB disconnected (Degraded) |
| `HealthServiceTests` | In-memory DB reachable |
| `JwtAuthMiddlewareTests` | GET passes through, missing header → 401, invalid token → 401, user not found → 401, inactive user → 403, active user passes through, PUT/DELETE protected, Java API down → 401 |

---

# Traffic Simulator (simulator/)

A config-driven C# console app that simulates concurrent users performing the full CIS flow: register → login → create topic → post ideas → vote → unvote. Used to validate performance, relational integrity, and the JWT interceptor behavior under load.

> **Note:** The simulator has not been end-to-end tested yet because the Topics, Ideas, and Votes endpoints in the .NET CIS API are still pending implementation. It will run automatically once those endpoints are available. The Java Users API endpoints (`/users`, `/auth/login`) are already functional.

## Run

```bash
cd simulator/
dotnet run                       # uses config.json by default
dotnet run -- path/to/config.json  # custom config file
```

## Configuration — `simulator/config.json`

All simulation parameters and endpoint definitions live in `config.json`. No code changes are needed to adjust the simulation.

### `simulation` section

| Field | Description |
|---|---|
| `concurrentUsers` | Number of users simulated in parallel |
| `ideasPerUser` | Ideas each user creates under their topic |
| `votesPerUser` | How many of those ideas each user votes on |
| `delayBetweenRequestsMs` | Pause between requests per user (ms) |

### `apis` section

```json
"apis": {
  "usersApi": "http://localhost:8081/api/v1",
  "cisApi":   "http://localhost:5281/api/v1"
}
```

Change these URLs to point to any environment (local, staging, CI).

### `endpoints` section

Each entry defines one HTTP call the simulator can make:

```json
"createTopic": {
  "api":          "cisApi",
  "method":       "POST",
  "path":         "/topics",
  "requiresAuth": true,
  "body": {
    "title":       { "type": "sentence", "prefix": "Topic" },
    "description": { "type": "sentence", "prefix": "About" },
    "userId":      { "type": "context",  "key": "userId" }
  },
  "expectId": "id"
}
```

**Supported field types:**

| Type | Generated value |
|---|---|
| `name` | Random display name (`SimUser_xxxxxxxx`) |
| `username` | Random login handle (`sim_xxxxxxxx`) |
| `base64password` | Base64-encoded random password |
| `sentence` | Short phrase with optional `prefix` |
| `uuid` | Random UUID v4 |
| `context` | Value previously stored in the user session (e.g. `userId`, `topicId`, `token`) |

To add or change an endpoint, edit `config.json` only — no code changes required.

## Simulated flow per user

```
1. Register     →  POST /users          (Java Users API)
2. Login        →  POST /auth/login     (Java Users API) → JWT
3. Create Topic →  POST /topics         (CIS .NET API, JWT required)
4. Create Ideas →  POST /ideas  × n     (CIS .NET API, JWT required)
5. Cast Votes   →  POST /votes  × n     (CIS .NET API, JWT required)
6. Unvote       →  DELETE /votes/{id}   (CIS .NET API, JWT required)
```

## Error reporting

Any `401`, `403`, or `500` response is captured and printed in red at the end with full detail (step, status code, response body). The process exits with code `1` if any errors occurred, making it CI-friendly.

---

# Log of Changes

- **V2.5. March 2026**: Added config-driven Traffic Simulator (`simulator/`) for concurrent load testing of the full CIS flow.
- **V2.4. March 2026**: Implemented JWT Auth Interceptor in .NET API — delegated token validation to Java Users API (AUTH-401 / AUTH-403).
- **V2.3. March 2026**: Initial setup for CIS API (C# / .NET 10) with Clean Architecture and EF Core for Topics/Ideas/Votes.
- **V2.2. March 2026**: Implemented Unit Tests (JUnit 5 + Mockito) and JaCoCo coverage (80% min).
- **V2.1. March 2026**: Added full CRUD endpoints and complete API documentation. Added JWT authentication.
- **V2.0. March 2026**: Added `cis-api` with Base64 decoding for legacy compatibility.
- **V1.0. February 2023**: Initial Version J.ROCA (MasterClass Professor)

_This project is property of Jala University. Do not distribute externally._
