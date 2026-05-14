# アジャイル開発ドキュメント整備 ToDo

## 前提

- 対象システム: 自炊本閲覧Webアプリケーション
- 開発方式: アジャイル開発
- アーキテクチャ方針: パフォーマンスを意識したモジュラーモノリス構成
- ドキュメント方針: 開発前にすべてを固定するのではなく、プロダクト、バックログ、設計判断、運用知識を継続的に更新する
- 想定規模: 小から中規模のWebアプリケーション
- 主な構成要素:
  - Next.jsフロントエンドWebアプリケーション
  - Spring Boot 4.0.6バックエンドAPI
  - Spring Boot 4.0.6変換ワーカー
  - PostgreSQL: メタ情報、ユーザ、権限、ジョブ状態の正本
  - Elasticsearch: タイトル、著者、タグ、シリーズの検索用インデックス
  - 書籍ファイル保存領域: 原本ファイル、変換済みwebp、サムネイルを保存
  - RabbitMQによるzip / rar / 7zip 展開処理のジョブ配送
  - 非同期ジョブによるwebp変換処理
  - 一般ユーザ向け機能
  - 管理ユーザ向け機能

## ToDo管理ルール

- `[ ]`: 未着手
- `[~]`: 作業中
- `[x]`: 完了
- 各ToDoは、完了時に関連ドキュメントへ反映する
- 仕様判断が必要なものはADRまたは設計ドキュメントに理由を残す
- 実装後に仕様が変わった場合は、該当するユーザーストーリー、受入条件、設計メモ、Runbookを更新する

## Sprint 0: ドキュメント基盤整備

### ディレクトリ構成

- [x] [doc/01_product/](01_product/) を作成する
- [x] [doc/02_backlog/01_epics/](02_backlog/01_epics/) を作成する
- [x] [doc/02_backlog/02_user_stories/](02_backlog/02_user_stories/) を作成する
- [x] [doc/02_backlog/03_acceptance_criteria/](02_backlog/03_acceptance_criteria/) を作成する
- [x] [doc/03_architecture/](03_architecture/) を作成する
- [x] [doc/03_architecture/03_adr/](03_architecture/03_adr/) を作成する
- [x] [doc/04_design/](04_design/) を作成する
- [x] [doc/04_design/03_api_contracts/](04_design/03_api_contracts/) を作成する
- [x] [doc/04_design/05_search_design/](04_design/05_search_design/) を作成する
- [x] [doc/04_design/08_authorization_design/](04_design/08_authorization_design/) を作成する
- [x] [doc/05_development/](05_development/) を作成する
- [x] [doc/06_testing/](06_testing/) を作成する
- [x] [doc/06_testing/02_acceptance_tests/](06_testing/02_acceptance_tests/) を作成する
- [x] [doc/07_operations/](07_operations/) を作成する

補足:

- 以下のディレクトリは、現時点では初期作成対象に含めない。必要になった段階で個別の作成ToDoとして追加する
  - `doc/01_product/03_personas/`
    - 一般ユーザ、管理ユーザという利用者種別は既存のプロダクトビジョンと用語集で扱っており、詳細なペルソナはUI設計や利用者理解が必要になった段階で作成する
  - `doc/02_backlog/04_story_templates/`
    - 現時点では個別のユーザーストーリー作成ToDoを優先し、ストーリー形式の標準化が必要になった段階でテンプレート化する
  - `doc/06_testing/04_exploratory_testing/`
    - 探索的テストはMVPの主要画面や機能が具体化してから計画する方が実効性が高いため、初期作成対象から外す
  - `doc/06_testing/05_test_data/`
    - テストデータはデータモデル、受入テスト、回帰テストの内容が固まってから作成する方が重複や手戻りを避けやすいため、初期作成対象から外す
  - `doc/90_decisions/`
    - 当面の設計判断はADRまたは各設計ドキュメントに記録し、横断的な決定ログが必要になった段階で整理する
    - ADRや設計ドキュメントと判断記録が二重管理にならないよう、初期段階では作成しない
  - `doc/99_archive/`
    - アーカイブ対象となる旧版文書や廃止文書が発生してから利用する置き場であり、初期ドキュメントとして作成する必要はない

### 初期ドキュメント

- [x] プロダクトビジョンを作成する
  - 作成先: [doc/01_product/01_product_vision.md](01_product/01_product_vision.md)
  - 記載内容:
    - このアプリで解決したい課題
    - 対象ユーザ
    - 主要価値
    - MVPの範囲
    - MVP外の範囲
    - 成功指標
- [x] 用語集を作成する
  - 作成先: [doc/01_product/05_glossary.md](01_product/05_glossary.md)
  - 記載候補:
    - 自炊本
    - 原本ファイル
    - 変換済み画像
    - webp
    - シリーズ
    - タグ
    - 著者
    - お気に入り
    - 一般ユーザ
    - 管理ユーザ
    - ロール
- [x] ユーザーストーリーマップを作成する
  - 作成先: [doc/01_product/04_user_story_map.md](01_product/04_user_story_map.md)
  - 軸:
    - 登録する
    - アップロードする
    - 変換する
    - 整理する
    - 探す
    - 読む
    - お気に入り管理する
    - ユーザ管理する
    - 運用する
- [x] ロードマップ初版を作成する
  - 作成先: [doc/01_product/02_product_roadmap.md](01_product/02_product_roadmap.md)
  - 区分:
    - MVP
    - Beta
    - v1.0
    - 将来対応
- [x] Definition of Doneを作成する
  - 作成先: [doc/05_development/05_definition_of_done.md](05_development/05_definition_of_done.md)
  - 含める条件:
    - 受入条件を満たす
    - 単体テストが通る
    - 主要な異常系が確認済み
    - 関連ドキュメントが更新済み
    - ログ、エラー処理、権限確認が実装済み
- [x] コーディングルールを作成する
  - 作成先: [doc/05_development/01_coding_rules.md](05_development/01_coding_rules.md)
  - 記載内容:
    - [rules/CODING_STANDARDS.md](../rules/CODING_STANDARDS.md) との関係
    - 命名、責務分離、例外処理、ログ、テストの基本方針
    - APIモデル、永続化エンティティ、ドメインモデルの分離方針
    - セキュリティ上の注意点
- [x] ブランチ戦略を作成する
  - 作成先: [doc/05_development/02_branch_strategy.md](05_development/02_branch_strategy.md)
  - 記載内容:
    - [rules/CONTRIBUTING.md](../rules/CONTRIBUTING.md) との関係
    - ブランチ命名規則
    - 作業単位とコミット粒度
    - プルリクエスト運用
- [x] 環境構築手順を作成する
  - 作成先: [doc/05_development/03_environment_setup.md](05_development/03_environment_setup.md)
  - 記載内容:
    - 必要なツールとバージョン
    - Java 25、Node.js、Docker、PostgreSQL、Elasticsearchの準備
    - 7-Zip for Linux コンソール版の扱い
    - 環境変数とシークレット管理方針
- [x] ローカル開発手順を作成する
  - 作成先: [doc/05_development/04_local_development.md](05_development/04_local_development.md)
  - 記載内容:
    - Next.jsフロントエンドの起動方法
    - Spring BootバックエンドAPIの起動方法
    - Spring Boot変換ワーカーの起動方法
    - Docker Composeによるミドルウェア起動方法
    - テスト、ログ確認、よく使う開発コマンド

