# ユーザーストーリー: 本削除

## 目的

管理ユーザが不要になった本を通常の一覧、検索、閲覧対象から外し、ファイル実体と検索インデックスの扱いを安全に管理できるようにする。

## 背景

書籍削除では、PostgreSQL、ファイル保存領域、Elasticsearchの整合性を保つ必要がある。原本ファイルは通常運用では保存し続ける方針であり、変換済みWebP、サムネイル、検索インデックスはPostgreSQLを正として後から整理できるようにする。

## 利用者

- 主利用者: 管理ユーザ
- 関係者: 一般ユーザ、運用者

## ストーリー

### BD-US-001: 管理ユーザとして本を削除する

管理ユーザとして、不要になった本を削除したい。なぜなら、一般ユーザの一覧、検索、閲覧対象から除外し、管理対象を整理したいから。

### BD-US-002: 一般ユーザとして削除済みの本が表示されない

一般ユーザとして、削除済みの本が一覧、検索、閲覧に表示されないようにしてほしい。なぜなら、読めない本や管理上除外された本に迷わず、閲覧できる本だけを扱いたいから。

### BD-US-003: 運用者として派生ファイルを後から整理する

運用者として、削除済み本に紐づくWebP、サムネイル、検索インデックスを後から安全に整理したい。なぜなら、論理削除と物理削除を同一トランザクションにせず、失敗時にもPostgreSQLを正として回復できるようにしたいから。

## 対象範囲

- 管理ユーザによる書籍の論理削除
- 一般向け一覧、検索、閲覧対象からの除外
- Elasticsearchドキュメント削除または再インデックスによる回復
- 原本、WebP、サムネイルの物理削除タイミング方針
- 一般ユーザと権限不足の管理ユーザの拒否

## 対象外

- 物理削除ジョブまたは管理コマンドの詳細API
- 削除済み本の復元機能
- バックアップからの復旧

## 関連ドキュメント

- [doc/02_backlog/03_acceptance_criteria/03_book_delete.md](../03_acceptance_criteria/03_book_delete.md)
- [doc/02_backlog/01_epics/01_book_management.md](../01_epics/01_book_management.md)
- [doc/04_design/03_api_contracts/02_book_api.md](../../04_design/03_api_contracts/02_book_api.md)
- [doc/04_design/04_data_model.md](../../04_design/04_data_model.md)
- [doc/04_design/06_file_storage_design.md](../../04_design/06_file_storage_design.md)
- [doc/04_design/05_search_design/01_search_design.md](../../04_design/05_search_design/01_search_design.md)

