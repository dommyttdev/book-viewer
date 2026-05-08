# 検索API契約初版

## 目的

このドキュメントは、検索API契約初版を定義する。

対象は、キーワード検索、タグ検索、著者検索、シリーズ検索、サジェストである。

## 前提

- PostgreSQLを正本、Elasticsearchを検索用派生データとする。
- 一般ユーザ向け検索では、閲覧可能な書籍のみ返す。
- 管理向け検索では、権限に応じて表示状態や変換状態で絞り込める。
- クライアントからElasticsearch Query DSLを直接受け取らない。

## リソース概要

| リソース | URL |
| --- | --- |
| 一般向け検索 | `/api/v1/search/books` |
| 管理向け検索 | `/api/v1/admin/search/books` |
| サジェスト | `/api/v1/search/suggest` |

## キーワード検索

```http
GET /api/v1/search/books
```

| クエリ | 内容 |
| --- | --- |
| `q` | キーワード |
| `tagIds` | タグIDのカンマ区切り |
| `authorId` | 著者ID |
| `seriesId` | シリーズID |
| `bookTypeCode` | 種別コード |
| `page`, `size`, `sort` | ページング、ソート |

```json
{
  "items": [
    {
      "id": "book_...",
      "title": "サンプル本",
      "authors": [],
      "series": null,
      "bookType": null,
      "tags": [],
      "thumbnailUrl": "/api/v1/books/book_.../thumbnail",
      "score": 12.3
    }
  ],
  "page": { "number": 1, "size": 20, "totalItems": 1, "totalPages": 1 }
}
```

キーワードが空の場合は、絞り込み付き一覧として扱う。過度に長いキーワード、過大なページサイズ、未定義ソートは拒否する。

## タグ検索

タグ検索は、検索APIの`tagIds`で行う。複数タグ指定時はAND条件を初期方針とする。

タグ名の補完やタグ候補一覧は、後続のマスタAPIまたはサジェストAPIで扱う。

## 著者検索

著者IDによる完全一致は`authorId`で行う。著者名の文字列検索は`q`による横断検索に含める。

## シリーズ検索

シリーズIDによる完全一致は`seriesId`で行う。シリーズ名、シリーズ読み、シリーズ概要は`q`による横断検索に含める。

## 管理向け検索

```http
GET /api/v1/admin/search/books
```

一般向け条件に加えて、次の条件を扱う。

| クエリ | 内容 |
| --- | --- |
| `visibilityStatus` | 書籍表示状態 |
| `conversionStatus` | 最新変換ジョブ状態 |
| `includeDeleted` | 論理削除済みを含めるか。初版では`false`既定 |

管理向け検索は管理ユーザの権限に応じて許可する。

## サジェスト

```http
GET /api/v1/search/suggest
```

初版では提供候補として定義し、MVPで必要になった場合に実装する。

| クエリ | 内容 |
| --- | --- |
| `q` | 入力途中の文字列 |
| `type` | `title`, `author`, `tag`, `series` |
| `size` | 件数 |

```json
{
  "items": [
    { "type": "title", "value": "サンプル本", "bookId": "book_..." }
  ]
}
```

## ソート

| 値 | 内容 |
| --- | --- |
| `relevance.desc` | 関連度順 |
| `updatedAt.desc` | 更新日時降順 |
| `createdAt.desc` | 登録日時降順 |
| `title.asc` | タイトル昇順 |
| `series.asc` | シリーズ名、シリーズ内順 |

キーワードありの既定は`relevance.desc`、キーワードなしの既定は`updatedAt.desc`とする。

## 主なエラー

| 状態 | エラー |
| --- | --- |
| 入力不正 | `400 Bad Request` |
| 権限不足 | `403 Forbidden` |
| Elasticsearch利用不可 | `503 Service Unavailable` |

## 後続で詳細化する事項

- Elasticsearchのindex name、mapping、analyzer、boost値。
- サジェストAPIをMVPに含めるかどうか。
- 検索ハイライトの有無とXSS対策。
- 深いページング時の`search_after`利用。
