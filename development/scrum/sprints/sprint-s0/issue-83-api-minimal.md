# Issue #83 Spring Boot APIの最小構成を作成する

## 目的

issue #48 / PBI-001 のsub-issueとして、`apps/api/` にSpring Boot APIの最小構成を作成するための実装入力、受け入れ条件、TDD開始点、確認記録欄を整理する。

この文書はSprint S0の作業成果物である。Spring Boot API最小構成、Gradle Wrapper配置、依存関係取得、テスト、local profile起動、ヘルスチェック確認結果をこの文書と `test-report.md` に記録した。

## GitHub Issue

- Issue: #83 Spring Boot APIの最小構成を作成する
- Parent: #48 開発者として、フロントエンド、API、ワーカーをローカルで起動できるようにしたい
- Labels: `type:feature`, `area:api`

## 受け入れ条件

| 条件 | 判定方法 | 現時点 |
| --- | --- | --- |
| Spring Boot 4.0.6 / Java 25 のAPIアプリケーションが作成されている。 | `apps/api/` がGradleサブプロジェクトとして定義され、API用Spring Bootアプリケーションクラスが存在する。 | 完了。Spring Initializr生成物を配置済み。 |
| ローカルでAPIを起動できる。 | `./gradlew :apps:api:bootRun --args='--spring.profiles.active=local'` または同等のコマンドで起動できる。 | 完了。8080競合のため確認時は `--server.port=18080` を指定。 |
| ヘルスチェックまたは同等の疎通確認手段がある。 | `/actuator/health` またはSprint S0用の最小APIでHTTP応答を確認できる。 | 完了。`http://localhost:18080/actuator/health` が `{"groups":["liveness","readiness"],"status":"UP"}` を返す。 |
| 最小テストまたは起動確認コマンドが用意されている。 | `./gradlew :apps:api:test` が成功し、起動確認コマンドが記録されている。 | 完了。`.\gradlew.bat :apps:api:test` 成功。 |

## 採用方針

| 項目 | 方針 |
| --- | --- |
| 配置 | `apps/api/` |
| フレームワーク | Spring Boot 4.0.6 |
| Java | Java 25 toolchain |
| ビルド | ルートGradleマルチプロジェクト / Kotlin DSL |
| Gradle include | `include(":apps:api")` |
| 起動単位 | Workerとは別Spring Bootアプリケーション、別プロセスとして扱う。 |
| local profile | `local` プロファイルを用意する。外部依存の詳細設定は #86 で扱う。 |
| ヘルスチェック | Spring Boot Actuatorの `/actuator/health` を第一候補にする。Actuatorを採用しない場合は、同等の疎通確認APIをSprint S0内で明示する。 |
| 業務API | Sprint S0では実装しない。書籍、認証、検索、閲覧、管理APIは後続PBIで扱う。 |

## Spring Initializr設定値

Spring Initializrで初期プロジェクトを生成する場合は、次の値を指定する。

| 項目 | 設定値 |
| --- | --- |
| Project | Gradle - Kotlin |
| Language | Java |
| Spring Boot | 4.0.6 |
| Group | `com.dommy.manga` |
| Artifact | `api` |
| Name | `manga-api` |
| Description | `Backend API for the self-scanned book viewer` |
| Package name | `com.dommy.manga.api` |
| Packaging | Jar |
| Java | 25 |

生成したファイルは `apps/api/` へ配置する。ルートGradleマルチプロジェクトへ組み込むため、生成後に `settings.gradle.kts` の `include(":apps:api")` と、必要に応じたルートビルド設定へ統合する。

## Spring Initializr dependencies

今後使用する依存関係も、API初期プロジェクト生成時点で追加する。Sprint S0では起動確認とヘルスチェック以外の業務実装は行わないが、後続PBIで使う依存を先に含め、後からビルド構成を大きく動かさないようにする。

### Initializrで選択する

