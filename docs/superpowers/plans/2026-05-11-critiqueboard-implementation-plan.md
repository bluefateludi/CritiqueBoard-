# CRITIQUEBOARD Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the first API-first CRITIQUEBOARD backend that accepts review requests, persists them, runs asynchronous multi-agent review orchestration, and stores final reports with token accounting.

**Architecture:** Use a standard Spring Boot Maven project with `src/main/java`, `src/main/resources`, and `src/test/java`. Spring Boot owns REST APIs, persistence, RabbitMQ, SSE, and application configuration; LangGraph4j owns the review state graph; LangChain4j owns DeepSeek chat calls, structured output, embeddings, and vector retrieval.

**Tech Stack:** Java 21, Spring Boot 3.5.x, Maven, PostgreSQL + pgvector, RabbitMQ, Flyway, JPA, LangChain4j, LangGraph4j, JUnit 5, Testcontainers.

---

## File Structure

Create or modify these paths:

- `pom.xml` - Maven build, dependency management, plugins, Java version.
- `docker-compose.yml` - local PostgreSQL/pgvector and RabbitMQ services.
- `db/001_init_critiqueboard_pgvector.sql` - source SQL schema reference already created.
- `src/main/resources/application.yml` - Spring profiles and default app configuration.
- `src/main/resources/db/migration/V1__init_critiqueboard_pgvector.sql` - Flyway migration copied from `db/001_init_critiqueboard_pgvector.sql`.
- `src/main/java/com/bluefateludi/critiqueboard/CritiqueBoardApplication.java` - Spring Boot entry point.
- `src/main/java/com/bluefateludi/critiqueboard/review/api/ReviewController.java` - REST endpoints for task creation and query.
- `src/main/java/com/bluefateludi/critiqueboard/review/api/dto/CreateReviewRequest.java` - create-review request DTO.
- `src/main/java/com/bluefateludi/critiqueboard/review/api/dto/CreateReviewResponse.java` - create-review response DTO.
- `src/main/java/com/bluefateludi/critiqueboard/review/domain/ReviewTask.java` - JPA entity for review tasks.
- `src/main/java/com/bluefateludi/critiqueboard/review/domain/ReviewTaskStatus.java` - status enum.
- `src/main/java/com/bluefateludi/critiqueboard/review/domain/AgentRole.java` - Agent role enum.
- `src/main/java/com/bluefateludi/critiqueboard/review/repository/ReviewTaskRepository.java` - task repository.
- `src/main/java/com/bluefateludi/critiqueboard/review/service/ReviewTaskService.java` - create/query task use cases.
- `src/main/java/com/bluefateludi/critiqueboard/review/service/ReviewTaskPublisher.java` - queue publishing port.
- `src/main/java/com/bluefateludi/critiqueboard/review/messaging/RabbitReviewTaskPublisher.java` - RabbitMQ publisher implementation.
- `src/main/java/com/bluefateludi/critiqueboard/review/messaging/ReviewQueueConfig.java` - exchange, queue, binding config.
- `src/main/java/com/bluefateludi/critiqueboard/review/messaging/ReviewTaskMessage.java` - queue message payload.
- `src/main/java/com/bluefateludi/critiqueboard/review/agent/CritiqueResult.java` - structured Specialist result DTO.
- `src/main/java/com/bluefateludi/critiqueboard/review/agent/ReviewGraphRunner.java` - orchestration port for LangGraph4j.
- `src/main/java/com/bluefateludi/critiqueboard/review/agent/ReviewWorker.java` - RabbitMQ consumer that runs the graph.
- `src/main/java/com/bluefateludi/critiqueboard/review/progress/ReviewEventService.java` - stores progress events and pushes SSE updates.
- `src/main/java/com/bluefateludi/critiqueboard/review/progress/ReviewSseController.java` - SSE endpoint.
- `src/test/java/com/bluefateludi/critiqueboard/review/service/ReviewTaskServiceTest.java` - service tests.
- `src/test/java/com/bluefateludi/critiqueboard/review/api/ReviewControllerTest.java` - API slice tests.
- `src/test/java/com/bluefateludi/critiqueboard/review/agent/CritiqueResultTest.java` - structured result validation tests.

## Chunk 1: Project Skeleton And Local Infrastructure

### Task 1: Maven Spring Boot Skeleton

