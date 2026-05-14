# 権限マトリクス

## 目的

このドキュメントは、管理ユーザロールと主要操作の可否を整理し、Spring BootバックエンドAPIで実施する権限確認の基準を定義する。

フロントエンドのメニューやボタン表示は利便性のために行う。セキュリティ境界はバックエンドAPIの認証、認可、対象リソース状態の確認に置く。

## 前提

- PostgreSQLをロールと権限の正本とする。
- Elasticsearchは派生データであり、検索結果もAPI側の権限確認に従う。
- 一般ユーザは書籍を保持せず、書籍アップロード、書籍削除、変換ジョブ操作、管理API操作を行わない。
- 管理ユーザには1つ以上の管理ロールを割り当てられる。
- 複数ロールを持つ管理ユーザは、付与ロールの許可の和集合で操作可否を判断する。
- 権限不足時は、対象リソースや内部状態を過度に推測できない応答にする。

## ロール定義

| ロール | 目的 | 主な利用者像 |
| --- | --- | --- |
| `super_admin` | システム全体の管理。 | 初期管理者、権限管理責任者 |
| `admin` | 日常的な管理業務。 | 書籍と一般ユーザを管理する担当者 |
| `operator` | 取込、変換、検索インデックス運用。 | アップロードと変換復旧を担当する運用寄り担当者 |
| `viewer` | 管理情報の参照。 | 問い合わせ対応や状況確認のみ行う担当者 |

## 権限コード

| 権限コード | 対象操作 |
| --- | --- |
| `admin_user.read` | 管理ユーザ一覧、詳細参照 |
| `admin_user.manage` | 管理ユーザ登録、編集、停止、削除 |
| `role.read` | ロール一覧、権限一覧参照 |
| `role.manage` | ロール設定、ロールへの権限割り当て |
| `user.read` | 一般ユーザ一覧、詳細参照 |
| `user.manage` | 一般ユーザ停止、復帰 |
| `book.read` | 書籍一覧、詳細、メタ情報参照 |
| `book.upload` | 書籍アップロード、変換ジョブ作成 |
| `book.update` | 書籍メタ情報編集、公開状態変更 |
| `book.delete` | 書籍削除 |
| `book.view` | 変換済み書籍閲覧、ページ画像取得 |
| `conversion_job.read` | 変換ジョブ状態参照 |
| `conversion_job.retry` | 変換ジョブ再実行 |
| `search.reindex` | 書籍単位または全件のElasticsearch再インデックス |
| `favorite.manage` | 自分のお気に入り登録、解除 |
| `reading_history.manage` | 自分の閲覧履歴保存、更新 |
| `profile.manage` | 自分の会員情報編集、退会 |

## `book.view` の意味

`book.view` は、認証済み利用者が変換済みページやサムネイルを取得するための権限である。ただし、管理ユーザと一般ユーザでは利用目的と対象範囲を分けて扱う。

| 利用者 | 意味 | 対象範囲 |
| --- | --- | --- |
| 一般ユーザ | 公開・閲覧可能状態の書籍を読むための利用者権限。 | 有効、変換済み、閲覧可能状態の書籍。お気に入りや閲覧履歴は自分のデータのみ操作できる。 |
| 管理ユーザ | 管理対象書籍の変換結果、ページ順、表示品質、メタ情報整合性を確認するための管理権限。 | システムに登録された管理対象書籍。一般ユーザが保持する書籍を管理者が閲覧する権限ではない。 |

初期設計では一般ユーザは書籍を保持しないため、一般ユーザ所有書籍に対する管理者閲覧権限は定義しない。管理ユーザによる `book.view` は、一般ユーザ体験と同等の読書機能ではなく、登録済み書籍を公開または運用するための内容確認・品質確認として扱う。

## ロール別操作可否

| 操作 | 必要権限 | `super_admin` | `admin` | `operator` | `viewer` | 一般ユーザ |
| --- | --- | --- | --- | --- | --- | --- |
| 管理ユーザ一覧 / 詳細 | `admin_user.read` | 可 | 不可 | 不可 | 不可 | 不可 |
| 管理ユーザ登録 | `admin_user.manage` | 可 | 不可 | 不可 | 不可 | 不可 |
| 管理ユーザ編集 | `admin_user.manage` | 可 | 不可 | 不可 | 不可 | 不可 |
| 管理ユーザ削除 / 停止 | `admin_user.manage` | 可 | 不可 | 不可 | 不可 | 不可 |
| ロール一覧 / 権限一覧 | `role.read` | 可 | 不可 | 不可 | 不可 | 不可 |
| ロール設定 | `role.manage` | 可 | 不可 | 不可 | 不可 | 不可 |
| 一般ユーザ一覧 / 詳細 | `user.read` | 可 | 可 | 不可 | 可 | 不可 |
| 一般ユーザ停止 / 復帰 | `user.manage` | 可 | 可 | 不可 | 不可 | 不可 |
| 書籍一覧 / 詳細参照 | `book.read` | 可 | 可 | 可 | 可 | 可 |
| 書籍アップロード | `book.upload` | 可 | 可 | 可 | 不可 | 不可 |
| 書籍メタ情報編集 | `book.update` | 可 | 可 | 不可 | 不可 | 不可 |
| 書籍削除 | `book.delete` | 可 | 可 | 不可 | 不可 | 不可 |
| 書籍閲覧 | `book.view` | 可 | 可 | 可 | 可 | 可 |
| 変換ジョブ状態参照 | `conversion_job.read` | 可 | 可 | 可 | 可 | 不可 |
| 変換ジョブ再実行 | `conversion_job.retry` | 可 | 可 | 可 | 不可 | 不可 |
| Elasticsearch再インデックス | `search.reindex` | 可 | 不可 | 可 | 不可 | 不可 |
| お気に入り管理 | `favorite.manage` | 不可 | 不可 | 不可 | 不可 | 可 |
| 閲覧履歴保存 | `reading_history.manage` | 不可 | 不可 | 不可 | 不可 | 可 |
| 会員情報編集 / 退会 | `profile.manage` | 不可 | 不可 | 不可 | 不可 | 可 |

