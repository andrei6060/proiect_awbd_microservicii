# Clinic Management — Spring Cloud Microservices

A clinic management platform built as a **Spring Boot 3.4.3 / Spring Cloud 2024.0.3** microservices
system. It demonstrates the full cloud-native stack: service discovery, an API gateway, centralized
configuration, load-balanced inter-service calls, fault tolerance, distributed tracing, metrics,
Redis caching, JWT security, and a Redis-backed rate limiter.

Domain: doctors and patients manage **appointments**, **medications**, **patient files**, and
**reviews**, behind stateless JWT authentication with `USER` and `DOCTOR` roles.

---

## Table of contents

1. [Architecture](#architecture)
2. [Modules & ports](#modules--ports)
3. [Technology stack](#technology-stack)
4. [Cross-cutting features](#cross-cutting-features)
5. [Prerequisites](#prerequisites)
6. [How to run (start order)](#how-to-run-start-order)
7. [Getting a JWT](#getting-a-jwt)
8. [API reference](#api-reference)
9. [Configuration reference](#configuration-reference)
10. [Observability & operations](#observability--operations)
11. [Testing](#testing)
12. [Project layout](#project-layout)
13. [Troubleshooting](#troubleshooting)

---

## Architecture

```
                                  ┌──────────────────┐
        Angular SPA (4200)  ──────▶   API Gateway     │  :8080  (reactive / Netty)
                                  │  routing, CORS,    │
                                  │  rate limit (Redis)│
                                  │  request logging   │
                                  └─────────┬──────────┘
                                            │ lb://  (Eureka + load balancing)
            ┌───────────────────────────────┼───────────────────────────────┐
            ▼                               ▼                                ▼
    ┌───────────────┐              ┌────────────────┐               ┌────────────────┐
    │ auth-service  │  :8087       │business-service│  :8085        │   db-service   │  :8086
    │ register/login│              │ clinical API   │               │ data layer +   │
    │ JWT issuing   │              │ + Redis cache  │               │ internal CRUD  │
    └───────┬───────┘              └───────┬────────┘               └───────┬────────┘
            │   RestTemplate (@LoadBalanced, Resilience4j) → http://db-service/...      │
            └───────────────────────────────┴────────────────────────────────┘
                                            │
                          ┌─────────────────┼──────────────────┐
                          ▼                 ▼                  ▼
                  ┌──────────────┐  ┌──────────────┐   ┌──────────────┐
                  │ Eureka :8761 │  │Config  :8888 │   │ PostgreSQL   │  :5432
                  │ discovery    │  │ server       │   │ (shared db)  │
                  └──────────────┘  └──────────────┘   └──────────────┘

   Infra (Docker):  Prometheus :9090   Grafana :3000   Zipkin :9411   Redis :6379   MailDev :1025/1080
```

**Request flow:** clients hit the **gateway (8080)**, which resolves the target service through **Eureka**
by name (`lb://business-service`, …) and load-balances across instances. Each service pulls shared
settings from the **config server** at startup, registers with **Eureka**, exposes **Actuator/Prometheus**
metrics, sends **Zipkin** traces, and validates the **JWT** itself. `business-service` caches hot reads in
**Redis** and protects its calls to `db-service` with a **Resilience4j** circuit breaker + retry + fallback.

---

## Modules & ports

| Module | Port | Context path | Role |
|---|---|---|---|
| `config-server` | 8888 | – | Spring Cloud Config Server (native filesystem backend) |
| `eureka-server` | 8761 | – | Netflix Eureka service registry / dashboard |
| `api-gateway` | 8080 | – (routes preserve `/api/v1`) | Spring Cloud Gateway (reactive) — single entry point |
| `auth-service` | 8087 | `/api/v1` | Registration, account activation, JWT issuing |
| `business-service` | 8085 | `/api/v1` | Client-facing clinical API (appointments, medications, reviews, files, paginated lists, UI) |
| `db-service` | 8086 | `/api/v1` | Data/persistence layer + internal `/save` `/get` `/delete` endpoints |

Backing services (run in Docker): **PostgreSQL** 5432, **Redis** 6379, **Prometheus** 9090,
**Grafana** 3000, **Zipkin** 9411, **MailDev** SMTP 1025 / UI 1080.

All apps are **Java 17**, group `com.clinic`, base Java package `com.clinic.clinic`
(`com.clinic.gateway`, `com.clinic.config`, `com.clinic.eureka` for the infra modules), each a
standalone Maven module with its own wrapper (`mvnw` / `mvnw.cmd`). There is no parent aggregator POM.

---

## Technology stack

- **Spring Boot** 3.4.3, **Spring Cloud** 2024.0.3 (BOM-managed — no pinned Spring Cloud versions)
- **Spring Cloud Netflix Eureka** — service discovery, client-side load balancing (`@LoadBalanced` RestTemplate)
- **Spring Cloud Gateway** (reactive/WebFlux/Netty) — routing, CORS, rate limiting, request logging
- **Spring Cloud Config Server** — centralized configuration (native backend, live refresh via `@RefreshScope`)
- **Spring Cloud Circuit Breaker + Resilience4j** — circuit breaker, retry, time limiter, fallback
- **Spring Security** + **JWT** (jjwt 0.11.5), BCrypt, method-level `@PreAuthorize`
- **Spring Data JPA** + **PostgreSQL** (H2 for tests)
- **Spring Data Redis** + **Spring Cache** — Redis as caching layer **and** NoSQL key-value store
- **Actuator + Micrometer Prometheus** — metrics; **Micrometer Tracing + Zipkin (Brave)** — distributed tracing
- **SLF4J + Logback** — structured logging with a dedicated error log + AOP method logging
- **JUnit 5 + Mockito + Spring Security Test + JaCoCo** — unit/integration tests + coverage
- **springdoc-openapi** — OpenAPI/Swagger UI

---

## Cross-cutting features

| Capability | Where | Notes |
|---|---|---|
| **Pagination & sorting** | `business-service` `/…/page` endpoints | `PageResponse<T>` wrapper, validated sort fields, configurable page size, vanilla-JS UI at `/api/v1/ui/records` |
| **Centralized logging** | all 3 services | `logback-spring.xml`, separate `*-error.log`, dev=DEBUG/test=INFO, `LoggingAspect` (AOP) on the service layer |
| **Consistent error model** | all 3 services | `@RestControllerAdvice` → uniform `ExceptionResponse` JSON (`status`, `error`, `path`, `timestamp`); semantically correct HTTP codes; JSON 401/403 from security |
| **Security** | all 3 services | stateless JWT, `@EnableMethodSecurity`, plain authorities `USER`/`DOCTOR` (always `hasAuthority`), CSRF disabled (token API) |
| **Service discovery** | Eureka | services register by `spring.application.name` |
| **Load balancing** | `@LoadBalanced` RestTemplate | `http://db-service/...` resolved + round-robined via Eureka |
| **API gateway** | `api-gateway` | explicit routes per prefix, discovery-locator fallback, gateway CORS for `http://localhost:4200`, `X-Request-Id`, rate limiting |
| **Centralized config** | `config-server` | `config-repo/` shared + per-service files; `optional:` import so services start if the server is down |
| **Fault tolerance** | `business-service`, `auth-service` | Resilience4j circuit breaker `db-service` (retry inside, breaker outside), fallbacks (empty list for reads, 503 for writes) |
| **Caching / NoSQL** | `business-service` | Redis cache on medication reads, evicted on writes; graceful degrade if Redis down |
| **Monitoring** | all 6 apps | `/actuator/prometheus`, Grafana dashboards |
| **Tracing** | 3 services + gateway | Zipkin spans across hops |

---

## Prerequisites

- **JDK 17**
- **Docker Desktop**
- Windows PowerShell examples below (adapt for bash). Each module has a Maven wrapper, or run from your IDE.

Start the backing containers once:

```powershell
# PostgreSQL — matches application-dev.yml (db=postgres, user=username, pass=password)
docker run -d --name clinic-postgres -e POSTGRES_USER=username -e POSTGRES_PASSWORD=password -e POSTGRES_DB=postgres -p 5432:5432 postgres:16

# MailDev — SMTP sink on 1025, web UI on 1080 (activation emails land here)
docker run -d --name clinic-maildev -p 1025:1025 -p 1080:1080 maildev/maildev

# Monitoring + cache stack (Prometheus, Grafana, Zipkin, Redis)
cd infra
docker compose up -d
```

> Redis can also be run standalone: `docker run -d --name clinic-redis -p 6379:6379 redis:7-alpine`

---

## How to run (start order)

Start each Spring app in its own terminal, **in this order** (config + discovery first, gateway last):

```powershell
cd config-server   ; .\mvnw.cmd spring-boot:run     # :8888  (run from this folder — uses file:./config-repo)
cd eureka-server   ; .\mvnw.cmd spring-boot:run     # :8761
cd db-service      ; .\mvnw.cmd spring-boot:run     # :8086
cd business-service; .\mvnw.cmd spring-boot:run     # :8085
cd auth-service    ; .\mvnw.cmd spring-boot:run     # :8087
cd api-gateway     ; .\mvnw.cmd spring-boot:run     # :8080
```

Verify everything registered at the Eureka dashboard: **http://localhost:8761**
(you should see `API-GATEWAY`, `AUTH-SERVICE`, `BUSINESS-SERVICE`, `CONFIG-SERVER`, `DB-SERVICE`, all `UP`).

**Running a second instance** (to demo load balancing):
```powershell
cd db-service; .\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=8088"
```

**Containerization knobs** — every external location is overridable via env vars:
`CONFIG_SERVER_URL`, `EUREKA_URL`, `REDIS_HOST`/`REDIS_PORT`, `ZIPKIN_URL`,
`CACHE_DEFAULT_TTL_MINUTES`/`CACHE_LIST_TTL_MINUTES`, `GATEWAY_RATE_LIMIT_RPM`.

---

## Getting a JWT

Endpoints are role-protected, so register → activate → authenticate to obtain a token. Through the gateway:

```powershell
# 1) Register a DOCTOR (public). Password >= 10 chars; specialization is an enum:
#    ORL | GINECOLOGIE | CARDIOLOGIE | ORTOPEDIE | ONCOLOGIE | PEDIATRIE
$reg = @{ firstname="Greg"; lastname="House"; email="doc@clinic.com"; password="password123"; specialization="CARDIOLOGIE" } | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/v1/auth/register -ContentType application/json -Body $reg

# 2) Open http://localhost:1080 (MailDev), copy the 6-digit activation code from the email, then:
Invoke-RestMethod -Method Get -Uri "http://localhost:8080/api/v1/auth/activateUser?token=123456"

# 3) Authenticate -> JWT
$body = @{ email="doc@clinic.com"; password="password123" } | ConvertTo-Json
$resp = Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/v1/auth/authenticate -ContentType application/json -Body $body
$H = @{ Authorization = "Bearer $($resp.token)" }     # reuse $H for protected calls
```

Register **without** `specialization` to create a `USER` (patient) account.

---

## API reference

All client calls go through the gateway at `http://localhost:8080` and keep the `/api/v1` prefix
(services can also be called directly on their own ports). Authorities in **bold**.

### auth-service — `/api/v1/auth/**`
| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/auth/register` | public | Register a user (no specialization) or doctor (with specialization) |
| POST | `/auth/authenticate` | public | Login → `{ "token": "<jwt>" }` |
| GET | `/auth/activateUser?token=` | public | Activate an account with the emailed code |
| DELETE | `/auth/deleteUser?token=` | **DOCTOR** | Delete a user account |

### business-service
**Appointments** `/api/v1/appointment/**`
| Method | Path | Auth |
|---|---|---|
| POST | `/appointment/addAppointment` | **USER** |
| PUT | `/appointment/acceptAppointment` | **DOCTOR** |
| GET | `/appointment/getAllAppointments` | **DOCTOR** |
| GET | `/appointment/getAllAppointments/page?page&size&sortBy&direction` | **DOCTOR** (sort: `date`,`neededSpecialization`) |
| GET | `/appointment/getAvailableAppointments` | **DOCTOR** |
| GET | `/appointment/getOwnAppointments` | **DOCTOR** |
| GET | `/appointment/getMyAppointments` | **USER** |
| DELETE | `/appointment/notGoodDoctor` | **DOCTOR/USER** |
| DELETE | `/appointment/deleteAppointment` | **DOCTOR/USER** |

**Medications** `/api/v1/medication/**` — all **DOCTOR**
| Method | Path | Notes |
|---|---|---|
| POST | `/medication/addNewMedication` | evicts cache |
| PUT | `/medication/supplyMedication` | evicts cache |
| DELETE | `/medication/discontinueMedication` | evicts cache |
| PUT | `/medication/reactivateMedication` | evicts cache |
| PUT | `/medication/giveMedication` | evicts cache |
| GET | `/medication/getMedicine` | **cached** (`medicationByName`) |
| GET | `/medication/getAllActiveMedicine` | **cached** (`medicationsActive`) |
| GET | `/medication/getAllAvailableMedicine` | **cached** (`medicationsAvailable`) |
| GET | `/medication/getAllMedications/page?page&size&sortBy&direction` | not cached (sort: `name`,`quantity`) |

**Reviews** `/api/v1/review/**`
| Method | Path | Auth |
|---|---|---|
| POST | `/review/addReview` | **USER** |
| GET | `/review/getOwnReviews` | **USER** |
| GET | `/review/getOwnReviewsDoctor` | **DOCTOR** |
| GET | `/review/getAllReviews` | **DOCTOR** |
| GET | `/review/getAllReviews/page?page&size&sortBy&direction` | **DOCTOR** (sort: `rating`,`aspect`) |
| DELETE | `/review/deleteReview` | **USER** |

**Files** `/api/v1/file/**`: `addNewFile` (DOCTOR), `getPatientFile` (DOCTOR), `getOwnFile` (USER),
`getAllFiles` (DOCTOR), `addBloodType` (DOCTOR).
**UI:** `GET /api/v1/ui/records` (public HTML page; supply the JWT in the page).

### db-service — internal `/api/v1/{save,get,delete}/**`
Called service-to-service (permit-listed). Not intended for direct external use.

### Pagination response shape
```json
{
  "content": [ ... ],
  "page": 0, "size": 2, "totalElements": 3, "totalPages": 2,
  "first": true, "last": false, "numberOfElements": 2,
  "sortBy": "name", "direction": "asc"
}
```

### Error response shape (every handled error)
```json
{ "status": 404, "error": "Medication 'X' not found",
  "path": "/api/v1/medication/getMedicine", "timestamp": "2026-06-17T10:15:30.12+03:00" }
```
HTTP status mapping: not-found → **404**, role denial / not-a-doctor → **403**, business conflicts
(already accepted/discontinued, not enough stock) → **409**, validation/bad enum → **400**,
bad credentials / locked / disabled → **401**, downstream service down → **503**, unexpected → **500**
(generic message, no stack trace leaked).

---

## Configuration reference

### Centralized (served by config-server from `config-server/config-repo/`)
- `application.yml` (shared by all): Eureka URL, `application.pagination.*`, demo props `app.message` / `app.features.beta-enabled`.
- `business-service.yml`, `auth-service.yml`, `db-service.yml`: per-service overrides (e.g. `app.message`).

Clients import it with `spring.config.import: "optional:configserver:${CONFIG_SERVER_URL:http://localhost:8888}"`
(modern Spring Boot 3 mechanism — **no** `bootstrap.yml`). `optional:` means a service still starts if the
config server is down.

### Left local in each service (`application-dev.yml`)
Startup-critical / secret values stay local: datasource URL/user/password, JPA, mail credentials,
`application.security.jwt.secret-key`, and `server.port`. Resilience4j config is also kept local so fault
tolerance works even if the config server is down.

### Live refresh demo
`business-service` exposes `app.message` via a `@RefreshScope` controller:
```powershell
Invoke-RestMethod http://localhost:8085/api/v1/config/message       # current value
# edit config-server/config-repo/business-service.yml -> change app.message, save
Invoke-RestMethod -Method Post http://localhost:8085/api/v1/actuator/refresh   # -> ["app.message"]
Invoke-RestMethod http://localhost:8085/api/v1/config/message       # new value, no restart
```

### Caching (Redis)
- Keys: `StringRedisSerializer`; values: `GenericJackson2JsonRedisSerializer` (readable JSON, type-safe DTOs).
- TTL: by-id 10 min (`app.cache.default-ttl-minutes`), lists 2 min (`app.cache.list-ttl-minutes`).
- Keys look like `medicationsActive::all`, `medicationByName::Paracetamol`.
- **Redis down → graceful degrade**: a `CacheErrorHandler` logs a warning and reads fall through to the DB.
- **NoSQL note:** Redis serves a dual role here — caching layer **and** NoSQL key-value store. A document
  store (MongoDB) was intentionally **not** added; it would be the candidate for audit logs / document
  attachments / denormalized read-models if needed later.

### Resilience4j (`db-service` instance)
Circuit breaker: COUNT window 10, min 5 calls, 50% failure threshold, 10s open, 3 half-open calls.
Retry: 3 attempts, 300 ms backoff, on connection/5xx errors only. Timeout: RestTemplate read 3 s/attempt
(primary) + 12 s time-limiter (backstop). **Order:** retry inside, circuit breaker outside.

### Gateway rate limiting
Redis-backed `RequestRateLimiter` (default filter, keyed by client IP): `replenishRate=5/s`,
`burstCapacity=10` → burst > 10 req/s yields **429**. State lives in Redis; fails **open** if Redis is down.

---

## Observability & operations

| What | URL / command |
|---|---|
| Eureka dashboard | http://localhost:8761 |
| Config server (raw) | http://localhost:8888/business-service/default |
| Prometheus targets | http://localhost:9090/targets (all 6 jobs `UP`) |
| Grafana | http://localhost:3000 — `admin` / `admin` → dashboard "Clinic Microservices – JVM & HTTP" |
| Zipkin | http://localhost:9411 (search a recent trace → spans across gateway → business → db) |
| MailDev | http://localhost:1080 |
| Health (business) | http://localhost:8085/api/v1/actuator/health (shows DB, disk, **circuit breaker state**, liveness/readiness) |
| Prometheus scrape | services: `/api/v1/actuator/prometheus`; infra apps: `/actuator/prometheus` |
| Logs | each service's `logs/<name>.log` and `logs/<name>-error.log` |

Key metrics: `resilience4j_circuitbreaker_state{name="db-service"}`,
`http_server_requests_seconds_*`, `jvm_memory_used_bytes`, `process_cpu_usage`
(all tagged with `application=<service>`).

---

## Testing

```powershell
cd business-service; .\mvnw.cmd test     # 49 tests
cd db-service      ; .\mvnw.cmd test     # 70 tests  (service-layer coverage ~95%)
cd auth-service    ; .\mvnw.cmd test     # 13 tests
```
- Unit tests (JUnit 5 + Mockito) isolate the service layer; integration tests use `@SpringBootTest` +
  `@AutoConfigureMockMvc` against **H2** under the `test` profile, exercising security with `@WithMockUser`.
- Coverage report after `test`: open `<module>/target/site/jacoco/index.html`.
- Tests are self-contained: Eureka, config client, and Redis cache are disabled under the `test` profile,
  so no external infrastructure is required.

---

## Project layout

```
proiect_awbd_microservicii/
├─ config-server/        # Spring Cloud Config Server (+ config-repo/ central files)
├─ eureka-server/        # Eureka registry
├─ api-gateway/          # Spring Cloud Gateway (filters: logging+X-Request-Id, Redis rate limiter)
├─ auth-service/         # registration / activation / JWT
├─ business-service/     # client-facing clinical API (cache, pagination, UI, resilience)
├─ db-service/           # persistence + internal CRUD endpoints
└─ infra/
   ├─ docker-compose.yml             # Prometheus, Grafana, Zipkin, Redis
   ├─ prometheus/prometheus.yml      # scrape jobs (correct path per app)
   └─ grafana/provisioning/          # datasource + dashboard auto-provisioning
```

Each business service follows the same package layout under `com.clinic.clinic`:
`Controller/`, `Service/`, `Entity/`, `JpaRepo/`, `security/`, `global/` (exceptions + handler),
`config/`, `aspect/` (AOP logging), `resilience/` (circuit-breaker client), `email/`, `role/`.

---

## Troubleshooting

- **A service didn't register / 503 from gateway** — start `config-server` and `eureka-server` first; give services ~30 s to register, then restart the late one.
- **Can't activate the account** — ensure MailDev (1025/1080) is running before registering; the activation code arrives in its UI.
- **401 on a protected call** — missing/expired JWT; re-run the authenticate step.
- **403 on a DOCTOR endpoint** — you used a `USER` token; register an account *with* a specialization.
- **Cache/rate-limit not working** — ensure Redis (6379) is up; the cache degrades gracefully (still works, just uncached) but the gateway limiter needs Redis for state.
- **`mvnw.cmd` issues** — run the module's `*Application` main class from your IDE instead; behavior is identical.
- **Ports busy** — default ports: 8080/8085/8086/8087/8761/8888 (apps) and 5432/6379/9090/3000/9411/1025/1080 (infra).