## Sprint 0: アーキテクチャと主要設計

### システム概要

- [x] システム概要を作成する
  - 作成先: [doc/03_architecture/01_system_overview.md](03_architecture/01_system_overview.md)
  - 記載内容:
    - システム目的
    - 主要機能
    - システム構成: 単一Linuxホスト上のDocker Compose構成
    - パフォーマンス上の基本方針
    - モジュラーモノリス構成の採用理由: 単一アプリ内モジュール構成、将来的にAPI / Workerを別アプリへ分離可能にする
    - 利用者種別
    - 外部依存コンポーネント
    - ファイル処理の大まかな流れ
    - 検索処理の大まかな流れ
- [x] 技術スタック初版を作成する
  - 作成先: [doc/03_architecture/02_technology_stack.md](03_architecture/02_technology_stack.md)
  - 記載内容:
    - フロントエンド技術: Next.js
    - バックエンド技術: Spring Boot 4.0.6
    - Javaバージョン: 25
    - PostgreSQL
    - Elasticsearch: 必須プラグインは技術スタックを正本とする
    - ファイル保存方式: 原本ファイル、変換済みwebp、サムネイルを保存
    - 画像変換方式: webp品質値80、サムネイル生成あり
    - アーカイブ展開方式: 7-Zip for Linux コンソール版を外部プロセスで呼び出す
    - 非同期ジョブ実行方式: RabbitMQ
    - ローカル開発環境 / 本番運用環境: Docker Compose
    - パフォーマンス上の選定理由
- [x] システムコンテキストを作成する
  - 作成先: [doc/03_architecture/04_system_context.md](03_architecture/04_system_context.md)
  - 記載内容:
    - 一般ユーザ
    - 管理ユーザ
    - Next.jsフロントエンドWebアプリケーション
    - Spring Boot 4.0.6バックエンドAPI
    - Spring Boot 4.0.6変換ワーカー
    - PostgreSQL
    - Elasticsearch
    - 書籍ファイル保存領域
    - RabbitMQによる非同期画像変換処理
    - 7-Zip for Linux コンソール版
- [x] コンテナ図を作成する
  - 作成先: [doc/03_architecture/05_container_diagram.md](03_architecture/05_container_diagram.md)
  - 記載内容:
    - Next.jsフロントエンドWebアプリケーション
    - Spring BootバックエンドAPI
    - Spring Boot変換ワーカー
    - PostgreSQL
    - Elasticsearch
    - 書籍ファイル保存領域
    - RabbitMQ
    - 7-Zip for Linux コンソール版
    - APIと変換ワーカーの責務分離
    - 単一Linuxホスト上のDocker Compose構成
- [x] データフローを作成する
  - 作成先: [doc/03_architecture/06_data_flow.md](03_architecture/06_data_flow.md)
  - 記載内容:
    - 書籍アップロードから原本保存までの流れ
    - 変換ジョブ投入、取得、実行、状態更新の流れ
    - アーカイブ展開からwebp変換、サムネイル生成までの流れ
    - PostgreSQL更新とElasticsearchインデックス更新の流れ
    - 閲覧時の画像配信と検索時のデータ参照の流れ
    - 失敗時の再試行、再構築、整合性回復の流れ
- [x] 品質特性初版を作成する
  - 作成先: [doc/03_architecture/07_quality_attributes.md](03_architecture/07_quality_attributes.md)
  - 記載内容:
    - レスポンス時間の目標
    - アップロード後の変換待ち時間の考え方
    - 同時利用ユーザ数の想定
    - 1冊あたりのページ数、ファイルサイズの想定: 業務上の保存容量上限なし、技術的安全上限あり
    - 変換ワーカーのスループット目標: 具体値は画像変換設計のリソース制限と設定を正本とする
    - ストレージ容量増加への対応方針: 1ユーザあたりの保存容量上限なし
    - 検索レスポンスの目標

### ADR

- [x] ADRテンプレートを作成する
  - 作成先: [doc/03_architecture/03_adr/01_ADR-template.md](03_architecture/03_adr/01_ADR-template.md)
  - 項目:
    - Status
    - Context
    - Decision
    - Consequences
    - Alternatives
- [x] Spring Boot採用ADRを作成する
  - 作成先: [doc/03_architecture/03_adr/02_ADR-0001-use-spring-boot.md](03_architecture/03_adr/02_ADR-0001-use-spring-boot.md)
  - 判断観点:
    - バックエンドAPI開発の生産性
    - 認証、バリデーション、DBアクセス、監視との統合
    - パフォーマンスチューニングのしやすさ
    - チームの習熟性
- [x] モジュラーモノリス採用ADRを作成する
  - 作成先: [doc/03_architecture/03_adr/03_ADR-0002-use-modular-monolith.md](03_architecture/03_adr/03_ADR-0002-use-modular-monolith.md)
  - 判断観点:
    - 小から中規模での開発速度
    - モジュール境界の明確化
    - 将来的なサービス分割余地
    - デプロイと運用の単純さ
- [x] PostgreSQL採用ADRを作成する
  - 作成先: [doc/03_architecture/03_adr/04_ADR-0003-use-postgresql.md](03_architecture/03_adr/04_ADR-0003-use-postgresql.md)
  - 判断観点:
    - リレーショナルなメタ情報管理
    - トランザクション
    - タグ、著者、シリーズ、ユーザ、ロールの関連管理
    - 変換ジョブ状態の一貫性
- [x] Elasticsearch採用ADRを作成する
  - 作成先: [doc/03_architecture/03_adr/05_ADR-0004-use-elasticsearch.md](03_architecture/03_adr/05_ADR-0004-use-elasticsearch.md)
  - 判断観点:
    - タイトル、著者、タグ、シリーズのあいまい検索
    - 日本語検索と表記揺れ正規化
    - 必須プラグインは技術スタックを正本とする
    - 必須プラグイン確認: 起動時またはインデックス作成前に確認
    - 補完 / 部分一致: 必要な項目にedge n-gram系フィールドを追加
    - 将来的なスコアリング調整
    - PostgreSQLから再構築可能な派生インデックスとして扱う方針
- [x] webp変換方針ADRを作成する
  - 作成先: [doc/03_architecture/03_adr/06_ADR-0005-convert-images-to-webp.md](03_architecture/03_adr/06_ADR-0005-convert-images-to-webp.md)
  - 判断観点:
    - 表示速度
    - 保存容量
    - 対応ブラウザ
    - 画質: 品質値80
    - スマートフォン閲覧前提、拡大表示は想定しない
    - 漫画や小説の本文・セリフの可読性
- [x] アーカイブ展開方式ADRを作成する
  - 作成先: [doc/03_architecture/03_adr/07_ADR-0006-extract-archive-files.md](03_architecture/03_adr/07_ADR-0006-extract-archive-files.md)
  - 判断観点:
    - zip / rar / 7zip対応: 7-Zip for Linux コンソール版を使用
    - Java標準ライブラリおよび Apache Commons Compress のrar対応には依存しない
    - Spring Bootアプリケーションから外部プロセスとして呼び出す
    - セキュリティ
    - パストラバーサル対策
    - 破損ファイル対応
