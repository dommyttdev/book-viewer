# Issue #84 Spring Boot Workerの最小構成を作成する

## 目的

issue #48 / PBI-001 のsub-issueとして、`apps/worker/` にSpring Boot変換ワーカーの最小構成を作成するための実装入力、受け入れ条件、TDD開始点、Spring Initializr設定値、必要なdependencyを整理する。

この文書はSprint S0の作業成果物である。初期プロジェクト配置後の確認結果、最小起動に必要な調整、実行したコマンド、未実施の外部依存確認を記録する。

## GitHub Issue

- Issue: #84 Spring Boot Workerの最小構成を作成する
- Parent: #48 開発者として、フロントエンド、API、ワーカーをローカルで起動できるようにしたい
- Labels: `type:feature`, `area:worker`

## 受け入れ条件

| 条件 | 判定方法 | 現時点 |
| --- | --- | --- |
| Spring Boot 4.0.6 / Java 25 のWorker最小構成が作成されている。 | `apps/worker/` がGradleサブプロジェクトとして定義され、Worker用Spring Bootアプリケーションクラスが存在する。 | 完了。`settings.gradle.kts` に `include(":apps:worker")` を追加し、`WorkerApplication` を確認した。 |
| APIとは別プロセスまたは別起動モードで起動できる。 | `./gradlew :apps:worker:bootRun --args='--spring.profiles.active=local'` または同等のコマンドでAPIとは別に起動できる。 | 完了。`local` プロファイルで起動ログを確認した。 |
| 起動時にジョブ待機またはWorker起動状態を確認できる。 | 起動ログ、Actuator、または最小の起動状態ログでWorkerが待機状態になったことを確認できる。 | 完了。`WorkerStartupLogger` の起動状態ログを確認した。 |
| 最小テストまたは起動確認コマンドが用意されている。 | `./gradlew :apps:worker:test` が成功し、起動確認コマンドが記録されている。 | 完了。`.\gradlew.bat :apps:worker:test` 成功。 |

## 採用方針

| 項目 | 方針 |
| --- | --- |
| 配置 | `apps/worker/` |
| フレームワーク | Spring Boot 4.0.6 |
| Java | Java 25 toolchain |
| ビルド | ルートGradleマルチプロジェクト / Kotlin DSL |
| Gradle include | `include(":apps:worker")` |
| 起動単位 | APIとは別Spring Bootアプリケーション、別プロセスとして扱う。 |
| Webアプリ種別 | 原則としてHTTP APIを持たない非Webアプリケーションとして開始する。必要になった場合だけActuator HTTP公開のためにWeb依存を追加する。 |
| local profile | `local` プロファイルを用意する。外部依存の詳細設定は #86 で扱う。 |
| 起動状態確認 | 起動ログまたは最小のApplicationRunnerでWorker起動状態を確認する。RabbitMQ実接続待機は #86 以降で扱う。 |
| 業務処理 | Sprint S0では実装しない。アーカイブ展開、WebP変換、サムネイル生成、ジョブ状態遷移は後続PBIで扱う。 |

## Spring Initializr設定値

Spring Initializrで初期プロジェクトを生成する場合は、次の値を指定する。

| 項目 | 設定値 |
| --- | --- |
| Project | Gradle - Kotlin |
| Language | Java |
| Spring Boot | 4.0.6 |
| Group | `com.dommy.manga` |
| Artifact | `worker` |
| Name | `manga-worker` |
| Description | `Conversion worker for the self-scanned book viewer` |
| Package name | `com.dommy.manga.worker` |
| Packaging | Jar |
| Java | 25 |

生成したファイルは `apps/worker/` へ配置する。ルートGradleマルチプロジェクトへ組み込むため、生成後に `settings.gradle.kts` の `include(":apps:worker")` と、必要に応じたルートビルド設定へ統合する。

## Spring Initializr dependencies

WorkerはHTTP APIを提供せず、RabbitMQから変換ジョブを取得してPostgreSQL、ファイル保存領域、7-Zip、画像変換処理を扱う。Sprint S0では起動確認と設定読み込み以外の業務実装は行わないが、後続PBIで使う依存を先に含め、APIとは別の起動単位として整える。

### Initializrで選択する

