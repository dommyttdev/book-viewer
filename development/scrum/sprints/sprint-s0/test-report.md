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
| フロントエンド確認 | `npm.cmd run typecheck` | 成功。#87で `typecheck` scriptを追加し、`tsc --noEmit` で型エラーなしを確認。 |
| フロントエンド確認 | `npm.cmd run build` | 成功。Turbopackでコンパイル、TypeScript確認、`/` と `/_not-found` の静的生成が完了。 |
| フロントエンド確認 | `npm.cmd run dev` | 成功。Next.js 16.2.6 / Turbopackで起動。#87では `http://127.0.0.1:3000` がHTTP 200を返すことを確認し、確認後にプロセスを停止。 |
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
| Docker Compose確認 | `docker compose config` | Red確認では `compose.yaml` 未作成のため `no configuration file provided: not found` で失敗。実装後は成功し、`postgres`、`elasticsearch`、`rabbitmq`、3つのvolume、既定ネットワークが展開されることを確認。Docker設定ファイルへのアクセス拒否警告は継続。 |
| Docker Compose確認 | `docker compose config` | #87で再確認。`postgres`、`elasticsearch`、`rabbitmq`、3つのvolume、既定ネットワークが展開されることを確認。Docker設定ファイルへのアクセス拒否警告は継続。 |
| Docker Compose確認 | `docker compose up -d postgres elasticsearch rabbitmq` | 成功。初回はDocker Desktop Linux engineへ接続できず失敗したが、Docker Desktop起動後に権限付きで再実行して成功。Elasticsearchカスタムイメージのbuild、PostgreSQL / RabbitMQのpull、3サービス起動を確認。 |
| Docker環境確認 | `docker version` | 成功。Docker Desktop 4.38.0、Docker Engine 27.5.1、context `desktop-linux` を確認。 |
| Docker Compose状態確認 | `docker compose ps` | 成功。`manga-postgres`、`manga-elasticsearch`、`manga-rabbitmq` がすべてhealthy。 |
| PostgreSQL確認 | `docker compose exec postgres pg_isready -U manga -d manga` | 成功。`/var/run/postgresql:5432 - accepting connections` を確認。 |
| Elasticsearch確認 | `Invoke-RestMethod http://localhost:9200` | 成功。Elasticsearch 8.17.0のHTTP応答を確認。 |
| Elasticsearchプラグイン確認 | `docker compose exec elasticsearch bin/elasticsearch-plugin list` / `http://localhost:9200/_cat/plugins` | 成功。`analysis-icu` と `analysis-kuromoji` を確認。 |
| RabbitMQ確認 | `docker compose exec rabbitmq rabbitmqctl status` | 成功。RabbitMQ 4.3.0、AMQP 5672、管理UI 15672のlistenerを確認。 |
| Docker Composeログ確認 | `docker compose logs --tail=30 postgres`、`elasticsearch`、`rabbitmq` | 成功。各サービスの起動ログを確認。ログに本番秘密情報、トークン、不要な個人情報は見当たらない。 |
| Docker Compose停止確認 | `docker compose down` | 成功。3コンテナと既定ネットワークが停止、削除された。volumeは削除していない。 |
| API / Worker設定テスト | `.\gradlew.bat :apps:api:test :apps:worker:test` | 成功。APIの `BookStorageProperties`、Workerの `BookStorageProperties` / `ConversionProperties` が外部依存なしのテストでバインドできることを確認。 |
| API local外部依存確認 | `.\gradlew.bat :apps:api:bootRun --args='--spring.profiles.active=local --server.port=18081 --debug=false'` 後に `Invoke-RestMethod http://localhost:18081/actuator/health` | 成功。health詳細で `db=UP`、`elasticsearch=UP`、`rabbit=UP` を確認。PostgreSQL 17.10、Elasticsearch cluster `docker-cluster` green、RabbitMQ 4.3.0。 |
| Worker local外部依存確認 | `.\gradlew.bat :apps:worker:bootRun --args='--spring.profiles.active=local --debug=false'` | 成功。起動ログでPostgreSQL接続、RabbitMQ接続、`manga-worker local dependency health: db=UP, elasticsearch=UP, rabbit=UP` を確認。 |
| 7-Zip確認 | `Get-Command 7z` | 未検出。`SEVENZIP_EXECUTABLE_PATH` で差し替え可能な設定は追加済み。実行確認は7-Zip導入後またはWorkerコンテナ化時に行う。 |
| Docker Compose停止確認 | `docker compose down` | 成功。#86確認で起動したPostgreSQL、Elasticsearch、RabbitMQを停止、削除した。volumeは削除していない。 |

## 未実行のテスト

| テスト | 未実行理由 | 次の扱い |
| --- | --- | --- |
| Spring Boot API起動テスト | APIプロジェクト生成後、`test`、`bootRun`、`/actuator/health` は確認済み。#86で外部依存healthも確認済み。 | 業務API実装時に対象機能のテストを追加する。 |
| Spring Boot Worker外部依存接続確認 | #86でPostgreSQL、RabbitMQ、Elasticsearch接続を確認済み。 | RabbitMQ listener、queue宣言、変換処理は後続PBIで確認する。 |
| Docker Composeミドルウェア起動 | #85の範囲では確認済み。 | API / Workerからの外部依存接続は #86 で確認する。 |
| PostgreSQL接続 / DBマイグレーション | PostgreSQL接続は#86で確認済み。DBマイグレーションはスキーマ未整備のため未実行。 | 後続PBIでFlyway migrationを追加して確認する。 |
| Elasticsearch必須プラグイン確認 | #85の範囲では確認済み。 | 検索実装上の詳細確認はSPIKE-003 / PBI-014で扱う。 |
| RabbitMQ接続確認 | #85の範囲では `rabbitmqctl status` を確認済み。#86でAPI / Workerからの接続も確認済み。 | 業務queueとlistenerは後続PBIで確認する。 |
| 7-Zip実行確認 | WindowsローカルPATHで `7z` が見つからなかったため。 | 7-Zip導入後またはWorkerコンテナ化時に `SEVENZIP_EXECUTABLE_PATH` を設定して確認する。 |
| ログの秘密情報確認 | #86のAPI / Worker local起動ログではパスワード、トークン、本番秘密情報が出ていないことを確認済み。 | 業務処理ログは各機能実装時に確認する。 |

