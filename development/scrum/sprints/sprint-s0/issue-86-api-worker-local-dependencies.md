# Issue #86 APIとWorkerのローカル設定と外部依存の疎通確認を整える

## 目的

issue #48 / PBI-001 のsub-issueとして、APIとWorkerがローカル環境でPostgreSQL、Elasticsearch、RabbitMQ、書籍ファイル保存領域、Worker作業ディレクトリ、7-Zip実行ファイル、WebP品質値を設定から扱えるようにするための実装入力、受け入れ条件、確認観点を整理する。

この文書はSprint S0の作業成果物である。`apps/api` と `apps/worker` の `local` プロファイル設定を更新し、Docker Composeで起動したローカルミドルウェアとの疎通確認結果をこの文書と `test-report.md` に記録した。

最終更新日: 2026-05-16

## GitHub Issue

- Issue: #86 APIとWorkerのローカル設定と外部依存の疎通確認を整える
- Parent: #48 開発者として、フロントエンド、API、ワーカーをローカルで起動できるようにしたい
- Labels: `type:feature`, `area:api`, `area:worker`, `area:infra`

## 受け入れ条件

| 条件 | 判定方法 | 現時点 |
| --- | --- | --- |
| PostgreSQL接続情報をローカル設定で差し替えられる。 | API / Workerの `local` プロファイルでJDBC URL、ユーザ、パスワードを環境変数または既定値から読める。 | 完了。API / WorkerともにPostgreSQL接続を確認。 |
| Elasticsearch接続情報をローカル設定で差し替えられる。 | API / Workerの `local` プロファイルでElasticsearch URLを環境変数または既定値から読める。 | 完了。API healthとWorker local health loggerで `UP` を確認。 |
| RabbitMQ接続情報をローカル設定で差し替えられる。 | API / Workerの `local` プロファイルでhost、port、user、passwordを環境変数または既定値から読める。 | 完了。API healthとWorker local health loggerで `UP` を確認。 |
| 書籍ファイル保存領域、Worker作業ディレクトリ、7-Zip実行ファイルパスを設定で扱える。 | API / Workerそれぞれの責務に応じた設定項目が `application.properties` または `application-local.properties` に定義される。 | 完了。型付き設定とテストを追加。7-ZipはPATH未設定のため実行確認は未完了。 |
| ローカル起動時の疎通確認方法が記録されている。 | API / Worker、PostgreSQL、Elasticsearch、RabbitMQ、ファイルパス、7-Zipの確認コマンドと結果を記録する。 | 完了。この文書と `test-report.md` に記録。 |

## 採用結果

| 項目 | 方針 |
| --- | --- |
| 設定形式 | 既存構成に合わせて `application.properties` と `application-local.properties` を使った。YAMLへの移行はこのissueでは行っていない。 |
| 環境変数 | ローカル既定値を持たせつつ、`.env.example` と起動環境の環境変数で差し替え可能にした。 |
| APIの責務 | PostgreSQL、Elasticsearch、RabbitMQ投入、書籍ファイル保存領域の設定を扱う。Worker作業ディレクトリと7-Zip実行は扱わない。 |
| Workerの責務 | PostgreSQL、Elasticsearch、RabbitMQ受信、書籍ファイル保存領域、Worker作業ディレクトリ、7-Zip実行ファイル、WebP品質値を扱う。 |
| 外部依存の自動設定 | `local` プロファイルで外部依存を有効化した。テストでは `@SpringBootTest` のpropertiesでDB / JPA / Flyway自動設定を除外し、外部依存なしで実行できるようにした。 |
| API local profile | Security自動設定とSpring Session JDBC自動設定はSprint S0のlocal起動では無効化した。Flywayは `API_FLYWAY_ENABLED` で切り替え可能にし、既定は無効にした。 |
| Worker local profile | 非Webアプリケーションのまま外部依存を有効化した。local起動時だけ `LocalDependencyHealthLogger` で `db`、`elasticsearch`、`rabbit` の個別healthをログ出力する。 |
| 疎通確認 | 業務API、queue宣言、DBマイグレーション、検索インデックス作成までは行わず、設定読み込みと基本接続確認を主対象にした。 |
| 秘密情報 | パスワード、接続文字列の実値、内部物理パスを不要にログ出力しない。`.env.example` はローカル専用のダミー値のみを置いた。 |

## 実装した設定項目

Spring Boot標準設定を優先し、アプリ固有の値だけ独自prefixを使う。環境変数名はローカルの分かりやすさを優先し、必要なら後続の本番設定設計で調整する。

### 共通

