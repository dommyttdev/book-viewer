# PBI-001 ユーザーストーリー分解

## ストーリー

- ID: PBI-001 / GitHub Issue #48
- ユーザ: 開発者
- 目的: フロントエンド、API、Workerをローカルで起動できるようにする。
- 価値: 後続PBIを継続的にTDDで実装できるプロジェクト基盤を作る。

## 参照

- 設計書:
  - `doc/03_architecture/02_technology_stack.md`
  - `doc/03_architecture/05_container_diagram.md`
  - `doc/05_development/03_environment_setup.md`
  - `doc/05_development/04_local_development.md`
  - `doc/05_development/05_definition_of_done.md`
- 受入条件:
  - GitHub Issue #48
  - `development/scrum/04_sprint_plan.md` の Sprint 0 完了条件
- API契約:
  - Sprint S0では業務API契約の実装は対象外。ヘルスチェックまたは疎通確認手段のみ対象にする。
- 画面メモ:
  - Sprint S0では本格画面は対象外。Next.jsの最小起動とAPI接続先設定方針のみ対象にする。

## 受入条件

- [ ] プロジェクト構成が決まり、フロントエンド、API、Workerのディレクトリ、ビルドツール、起動単位がREADMEまたは開発手順から分かる。
- [ ] Next.jsフロントエンドの最小構成が作成され、ローカルで開発サーバを起動できる。
- [ ] Spring Boot 4.0.6 / Java 25 のバックエンドAPI最小構成が作成され、ローカルで起動できる。
- [ ] バックエンドAPIにヘルスチェックまたは同等の疎通確認手段があり、起動確認に利用できる。
- [ ] Spring Boot 4.0.6 / Java 25 の変換ワーカー最小構成が作成され、APIとは別プロセスまたは別起動モードとしてローカルで起動できる。
- [ ] PostgreSQL、Elasticsearch、RabbitMQをDocker Composeでローカル起動できる。
- [ ] Docker Composeでミドルウェアの状態確認、ログ確認、停止ができる。
- [ ] APIとWorkerのローカル設定で、PostgreSQL、Elasticsearch、RabbitMQ、書籍ファイル保存領域、Worker作業ディレクトリ、7-Zip実行ファイルパスを環境差分として扱える。
- [ ] Elasticsearch必須プラグインの扱いは技術スタックを参照し、PBI-001で実装する範囲とSPIKE-003 / PBI-014へ委ねる範囲を明記する。
- [ ] WebP品質値の既定値80を設定可能値として扱う方針が、APIまたはWorkerの設定に反映されるか、後続実装で反映する場所が明記されている。
- [ ] フロントエンド、API、Workerについて、最低限のテストまたは起動確認コマンドが用意されている。
- [ ] ローカル起動時のログに、シークレット、パスワード、トークン、不要な個人情報が出力されないことを確認する。
- [ ] `doc/05_development/03_environment_setup.md` と `doc/05_development/04_local_development.md` の仮コマンド、サービス名、ディレクトリ名が実構成に合わせて更新されている。
- [ ] 変更に応じて `doc/TODO.md` の関連項目と本Issueのチェックリストが更新されている。
- [ ] 完了時に、実行した起動確認、テスト、手動確認の結果をIssueまたはPull Requestへ記録している。

## TDD観点

| 観点 | 最初に書くテスト | テスト種別 |
| --- | --- | --- |
| 正常系 | APIアプリケーションがローカルプロファイルで起動し、ヘルスチェックが成功することを先にテストする。 | 単体 / 起動テスト |
| 入力検証 | 設定値としてWebP品質値、保存先、7-Zipパスなどを読み込み、範囲外または空値を拒否する方針を確認する。 | 単体 |
| 権限 | Sprint S0では業務権限は実装しない。将来のAPI / Worker分離を妨げない起動単位を確認する。 | 設計確認 |
| 異常系 | PostgreSQL、RabbitMQ、Elasticsearch、保存領域、7-Zipパスが未設定または到達不能な場合の起動失敗または警告方針を確認する。 | 単体 / 結合 |
| 外部依存 | Docker ComposeでPostgreSQL、Elasticsearch、RabbitMQを起動し、状態確認とログ確認ができることを確認する。 | 結合 / 手動確認 |

## 実装タスク

- [ ] Red: APIヘルスチェックまたはアプリケーションコンテキスト起動確認の失敗テストを追加する。
- [ ] Green: Spring Boot API最小構成を追加し、テストを通す。
- [ ] Refactor: API、Worker、共通設定、インフラ設定の責務境界を見直す。
- [ ] Document: 実構成に合わせてローカル開発手順、環境構築手順、TODO、Issueチェックリストを更新する。

## sub-issueへの分割

| Issue | Red / Green / Documentの焦点 |
| --- | --- |
| #81 | 完了。`issue-81-project-structure.md` で構成とビルドツールを確定し、以後のsub-issueが同じ前提で進められるようにした。 |
| #82 | `issue-82-frontend-minimal.md` で実装入力を整理済み。Next.jsの最小起動、lint、typecheck、build、API接続先設定を確認する。実際の依存関係インストールと起動確認は環境構築担当が実施する。 |
| #83 | `issue-83-api-minimal.md` で実装入力と実施結果を整理済み。Spring Boot API最小構成、ルートGradle統合、最小テスト、bootRun、`/actuator/health` を確認した。 |
| #84 | Workerの起動単位、プロファイル、設定読み込みを確認する。 |
| #85 | PostgreSQL、Elasticsearch、RabbitMQのCompose起動、状態確認、ログ確認、停止を確認する。 |
| #86 | API / Workerの外部依存接続と環境変数差し替えを確認する。 |
| #87 | 各コンポーネントの最小テスト、起動確認、停止確認コマンドを整備する。フロントエンドは `lint`、`typecheck`、`build`、開発サーバ起動確認を含める。 |
| #88 | 実装結果を開発手順、環境構築手順、TODO、Issueへ反映する。フロントエンドのローカルAPI接続先 `NEXT_PUBLIC_API_BASE_URL` は `.env.example` と手順更新で扱う。 |

## 完了メモ

- 実行したテスト: 今回は実装を行わないため未実行。`test-report.md` に環境確認のみ記録する。
- 手動確認: Java、Node.js、Docker、Docker Compose、Gradle、npmの利用可否を確認した。
- 未対応事項: 実際のNext.js / Spring Boot / Docker Compose構成作成、起動確認、テスト追加、Issueチェックリスト更新。
- 更新したドキュメント:
  - `development/scrum/sprints/sprint-s0/planning.md`
  - `development/scrum/sprints/sprint-s0/issue-81-project-structure.md`
  - `development/scrum/sprints/sprint-s0/issue-83-api-minimal.md`
  - `development/scrum/sprints/sprint-s0/pbi-001-breakdown.md`
  - `development/scrum/sprints/sprint-s0/test-report.md`
  - `development/scrum/sprints/sprint-s0/review.md`
  - `development/scrum/sprints/sprint-s0/retrospective.md`