## 確認した異常系

- PowerShellで `npm --version` を実行すると、`npm.ps1` が実行ポリシーによりブロックされる可能性がある。
- Docker / Docker Composeはバージョン取得できるが、Docker設定ファイルへのアクセス拒否警告が出る環境がある。
- Docker daemonへ接続できない場合、`docker compose config` は成功しても `docker compose up`、`docker compose ps`、`docker version` のServer確認は失敗する。Docker Desktop起動後、権限付き実行では `desktop-linux` contextで確認できた。
- `gradle` はグローバルコマンドとして利用できないため、実装時はWrapperを前提にするのが安全である。
- `create-next-app` の実行結果は、当初想定した `app/` 直下ではなく `src/app/` 構成である。Next.jsの最小構成として問題ないため、Sprint S0では生成結果を採用する。
- Next.jsの `next/font/google` はビルド時にGoogle Fonts取得が必要になり、ネットワーク制限下で `npm.cmd run build` が失敗した。#87で外部フォント依存を外し、system font指定へ変更した。
- 開発サーバ確認時に、生成直後テンプレートの `vercel.svg` について画像の縦横比に関するブラウザ警告が出ている。S0の起動基盤としてはブロッカーにしない。

## 残リスク

- GitHub Issue #48のチェックリストは、実装未実施のため未更新。
- GitHub Issue #82のチェックリストは、GitHub Project運用タイミングで更新する。
- GitHub Issue #81のチェックリストは、`doc/05_development/00_project_structure.md` を正本としてREADME、環境構築手順、ローカル開発手順から参照できることをProject運用タイミングで更新する。
- Sprint S0の完了には、#81から#88の実作業、起動確認、テスト、ドキュメント更新が必要。
- Spring Boot APIはSpring Initializr生成物を `apps/api/` に配置済み。生成時のgroup / packageは `com.dommy.manga` / `com.dommy.manga.api` を採用する。
- #83ではルートGradle Wrapperと `settings.gradle.kts` を追加し、`:apps:api:test`、`:apps:api:bootRun`、`/actuator/health` を確認した。8080競合のため、HTTP疎通確認は18080で実施した。
- Elasticsearch必須プラグインの導入方式は、`docker/elasticsearch/Dockerfile` で `analysis-kuromoji` と `analysis-icu` を導入する方針にした。実際のプラグイン導入は `_cat/plugins` で確認済み。
- Docker設定ファイルのアクセス拒否がCompose起動時にも影響する場合、ローカル環境側の権限調整が必要になる。
- #85では `compose.yaml`、`.env.example`、`docker/elasticsearch/Dockerfile` を追加した。`.env.example` は安全なサンプル値のみを含み、実値を含む `.env` はGit管理外とする。PostgreSQL、Elasticsearch、RabbitMQの実コンテナ起動、状態確認、ログ確認、停止確認は完了。
- #86ではAPI / Workerの `local` プロファイルでPostgreSQL、Elasticsearch、RabbitMQ、書籍ファイル保存領域、Worker作業ディレクトリ、7-Zip実行ファイルパス、WebP品質値を設定から扱えるようにした。API healthとWorker local health loggerで外部依存疎通を確認済み。7-ZipはローカルPATH未設定のため実行未確認。
- #87ではフロントエンドの `lint`、`typecheck`、`build`、開発サーバHTTP 200、API / Workerの最小テスト、Docker Compose `config` を確認済み。API / Workerの `local` profile起動とミドルウェア実疎通は#86の確認結果を最小確認コマンドとして引き継ぐ。
- 設計判断を先行するsub-issueの完了扱いは、[Definition of Done](../../../../doc/05_development/05_definition_of_done.md#設計判断を先行するsub-issueの扱い) を参照する。

## 更新したドキュメント

- `development/scrum/sprints/sprint-s0/planning.md`
- `development/scrum/sprints/sprint-s0/issue-81-project-structure.md`
- `development/scrum/sprints/sprint-s0/issue-83-api-minimal.md`
- `development/scrum/sprints/sprint-s0/issue-84-worker-minimal.md`
- `development/scrum/sprints/sprint-s0/issue-85-local-middleware-compose.md`
- `development/scrum/sprints/sprint-s0/issue-86-api-worker-local-dependencies.md`
- `development/scrum/sprints/sprint-s0/issue-87-minimal-test-commands.md`
- `development/scrum/sprints/sprint-s0/pbi-001-breakdown.md`
- `development/scrum/sprints/sprint-s0/test-report.md`
- `development/scrum/sprints/sprint-s0/review.md`
- `development/scrum/sprints/sprint-s0/retrospective.md`
- `README.md`
- `doc/05_development/00_project_structure.md`
- `doc/05_development/03_environment_setup.md`
- `doc/05_development/04_local_development.md`
- `doc/05_development/05_definition_of_done.md`
