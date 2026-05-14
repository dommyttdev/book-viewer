# プロダクトバックログ初期版

## 目的

このドキュメントは、スクラムで実装を進めるためのプロダクトバックログ初期版である。

詳細な仕様の正本は `doc/` 配下の設計書と受入条件に置き、このバックログではスプリント計画に使う粒度、優先順位、TDD観点を管理する。

## 優先順位の考え方

1. MVPの主導線である「登録、アップロード、変換、検索、閲覧」を優先する。
2. 技術的に不確実性が高い「アーカイブ展開、画像変換、検索インデックス、権限」を早めに検証する。
3. PostgreSQLを正本、Elasticsearchを派生データとする境界を崩さない。
4. UIは主要フローを通せる最小構成から始め、細かな体験改善はBeta以降へ回す。
5. 各PBIは、TDDで開始できる大きさまで分割する。

## MVPバックログ

| ID | PBI | 価値 | 主なTDD観点 | 設計参照 | 優先度 |
| --- | --- | --- | --- | --- | --- |
| PBI-001 | プロジェクト基盤を作る | フロントエンド、API、ワーカーを継続的に実装できる。 | 起動確認、ヘルスチェック、最小API応答、DBマイグレーション、ミドルウェア接続、メール送信境界 | [技術スタック](../../doc/03_architecture/02_technology_stack.md), [ローカル開発手順](../../doc/05_development/04_local_development.md), [ADR-0012](../../doc/03_architecture/03_adr/13_ADR-0012-use-single-linux-host-docker-compose.md) | 高 |
| PBI-002 | 一般ユーザ登録とメール確認を実装する | 一般ユーザがメール確認後に利用開始できる。 | 入力検証、メール重複、トークンハッシュ、有効期限、再送制限、開発用メール確認 | [アカウントAPI](../../doc/04_design/03_api_contracts/06_account_api.md), [データモデル](../../doc/04_design/04_data_model.md) | 高 |
| PBI-003 | ログイン、メール2段階認証、セッションを実装する | 一般ユーザと管理ユーザが安全にログインできる。 | 認証失敗、challengeId、code_hash、メール送信、セッション分離、セッション失効、レート制限 | [ADR-0008](../../doc/03_architecture/03_adr/09_ADR-0008-use-email-authentication-and-server-side-sessions.md), [アカウントAPI](../../doc/04_design/03_api_contracts/06_account_api.md), [管理API](../../doc/04_design/03_api_contracts/07_admin_api.md), [管理ユーザログイン](../../doc/02_backlog/02_user_stories/11_admin_login_logout.md) | 高 |
| PBI-004 | パスワードリセットを実装する | パスワードを忘れた利用者が復旧できる。 | 存在推測防止、トークンハッシュ、メール送信、有効期限、既存セッション失効 | [アカウントAPI](../../doc/04_design/03_api_contracts/06_account_api.md) | 高 |
| PBI-005 | 初期管理ユーザと固定ロール認可を実装する | 初期 `super_admin` を用意し、管理操作を `super_admin`, `admin`, `operator`, `viewer` の権限に限定できる。 | 初期管理ユーザ作成、未認証、一般ユーザ、管理ロール別アクセス、最後のsuper_admin保護 | [権限設計](../../doc/04_design/08_authorization_design/01_authorization_design.md), [権限マトリクス](../../doc/04_design/08_authorization_design/02_permission_matrix.md), [管理ユーザ管理](../../doc/02_backlog/02_user_stories/12_admin_user_management.md) | 高 |
| PBI-006 | 書籍メタ情報のドメインモデルを作る | 本を検索、閲覧、整理する基盤を作る。 | タイトル必須、著者、タグ、シリーズ、公開状態、論理削除 | [データモデル](../../doc/04_design/04_data_model.md), [書籍API](../../doc/04_design/03_api_contracts/02_book_api.md) | 高 |
| PBI-007 | RabbitMQ基盤と変換ジョブ配送を実装する | APIと重い変換処理を疎結合にする。 | queue設定、ack、再配送、DLQ、冪等性、PostgreSQL状態との分離 | [ADR-0007](../../doc/03_architecture/03_adr/08_ADR-0007-use-async-conversion-worker.md), [データモデル](../../doc/04_design/04_data_model.md) | 高 |
| PBI-008 | 管理ユーザがアーカイブをアップロードできる | 自炊本登録の入口を作る。 | 拡張子、サイズ、権限、保存失敗、ジョブ作成 | [アップロード受入条件](../../doc/02_backlog/03_acceptance_criteria/01_book_upload.md), [書籍API](../../doc/04_design/03_api_contracts/02_book_api.md) | 高 |
| PBI-009 | 原本ファイル保存を実装する | 再変換できるよう原本を保持する。 | パス検証、重複、保存場所、失敗時ロールバック | [ファイル保存設計](../../doc/04_design/06_file_storage_design.md) | 高 |
| PBI-010 | zipのみでアップロードからWebP変換までの縦切りを通す | 最小の変換処理を早期に検証できる。 | 作業ディレクトリ、パストラバーサル、破損zip、1ページWebP生成 | [画像変換設計](../../doc/04_design/07_image_conversion_design.md), [画像変換受入テスト](../../doc/06_testing/02_acceptance_tests/02_image_conversion_acceptance_tests.md) | 高 |
| PBI-011 | 7-Zip外部プロセスでrar / 7zipを展開できる | 対応アーカイブ形式を満たす。 | コマンド引数、終了コード、タイムアウト、ログ | [ADR-0006](../../doc/03_architecture/03_adr/07_ADR-0006-extract-archive-files.md), [画像変換設計](../../doc/04_design/07_image_conversion_design.md) | 高 |
| PBI-012 | サムネイル生成と変換ジョブ状態確認を実装する | 管理ユーザが成功、失敗、処理中を追跡できる。 | 品質値80、サムネイル、状態遷移、失敗理由、権限制御 | [変換ジョブAPI](../../doc/04_design/03_api_contracts/03_conversion_job_api.md), [データモデル](../../doc/04_design/04_data_model.md) | 高 |
| PBI-013 | 書籍メタ情報を編集できる | 検索と閲覧に必要な情報を整えられる。 | 必須項目、タグ重複、シリーズ順序、認可 | [書籍API](../../doc/04_design/03_api_contracts/02_book_api.md), [データモデル](../../doc/04_design/04_data_model.md) | 高 |
| PBI-014 | Elasticsearchインデックスと検索Outboxを作成する | 検索用派生データを安全に構築できる。 | PostgreSQL正本、Outbox、再構築、書籍単位更新、再試行通知 | [検索設計](../../doc/04_design/05_search_design/01_search_design.md), [検索インデックス設計](../../doc/04_design/05_search_design/02_search_index_design.md) | 高 |
| PBI-015 | タイトル、著者、タグ、シリーズで検索できる | 一般ユーザが読みたい本を探せる。 | 日本語検索、空検索、ページング、閲覧可能状態 | [検索API](../../doc/04_design/03_api_contracts/04_search_api.md), [検索受入テスト](../../doc/06_testing/02_acceptance_tests/03_search_acceptance_tests.md) | 高 |
| PBI-016 | 本一覧と詳細を表示できる | 検索以外から本を見つけられる。 | 閲覧可能状態、ページング、サムネイル有無 | [画面メモ](../../doc/04_design/02_screen_notes.md), [ビューアAPI](../../doc/04_design/03_api_contracts/05_viewer_api.md) | 高 |
| PBI-017 | ビューアでページ表示とページ送りができる | 一般ユーザが本を読める。 | ページ範囲、存在しない本、未変換本、認可 | [ビューアAPI](../../doc/04_design/03_api_contracts/05_viewer_api.md), [閲覧受入テスト](../../doc/06_testing/02_acceptance_tests/04_viewer_acceptance_tests.md) | 高 |
| PBI-018 | 読みかけ位置を保存できる | 再閲覧しやすくなる。 | 最終ページ、範囲外ページ、ユーザ別保存 | [ビューアAPI](../../doc/04_design/03_api_contracts/05_viewer_api.md) | 中 |
| PBI-019 | お気に入り登録、解除、一覧表示ができる | よく読む本へ戻りやすくなる。 | 重複防止、解除、ユーザ別、未認証 | [お気に入り受入条件](../../doc/02_backlog/03_acceptance_criteria/02_favorite.md) | 中 |
| PBI-020 | 本番相当Docker Compose統合を確認する | 主要コンポーネントをまとめて検証できる。 | API、Worker、Frontend、PostgreSQL、Elasticsearch、RabbitMQ、永続ボリューム、設定差し替え | [ADR-0012](../../doc/03_architecture/03_adr/13_ADR-0012-use-single-linux-host-docker-compose.md), [ローカル開発手順](../../doc/05_development/04_local_development.md), [Runbook](../../doc/07_operations/01_runbook.md) | 中 |
| PBI-021 | MVP主要フローのE2Eを作る | 回帰を検知できる。 | ログイン、アップロード、検索、閲覧、権限不足 | [テスト戦略](../../doc/06_testing/01_test_strategy.md), [回帰テスト一覧](../../doc/06_testing/03_regression_tests.md) | 中 |

