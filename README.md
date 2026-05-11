# CRITIQUEBOARD

CRITIQUEBOARD is an API-first multi-agent review system. A user submits text and review requirements, the backend creates an asynchronous review task, publishes it to RabbitMQ, streams progress over SSE, and prepares the path for LangGraph4j/LangChain4j Specialist review.

## Stack

- Java 21
- Spring Boot 3.5.x
- PostgreSQL with pgvector
- RabbitMQ
- Flyway
- LangChain4j
- DeepSeek OpenAI-compatible API

## Local Infrastructure

Start PostgreSQL and RabbitMQ:

```bash
docker compose up -d
```

RabbitMQ management UI:

```text
http://localhost:15672
username: critiqueboard
password: critiqueboard
```

PostgreSQL defaults:

```text
host: localhost
port: 5432
database: critiqueboard
username: critiqueboard
password: critiqueboard
```

## Configuration

Set a DeepSeek API key before running with real model calls:

```bash
set DEEPSEEK_API_KEY=your-api-key
```

Default model config is in `src/main/resources/application.yml`.

## Run

```bash
mvn spring-boot:run
```

## Test

```bash
mvn test
```

## API Smoke Test

Create a review task:

```http
POST http://localhost:8080/api/reviews
Content-Type: application/json

{
  "title": "Launch Plan",
  "text": "We will launch the product in Q3. The rollout depends on partner approval.",
  "requirement": "Review structure, logic, and execution risk.",
  "secondRoundEnabled": true
}
```

Subscribe to progress:

```http
GET http://localhost:8080/api/reviews/{{reviewTaskId}}/events
Accept: text/event-stream
```

The current graph runner is still a deterministic skeleton. It emits review phases in order and will be replaced by the real LangGraph4j Supervisor/Specialist graph in the next implementation milestone.
