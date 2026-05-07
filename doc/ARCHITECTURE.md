# Architecture Guidelines

## Purpose

This document describes the architectural direction for the self-scanned book viewing web application. It should be updated when important technical decisions change.

## System Overview

The system consists of:

- Frontend: Next.js
- Backend API: Spring Boot 4.0.6
- Conversion worker: Spring Boot 4.0.6
- Database: PostgreSQL
- Search: Elasticsearch with analysis-kuromoji
- Deployment: Docker Compose on a single Linux host

The initial deployment target is a single Linux host, but the architecture should keep future separation of API, worker, PostgreSQL, Elasticsearch, and other middleware possible.

## Core Decisions

- PostgreSQL is the source of truth for metadata, users, permissions, and job state.
- Elasticsearch is derived and rebuildable from PostgreSQL and stored book metadata.
- The backend should follow a modular monolith direction.
- API and worker responsibilities should remain separable even if implemented in the same repository.
- Archive extraction should be performed by the conversion worker using the 7-Zip for Linux console tool.
- Image conversion should target WebP quality 80 by default and remain configurable.

## Modular Monolith Direction

A modular monolith means the application may be deployed as one system initially, but internal boundaries must remain clear.

Expected module examples:

- Book catalog
- File ingestion
- Archive extraction
- Image conversion
- User and permission management
- Search indexing
- Job management
- Viewer API

Rules:

- Keep module responsibilities explicit.
- Avoid cross-module database shortcuts that bypass intended boundaries.
- Avoid sharing mutable implementation details between modules.
- Prefer module-level application services for use case coordination.
- Keep infrastructure details behind module-specific adapters or repositories.

## Backend Layering

Recommended backend layering:

```text
Controller
  -> Application Service
    -> Domain Model / Domain Service
      -> Repository Port
        -> Infrastructure Adapter
```

Rules:

- Controllers handle HTTP request/response concerns only.
- Application services coordinate use cases and transaction boundaries.
- Domain code contains business rules.
- Repositories hide persistence details.
- Infrastructure adapters handle PostgreSQL, Elasticsearch, file system, external commands, and other technical integrations.

## Frontend Direction

- Keep page-level routing separate from reusable UI components.
- Keep API access logic separate from presentation components.
- Avoid duplicating backend authorization rules on the client. Client-side checks are only for user experience.
- Treat server responses and error states explicitly.
- Use pagination or incremental loading for large book lists, image sets, or search results.

## Worker Direction

The conversion worker is responsible for long-running and resource-intensive tasks such as:

- Archive extraction
- Image conversion
- Thumbnail generation
- Metadata extraction where appropriate
- Search indexing requests or indexing coordination
- Job state updates

Rules:

- Worker tasks must be observable through job state.
- Failures should update job state with enough diagnostic context.
- Worker logic should not depend on active HTTP request scope.
- External process calls must have explicit timeout and error handling policies.
- Worker output should be reproducible where practical.

## Data Ownership

PostgreSQL owns:

- User data
- Permission data
- Book metadata
- File metadata
- Job state
- Conversion status
- Search indexing status

Elasticsearch owns:

- Derived search documents
- Analyzers and indexes optimized for search

Elasticsearch must be rebuildable from PostgreSQL and stored metadata.

## Search Architecture

- Use Elasticsearch with analysis-kuromoji for Japanese search.
- Keep search documents derived from PostgreSQL records.
- Search index updates should be triggered by explicit state changes or indexing jobs.
- Index rebuilds should be possible without losing authoritative data.
- Search result permissions must be enforced correctly. Do not rely only on Elasticsearch filtering if permission correctness requires PostgreSQL validation.

## File and Asset Architecture

- Uploaded archives and generated images should be handled as private assets unless explicitly made public.
- Prevent path traversal and unsafe extraction paths.
- Generated assets should be traceable to source files and conversion jobs.
- Large files should not be stored in Git.
- File paths should not be exposed as public API details unless intentionally designed.

## Configuration

- Environment-specific values must not be hard-coded.
- Use application properties and environment variables for configurable settings.
- Important configuration examples:
  - WebP quality
  - Worker concurrency
  - External process timeout
  - Upload size limit
  - Storage paths
  - Elasticsearch index names
  - Database connection settings

## Architecture Decision Records

Use ADRs for decisions that affect long-term maintainability or system behavior.

Recommended path:

```text
doc/03_architecture/03_adr/NN_ADR-0000-title.md
```

ADR template:

```markdown
# ADR-0000: Title

## Status

Accepted

## Context

What problem or decision is being addressed?

## Decision

What decision was made?

## Consequences

What are the positive and negative consequences?

## Alternatives

What alternatives were considered?
```

The full repository template is maintained at:

```text
doc/03_architecture/03_adr/01_ADR-template.md
```