| Initializr名 | Gradle dependency | 用途 |
| --- | --- | --- |
| Spring Boot Actuator | `org.springframework.boot:spring-boot-starter-actuator` | 起動状態、ヘルス、将来の監視メトリクスの基盤。S0ではHTTP公開を必須にせず、起動状態確認の補助として扱う。 |
| Validation | `org.springframework.boot:spring-boot-starter-validation` | Worker設定値、ジョブパラメータ、ファイルパスなどの検証で使う。 |
| Spring Data JPA | `org.springframework.boot:spring-boot-starter-data-jpa` | PostgreSQL上の変換ジョブ状態、ページ情報、保存メタ情報を更新する永続化基盤。 |
| PostgreSQL Driver | `org.postgresql:postgresql` | PostgreSQL接続用JDBCドライバ。 |
| Flyway Migration | `org.flywaydb:flyway-core` | DBマイグレーション管理。PostgreSQL固有モジュールが必要な生成結果の場合は `org.flywaydb:flyway-database-postgresql` も追加する。 |
| Spring for RabbitMQ | `org.springframework.boot:spring-boot-starter-amqp` | RabbitMQから変換ジョブを取得し、ack、再配送、dead letterを扱うために使う。 |
| Spring Data Elasticsearch | `org.springframework.boot:spring-boot-starter-data-elasticsearch` | 変換完了後の検索更新契機、インデックス更新、必須プラグイン確認の実装候補として使う。 |
| Spring Boot DevTools | `org.springframework.boot:spring-boot-devtools` | ローカル開発時の再起動支援。実行時成果物には含めない開発用依存として扱う。 |

### テスト用

| Initializr名 | Gradle dependency | 用途 |
| --- | --- | --- |
| Spring Boot Test | `org.springframework.boot:spring-boot-starter-*-test` | アプリケーションコンテキスト起動、設定値、Workerコンポーネント、Repository、RabbitMQ連携境界のテスト支援で使う。 |
| Spring Boot Testcontainers | `org.springframework.boot:spring-boot-testcontainers` | Spring BootのService ConnectionとTestcontainers連携で使う。 |
| Testcontainers JUnit Jupiter | `org.testcontainers:testcontainers-junit-jupiter` | JUnit JupiterでTestcontainersを使う。 |
| Testcontainers PostgreSQL | `org.testcontainers:testcontainers-postgresql` | PostgreSQL結合テスト候補。 |
| Testcontainers RabbitMQ | `org.testcontainers:testcontainers-rabbitmq` | RabbitMQ結合テスト候補。 |
| Testcontainers Elasticsearch | `org.testcontainers:testcontainers-elasticsearch` | Elasticsearch結合テスト候補。 |

Spring Initializrの選択肢に細分化されたTestcontainersモジュールがない場合は、生成後に必要なモジュールを `testImplementation` として追加する。

### 生成後に判断する

| 依存関係 | 扱い |
| --- | --- |
| `org.springframework.boot:spring-boot-configuration-processor` | `@ConfigurationProperties` でWorker設定クラスを作る段階で、開発支援用のannotation processorとして追加する。 |
| Spring Web MVC | 初期Workerには追加しない。HTTP APIを持たせず、APIとWorkerの責務分離を保つ。Actuator HTTP公開が必要になった場合に追加を判断する。 |
| Spring Security | 初期Workerには追加しない。WorkerはAPIで検証済みのジョブを処理する前提で、認証認可の入口はAPIに置く。 |
| Spring Session JDBC | 初期Workerには追加しない。WorkerはHTTPセッションを扱わない。 |
| Java Mail Sender | 初期Workerには追加しない。通知やメール送信が必要になった場合は責務境界を再確認してから追加する。 |
| Lombok | 現時点では採用しない。IDEやビルド設定への依存を増やさず、明示的なJavaコードを優先する。 |
| Liquibase | Flywayを採用するため追加しない。 |
| Spring Reactive Web | Workerはキュー駆動の非Webプロセスとして開始する。Reactive前提の要件が出るまでは追加しない。 |

`spring-boot-starter-test` はSpring Initializr生成時のテスト依存として含まれる前提にする。生成結果に含まれない場合は、`testImplementation("org.springframework.boot:spring-boot-starter-test")` を追加する。

## 推奨ディレクトリ構成

```text
apps/worker/
├── build.gradle.kts
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/dommy/manga/worker/
    │   │       └── WorkerApplication.java
    │   └── resources/
    │       ├── application.properties
    │       └── application-local.properties
    └── test/
        └── java/
            └── com/dommy/manga/worker/
                └── WorkerApplicationTests.java
```

生成直後は、業務処理クラスを追加しすぎない。RabbitMQ listener、7-Zip実行、画像変換、ファイル保存、DB更新は後続PBIで責務単位を分けて追加する。

## 初期設定方針

Sprint S0の最小起動では、外部依存の実接続を必須にしない。外部依存接続と環境変数の詳細は #86 で扱う。

`application.properties` では、WorkerをHTTP APIを持たない非Webアプリケーションとして起動するため、次の値を設定する。

```properties
spring.main.web-application-type=none
spring.application.name=manga-worker
management.endpoints.enabled-by-default=true
```

`application-local.properties` では、Sprint S0の最小起動確認で外部依存を必須にしないため、DB / JPA / Flywayの自動設定と外部依存ヘルスチェックを無効化する。

```properties
spring.autoconfigure.exclude=org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration,org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration
spring.flyway.enabled=false
management.health.elasticsearch.enabled=false
management.health.rabbit.enabled=false
debug=false
logging.level.root=INFO
```

Worker固有の設定値は、将来 `@ConfigurationProperties` で型安全に読み込む。

