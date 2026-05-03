# Learning Backend: Quote of the Day API

**Who this is for:** Android engineer (strong Kotlin/Java, zero backend/deployment experience) building their first production-grade Spring Boot API.

**Goal:** Ship a real, publicly accessible REST API — not just get it running locally.

---

## The App: "Quote of the Day" API

### Endpoints

| Method | Path | Auth? | What it does |
|--------|------|-------|--------------|
| GET | `/api/quotes/today` | No | Returns today's quote (same for everyone, all day) |
| GET | `/api/quotes` | No | Paginated list of all quotes |
| GET | `/api/quotes/{id}` | No | Fetch a specific quote |
| POST | `/api/quotes` | Yes | Add a new quote |
| PUT | `/api/quotes/{id}` | Yes | Edit a quote |
| DELETE | `/api/quotes/{id}` | Yes | Delete a quote |
| POST | `/api/auth/register` | No | Create an account |
| POST | `/api/auth/login` | No | Get a JWT token |
| POST | `/api/quotes/{id}/like` | Yes | Like a quote |

---

## Your Actual Stack

> Already scaffolded — do not change these.

- **Language:** Kotlin (you know this from Android — it works great here)
- **Framework:** Spring Boot 4.0.6
- **Build:** Gradle Kotlin DSL (`build.gradle.kts`) — already configured
- **Database:** MongoDB via Spring Data MongoDB
- **Auth:** Spring Security + JWT (need to add `jjwt` library)
- **Validation:** Jakarta Bean Validation (already in dependencies)
- **API docs:** springdoc-openapi 3.0.2 (already in dependencies — Swagger UI at `/swagger-ui/index.html`)
- **Testing:** JUnit 5 + Testcontainers (add Testcontainers for real MongoDB in tests)
- **Containerization:** Docker + Docker Compose
- **Deployment:** Render (free tier) → Hetzner VPS (€4/month, later)
- **CI/CD:** GitHub Actions

### Kotlin vs Java here
Since you already know Kotlin from Android, **stick with it**. You get data classes (no Lombok needed — remove it from `build.gradle.kts`), null safety, extension functions, and coroutines if you ever want them. Spring Boot 4 has excellent Kotlin support. Every tutorial you find in Java translates 1:1.

---

## Dependencies to Add

Add these to `build.gradle.kts` before Phase 3:

```kotlin
// JWT
implementation("io.jsonwebtoken:jjwt-api:0.12.6")
runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

// Testcontainers (for integration tests)
testImplementation("org.testcontainers:mongodb:1.20.4")
testImplementation("org.testcontainers:junit-jupiter:1.20.4")
```

Remove Lombok — it's a Java annotation processor and doesn't do anything useful in Kotlin:

```kotlin
// Remove these:
// compileOnly("org.projectlombok:lombok")
// annotationProcessor("org.projectlombok:lombok")
// testCompileOnly("org.projectlombok:lombok")
// testAnnotationProcessor("org.projectlombok:lombok")
```

---

## Phase-by-Phase Roadmap

### Phase 1 — Verify the Skeleton (Day 1–2)
**Goal:** Understand what you have and get it running.

1. Add to `application.properties`:
   ```properties
   spring.data.mongodb.uri=mongodb://localhost:27017/quotesdb
   spring.application.name=quotes
   ```
2. Install MongoDB locally via Docker:
   ```bash
   docker run -d -p 27017:27017 --name mongo mongo:7
   ```
3. Run `./gradlew bootRun` — it should start on port 8080.
4. Hit `http://localhost:8080/actuator/health` — you should see `{"status":"UP"}`.

**What you're learning:** Spring Boot auto-configuration, how `application.properties` maps to beans, what an actuator is.

---

### Phase 2 — Domain Model + CRUD (Day 3–5)
**Goal:** Read and write quotes to MongoDB.

#### Package structure
```
com.tresluke.quotes/
  document/       ← MongoDB documents (like Room entities in Android)
  repository/     ← MongoRepository interfaces (like DAOs)
  service/        ← Business logic
  controller/     ← HTTP layer (like ViewModels expose data, controllers expose HTTP)
  dto/            ← Request/Response shapes (separate from DB model)
  exception/      ← Custom exceptions + global handler
```

