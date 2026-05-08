# ローカル開発手順

## 目的

このドキュメントは、自炊本閲覧Webアプリケーションをローカルで開発するときの起動、停止、テスト、ログ確認、よく使う開発コマンドを整理する。

実装初期のため、具体的なディレクトリ名、Gradle / Maven、npm / pnpm / yarn、Docker Composeサービス名は今後のプロジェクト構成に合わせて更新する。ここでは、開発時に必要な流れと確認観点を定義する。

## 前提

- 環境構築は `doc/05_development/03_environment_setup/01_environment_setup.md` に従う。
- コーディングルールは `doc/05_development/01_coding_rules/01_coding_rules.md` に従う。
- PostgreSQLを正本、Elasticsearchを再構築可能な派生データとして扱う。
- アーカイブ展開は変換ワーカーから7-Zip for Linuxコンソール版を呼び出す。
- WebP品質値の既定値は80、変換ワーカー同時実行数の既定値は10、1ジョブのタイムアウトは30分を基本とする。

## 基本の開発フロー

1. `doc/TODO.md` と関連Issueを確認する。
2. 必要な環境変数を `.env` などのGit管理外ファイルへ設定する。
3. Docker ComposeでPostgreSQL、Elasticsearch、専用キューを起動する。
4. DBマイグレーションまたは初期化処理を実行する。
5. Spring BootバックエンドAPIを起動する。
6. Spring Boot変換ワーカーを起動する。
7. Next.jsフロントエンドを起動する。
8. 画面、API、ログ、ジョブ状態を確認しながら開発する。
9. 変更範囲に応じたテストまたは手動確認を実行する。
10. 関連ドキュメントと `doc/TODO.md` を更新する。

## ミドルウェア起動

PostgreSQL、Elasticsearch、専用キューなどのミドルウェアはDocker Composeで起動する。

起動:

```bash
docker compose up -d postgres elasticsearch queue
```

状態確認:

```bash
docker compose ps
```

ログ確認:

```bash
docker compose logs --tail=200 postgres
docker compose logs --tail=200 elasticsearch
docker compose logs --tail=200 queue
```

停止:

```bash
docker compose down
```

サービス名は候補であり、実際のCompose定義に合わせて更新する。データボリュームを削除するコマンドは、開発用データを初期化してよい場合だけ実行する。

## Spring BootバックエンドAPIの起動

バックエンドAPIは、認証、書籍管理、検索、閲覧、管理操作、PostgreSQL更新、Elasticsearch検索、専用キューへのジョブ投入を担当する。

起動前の確認:

- Java 25を使用している。
- PostgreSQLへ接続できる。
- Elasticsearchへ接続できる。
- 専用キューへ接続できる。
- 原本ファイル保存領域、変換済みWebP保存領域、サムネイル保存領域を参照できる。
- 必要な環境変数が設定されている。

起動コマンド例:

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

Mavenを採用する場合の候補:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

Windows PowerShellで実行する場合は、プロジェクトの実際のラッパースクリプト名に合わせる。

起動後の確認:

- APIのヘルスチェックが成功する。
- PostgreSQL、Elasticsearch、専用キューへの接続エラーが出ていない。
- ログにシークレットや不要な個人情報が出ていない。
- アップロード、検索、閲覧などの主要APIが実装済み範囲で応答する。

## Spring Boot変換ワーカーの起動

変換ワーカーは、専用キューからジョブを取得し、アーカイブ展開、WebP変換、サムネイル生成、ジョブ状態更新を行う。

起動前の確認:

- Java 25を使用している。
- 専用キューへ接続できる。
- PostgreSQLへ接続できる。
- 原本ファイル保存領域、変換済みWebP保存領域、サムネイル保存領域へアクセスできる。
- Worker作業ディレクトリへ書き込める。
- 7-Zip for Linuxコンソール版を実行できる。
- WebP品質値、同時実行数、ジョブタイムアウトを設定で変更できる。

起動コマンド例:

```bash
./gradlew bootRun --args='--spring.profiles.active=local,worker'
```

