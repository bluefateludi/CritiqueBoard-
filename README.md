# CRITIQUEBOARD

CRITIQUEBOARD is an API-first multi-agent review system. A user submits text and review requirements, the backend creates an asynchronous review task, chunks the document, publishes the task to RabbitMQ, streams progress over SSE, runs Specialist reviewers with document evidence, and stores a final review report.

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

Optional cost settings are used when writing `token_usage` records:

```bash
set DEEPSEEK_INPUT_COST_PER_MILLION_TOKENS=0
set DEEPSEEK_OUTPUT_COST_PER_MILLION_TOKENS=0
```

Default model config is in `src/main/resources/application.yml`.

## Run

```bash
mvn spring-boot:run
```

For a packaged local smoke test:

```bash
mvn -DskipTests package
java -jar target/critiqueboard-0.0.1-SNAPSHOT.jar
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

Query task status:

```http
GET http://localhost:8080/api/reviews/{{reviewTaskId}}
```

Completed tasks include Specialist results and a final report:

```json
{
  "reviewTaskId": "00000000-0000-0000-0000-000000000000",
  "title": "Launch Plan",
  "status": "COMPLETED",
  "report": {
    "overallScore": 76,
    "summary": "Synthesized 3 specialist reviews with an overall score of 76.",
    "strengths": ["The document has a workable structure."],
    "weaknesses": ["The risk section needs clearer mitigation owners."],
    "actions": ["Add risk owners, trigger thresholds, and rollback actions."],
    "finalMarkdown": "# Review Report\n\n..."
  },
  "specialistReviews": []
}
```

The worker publishes `TASK_FAILED` over SSE and marks the task `FAILED` if an unexpected graph/worker error escapes. DeepSeek calls fall back to the deterministic reviewer when the API is disabled, the key is blank, the model call fails, or the JSON response cannot be parsed.

## Local End-to-End Checklist

1. Start infrastructure with `docker compose up -d`.
2. Set `DEEPSEEK_API_KEY` for real DeepSeek calls. Leave it blank to exercise the deterministic fallback.
3. Run the app with `mvn spring-boot:run` or the packaged jar.
4. Submit `http/reviews.http` create request.
5. Open `/api/reviews/{{reviewTaskId}}/events` before or immediately after submitting to observe live SSE progress.
6. Poll `GET /api/reviews/{{reviewTaskId}}` until `COMPLETED` or `FAILED`.
7. Confirm Postgres contains one `review_report`, three `agent_run` rows, three `critique_result` rows, and evidence rows referencing `document_chunk` content.
8. Confirm RabbitMQ queue `review.task.queue` has zero ready and unacknowledged messages.
