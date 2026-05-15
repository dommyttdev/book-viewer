# Issue #85 ローカルミドルウェアのDocker Composeを用意する

## 目的

issue #48 / PBI-001 のsub-issueとして、ローカル開発で使用するPostgreSQL、Elasticsearch、RabbitMQをDocker Composeで起動、状態確認、ログ確認、停止できるようにするための実装入力、受け入れ条件、確認観点を整理する。

この文書はSprint S0の作業成果物である。実装では、ルート `compose.yaml` と必要な補助ファイルを作成し、確認結果をこの文書と `test-report.md` に記録する。

## GitHub Issue

- Issue: #85 ローカルミドルウェアのDocker Composeを用意する
- Parent: #48 開発者として、フロントエンド、API、ワーカーをローカルで起動できるようにしたい
- Labels: `type:feature`, `area:infra`, `area:db`, `area:search`

## 受け入れ条件

| 条件 | 判定方法 | 現時点 |
| --- | --- | --- |
| PostgreSQLをDocker Composeで起動できる。 | `docker compose up -d postgres` 後に `docker compose ps postgres` で起動状態を確認できる。 | 完了。`docker compose ps` でhealthy、`pg_isready` でaccepting connectionsを確認。 |
| ElasticsearchをDocker Composeで起動できる。 | `docker compose up -d elasticsearch` 後にHTTP疎通と必須プラグインの確認ができる。 | 完了。HTTP疎通と `analysis-kuromoji` / `analysis-icu` を確認。 |
| RabbitMQをDocker Composeで起動できる。 | `docker compose up -d rabbitmq` 後に `docker compose ps rabbitmq` と管理UIまたは `rabbitmqctl status` で状態確認できる。 | 完了。`docker compose ps` でhealthy、`rabbitmqctl status` で起動状態を確認。 |
| 状態確認、ログ確認、停止コマンドが利用できる。 | `docker compose ps`、`docker compose logs --tail=200 <service>`、`docker compose down` が実行できる。 | 完了。状態、ログ、停止を確認。 |

## 採用方針

| 項目 | 方針 |
| --- | --- |
| Compose定義 | ルート `compose.yaml` に置く。 |
| 対象サービス | `postgres`、`elasticsearch`、`rabbitmq`。 |
| PostgreSQL | Sprint S0では公式イメージを直接参照する。DB名、ユーザ、パスワードは `.env` で上書き可能にする。`.env.example` は安全なサンプルとしてGit管理し、実値を含む `.env` はGit管理外にする。 |
| Elasticsearch | 技術スタックで定義された `analysis-kuromoji` と `analysis-icu` を利用できる構成にする。必要に応じて `docker/elasticsearch/` にカスタムイメージ定義を置く。 |
| RabbitMQ | 管理UI付き公式イメージを使用する。ユーザ、パスワード、公開ポートはローカル開発用の値を `.env` で上書き可能にする。 |
| データ保持 | PostgreSQL、Elasticsearch、RabbitMQはDocker volumeを使用する。通常の停止ではvolumeを削除しない。 |
| ネットワーク | Compose内のサービス名で相互参照できるよう、同一Composeネットワークに配置する。 |
| アプリケーション | Sprint S0の #85 ではAPI、Worker、FrontendのCompose起動は対象外にし、ローカルミドルウェアだけを扱う。 |

## サービス名と公開ポート

初期候補は次のとおりとする。実装時に競合が見つかった場合は、`.env.example` とローカル開発手順へ実値を反映する。

| サービス | Composeサービス名 | コンテナ内ポート | ローカル公開ポート候補 | 用途 |
| --- | --- | --- | --- | --- |
| PostgreSQL | `postgres` | `5432` | `5432` | 正本データストア。 |
| Elasticsearch | `elasticsearch` | `9200` | `9200` | 検索用派生インデックス。 |
| RabbitMQ | `rabbitmq` | `5672`, `15672` | `5672`, `15672` | 変換ジョブ配送と管理UI。 |

## 環境変数方針

Compose定義にはローカル開発用の既定値を持たせる。`.env.example` には安全なサンプル値を置く。実際の `.env` はGit管理外とし、本番用の秘密情報や実データの接続情報を入れない。

