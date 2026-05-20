# Security Guidelines

## Purpose

This document defines security expectations for the self-scanned book viewing web application. The application handles private files, generated images, metadata, users, permissions, and search indexes, so security must be considered in all implementation work.

## Core Principles

- Validate all external input.
- Enforce authorization on the server side.
- Treat uploaded archives, extracted files, metadata, thumbnails, and search indexes as potentially sensitive.
- Do not expose internal paths, stack traces, credentials, or implementation details.
- Apply least privilege to files, processes, database users, and service accounts.
- Prefer safe defaults.

## Input Validation

Validate:

- HTTP request bodies
- Query parameters
- Path parameters
- Uploaded file names
- Archive entries
- Extracted paths
- Metadata fields
- Job parameters
- Pagination and sorting values

Rules:

- Reject invalid input early.
- Use allowlists where practical.
- Do not trust file extensions alone.
- Set explicit size limits for uploads and extracted output.
- Normalize and validate paths before file operations.

## Authentication and Authorization

- Enforce authorization on the backend for every protected operation.
- Do not rely on frontend checks for security.
- Check access before returning book metadata, images, thumbnails, search results, or job details.
- Avoid leaking whether private resources exist when the user is not authorized to access them.
- Use consistent handling for authentication failures and authorization failures.
- Do not store plaintext authentication tokens, WebAuthn challenges, recovery tokens, or session identifiers.
- Do not store WebAuthn credential private keys. Store only credential IDs, public keys, counters, transports, and state needed for verification.
- Store only purpose-specific hashes for email verification tokens, WebAuthn registration/authentication challenges, account recovery challenges, and session identifiers.
- Track expiration, used, revoked, attempt count, and resend count fields for authentication challenges where applicable.
- Revoke sessions and unused authentication challenges when users withdraw, accounts are suspended, credentials are disabled, account recovery completes, emails change, or logout is performed.

## File Upload and Archive Extraction

Archive extraction is security-sensitive.

Rules:

- Prevent path traversal, including `../`, absolute paths, drive letters, and symlink-based escapes.
- Extract only into controlled worker directories.
- Do not overwrite arbitrary files.
- Limit extracted file count, total size, and nesting depth where possible.
- Treat archive entry names as untrusted input.
- Use explicit timeouts for external extraction processes.
- Capture failures and update job state safely.
- Do not expose raw extraction logs to end users.

## Image Processing

- Treat image files as untrusted input.
- Isolate conversion in worker logic.
- Apply limits to image dimensions, file size, and processing time where practical.
- Store generated images in controlled locations.
- Avoid exposing internal storage paths.
- Keep conversion settings configurable.

## Database Security

- Use parameterized queries or safe ORM mechanisms.
- Do not build SQL from untrusted string concatenation.
- Use database users with least required privileges.
- Keep migrations reviewable.
- Avoid storing secrets in database fields unless explicitly required and protected.
- Consider optimistic locking or safe state transitions for job updates.

## Search Security

- Treat Elasticsearch data as derived but still sensitive.
- Do not index secrets, credentials, or unnecessary private data.
- Ensure search results respect permissions.
- Keep index rebuilds controlled and observable.
- Do not expose raw Elasticsearch errors to users.

## Secrets Management

Do not commit:

- Passwords
- API keys
- Tokens
- Private keys
- Production database URLs
- Local `.env` files
- Cloud credentials

Rules:

- Use environment variables or secret management mechanisms.
- Provide safe examples such as `.env.example` without real secrets.
- Rotate secrets if they are accidentally exposed.

## Logging and Error Messages

Do not log:

- Passwords
- Tokens
- Session identifiers
- WebAuthn challenges
- One-time authentication codes
- Token hashes or session identifier hashes
- Credential private keys
- Private keys
- Full private file contents
- Unnecessary personal data

Rules:

- User-facing errors should be safe and understandable.
- Internal errors should include enough context for diagnosis without leaking sensitive data.
- Avoid duplicate exception logging across layers.
- Do not return stack traces to clients.
- Do not expose host paths or container paths to clients.

## Dependency Security

- Prefer maintained and widely used dependencies.
- Review security implications before adding dependencies for parsing, archive handling, image processing, authentication, or file access.
- Keep dependencies updated intentionally.
- Remove unused dependencies.
- Run dependency vulnerability checks where available.

## Local Development Safety

- Local defaults must not connect to production resources.
- Local sample credentials must be fake.
- Development-only features must not be enabled in production.
- Test data should not contain real private user data unless explicitly approved and protected.

## Security Review Checklist

Before completing a security-relevant change, verify:

- External input is validated.
- Authorization is enforced server-side.
- File paths are normalized and checked.
- Secrets are not hard-coded or logged.
- Errors do not leak internal details.
- Job failures are recorded safely.
- Search results do not bypass permissions.
- Tests cover important failure or abuse cases where practical.