#### The `Quote` document
```kotlin
// document/Quote.kt
@Document(collection = "quotes")
data class Quote(
    @Id val id: String? = null,
    @NotBlank val text: String,
    @NotBlank val author: String,
    val tags: List<String> = emptyList(),
    val createdAt: Instant = Instant.now(),
    val likes: Int = 0
)
```

**Android analogy:** `@Document` is like `@Entity` in Room. `MongoRepository` is like a `DAO` but you don't write SQL/queries — Spring generates them from the method name (e.g., `findByAuthor(author: String)`).

#### Repository
```kotlin
// repository/QuoteRepository.kt
interface QuoteRepository : MongoRepository<Quote, String>
```

That one line gives you `findAll()`, `findById()`, `save()`, `deleteById()`, and pagination — all generated.

#### Service + Controller
Build `QuoteService` with your business logic, then `QuoteController` with `@RestController` and `@RequestMapping("/api/quotes")`. Inject the service, not the repository, into the controller.

**Test with:** Bruno (free, like Postman) or `curl`.

---

### Phase 3 — Auth with JWT (Day 6–9)
**Goal:** Lock write endpoints behind a token.

This is the steepest learning curve. Take it slowly.

#### What JWT is (Android analogy)
Think of a JWT like a signed `SharedPreferences` token. When a user logs in, your server gives them a signed string. Every subsequent request includes that string in the `Authorization: Bearer <token>` header. Your server verifies the signature — no database lookup needed.

#### The moving parts
1. `User` document — stores `email`, `passwordHash` (never the plain password)
2. `AuthController` — `POST /api/auth/register` and `POST /api/auth/login`
3. `BCryptPasswordEncoder` — hashes passwords (Spring Security provides this)
4. `JwtService` — generates and validates tokens using `jjwt`
5. `JwtAuthFilter` — a `OncePerRequestFilter` that runs before every request, extracts the token, validates it, and sets the security context
6. `SecurityConfig` — tells Spring which endpoints are public, which require auth

#### Key config (`SecurityConfig.kt`)
```kotlin
@Configuration
@EnableWebSecurity
class SecurityConfig(private val jwtAuthFilter: JwtAuthFilter) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }                          // APIs are stateless, no CSRF needed
            .sessionManagement { it.sessionCreationPolicy(STATELESS) }
            .authorizeHttpRequests {
                it.requestMatchers("/api/auth/**").permitAll()
                it.requestMatchers(HttpMethod.GET, "/api/quotes/**").permitAll()
                it.anyRequest().authenticated()
            }
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
        return http.build()
    }
}
```

**Do not skip understanding each line here.** Spring Security is famously confusing when cargo-culted.

---

### Phase 4 — Today's Quote (Day 10)
**Goal:** Learn scheduled jobs.

Add `@EnableScheduling` to your main application class. Then:

```kotlin
@Service
class DailyQuoteService(private val quoteRepository: QuoteRepository) {

    private var todaysQuote: Quote? = null

    @Scheduled(cron = "0 0 0 * * *")  // midnight every day
    fun rotateDailyQuote() {
        val all = quoteRepository.findAll()
        todaysQuote = all.random()
    }

    fun getToday(): Quote = todaysQuote ?: quoteRepository.findAll().random()
}
```

**Android analogy:** `@Scheduled` is like a `WorkManager` periodic task, but managed by the Spring container instead of Android's OS.

---

### Phase 5 — Polish (Day 11–13)
**Goal:** Make it production-quality.

1. **Input validation** — add `@Valid` to controller parameters, `@NotBlank`/`@Size` to DTOs. Spring will return 400 automatically on violations.
2. **Global error handling** — one `@RestControllerAdvice` class that catches exceptions and returns consistent JSON error shapes.
3. **Pagination** — `MongoRepository` supports `Pageable` out of the box: `findAll(PageRequest.of(page, size))`.
4. **Swagger UI** — already configured via springdoc. Hit `/swagger-ui/index.html`. You get an interactive API explorer for free.
5. **Integration tests** — write at least 3: one happy-path GET, one auth flow, one validation failure. Use Testcontainers so tests run against real MongoDB.

---

### Phase 6 — Dockerize (Day 14–15)
**Goal:** Your app runs identically anywhere.