| 設定項目 | 環境変数候補 | 既定値候補 | 対象 | 用途 |
| --- | --- | --- | --- | --- |
| `spring.datasource.url` | `DATABASE_URL` | `jdbc:postgresql://localhost:5432/manga` | API / Worker | PostgreSQL JDBC URL。 |
| `spring.datasource.username` | `DATABASE_USERNAME` | `manga` | API / Worker | PostgreSQLユーザ。 |
| `spring.datasource.password` | `DATABASE_PASSWORD` | `manga_local_password` | API / Worker | PostgreSQLパスワード。 |
| `spring.elasticsearch.uris` | `ELASTICSEARCH_URIS` | `http://localhost:9200` | API / Worker | Elasticsearch接続先。 |
| `spring.rabbitmq.host` | `RABBITMQ_HOST` | `localhost` | API / Worker | RabbitMQホスト。 |
| `spring.rabbitmq.port` | `RABBITMQ_PORT` | `5672` | API / Worker | RabbitMQ AMQPポート。 |
| `spring.rabbitmq.username` | `RABBITMQ_USERNAME` | `manga` | API / Worker | RabbitMQユーザ。 |
| `spring.rabbitmq.password` | `RABBITMQ_PASSWORD` | `manga_local_password` | API / Worker | RabbitMQパスワード。 |
| `storage.books.root` | `BOOK_STORAGE_ROOT` | `./.local/storage` | API / Worker | 書籍ファイル保存領域のルート。 |

### API固有

| 設定項目 | 環境変数候補 | 既定値候補 | 用途 |
| --- | --- | --- | --- |
| `storage.books.originals-dir` | `BOOK_STORAGE_ORIGINALS_DIR` | `books` | APIが原本ファイル保存先を組み立てるための設定。 |
| `spring.flyway.enabled` | `API_FLYWAY_ENABLED` | `false` | Sprint S0ではマイグレーション未整備のため既定は無効。 |

### Worker固有

| 設定項目 | 環境変数候補 | 既定値候補 | 用途 |
| --- | --- | --- | --- |
| `conversion.worker.work-root` | `WORKER_WORK_ROOT` | `./.local/worker` | ジョブ専用作業ディレクトリのルート。 |
| `conversion.sevenzip.executable-path` | `SEVENZIP_EXECUTABLE_PATH` | `7z` | 7-Zip for Linuxコンソール版の実行ファイルパス。Windowsローカルではインストール済み `7z` またはフルパスへ差し替える。 |
| `conversion.webp.quality` | `CONVERSION_WEBP_QUALITY` | `80` | WebP品質値。 |
| `conversion.worker.concurrency` | `CONVERSION_WORKER_CONCURRENCY` | `2` | Worker同時実行数。Sprint S0では設定読み込み対象に留める。 |
| `conversion.job.timeout` | `CONVERSION_JOB_TIMEOUT` | `30m` | 1ジョブのタイムアウト。Sprint S0では設定読み込み対象に留める。 |
| `spring.flyway.enabled` | `WORKER_FLYWAY_ENABLED` | `false` | Sprint S0ではマイグレーション未整備のため既定は無効。 |