**Files:**
- Create: `pom.xml`
- Create: `src/main/java/com/bluefateludi/critiqueboard/CritiqueBoardApplication.java`
- Create: `src/main/resources/application.yml`
- Create: `src/test/java/com/bluefateludi/critiqueboard/CritiqueBoardApplicationTests.java`

- [ ] **Step 1: Create Maven build file**

Use Java 21, Spring Boot 3.5.x, and dependency management for Spring Boot.

Include starters:

- `spring-boot-starter-web`
- `spring-boot-starter-validation`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-amqp`
- `flyway-core`
- `flyway-database-postgresql`
- `postgresql`
- `spring-boot-starter-test`

- [ ] **Step 2: Create the Spring Boot entry point**

Class: `com.bluefateludi.critiqueboard.CritiqueBoardApplication`

- [ ] **Step 3: Add minimal application configuration**

Configure app name, datasource placeholders, JPA validation, Flyway, RabbitMQ, and DeepSeek placeholders under `critiqueboard.ai`.

- [ ] **Step 4: Add context-load test**

Create a basic `@SpringBootTest` that verifies the app context can start.

- [ ] **Step 5: Run verification**

Run: `mvn test`

Expected: app context test passes.

- [ ] **Step 6: Commit**

```bash
git add pom.xml src/main/java src/main/resources src/test/java
git commit -m "chore: add spring boot project skeleton"
```

### Task 2: Docker Compose And Flyway Migration

**Files:**
- Create: `docker-compose.yml`
- Create: `src/main/resources/db/migration/V1__init_critiqueboard_pgvector.sql`
- Modify: `src/main/resources/application.yml`

- [ ] **Step 1: Add Docker Compose**

Use:

- PostgreSQL image with pgvector support.
- RabbitMQ management image.

Expose:

- PostgreSQL: `5432`
- RabbitMQ: `5672`
- RabbitMQ management: `15672`

- [ ] **Step 2: Copy schema into Flyway migration**

Copy `db/001_init_critiqueboard_pgvector.sql` to `src/main/resources/db/migration/V1__init_critiqueboard_pgvector.sql`.

- [ ] **Step 3: Configure local datasource**

Use local defaults:

- Database: `critiqueboard`
- User: `critiqueboard`
- Password: `critiqueboard`

- [ ] **Step 4: Run infrastructure**

Run: `docker compose up -d`

Expected: PostgreSQL and RabbitMQ are healthy or running.

- [ ] **Step 5: Run migration through app test**

Run: `mvn test`

Expected: Flyway migration runs successfully.

- [ ] **Step 6: Commit**

```bash
git add docker-compose.yml src/main/resources/application.yml src/main/resources/db/migration/V1__init_critiqueboard_pgvector.sql
git commit -m "chore: add local infrastructure"
```

## Chunk 2: Review Task API And Persistence

### Task 3: Review Task Domain

**Files:**
- Create: `src/main/java/com/bluefateludi/critiqueboard/review/domain/ReviewTask.java`
- Create: `src/main/java/com/bluefateludi/critiqueboard/review/domain/ReviewTaskStatus.java`
- Create: `src/main/java/com/bluefateludi/critiqueboard/review/domain/AgentRole.java`
- Create: `src/main/java/com/bluefateludi/critiqueboard/review/repository/ReviewTaskRepository.java`
- Test: `src/test/java/com/bluefateludi/critiqueboard/review/domain/ReviewTaskTest.java`

- [ ] **Step 1: Write failing entity behavior test**

Test that a new task starts as `PENDING`, stores the original text and requirement, and preserves `secondRoundEnabled`.

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -Dtest=ReviewTaskTest test`

Expected: fails because `ReviewTask` does not exist yet.

- [ ] **Step 3: Implement minimal domain model**

Create JPA entity mapping to `review_task`.

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn -Dtest=ReviewTaskTest test`

Expected: pass.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/bluefateludi/critiqueboard/review/domain src/main/java/com/bluefateludi/critiqueboard/review/repository src/test/java/com/bluefateludi/critiqueboard/review/domain
git commit -m "feat: add review task domain"
```

### Task 4: Create Review Use Case

**Files:**
- Create: `src/main/java/com/bluefateludi/critiqueboard/review/service/ReviewTaskService.java`
- Create: `src/main/java/com/bluefateludi/critiqueboard/review/service/ReviewTaskPublisher.java`
- Test: `src/test/java/com/bluefateludi/critiqueboard/review/service/ReviewTaskServiceTest.java`

