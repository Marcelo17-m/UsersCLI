# Introduction


# Requirements

This app works with **MySQL**. For simplicity, we recommend using **Docker**:

Pull Image:
```bash
docker pull mysql:latest
```

Run Container:
```bash
docker run -d --name sd3db -e MYSQL_ROOT_PASSWORD=sd5 -p 3314:3306 mysql
```

# DB Schema

We use the following DB Schema (MySQL, we use schema **sd3** for this example):

```sql
CREATE DATABASE IF NOT EXISTS sd3;
USE sd3;

CREATE TABLE IF NOT EXISTS `users` (
  `id` VARCHAR(36) NOT NULL,
  `name` VARCHAR(200) NOT NULL,
  `login` VARCHAR(20) NOT NULL,
  `password` VARCHAR(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC) VISIBLE
);
```

This schema needs to be created once. Both CLI and API use this same table.

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



# CIS API (Modern Extension)

A Spring Boot 3 REST API that allows user registration.

## Build and Run
```bash
cd cis-api/
mvn clean spring-boot:run
```

## Endpoints (Base Path: `/api/v1`)

| Method   | URL                        | Description |
|----------|----------------------------|-------------|
| `GET`    | `/health`                  | API & Database health check |
| `POST`   | `/users`                   | **Create user** – receives a Base64 password and stores it as plain text |
| `GET`    | `/users`                   | **List all users** |
| `GET`    | `/users/{id}`              | **Get user by ID** |
| `GET`    | `/users/login/{login}`     | **Get user by login** (response excludes password) |
| `PUT`    | `/users/{id}`              | **Update user** – `name`, `login`, and/or `password` (Base64); partial updates supported |
| `DELETE` | `/users/{id}`              | **Physical delete** – removes the record from the database |
| `GET`    | `/swagger-ui.html`         | Swagger UI interactive documentation |

### Request body for `POST /users` and `PUT /users/{id}`

```json
{
  "id": "unique-uuid",
  "name": "Full Name",
  "login": "username",
  "password": "BASE64_ENCODED_PASSWORD"
}
```

> For `PUT`, all fields except `id` are optional (partial update).

### Response codes

| Code | Meaning |
|------|---------|
| `200` | OK |
| `201` | User created successfully |
| `400` | Bad request (e.g. invalid Base64 password) |
| `404` | User not found |
| `409` | Conflict – user with same `id` or `login` already exists |
| `500` | Internal server error |

## Compatibility Note
The API receives the password in **Base64** to avoid plain-text transmission over HTTP. The API **decodes** it before storing in the database to maintain 100% compatibility with the **CLI Legacy**, which reads and writes plain text passwords.

The `DELETE` endpoint performs a **physical delete** to match the legacy behavior.

# Testing & Code Coverage

The project includes a robust suite of unit tests for controllers and services.

## Run Tests
To execute all tests and generate the coverage report:
```bash
cd cis-api/
mvn clean test
```

## Code Coverage (JaCoCo)
After running the tests, the HTML report is generated at:
`cis-api/target/site/jacoco/index.html`

The project is configured to enforce a minimum of **80% line coverage** for the `jalau.cis.api` package. If the coverage falls below this threshold, the build will fail.

---

---

# Log of Changes 

- **V2.2. March 2026**: Implemented Unit Tests (JUnit 5 + Mockito) and JaCoCo coverage (80% min).
- **V2.1. March 2026**: Added full CRUD endpoints and complete API documentation. Removed `active` column for legacy compatibility.
- **V2.0. March 2026**: Added `cis-api` with Base64 decoding for legacy compatibility.
- **V1.0. February 2023**: Initial Version J.ROCA (MasterClass Professor)

_This project is property of Jala University. Do not distribute externally._