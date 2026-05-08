# Coding Standards

## Purpose

These standards define the default coding expectations for this repository. They are intentionally language-neutral where possible and should be applied consistently across the frontend, backend API, conversion worker, tests, and supporting scripts.

## General Principles

- Prioritize readability, correctness, maintainability, and security over cleverness.
- Follow clean code principles: names, functions, classes, and modules should each have one clear intent.
- Prefer explicit behavior over implicit behavior.
- Follow DRY for shared knowledge and business rules, but do not over-abstract just to remove small structural similarities.
- Keep code simple and avoid unnecessary abstractions.
- Do not implement speculative features or extension points that are not currently required.
- Follow the principle of least astonishment: names, behavior, return values, and side effects should match developer expectations.
- Keep changes small, cohesive, and easy to review.
- Follow clean architecture principles where practical: domain and use case code should not depend on frameworks, databases, external services, or UI details.

## Naming

- Use names that clearly express purpose, responsibility, and domain meaning.
- Avoid vague names such as `data`, `info`, `list`, `manager`, `processor`, or `handler` unless the role is genuinely generic.
- Use domain terms consistently across frontend, backend, worker, database, API, and documentation.
- Boolean names should read naturally, such as `isVisible`, `hasPermission`, `canConvert`, or `shouldRetry`.
- Method names should clearly indicate whether they query, create, update, delete, convert, validate, or trigger side effects.
- Names that imply no side effects must not perform hidden updates, database writes, external API calls, or file operations.
- Avoid unnecessary abbreviations.

## Function and Method Design

- A function or method should have one clear responsibility.
- Keep parameter lists small. If many arguments are required, consider a request object, command object, or value object.
- Return values should have clear meaning. Avoid ambiguous `null` results unless the behavior is explicit and consistent.
- Prefer early validation and early return to deeply nested conditional logic.
- Avoid hidden side effects. If a method mutates state, writes to storage, performs I/O, or sends messages, its name and layer should make that clear.
- Avoid magic numbers and magic strings. Use named constants, enums, configuration properties, or domain objects.
- Keep business rules out of low-level infrastructure code.

## Class and Module Design

- Each class should have a single, clear responsibility.
- Prefer high cohesion and low coupling.
- Prefer composition over inheritance unless inheritance clearly models a stable relationship.
- Use interfaces when they clarify boundaries, enable substitution, improve testing, or isolate infrastructure.
- Do not introduce interfaces only for mechanical abstraction.
- Avoid large utility classes that become dumping grounds for unrelated behavior.
- Keep mutable state to the minimum necessary.
- Separate domain concepts from transport, persistence, and framework-specific models.

## Layering

- Controllers should handle HTTP concerns and delegate application behavior.
- Application services should coordinate use cases, validation flow, authorization checks, and transaction boundaries.
- Domain code should express business rules and should not depend on web, database, or framework-specific details.
- Repositories should encapsulate persistence access.
- Infrastructure code should handle PostgreSQL, Elasticsearch, file system access, external processes, and other technical integrations.
- Conversion worker logic should be isolated from API request handling.
- Dependencies should point inward toward business policy. Outer details such as HTTP, persistence, search indexes, file storage, queues, external commands, and UI formats should depend on inner abstractions rather than the other way around.
- Boundary conversions should be explicit. Do not pass transport models, persistence entities, or search documents deep into domain logic as substitutes for domain models.

## Clean Code

- Keep functions and methods focused on one purpose.
- Keep abstraction levels consistent within a function. Avoid mixing high-level use case flow with low-level technical details in the same block.
- Prefer clear names and structure over explanatory comments.
- Use comments to explain why something exists, not to restate what the code already says.
- Replace complex conditionals with named methods, domain concepts, value objects, or small focused services when that improves readability.
- Avoid magic numbers and magic strings. Use named constants, enums, configuration properties, or domain objects.
- Treat `null`, blank values, empty collections, and missing values explicitly and consistently.
- Extract shared code when it removes duplicated knowledge or duplicated business rules. Do not abstract code only because the shape looks similar.
- If code is difficult to test, reconsider its responsibility boundaries and dependencies.

## Error Handling

- Do not swallow exceptions silently.
- Avoid broad exception handling such as catching `Exception` unless there is a clear boundary-level reason.
- Distinguish validation errors, business errors, external dependency failures, and unexpected system errors.
- User-facing error messages should be safe and understandable.
- Internal error details should be logged where appropriate, but must not leak sensitive information to users.
- Retry behavior must be explicit and limited to safe, retryable operations.
- File conversion, archive extraction, and search indexing failures should be represented as recoverable job states when appropriate.

## Validation

