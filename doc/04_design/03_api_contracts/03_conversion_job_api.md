# 変換ジョブAPI契約初版

## 目的

このドキュメントは、変換ジョブに関するAPI契約初版を定義する。

対象は、ジョブ状態取得、ジョブ再実行、ジョブキャンセルである。

## 前提

- 変換ジョブの業務状態はPostgreSQLを正本とする。
- 変換処理はSpring Boot変換ワーカーがRabbitMQから取得して非同期に実行する。
- 一般ユーザは変換ジョブを直接操作しない。
- 失敗理由には秘密情報、内部物理パス、不要な個人情報を含めない。

## リソース概要

| リソース | URL |
| --- | --- |
| 管理向け変換ジョブ | `/api/v1/admin/conversion-jobs` |

## ジョブ一覧取得

```http
GET /api/v1/admin/conversion-jobs
```

| クエリ | 内容 |
| --- | --- |
| `bookId` | 書籍IDで絞り込み |
| `status` | `queued`, `extracting`, `converting`, `completed`, `failed`, `canceled` |
| `page`, `size`, `sort` | ページング、ソート |

```json
{
  "items": [
    {
      "id": "job_...",
      "bookId": "book_...",
      "status": "failed",
      "attemptNumber": 1,
      "queuedAt": "2026-05-08T00:00:00Z",
      "startedAt": "2026-05-08T00:01:00Z",
      "finishedAt": "2026-05-08T00:02:00Z",
      "failurePhase": "extracting",
      "failureCode": "encrypted_archive",
      "timedOut": false
    }
  ],
  "page": { "number": 1, "size": 20, "totalItems": 1, "totalPages": 1 }
}
```

## ジョブ状態取得

```http
GET /api/v1/admin/conversion-jobs/{conversionJobId}
```

```json
{
  "id": "job_...",
  "bookId": "book_...",
  "sourceFileId": "file_...",
  "status": "failed",
  "attemptNumber": 1,
  "retryOfConversionJobId": null,
  "workerId": "worker-1",
  "failurePhase": "extracting",
  "failureCode": "encrypted_archive",
  "failureMessage": "暗号化アーカイブは初版では非対応です。",
  "externalExitCode": 2,
  "timedOut": false,
  "queuedAt": "2026-05-08T00:00:00Z",
  "startedAt": "2026-05-08T00:01:00Z",
  "finishedAt": "2026-05-08T00:02:00Z"
}
```

## ジョブ再実行

```http
POST /api/v1/admin/conversion-jobs/{conversionJobId}/retry
```

失敗、キャンセル、完了済みのジョブを元に、新しい`conversion_job`を作成する。既存ジョブを直接`queued`へ戻さない。

```json
{
  "conversionJobId": "job_new_...",
  "retryOfConversionJobId": "job_...",
  "status": "queued"
}
```

再実行時は、既存の閲覧可能なWebPとサムネイルを壊さないことを優先する。新しい生成物は一時出力先に作成し、PostgreSQL更新が成功してから差し替える。

APIは新しい`conversion_job`をPostgreSQLへ作成した後、RabbitMQへ`conversionJobId`を含む変換要求メッセージを投入する。RabbitMQ投入に失敗した場合は、PostgreSQL上のジョブ状態を基準に再投入または失敗状態への遷移を判断する。

## ジョブキャンセル

```http
POST /api/v1/admin/conversion-jobs/{conversionJobId}/cancel
```

初版では`queued`状態のキャンセルを主対象とする。`extracting`または`converting`中のキャンセルは、変換ワーカー側の安全な中断方式が実装できる範囲で扱う。

成功時はキャンセル後の状態を返す。

```json
{
  "conversionJobId": "job_...",
  "status": "canceled"
}
```

## 権限

| 操作 | 必要権限 |
| --- | --- |
| 一覧 / 詳細 | `conversion_job.read` |
| 再実行 | `conversion_job.retry` |
| キャンセル | `conversion_job.cancel` |

## 主なエラー

| 状態 | エラー |
| --- | --- |
| 対象ジョブなし | `404 Not Found` |
| 状態遷移不可 | `409 Conflict` |
| 権限不足 | `403 Forbidden` |
| キュー投入失敗 | `503 Service Unavailable` |

## 後続で詳細化する事項

- 中断中ジョブのキャンセル方式。
- 失敗コード一覧。
- RabbitMQのexchange、queue、routing key、retry、dead letter exchangeの具体値。
- 運用者向け再インデックスや残存作業ディレクトリ確認との連携。