- [x] 非同期変換ジョブ方式ADRを作成する
  - 作成先: [doc/03_architecture/03_adr/08_ADR-0007-use-async-conversion-worker.md](03_architecture/03_adr/08_ADR-0007-use-async-conversion-worker.md)
  - 判断観点:
    - アップロード後の待ち時間
    - バックエンドAPIと変換ワーカーの責務分離
    - 失敗時の再実行
    - ジョブ状態管理
    - RabbitMQを使用する
    - DBをジョブ配送キューとして使わず、RabbitMQで配送する
    - 変換ワーカーの同時実行数: 具体値は画像変換設計のリソース制限と設定を正本とする
    - 将来的なスケール
- [x] 認証方式とサーバ側セッション採用ADRを作成する
  - 作成先: [doc/03_architecture/03_adr/09_ADR-0008-use-email-authentication-and-server-side-sessions.md](03_architecture/03_adr/09_ADR-0008-use-email-authentication-and-server-side-sessions.md)
  - 判断観点:
    - メール認証
    - ログイン時のメール2段階認証
    - パスワードリセット
    - HTTP onlyかつSecure属性付きCookieによるサーバ側セッション
    - 認証トークン、チャレンジ、セッションのPostgreSQL正本管理
    - JWTおよび外部IDプロバイダとの比較
- [x] バックアップなし運用ADRを作成する
  - 作成先: [doc/03_architecture/03_adr/10_ADR-0009-no-backup-policy.md](03_architecture/03_adr/10_ADR-0009-no-backup-policy.md)
  - 判断観点:
    - 初期運用の単純さ
    - PostgreSQLと原本ファイル喪失時の復旧不能リスク
    - 派生データの再構築範囲
    - RPO / RTOを定義しない方針
    - バックアップ方針を見直す条件
- [x] 原本ファイル保存方針ADRを作成する
  - 作成先: [doc/03_architecture/03_adr/11_ADR-0010-keep-original-book-files.md](03_architecture/03_adr/11_ADR-0010-keep-original-book-files.md)
  - 判断観点:
    - 原本ファイルを通常運用で保存し続ける
    - WebPとサムネイルの再生成
    - 再変換と変換条件変更への対応
    - 業務上の保存容量上限なし
    - 技術的安全上限との分離

### 主要設計

- [x] データモデル初版を作成する
  - 作成先: [doc/04_design/04_data_model.md](04_design/04_data_model.md)
  - 対象エンティティ:
    - user
    - admin_user
    - role
    - permission
    - book
    - book_file
    - book_page
    - author
    - series
    - book_type
    - tag
    - favorite
    - conversion_job
    - reading_history
  - 検討事項:
    - Spring Bootアプリケーションで扱うドメイン境界
    - 一般ユーザと管理ユーザを同一テーブルにするか分けるか
    - 一般ユーザは書籍を保持しない
    - 書籍アップロードは管理ユーザのみ可能
    - 書籍と著者の多対多
    - 書籍とタグの多対多
    - シリーズ順序の持ち方
    - 論理削除の有無
    - 変換ジョブ実行はRabbitMQで配送し、ジョブ状態管理はDBに保持するか
- [x] 認証トークンとセッションの正本データモデルを定義する
  - 反映先: [doc/04_design/04_data_model.md](04_design/04_data_model.md)
  - 関連:
    - [doc/04_design/08_authorization_design/01_authorization_design.md](04_design/08_authorization_design/01_authorization_design.md)
    - [doc/04_design/03_api_contracts/06_account_api.md](04_design/03_api_contracts/06_account_api.md)
    - [rules/SECURITY.md](../rules/SECURITY.md)
  - 決定事項:
    - `email_verification_token`、`login_challenge`、`password_reset_token`、`session`をPostgreSQLの正本テーブルとする
    - 平文トークン、ワンタイムコード、セッションIDは保存せず、用途別のハッシュ値を保存する
    - メール確認、ログイン2段階認証、パスワードリセットで有効期限、使用済み日時、失効日時、試行回数、再送回数を用途に応じて保持する
    - 退会、停止、パスワード変更、メール変更、ログアウト時の失効条件を定義する
- [x] 検索設計初版を作成する
  - 作成先: [doc/04_design/05_search_design/01_search_design.md](04_design/05_search_design/01_search_design.md)
  - 記載内容:
    - 検索対象項目
    - タイトル検索
    - 著者検索
    - タグ検索
    - シリーズ検索
    - 複合検索
    - ソート条件
    - ページング
    - 日本語検索と表記揺れ正規化の方針
    - Elasticsearch必須プラグインは技術スタックを正本とする
    - インデックス更新タイミング
    - PostgreSQLを正とし、Elasticsearchは再構築可能な派生データとして扱う
    - 更新失敗時は再試行キューに積む
- [x] ファイル保存設計初版を作成する
  - 作成先: [doc/04_design/06_file_storage_design.md](04_design/06_file_storage_design.md)
  - 記載内容:
    - 原本ファイルの保存有無: 保存し続ける
    - 変換後webpの保存場所
    - サムネイルの保存場所
    - ディレクトリ命名規則
    - ファイル命名規則
    - 削除時の扱い
    - バックアップ対象: バックアップは行わない
- [x] 画像変換設計初版を作成する
  - 作成先: [doc/04_design/07_image_conversion_design.md](04_design/07_image_conversion_design.md)
  - 記載内容:
    - Spring Boot変換ワーカーの責務
    - 非同期ジョブの投入、取得、実行方式: RabbitMQ
    - 対応アーカイブ形式: zip / rar / 7zip
    - rar / 7zip展開方式: 7-Zip for Linux コンソール版を外部プロセスとして呼び出す
    - 対応画像形式
    - 展開処理: ジョブごとに専用作業ディレクトリで実行
    - 画像ソート順
    - webp変換条件: 品質値80、application.propertiesで設定可能にする
    - サムネイル生成条件: 生成する
    - 変換失敗時の扱い
    - 再変換仕様
    - 変換ジョブ状態
    - リソース制限: 具体値は画像変換設計のリソース制限と設定を正本とする
- [x] 権限設計初版を作成する
  - 作成先: [doc/04_design/08_authorization_design/01_authorization_design.md](04_design/08_authorization_design/01_authorization_design.md)
  - 記載内容:
    - 一般ユーザの権限: 書籍アップロード不可、閲覧のみ
    - 管理ユーザの権限: 書籍アップロード可能
    - 管理ロール
    - ロール別操作可否
    - 退会時のデータ扱い: 一般ユーザは書籍を保持しない
    - 自分の書籍のみ操作できるか: 一般ユーザは書籍を保持しない
    - 管理者が一般ユーザの書籍を操作できるか: 対象外

## エピック別ToDo

### エピック: 自炊本管理

- [x] エピック定義を作成する
  - 作成先: [doc/02_backlog/01_epics/01_book_management.md](02_backlog/01_epics/01_book_management.md)
  - 目的:
    - 自炊した本をアップロードし、メタ情報を管理できるようにする
- [x] 本アップロードのユーザーストーリーを作成する
  - 作成先: [doc/02_backlog/02_user_stories/01_book_upload.md](02_backlog/02_user_stories/01_book_upload.md)
  - 例:
    - 管理ユーザとして、自炊本のアーカイブファイルをアップロードしたい。なぜなら一般ユーザがWeb上で本を読めるようにしたいから。
