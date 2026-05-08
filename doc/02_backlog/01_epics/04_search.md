# エピック: 検索

## 目的

一般ユーザがタイトル、著者、タグ、シリーズなどから読みたい本を見つけられるようにし、PostgreSQLを正本、Elasticsearchを再構築可能な検索用派生データとして運用できるようにする。

このエピックでは、検索ユーザーストーリー、検索対象、Elasticsearchインデックス設計、検索結果表示、再インデックス方針を扱う。

## 対象利用者

- 一般ユーザ
- 管理ユーザ
- 運用者

一般ユーザは本を探すために検索を利用する。管理ユーザと運用者は検索インデックスの更新状態や再インデックス手順を扱う。

## 範囲

### MVP

- タイトル、著者、タグ、シリーズを対象に基本検索できる。
- 検索結果に表紙サムネイル、タイトル、著者、タグ、シリーズを表示する。
- 検索結果をページングできる。
- Elasticsearchで analysis-kuromoji を使った日本語検索を行う。
- PostgreSQLの書籍、著者、タグ、シリーズ、種別を正本として検索ドキュメントを作る。
- ElasticsearchインデックスをPostgreSQLから全件再構築できる方針を持つ。
- 書籍単位で再インデックスできる方針を持つ。

### Beta / v1.0

- 種別や複数条件で絞り込みできる。
- 検索結果を並び替えできる。
- ICU normalizerやedge n-gram系フィールドの利用範囲を具体化する。
- boost設定と検索対象フィールドの重みを調整できる。
- 検索インデックス更新失敗時の再試行方式を具体化する。

### 対象外

- 本文全文検索。
- OCR結果の検索。
- 高度な検索ランキング調整。
- 外部検索サービスへの切り替え。

## 主な成果物

- `doc/02_backlog/02_user_stories/06_book_search.md`
- `doc/04_design/05_search_design/02_search_index_design.md`
- `doc/04_design/05_search_design/01_search_design.md` への必要な反映
- `doc/04_design/03_api_contracts/04_search_api.md`
- `doc/06_testing/02_acceptance_tests/03_search_acceptance_tests.md`
- `doc/07_operations/01_runbook.md` への再インデックス手順の反映

## 完了の目安

- 一般ユーザが主要メタ情報から本を検索できる。
- Elasticsearchのindex name、mapping、analyzer、normalizer、検索対象、ソート対象、更新タイミングが設計されている。
- PostgreSQLを正として、全件および書籍単位で再インデックスできる手順方針が記録されている。
- 検索結果に閲覧権限と書籍公開状態の制約が反映されている。

## 関連ドキュメント

- `doc/04_design/05_search_design/01_search_design.md`
- `doc/04_design/04_data_model.md`
- `doc/03_architecture/03_adr/05_ADR-0004-use-elasticsearch.md`
- `doc/03_architecture/06_data_flow.md`