## Beta / v1.0候補バックログ

| ID | PBI | 価値 | 主なTDD観点 | 優先度 |
| --- | --- | --- | --- | --- |
| PBI-101 | 変換ジョブの再実行を実装する | 失敗時の復旧ができる。 | 再実行可能状態、生成物扱い、権限制御 | 中 |
| PBI-102 | 変換ジョブのキャンセルを実装する | 長時間処理を止められる。 | canceled状態、外部プロセス停止、競合 | 中 |
| PBI-103 | 検索の絞り込みと並び替えを実装する | 本を探しやすくする。 | 複合条件、ソート、空結果、ページング | 中 |
| PBI-104 | ビューア操作を強化する | 読書体験を改善する。 | 見開き、ページ番号指定、拡大縮小 | 中 |
| PBI-105 | ユーザプロフィール編集を実装する | アカウント管理を実用化する。 | 入力検証、重複、認可 | 低 |
| PBI-106 | 管理ユーザ管理とロール管理を強化する | 管理運用を安全にする。 | ロール別権限、自己削除防止、監査観点 | 中 |
| PBI-107 | 監視と障害記録を整える | 運用時の調査性を上げる。 | ログ、メトリクス、障害テンプレート | 低 |

## バックログリファインメント観点

各PBIをスプリントへ入れる前に、次を確認する。

- 対応する `doc/` の設計書と受入条件は存在するか。
- 先に書くテストは単体、結合、E2Eのどれか。
- 外部依存をモックにするか、テストコンテナまたはDocker Composeで確認するか。
- 権限、入力検証、異常系、ログ、ドキュメント更新のタスクが含まれているか。
- DBマイグレーション、初期データ、設定値、環境変数の追加が必要か。
- メール送信、ファイル保存、外部プロセス、検索、キューなどの外部境界を開発環境でどう代替または確認するか。
- 初期管理ユーザ、ロール、権限、検索インデックス、キュー名などの運用初期化が必要か。
- 利用者が操作できる最小UIまで含めるか、APIのみのスパイクとして扱うか。
- 1スプリントで完了できない場合、縦に薄く動く単位へ分割できるか。
