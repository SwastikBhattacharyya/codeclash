# Common

Shared infrastructure used by every service. Each piece is switched on with its own `@Enable…` annotation.

## Overview

- **Events:** `CdcEvent` is the change-event envelope. `OutboxKafkaRouting` sends outbox records to the Kafka topic named in their context.
- **Consumers:** `RetryProperties` and `LoggingDeadLetterRecoverer` configure retries and dead-letter logging.
- **JDBC:** `JdbcFilters`, `JdbcPages` and `JdbcSorts` build filtered, paged and sorted queries. `JdbcConstraints` and `ConstraintFinder` turn Postgres constraint names into domain exceptions.
- **Errors:** `CodedException`, `ErrorResponseBody` and the global handler give every API the same error shape.
- **PATCH:** `PatchObject` reads PATCH bodies.
- **Config:** shared OpenAPI and observability setup.
