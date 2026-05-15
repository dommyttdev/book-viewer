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
| GitHub確認 | `gh issue view 83 --repo dommyttdev/book-viewer` | issue #83の本文、受入条件、Project status Todoを確認。実際の環境構築は担当者が実施する前提で、Sprint S0の実装入力を整理する。 |
| GitHub確認 | `gh issue view 84 --repo dommyttdev/book-viewer` | issue #84の本文、受入条件、ラベル `type:feature` / `area:worker` を確認。実際の環境構築は担当者が実施する前提で、Spring Initializr設定値と依存関係を整理する。 |
| 設計確認 | #81プロジェクト構成とビルド方針 | `apps/frontend`、`apps/api`、`apps/worker`、`libs/backend-common`、Gradle Wrapper、npm、API / Worker別プロセス方針を決定。 |
| フロントエンド確認 | `npx create-next-app@latest . --typescript --eslint --app --src-dir false --import-alias "@/*" --use-npm` | 成功。`apps/frontend/` にNext.js 16.2.6 / React 19.2.4 / TypeScript構成を作成。実際の構成は `src/app/`。 |
| フロントエンド確認 | `npm.cmd run lint` | 成功。ESLintエラーなし。 |
| フロントエンド確認 | `npm.cmd run build` | 成功。Turbopackでコンパイル、TypeScript確認、`/` と `/_not-found` の静的生成が完了。 |
| フロントエンド確認 | `npm.cmd run dev` | 成功。Next.js 16.2.6 / Turbopackで起動。Local URLは `http://localhost:3000`、Network URLは `http://192.168.0.10:3000`。 |
| フロントエンド確認 | `GET /` | 成功。`GET / 200` を確認。 |
| フロントエンド確認 | `git check-ignore -v apps\frontend\node_modules apps\frontend\.next apps\frontend\next-env.d.ts` | 成功。`node_modules/`、`.next/`、`next-env.d.ts` は `apps/frontend/.gitignore` でignore済み。 |
| API確認 | `.\gradlew.bat :apps:api:test` | 成功。初回はGradle配布物と依存関係取得のためネットワーク許可が必要だった。`ApiApplicationTests.contextLoads` はTestcontainersを起動しない最小コンテキスト確認として実行。 |
| API確認 | `.\gradlew.bat :apps:api:bootRun --args="--spring.profiles.active=local --server.port=18080 --debug=false"` | 成功。8080は既存プロセスと競合したため、確認時は18080を使用。Tomcat started on port 18080 と Started ApiApplication を確認。 |
| API確認 | `Invoke-RestMethod http://localhost:18080/actuator/health` | 成功。`{"groups":["liveness","readiness"],"status":"UP"}` を確認。 |
| APIログ確認 | 起動ログの秘密情報確認 | 成功。`generated security password`、`token`、`secret`、`PasswordConfigured` の実値出力がないことを確認。確認後、bootRun確認用プロセスを停止。 |
| Worker確認 | `.\gradlew.bat :apps:worker:dependencies --configuration testRuntimeClasspath` | 成功。issue #84で定義したWorker用依存関係が解決できることを確認。 |
| Worker確認 | `.\gradlew.bat :apps:worker:compileTestJava` | 成功。生成されたWorkerテストコードのコンパイルに必要な依存関係が揃っていることを確認。 |
| Worker確認 | `.\gradlew.bat :apps:worker:test` | 成功。初回は生成直後の `@Import(TestcontainersConfiguration.class)` がDockerを要求して失敗したため、S0最小テストでは外部依存を起動しない構成へ修正した。 |
| Worker確認 | `.\gradlew.bat :apps:worker:bootRun --args='--spring.profiles.active=local'` | 成功。`Started WorkerApplication` と `manga-worker started. Waiting for conversion jobs is not enabled in Sprint S0 minimal setup.` を確認。Workerは常駐するため、確認後に対象プロセスを停止した。 |
| Workerログ確認 | 起動ログの秘密情報確認 | 成功。パスワード、トークン、シークレット、接続文字列の実値出力がないことを確認。 |

## 未実行のテスト

