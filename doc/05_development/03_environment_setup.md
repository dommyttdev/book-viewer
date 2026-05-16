# 環境構築手順

## 目的

このドキュメントは、自炊本閲覧Webアプリケーションをローカルで開発するために必要なツール、ミドルウェア、環境変数、シークレット管理方針を整理する。

この文書は、開発者が環境を揃えるための基準と確認観点を定義する。リポジトリ構成、アプリケーション配置、ビルド単位、起動単位は [プロジェクト構成](00_project_structure.md) を正本とする。

## 前提

- 開発対象は、Next.jsフロントエンド、Spring BootバックエンドAPI、Spring Boot変換ワーカーである。
- Javaは25を使用する。
- Spring Bootは4.0.6を使用する。
- PostgreSQLを正本データストアとして扱う。
- ElasticsearchはPostgreSQLから再構築可能な派生データとして扱う。
- Elasticsearchの必須プラグインは [技術スタックのElasticsearch必須プラグイン](../03_architecture/02_technology_stack.md#elasticsearch必須プラグイン) を正本とする。
- rar / 7zip形式の展開には、変換ワーカーから7-Zip for Linuxコンソール版を外部プロセスとして呼び出す。
- ローカル開発では、PostgreSQL、Elasticsearch、RabbitMQなどのミドルウェアをDocker Composeで起動する。

## プロジェクト構成の参照

リポジトリ構成、`apps/frontend/`、`apps/api/`、`apps/worker/`、`libs/backend-common/` の役割、Gradleマルチプロジェクトのinclude名は [プロジェクト構成](00_project_structure.md) を参照する。

この文書では、環境構築に必要なツール、ミドルウェア、環境変数、確認観点だけを扱う。

## Sprint S0の実構成

Sprint S0 / issue #81から#88では、次の実構成を採用している。

| 項目 | 実構成 |
| --- | --- |
| フロントエンド | `apps/frontend`。Next.js 16.2.6 / React 19.2.4 / TypeScript / npmを使用する。 |
| フロントエンドAPI接続先 | `NEXT_PUBLIC_API_BASE_URL` で差し替える。ローカル既定値は `http://localhost:18081` とする。 |
| API | `apps/api`。Spring Boot 4.0.6 / Java 25 / Gradle Wrapperを使用する。 |
| Worker | `apps/worker`。Spring Boot 4.0.6 / Java 25 / Gradle Wrapperを使用する。 |
| ミドルウェア | `compose.yaml` の `postgres`、`elasticsearch`、`rabbitmq` を使用する。 |
| Elasticsearch必須プラグイン | `docker/elasticsearch/Dockerfile` で `analysis-kuromoji` と `analysis-icu` を導入する。 |
| ローカル設定サンプル | `.env.example` を `.env` へコピーし、必要な値だけローカルで上書きする。 |
| 確認結果 | Sprint S0の確認結果は `development/scrum/sprints/sprint-s0/test-report.md` を正本とする。 |

## 必要なツール

| ツール | 用途 | 必須バージョン / 方針 |
| --- | --- | --- |
| Git | ソースコード管理 | 現行の安定版を使用する。 |
| Java | Spring Boot API / Workerの実行とテスト | Java 25を使用する。 |
| Node.js | Next.jsフロントエンドの実行とテスト | Sprint S0ではv22.14.0で確認済み。 |
| npm | Next.jsフロントエンドのパッケージ管理 | `apps/frontend/package-lock.json` を使用する。 |
| Gradle Wrapper | Spring Boot API / Workerのビルドとテスト | ルートGradleマルチプロジェクトとして使用する。 |
| Docker | ローカルミドルウェア実行 | Docker Compose v2を使用できる状態にする。 |
| Docker Compose | PostgreSQL、Elasticsearch、RabbitMQなどの起動 | `docker compose` コマンドを使用する。 |
| 7-Zip for Linuxコンソール版 | rar / 7zipを含むアーカイブ展開 | 変換ワーカーコンテナ内で実行できるようにする。 |
| IDE / エディタ | 実装とMarkdown編集 | UTF-8を扱えるものを使用する。 |

## Java 25

Java 25をインストールし、`java` とビルドツールから同じJDKを参照できるようにする。

確認コマンド:

```bash
java --version
```

期待する状態:

- Java 25が表示される。
- `JAVA_HOME` がJava 25のJDKを指している。
- IDEのプロジェクトSDKもJava 25に設定されている。

Spring Boot APIと変換ワーカーは同じJavaバージョンを前提にする。複数のJDKを入れている場合は、シェル、IDE、CIで参照するJDKがずれないようにする。

## Node.js

Node.jsはNext.jsフロントエンドで使用する。パッケージマネージャはnpmを使用し、`apps/frontend/package-lock.json` をロックファイルとする。

確認コマンド:

```bash
node --version
npm --version
```

パッケージマネージャとしてnpm以外を採用する場合は、[doc/05_development/04_local_development.md](04_local_development.md) の起動手順とこの文書を更新する。

Windows PowerShellで `npm.ps1` が実行ポリシーによりブロックされる場合は、`npm.cmd` を使用する。

## DockerとDocker Compose

ローカル開発では、PostgreSQL、Elasticsearch、RabbitMQをDocker Composeで起動する。アプリケーションもDocker Composeで起動できる構成にしてよいが、開発中はフロントエンド、API、Workerをホスト上で起動してもよい。

確認コマンド:

```bash
docker --version
docker compose version
docker ps
```

注意点:

- Sprint S0のComposeサービス名は `postgres`、`elasticsearch`、`rabbitmq` とする。
- PostgreSQLとElasticsearchのデータボリュームを不用意に削除しない。
- ローカル検証でデータを初期化する場合は、対象が開発環境であることを確認する。
- 本番用の秘密情報や実データをローカルの検証用Composeへ流用しない。

## PostgreSQL

PostgreSQLは、メタ情報、ユーザ、権限、ジョブ状態、閲覧履歴、お気に入り、検索更新Outboxの正本を保持する。

ローカルではDocker Composeサービスとして起動することを基本とする。直接ホストへインストールして使用する場合も、接続情報は環境変数または外部設定で渡し、アプリケーションコードへ埋め込まない。

設定する主な値:

| 項目 | 方針 |
| --- | --- |
| ホスト | Docker Composeサービス名またはlocalhostを環境ごとに設定する。 |
| ポート | ローカル公開ポートを環境ごとに設定する。 |
| DB名 | 開発用DB名を使用する。 |
| ユーザ | 開発用ユーザを使用する。 |
| パスワード | `.env` などGit管理外のファイルで管理する。 |

## Elasticsearch

Elasticsearchは、タイトル、著者、タグ、シリーズなどの検索用派生インデックスを保持する。PostgreSQLを正とし、Elasticsearchのデータは再構築可能なものとして扱う。

ローカルではDocker Composeで起動し、技術スタックで定義されたElasticsearch必須プラグインを利用できるイメージまたはプラグイン導入手順を用意する。本番環境のElasticsearchにも同じプラグインを導入し、ローカルと本番でインデックス定義がずれないようにする。

確認観点:

- Elasticsearchへ接続できる。
- 技術スタックで定義されたElasticsearch必須プラグインを利用できる。
- API起動時またはインデックス作成前の必須プラグイン確認が成功する。
- 開発用インデックス名が環境変数または設定で分離されている。
- Elasticsearchへ秘密情報や不要な個人情報を入れない。

確認コマンド例:

```bash
curl http://localhost:9200
curl http://localhost:9200/_cat/plugins
```

Docker Compose用のElasticsearchイメージを作成する場合は、同一イメージ内で技術スタックの必須プラグインを導入する。プラグインを追加または変更した場合は、Elasticsearchコンテナを再作成し、`_cat/plugins`で必須プラグインが表示されることを確認してからインデックス作成または再インデックスを実行する。

ローカル公開ポートや認証方式は、Docker Compose定義に合わせて更新する。

## 7-Zip for Linuxコンソール版

7-Zip for Linuxコンソール版は、変換ワーカーがzip / rar / 7zip形式のアーカイブを展開するために使用する。

方針:

- 7-Zipは変換ワーカーコンテナ内に配置する。
- 変換ワーカーから外部プロセスとして呼び出す。
- 実行ファイルパスは環境変数またはapplication propertiesで設定可能にする。
- Java標準ライブラリやApache Commons Compressのrar対応には依存しない。
- 実行権限、タイムアウト、終了コード、標準出力、標準エラーの扱いを実装で確認する。

確認コマンド例:

```bash
7zz
7zz i
```

変換ワーカーコンテナ内では、次のように確認する想定とする。

```bash
docker compose exec worker 7zz
```

サービス名と実行ファイル名は、実装時のDockerfileとCompose定義に合わせて更新する。

## RabbitMQ

RabbitMQは、バックエンドAPIと変換ワーカーを非同期に接続する専用キューとして使用する。

ローカルではDocker Composeサービスとして起動することを基本とする。管理UIを利用できる構成にする場合は、開発用ポートと認証情報を`.env`などGit管理外の設定で管理する。

確認観点:

- APIからRabbitMQへ接続できる。
- WorkerがRabbitMQからメッセージを取得できる。
- 変換ジョブキュー、dead letter queueが作成される。
- メッセージのack、再配送、dead letterをローカルで確認できる。
- RabbitMQの管理ユーザ、パスワードをGitへコミットしない。

確認コマンド例:

```bash
docker compose exec rabbitmq rabbitmqctl status
docker compose exec rabbitmq rabbitmqctl list_queues name messages messages_unacknowledged
```

Docker Composeサービス名は `rabbitmq` とする。キュー名、exchange名は、実装時のRabbitMQ設定に合わせて更新する。

## 環境変数

環境変数は、環境差分、接続情報、秘密情報、変更可能な設定値をアプリケーションへ渡すために使用する。

主な環境変数候補:

| 環境変数 | 用途 | 秘密情報 |
| --- | --- | --- |
| `APP_ENV` | 実行環境名 | いいえ |
| `APP_BASE_URL` | フロントエンドまたはAPIの基準URL | いいえ |
| `NEXT_PUBLIC_API_BASE_URL` | Next.jsフロントエンドから見たAPI基準URL。ローカル既定値は `http://localhost:18081` | いいえ |
| `DATABASE_URL` | PostgreSQL接続URL | はい |
| `DATABASE_USERNAME` | PostgreSQLユーザ | はい |
| `DATABASE_PASSWORD` | PostgreSQLパスワード | はい |
| `ELASTICSEARCH_URIS` | Elasticsearch接続URL | 環境による |
| `ELASTICSEARCH_USERNAME` | Elasticsearchユーザ | はい |
| `ELASTICSEARCH_PASSWORD` | Elasticsearchパスワード | はい |
| `ELASTICSEARCH_INDEX_PREFIX` | 開発用インデックス接頭辞 | いいえ |
| `RABBITMQ_HOST` | RabbitMQ接続先ホスト | いいえ |
| `RABBITMQ_PORT` | RabbitMQ接続先ポート | いいえ |
| `RABBITMQ_USERNAME` | RabbitMQユーザ | はい |
| `RABBITMQ_PASSWORD` | RabbitMQパスワード | はい |
| `RABBITMQ_VHOST` | RabbitMQ virtual host | 環境による |
| `BOOK_STORAGE_ROOT` | 原本ファイル保存先 | いいえ |
| `BOOK_STORAGE_ORIGINALS_DIR` | 原本ファイル保存領域内の原本ディレクトリ名 | いいえ |
| `WORKER_WORK_ROOT` | 変換ワーカー作業ディレクトリ | いいえ |
| `SEVENZIP_EXECUTABLE_PATH` | 7-Zip実行ファイルパス | いいえ |
| `CONVERSION_WEBP_QUALITY` | WebP品質値。ローカル既定値は80 | いいえ |
| `CONVERSION_WORKER_CONCURRENCY` | 変換ワーカー同時実行数。ローカル既定値は2 | いいえ |
| `CONVERSION_JOB_TIMEOUT` | 1ジョブのタイムアウト。ローカル既定値は30分 | いいえ |
| `API_FLYWAY_ENABLED` | API local profileでFlywayを有効化するか | いいえ |
| `WORKER_FLYWAY_ENABLED` | Worker local profileでFlywayを有効化するか | いいえ |
| `UPLOAD_MAX_FILE_SIZE` | アップロード1ファイルの安全上限。既定値は画像変換設計を参照する | いいえ |
| `UPLOAD_MAX_CONCURRENT_UPLOADS` | 同時アップロード数。既定値は画像変換設計を参照する | いいえ |
| `WORKER_MAX_CONCURRENCY` | 変換ワーカー同時実行数の安全上の最大値。既定値は画像変換設計を参照する | いいえ |
| `CONVERSION_MAX_EXTRACTED_SIZE` | 展開後総量の安全上限。既定値は画像変換設計を参照する | いいえ |
| `CONVERSION_MAX_EXTRACTED_SIZE_RATIO` | 展開後総量のアップロードサイズ比。既定値は画像変換設計を参照する | いいえ |
| `CONVERSION_MAX_ENTRY_COUNT` | アーカイブエントリ数の安全上限。既定値は画像変換設計を参照する | いいえ |
| `CONVERSION_MAX_IMAGE_PIXELS` | 1画像の最大ピクセル数。既定値は画像変換設計を参照する | いいえ |
| `CONVERSION_MAX_TEMP_DISK_USAGE` | 1ジョブ一時領域の安全上限。既定値は画像変換設計を参照する | いいえ |
| `MAIL_HOST` | メール送信ホスト | いいえ |
| `MAIL_USERNAME` | メールユーザ | はい |
| `MAIL_PASSWORD` | メールパスワード | はい |

Sprint S0で実装済みの変数は `.env.example` を正本とする。未実装の業務機能に関する変数は初期方針であり、実装時にSpring Boot properties、Next.jsの公開変数命名、Docker Compose定義へ合わせて必要最小限の差分を反映する。

## シークレット管理

シークレットには、DBパスワード、Elasticsearchパスワード、キュー認証情報、メール認証情報、セッション署名鍵、トークン、秘密鍵を含める。

ルール:

- シークレットをGitへコミットしない。
- `.env`、`.env.local`、IDEローカル設定、個人用Compose overrideはGit管理外にする。
- 必要な変数名は `.env.example` などのサンプルに記載してよいが、実値は入れない。
- ログ、エラーレスポンス、スクリーンショット、Issue、Pull Requestへシークレットを記載しない。
- 開発用、検証用、本番用のシークレットを使い回さない。
- シークレットが漏えいした可能性がある場合は、値を無効化またはローテーションし、影響範囲を記録する。

## ローカル保存領域

開発環境では、原本ファイル、変換済みWebP、サムネイル、変換ワーカー作業ディレクトリを分ける。

推奨する考え方:

| 領域 | 用途 | Git管理 |
| --- | --- | --- |
| 原本ファイル保存領域 | アップロードされたアーカイブ原本 | しない |
| 変換済みWebP保存領域 | 閲覧用画像 | しない |
| サムネイル保存領域 | 一覧、検索結果、詳細画面用画像 | しない |
| Worker作業ディレクトリ | ジョブごとの展開、変換の一時領域 | しない |

保存先は環境変数またはapplication propertiesで指定し、OS固有の絶対パスをコードへ埋め込まない。

## セットアップ確認チェックリスト

- [ ] Java 25を使用できる。
- [ ] Node.jsとnpmを使用できる。
- [ ] Gradle Wrapperでバックエンドをビルドできる。
- [ ] DockerとDocker Composeを使用できる。
- [ ] PostgreSQLコンテナを起動できる。
- [ ] Elasticsearchコンテナを起動できる。
- [ ] 技術スタックで定義されたElasticsearch必須プラグインを利用できる。
- [ ] RabbitMQを起動できる。
- [ ] API / Workerを `local` profileで起動し、PostgreSQL、Elasticsearch、RabbitMQのhealthを確認できる。
- [ ] フロントエンドの `NEXT_PUBLIC_API_BASE_URL` を `.env` で差し替えられる。
- [ ] `SEVENZIP_EXECUTABLE_PATH` で7-Zip実行ファイルパスを差し替えられる。
- [ ] `.env` などのローカル設定がGit管理外になっている。
- [ ] 原本、WebP、サムネイル、作業ディレクトリの保存先を分けている。
- [ ] WebP品質値、ワーカー同時実行数、1ジョブタイムアウト、変換リソースの安全上限を設定で変更できる方針を確認している。具体値は [画像変換設計のリソース制限と設定](../04_design/07_image_conversion_design.md#リソース制限と設定) を正本とする。

## 更新方針

実装により、Node.jsバージョン、Gradleバージョン、Docker Composeサービス名、環境変数名、7-Zipの配置、RabbitMQ設定、保存領域の構成が確定または変更された場合は、このドキュメントを更新する。
