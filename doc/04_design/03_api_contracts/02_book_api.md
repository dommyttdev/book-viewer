# 自炊本管理API契約初版

## 目的

このドキュメントは、自炊本管理に関するAPI契約初版を定義する。

対象は、本一覧取得、本詳細取得、本アップロード、本メタ情報更新、本削除である。

## 前提

- 書籍アップロード、メタ情報更新、削除は管理ユーザのみが実行できる。
- 一般ユーザは閲覧可能な書籍の一覧と詳細のみ取得できる。
- アップロード後、バックエンドAPIは原本保存、PostgreSQL登録、変換ジョブ作成、RabbitMQ投入までを行い、変換完了を待たない。
- 物理パス、コンテナパス、`storage_key`はレスポンスへ返さない。

## リソース概要

| リソース | URL |
| --- | --- |
| 一般向け書籍 | `/api/v1/books` |
| 管理向け書籍 | `/api/v1/admin/books` |

## 本一覧取得

```http
GET /api/v1/books
```

一般ユーザ向けに、閲覧可能な書籍のみ返す。

| クエリ | 内容 |
| --- | --- |
| `page` | 1始まりのページ番号 |
| `size` | 1ページ件数 |
| `sort` | `updatedAt.desc`, `createdAt.desc`, `title.asc`など |

```json
{
  "items": [
    {
      "id": "book_...",
      "title": "サンプル本",
      "authors": [{ "id": "author_...", "name": "著者名" }],
      "series": { "id": "series_...", "name": "シリーズ名", "order": 1 },
      "bookType": { "code": "comic", "name": "コミック" },
      "tags": [{ "id": "tag_...", "name": "タグ" }],
      "thumbnailUrl": "/api/v1/books/book_.../thumbnail",
      "pageCount": 120,
      "updatedAt": "2026-05-08T00:00:00Z"
    }
  ],
  "page": { "number": 1, "size": 20, "totalItems": 1, "totalPages": 1 }
}
```

管理向け一覧は状態で絞り込める。

```http
GET /api/v1/admin/books
```

| クエリ | 内容 |
| --- | --- |
| `visibilityStatus` | `draft`, `converting`, `available`, `failed`, `hidden`, `deleted` |
| `conversionStatus` | 最新変換ジョブ状態 |
| `page`, `size`, `sort` | ページング、ソート |

## 本詳細取得

```http
GET /api/v1/books/{bookId}
GET /api/v1/admin/books/{bookId}
```

一般向けは閲覧可能な書籍のみ返す。管理向けは管理権限に応じて非表示、変換失敗、論理削除済みの情報も参照できる。

```json
{
  "id": "book_...",
  "title": "サンプル本",
  "description": "説明",
  "authors": [],
  "series": null,
  "bookType": null,
  "tags": [],
  "visibilityStatus": "available",
  "thumbnailUrl": "/api/v1/books/book_.../thumbnail",
  "pageCount": 120,
  "createdAt": "2026-05-08T00:00:00Z",
  "updatedAt": "2026-05-08T00:00:00Z"
}
```

## 本アップロード

```http
POST /api/v1/admin/books
Content-Type: multipart/form-data
```

管理ユーザのみ実行できる。zip / rar / 7zip形式の原本アーカイブを受け付ける。

| フィールド | 必須 | 内容 |
| --- | --- | --- |
| `file` | 必須 | 原本アーカイブ |
| `title` | 必須 | 書籍タイトル |
| `description` | 任意 | 説明 |
| `authorIds` | 任意 | 既存著者ID |
| `tagIds` | 任意 | 既存タグID |
| `seriesId` | 任意 | シリーズID |
| `seriesOrder` | 任意 | シリーズ内順序 |
| `bookTypeCode` | 任意 | 種別コード |

成功時は`201 Created`を返す。

```json
{
  "bookId": "book_...",
  "conversionJobId": "job_...",
  "visibilityStatus": "converting",
  "conversionStatus": "queued"
}
```

主なエラーは、非対応形式、入力不正、権限不足、原本保存失敗、ジョブ作成失敗である。

## 本メタ情報更新

```http
PATCH /api/v1/admin/books/{bookId}
```

管理ユーザのみ実行できる。

```json
{
  "title": "更新後タイトル",
  "description": "更新後説明",
  "authorIds": ["author_..."],
  "tagIds": ["tag_..."],
  "seriesId": "series_...",
  "seriesOrder": 2,
  "bookTypeCode": "comic",
  "visibilityStatus": "available"
}
```

更新後、検索対象項目が変わる場合はElasticsearch更新を行う。Elasticsearch更新に失敗してもPostgreSQLを正として再試行できるようにする。

## 本削除

```http
DELETE /api/v1/admin/books/{bookId}
```

管理ユーザのみ実行できる。初版ではPostgreSQL上の論理削除を行い、通常の一覧、検索、閲覧対象から除外する。

物理ファイル削除は同一トランザクションでは行わず、削除ジョブまたは管理コマンドで扱う。ElasticsearchはPostgreSQLを正として削除または再インデックスする。

成功時は`204 No Content`を返す。

## 権限

| 操作 | 一般ユーザ | 管理ユーザ |
| --- | --- | --- |
| 一般向け一覧 / 詳細 | 可 | 可 |
| 管理向け一覧 / 詳細 | 不可 | 権限に応じて可 |
| アップロード | 不可 | `book.upload`権限 |
| メタ情報更新 | 不可 | `book.update`権限 |
| 削除 | 不可 | `book.delete`権限 |

## 後続で詳細化する事項

- 著者、タグ、シリーズ、種別のマスタ管理API。
- 公開IDの形式。
- アップロードサイズが環境上の制限に当たった場合の扱い。
- 書籍削除後の物理削除ジョブAPIまたは管理コマンド。
