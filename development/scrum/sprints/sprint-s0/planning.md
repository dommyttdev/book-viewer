# Sprint S0 プランニング

## スプリント

- Sprint: S0
- 期間: 2026-05-15 から 2026-05-16 まで。
- スプリントゴール: 後続PBIをTDDで継続実装できるよう、フロントエンド、API、Worker、ローカルミドルウェアの最小基盤を構築するための作業順序と完了判定を明確にする。

## GitHub Project / Issue

- GitHub Project: Sprint S0
- 親Issue: #48 開発者として、フロントエンド、API、ワーカーをローカルで起動できるようにしたい
- 対象PBI: PBI-001 プロジェクト基盤を作る
- sub-issue: #81, #82, #83, #84, #85, #86, #87, #88

## 対象PBI

| PBI | 内容 | 選定理由 | 受入条件 | TDD開始観点 |
| --- | --- | --- | --- | --- |
| PBI-001 | プロジェクト基盤を作る | PBI-002以降の認証、アップロード、変換、検索、閲覧すべての前提になる。 | issue #48 の受け入れ条件を正とする。 | APIヘルスチェックまたはアプリケーションコンテキスト起動確認を最初に失敗させる。 |

## sub-issue別の作業順序

| Issue | 作業 | 先行関係 | 完了条件 |
| --- | --- | --- | --- |
| #81 | プロジェクト構成とビルド方針を確定する。 | 最初に実施する。 | フロントエンド、API、Workerのディレクトリ、ビルドツール、起動単位が決まっている。 |
| #82 | Next.jsフロントエンドの最小構成を作成する。 | #81の構成決定後。 | 完了。開発サーバ起動、lint、typecheck、build、HTTP 200、API URL設定方針を確認した。 |
| #83 | Spring Boot APIの最小構成を作成する。 | #81の構成決定後。 | 完了。Java 25 / Spring Boot 4.0.6 のAPI起動、ヘルスチェック、最小テストを確認した。 |
| #84 | Spring Boot Workerの最小構成を作成する。 | #81と#83のバックエンド方針決定後。 | 完了。APIとは別プロセスとして起動でき、Worker専用設定と起動ログを確認した。 |
| #85 | ローカルミドルウェアのDocker Composeを用意する。 | #81と並行可能。 | 完了。PostgreSQL、Elasticsearch、RabbitMQの起動、状態確認、ログ確認、停止を確認した。 |
| #86 | APIとWorkerのローカル設定と外部依存の疎通確認を整える。 | #83、#84、#85の後。 | 完了。PostgreSQL、Elasticsearch、RabbitMQ、保存領域、Worker作業ディレクトリ、7-Zipパス、WebP品質値を環境差分として扱える。 |
| #87 | 最小テストと確認コマンドを整備する。 | #82から#86の実装に合わせて実施する。 | 完了。フロントエンド、API、Worker、ミドルウェアの最小確認コマンドを記録した。 |
| #88 | ローカル開発手順とTODOを実装結果に合わせて更新する。 | 最後に実施する。 | リポジトリ内成果物は完了。`doc/05_development/03_environment_setup.md`、`doc/05_development/04_local_development.md`、`doc/TODO.md`、`.env.example`、フロントエンドの `NEXT_PUBLIC_API_BASE_URL` を実構成へ反映した。GitHub Issue / Project更新は #88 の残タスクとして扱う。 |

## タスク分解