| テスト | 未実行理由 | 次の扱い |
| --- | --- | --- |
| Spring Boot API起動テスト | APIプロジェクト生成後、`test`、`bootRun`、`/actuator/health` は確認済み。 | 外部依存接続は #86 で確認する。 |
| Spring Boot Worker外部依存接続確認 | S0のWorker最小確認では外部依存を必須にしないため。 | #86でPostgreSQL、RabbitMQ、Elasticsearch接続を確認する。 |
| Docker Composeミドルウェア起動 | 今回は実際の基盤構築を行わない依頼のため。 | #85でPostgreSQL、Elasticsearch、RabbitMQを確認する。 |
| PostgreSQL接続 / DBマイグレーション | 実装構成とマイグレーションツール未確定のため。 | #81で方針確定後、#86または#87で確認する。 |
| Elasticsearch必須プラグイン確認 | Compose構成未作成のため。 | #85で起動確認、SPIKE-003 / PBI-014で詳細確認する。 |
| RabbitMQ接続確認 | Compose構成未作成のため。 | #85 / #86で確認する。 |
| ログの秘密情報確認 | 実アプリケーションログ未生成のため。 | #87で起動確認時に確認する。 |

## 確認した異常系

- PowerShellで `npm --version` を実行すると、`npm.ps1` が実行ポリシーによりブロックされる可能性がある。
- Docker / Docker Composeはバージョン取得できるが、Docker設定ファイルへのアクセス拒否警告が出る環境がある。
- `gradle` はグローバルコマンドとして利用できないため、実装時はWrapperを前提にするのが安全である。
- `create-next-app` の実行結果は、当初想定した `app/` 直下ではなく `src/app/` 構成である。Next.jsの最小構成として問題ないため、Sprint S0では生成結果を採用する。
- `package.json` には `typecheck` script が未追加である。必要に応じて `tsc --noEmit` を追加する。
- 開発サーバ確認時に、生成直後テンプレートの `vercel.svg` について画像の縦横比に関するブラウザ警告が出ている。S0の起動基盤としてはブロッカーにしない。

## 残リスク

- GitHub Issue #48のチェックリストは、実装未実施のため未更新。
- GitHub Issue #82のチェックリストは、GitHub Project運用タイミングで更新する。
- GitHub Issue #81のチェックリストは、`doc/05_development/00_project_structure.md` を正本としてREADME、環境構築手順、ローカル開発手順から参照できることをProject運用タイミングで更新する。
- Sprint S0の完了には、#81から#88の実作業、起動確認、テスト、ドキュメント更新が必要。
- Spring Boot APIはSpring Initializr生成物を `apps/api/` に配置済み。生成時のgroup / packageは `com.dommy.manga` / `com.dommy.manga.api` を採用する。
- #83ではルートGradle Wrapperと `settings.gradle.kts` を追加し、`:apps:api:test`、`:apps:api:bootRun`、`/actuator/health` を確認した。8080競合のため、HTTP疎通確認は18080で実施した。
- Elasticsearch必須プラグインの導入方式は、S0ではCompose手順に前提を残し、検索実装の詳細とは分離する必要がある。
- Docker設定ファイルのアクセス拒否がCompose起動時にも影響する場合、ローカル環境側の権限調整が必要になる。
- 設計判断を先行するsub-issueの完了扱いは、[Definition of Done](../../../../doc/05_development/05_definition_of_done.md#設計判断を先行するsub-issueの扱い) を参照する。

## 更新したドキュメント

- `development/scrum/sprints/sprint-s0/planning.md`
- `development/scrum/sprints/sprint-s0/issue-81-project-structure.md`
- `development/scrum/sprints/sprint-s0/issue-83-api-minimal.md`
- `development/scrum/sprints/sprint-s0/issue-84-worker-minimal.md`
- `development/scrum/sprints/sprint-s0/pbi-001-breakdown.md`
- `development/scrum/sprints/sprint-s0/test-report.md`
- `development/scrum/sprints/sprint-s0/review.md`
- `development/scrum/sprints/sprint-s0/retrospective.md`
- `README.md`
- `doc/05_development/00_project_structure.md`
- `doc/05_development/03_environment_setup.md`
- `doc/05_development/04_local_development.md`
- `doc/05_development/05_definition_of_done.md`
