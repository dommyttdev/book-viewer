# 環境構築手順

## 目的

このドキュメントは、自炊本閲覧Webアプリケーションをローカルで開発するために必要なツール、ミドルウェア、環境変数、シークレット管理方針を整理する。

実装初期のため、具体的なパッケージ名、Docker Composeサービス名、起動スクリプト名は今後のプロジェクト構成に合わせて更新する。ここでは、開発者が環境を揃えるための基準と確認観点を定義する。

## 前提

- 開発対象は、Next.jsフロントエンド、Spring BootバックエンドAPI、Spring Boot変換ワーカーである。
- Javaは25を使用する。
- Spring Bootは4.0.6を使用する。
- PostgreSQLを正本データストアとして扱う。
- ElasticsearchはPostgreSQLから再構築可能な派生データとして扱う。
- Elasticsearchでは日本語検索用にanalysis-kuromojiを使用する。
- rar / 7zip形式の展開には、変換ワーカーから7-Zip for Linuxコンソール版を外部プロセスとして呼び出す。
- ローカル開発では、PostgreSQL、Elasticsearch、専用キューなどのミドルウェアをDocker Composeで起動する。

## 必要なツール

| ツール | 用途 | 必須バージョン / 方針 |
| --- | --- | --- |
| Git | ソースコード管理 | 現行の安定版を使用する。 |
| Java | Spring Boot API / Workerの実行とテスト | Java 25を使用する。 |
| Node.js | Next.jsフロントエンドの実行とテスト | Next.jsがサポートするLTS系を使用し、具体バージョンはフロントエンド作成時に固定する。 |
| Docker | ローカルミドルウェア実行 | Docker Compose v2を使用できる状態にする。 |
| Docker Compose | PostgreSQL、Elasticsearch、専用キューなどの起動 | `docker compose` コマンドを使用する。 |
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

Node.jsはNext.jsフロントエンドで使用する。具体バージョンとパッケージマネージャはフロントエンド作成時に、`package.json`、ロックファイル、またはバージョン管理ファイルで固定する。

確認コマンド:

```bash
node --version
npm --version
```

パッケージマネージャとしてnpm以外を採用する場合は、`doc/05_development/04_local_development.md` の起動手順とこの文書を更新する。

## DockerとDocker Compose

ローカル開発では、PostgreSQL、Elasticsearch、専用キューをDocker Composeで起動する。アプリケーションもDocker Composeで起動できる構成にしてよいが、開発中はフロントエンド、API、Workerをホスト上で起動してもよい。

確認コマンド:

```bash
docker --version
docker compose version
docker ps
```

注意点:

- PostgreSQLとElasticsearchのデータボリュームを不用意に削除しない。
- ローカル検証でデータを初期化する場合は、対象が開発環境であることを確認する。
- 本番用の秘密情報や実データをローカルの検証用Composeへ流用しない。

## PostgreSQL

PostgreSQLは、メタ情報、ユーザ、権限、ジョブ状態、閲覧履歴、お気に入り、検索インデックス更新状態の正本を保持する。

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

ローカルではDocker Composeで起動し、analysis-kuromojiを利用できるイメージまたはプラグイン導入手順を用意する。

確認観点:

- Elasticsearchへ接続できる。
- analysis-kuromojiが利用できる。
- 開発用インデックス名が環境変数または設定で分離されている。
- Elasticsearchへ秘密情報や不要な個人情報を入れない。

確認コマンド例:

```bash
curl http://localhost:9200
curl http://localhost:9200/_cat/plugins
```

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

## 環境変数

環境変数は、環境差分、接続情報、秘密情報、変更可能な設定値をアプリケーションへ渡すために使用する。

主な環境変数候補:

| 環境変数 | 用途 | 秘密情報 |
| --- | --- | --- |
| `APP_ENV` | 実行環境名 | いいえ |
| `APP_BASE_URL` | フロントエンドまたはAPIの基準URL | いいえ |
| `DATABASE_URL` | PostgreSQL接続URL | はい |
| `DATABASE_USERNAME` | PostgreSQLユーザ | はい |
| `DATABASE_PASSWORD` | PostgreSQLパスワード | はい |
| `ELASTICSEARCH_URL` | Elasticsearch接続URL | 環境による |
| `ELASTICSEARCH_USERNAME` | Elasticsearchユーザ | はい |
| `ELASTICSEARCH_PASSWORD` | Elasticsearchパスワード | はい |
| `ELASTICSEARCH_INDEX_PREFIX` | 開発用インデックス接頭辞 | いいえ |
| `QUEUE_URL` | 専用キュー接続先 | 環境による |
| `QUEUE_USERNAME` | 専用キューユーザ | はい |
| `QUEUE_PASSWORD` | 専用キューパスワード | はい |
| `BOOK_STORAGE_ROOT` | 原本ファイル保存先 | いいえ |
| `CONVERTED_IMAGE_STORAGE_ROOT` | 変換済みWebP保存先 | いいえ |
| `THUMBNAIL_STORAGE_ROOT` | サムネイル保存先 | いいえ |
| `WORKER_WORK_DIR` | 変換ワーカー作業ディレクトリ | いいえ |
| `SEVEN_ZIP_PATH` | 7-Zip実行ファイルパス | いいえ |
| `WEBP_QUALITY` | WebP品質値。既定値80 | いいえ |
| `WORKER_CONCURRENCY` | 変換ワーカー同時実行数。既定値10 | いいえ |
| `CONVERSION_JOB_TIMEOUT` | 1ジョブのタイムアウト。既定値30分 | いいえ |
| `MAIL_HOST` | メール送信ホスト | いいえ |
| `MAIL_USERNAME` | メールユーザ | はい |
| `MAIL_PASSWORD` | メールパスワード | はい |

変数名は初期候補であり、実装時にSpring Boot properties、Next.jsの公開変数命名、Docker Compose定義へ合わせて確定する。

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
- [ ] Node.jsとパッケージマネージャを使用できる。
- [ ] DockerとDocker Composeを使用できる。
- [ ] PostgreSQLコンテナを起動できる。
- [ ] Elasticsearchコンテナを起動できる。
- [ ] analysis-kuromojiを利用できる。
- [ ] 専用キューを起動できる。
- [ ] 変換ワーカーコンテナ内で7-Zip for Linuxコンソール版を実行できる。
- [ ] `.env` などのローカル設定がGit管理外になっている。
- [ ] 原本、WebP、サムネイル、作業ディレクトリの保存先を分けている。
- [ ] WebP品質値80、ワーカー同時実行数10、1ジョブ30分タイムアウトを設定で変更できる方針を確認している。

## 更新方針

実装により、Node.jsバージョン、パッケージマネージャ、ビルドツール、Docker Composeサービス名、環境変数名、7-Zipの配置、専用キュー製品、保存領域の構成が確定または変更された場合は、このドキュメントを更新する。