| タスク | 種別 | 担当 | 完了条件 |
| --- | --- | --- | --- |
| PBI-001の親Issueとsub-issueの関係を確認する。 | ドキュメント | シニアアーキテクト | 完了。issue #48と#81から#88の責務をこの計画に反映した。 |
| #81のプロジェクト構成とビルド方針を決定する。 | ドキュメント | シニアアーキテクト | 完了。`issue-81-project-structure.md` にディレクトリ、起動単位、ビルドツール、後続Issueへの入力を記録した。 |
| #82のNext.js最小構成を作成し、確認する。 | 実装 / ドキュメント | シニアアーキテクト | 完了。`apps/frontend` を作成し、`lint`、`typecheck`、`build`、`dev`、HTTP 200を確認した。 |
| #83のSpring Boot API最小構成を作成し、確認する。 | 実装 / ドキュメント | シニアアーキテクト | 完了。`apps/api` を追加し、最小テスト、`local` 起動、`/actuator/health` を確認した。 |
| #84のSpring Boot Worker最小構成を確認する。 | 実装 / ドキュメント | シニアアーキテクト | 完了。`apps/worker` をGradleサブプロジェクトとして追加し、最小テスト、`local` 起動、Worker起動状態ログを確認した。結果は `issue-84-worker-minimal.md` と `test-report.md` に記録した。 |
| Sprint S0の作業順序を定義する。 | ドキュメント | シニアアーキテクト | 完了。先行関係、並行可能な作業、完了条件を明確化した。 |
| PBI-001のTDD分解を作成する。 | ドキュメント | シニアアーキテクト | 完了。`pbi-001-breakdown.md` にテスト観点とRed / Green / Refactor / Documentの進捗を記録した。 |
| 環境確認結果を記録する。 | ドキュメント | シニアアーキテクト | 完了。ローカル前提確認、実行結果、注意点を `test-report.md` に記録した。 |
| #87の最小テストと確認コマンドを整備する。 | 実装 / ドキュメント | シニアアーキテクト | フロントエンド、API、Worker、Docker Composeの最小確認コマンドが `issue-87-minimal-test-commands.md` とローカル開発手順へ記録され、フロントエンドの `typecheck` scriptが実行できる。 |
| #88のローカル開発手順とTODOを更新する。 | 実装 / ドキュメント | シニアアーキテクト | 完了。`issue-88-local-development-docs.md`、`.env.example`、環境構築手順、ローカル開発手順、TODOへ実構成を反映した。 |
| 実装担当へ渡すDoDを整理する。 | ドキュメント | シニアアーキテクト | 完了。受入条件、ログ、安全性、ドキュメント更新、後続PBIへの引き継ぎが追跡可能である。 |

## リスク

| リスク | 対応 |
| --- | --- |
| Sprint S0の親Issueが大きく、1回の作業で完了判定しにくい。 | #81から#88を縦に順序づけ、親Issueはsub-issue完了の集約として扱う。 |
| Spring Boot 4.0.6 / Java 25 の組み合わせは実装時点の生成方法や依存関係取得に左右される。 | #81でビルド方針を先に確定し、生成結果を `doc/05_development/03_environment_setup.md` と `04_local_development.md` へ反映する。 |
| Elasticsearch必須プラグインの扱いをS0で作り込みすぎると検索実装に踏み込む。 | S0ではDocker Composeまたは手順に必須プラグイン前提を残し、インデックス設計と詳細確認はSPIKE-003 / PBI-014へ委ねる範囲を明記する。 |
| RabbitMQのack、再配送、DLQ、冪等性までS0で実装するとPBI-007と重複する。 | S0では接続設定と起動確認までに絞り、配送詳細はPBI-007で扱う。 |
| 7-ZipとWebP変換をS0で実装するとPBI-010以降と重複する。 | S0では設定項目と実行ファイルパスの扱いだけを定義し、展開と変換の実装は後続PBIへ送る。 |
| PowerShell環境では `npm.ps1` が実行ポリシーでブロックされる場合がある。 | 実装時のローカル手順では `npm.cmd` の利用または実行ポリシーの扱いを明記する。 |
| Docker設定ファイルにアクセスできない環境ではCompose確認が失敗する。 | 実装時の確認結果にDocker Desktop設定、権限、代替確認手順を残す。 |

## 確認結果

- フロントエンド: `npm.cmd run lint`、`npm.cmd run typecheck`、`npm.cmd run build`、`npm.cmd run dev`、HTTP 200を確認した。
- API: `.\gradlew.bat :apps:api:test`、`local` profile起動、`/actuator/health` を確認した。
- Worker: `.\gradlew.bat :apps:worker:test`、`local` profile起動、Worker起動ログを確認した。
- Docker Compose: `docker compose config`、`up`、`ps`、PostgreSQL / Elasticsearch / RabbitMQ疎通、ログ、`down` を確認した。
- API / Worker外部依存: API healthとWorker local health loggerでPostgreSQL、Elasticsearch、RabbitMQを確認した。
- E2E: Sprint S0では業務導線が未実装のため未実行。
- 更新した設計書: `doc/05_development/03_environment_setup.md`、`doc/05_development/04_local_development.md`、`doc/TODO.md`。

## 作業範囲の結果

Sprint S0では、後続PBIの前提となるフロントエンド、API、Worker、ローカルミドルウェア、ローカル設定、最小確認コマンド、開発手順を整備した。業務API、認証、アップロード、変換ジョブ、検索インデックス、7-Zip実行確認は後続PBIで扱う。