## ロール初期権限

| ロール | 初期権限 |
| --- | --- |
| `super_admin` | `admin_user.read`, `admin_user.manage`, `role.read`, `role.manage`, `user.read`, `user.manage`, `book.read`, `book.upload`, `book.update`, `book.delete`, `book.view`, `conversion_job.read`, `conversion_job.retry`, `search.reindex` |
| `admin` | `user.read`, `user.manage`, `book.read`, `book.upload`, `book.update`, `book.delete`, `book.view`, `conversion_job.read`, `conversion_job.retry` |
| `operator` | `book.read`, `book.upload`, `book.view`, `conversion_job.read`, `conversion_job.retry`, `search.reindex` |
| `viewer` | `user.read`, `book.read`, `book.view`, `conversion_job.read` |

一般ユーザの権限は管理ロールではなく、一般ユーザとして認証済みであることと対象データが自分に属することを合わせて確認する。一般ユーザの初期権限は `book.read`, `book.view`, `favorite.manage`, `reading_history.manage`, `profile.manage` とする。

## 操作別の注意点

### 管理ユーザ管理

- `admin_user.manage` は `super_admin` のみに付与する。
- 最後の `super_admin` を停止、削除、または `super_admin` から外す操作は拒否する。
- 管理ユーザ削除は論理削除または停止を基本とし、過去の操作主体参照を壊さない。
- 管理ユーザ停止、削除、ロール変更時は必要に応じて既存の管理セッションを失効する。

### ロール設定

- `role.manage` は `super_admin` のみに付与する。
- MVPでは固定ロールを基本とし、ロール設定変更APIは Beta / v1.0 候補として扱う。
- 権限コードを追加、削除する場合はAPI契約、データモデル、受入テストを同期する。

### 一般ユーザ管理

- `user.read` は `super_admin`, `admin`, `viewer` に付与する。
- `user.manage` は `super_admin`, `admin` に付与する。
- 一般ユーザ停止時は一般ユーザ用セッションを失効する。
- 一般ユーザは書籍を保持しないため、一般ユーザ停止や退会に伴う書籍削除は対象外とする。

### 書籍管理

- 書籍アップロードは管理ユーザのみ実行できる。
- 書籍メタ情報編集と書籍削除は `super_admin`, `admin` に限定する。
- `operator` はアップロードと変換復旧を担当するが、メタ情報編集と削除は行わない。
- 一般ユーザは有効かつ閲覧可能な書籍だけを参照、閲覧できる。
- 管理ユーザの `book.view` は、管理対象書籍の内容確認・品質確認に限定する。一般ユーザ所有書籍の閲覧権限ではない。

### 変換ジョブと検索インデックス

- 変換ジョブ状態参照は管理ユーザの参照権限を持つロールに許可する。
- 変換ジョブ再実行は `super_admin`, `admin`, `operator` に許可する。
- Elasticsearch再インデックスは `super_admin`, `operator` に許可する。
- ElasticsearchはPostgreSQLから再構築可能な派生データとして扱い、権限判断の正本にしない。

## API権限確認方針

| API領域 | 確認内容 |
| --- | --- |
| 管理認証API | 管理ユーザとして有効であり、停止または削除されていないこと。 |
| 管理ユーザAPI | 管理ユーザとして認証済みであり、`admin_user.read` または `admin_user.manage` を持つこと。 |
| ロールAPI | 管理ユーザとして認証済みであり、`role.read` または `role.manage` を持つこと。 |
| 一般ユーザ管理API | 管理ユーザとして認証済みであり、`user.read` または `user.manage` を持つこと。 |
| 書籍管理API | 操作に応じて `book.read`, `book.upload`, `book.update`, `book.delete` を持つこと。 |
| 書籍閲覧API | 一般ユーザは対象書籍が有効かつ閲覧可能状態であること。管理ユーザは `book.view` を持ち、管理対象書籍の内容確認・品質確認として閲覧すること。 |
| 変換ジョブAPI | 操作に応じて `conversion_job.read` または `conversion_job.retry` を持つこと。 |
| 検索 / 再インデックスAPI | 一般検索は閲覧可能書籍に限定し、再インデックスは `search.reindex` を持つこと。 |

## 更新方針

ロール、権限コード、操作可否、API権限確認単位が変わった場合は、このドキュメント、[doc/04_design/08_authorization_design/01_authorization_design.md](01_authorization_design.md)、[doc/04_design/03_api_contracts/07_admin_api.md](../03_api_contracts/07_admin_api.md)、権限機能の受入テストを合わせて更新する。
