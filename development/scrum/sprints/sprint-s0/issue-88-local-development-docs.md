# Issue #88 ローカル開発手順とTODOを実装結果に合わせて更新する

## 目的

issue #48 / PBI-001 のsub-issueとして、Sprint S0で作成したフロントエンド、API、Worker、Docker Composeミドルウェアの実構成を、環境構築手順、ローカル開発手順、TODO、親issueへ反映する。

この文書はSprint S0の作業成果物である。実装では、フロントエンドのローカルAPI接続先を `NEXT_PUBLIC_API_BASE_URL` で差し替えられる形にし、`.env.example` と開発手順へ記録する。
最終更新日: 2026-05-16

## GitHub Issue

- Issue: #88 ローカル開発手順とTODOを実装結果に合わせて更新する
- Parent: #48 開発者として、フロントエンド、API、ワーカーをローカルで起動できるようにしたい
- Labels: `type:docs`, `area:infra`

## 受け入れ条件

| 条件 | 判定方法 | 現時点 |
| --- | --- | --- |
| `doc/05_development/03_environment_setup.md` が実構成に合わせて更新されている。 | Sprint S0で確定したNode.js、npm、Gradle Wrapper、Composeサービス名、local profile設定名、7-Zip設定名、WebP品質値を記録する。 | 完了。 |
| `doc/05_development/04_local_development.md` が実構成に合わせて更新されている。 | 実行済みの最小確認コマンド、PowerShell向け `npm.cmd`、API / Worker local起動、health確認、Compose確認を記録する。 | 完了。 |
| フロントエンドのローカルAPI接続先を `NEXT_PUBLIC_API_BASE_URL` で差し替えられることが `.env.example` とローカル開発手順に記録されている。 | `.env.example` に安全なサンプル値を追加し、フロントエンドが `process.env.NEXT_PUBLIC_API_BASE_URL` を読む。 | 完了。 |
| `doc/TODO.md` の関連項目が実態に合わせて更新されている。 | S0で完了したミドルウェアサービス名、Elasticsearch必須プラグイン確認、ローカル起動基盤を未決事項から外す。 | 完了。 |
| issue #48 のチェックリストへ完了状況が反映されている。 | #88完了後に、親issue本文の未完了チェックを実行結果に合わせて更新する。 | GitHub更新時に実施する。 |

## 採用方針

| 項目 | 方針 |
| --- | --- |
| 正本 | リポジトリ構成は `doc/05_development/00_project_structure.md`、確認結果は `test-report.md`、#88固有の判断はこの文書を正本にする。 |
| フロントエンドAPI接続先 | 業務API呼び出し実装前でも、`NEXT_PUBLIC_API_BASE_URL` をローカル設定として読み取れる最小導線を作る。 |
| `.env.example` | 本番秘密情報ではなく、ローカル開発用の安全なサンプル値だけを置く。 |
| TODO | 完了済みのS0基盤事項はTODOから除外または完了済み扱いへ移し、後続で詳細化すべき事項だけを残す。 |
| issue更新 | #88本文は完了条件のチェックと実行結果を追記し、親issue #48は#88と残りの親条件を反映する。 |

## 実装タスク

- [x] Document: #88の作業ドキュメントを作成する。
- [x] Green: フロントエンドが `NEXT_PUBLIC_API_BASE_URL` を設定から読めるようにする。
- [x] Document: `.env.example`、環境構築手順、ローカル開発手順へ実構成を反映する。
- [x] Document: `doc/TODO.md` の関連項目を実態に合わせて更新する。
- [x] Verify: 最小確認コマンドとMarkdown構造を確認する。
- [ ] GitHub: #88本文、親issue #48本文を更新し、#88をcloseする。

## 対象外

- フロントエンドから業務APIを実際に呼び出す画面実装。
- CORS、認証、セッション、APIクライアント層の詳細実装。
- API / Worker / Frontendを含む本番相当Docker Compose統合。
- 7-Zip実行確認。ローカルPATHで未検出のため、後続の変換実装またはWorkerコンテナ化で扱う。

## 確認予定

- `npm.cmd run lint`
- `npm.cmd run typecheck`
- `npm.cmd run build`
- `.\gradlew.bat :apps:api:test :apps:worker:test`
- `docker compose config`
- MarkdownリンクとTODO状態の目視確認

## 実行結果

| 観点 | 結果 |
| --- | --- |
| フロントエンドlint | `npm.cmd run lint` 成功。 |
| フロントエンド型チェック | `npm.cmd run typecheck` 成功。 |
| フロントエンドビルド | `npm.cmd run build` 成功。`NEXT_PUBLIC_API_BASE_URL` を読む最小導線を含めて静的生成まで完了。 |
| API / Worker最小テスト | 初回はGradle配布物取得がネットワーク制限で失敗。権限付き再実行で `.\gradlew.bat :apps:api:test :apps:worker:test` 成功。 |
| Docker Compose設定確認 | `docker compose config` 成功。Docker設定ファイルへのアクセス拒否警告は継続。 |
| Markdown確認 | `doc/05_development/03_environment_setup.md`、`doc/05_development/04_local_development.md`、`doc/TODO.md`、この文書の見出し、リンク、状態を確認。 |

## 更新したファイル

| ファイル | 内容 |
| --- | --- |
| `.env.example` | `NEXT_PUBLIC_API_BASE_URL=http://localhost:18081` を追加。 |
| `apps/frontend/src/lib/runtimeConfig.ts` | `NEXT_PUBLIC_API_BASE_URL` を読み、未設定時は `http://localhost:18081` を使う最小設定を追加。 |
| `apps/frontend/src/app/page.tsx` | フロントエンドがローカルAPI接続先設定を読めることを確認できる表示を追加。 |
| `doc/05_development/03_environment_setup.md` | Sprint S0の実構成、環境変数名、Composeサービス名、確認チェックリストを更新。 |
| `doc/05_development/04_local_development.md` | ローカルAPI接続先、local profile起動、health確認、Compose疎通確認、最小テストを更新。 |
| `doc/TODO.md` | S0で完了したComposeサービス名とElasticsearchプラグイン導入確認を実態に合わせて整理。 |
| `development/scrum/sprints/sprint-s0/test-report.md` | #88の確認結果を追記。 |