| Initializr名 | Gradle dependency | 用途 |
| --- | --- | --- |
| Spring Web MVC | `org.springframework.boot:spring-boot-starter-webmvc` | HTTP APIと起動確認用のWebアプリケーション基盤。Sprint S0では業務APIではなく疎通確認に使う。 |
| Spring Boot Actuator | `org.springframework.boot:spring-boot-starter-actuator` | `/actuator/health` によるヘルスチェックを提供する。 |
| Validation | `org.springframework.boot:spring-boot-starter-validation` | APIリクエスト、設定値、フォーム値の入力検証で使う。 |
| Spring Security | `org.springframework.boot:spring-boot-starter-security` | 一般ユーザ、管理ユーザ、権限確認、Cookieベース認証で使う。Sprint S0では業務認証を有効化せず、後続PBIで設定する。 |
| Spring Data JPA | `org.springframework.boot:spring-boot-starter-data-jpa` | PostgreSQLを正本データストアとして扱う永続化基盤。 |
| PostgreSQL Driver | `org.postgresql:postgresql` | PostgreSQL接続用JDBCドライバ。 |
| Flyway Migration | `org.flywaydb:flyway-core` | DBマイグレーション管理。PostgreSQL固有モジュールが必要な生成結果の場合は `org.flywaydb:flyway-database-postgresql` も追加する。 |
| Spring for RabbitMQ | `org.springframework.boot:spring-boot-starter-amqp` | APIから変換ジョブをRabbitMQへ投入するために使う。 |
| Spring Data Elasticsearch | `org.springframework.boot:spring-boot-starter-data-elasticsearch` | 検索API、インデックス作成、Elasticsearch必須プラグイン確認の実装候補として使う。 |
| Java Mail Sender | `org.springframework.boot:spring-boot-starter-mail` | 会員登録、ログイン2段階認証、パスワードリセットのメール送信境界で使う。 |
| Spring Session JDBC | `org.springframework.boot:spring-boot-starter-session-jdbc` | サーバ側セッションをPostgreSQLへ保存する基盤として使う。 |
| Spring Boot DevTools | `org.springframework.boot:spring-boot-devtools` | ローカル開発時の再起動支援。実行時成果物には含めない開発用依存として扱う。 |

### テスト用

| Initializr名 | Gradle dependency | 用途 |
| --- | --- | --- |
| Spring Boot Test | `org.springframework.boot:spring-boot-starter-*-test` | アプリケーションコンテキスト起動、Controller、サービス、Repository、各starterのテスト支援で使う。 |
| Spring Boot Testcontainers | `org.springframework.boot:spring-boot-testcontainers` | Spring BootのService ConnectionとTestcontainers連携で使う。 |
| Testcontainers JUnit Jupiter | `org.testcontainers:testcontainers-junit-jupiter` | JUnit JupiterでTestcontainersを使う。 |
| Testcontainers PostgreSQL | `org.testcontainers:testcontainers-postgresql` | PostgreSQL結合テスト候補。 |
| Testcontainers RabbitMQ | `org.testcontainers:testcontainers-rabbitmq` | RabbitMQ結合テスト候補。 |
| Testcontainers Elasticsearch | `org.testcontainers:testcontainers-elasticsearch` | Elasticsearch結合テスト候補。 |

Spring Initializrの選択肢に細分化されたTestcontainersモジュールがない場合は、生成後に必要なモジュールを `testImplementation` として追加する。

### 生成後に判断する

| 依存関係 | 扱い |
| --- | --- |
| `org.springframework.boot:spring-boot-configuration-processor` | `@ConfigurationProperties` を使う設定クラスを作る段階で、開発支援用のannotation processorとして追加する。 |
| Lombok | 現時点では採用しない。IDEやビルド設定への依存を増やさず、明示的なJavaコードを優先する。 |
| Liquibase | Flywayを採用するため追加しない。 |
| Spring Reactive Web | APIは通常のServletベースで開始する。Reactive前提の要件が出るまでは追加しない。 |
| OAuth2 Client / Resource Server | 初期認証はメールアドレス、パスワード、サーバ側セッションで進めるため追加しない。 |

`spring-boot-starter-test` はSpring Initializr生成時のテスト依存として含まれる前提にする。生成結果に含まれない場合は、`testImplementation("org.springframework.boot:spring-boot-starter-test")` を追加する。

## 推奨ディレクトリ構成

```text
apps/api/
├── build.gradle.kts
└── src/
    ├── main/
    │   ├── java/
│   │   └── com/dommy/manga/api/
    │   │       └── MangaApiApplication.java
    │   └── resources/
    │       ├── application.yml
    │       └── application-local.yml
    └── test/
        └── java/
            └── com/dommy/manga/api/
                └── MangaApiApplicationTests.java
```