Mavenを採用する場合の候補:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local,worker
```

ワーカーがAPIと同じSpring Bootアプリケーション内の別起動モードになるか、別モジュールになるかは実装時に確定する。確定後、この起動コマンドを更新する。

起動後の確認:

- 専用キューへの接続に成功している。
- ジョブ取得待ち状態になっている。
- 7-Zip実行ファイルパスの設定が読み込まれている。
- Worker作業ディレクトリの作成または利用に失敗していない。
- 起動直後に不要な変換ジョブを実行していない。

## Next.jsフロントエンドの起動

フロントエンドは、一般ユーザ向け画面と管理ユーザ向け画面を提供する。

起動前の確認:

- Node.jsを使用できる。
- パッケージ依存関係をインストール済みである。
- APIの接続先URLが環境変数で設定されている。
- ブラウザへ公開してよい値だけをNext.jsの公開環境変数として扱っている。

依存関係インストール:

```bash
npm install
```

起動:

```bash
npm run dev
```

pnpmまたはyarnを採用する場合は、ロックファイルとあわせてこの手順を更新する。

起動後の確認:

- 開発サーバへブラウザからアクセスできる。
- APIへの通信先がローカルAPIになっている。
- ログイン、一覧、検索、閲覧、管理画面など、実装済み画面でAPIエラー、読み込み中、空状態を確認できる。
- ブラウザコンソールに不要なエラーやシークレット出力がない。

## 全体をDocker Composeで起動する場合

ローカルでもアプリケーションを含めてDocker Composeで起動できる構成にした場合は、次の流れを候補とする。

```bash
docker compose up -d
```

状態確認:

```bash
docker compose ps
```

ログ確認:

```bash
docker compose logs --tail=200 api
docker compose logs --tail=200 worker
docker compose logs --tail=200 frontend
```

ビルドし直す場合:

```bash
docker compose up -d --build
```

Composeでアプリケーションを起動する場合も、PostgreSQL、Elasticsearch、専用キュー、保存領域のデータを不用意に削除しない。

## DBマイグレーションと初期データ

DBマイグレーションツールは実装時に確定する。FlywayまたはLiquibaseを採用する場合は、バックエンドAPI起動時または専用コマンドでマイグレーションを実行する。

確認観点:

- マイグレーションはPostgreSQLに対して実行する。
- Elasticsearchは正本ではないため、必要に応じてPostgreSQLから再インデックスする。
- 開発用初期データには本番データや秘密情報を含めない。
- 管理ユーザ初期作成の手順は、実装時に安全な方法を定義する。

コマンド例:

```bash
./gradlew flywayMigrate
```

実際のコマンドは、採用するビルドツールとマイグレーションツールに合わせて更新する。

## テスト

変更範囲に応じて、単体テスト、結合テスト、E2Eテスト、手動確認を使い分ける。

バックエンドのテスト例:

```bash
./gradlew test
```

フロントエンドのテスト例:

```bash
npm test
```

E2Eテスト例:

```bash
npm run test:e2e
```

確認観点:

- ビジネスルール、入力検証、権限確認、変換ジョブ状態遷移を優先してテストする。
- PostgreSQL、Elasticsearch、ファイルシステム、7-Zip、専用キューなどの外部依存は、必要に応じてモック、統合テスト、手動確認を使い分ける。
- 変換ワーカーの長時間処理や大きなファイルを扱うテストは、通常の単体テストと分ける。
- ドキュメントのみの変更では、Markdownの見出し構造、リンク、パス、TODO状態を確認する。

## ログ確認

ローカル開発では、API、Worker、フロントエンド、ミドルウェアのログを分けて確認する。

Docker Composeログ:

```bash
docker compose logs --tail=200
docker compose logs -f worker
```

確認観点:

- APIリクエスト、変換ジョブ、検索インデックス更新を安全な識別子で追跡できる。
- パスワード、トークン、セッションID、秘密鍵、不要な個人情報が出ていない。
- 7-Zip外部プロセスの終了コード、タイムアウト、失敗理由が調査可能な形で残る。
- PostgreSQL、Elasticsearch、専用キューへの接続失敗が区別できる。

## よく使う開発コマンド

| 用途 | コマンド例 |
| --- | --- |
| ミドルウェア起動 | `docker compose up -d postgres elasticsearch queue` |
| 全サービス起動 | `docker compose up -d` |
| コンテナ状態確認 | `docker compose ps` |
| 全体ログ確認 | `docker compose logs --tail=200` |
| Workerログ追跡 | `docker compose logs -f worker` |
| バックエンドテスト | `./gradlew test` |
| バックエンド起動 | `./gradlew bootRun --args='--spring.profiles.active=local'` |
| Worker起動 | `./gradlew bootRun --args='--spring.profiles.active=local,worker'` |
| フロントエンド依存関係インストール | `npm install` |
| フロントエンド起動 | `npm run dev` |
| フロントエンドテスト | `npm test` |

コマンドは初期候補であり、プロジェクト構成が確定したら実際のディレクトリ、サービス名、スクリプト名へ更新する。

## トラブルシューティング

| 症状 | 確認観点 |
| --- | --- |
| APIが起動しない | Java 25、環境変数、PostgreSQL接続、ポート競合を確認する。 |
| Workerがジョブを取得しない | 専用キュー接続、キュー名、Worker起動プロファイル、同時実行数を確認する。 |
| 7-Zip実行に失敗する | `SEVEN_ZIP_PATH`、実行権限、コンテナ内配置、対象アーカイブ形式を確認する。 |
| 検索結果が古い | PostgreSQLを正として、Elasticsearch再インデックス対象か確認する。 |
| 画像が表示されない | PostgreSQLのページ情報、WebP保存領域、APIの画像配信、ファイル権限を確認する。 |
| フロントエンドからAPIへ接続できない | API起動状態、CORS、API URL環境変数、ブラウザコンソールを確認する。 |
| ローカルデータを初期化したい | 対象が開発環境であること、削除対象ボリュームと保存領域を確認する。 |

## 開発時の注意点

- ローカル環境の秘密情報をGitへ含めない。
- 原本ファイル、変換済みWebP、サムネイル、大容量の生成物をGitへ含めない。
- API、Worker、ミドルウェアを将来分離できるよう、接続先や保存先を設定で切り替えられるようにする。
- フロントエンドの表示制御だけを権限制御として扱わない。
- アップロードファイル、アーカイブ内パス、ジョブパラメータはバックエンドまたはWorkerで検証する。
- Elasticsearchを正本として扱わず、不整合時はPostgreSQLから再構築する。

## 更新方針

アプリケーションのディレクトリ構成、ビルドツール、Docker Composeサービス名、起動プロファイル、テストコマンド、ログ形式、管理コマンドが確定または変更された場合は、このドキュメントを更新する。