- [ ] **Step 1: Write failing service test**

Test that `createReview` persists a task and publishes its id exactly once.

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -Dtest=ReviewTaskServiceTest test`

Expected: fails because service does not exist.

- [ ] **Step 3: Implement minimal service**

Use repository and publisher port.

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn -Dtest=ReviewTaskServiceTest test`

Expected: pass.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/bluefateludi/critiqueboard/review/service src/test/java/com/bluefateludi/critiqueboard/review/service
git commit -m "feat: add review task creation use case"
```

### Task 5: REST API

**Files:**
- Create: `src/main/java/com/bluefateludi/critiqueboard/review/api/ReviewController.java`
- Create: `src/main/java/com/bluefateludi/critiqueboard/review/api/dto/CreateReviewRequest.java`
- Create: `src/main/java/com/bluefateludi/critiqueboard/review/api/dto/CreateReviewResponse.java`
- Test: `src/test/java/com/bluefateludi/critiqueboard/review/api/ReviewControllerTest.java`

- [ ] **Step 1: Write failing MVC test**

Test that `POST /api/reviews` accepts title, text, requirement, and second-round flag, then returns `reviewTaskId` and `PENDING`.

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -Dtest=ReviewControllerTest test`

Expected: fails because controller does not exist.

- [ ] **Step 3: Implement DTOs and controller**

Use validation annotations:

- `text` required and non-blank.
- `requirement` required and non-blank.
- `title` optional.
- `secondRoundEnabled` defaults to false if omitted.

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn -Dtest=ReviewControllerTest test`

Expected: pass.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/bluefateludi/critiqueboard/review/api src/test/java/com/bluefateludi/critiqueboard/review/api
git commit -m "feat: add review creation api"
```

## Chunk 3: RabbitMQ And Progress Events

### Task 6: Queue Publisher And Consumer Skeleton

**Files:**
- Create: `src/main/java/com/bluefateludi/critiqueboard/review/messaging/ReviewQueueConfig.java`
- Create: `src/main/java/com/bluefateludi/critiqueboard/review/messaging/ReviewTaskMessage.java`
- Create: `src/main/java/com/bluefateludi/critiqueboard/review/messaging/RabbitReviewTaskPublisher.java`
- Create: `src/main/java/com/bluefateludi/critiqueboard/review/agent/ReviewWorker.java`
- Test: `src/test/java/com/bluefateludi/critiqueboard/review/messaging/ReviewTaskMessageTest.java`

- [ ] **Step 1: Write failing message test**

Test that a message can be created with a non-null review task id.

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -Dtest=ReviewTaskMessageTest test`

- [ ] **Step 3: Implement message and RabbitMQ config**

Define exchange, queue, and routing key constants.

- [ ] **Step 4: Implement publisher**

Publish `ReviewTaskMessage` when service creates a review.

- [ ] **Step 5: Add worker skeleton**

Consume message and call `ReviewGraphRunner`.

- [ ] **Step 6: Run tests**

Run: `mvn test`

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/bluefateludi/critiqueboard/review/messaging src/main/java/com/bluefateludi/critiqueboard/review/agent src/test/java/com/bluefateludi/critiqueboard/review/messaging
git commit -m "feat: add review queue workflow"
```

### Task 7: SSE Progress

**Files:**
- Create: `src/main/java/com/bluefateludi/critiqueboard/review/progress/ReviewEventService.java`
- Create: `src/main/java/com/bluefateludi/critiqueboard/review/progress/ReviewSseController.java`
- Test: `src/test/java/com/bluefateludi/critiqueboard/review/progress/ReviewEventServiceTest.java`

- [ ] **Step 1: Write failing progress test**

Test that subscribers receive an event emitted for the same review task id.

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -Dtest=ReviewEventServiceTest test`

- [ ] **Step 3: Implement in-memory SSE emitter registry**

Keep database persistence for events as a later task if needed.

- [ ] **Step 4: Add SSE controller**

Expose `GET /api/reviews/{id}/events`.

- [ ] **Step 5: Run tests**

Run: `mvn test`

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/bluefateludi/critiqueboard/review/progress src/test/java/com/bluefateludi/critiqueboard/review/progress
git commit -m "feat: add review progress stream"
```

## Chunk 4: Agent Contracts And Graph Skeleton

