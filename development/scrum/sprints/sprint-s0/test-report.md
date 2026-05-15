# Sprint S0 テスト結果

## 実行した確認

| 種別 | コマンドまたは確認内容 | 結果 |
| --- | --- | --- |
| 環境確認 | `java --version` | 成功。Java 25.0.1 が利用可能。 |
| 環境確認 | `node --version` | 成功。Node.js v22.14.0 が利用可能。 |
| 環境確認 | `npm --version` | PowerShellの実行ポリシーにより `npm.ps1` がブロックされた。実装時は `npm.cmd` 利用または手順への注意書きが必要。 |
| 環境確認 | `docker --version` | Docker 27.5.1 を確認。ただし `C:\Users\dommy\.docker\config.json` へのアクセス拒否警告あり。 |
| 環境確認 | `docker compose version` | Docker Compose v2.32.4 を確認。ただしDocker設定ファイルへのアクセス拒否警告あり。 |
| 環境確認 | `gradle --version` | 未導入またはPATH未設定。#81ではGradle Wrapper採用を決定済みのため、実装時はリポジトリにWrapperを含める。 |
| ドキュメント確認 | `development/README.md`、`development/01_development_cycle.md`、`development/scrum/04_sprint_plan.md`、`development/tdd/02_test_matrix.md` | Sprint S0の対象がPBI-001であり、TDD分解とスプリント成果物を残す運用であることを確認。 |
| GitHub確認 | `gh issue view 48 --json ...` | issue #48の本文、受入条件、sub-issue #81から#88、Project status Todoを確認。 |
| GitHub確認 | `gh issue view 81 --json ...` | issue #81の本文、受入条件、Project status Todoを確認。 |
| 設計確認 | #81プロジェクト構成とビルド方針 | `apps/frontend`、`apps/api`、`apps/worker`、`libs/backend-common`、Gradle Wrapper、npm、API / Worker別プロセス方針を決定。 |

## 未実行のテスト

| テスト | 未実行理由 | 次の扱い |
| --- | --- | --- |
| Next.js開発サーバ起動 | 今回は実際の基盤構築を行わない依頼のため。 | #82で実施し、結果をissueまたはPRへ記録する。 |
| Spring Boot API起動テスト | 今回は実際の基盤構築を行わない依頼のため。 | #83でRedから開始する。 |
| Spring Boot Worker起動確認 | 今回は実際の基盤構築を行わない依頼のため。 | #84で実施する。 |
| Docker Composeミドルウェア起動 | 今回は実際の基盤構築を行わない依頼のため。 | #85でPostgreSQL、Elasticsearch、RabbitMQを確認する。 |
| PostgreSQL接続 / DBマイグレーション | 実装構成とマイグレーションツール未確定のため。 | #81で方針確定後、#86または#87で確認する。 |
| Elasticsearch必須プラグイン確認 | Compose構成未作成のため。 | #85で起動確認、SPIKE-003 / PBI-014で詳細確認する。 |
| RabbitMQ接続確認 | Compose構成未作成のため。 | #85 / #86で確認する。 |
| ログの秘密情報確認 | 実アプリケーションログ未生成のため。 | #87で起動確認時に確認する。 |

## 確認した異常系

- PowerShellで `npm --version` を実行すると、`npm.ps1` が実行ポリシーによりブロックされる可能性がある。
- Docker / Docker Composeはバージョン取得できるが、Docker設定ファイルへのアクセス拒否警告が出る環境がある。
- `gradle` はグローバルコマンドとして利用できないため、実装時はWrapperを前提にするのが安全である。

## 残リスク

- GitHub Issue #48のチェックリストは、実装未実施のため未更新。
- GitHub Issue #81のチェックリストは、`doc/05_development/00_project_structure.md` を正本としてREADME、環境構築手順、ローカル開発手順から参照できることをProject運用タイミングで更新する。
- Sprint S0の完了には、#81から#88の実作業、起動確認、テスト、ドキュメント更新が必要。
- Spring Boot 4.0.6 / Java 25 のプロジェクト生成方法、Gradleの正確なバージョン、Next.jsの正確なバージョンは未確定。ビルドツールはGradle Wrapper、フロントエンドのパッケージマネージャはnpmに決定済み。
- Elasticsearch必須プラグインの導入方式は、S0ではCompose手順に前提を残し、検索実装の詳細とは分離する必要がある。
- Docker設定ファイルのアクセス拒否がCompose起動時にも影響する場合、ローカル環境側の権限調整が必要になる。
- 設計判断を先行するsub-issueの完了扱いは、[Definition of Done](../../../../doc/05_development/05_definition_of_done.md#設計判断を先行するsub-issueの扱い) を参照する。

## 更新したドキュメント

- `development/scrum/sprints/sprint-s0/planning.md`
- `development/scrum/sprints/sprint-s0/issue-81-project-structure.md`
- `development/scrum/sprints/sprint-s0/pbi-001-breakdown.md`
- `development/scrum/sprints/sprint-s0/test-report.md`
- `development/scrum/sprints/sprint-s0/review.md`
- `development/scrum/sprints/sprint-s0/retrospective.md`
- `README.md`
- `doc/05_development/00_project_structure.md`
- `doc/05_development/03_environment_setup.md`
- `doc/05_development/04_local_development.md`
- `doc/05_development/05_definition_of_done.md`
