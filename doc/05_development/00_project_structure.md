# プロジェクト構成

## 目的

このドキュメントは、自炊本閲覧Webアプリケーションのリポジトリ構成、アプリケーション配置、ビルド単位、起動単位を定義する。

リポジトリ構成の正本はこのドキュメントとする。READMEや環境構築手順では、このドキュメントを参照し、構成ツリーを重複定義しない。

## リポジトリ構成

アプリケーションコードはルート直下の `apps/` に集約する。APIとWorkerで本当に共有すべき最小限のJavaコードは `libs/backend-common/` に置く。

```text
.
├── apps/
│   ├── frontend/        # Next.jsフロントエンド
│   ├── api/             # Spring BootバックエンドAPI
│   └── worker/          # Spring Boot変換ワーカー
├── libs/
│   └── backend-common/  # APIとWorkerで共有する最小限のJavaコード
├── docker/
│   └── elasticsearch/   # 必須プラグイン入りElasticsearchカスタムイメージ等
├── compose.yaml         # ローカルミドルウェアと将来の全体起動
├── .env.example         # ローカル設定の安全なサンプル
├── doc/
├── development/
├── rules/
├── AGENTS.md
└── README.md
```

## アプリケーション配置

| パス | 用途 |
| --- | --- |
| `apps/frontend/` | Next.jsフロントエンド。一般ユーザ向け画面、管理ユーザ向け画面、API呼び出し、画面状態管理を担当する。 |
| `apps/api/` | Spring BootバックエンドAPI。HTTP API、認証、認可、入力検証、書籍管理、検索、閲覧、管理操作、ジョブ投入を担当する。 |
| `apps/worker/` | Spring Boot変換ワーカー。RabbitMQからジョブを取得し、アーカイブ展開、WebP変換、サムネイル生成、ジョブ状態更新を担当する。 |
| `libs/backend-common/` | APIとWorkerで共有する最小限のJavaコード。 |
| `docker/elasticsearch/` | Elasticsearch必須プラグイン入りカスタムイメージなど、Elasticsearch固有の補助ファイル。 |
| `compose.yaml` | ローカルミドルウェアと将来の全体起動用Docker Compose定義。 |

`docker/` は、Docker Composeで使う全サービスの定義を個別ディレクトリとして必ず置く場所ではない。PostgreSQLとRabbitMQは、Sprint S0時点では公式イメージを `compose.yaml` から直接参照する想定のため、専用ディレクトリを作らない。

Elasticsearchだけは `analysis-kuromoji` と `analysis-icu` の必須プラグイン導入が必要なため、カスタムイメージまたは補助スクリプトの置き場として `docker/elasticsearch/` を用意する。将来PostgreSQL、RabbitMQ、API、Worker、FrontendにもカスタムDockerfileや補助ファイルが必要になった場合は、`docker/postgres/`、`docker/rabbitmq/`、`docker/api/`、`docker/worker/`、`docker/frontend/` などを追加する。

## ビルドツールと起動単位

| 領域 | 方針 |
| --- | --- |
| フロントエンド | npmを使用する。ロックファイルは `apps/frontend/package-lock.json` とする。 |
| バックエンド | ルートGradleマルチプロジェクトを使用する。 |
| Gradle DSL | Kotlin DSLを使用する。 |
| Gradle Wrapper | リポジトリにGradle Wrapperを含める。 |
| API | `apps/api` を別Gradleサブプロジェクト、別Spring Bootアプリケーションとして扱う。 |
| Worker | `apps/worker` を別Gradleサブプロジェクト、別Spring Bootアプリケーションとして扱う。 |
| 共通Javaコード | `libs/backend-common` を別Gradleサブプロジェクトとして扱う。 |

Gradleのinclude名は次を基本とする。

```kotlin
include(":apps:api")
include(":apps:worker")
include(":libs:backend-common")
```

ルートの `settings.gradle.kts` では、`rootProject.name = "manga"` とする。

APIとWorkerは、同じSpring Boot 4.0.6 / Java 25を使うが、別プロセスとして起動する。変換処理のCPU、メモリ、ディスクI/O、外部プロセス実行の負荷をHTTP APIから分離し、将来の別コンテナ化や別ホスト化を容易にする。

## 共有モジュールの扱い

`libs/backend-common` は、設定プロパティ、例外基底、ID型、ログ方針など薄い技術的共通部品に限定する。

ドメインロジックを安易に共有モジュールへ集約しない。モジュール境界をまたぐ業務判断は、各アプリケーションまたは明確なドメインモジュールで扱う。

## 関連ドキュメント

- [環境構築手順](03_environment_setup.md)
- [ローカル開発手順](04_local_development.md)
- [コンテナ図](../03_architecture/05_container_diagram.md)
- [技術スタック](../03_architecture/02_technology_stack.md)

## 更新方針

リポジトリ構成、アプリケーション配置、ビルド単位、起動単位が変わった場合は、このドキュメントを更新する。README、環境構築手順、ローカル開発手順では、必要最小限の要約とこのドキュメントへの参照に留める。