- [x] 本アップロードの受入条件を作成する
  - 作成先: [doc/02_backlog/03_acceptance_criteria/01_book_upload.md](02_backlog/03_acceptance_criteria/01_book_upload.md)
  - 条件候補:
    - 管理ユーザのみアップロードできる
    - 一般ユーザはアップロードできない
    - zipファイルをアップロードできる
    - rarファイルをアップロードできる
    - 7zipファイルをアップロードできる
    - 許可されていない拡張子は拒否される
    - 業務上の保存容量上限は設けず、アップロード1ファイルの技術的安全上限は設ける
    - アップロード後に変換ジョブが作成される
    - 変換状態を画面で確認できる
- [x] 本メタ情報編集のユーザーストーリーを作成する
  - 作成先: [doc/02_backlog/02_user_stories/02_book_metadata_edit.md](02_backlog/02_user_stories/02_book_metadata_edit.md)
  - 受入条件: [doc/02_backlog/03_acceptance_criteria/02_book_metadata_edit.md](02_backlog/03_acceptance_criteria/02_book_metadata_edit.md)
  - 対象項目:
    - タイトル
    - 著者
    - タグ
    - シリーズ
    - 種別
    - シリーズ概要
    - 巻数または並び順
    - 表紙画像
- [x] 本削除の仕様を決める
  - ユーザーストーリー: [doc/02_backlog/02_user_stories/03_book_delete.md](02_backlog/02_user_stories/03_book_delete.md)
  - 受入条件: [doc/02_backlog/03_acceptance_criteria/03_book_delete.md](02_backlog/03_acceptance_criteria/03_book_delete.md)
  - 反映先:
    - [doc/04_design/06_file_storage_design.md](04_design/06_file_storage_design.md)
    - [doc/04_design/04_data_model.md](04_design/04_data_model.md)
  - 決定事項:
    - 管理ユーザのみ書籍を削除できる
    - 一般ユーザは書籍を保持しない
    - 原本ファイルは通常運用では保存し続ける
    - 書籍削除時の原本 / 変換済みwebp / サムネイルの物理削除タイミングを設計に明記する
    - ElasticsearchのインデックスはPostgreSQLを正として削除 / 再構築できるようにする

### エピック: 画像変換

- [x] エピック定義を作成する
  - 作成先: [doc/02_backlog/01_epics/02_image_conversion.md](02_backlog/01_epics/02_image_conversion.md)
- [x] アーカイブ展開の仕様を決める
  - 反映先: [doc/04_design/07_image_conversion_design.md](04_design/07_image_conversion_design.md)
  - 決定事項:
    - rar / 7zipは7-Zip for Linux コンソール版で展開する
    - 7-Zipは変換ワーカーコンテナ内で利用する
    - 展開先はジョブごとの専用作業ディレクトリとする
  - 検討事項:
    - 展開後のクリーンアップ
    - パストラバーサル対策
    - 暗号化アーカイブの扱い
    - 破損アーカイブの扱い
- [x] 画像ファイル判定の仕様を決める
  - 検討事項:
    - 拡張子で判定するか
    - MIME typeで判定するか
    - 実体を読んで判定するか
    - 対応画像形式
- [x] ページ順序の仕様を決める
  - 検討事項:
    - ファイル名順
    - 自然順ソート
    - サブディレクトリを含む場合の順序
    - 表紙の決定方法
- [x] webp変換条件を決める
  - 決定事項:
    - 品質値: 80
    - スマートフォンでの閲覧を想定する
    - 拡大表示は想定しない
    - application.propertiesで設定可能にする
  - 検討事項:
    - 最大幅
    - 最大高さ
    - 透過画像の扱い
    - 縦長画像の扱い
    - 変換後のファイルサイズ目安
- [x] 変換ジョブ状態を定義する
  - 状態候補:
    - queued
    - extracting
    - converting
    - completed
    - failed
    - canceled
  - 反映先:
    - [doc/04_design/04_data_model.md](04_design/04_data_model.md)
    - [doc/04_design/07_image_conversion_design.md](04_design/07_image_conversion_design.md)
- [x] 再変換仕様を決める
  - 検討事項:
    - 手動再実行できるか
    - 失敗ページだけ再実行するか
    - 全ページを再実行するか
    - 再変換時に既存webpを上書きするか

### エピック: 本の閲覧

- [x] エピック定義を作成する
  - 作成先: [doc/02_backlog/01_epics/03_book_viewer.md](02_backlog/01_epics/03_book_viewer.md)
- [x] 本一覧表示のユーザーストーリーを作成する
  - 作成先: [doc/02_backlog/02_user_stories/03_book_list.md](02_backlog/02_user_stories/03_book_list.md)
- [x] ビューア表示のユーザーストーリーを作成する
  - 作成先: [doc/02_backlog/02_user_stories/04_book_viewing.md](02_backlog/02_user_stories/04_book_viewing.md)
- [x] ビューア操作仕様を決める
  - 反映先: [doc/04_design/01_ui_flows.md](04_design/01_ui_flows.md)
  - 検討事項:
    - 1ページ表示
    - 見開き表示
    - 次ページ
    - 前ページ
    - 最初のページへ移動
    - 最後のページへ移動
    - ページ番号指定
    - 拡大縮小
    - キーボード操作
    - スマートフォン操作
- [x] 閲覧履歴仕様を決める
  - 決定事項:
    - 閲覧履歴は保存する
  - 検討事項:
    - 最後に読んだページ
    - 最終閲覧日時
    - ユーザ単位で持つか
    - 端末単位で持つか
- [x] お気に入り登録のユーザーストーリーを作成する
  - 作成先: [doc/02_backlog/02_user_stories/05_favorite.md](02_backlog/02_user_stories/05_favorite.md)
- [x] お気に入りの受入条件を作成する
  - 作成先: [doc/02_backlog/03_acceptance_criteria/02_favorite.md](02_backlog/03_acceptance_criteria/02_favorite.md)
  - 条件候補:
    - 本をお気に入り登録できる
    - お気に入り解除できる
    - お気に入り一覧を表示できる
    - 同じ本を重複登録できない

### エピック: 検索

- [x] エピック定義を作成する
  - 作成先: [doc/02_backlog/01_epics/04_search.md](02_backlog/01_epics/04_search.md)
- [x] 検索ユーザーストーリーを作成する
  - 作成先: [doc/02_backlog/02_user_stories/06_book_search.md](02_backlog/02_user_stories/06_book_search.md)
- [x] 検索対象項目を確定する
  - 反映先: [doc/04_design/05_search_design/01_search_design.md](04_design/05_search_design/01_search_design.md)
  - 対象候補:
    - タイトル
    - 著者名
    - タグ
    - シリーズ名
    - 種別
    - シリーズ概要
- [x] あいまい検索仕様を決める
  - 決定事項:
    - 日本語検索用のカスタムアナライザを使用する
    - 全角 / 半角などの表記揺れ対策としてnormalizerを利用する
    - Elasticsearch必須プラグインは技術スタックを正本とする
    - 補完 / 部分一致が必要な項目にはedge n-gram系フィールドを追加する
  - 検討事項:
    - typo許容
    - ひらがなカタカナ
- [x] Elasticsearchインデックス設計を作成する
  - 作成先: [doc/04_design/05_search_design/02_search_index_design.md](04_design/05_search_design/02_search_index_design.md)
  - 記載内容:
    - index name
    - mapping
    - analyzer
    - normalizer
    - 必須プラグイン確認
    - edge n-gram系フィールド
    - searchable fields
    - sortable fields
    - boost設定
    - 更新タイミング
    - PostgreSQLからの全件再インデックス手順
    - 書籍単位の再インデックス手順