- Backend services must not trust frontend validation.
- Validate request bodies, query parameters, path parameters, headers, cookies, uploaded files, archive entries, metadata, and job parameters on the server side.
- Treat hidden fields, read-only UI fields, select options, client-side ranges, and client-side validation as untrusted because requests can be modified.
- Validate required fields, lengths, numeric ranges, formats, enum values, dates, pagination values, and sorting values.
- Authorization-sensitive operations must combine input validation with server-side authorization checks.
- Normalize and validate file names, archive paths, and extracted paths before file operations.
- Keep validation error responses consistent and safe.
- Domain invariants should not rely only on API-level validation. Enforce important invariants in the domain or use case layer as well.

## Logging

- Logs should help diagnose behavior without exposing sensitive information.
- Do not log passwords, tokens, secrets, session identifiers, private user data, or unnecessary file contents.
- Use log levels consistently:
  - `DEBUG`: detailed diagnostic information
  - `INFO`: meaningful lifecycle or job progress events
  - `WARN`: recoverable but notable problems
  - `ERROR`: failures requiring investigation
- Avoid duplicate logging of the same exception at multiple layers.
- Long-running conversion and indexing jobs should log meaningful progress and failure context.

## Testing

- Use TDD as the default development approach for behavior changes.
- Before implementing a new behavior, express the expected behavior as a failing automated test where practical.
- Follow the red, green, refactor cycle in small increments: add a failing test, implement the minimum code to pass it, then improve structure without changing behavior.
- When a behavior is difficult to automate at the start, define the acceptance test or manual verification point before implementation and add automated coverage as the design becomes testable.
- Add or update tests proportional to the affected behavior.
- Business rules, permission checks, conversion job state transitions, and search indexing behavior should have tests where practical.
- Include normal cases, boundary cases, and failure cases.
- Tests should have descriptive names that explain the expected behavior.
- Mock external dependencies where appropriate, especially file system operations, external processes, Elasticsearch, and long-running conversions.
- Do not rely only on coverage numbers. Important behavior must be meaningfully asserted.
- Regression bugs should be covered by tests when fixed.

## Dependency Management

- Do not add new libraries without a clear reason.
- Prefer well-maintained, widely used dependencies.
- Avoid dependencies that duplicate functionality already available in the platform or existing stack.
- Keep dependency usage isolated when it may be replaced later.
- Check security implications before introducing libraries for archive handling, image processing, authentication, parsing, or file access.
- Do not introduce Java rar support for archive extraction when the architecture uses the 7-Zip for Linux console tool from the conversion worker.

## Data and Model Design

- Keep API request/response models, persistence entities, and domain models conceptually separate.
- Do not let database entities become catch-all business objects.
- Use enums or value objects for fixed states such as job status, visibility, permission type, conversion status, and processing phase.
- Avoid representing important domain states as arbitrary strings.
- Treat date, time, timezone, and duration handling consistently.
- Use appropriate numeric types for counts, file sizes, image dimensions, durations, and quality settings.
- Configuration values such as WebP quality should be configurable through application properties.
- Convert models at architectural boundaries explicitly. API request models should be validated and transformed before use case execution, and persistence entities should not be reused as public API responses or broad domain objects.

## API Design

- API behavior should be predictable and consistent.
- Use appropriate HTTP status codes for validation errors, authentication failures, authorization failures, missing resources, conflicts, and server errors.
- Error response structures should be consistent.
- Avoid exposing internal database IDs, file paths, or implementation details unless intentionally part of the API contract.
- Preserve backward compatibility where practical.
- Document breaking API changes.

## Database Access

- Keep transaction boundaries explicit and appropriate to the use case.
- Avoid unnecessary database access in loops.
- Be aware of N+1 query problems.
- Use indexes intentionally for search conditions, joins, and frequently accessed metadata.
- Use optimistic locking or another concurrency strategy where concurrent updates may occur.
- Do not treat Elasticsearch as authoritative storage.

## Performance and Scalability

- Prefer measurement before optimization.
- Avoid loading large archives, books, image sets, or search results fully into memory when streaming or paging is more appropriate.
- Use pagination for large result sets.
- Keep conversion and indexing workloads isolated from request-response paths.
- Design job processing so that API, worker, PostgreSQL, and Elasticsearch can be separated in the future.
- Make expensive operations explicit and observable.

## Comments

- Comments should explain why code exists, not restate what the code already says.
- Avoid obvious comments.
- Remove commented-out code. Version history belongs in Git.
- TODO comments should include enough context to be actionable.
- Important specification or architecture decisions should be moved to ADRs or design documents, not left only as code comments.

## Review Expectations

- Review for correctness, readability, responsibility boundaries, security, and test coverage.
- Do not focus on style issues that should be handled automatically by tools.
- Check whether names accurately express intent.
- Check whether code follows clean code principles: small focused functions, clear responsibilities, and readable control flow.
- Check whether clean architecture dependency direction is preserved.
- Check whether business rules are placed in the correct layer.
- Check whether errors, logs, permissions, and invalid inputs are handled safely.
- Check whether backend validation does not depend on frontend checks.
- Check whether documentation should be updated together with the code change.
- Prefer small pull requests with focused intent.