パッケージ名は実装時に確定する。既存ドキュメントにパッケージ方針が追加された場合はそちらを優先する。

## TDD / Red開始点

最初のRedは、APIアプリケーションが存在しない、または起動確認テストを実行できない状態を確認するところから開始する。

| 観点 | 最初に確認すること | 種別 |
| --- | --- | --- |
| 起動確認 | `./gradlew :apps:api:test` が、未作成状態では失敗する。 | Red |
| アプリケーションコンテキスト | `@SpringBootTest` の最小テストが成功する。 | Green |
| ヘルスチェック | `/actuator/health` または同等APIが起動中に成功する。 | 手動 / 結合 |
| 設定 | `local` プロファイルで起動できる。 | 手動 |
| ログ安全性 | 起動ログにシークレット、パスワード、トークン、不要な個人情報が出ない。 | 手動 |

## 確認コマンド案

Windows PowerShellではGradle Wrapper作成後、次を候補にする。

```powershell
.\gradlew.bat :apps:api:test
.\gradlew.bat :apps:api:bootRun --args='--spring.profiles.active=local'
```

Linux / macOSでは次を候補にする。

```bash
./gradlew :apps:api:test
./gradlew :apps:api:bootRun --args='--spring.profiles.active=local'
```

起動後の疎通確認は、採用したヘルスチェック方式に合わせて記録する。

```bash
curl http://localhost:8080/actuator/health
```

ポート番号はSpring Boot既定値の `8080` を初期候補にする。競合する場合は `application-local.yml` または起動引数で差し替え、実際の値をこの文書と `test-report.md` に記録する。

## 実装タスク

- [x] Red: `apps/api/` 未作成またはAPIテスト未定義の状態で、API最小テストが実行できないことを確認する。
- [x] Green: Spring Boot 4.0.6 / Java 25 のAPI最小構成を作成し、アプリケーションコンテキスト起動テストを通す。
- [x] Green: ヘルスチェックまたは同等APIでHTTP疎通を確認できるようにする。
- [x] Refactor: API固有の責務と、Worker / backend-commonへ送るべき責務が混ざっていないことを確認する。
- [x] Document: 実行したコマンド、バージョン、起動URL、未実行項目、ログ安全性確認を `test-report.md` へ追記する。

## 対象外

- 書籍、認証、認可、検索、閲覧、管理APIの実装。
- PostgreSQL、Elasticsearch、RabbitMQへの実接続。
- DBマイグレーション、永続化、リポジトリ層。
- Workerの起動構成。
- Docker Composeによるミドルウェア起動。

外部依存の設定名、疎通確認、接続失敗時の扱いは #86 で扱う。最小テストと確認コマンドの横断整理は #87 で扱う。

## 実施結果記録欄

実環境構築後、次を追記する。

- 使用したJava / Gradle / Spring Bootのバージョン: Spring Boot 4.0.6、Java 25、Gradle Wrapper 9.4.1。
- 作成したGradleサブプロジェクトと主要依存関係: `apps/api`。Spring Web MVC、Actuator、Validation、Security、JPA、Flyway、PostgreSQL、RabbitMQ、Elasticsearch、Mail、Session JDBC、Testcontainersを含む。
- 実行したテストコマンドと結果: `.\gradlew.bat :apps:api:test` 成功。初回はGradle配布物と依存関係取得のためネットワーク許可が必要だった。
- 起動コマンド、起動URL、ヘルスチェックURL、HTTP応答: `.\gradlew.bat :apps:api:bootRun --args="--spring.profiles.active=local --server.port=18080 --debug=false"` で起動。`http://localhost:18080/actuator/health` が `{"groups":["liveness","readiness"],"status":"UP"}` を返した。
- ログに秘密情報が出力されていないことの確認結果: `generated security password`、`token`、`secret`、`PasswordConfigured` の実値出力がないことを確認した。S0の `local` 単体起動ではSecurity自動設定を除外する。
- 未実行の確認と理由: DBマイグレーション、業務API、認証、RabbitMQへの業務ジョブ投入、Elasticsearchインデックス作成は後続PBIで扱う。外部依存の基本疎通は #86 で確認済み。
- 更新したドキュメント: `development/scrum/sprints/sprint-s0/issue-83-api-minimal.md`、`development/scrum/sprints/sprint-s0/test-report.md`。