- [x] 検索結果表示仕様を決める
  - 検討事項:
    - 表紙サムネイル
    - タイトル
    - 著者
    - タグ
    - シリーズ
    - 並び順
    - ページング
    - 絞り込み

### エピック: アカウント管理

- [x] エピック定義を作成する
  - 作成先: [doc/02_backlog/01_epics/05_account_management.md](02_backlog/01_epics/05_account_management.md)
- [x] 会員登録のユーザーストーリーを作成する
  - 作成先: [doc/02_backlog/02_user_stories/07_user_registration.md](02_backlog/02_user_stories/07_user_registration.md)
- [x] ログイン・ログアウトのユーザーストーリーを作成する
  - 作成先: [doc/02_backlog/02_user_stories/08_user_login_logout.md](02_backlog/02_user_stories/08_user_login_logout.md)
- [x] 会員情報編集のユーザーストーリーを作成する
  - 作成先: [doc/02_backlog/02_user_stories/09_user_profile_edit.md](02_backlog/02_user_stories/09_user_profile_edit.md)
- [x] 退会のユーザーストーリーを作成する
  - 作成先: [doc/02_backlog/02_user_stories/10_user_withdrawal.md](02_backlog/02_user_stories/10_user_withdrawal.md)
- [x] 認証方式を決める
  - 反映先: [doc/04_design/08_authorization_design/01_authorization_design.md](04_design/08_authorization_design/01_authorization_design.md)
  - 決定事項:
    - メール認証を行う
    - 登録時だけでなく、ログイン時の2段階認証にもメールを活用する
    - パスワードリセットを提供する
  - 検討事項:
    - セッション認証
    - JWT
    - ログイン失敗制限
- [x] 退会時のデータ扱いを決める
  - 決定事項:
    - 一般ユーザは書籍を保持しないため、退会時のアップロード済み書籍削除は対象外
  - 検討事項:
    - ユーザ情報の論理削除
    - お気に入りの削除
    - 監査ログの保持

### エピック: 管理機能

- [x] エピック定義を作成する
  - 作成先: [doc/02_backlog/01_epics/06_admin_management.md](02_backlog/01_epics/06_admin_management.md)
- [x] 管理ユーザログインのユーザーストーリーを作成する
  - 作成先: [doc/02_backlog/02_user_stories/11_admin_login_logout.md](02_backlog/02_user_stories/11_admin_login_logout.md)
- [x] 管理ユーザ管理のユーザーストーリーを作成する
  - 作成先: [doc/02_backlog/02_user_stories/12_admin_user_management.md](02_backlog/02_user_stories/12_admin_user_management.md)
- [x] ロール設定のユーザーストーリーを作成する
  - 作成先: [doc/02_backlog/02_user_stories/13_role_management.md](02_backlog/02_user_stories/13_role_management.md)
- [x] 管理ロール一覧を定義する
  - 反映先: [doc/04_design/08_authorization_design/01_authorization_design.md](04_design/08_authorization_design/01_authorization_design.md)
  - ロール候補:
    - super_admin
    - admin
    - operator
    - viewer
- [x] 権限マトリクスを作成する
  - 作成先: [doc/04_design/08_authorization_design/02_permission_matrix.md](04_design/08_authorization_design/02_permission_matrix.md)
  - 操作候補:
    - 管理ユーザ登録
    - 管理ユーザ編集
    - 管理ユーザ削除
    - ロール設定
    - 一般ユーザ閲覧
    - 一般ユーザ停止
    - 書籍アップロード: 管理ユーザのみ
    - 書籍閲覧
    - 書籍削除: 管理ユーザのみ
    - 変換ジョブ再実行

### エピック: 運用

- [x] エピック定義を作成する
  - 作成先: [doc/02_backlog/01_epics/07_operations.md](02_backlog/01_epics/07_operations.md)
- [x] Runbookを作成する
  - 作成先: [doc/07_operations/01_runbook.md](07_operations/01_runbook.md)
  - 記載内容:
    - Docker Composeによる単一Linuxホスト運用手順
    - Spring BootバックエンドAPIの起動手順
    - Spring Boot変換ワーカーの起動手順
    - 停止手順
    - 再起動手順
    - ログ確認手順
    - 変換ジョブ失敗時の確認手順
    - Elasticsearch再インデックス手順
    - 全件再インデックス手順
    - 書籍単位の再インデックス手順
    - `docker compose down -v`、Docker volume削除、保存領域削除の通常運用禁止
    - 削除系管理コマンドのdry-run、対象件数表示、二段階確認
    - 障害時の利用者向け説明方針
- [x] バックアップなし方針を作成する
  - 作成先: [doc/07_operations/02_backup_restore.md](07_operations/02_backup_restore.md)
  - 記載内容:
    - バックアップは行わない
    - PostgreSQL / Elasticsearch / 原本ファイル / 変換済みwebp / サムネイルはバックアップ対象にしない
    - ElasticsearchはPostgreSQLから再構築可能な派生データとして扱う
    - バックアップなし運用のリスクと許容範囲
    - RPO / RTOと復旧不能リスク
    - バックアップ方針見直しトリガー
- [x] 監視方針を作成する
  - 作成先: [doc/07_operations/03_monitoring.md](07_operations/03_monitoring.md)
  - 監視候補:
    - Spring BootバックエンドAPI死活
    - Spring Boot変換ワーカー死活
    - JVMメトリクス
    - DB接続
    - Elasticsearch接続
    - ストレージ容量
    - 変換ジョブ滞留
    - 変換失敗数
    - 画像変換処理時間
    - 7-Zip外部プロセスの失敗数
    - 変換ワーカーの同時実行数
    - 1ジョブ30分タイムアウトの発生数
- [x] 障害ログの記録形式を作成する
  - 作成先: [doc/07_operations/05_incident_log_template.md](07_operations/05_incident_log_template.md)
  - 項目:
    - 発生日時
    - 影響範囲
    - 原因
    - 暫定対応
    - 恒久対応
    - 再発防止策
    - バックアップなし運用に関するRPO / RTO判断
    - 復旧不能範囲と利用者向け説明
- [x] リリースノート形式を作成する
  - 作成先: [doc/07_operations/04_release_note_template.md](07_operations/04_release_note_template.md)
  - 項目:
    - バージョン
    - リリース日
    - 追加機能
    - 修正
    - 既知の問題
    - 移行作業

## テスト関連ToDo

- [x] テスト戦略を作成する
  - 作成先: [doc/06_testing/01_test_strategy.md](06_testing/01_test_strategy.md)
  - 記載内容:
    - テスト範囲
    - 単体テスト方針
    - 結合テスト方針
    - E2Eテスト方針
    - パフォーマンステスト方針
    - 画像変換ワーカーの負荷テスト方針
    - 手動確認方針
    - 回帰テスト方針
- [x] アップロード機能の受入テストを作成する
  - 作成先: [doc/06_testing/02_acceptance_tests/01_book_upload_acceptance_tests.md](06_testing/02_acceptance_tests/01_book_upload_acceptance_tests.md)
  - 観点:
    - 管理ユーザによる正常アップロード
    - 一般ユーザによるアップロード不可
    - 非対応形式
    - 業務上の保存容量上限なし、アップロード1ファイルの技術的安全上限あり
    - 破損ファイル
    - 変換ジョブ作成
