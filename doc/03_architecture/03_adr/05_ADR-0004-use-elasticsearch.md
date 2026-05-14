# ADR-0004: Elasticsearchを検索用派生インデックスとして採用する

## Status

Accepted

## Context

本システムでは、タイトル、著者、タグ、シリーズを中心に、日本語の検索、表記揺れ対策、補完、部分一致、将来的なスコアリング調整を行う必要がある。

PostgreSQLは正本データ管理に適しているが、日本語のあいまい検索や検索品質の調整を担うには専用の検索基盤が必要になる。

検索インデックスは業務上の正本ではなく、PostgreSQLと保存済みメタ情報から再構築できる派生データとして扱う。

## Decision

検索基盤としてElasticsearchを採用する。

日本語検索にはanalysis-kuromojiを使用する。タイトル、著者、タグ、シリーズなどの検索対象項目にはkuromojiベースのカスタムアナライザを使用する。

全角 / 半角、大小文字、互換文字などの表記揺れ対策としてanalysis-icuのICU normalizerを利用する。補完や部分一致が必要な項目には、必要に応じてedge n-gram系フィールドを追加する。

Docker Compose、ローカル開発環境、本番運用環境では、Elasticsearchにanalysis-kuromojiとanalysis-icuの両方を導入する。バックエンドAPIの起動時またはインデックス作成前に必須プラグインの存在を確認し、未導入の場合はインデックス作成を失敗させる。

ElasticsearchはPostgreSQLから再構築可能な派生インデックスとして扱い、正本データを持たせない。

PostgreSQL更新後の検索インデックス更新は「同期更新 + 失敗時Outbox」を採用する。APIまたは変換ワーカーは、検索対象の正本更新と同じユースケース内でPostgreSQLに`search_index_outbox`を記録し、コミット後にElasticsearch更新を即時試行する。成功時はOutboxを完了状態にし、失敗時は再試行待ちとして記録する。

再試行通知の配送にはRabbitMQを使う。ただし、RabbitMQは検索更新状態の正本ではなく、Outbox IDを再試行ワーカーへ通知、配送する手段として扱う。再試行ワーカーはPostgreSQLの最新正本を読み直してElasticsearchドキュメントを再生成し、重複配送時も冪等に処理する。

## Consequences

- 日本語のタイトル、著者、タグ、シリーズ検索を高速化しやすい。
- analysis-kuromojiにより、日本語の形態素解析を前提とした検索品質を設計できる。
- analysis-icuにより、全角 / 半角、大小文字、互換文字などの正規化をインデックス設計で一貫して扱える。
- 必須プラグインが増えるため、環境構築、Docker Compose、本番運用、起動時チェック、Runbookで導入と復旧手順を明確にする必要がある。
- インデックス設計、アナライザ、スコアリング、補完用フィールドを検索要件に合わせて調整できる。
- PostgreSQLを正本とするため、Elasticsearchの破損や不整合が起きても再インデックスで回復できる。
- PostgreSQLとElasticsearchの二重更新が発生するため、Outboxによる更新失敗記録、再試行ワーカー、全件再インデックス手順が必要になる。
- RabbitMQ障害時でも検索更新状態はPostgreSQLに残るため、再通知や手動再インデックスで回復しやすい。
- Outboxテーブルと再試行ワーカーの実装が必要になり、検索対象更新のユースケースではOutbox記録と状態遷移のテストが必要になる。
- 検索結果の権限や表示可否が重要な場合は、必要に応じてPostgreSQLで確認する。

## Alternatives

- PostgreSQLの全文検索を使用する。
  - 構成は単純になるが、日本語検索、補完、表記揺れ、スコアリング調整の柔軟性を優先し、今回は採用しない。
- OpenSearchを採用する。
  - Elasticsearchと近い用途で利用できるが、初期方針ではElasticsearch、analysis-kuromoji、analysis-icuを前提にする。
- 検索基盤を導入せずLIKE検索にする。
  - 初期実装は容易だが、日本語検索品質と性能が不足しやすいため採用しない。
