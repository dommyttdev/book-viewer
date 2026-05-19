# Issue #81 プロジェクト構成とビルド方針

## 目的

issue #48 / PBI-001 の最初のsub-issueとして、フロントエンド、API、Workerのディレクトリ構成、起動単位、ビルドツール、起動コマンド方針を確定する。

この文書はSprint S0の開発成果物であり、実装着手時の入力として扱う。決定事項は `doc/05_development/00_project_structure.md` を正本として反映済みである。README、環境構築手順、ローカル開発手順では同じ構成ツリーを重複管理せず、正本ドキュメントを参照する。

設計判断を先行するsub-issueの完了扱いは、[Definition of Doneの設計判断を先行するsub-issueの扱い](../../../../doc/05_development/05_definition_of_done.md#設計判断を先行するsub-issueの扱い) に従う。

## 決定

### リポジトリ構成

アプリケーションコードはルート直下の `apps/` に集約する。

```text
.
├── apps/
│   ├── frontend/        # Next.jsフロントエンド
│   ├── api/             # Spring BootバックエンドAPI
│   └── worker/          # Spring Boot変換ワーカー
├── libs/
│   └── backend-common/  # APIとWorkerで共有する最小限のJavaコード
├── docker/
│   └── elasticsearch/   # 必須プラグイン入りElasticsearchイメージ等
├── compose.yaml         # ローカルミドルウェアと将来の全体起動
├── .env.example         # ローカル設定の安全なサンプル
├── doc/
├── development/
├── rules/
└── README.md
```

### フロントエンド

| 項目 | 方針 |
| --- | --- |
| ディレクトリ | `apps/frontend/` |
| 技術 | Next.js |
| パッケージマネージャ | npm |
| ロックファイル | `apps/frontend/package-lock.json` |
| Node.js | 当面はローカル確認済みの v22 系を前提にし、生成時に `package.json` の `engines` へ固定方針を記録する。 |
| 開発起動 | `cd apps/frontend && npm run dev` |
| Windows PowerShell | `npm.ps1` が実行ポリシーでブロックされる場合は `npm.cmd run dev` を使う。 |
| 最小確認 | `npm run lint`、`npm run typecheck`、必要に応じて `npm test` |

Next.jsは画面表示、入力補助、API呼び出し、ユーザ体験上の状態管理に責務を限定する。認証、認可、入力値、アップロードファイルの最終検証はAPIで行う。

### バックエンド

| 項目 | 方針 |
| --- | --- |
| ビルド | ルートGradleマルチプロジェクト |
| DSL | Kotlin DSL |
| Wrapper | Gradle Wrapperをリポジトリに含める |
| Java | Java 25 toolchain |
| Spring Boot | 4.0.6 |
| APIモジュール | `apps/api` |
| Workerモジュール | `apps/worker` |
| 共通モジュール | `libs/backend-common` |
| API起動 | `./gradlew :apps:api:bootRun --args='--spring.profiles.active=local'` |
| Worker起動 | `./gradlew :apps:worker:bootRun --args='--spring.profiles.active=local'` |
| APIテスト | `./gradlew :apps:api:test` |
| Workerテスト | `./gradlew :apps:worker:test` |
| 全バックエンドテスト | `./gradlew test` |

APIとWorkerは、同じSpring Boot / Java 25を使うが、別Gradleサブプロジェクト、別Spring Bootアプリケーション、別プロセスとして扱う。これにより、変換処理の負荷とHTTP APIの応答性を分離し、将来の別コンテナ化や別ホスト化を容易にする。

### Gradleプロジェクト名

Gradleのinclude名は次の方針にする。

```kotlin
include(":apps:api")
include(":apps:worker")
include(":libs:backend-common")
```

ルートの `settings.gradle.kts` では、リポジトリ名に合わせて `rootProject.name = "manga"` とする。

### APIとWorkerの責務境界

| 領域 | API | Worker |
| --- | --- | --- |
| HTTP API | 担当する | 担当しない |
| 認証 / 認可 | 担当する | 原則としてAPIで検証済みジョブを処理する |
| アップロード受付 | 担当する | 担当しない |
| 原本保存 | 担当する | 参照する |
| RabbitMQへのジョブ投入 | 担当する | 担当しない |
| RabbitMQからのジョブ取得 | 担当しない | 担当する |
| アーカイブ展開 | 担当しない | 担当する |
| WebP変換 / サムネイル生成 | 担当しない | 担当する |
| PostgreSQL | 正本データを参照 / 更新する | ジョブ状態とページ情報を更新する |
| Elasticsearch | 検索要求と検索更新を担当する | 変換完了後の更新契機を扱う余地を残す |

`libs/backend-common` は、設定プロパティ、例外基底、ID型、ログ方針など、本当に共有すべき薄い技術的共通部品に限定する。ドメインロジックを安易に共有モジュールへ集約しない。

### Docker Compose

Compose定義はルートの `compose.yaml` に置く。Sprint S0では、まずローカルミドルウェアを対象にする。

| サービス名 | 用途 |
| --- | --- |
| `postgres` | PostgreSQL |
| `elasticsearch` | Elasticsearch。必須プラグインは技術スタックを参照する。 |
| `rabbitmq` | RabbitMQ |

将来、アプリケーションもComposeに含める場合は、`frontend`、`api`、`worker` を追加する。ただし、ローカル開発ではホスト上で各アプリを起動できることを優先する。

### 環境変数と設定ファイル

安全なサンプルとして `.env.example` を置き、実値を含む `.env` はGit管理外にする。

最初に確定する主な変数は次のとおり。

| 変数 | 用途 |
| --- | --- |
| `DATABASE_URL` | PostgreSQL接続URL |
| `DATABASE_USERNAME` | PostgreSQLユーザ |
| `DATABASE_PASSWORD` | PostgreSQLパスワード |
| `ELASTICSEARCH_URL` | Elasticsearch接続URL |
| `RABBITMQ_HOST` | RabbitMQホスト |
| `RABBITMQ_PORT` | RabbitMQポート |
| `RABBITMQ_USERNAME` | RabbitMQユーザ |
| `RABBITMQ_PASSWORD` | RabbitMQパスワード |
| `BOOK_STORAGE_ROOT` | 原本保存領域 |
| `CONVERTED_IMAGE_STORAGE_ROOT` | 変換済み画像保存領域 |
| `THUMBNAIL_STORAGE_ROOT` | サムネイル保存領域 |
| `WORKER_WORK_DIR` | Worker作業ディレクトリ |
| `SEVEN_ZIP_PATH` | 7-Zip実行ファイルパス |
| `WEBP_QUALITY` | WebP品質値。既定値は80。 |

### README / 開発手順への反映先

Issue #81 の方針が実装された時点で、次の文書を更新する。

- `README.md`: リポジトリ構成の概要に `apps/`、`libs/`、`docker/`、`compose.yaml` を追加する。
- `doc/05_development/03_environment_setup.md`: Node.js、npm、Gradle Wrapper、Composeサービス名、環境変数名を実構成に合わせる。
- `doc/05_development/04_local_development.md`: フロントエンド、API、Worker、ミドルウェアの実起動コマンドへ更新する。

## 受け入れ条件との対応

| 受け入れ条件 | 対応 |
| --- | --- |
| フロントエンド、API、Workerのディレクトリ構成が決まっている。 | `apps/frontend`、`apps/api`、`apps/worker`、`libs/backend-common` に決定。 |
| APIとWorkerの起動単位が決まっている。 | 別Gradleサブプロジェクト、別Spring Bootアプリケーション、別プロセスに決定。 |
| ビルドツール、パッケージマネージャ、起動コマンドの方針が決まっている。 | バックエンドはGradle Wrapper + Kotlin DSL、フロントエンドはnpm。起動コマンド方針を記載。 |
| READMEまたはローカル開発手順から構成を確認できる。 | `README.md` と `doc/05_development/03_environment_setup.md`、`doc/05_development/04_local_development.md` から、正本である `doc/05_development/00_project_structure.md` を参照できる。 |

## 後続Issueへの入力

| Issue | 入力 |
| --- | --- |
| #82 | `apps/frontend` にNext.js最小構成を作成し、npm scriptsを定義する。 |
| #83 | `apps/api` にSpring Boot APIを作成し、ヘルスチェックのRedから始める。 |
| #84 | `apps/worker` にSpring Boot Workerを作成し、Worker起動と設定読み込みを確認する。 |
| #85 | ルート `compose.yaml` に `postgres`、`elasticsearch`、`rabbitmq` を定義する。 |
| #86 | API / Workerのlocal profileで環境変数と外部依存設定を読み込めるようにする。 |
| #87 | `npm run lint`、`npm run typecheck`、`./gradlew test`、Compose確認コマンドを整備する。 |
| #88 | 実装結果を `README.md`、環境構築手順、ローカル開発手順、TODOへ反映する。GitHub Issue / Project更新は #88 の残タスクとして扱う。 |

## 未決事項

- Gradleの正確なバージョンは、Spring Boot 4.0.6とJava 25の組み合わせで生成時に確定する。
- Next.jsの正確なバージョンは、#82で作成時に確定し、`package.json` とロックファイルへ固定する。
- Elasticsearch必須プラグインのDockerイメージ作成方法は、#85またはSPIKE-003で具体化する。
- API / Worker共通モジュールに置く型は、実装時に最小限から始める。

## 完了判定

この文書と `doc/` への反映により、#81の設計判断とドキュメント化は完了とする。GitHub Issue #81のチェックリストは、実装またはProject運用上の更新タイミングで反映する。