| 設定候補 | 用途 |
| --- | --- |
| `worker.enabled` | Worker処理を起動するかどうか。テストやローカル確認で切り替える。 |
| `worker.concurrency` | 変換ワーカー同時実行数。 |
| `worker.work-dir` | Worker作業ディレクトリ。 |
| `conversion.webp-quality` | WebP品質値。既定値は80。 |
| `conversion.seven-zip-path` | 7-Zip実行ファイルパス。 |
| `conversion.job-timeout` | 1ジョブのタイムアウト。 |

設定名は初期候補であり、#86でAPI / Worker横断の設定命名を整理する。

## TDD / Red開始点

最初のRedは、Workerアプリケーションが存在しない、またはWorkerテストを実行できない状態を確認するところから開始する。

| 観点 | 最初に確認すること | 種別 |
| --- | --- | --- |
| 起動確認 | `./gradlew :apps:worker:test` が、未作成状態では失敗する。 | Red |
| アプリケーションコンテキスト | `@SpringBootTest` の最小テストが成功する。 | Green |
| 別プロセス起動 | `./gradlew :apps:worker:bootRun --args='--spring.profiles.active=local'` でAPIとは別に起動できる。 | 手動 |
| Worker起動状態 | 起動ログで `manga-worker` とWorker待機状態を確認できる。 | 手動 |
| 設定 | `local` プロファイルでWorker固有設定を読める。 | 手動 / 単体 |
| ログ安全性 | 起動ログにシークレット、パスワード、トークン、不要な個人情報が出ない。 | 手動 |

## 確認コマンド案

Windows PowerShellではGradle Wrapper作成後、次を候補にする。

```powershell
.\gradlew.bat :apps:worker:test
.\gradlew.bat :apps:worker:bootRun --args='--spring.profiles.active=local'
```

Linux / macOSでは次を候補にする。

```bash
./gradlew :apps:worker:test
./gradlew :apps:worker:bootRun --args='--spring.profiles.active=local'
```

起動確認では、API用の `:apps:api:bootRun` と同時に起動できること、APIとWorkerのログが別プロセスとして区別できることを確認する。

## 実装タスク

- [x] Red: `apps/worker/` 未作成またはWorkerテスト未定義の状態で、Worker最小テストが実行できないことを確認する。
- [x] Green: Spring Boot 4.0.6 / Java 25 のWorker最小構成を作成し、アプリケーションコンテキスト起動テストを通す。
- [x] Green: APIとは別プロセスとしてWorkerを起動できるようにする。
- [x] Green: 起動ログまたは最小コンポーネントでWorker起動状態を確認できるようにする。
- [x] Refactor: Worker固有の責務と、API / backend-commonへ送るべき責務が混ざっていないことを確認する。
- [x] Document: 実行したコマンド、バージョン、起動状態、未実行項目、ログ安全性確認を `test-report.md` へ追記する。

## 対象外

- RabbitMQへの実接続、キュー宣言、listener実装。
- PostgreSQLへの実接続、DBマイグレーション、Repository実装。
- Elasticsearchへの実接続、インデックス更新。
- アーカイブ展開、WebP変換、サムネイル生成。
- 7-Zip外部プロセス呼び出し。
- 変換ジョブのack、再配送、dead letter、冪等性。
- Docker Composeによるミドルウェア起動。

外部依存の設定名、疎通確認、接続失敗時の扱いは #86 で扱う。最小テストと確認コマンドの横断整理は #87 で扱う。

## 実施結果

- 使用したJava / Gradle / Spring Bootのバージョン: Spring Boot 4.0.6、Java 25.0.1、Gradle Wrapper 9.4.1。
- 作成したGradleサブプロジェクトと主要依存関係: `apps/worker`。Actuator、RabbitMQ、Spring Data Elasticsearch、Spring Data JPA、Flyway、Validation、PostgreSQL、Testcontainersを含む。`settings.gradle.kts` に `include(":apps:worker")` を追加した。
- 実行したテストコマンドと結果: `.\gradlew.bat :apps:worker:test` 成功。初回は生成直後のTestcontainers設定がDockerを要求して失敗したため、S0最小テストでは外部依存を起動しない構成へ修正した。
- 起動コマンド、Worker起動状態、確認したログ: `.\gradlew.bat :apps:worker:bootRun --args='--spring.profiles.active=local'` で起動。`Started WorkerApplication` と `manga-worker started. Waiting for conversion jobs is not enabled in Sprint S0 minimal setup.` を確認した。Workerは常駐するため、確認後に対象プロセスを停止した。
- ログに秘密情報が出力されていないことの確認結果: 起動ログにパスワード、トークン、シークレット、接続文字列の実値は出力されていないことを確認した。
- 未実行の確認と理由: RabbitMQ、PostgreSQL、Elasticsearchへの実接続、7-Zip実行、ジョブ待機listenerは #86 以降または後続PBIで扱うため未実施。
- 更新したドキュメント: `development/scrum/sprints/sprint-s0/issue-84-worker-minimal.md`、`development/scrum/sprints/sprint-s0/test-report.md`。
