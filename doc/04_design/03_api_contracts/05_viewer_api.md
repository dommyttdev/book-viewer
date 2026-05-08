# 閲覧API契約初版

## 目的

このドキュメントは、本の閲覧に関するAPI契約初版を定義する。

対象は、ページ一覧取得、ページ画像取得、閲覧位置保存である。

## 前提

- 一般ユーザは、閲覧可能な書籍のみ閲覧できる。
- 管理ユーザも権限に応じて書籍参照や動作確認のために閲覧できる。
- 変換済みで閲覧可能なページのみ画像配信対象にする。
- 画像ファイルの物理パスや`storage_key`は返さない。

## ページ一覧取得

```http
GET /api/v1/books/{bookId}/pages
```

```json
{
  "bookId": "book_...",
  "pageCount": 120,
  "pages": [
    {
      "id": "page_...",
      "pageNumber": 1,
      "width": 1200,
      "height": 1800,
      "imageUrl": "/api/v1/books/book_.../pages/1/image"
    }
  ]
}
```

対象書籍が非表示、変換未完了、論理削除済みの場合、一般ユーザには閲覧不可として扱う。

## ページ画像取得

```http
GET /api/v1/books/{bookId}/pages/{pageNumber}/image
```

変換済みWebP画像を返す。レスポンスContent-Typeは`image/webp`とする。

| 項目 | 方針 |
| --- | --- |
| 権限確認 | 書籍IDとページ番号から正本を確認してから配信する。 |
| キャッシュ | 実装時に`Cache-Control`、`ETag`を検討する。 |
| 範囲取得 | 初版では必須にしない。 |
| 404 | 書籍なし、ページなし、閲覧不可の存在秘匿に使用できる。 |

## サムネイル取得

```http
GET /api/v1/books/{bookId}/thumbnail
```

表紙サムネイルを返す。レスポンスContent-Typeは`image/webp`とする。

## 閲覧位置取得

```http
GET /api/v1/books/{bookId}/reading-position
```

一般ユーザ自身の読みかけ位置を返す。

```json
{
  "bookId": "book_...",
  "lastPageNumber": 42,
  "lastReadAt": "2026-05-08T00:00:00Z"
}
```

閲覧履歴がない場合は`404 Not Found`または`lastPageNumber: null`のどちらにするかを実装時に決める。初版ではUIの扱いやすさを優先し、`200 OK`で`null`を返す方式を候補とする。

## 閲覧位置保存

```http
PUT /api/v1/books/{bookId}/reading-position
```

一般ユーザ自身の読みかけ位置を保存する。

```json
{
  "pageNumber": 42
}
```

成功時:

```json
{
  "bookId": "book_...",
  "lastPageNumber": 42,
  "lastReadAt": "2026-05-08T00:00:00Z"
}
```

ページ番号は対象書籍のページ範囲内で検証する。

## お気に入り

閲覧体験に関係するため、初版ではViewer API配下にお気に入り操作を含める。

```http
GET /api/v1/me/favorites
POST /api/v1/books/{bookId}/favorite
DELETE /api/v1/books/{bookId}/favorite
```

お気に入りは一般ユーザ自身のデータのみ操作できる。同一ユーザ、同一書籍の有効なお気に入りは一件までとする。

## 権限

| 操作 | 一般ユーザ | 管理ユーザ |
| --- | --- | --- |
| ページ一覧 / 画像 / サムネイル | 閲覧可能書籍のみ可 | 権限に応じて可 |
| 閲覧位置保存 | 自分のみ可 | 不可 |
| お気に入り | 自分のみ可 | 不可 |

## 後続で詳細化する事項

- 見開き表示、ページ番号指定、拡大縮小、キーボード操作に必要な追加APIの有無。
- 画像配信のキャッシュ方針。
- 管理ユーザの閲覧確認を一般向けAPIと共通化するか、管理向けAPIに分けるか。