`storage.books.root` は [ファイル保存設計](../../../../doc/04_design/06_file_storage_design.md#保存領域) の設定候補を正本とする。`conversion.*` は [画像変換設計のリソース制限と設定](../../../../doc/04_design/07_image_conversion_design.md#リソース制限と設定) を正本とする。

## 追加・更新した実装ファイル

| ファイル | 内容 |
| --- | --- |
| `apps/api/src/main/resources/application-local.properties` | APIのlocal外部依存接続、書籍ファイル保存領域、Actuator health詳細表示を設定。 |
| `apps/worker/src/main/resources/application-local.properties` | Workerのlocal外部依存接続、書籍ファイル保存領域、作業ディレクトリ、7-Zip、WebP品質値、Actuator health詳細表示を設定。 |
| `.env.example` | API / Worker用のローカル環境変数サンプルを追加。 |
| `apps/api/src/main/java/com/dommy/manga/api/config/BookStorageProperties.java` | API用の `storage.books.*` 型付き設定。 |
| `apps/worker/src/main/java/com/dommy/manga/worker/config/BookStorageProperties.java` | Worker用の `storage.books.*` 型付き設定。 |
| `apps/worker/src/main/java/com/dommy/manga/worker/config/ConversionProperties.java` | Worker用の `conversion.*` 型付き設定。 |
| `apps/worker/src/main/java/com/dommy/manga/worker/LocalDependencyHealthLogger.java` | Worker local起動時に `db`、`elasticsearch`、`rabbit` の個別healthをログ出力。 |
| `apps/api/src/main/java/com/dommy/manga/api/ApiApplication.java` | `@ConfigurationPropertiesScan` を有効化。 |
| `apps/worker/src/main/java/com/dommy/manga/worker/WorkerApplication.java` | `@ConfigurationPropertiesScan` を有効化。 |
| `apps/api/src/test/java/com/dommy/manga/api/ApiApplicationTests.java` | APIの設定バインド確認を追加。 |
| `apps/worker/src/test/java/com/dommy/manga/worker/WorkerApplicationTests.java` | Workerの設定バインド確認を追加。 |

## local profile方針

`local` で外部依存を有効化する方式を採用した。これにより、`docker compose up` 後に通常の `local` 起動でPostgreSQL、Elasticsearch、RabbitMQへの接続確認ができる。

外部依存なしの単体テストは、テスト側の `@SpringBootTest(properties = ...)` でDB / JPA / Flyway自動設定を除外して維持した。`local-connect` などの追加profileは作成していない。

## TDD / Red開始点

最初のRedは、API / Workerが外部依存設定を読み込めない、またはDocker Composeで起動したミドルウェアへ疎通できない状態を確認するところから開始する。

| 観点 | 最初に確認すること | 種別 |
| --- | --- | --- |
| PostgreSQL | `local` 起動時にDB接続情報を設定できない、または接続確認が失敗する。 | Red |
| Elasticsearch | `spring.elasticsearch.uris` の差し替えと接続確認ができない。 | Red |
| RabbitMQ | RabbitMQの接続情報を差し替えられない、または接続確認が失敗する。 | Red |
| ファイル保存領域 | `storage.books.root` が設定として読み込まれていない。 | Red |
| Worker作業ディレクトリ | `conversion.worker.work-root` が設定として読み込まれていない。 | Red |
| 7-Zip | `conversion.sevenzip.executable-path` を設定できない、または実行ファイルの存在確認方針がない。 | Red |
| WebP品質値 | `conversion.webp.quality` の既定値80と上書き方法が確認できない。 | Red |
| ログ安全性 | 接続確認ログにパスワードや不要な内部情報が出ない。 | 手動 |

上記のRed観点は、設定未定義または #83 / #84 の最小起動設定では外部依存へ接続しない状態から開始した。実装後は型付き設定テストとlocal起動確認でGreenにした。

## 確認コマンド

前提として #85 のComposeミドルウェアを起動した。

```powershell
docker compose up -d postgres elasticsearch rabbitmq
docker compose ps
```

API:

```powershell
.\gradlew.bat :apps:api:test
.\gradlew.bat :apps:api:bootRun --args='--spring.profiles.active=local --server.port=18081 --debug=false'
Invoke-RestMethod http://localhost:18081/actuator/health
```

Worker:

```powershell
.\gradlew.bat :apps:worker:test
.\gradlew.bat :apps:worker:bootRun --args='--spring.profiles.active=local --debug=false'
```

外部依存単体:

```powershell
docker compose exec postgres pg_isready -U manga -d manga
Invoke-RestMethod http://localhost:9200
docker compose exec elasticsearch bin/elasticsearch-plugin list
docker compose exec rabbitmq rabbitmqctl status
```

7-Zip:

```powershell
7z
```

実装時に `7z` がPATHにない場合は、`SEVENZIP_EXECUTABLE_PATH` にフルパスを設定して確認する。Linuxコンテナ内での7-Zip配置確認は、WorkerのDocker化または変換処理実装時に詳細化する。

確認完了後、次でローカルミドルウェアを停止した。

```powershell
docker compose down
```

## 確認結果

| 観点 | 結果 |
| --- | --- |
| API / Workerテスト | `.\gradlew.bat :apps:api:test :apps:worker:test` 成功。 |
| API local起動 | `.\gradlew.bat :apps:api:bootRun --args='--spring.profiles.active=local --server.port=18081 --debug=false'` で起動成功。 |
| API health | `http://localhost:18081/actuator/health` が `status=UP` を返した。詳細healthで `db=UP`、`elasticsearch=UP`、`rabbit=UP` を確認。 |
| Worker local起動 | `.\gradlew.bat :apps:worker:bootRun --args='--spring.profiles.active=local --debug=false'` で起動成功。 |
| Worker health | 起動ログで `manga-worker local dependency health: db=UP, elasticsearch=UP, rabbit=UP` を確認。 |
| PostgreSQL | API / Workerともに `jdbc:postgresql://localhost:5432/manga` へ接続し、PostgreSQL 17.10を確認。 |
| Elasticsearch | API healthでcluster `docker-cluster`、status `green` を確認。Worker healthでも `elasticsearch=UP` を確認。 |
| RabbitMQ | API healthでRabbitMQ 4.3.0を確認。Worker healthでも `rabbit=UP` を確認。 |
| 7-Zip | `Get-Command 7z` では未検出。設定値は `SEVENZIP_EXECUTABLE_PATH` で差し替え可能。 |
| 停止 | `docker compose down` で確認に使ったPostgreSQL、Elasticsearch、RabbitMQを停止、削除した。volumeは削除していない。 |

## 実装タスク

- [x] Red: API / Workerが現在の `local` プロファイルでは外部依存へ接続しない、または設定差し替えを確認できないことを記録する。
- [x] Green: APIの `application-local.properties` にPostgreSQL、Elasticsearch、RabbitMQ、書籍ファイル保存領域のローカル設定を追加する。
- [x] Green: Workerの `application-local.properties` にPostgreSQL、Elasticsearch、RabbitMQ、書籍ファイル保存領域、作業ディレクトリ、7-Zip、WebP品質値のローカル設定を追加する。
- [x] Green: `.env.example` にAPI / Workerが読むローカル設定のサンプル値を追加する。
- [x] Green: API / Workerのテストが外部依存なしで実行でき、必要な疎通確認は明示的な起動確認で行えるようにする。
- [x] Refactor: 設定名が設計ドキュメント、Composeサービス名、Spring Boot標準設定と矛盾していないことを確認する。
- [x] Document: 実行したコマンド、接続確認結果、未実行項目、ログ安全性確認を `test-report.md` へ追記する。

## 残タスクの切り分け

### #86で確認すべきもの

#86の受け入れ条件は、API / Workerがローカル設定で外部依存とパス設定を扱えること、および疎通確認方法を記録することまでとする。次の項目は #86 内で確認済みであり、追加の残タスクとしては扱わない。

| 項目 | 判定 | 結果 |
| --- | --- | --- |
| PostgreSQL接続情報の差し替え | #86で確認する。 | API / Workerともに `local` プロファイルで接続確認済み。 |
| Elasticsearch接続情報の差し替え | #86で確認する。 | API healthとWorker local health loggerで `UP` を確認済み。 |
| RabbitMQ接続情報の差し替え | #86で確認する。 | API healthとWorker local health loggerで `UP` を確認済み。 |
| 書籍ファイル保存領域の設定読み込み | #86で確認する。 | `storage.books.root` と `storage.books.originals-dir` の型付き設定バインドをテスト済み。 |
| Worker作業ディレクトリの設定読み込み | #86で確認する。 | `conversion.worker.work-root` の型付き設定バインドをテスト済み。 |
| 7-Zip実行ファイルパスの設定読み込み | #86で確認する。 | `conversion.sevenzip.executable-path` の型付き設定バインドをテスト済み。実行確認は後続issueへ送る。 |
| WebP品質値の設定読み込み | #86で確認する。 | `conversion.webp.quality=80` の型付き設定バインドをテスト済み。 |
| ローカル起動時の疎通確認方法 | #86で記録する。 | この文書と `test-report.md` に記録済み。 |

### 後続issueで扱うもの

次の項目は、#86の「設定と基本疎通確認」を超えて業務機能、スキーマ、外部プロセス実行、または運用手順に入るため、後続issueへ引き継ぐ。

2026-05-16時点で、該当するGitHub issue本文へ `#86から引き継ぐタスク` として追記済み。

| 項目 | 後続issue | 引き継ぎ内容 |
| --- | --- | --- |
| 7-Zip実行確認 | #57 管理ユーザとして、zipアップロードからWebP変換完了まで確認したい | `SEVENZIP_EXECUTABLE_PATH` で指定した7-Zip実行ファイルをWorkerから呼び出し、zip展開からWebP変換までの縦切りで確認する。 |
| rar / 7zip形式での7-Zip確認 | #58 管理ユーザとして、rarと7zipのアーカイブも変換したい | rar / 7zip展開時の7-Zip引数、終了コード、タイムアウト、ログ安全性を確認する。 |
| DBマイグレーション | #53 開発者として、書籍メタ情報のドメインモデルを扱えるようにしたい | 書籍メタ情報と関連する最小テーブルのFlyway migrationを追加し、`API_FLYWAY_ENABLED` / `WORKER_FLYWAY_ENABLED` の扱いを見直す。 |
| Spring Session JDBCテーブル | #50 利用者として、メール2段階認証でログインして安全なセッションを取得したい | セッション実装時にSpring Session JDBCテーブルを作成し、Sprint S0 localで無効化した `JdbcSessionAutoConfiguration` を有効化する条件を確認する。 |
| RabbitMQ業務queue / listener | #54 開発者として、RabbitMQで変換ジョブを安全に配送したい | exchange、queue、dead letter queue、listener、ack、再配送、冪等性を実装・確認する。 |
| Elasticsearchインデックス作成 | #61 開発者として、PostgreSQLから再構築可能な検索インデックスを作りたい | インデックス定義、必須プラグイン前提、Outbox、再構築手順を実装・確認する。 |
| 実ファイル保存 / アップロード時のパス検証 | #55 管理ユーザとして、自炊本アーカイブをアップロードしたい | `storage.books.root` 配下への原本保存、アップロードファイル名と保存パスの検証、保存領域外脱出防止を実装・確認する。 |
| 変換時の作業ディレクトリ / 生成物パス検証 | #57 管理ユーザとして、zipアップロードからWebP変換完了まで確認したい | `conversion.worker.work-root` 配下のジョブ作業ディレクトリ、展開先、生成WebP保存先の検証とクリーンアップを確認する。 |

## 対象外

- 業務API、書籍アップロード、変換ジョブ投入、RabbitMQ listenerの実装。
- DBマイグレーション、スキーマ作成、Repository実装。
- Elasticsearchインデックス作成、検索API、再インデックス処理。
- RabbitMQのexchange、queue、dead letter queueの業務設定。
- 7-Zipによる実アーカイブ展開、WebP変換、画像処理ライブラリ選定。
- API / WorkerをDocker Composeサービスとして起動する構成。
- 本番用の秘密情報管理、TLS、認証付きElasticsearch、RabbitMQ本番ユーザ設計。

最小テストと確認コマンドの横断整理は #87 で扱う。ローカル開発手順とTODOの最終更新は #88 で扱う。

## 実施結果記録欄

- 更新した設定ファイル: `apps/api/src/main/resources/application-local.properties`、`apps/worker/src/main/resources/application-local.properties`、`.env.example`。
- 追加した型付き設定: APIに `BookStorageProperties`、Workerに `BookStorageProperties` と `ConversionProperties` を追加した。両アプリで `@ConfigurationPropertiesScan` を有効化した。
- 採用したprofile方針: `local` で外部依存を有効化した。APIのSecurity自動設定とSpring Session JDBC自動設定はS0 localでは無効化し、Flywayは既定無効のまま環境変数で切り替え可能にした。
- APIの確認結果: `.\gradlew.bat :apps:api:test :apps:worker:test` 成功。`docker compose up -d postgres elasticsearch rabbitmq` 後、`.\gradlew.bat :apps:api:bootRun --args='--spring.profiles.active=local --server.port=18081 --debug=false'` で起動し、`http://localhost:18081/actuator/health` が `status=UP` を返した。詳細healthで `db=UP`、`elasticsearch=UP`、`rabbit=UP` を確認した。
- Workerの確認結果: `.\gradlew.bat :apps:worker:bootRun --args='--spring.profiles.active=local --debug=false'` で起動し、`LocalDependencyHealthLogger` により `manga-worker local dependency health: db=UP, elasticsearch=UP, rabbit=UP` を確認した。
- ファイル保存領域と作業ディレクトリの確認結果: テストで `storage.books.root`、`storage.books.originals-dir`、`conversion.worker.work-root` を設定としてバインドできることを確認した。実ディレクトリ作成とパス正規化はファイル保存/変換処理の実装時に扱う。
- 7-ZipとWebP品質値の確認結果: `conversion.sevenzip.executable-path` と `conversion.webp.quality=80` を型付き設定として読み込めることをテストで確認した。ローカルPATHでは `7z` が見つからなかったため、7-Zip実行確認は未完了。実行環境では `SEVENZIP_EXECUTABLE_PATH` で差し替える。
- ログに秘密情報が出力されていないことの確認結果: 起動ログとhealth確認ログにパスワード、トークン、本番秘密情報は出力されていない。HibernateのDB情報ログにJDBC URLは表示されるが、ユーザ名とパスワードは表示されていない。
- 未実行の確認と理由: DBマイグレーション、Spring Session JDBCテーブル作成、業務queue宣言、RabbitMQ listener、Elasticsearchインデックス作成、7-Zip実アーカイブ展開、WebP変換は後続PBIの業務実装で扱うため未実行。
- 更新したドキュメント: この文書、`development/scrum/sprints/sprint-s0/test-report.md`。