- [x] 画像変換機能の受入テストを作成する
  - 作成先: [doc/06_testing/02_acceptance_tests/02_image_conversion_acceptance_tests.md](06_testing/02_acceptance_tests/02_image_conversion_acceptance_tests.md)
  - 観点:
    - zip展開
    - 7-Zip for Linux コンソール版によるrar展開
    - 7-Zip for Linux コンソール版による7zip展開
    - webp変換: 品質値80
    - サムネイル生成
    - 失敗時のステータス
    - 1ジョブ30分タイムアウト
    - 画像変換設計で定義する同時実行数
    - 画像変換設計で定義する安全上限超過
- [x] 検索機能の受入テストを作成する
  - 作成先: [doc/06_testing/02_acceptance_tests/03_search_acceptance_tests.md](06_testing/02_acceptance_tests/03_search_acceptance_tests.md)
  - 観点:
    - タイトル検索
    - 著者検索
    - タグ検索
    - シリーズ検索
    - 表記揺れ
    - 日本語検索と表記揺れ正規化
    - Elasticsearch再インデックス
    - 検索結果なし
- [x] 閲覧機能の受入テストを作成する
  - 作成先: [doc/06_testing/02_acceptance_tests/04_viewer_acceptance_tests.md](06_testing/02_acceptance_tests/04_viewer_acceptance_tests.md)
  - 観点:
    - ページ表示
    - ページ送り
    - 見開き
    - 拡大縮小
    - スマートフォン表示
- [x] 権限機能の受入テストを作成する
  - 作成先: [doc/06_testing/02_acceptance_tests/05_authorization_acceptance_tests.md](06_testing/02_acceptance_tests/05_authorization_acceptance_tests.md)
  - 観点:
    - 未ログインアクセス
    - 一般ユーザアクセス
    - 管理ユーザアクセス
    - ロール別アクセス制御
    - 他ユーザデータの操作可否
- [x] 回帰テスト一覧を作成する
  - 作成先: [doc/06_testing/03_regression_tests.md](06_testing/03_regression_tests.md)
  - 対象:
    - ログイン
    - アップロード
    - 変換
    - 検索
    - 閲覧
    - お気に入り
    - 管理ユーザ管理

## API契約ToDo

- [x] API設計方針を作成する
  - 作成先: [doc/04_design/03_api_contracts/01_api_design_policy.md](04_design/03_api_contracts/01_api_design_policy.md)
  - 記載内容:
    - Spring BootバックエンドAPIの前提
    - URL命名
    - HTTPメソッド
    - 認証方式
    - ページング
    - エラーレスポンス
    - バリデーションエラー形式
- [x] 自炊本管理API契約を作成する
  - 作成先: [doc/04_design/03_api_contracts/02_book_api.md](04_design/03_api_contracts/02_book_api.md)
  - API候補:
    - 本一覧取得
    - 本詳細取得
    - 本アップロード
    - 本メタ情報更新
    - 本削除
- [x] 変換ジョブAPI契約を作成する
  - 作成先: [doc/04_design/03_api_contracts/03_conversion_job_api.md](04_design/03_api_contracts/03_conversion_job_api.md)
  - API候補:
    - ジョブ状態取得
    - ジョブ再実行
    - ジョブキャンセル
- [x] 検索API契約を作成する
  - 作成先: [doc/04_design/03_api_contracts/04_search_api.md](04_design/03_api_contracts/04_search_api.md)
  - API候補:
    - キーワード検索
    - タグ検索
    - 著者検索
    - シリーズ検索
    - サジェスト
- [x] 閲覧API契約を作成する
  - 作成先: [doc/04_design/03_api_contracts/05_viewer_api.md](04_design/03_api_contracts/05_viewer_api.md)
  - API候補:
    - ページ一覧取得
    - ページ画像取得
    - 閲覧位置保存
- [x] アカウントAPI契約を作成する
  - 作成先: [doc/04_design/03_api_contracts/06_account_api.md](04_design/03_api_contracts/06_account_api.md)
  - API候補:
    - 会員登録
    - ログイン
    - ログアウト
    - 会員情報取得
    - 会員情報更新
    - 退会
- [x] 管理API契約を作成する
  - 作成先: [doc/04_design/03_api_contracts/07_admin_api.md](04_design/03_api_contracts/07_admin_api.md)
  - API候補:
    - 管理ユーザ一覧
    - 管理ユーザ登録
    - 管理ユーザ編集
    - 管理ユーザ削除
    - ロール一覧
    - ロール設定

## UI関連ToDo

- [x] UIフロー全体図を作成する
  - 作成先: [doc/04_design/01_ui_flows.md](04_design/01_ui_flows.md)
  - 対象:
    - 一般ユーザ登録
    - ログイン
    - 本一覧
    - 本アップロード
    - メタ情報編集
    - 検索
    - 閲覧
    - お気に入り
    - 管理ユーザ管理
- [x] 画面メモを作成する
  - 作成先: [doc/04_design/02_screen_notes.md](04_design/02_screen_notes.md)
  - 画面候補:
    - ログイン画面
    - 会員登録画面
    - 本一覧画面
    - 本詳細画面
    - 本アップロード画面
    - メタ情報編集画面
    - 検索結果画面
    - ビューア画面
    - お気に入り画面
    - 管理ログイン画面
    - 管理ユーザ一覧画面
    - 管理ユーザ編集画面
    - ロール設定画面

## 決定事項

- [x] フロントエンド技術: Next.js
- [x] Javaバージョン: 25
- [x] Spring Bootバージョン: 4.0.6
- [x] Spring Bootプロジェクト構成: 単一アプリ内モジュールとする
  - 今後の規模拡大を想定し、API / Workerを別アプリへ分離可能な構成にする
- [x] 原本ファイル: 保存し続ける
- [x] 1冊あたりの業務上の保存容量上限: なし
- [x] アップロード、展開、エントリ数、画像ピクセル数、同時アップロードの技術的安全上限
  - 具体値は [doc/04_design/07_image_conversion_design.md](04_design/07_image_conversion_design.md) の「リソース制限と設定」を正本とする
- [x] 1ユーザあたりの保存容量上限: なし
- [x] 非同期ジョブの実装方式: DBをジョブ配送キューとして使わず、RabbitMQを採用する
  - Spring AMQPでAPIと変換ワーカーを接続する
  - PostgreSQLの`conversion_job`を業務状態の正本とする
  - 配送保証はat-least-onceとし、重複配送はPostgreSQLの状態確認で冪等に扱う
  - 再試行上限後はdead letter queueへ送る
- [x] 変換ワーカーの同時実行数
  - 具体値は [doc/04_design/07_image_conversion_design.md](04_design/07_image_conversion_design.md) の「リソース制限と設定」を正本とする
  - application.propertiesで設定可能にする
- [x] サムネイル: 生成する
- [x] 閲覧履歴: 保存する
- [x] 一般ユーザ同士の書籍共有: しない
- [x] 書籍アップロード権限: 管理者のみアップロード可能とする
  - 一般ユーザは書籍を保持しない