### Task 8: Critique Result Contract

**Files:**
- Create: `src/main/java/com/bluefateludi/critiqueboard/review/agent/CritiqueResult.java`
- Test: `src/test/java/com/bluefateludi/critiqueboard/review/agent/CritiqueResultTest.java`

- [ ] **Step 1: Write failing validation test**

Test that a valid result has role, score, feedback, evidence, suggestions, and confidence.

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -Dtest=CritiqueResultTest test`

- [ ] **Step 3: Implement record DTO**

Use nested `Evidence` record.

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn -Dtest=CritiqueResultTest test`

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/bluefateludi/critiqueboard/review/agent/CritiqueResult.java src/test/java/com/bluefateludi/critiqueboard/review/agent/CritiqueResultTest.java
git commit -m "feat: add critique result contract"
```

### Task 9: Graph Runner Skeleton

**Files:**
- Create: `src/main/java/com/bluefateludi/critiqueboard/review/agent/ReviewGraphRunner.java`
- Create: `src/main/java/com/bluefateludi/critiqueboard/review/agent/LangGraphReviewGraphRunner.java`
- Test: `src/test/java/com/bluefateludi/critiqueboard/review/agent/ReviewGraphRunnerTest.java`

- [ ] **Step 1: Write failing graph runner test**

Test that running a task records progress phases in order when using fake Agent clients.

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -Dtest=ReviewGraphRunnerTest test`

- [ ] **Step 3: Implement initial runner skeleton**

For the first pass, implement the port and a deterministic skeleton. Wire LangGraph4j in the next iteration once dependencies and APIs are confirmed.

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn -Dtest=ReviewGraphRunnerTest test`

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/bluefateludi/critiqueboard/review/agent src/test/java/com/bluefateludi/critiqueboard/review/agent
git commit -m "feat: add review graph runner skeleton"
```

## Chunk 5: DeepSeek And LangChain4j Integration

### Task 10: DeepSeek Configuration

**Files:**
- Create: `src/main/java/com/bluefateludi/critiqueboard/review/ai/DeepSeekProperties.java`
- Create: `src/main/java/com/bluefateludi/critiqueboard/review/ai/LangChainAiConfig.java`
- Modify: `src/main/resources/application.yml`
- Test: `src/test/java/com/bluefateludi/critiqueboard/review/ai/DeepSeekPropertiesTest.java`

- [ ] **Step 1: Write failing properties binding test**

Test base URL, API key, model, timeout, and enabled flag.

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -Dtest=DeepSeekPropertiesTest test`

- [ ] **Step 3: Implement properties class**

Use `@ConfigurationProperties(prefix = "critiqueboard.ai.deepseek")`.

- [ ] **Step 4: Add LangChain4j configuration**

Configure an OpenAI-compatible chat model for DeepSeek.

- [ ] **Step 5: Run tests**

Run: `mvn test`

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/bluefateludi/critiqueboard/review/ai src/main/resources/application.yml src/test/java/com/bluefateludi/critiqueboard/review/ai
git commit -m "feat: configure deepseek chat model"
```

## Chunk 6: End-To-End Smoke Path

### Task 11: Local Smoke Test

**Files:**
- Create: `http/reviews.http`
- Modify: `README.md`

- [ ] **Step 1: Add HTTP examples**

Add create-review and query-review examples.

- [ ] **Step 2: Add local run instructions**

Document:

- `docker compose up -d`
- set `DEEPSEEK_API_KEY`
- `mvn spring-boot:run`
- submit `POST /api/reviews`
- subscribe to SSE

- [ ] **Step 3: Run local verification**

Run:

```bash
docker compose up -d
mvn test
mvn spring-boot:run
```

Expected: app starts and accepts review creation.

- [ ] **Step 4: Commit**

```bash
git add README.md http/reviews.http
git commit -m "docs: add local smoke test guide"
```

## Implementation Notes

- Use TDD for production Java behavior.
- Keep DTOs and domain classes small.
- Do not put LLM prompt logic in controllers.
- Keep RabbitMQ message payloads small; pass ids, not full documents.
- Keep real DeepSeek calls behind interfaces so tests can use fake implementations.
- Use `src/main/resources/db/migration` as the canonical migration location for Spring Boot.
- Keep `db/001_init_critiqueboard_pgvector.sql` as a readable source/reference copy unless a future migration tool makes it redundant.
