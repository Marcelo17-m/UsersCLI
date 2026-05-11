# CIS API Traffic Simulator

A config-driven console tool written in C# (.NET 10) that simulates concurrent users performing the full CIS platform flow. Its purpose is to validate performance, relational integrity, and the correct behavior of the JWT security interceptor under a realistic load scenario.

> **Status:** Not yet end-to-end tested. The Java Users API endpoints (`/users`, `/auth/login`) are operational, but the .NET CIS API endpoints for Topics, Ideas, and Votes are still pending implementation. The simulator will run the full flow automatically once those endpoints are available. No code changes are needed — only `config.json` adjustments.

---

## Requirements

- [.NET 10 SDK](https://dotnet.microsoft.com/download) or later
- Java Users API running (`cis-api`) — default: `http://localhost:8081`
- .NET CIS API running (`cis-api-dotnet`) — default: `http://localhost:5281`
- Shared MySQL database up (`db/docker-compose.yml`)

---

## Project structure

```
simulator/
├── Program.cs              <- simulation logic (concurrent flow per user)
├── config.json             <- all configurable parameters and endpoint definitions
├── cis-simulator.csproj    <- .NET 10 console project
└── README.md               <- this file
```

---

## How to run

```bash
# From the simulator/ directory
cd simulator/

# Build
dotnet build

# Run with default config.json
dotnet run

# Run with a custom config file
dotnet run -- path/to/my-config.json
```

### Expected output

```
╔══════════════════════════════════════════════════════════════╗
║           CIS API Traffic Simulator                          ║
╚══════════════════════════════════════════════════════════════╝
  Concurrent users : 5
  Ideas per user   : 3
  Votes per user   : 2
  Delay between    : 100 ms
  Config file      : /path/to/simulator/config.json
────────────────────────────────────────────────────────────────
[User-001] ✓ register             userId=abc-123
[User-001] ✓ login                JWT obtained
[User-001] ✓ createTopic          topicId=xyz-456
[User-001] ✓ createIdea[1]        ideaId=idea-001
...
── SUMMARY ─────────────────────────────────────────────────────
  Elapsed          : 1.84s
  Users simulated  : 5
  Errors logged    : 0
  Result           : ALL FLOWS COMPLETED SUCCESSFULLY
```

Errors are printed in **red** with the step name, HTTP status code, and response body. The process exits with code `1` if any errors occurred, making it usable in CI pipelines.

---

## Simulated flow per user

Each simulated user runs these steps sequentially. Steps are independent across users (all users run concurrently via `Task.WhenAll`).

| Step | Method | Endpoint | Auth |
|------|--------|----------|:----:|
| 1. Register | `POST` | `/users` (Java API) | No |
| 2. Login | `POST` | `/auth/login` (Java API) | No |
| 3. Create Topic | `POST` | `/topics` (CIS API) | JWT |
| 4. Create Ideas × n | `POST` | `/ideas` (CIS API) | JWT |
| 5. Vote on ideas × n | `POST` | `/votes` (CIS API) | JWT |
| 6. Unvote (1 vote) | `DELETE` | `/votes/{id}` (CIS API) | JWT |

Steps 1 and 2 are **mandatory** — if either fails, the user's flow stops and the error is logged. Steps 3–6 continue even on partial failures to maximize coverage data.

---

## Configuration reference — `config.json`

### `simulation`

Controls the load profile of the simulation.

```json
"simulation": {
  "concurrentUsers": 5,
  "ideasPerUser": 3,
  "votesPerUser": 2,
  "delayBetweenRequestsMs": 100
}
```

| Field | Type | Description |
|-------|------|-------------|
| `concurrentUsers` | int | Number of users running in parallel |
| `ideasPerUser` | int | Ideas each user creates under their topic |
| `votesPerUser` | int | How many of those ideas each user votes on (≤ `ideasPerUser`) |
| `delayBetweenRequestsMs` | int | Pause between each request per user. Set to `0` for maximum load |

### `apis`

Base URLs for each API. Use the key name in endpoint definitions to route requests.

```json
"apis": {
  "usersApi": "http://localhost:8081/api/v1",
  "cisApi": "http://localhost:5281/api/v1"
}
```

Add as many API entries as needed. Any key defined here can be referenced in `endpoints[*].api`.

### `endpoints`

Each entry defines one HTTP call the simulator can make. The flow steps (`register`, `login`, `createTopic`, `createIdea`, `castVote`, `deleteVote`) must match these keys exactly.

```json
"createTopic": {
  "description": "Create a topic in the .NET CIS API (requires Bearer token)",
  "api": "cisApi",
  "method": "POST",
  "path": "/topics",
  "requiresAuth": true,
  "body": {
    "title": { "type": "sentence", "prefix": "Topic" },
    "description": { "type": "sentence", "prefix": "About" },
    "userId": { "type": "context",  "key": "userId" }
  },
  "expectId": "id"
}
```

| Field | Description |
|-------|-------------|
| `api` | Must match a key in the `apis` section |
| `method` | HTTP method: `GET`, `POST`, `PUT`, `DELETE` |
| `path` | Path relative to the API base URL. Supports `{placeholders}` |
| `requiresAuth` | If `true`, the Bearer token is attached automatically |
| `body` | Map of JSON fields → data type definitions |
| `pathParams` | Map of `{placeholder}` names → data type definitions |
| `expectId` | (Informational) name of the ID field expected in the response |
| `expectToken` | (Informational) name of the token field expected in the response |
| `description` | Free-text description, not used by the simulator |

### Field types

Used inside `body` and `pathParams` to define how each value is generated.

| Type | Example output | Notes |
|------|---------------|-------|
| `name` | `SimUser_a1b2c3d4` | Random display name |
| `username` | `sim_a1b2c3d4` | Random login handle |
| `base64password` | `UGFzc18xMjM0` | Base64-encoded random password |
| `sentence` | `Topic a1b2c3` | Short phrase; use `"prefix"` to set the leading word |
| `uuid` | `550e8400-e29b-...` | Random UUID v4 |
| `context` | *(varies)* | Reads a value stored in the user's session. Use `"key"` to specify which one (e.g. `"userId"`, `"topicId"`, `"voteId"`) |

**Context keys available across the flow:**

| Key | Set after step | Description |
|-----|---------------|-------------|
| `login` | Start | Generated login handle |
| `password` | Start | Base64-encoded password |
| `rawPassword` | Start | Plain-text password (for login body) |
| `userId` | Register | ID returned by `/users` |
| `token` | Login | JWT returned by `/auth/login` |
| `topicId` | Create Topic | ID returned by `/topics` |
| `ideaId` | Create Idea | ID of the current idea being voted on |
| `voteId` | Cast Vote | ID returned by `/votes` |

---

## Adding or changing an endpoint

1. Open `config.json`
2. Add or edit an entry under `endpoints`
3. Point it to the right `api`, set the `method` and `path`
4. Define each body field with its `type` (and `key` or `prefix` as needed)
5. Run `dotnet run` — no recompile needed if the flow steps map to existing keys

To change a URL or port, update the `apis` section only.

---

## Extending the flow

The simulation flow is defined in `Program.cs` in the `SimulateUser` function. Each step calls `Call("endpointKey", ctx, rnd, jwt?)`. To add a new step:

1. Add the endpoint definition to `config.json`
2. Add a new `Call(...)` block in `SimulateUser` following the same pattern as the existing steps

---

## CI usage

The simulator exits with code `0` on full success and `1` if any request returned an error. This makes it directly usable as a CI gate:

```bash
dotnet run && echo "Simulation passed" || echo "Simulation failed — check output"
```

---

_This simulator is part of the Jala University Capstone project. Do not distribute externally._
