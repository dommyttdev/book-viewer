# Repository Guidelines

## Project Context

This repository is for a self-scanned book viewing web application.

Planned stack:

- Frontend: Next.js
- Backend API: Spring Boot 4.0.6
- Conversion worker: Spring Boot 4.0.6
- Java: 25
- Database: PostgreSQL
- Search: Elasticsearch with analysis-kuromoji
- Deployment: Docker Compose on a single Linux host

PostgreSQL is the source of truth. Elasticsearch is derived and rebuildable.

Key planning items are tracked in [doc/TODO.md](doc/TODO.md).

## Documentation Workflow

- Treat [doc/TODO.md](doc/TODO.md) as the current planning source until more documents are created.
- Keep documentation in Japanese unless the surrounding file is already English.
- Read and write Markdown files as UTF-8.
- When completing a TODO, update the related document and then update the TODO status:
  - `[ ]` not started
  - `[~]` in progress
  - `[x]` completed
- Record important specification or architecture decisions in ADRs or design documents, not only in TODO notes.

## Core Architecture Rules

- Follow a modular monolith direction.
- Keep API, worker, and middleware separation possible in the future.
- Keep module boundaries clear.
- Keep business logic out of controllers and infrastructure code.
- Use PostgreSQL as authoritative storage.
- Treat Elasticsearch as rebuildable derived data.
- Use the 7-Zip for Linux console tool from the conversion worker for archive extraction.
- Default image conversion should target WebP quality 80 and remain configurable through application properties.

## Development Rules

- Follow [rules/CODING_STANDARDS.md](rules/CODING_STANDARDS.md).
- Follow DRY, KISS, YAGNI, single responsibility, and least astonishment.
- Use clear domain-oriented names.
- Keep functions, classes, and modules focused.
- Avoid hidden side effects.
- Keep API models, persistence entities, and domain models conceptually separate.
- Do not hard-code environment-specific values or secrets.
- Validate external input, uploaded files, archive contents, paths, and job parameters.
- Do not log secrets, tokens, passwords, or unnecessary private data.

## Commit and Change Management

- Keep commits small, focused, and logically grouped.
- Do not mix unrelated changes in a single commit.
- Use MCP for GitHub operations such as reading or updating issues and pull requests.
- Write commit messages in Japanese.
- Use the Conventional Commits-like format: `<type>: <Japanese summary>`.
- Allowed types: `feat`, `fix`, `docs`, `refactor`, `test`, `chore`, `style`, `perf`, `security`, `build`, `ci`, `revert`.
- Update related documentation and TODO status in the same change when behavior, architecture, or specifications change.
- Before committing code changes, run tests or checks proportional to the affected area.
- Do not commit secrets, credentials, generated large files, local environment files, or temporary artifacts.
- See [rules/CONTRIBUTING.md](rules/CONTRIBUTING.md) for detailed commit, branch, and pull request rules.

## MCP Tools

- Use MCP tools for GitHub operations, including reading issues, updating issues, reading pull requests, and updating pull requests.

## Verification

- For documentation-only changes, review rendered Markdown structure and check that links and paths match [doc/TODO.md](doc/TODO.md).
- For code changes, add or run tests proportional to the affected behavior.
- Update related documentation as part of the definition of done.
- Check naming, responsibility boundaries, error handling, logging, security, and tests before considering work complete.

## Detailed References

- Coding standards: [rules/CODING_STANDARDS.md](rules/CODING_STANDARDS.md)
- Architecture rules: [rules/ARCHITECTURE.md](rules/ARCHITECTURE.md)
- Security rules: [rules/SECURITY.md](rules/SECURITY.md)
- Contribution rules: [rules/CONTRIBUTING.md](rules/CONTRIBUTING.md)
