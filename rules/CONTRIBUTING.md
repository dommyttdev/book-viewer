# Contributing Guidelines

## Purpose

This document defines contribution, commit, branch, pull request, and verification rules for this repository.

## General Workflow

1. Check [doc/TODO.md](../doc/TODO.md) before starting work.
2. Keep changes small and focused.
3. Update related documentation when behavior, architecture, or specifications change.
4. Run tests or checks proportional to the affected area.
5. Use clear Japanese commit messages with the required prefix.
6. Keep pull requests focused and reviewable.
7. Follow the branch strategy in [doc/05_development/02_branch_strategy.md](../doc/05_development/02_branch_strategy.md).

## Branch Rules

This repository uses Git-flow as the basic branch strategy.

Branch roles:

- `main`: Stable branch for released or releasable code.
- `develop`: Integration branch for the next release.
- `feature/*`: Feature work created from `develop` and merged back into `develop`.
- `fix/*`: Normal bug fixes created from `develop` and merged back into `develop`.
- `docs/*`: Documentation-only changes created from `develop` and merged back into `develop`.
- `release/*`: Release preparation created from `develop` and merged into both `main` and `develop`.
- `hotfix/*`: Urgent fixes for released code created from `main` and merged into both `main` and `develop`.

Do not commit directly to `main` or `develop`. Use pull requests for integration.

Use short, descriptive branch names.

Recommended format:

```text
<type>/<short-description>
```

Examples:

```text
feature/book-search
fix/job-status-update
docs/architecture-guidelines
security/archive-path-validation
release/0.1.0
hotfix/login-failure
```

Allowed branch type prefixes:

- `feature`
- `fix`
- `docs`
- `refactor`
- `test`
- `chore`
- `style`
- `perf`
- `security`
- `build`
- `ci`
- `release`
- `hotfix`
- `revert`

Use `feature/*` for Git-flow feature branches. Use `feat` as the commit type for feature commits.

## Commit Rules

Commits must be small, focused, and logically grouped.

Rules:

- Write commit messages in Japanese.
- Use the format: `<type>: <Japanese summary>`.
- Use one of the allowed types.
- Do not mix unrelated changes in one commit.
- Do not commit secrets, credentials, generated large files, local environment files, or temporary artifacts.
- Update related documentation and TODO status in the same change when behavior, architecture, or specifications change.

Allowed types:

| Type | Meaning |
|---|---|
| `feat` | New feature |
| `fix` | Bug fix |
| `docs` | Documentation-only change |
| `refactor` | Refactoring without behavior change |
| `test` | Test addition or update |
| `chore` | Maintenance task |
| `style` | Formatting or style-only change |
| `perf` | Performance improvement |
| `security` | Security fix or hardening |
| `build` | Build system or dependency change |
| `ci` | CI/CD change |
| `revert` | Revert a previous change |

Good examples:

```text
feat: 書籍一覧画面に検索条件を追加
fix: 変換ジョブ失敗時にステータスが更新されない不具合を修正
docs: アーキテクチャ方針を更新
refactor: 書籍メタデータ取得処理をサービス層へ移動
test: 変換ジョブ状態遷移のテストを追加
security: アーカイブ展開時のパストラバーサル対策を追加
```

Bad examples:

```text
fix: 修正
feat: いろいろ追加
chore: 作業
update files
```

## Commit Body

Use a commit body when the summary alone is not enough.

Example:

```text
fix: 変換ジョブ失敗時にステータスが更新されない不具合を修正

例外発生時にFAILEDへ遷移せず、IN_PROGRESSのまま残るケースがあったため、
失敗理由と終了時刻を保存するように変更した。
```

## Breaking Changes

If a change breaks compatibility, state it clearly in the commit body or pull request description.

Example:

```text
feat: 書籍詳細APIのレスポンス形式を変更

BREAKING CHANGE: `thumbnailUrl` を `images.thumbnailUrl` に移動した。
```

## Pull Request Rules

A pull request should include:

- Purpose of the change
- Main implementation points
- Verification results
- Related TODO, issue, or document updates
- Screenshots for UI changes where helpful
- Migration or operational notes if needed

Keep pull requests small. If a change affects unrelated areas, split it.

Merge targets:

- Daily work branches such as `feature/*`, `fix/*`, `docs/*`, `refactor/*`, `test/*`, `chore/*`, `style/*`, `perf/*`, `security/*`, `build/*`, and `ci/*` should target `develop`.
- `release/*` should be merged into `main`, tagged, and then reflected back into `develop`.
- `hotfix/*` should be merged into `main`, tagged, and then reflected back into `develop`.

Before merging, confirm that the branch is up to date with the appropriate base branch, conflicts are resolved, required documentation is updated, and verification results are recorded.

## Documentation Updates

Update documentation when changing:

- Product behavior
- API behavior
- Architecture decisions
- Security behavior
- Configuration
- Job state behavior
- Database schema
- Search indexing behavior
- Worker behavior

Important decisions should be recorded in ADRs or design documents, not only in TODO notes.

## TODO Status Updates

Use these statuses in [doc/TODO.md](../doc/TODO.md):

- `[ ]` not started
- `[~]` in progress
- `[x]` completed

When completing a TODO:

1. Update the related implementation or document.
2. Update the TODO status.
3. Ensure links and references still work.

## Verification

Before submitting or committing code changes, run checks proportional to the affected area.

Examples:

- Frontend changes: type check, lint, relevant UI tests, or manual UI verification.
- Backend changes: unit tests, integration tests, API tests, or relevant Spring Boot tests.
- Worker changes: conversion job tests, archive extraction tests, image conversion tests, or failure-state tests.
- Documentation changes: rendered Markdown review and link/path check.
- Security-sensitive changes: abuse-case review and tests where practical.

## Files That Must Not Be Committed

Do not commit:

- Real secrets or credentials
- Local `.env` files
- Private keys
- Production configuration files
- Generated large archives or images
- Temporary files
- Local IDE metadata unless intentionally standardized
- Build outputs unless explicitly required

Use example files such as `.env.example` for documenting required variables.