- [x] 管理者による一般ユーザ所有書籍の閲覧: 対象外
  - 一般ユーザは書籍を保持しないため
  - 管理ユーザの `book.view` は管理対象書籍の内容確認・品質確認を意味し、詳細は [doc/04_design/08_authorization_design/02_permission_matrix.md](04_design/08_authorization_design/02_permission_matrix.md) を正本とする
- [x] 管理者による一般ユーザの書籍削除: 対象外
  - 一般ユーザは書籍を保持しないため
- [x] 退会時のアップロード済み書籍削除: 対象外
  - 一般ユーザは書籍を保持しないため
- [x] メール認証: 行う
  - 登録時だけでなく、ログイン時の2段階認証にもメールを活用する
- [x] パスワードリセット: 提供する
- [x] 認証トークンとセッションの正本データモデル
  - `email_verification_token`、`login_challenge`、`password_reset_token`、`session`をPostgreSQLの正本とする
  - 平文トークン、ワンタイムコード、セッションIDは保存せず、用途別ハッシュ、有効期限、使用済み日時、失効日時、試行回数、再送回数で管理する
  - 退会、停止、パスワード変更、メール変更、ログアウト時は関連する未使用トークン、未使用チャレンジ、既存セッションを失効する
- [x] バックアップ: 行わない
  - 詳細なRPO / RTO、復旧不能リスク、禁止操作、削除系操作の安全柵は [doc/07_operations/02_backup_restore.md](07_operations/02_backup_restore.md) を正本とする
  - 具体的な運用手順は [doc/07_operations/01_runbook.md](07_operations/01_runbook.md) を正本とする
- [x] デプロイ方式: Spring Boot APIと変換ワーカーを同一ホストに配置する
  - 今後の規模拡大を想定し、別ホストへ分離可能な構成にする
- [x] rar / 7zip の解凍方式: 外部アプリケーションの 7-Zip for Linux コンソール版を使用する
  - https://7-zip.opensource.jp/download.html
  - Java標準ライブラリおよび Apache Commons Compress の rar 対応には依存しない
  - Spring Bootアプリケーションから外部プロセスとして呼び出す
- [x] 変換ワーカーのリソース制限
  - 変換ワーカーの同時実行数、1ジョブのタイムアウト、1ジョブの一時ディスク使用量は画像変換設計を正本とする
  - 外部プロセス: ジョブごとに専用作業ディレクトリで実行
  - メモリ / CPU制限: OSまたはコンテナ側で制御
- [x] PostgreSQLとElasticsearchの整合性回復手順
  - PostgreSQLを正とする
  - ElasticsearchはPostgreSQLから再構築可能な派生データとする
  - 書籍単位でElasticsearchへ再インデックスできる管理コマンドを用意する
  - 全件再インデックスできる管理コマンドを用意する
  - Elasticsearchインデックスは破棄してPostgreSQLから再構築可能とする
  - 更新失敗時は再試行キューに積む
  - 全件再インデックス手順をRunbookに記載する
- [x] Elasticsearchの日本語アナライザと正規化方針
  - 日本語のタイトル / 著者 / タグ検索にはカスタムアナライザを使用する
  - 全角 / 半角などの表記揺れ対策としてnormalizerを利用する
  - Elasticsearch必須プラグインは [doc/03_architecture/02_technology_stack.md](03_architecture/02_technology_stack.md) を正本とする
  - Docker Compose、ローカル開発環境、本番運用環境では技術スタックで定義された必須プラグインを導入する
  - API起動時またはインデックス作成前に必須プラグインを確認し、未導入の場合はインデックス作成を失敗させる
  - 補完 / 部分一致が必要な項目には別途edge n-gram系フィールドを追加する
- [x] webpの品質値: 80
  - スマートフォンでの閲覧を想定する
  - 拡大表示は想定しない
  - 漫画や小説の本文・セリフの可読性を確保しつつ、過度な高品質設定にはしない
  - application.propertiesで設定可能にする
- [x] 本番運用環境: 単一Linuxホスト上のDocker Compose構成とする
  - Spring Boot API / Worker、Next.js、PostgreSQL、Elasticsearch、RabbitMQを同一ホストに配置する
  - 7-Zip for Linux コンソール版を変換ワーカーコンテナ内で利用する
  - 将来的にAPI / Worker / ミドルウェアを別ホストへ分離可能な構成にする

## 未決事項

未決事項は、単に「残っているもの」として扱わず、いつ決める必要があるかで分類する。
この節は横断的な分類インデックスとし、仕様や判断理由の正本はリンク先の設計ドキュメントまたはADRに置く。
分類を変更した場合は、関連ドキュメントの「後続で詳細化する事項」と矛盾しないことを確認する。

### 分類ルール

| 分類 | 意味 | 扱い |
| --- | --- | --- |
| 実装前必須 | 該当機能の実装に入る前に、仕様、責務、設定名、失敗時の扱いを確定しないと手戻りや不整合が大きい事項。 | 実装Issueの着手前または同一作業で設計ドキュメントへ反映する。 |
| Beta以降 | MVPの成立には不要だが、利用体験、運用性、品質を高めるために後続リリースで検討する事項。 | MVP実装のブロッカーにせず、ロードマップや個別Issueで優先度を判断する。 |
| 運用で確定 | 実ホスト、利用量、障害傾向、監視結果を見て調整する方が妥当な事項。 | 初期値または候補値を持ったうえで、Runbook、監視、運用記録から見直す。 |

### 実装前必須