| 変数 | 用途 | サンプル方針 |
| --- | --- | --- |
| `POSTGRES_DB` | 開発用DB名 | `manga` |
| `POSTGRES_USER` | 開発用DBユーザ | `manga` |
| `POSTGRES_PASSWORD` | 開発用DBパスワード | ローカル専用のダミー値 |
| `POSTGRES_PORT` | ホスト側PostgreSQL公開ポート | `5432` |
| `ELASTICSEARCH_PORT` | ホスト側Elasticsearch公開ポート | `9200` |
| `RABBITMQ_DEFAULT_USER` | RabbitMQ開発用ユーザ | `manga` |
| `RABBITMQ_DEFAULT_PASS` | RabbitMQ開発用パスワード | ローカル専用のダミー値 |
| `RABBITMQ_PORT` | ホスト側AMQP公開ポート | `5672` |
| `RABBITMQ_MANAGEMENT_PORT` | ホスト側管理UI公開ポート | `15672` |

API / WorkerのSpring Boot設定で使う `DATABASE_URL`、`DATABASE_USERNAME`、`DATABASE_PASSWORD`、`ELASTICSEARCH_URL`、`RABBITMQ_HOST` などとの対応は #86 で整理する。

## Elasticsearch必須プラグイン

Elasticsearchの必須プラグインは [技術スタックのElasticsearch必須プラグイン](../../../../doc/03_architecture/02_technology_stack.md#elasticsearch必須プラグイン) を正本とする。

Sprint S0の #85 では、少なくとも次のどちらかを満たす。

- `analysis-kuromoji` と `analysis-icu` を導入したElasticsearchイメージを `docker/elasticsearch/` でビルドできる。
- 公式イメージを使う場合は、起動後に必須プラグインを確認する明確な手順と、未導入時に #85 を完了扱いにしない判断を記録する。

検索インデックス定義やAPI起動時の必須プラグイン検証は #86 以降または検索関連PBIで扱う。

## TDD / Red開始点

最初のRedは、Compose定義が存在しない、または対象サービスを起動できない状態を確認するところから開始する。

| 観点 | 最初に確認すること | 種別 |
| --- | --- | --- |
| Compose定義 | `docker compose config` が、未作成状態では失敗する。 | Red |
| PostgreSQL起動 | `docker compose up -d postgres` 後に起動状態を確認できる。 | Green |
| Elasticsearch起動 | `docker compose up -d elasticsearch` 後にHTTP疎通と必須プラグイン確認ができる。 | Green |
| RabbitMQ起動 | `docker compose up -d rabbitmq` 後にAMQPポートまたは管理UI、`rabbitmqctl status` を確認できる。 | Green |
| ログ確認 | 各サービスで `docker compose logs --tail=200 <service>` が利用できる。 | 手動 |
| 停止 | `docker compose down` でコンテナを停止できる。 | 手動 |
| ログ安全性 | Compose定義、`.env.example`、ログに本番秘密情報や不要な個人情報が出ない。 | 手動 |

## 確認コマンド案

Windows PowerShell、Linux、macOSともにDocker Compose v2の `docker compose` を使用する。

```bash
docker compose config
docker compose up -d postgres elasticsearch rabbitmq
docker compose ps
docker compose logs --tail=200 postgres
docker compose logs --tail=200 elasticsearch
docker compose logs --tail=200 rabbitmq
docker compose down
```

PostgreSQL:

```bash
docker compose exec postgres pg_isready -U manga -d manga
```

Elasticsearch:

```bash
curl http://localhost:9200
curl http://localhost:9200/_cat/plugins
```

RabbitMQ:

```bash
docker compose exec rabbitmq rabbitmqctl status
docker compose exec rabbitmq rabbitmqctl list_queues name messages messages_unacknowledged
```

ポート番号、ユーザ名、DB名は実装時の `.env.example` に合わせて更新する。

## 実装タスク

- [x] Red: `compose.yaml` 未作成またはミドルウェア未定義の状態で、Compose確認が失敗することを確認する。
- [x] Green: ルート `compose.yaml` に `postgres`、`elasticsearch`、`rabbitmq` を定義する。
- [x] Green: Elasticsearch必須プラグインを利用できる構成を用意する。
- [x] Green: `.env.example` にローカルミドルウェア用の安全なサンプル値を定義し、実値を含む `.env` はGit管理外にする。
- [x] Green: 状態確認、ログ確認、停止コマンドを実行できることを確認する。
- [x] Refactor: サービス名、ポート、volume、ネットワーク、環境変数名が既存ドキュメントと矛盾していないことを確認する。
- [x] Document: 実行したコマンド、確認結果、未実行項目、ログ安全性確認を `test-report.md` へ追記する。

## 対象外

- API、Worker、FrontendをDocker Composeで起動する構成。
- Spring Boot API / WorkerからPostgreSQL、Elasticsearch、RabbitMQへ実接続する設定。
- DBマイグレーション、スキーマ作成、初期データ投入。
- RabbitMQのexchange、queue、dead letter queueの業務設定。
- Elasticsearchのインデックス作成、検索API、再インデックス処理。
- 本番用Docker Composeの完成版。
- Docker volume削除やローカルデータ初期化手順の詳細化。

API / Workerの外部依存設定と疎通確認は #86 で扱う。最小テストと確認コマンドの横断整理は #87 で扱う。本番相当のCompose統合はPBI-020で扱う。

## 実施結果記録欄

実装後、次を追記する。

- 作成したCompose定義と補助ファイル: ルート `compose.yaml`、`.env.example`、`docker/elasticsearch/Dockerfile`。
- 使用した主要イメージとバージョン: PostgreSQL `postgres:17-alpine`、Elasticsearch `docker.elastic.co/elasticsearch/elasticsearch:8.17.0` ベースのカスタムイメージ、RabbitMQ `rabbitmq:4-management-alpine`。
- 実行した確認コマンドと結果: Redとして `docker compose config` が `no configuration file provided: not found` で失敗することを確認した。実装後は `docker compose config --quiet` が成功した。初回はDocker daemon未起動で起動確認に失敗したが、Docker Desktop起動後に権限付きで再実行し、`docker compose up -d postgres elasticsearch rabbitmq` が成功した。
- PostgreSQLの起動確認結果: `docker compose ps` で `manga-postgres` がhealthy。`docker compose exec postgres pg_isready -U manga -d manga` が `/var/run/postgresql:5432 - accepting connections` を返した。
- Elasticsearchの起動確認結果と必須プラグイン確認結果: `manga-elasticsearch` がhealthy。`Invoke-RestMethod http://localhost:9200` でElasticsearch 8.17.0の応答を確認した。`docker compose exec elasticsearch bin/elasticsearch-plugin list` と `http://localhost:9200/_cat/plugins` で `analysis-icu`、`analysis-kuromoji` を確認した。
- RabbitMQの起動確認結果: `manga-rabbitmq` がhealthy。`docker compose exec rabbitmq rabbitmqctl status` でRabbitMQ 4.3.0、AMQP 5672、管理UI 15672のlistenerを確認した。
- 状態確認、ログ確認、停止確認の結果: `docker compose ps`、`docker compose logs --tail=30 postgres`、`docker compose logs --tail=30 elasticsearch`、`docker compose logs --tail=30 rabbitmq` を確認した。`docker compose down` により3コンテナと既定ネットワークが停止、削除された。volumeは削除していない。
- ログとサンプル設定に秘密情報が含まれていないことの確認結果: Compose定義内の既定値と `.env.example` はローカル開発用のダミー値のみであり、本番秘密情報は含めていない。確認したログに本番秘密情報、トークン、不要な個人情報は出力されていない。
- 未実行の確認と理由: API / Workerから各ミドルウェアへの接続確認は #86 で扱うため未実行。RabbitMQの業務queue、dead letter queue作成確認は後続PBIで扱うため未実行。
- 更新したドキュメント: `development/scrum/sprints/sprint-s0/issue-85-local-middleware-compose.md`、`development/scrum/sprints/sprint-s0/test-report.md`。