#### `Dockerfile` (multi-stage)
```dockerfile
# Build stage
FROM gradle:8.7-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle bootJar --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### `docker-compose.yml`
```yaml
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_DATA_MONGODB_URI: mongodb://mongo:27017/quotesdb
      JWT_SECRET: ${JWT_SECRET}
    depends_on:
      - mongo

  mongo:
    image: mongo:7
    volumes:
      - mongo-data:/data/db

volumes:
  mongo-data:
```

`docker compose up --build` → working stack, fresh machine.

**What you're learning:** Why containers exist (dependency isolation), multi-stage builds (smaller image), Docker Compose networking (services reach each other by service name, not `localhost`).

---

### Phase 7 — Deploy to Render (Day 16–18)
**Goal:** Public HTTPS URL. Hit it from your phone.

1. Create a free MongoDB Atlas cluster (M0 tier). Get your connection string.
2. Push your repo to GitHub.
3. Sign up at render.com → New Web Service → connect your repo.
4. Set environment variables:
   - `SPRING_DATA_MONGODB_URI` = your Atlas connection string
   - `JWT_SECRET` = a long random string
5. Render builds your Docker image and deploys it.
6. You have `https://quotes-xxxx.onrender.com`.

**What you're learning:** Environment-based config (never commit secrets), how PaaS platforms work, what a managed database is.

---

### Phase 8 — CI/CD with GitHub Actions (Day 19–20)
**Goal:** Every push to `main` runs tests and redeploys automatically.

```yaml
# .github/workflows/deploy.yml
name: CI/CD
on:
  push:
    branches: [main]

jobs:
  test-and-deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
      - name: Run tests
        run: ./gradlew test
      - name: Deploy to Render
        run: curl -X POST ${{ secrets.RENDER_DEPLOY_HOOK }}
```

**What you're learning:** What CI/CD means in practice, GitHub Actions syntax, deploy hooks.

---

### Phase 9 — Real Infrastructure (Week 4+)
**Goal:** Stop being a PaaS tourist.

1. Rent a **Hetzner CX22** VPS (~€4/month). SSH in.
2. Install Docker on it.
3. Install **Caddy** as a reverse proxy — it handles HTTPS automatically.
4. Copy your `docker-compose.yml` up, set env vars, `docker compose up -d`.
5. Your domain → Caddy → your app container.
6. Automate deploys: GitHub Actions SSHes into the server and runs `docker compose pull && docker compose up -d`.

**What you're learning:** Linux basics, reverse proxies, SSL/TLS in practice, process management, what a VPS actually is vs. a PaaS.

---

## Things to Add as You Go

- **Structured logging** — configure Logback to output JSON (makes logs searchable in production)
- **`application-{profile}.yml`** — separate config files for `local`, `test`, `prod`; Spring activates the right one via `SPRING_PROFILES_ACTIVE`
- **Rate limiting** — Bucket4j library; prevents abuse of your API
- **Caffeine cache** — cache today's quote in memory instead of hitting MongoDB every request
- **Observability** — Grafana Cloud free tier + Spring Boot Actuator metrics

---

## Things NOT to Add (Learning Project Killers)

- Kotlin coroutines / reactive WebFlux — stick to the standard servlet stack
- Kubernetes — massively premature
- Message queues (Kafka, RabbitMQ) — not needed here
- Microservices — one service is the right answer
- Multiple databases — MongoDB is enough

---

## Key Android → Spring Boot Mental Model Shifts

| Android concept | Spring Boot equivalent |
|----------------|----------------------|
| `ViewModel` | `@Service` (business logic layer) |
| `Repository` (MVVM) | `@Repository` / `MongoRepository` |
| `Room @Entity` | `@Document` |
| `Room @Dao` | `MongoRepository` interface |
| `WorkManager` periodic task | `@Scheduled` |
| `Hilt @Inject` | Spring's `@Autowired` / constructor injection |
| `okhttp interceptor` | Spring `Filter` / `HandlerInterceptor` |
| `SharedPreferences` (signed) | JWT token |
| `BuildConfig` fields | `application.properties` / env vars |

---

## Current Project State

- Spring Boot 4.0.6, Kotlin, Gradle KTS — already scaffolded
- MongoDB, Security, Validation, springdoc-openapi already in dependencies
- **Next step:** Add JWT library, remove Lombok, add MongoDB config, verify it starts