| 項目 | 決める内容 | 正本または反映先 |
| --- | --- | --- |
| 変換リソース上限 | アップロード、展開、画像デコード、一時領域、同時実行数、タイムアウトの設定名、既定値、上限超過時のエラーとジョブ状態。既定値は画像変換設計にあるが、実装時のプロパティ名と検証箇所を確定する。 | [画像変換設計](04_design/07_image_conversion_design.md#リソース制限と設定)、[アップロードAPI契約](04_design/03_api_contracts/02_book_api.md) |
| 認証トークン / セッションのデータモデル | トークン、ワンタイムコード、セッションIDのハッシュ方式、pepper、鍵管理、期限、試行回数、再送制限、期限切れレコード削除方針。 | [データモデル](04_design/04_data_model.md)、[認可設計](04_design/08_authorization_design/01_authorization_design.md)、[アカウントAPI契約](04_design/03_api_contracts/06_account_api.md)、[セキュリティルール](../rules/SECURITY.md) |
| Elasticsearch必須プラグイン前提 | `analysis-kuromoji` と `analysis-icu` の導入確認、未導入時の起動時またはインデックス作成前チェック、Docker ComposeとRunbookの復旧手順。 | [技術スタック](03_architecture/02_technology_stack.md#elasticsearch必須プラグイン)、[Elasticsearchインデックス設計](04_design/05_search_design/02_search_index_design.md#必須プラグイン確認)、[Runbook](07_operations/01_runbook.md) |
| 検索更新責務とOutbox方針 | PostgreSQL更新後のElasticsearch更新責務、失敗記録、再試行配送、Outboxテーブルを使うか、RabbitMQ再試行キューだけで扱うか、冪等な再生成単位。 | [検索設計](04_design/05_search_design/01_search_design.md#更新失敗時の再試行)、[Elasticsearchインデックス設計](04_design/05_search_design/02_search_index_design.md#更新タイミング)、[データフロー](03_architecture/06_data_flow.md) |

### Beta以降

| 項目 | 扱い | 関連ドキュメント |
| --- | --- | --- |
| 補完検索や高度な部分一致 | MVPでは基本検索を優先し、edge n-gramの対象拡大、サジェストAPI、typo許容、ハイライトは利用状況を見て検討する。 | [検索設計](04_design/05_search_design/01_search_design.md)、[検索API契約](04_design/03_api_contracts/04_search_api.md) |
| 詳細な監視基盤、SLO、アラート通知 | 初期はログ、ヘルスチェック、主要メトリクス確認を優先し、SLO、通知先、アラートしきい値は運用実績を見て追加する。 | [品質特性](03_architecture/07_quality_attributes.md)、[監視方針](07_operations/03_monitoring.md) |
| バックアップ方針の見直し | 初期運用ではバックアップなしを採用し、利用規模、復旧要求、データ保全要求が変わった段階で再検討する。 | [バックアップなし方針](07_operations/02_backup_restore.md)、[ADR-0009](03_architecture/03_adr/10_ADR-0009-no-backup-policy.md) |
| UIの後続機能 | 見開き、詳細な拡大縮小、キーボード操作、スマートフォン操作、詳細な空状態やエラー文言はMVP後に具体化する。 | [UIフロー](04_design/01_ui_flows.md)、[画面メモ](04_design/02_screen_notes.md)、[ビューアAPI契約](04_design/03_api_contracts/05_viewer_api.md) |
| 管理・マスタ管理の拡張 | 著者、タグ、シリーズ、種別のマスタ管理API、ロール設定変更、管理操作監査ログの詳細はMVPの基本管理機能後に扱う。 | [本API契約](04_design/03_api_contracts/02_book_api.md)、[管理API契約](04_design/03_api_contracts/07_admin_api.md)、[権限設計](04_design/08_authorization_design/01_authorization_design.md) |

### 運用で確定

| 項目 | 初期方針 | 関連ドキュメント |
| --- | --- | --- |
| 実ホストに合わせたワーカー同時実行数 | 既定値2で開始し、初期運用では2から4を目安に調整する。最大10は設定上の許容値であり、単一ホストのCPU、メモリ、一時ディスク、I/Oを確認してから引き上げる。 | [画像変換設計](04_design/07_image_conversion_design.md#リソース制限と設定)、[品質特性](03_architecture/07_quality_attributes.md) |
| ディスク容量警告閾値 | 初期値を監視方針またはRunbookで持ち、実データ増加量とバックアップなし方針のリスク許容に合わせて見直す。 | [監視方針](07_operations/03_monitoring.md)、[Runbook](07_operations/01_runbook.md)、[ファイル保存設計](04_design/06_file_storage_design.md) |
| ログ保持期間 | 初期運用で扱える保存量と障害調査に必要な期間を見て決める。個人情報や秘密情報を残さない前提を守る。 | [Runbook](07_operations/01_runbook.md)、[インシデントログテンプレート](07_operations/05_incident_log_template.md)、[セキュリティルール](../rules/SECURITY.md) |
| 実運用での再試行回数や通知閾値 | 変換失敗、検索更新失敗、dead letter、ジョブ滞留の傾向を見て、再試行回数、通知条件、手動確認条件を調整する。 | [画像変換設計](04_design/07_image_conversion_design.md)、[検索設計](04_design/05_search_design/01_search_design.md)、[監視方針](07_operations/03_monitoring.md) |

### 後続詳細化項目との対応

各設計ドキュメントの「後続で詳細化する事項」は、次の分類で扱う。ここにない細目は、同じ性質の分類へ寄せる。

| ドキュメント | 主な後続詳細化項目 | 分類 |
| --- | --- | --- |
| [システム概要](03_architecture/01_system_overview.md#今後詳細化するドキュメント) | 後続ドキュメント作成先の案内。 | 完了済みの案内であり、未決事項としては扱わない。 |
| [技術スタック](03_architecture/02_technology_stack.md#今後詳細化する事項) | ADR、設計、環境設定の具体化。 | 多くは完了済み。Elasticsearch必須プラグイン確認は実装前必須。 |
| [コンテナ図](03_architecture/05_container_diagram.md#今後詳細化する事項) | Docker Composeサービス名、ネットワーク、監視、ログ、リソース制限。 | サービス名と必須プラグイン確認は実装前必須。監視、ログ、リソース調整は運用で確定。 |
| [データフロー](03_architecture/06_data_flow.md#今後詳細化する事項) | 変換ジョブ状態、検索更新失敗、再試行、整合性回復。 | ジョブ状態と検索更新責務は実装前必須。整合性回復の運用手順は運用で確定。 |
| [品質特性](03_architecture/07_quality_attributes.md#今後詳細化する事項) | 負荷試験、監視、容量不足時の手順。 | 監視基盤とSLOはBeta以降。容量警告しきい値と手順は運用で確定。 |
| [UIフロー](04_design/01_ui_flows.md#後続で詳細化する事項) / [画面メモ](04_design/02_screen_notes.md#後続で詳細化する事項) | ワイヤーフレーム、見開き、拡大縮小、管理ダッシュボード表示。 | UIの後続機能としてBeta以降。 |
| [データモデル](04_design/04_data_model.md#後続設計で詳細化する事項) | 認証秘密情報、権限、保存、検索更新状態。 | 認証トークン / セッションと検索更新状態は実装前必須。権限や保存の拡張は対象機能の実装前に確定する。 |
| [ファイル保存設計](04_design/06_file_storage_design.md#後続設計で詳細化する事項) | 物理削除、再変換差し替え、サムネイル命名。 | 削除・再変換の実装前に確定。用途別サムネイル拡張はBeta以降。 |
| [画像変換設計](04_design/07_image_conversion_design.md#後続設計で詳細化する事項) | サムネイル、WebP条件、RabbitMQ、再実行、Runbook、画像処理ライブラリ。 | 変換リソース上限、RabbitMQ、失敗コード、ライブラリは実装前必須。Runbook細部と同時実行調整は運用で確定。 |
| [検索設計](04_design/05_search_design/01_search_design.md#elasticsearchインデックス設計との関係) / [Elasticsearchインデックス設計](04_design/05_search_design/02_search_index_design.md) | インデックス定義、必須プラグイン、更新失敗、再インデックス、補完。 | 必須プラグインと検索更新責務は実装前必須。補完や高度な部分一致はBeta以降。 |
| [API契約](04_design/03_api_contracts/) | 各APIの追加契約、エラー、権限、キャッシュ、管理操作。 | MVP対象APIの実装前に確定。サジェスト、見開き、管理拡張はBeta以降。 |
| [受入条件・受入テスト](02_backlog/03_acceptance_criteria/) / [受入テスト](06_testing/02_acceptance_tests/) | 重複アップロード、冪等性、物理削除、詳細E2E。 | 対象機能の実装前に確定。MVP外の復元や詳細E2EはBeta以降。 |

## 優先着手順

1. プロダクトビジョン
2. ユーザーストーリーマップ
3. エピック定義
4. MVP範囲の決定
5. 技術スタック
6. システム概要
7. 非同期変換ジョブ方式ADR
8. データモデル
9. ファイル保存設計
10. 画像変換設計
11. 検索設計
12. 権限設計
13. API契約
14. UIフロー
15. テスト戦略
16. Runbook
