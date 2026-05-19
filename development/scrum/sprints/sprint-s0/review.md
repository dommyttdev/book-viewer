# Sprint S0 レビュー

## スプリント

- Sprint: S0
- スプリントゴール: 後続PBIをTDDで継続実装できるよう、フロントエンド、API、Worker、ローカルミドルウェアの最小基盤を構築するための作業順序と完了判定を明確にする。
- レビュー日: 2026-05-15

## 完成した成果

| PBI | 結果 | デモ内容 | 受入判断 |
| --- | --- | --- | --- |
| PBI-001 | Sprint S0の最小基盤を構築し、確認結果を記録した。 | フロントエンド、API、Worker、Docker Composeミドルウェア、ローカル設定、最小確認コマンド、開発手順更新までを通して確認した。 | PBI-001のリポジトリ内成果物は完了。GitHub Issue / Projectの状態更新は運用タイミングで反映する。 |
| #81 | プロジェクト構成とビルド方針を決定し、`doc/` へ反映した。 | `apps/frontend`、`apps/api`、`apps/worker`、`libs/backend-common`、Gradle Wrapper、npm、API / Worker別プロセス方針を記録した。 | 完了。 |
| #82 | Next.jsフロントエンド最小構成を作成した。 | `npm.cmd run lint`、`typecheck`、`build`、`dev`、HTTP 200、`NEXT_PUBLIC_API_BASE_URL` の読み込みを確認した。 | 完了。 |
| #83 | Spring Boot API最小構成を作成した。 | `:apps:api:test`、`local` profile起動、`/actuator/health`、ログ安全性を確認した。 | 完了。 |
| #84 | Spring Boot Worker最小構成を作成した。 | `:apps:worker:test`、`local` profile起動、Worker起動ログ、ログ安全性を確認した。 | 完了。 |
| #85 | ローカルミドルウェアComposeを作成した。 | PostgreSQL、Elasticsearch、RabbitMQの起動、状態、ログ、停止、Elasticsearch必須プラグインを確認した。 | 完了。 |
| #86 | API / Workerのローカル設定と外部依存疎通を整備した。 | API healthとWorker local health loggerでPostgreSQL、Elasticsearch、RabbitMQの接続を確認した。 | 完了。 |
| #87 | 最小テストと確認コマンドを整備した。 | フロントエンド、API、Worker、Docker Composeの確認コマンドを文書化し、`typecheck` scriptを追加した。 | 完了。 |
| #88 | ローカル開発手順とTODOを実構成へ更新した。 | `.env.example`、環境構築手順、ローカル開発手順、`doc/TODO.md`、フロントエンドAPI接続先設定を更新した。 | 完了。 |

## テストと確認

| 種別 | 内容 | 結果 |
| --- | --- | --- |
| フロントエンド確認 | `npm.cmd run lint`、`npm.cmd run typecheck`、`npm.cmd run build`、`npm.cmd run dev`、HTTP 200確認。 | 成功。 |
| API確認 | `.\gradlew.bat :apps:api:test`、`local` profile起動、`/actuator/health`。 | 成功。 |
| Worker確認 | `.\gradlew.bat :apps:worker:test`、`local` profile起動、Worker起動ログ確認。 | 成功。 |
| Docker Compose確認 | `docker compose config`、`up`、`ps`、PostgreSQL / Elasticsearch / RabbitMQ疎通、ログ、`down`。 | 成功。 |
| API / Worker外部依存確認 | API healthとWorker local health loggerでPostgreSQL、Elasticsearch、RabbitMQを確認。 | 成功。 |
| E2E | 未実行。 | Sprint S0では業務導線が未実装のため対象外。 |

## 残事項

| 項目 | 理由 | 次の扱い |
| --- | --- | --- |
| GitHub Issue / Project更新 | リポジトリ内成果物とは別のProject運用作業。 | #88の残タスクとして、#88本文、親issue #48本文、Project状態を更新する。 |
| 7-Zip実行確認 | WindowsローカルPATHで `7z` が未検出。 | 変換実装またはWorkerコンテナ化時に `SEVENZIP_EXECUTABLE_PATH` を設定して確認する。 |
| DBマイグレーション、業務queue、検索インデックス | Sprint S0は基盤起動と設定確認までを対象にした。 | 後続PBIで業務スキーマ、RabbitMQ listener、Elasticsearchインデックスを実装する。 |

## フィードバック

- 親Issue #48は完了条件が広いため、sub-issue単位で順序づけたことで進捗と完了判定を扱いやすくなった。
- Sprint S0では検索、変換、ジョブ配送の詳細へ踏み込みすぎず、起動、設定、疎通確認、ログ安全性に集中できた。
- API / Workerのテストは外部依存なしで通し、外部依存疎通は明示的なlocal起動確認として分ける方針が有効だった。
- 設計判断を先行するsub-issueでは、[Definition of Done](../../../../doc/05_development/05_definition_of_done.md#設計判断を先行するsub-issueの扱い) に従い、「設計判断完了」と「Issue完了」を分けて扱う。

## ドキュメント更新

- 更新した設計書:
  - `README.md`
  - `doc/05_development/00_project_structure.md`
  - `doc/05_development/03_environment_setup.md`
  - `doc/05_development/04_local_development.md`
  - `doc/05_development/05_definition_of_done.md`
- 更新した開発成果物:
  - `development/scrum/sprints/sprint-s0/planning.md`
  - `development/scrum/sprints/sprint-s0/issue-81-project-structure.md`
  - `development/scrum/sprints/sprint-s0/issue-82-frontend-minimal.md`
  - `development/scrum/sprints/sprint-s0/issue-83-api-minimal.md`
  - `development/scrum/sprints/sprint-s0/issue-84-worker-minimal.md`
  - `development/scrum/sprints/sprint-s0/issue-85-local-middleware-compose.md`
  - `development/scrum/sprints/sprint-s0/issue-86-api-worker-local-dependencies.md`
  - `development/scrum/sprints/sprint-s0/issue-87-minimal-test-commands.md`
  - `development/scrum/sprints/sprint-s0/issue-88-local-development-docs.md`
  - `development/scrum/sprints/sprint-s0/pbi-001-breakdown.md`
  - `development/scrum/sprints/sprint-s0/test-report.md`
  - `development/scrum/sprints/sprint-s0/review.md`
  - `development/scrum/sprints/sprint-s0/retrospective.md`
