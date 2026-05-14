# GitHub Project Issue一覧

## 目的

このドキュメントは、GitHub Project `Manga Agile Board` へ初期登録するIssue候補を整理するための一覧である。

仕様、設計、受入条件の正本は `doc/` 配下に置き、Project上ではIssueの状態、優先順位、見積もり、スプリント投入状況を管理する。

## 前提

- GitHub Projectsのフィールド定義は [GitHub Projects運用マニュアル](05_github_projects_manual.md) を正とする。
- MVPの優先順位とスプリント見通しは [プロダクトバックログ初期版](02_product_backlog.md)、[リリース計画](03_release_plan.md)、[スプリント計画初期版](04_sprint_plan.md) を正とする。
- `Ready = Ready` は「起票済み」ではなく「スプリント投入可能」を意味する。
- `Ready = NotReady` のIssueは、Ready条件を満たしてからスプリントへ入れる。
- `Status` はスプリント中の作業状態である。未投入または将来スプリント予定のIssueでは `-` とし、スプリント投入時に `Todo` へ変更する。
- Issue本文は [GitHub Projects運用マニュアル](05_github_projects_manual.md#issue本文テンプレート) のテンプレートで作成する。

## 初期登録方針

1. まず補助Issueを作成し、Project運用と実装前必須の判断を見える化する。
2. MVP PBIはすべてGitHub Issueとして作成し、Projectへ追加する。
3. Beta / v1.0候補は `Ready = Draft`、`Sprint = Backlog` として起票し、MVP実装中に詳細化する。
4. 大きいIssueは、スプリント投入前に「利用者価値」または「技術リスク」を確認できる縦切りへ分割する。

## Project初期セットアップIssue

| ID | Issueタイトル | System | Priority | Ready | Size | Sprint | Status | Labels | Ready条件 / 備考 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| OPS-001 | GitHub Projectのフィールド、ビュー、ラベルを初期設定する | Documentation | P0: Critical | Ready | 2 | S0: 実装基盤と開発運用 | Todo | `type:docs`, `area:infra` | [GitHub Projects運用マニュアル](05_github_projects_manual.md) の初期セットアップチェックリストを完了する。 |

## 実装前必須の補助Issue

| ID | Issueタイトル | System | Priority | Ready | Size | Sprint | Status | Labels | Ready条件 / 備考 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| SPIKE-001 | 認証トークンとセッションの実装詳細を確定する | Cross-cutting | P0: Critical | NotReady | 3 | S1: 一般ユーザ認証 | - | `type:spike`, `area:security`, `risk:high` | PBI-002、PBI-003、PBI-004の着手前に、ハッシュ方式、pepper管理、期限、試行回数、期限切れレコード削除方針を実装観点で確定する。 |
| SPIKE-002 | 画像変換リソース上限と設定名を確定する | Conversion Worker | P0: Critical | NotReady | 3 | S4: zip変換縦切り | - | `type:spike`, `area:worker`, `risk:high` | PBI-010の着手前に、アップロード、展開、画像デコード、一時領域、同時実行数、タイムアウトのプロパティ名と上限超過時のジョブ状態を確定する。 |
| SPIKE-003 | Elasticsearch必須プラグイン確認と起動失敗方針を確定する | Search | P0: Critical | NotReady | 2 | S6: メタ情報と検索 | - | `type:spike`, `area:search`, `area:infra`, `risk:high` | PBI-014の着手前に、`analysis-kuromoji`、`analysis-icu` の導入確認、未導入時の失敗方針、Docker ComposeとRunbookへの反映範囲を確定する。 |

## MVP Issue一覧

| ID | Issueタイトル | System | Priority | Ready | Size | Sprint | Status | Labels | 関連PBI | Ready条件 / 備考 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| PBI-001 | 開発者として、フロントエンド、API、ワーカーをローカルで起動できるようにしたい | Infrastructure | P0: Critical | Ready | 5 | S0: 実装基盤と開発運用 | Todo | `type:feature`, `area:infra`, `area:api`, `area:frontend`, `area:worker` | プロジェクト基盤を作る | Project初期セットアップ完了後に着手する。 |
| PBI-002 | 一般ユーザとして、メール確認後に会員登録を完了したい | Cross-cutting | P0: Critical | NotReady | 5 | S1: 一般ユーザ認証 | - | `type:feature`, `area:api`, `area:frontend`, `area:security` | 一般ユーザ登録とメール確認を実装する | SPIKE-001完了後、受入条件とTDD観点をIssue本文へ具体化する。 |
| PBI-003 | 利用者として、メール2段階認証でログインして安全なセッションを取得したい | Cross-cutting | P0: Critical | NotReady | 8 | S1: 一般ユーザ認証 | - | `type:feature`, `area:api`, `area:frontend`, `area:security`, `risk:high` | ログイン、メール2段階認証、セッションを実装する | SPIKE-001完了後、セッション分離とレート制限の受入条件を具体化する。 |
| PBI-004 | 一般ユーザとして、パスワードを忘れたときに再設定したい | Cross-cutting | P1: High | NotReady | 3 | S2: 復旧と管理認可 | - | `type:feature`, `area:api`, `area:frontend`, `area:security` | パスワードリセットを実装する | SPIKE-001完了後、既存セッション失効の扱いを確認する。 |
| PBI-005 | 管理者として、初期super_adminと固定ロールで管理操作を制御したい | Cross-cutting | P0: Critical | NotReady | 5 | S2: 復旧と管理認可 | - | `type:feature`, `area:api`, `area:security`, `risk:high` | 初期管理ユーザと固定ロール認可を実装する | 初期super_admin作成手順と最後のsuper_admin保護をIssue本文へ具体化する。 |
| PBI-006 | 開発者として、書籍メタ情報のドメインモデルを扱えるようにしたい | Backend API | P1: High | NotReady | 3 | S3: アップロードとジョブ作成 | - | `type:feature`, `area:api`, `area:db` | 書籍メタ情報のドメインモデルを作る | データモデル参照と最小マイグレーション範囲を確認する。 |
| PBI-007 | 開発者として、RabbitMQで変換ジョブを安全に配送したい | Infrastructure | P0: Critical | NotReady | 5 | S3: アップロードとジョブ作成 | - | `type:feature`, `area:infra`, `area:worker`, `area:db`, `risk:high` | RabbitMQ基盤と変換ジョブ配送を実装する | ack、再配送、DLQ、冪等性の確認方法をIssue本文へ具体化する。 |
| PBI-008 | 管理ユーザとして、自炊本アーカイブをアップロードしたい | Backend API | P1: High | NotReady | 5 | S3: アップロードとジョブ作成 | - | `type:feature`, `area:api`, `area:security` | 管理ユーザがアーカイブをアップロードできる | アップロード受入条件と権限境界をIssue本文へ具体化する。 |
| PBI-009 | 管理ユーザとして、再変換できるよう原本ファイルを保存したい | Backend API | P1: High | NotReady | 3 | S3: アップロードとジョブ作成 | - | `type:feature`, `area:api`, `area:infra` | 原本ファイル保存を実装する | 保存パス検証、失敗時ロールバック、未参照ファイル検出方針を確認する。 |
| PBI-010 | 管理ユーザとして、zipアップロードからWebP変換完了まで確認したい | Conversion Worker | P0: Critical | NotReady | 8 | S4: zip変換縦切り | - | `type:feature`, `area:worker`, `area:security`, `risk:high` | zipのみでアップロードからWebP変換までの縦切りを通す | SPIKE-002完了後、リソース上限と失敗状態をIssue本文へ反映する。 |
| PBI-011 | 管理ユーザとして、rarと7zipのアーカイブも変換したい | Conversion Worker | P1: High | NotReady | 5 | S5: 7-Zipと変換状態 | - | `type:feature`, `area:worker`, `area:security`, `risk:high` | 7-Zip外部プロセスでrar / 7zipを展開できる | 7-Zip引数、終了コード、タイムアウト、ログ安全性を具体化する。 |
| PBI-012 | 管理ユーザとして、サムネイルと変換ジョブ状態を確認したい | Conversion Worker | P1: High | NotReady | 5 | S5: 7-Zipと変換状態 | - | `type:feature`, `area:worker`, `area:api` | サムネイル生成と変換ジョブ状態確認を実装する | 状態遷移、失敗理由、権限制御をIssue本文へ具体化する。 |
| PBI-013 | 管理ユーザとして、書籍メタ情報を編集したい | Backend API | P1: High | NotReady | 5 | S6: メタ情報と検索 | - | `type:feature`, `area:api`, `area:db`, `area:security` | 書籍メタ情報を編集できる | 入力検証、タグ重複、シリーズ順序、認可をIssue本文へ具体化する。 |
| PBI-014 | 開発者として、PostgreSQLから再構築可能な検索インデックスを作りたい | Search | P0: Critical | NotReady | 8 | S6: メタ情報と検索 | - | `type:feature`, `area:search`, `area:db`, `risk:high` | Elasticsearchインデックスと検索Outboxを作成する | SPIKE-003完了後、必須プラグイン確認と再構築手順を反映する。 |
| PBI-015 | 一般ユーザとして、タイトル、著者、タグ、シリーズで本を検索したい | Search | P1: High | NotReady | 5 | S6: メタ情報と検索 | - | `type:feature`, `area:search`, `area:frontend` | タイトル、著者、タグ、シリーズで検索できる | PBI-014のインデックス設計に合わせて検索受入条件を具体化する。 |
| PBI-016 | 一般ユーザとして、本一覧と詳細から読みたい本を見つけたい | Frontend | P1: High | NotReady | 5 | S7: 一覧とビューア | - | `type:feature`, `area:frontend`, `area:api` | 本一覧と詳細を表示できる | 閲覧可能状態、ページング、サムネイル有無をIssue本文へ具体化する。 |
| PBI-017 | 一般ユーザとして、ビューアでページ表示とページ送りをしたい | Frontend | P1: High | NotReady | 5 | S7: 一覧とビューア | - | `type:feature`, `area:frontend`, `area:api` | ビューアでページ表示とページ送りができる | ページ範囲、未変換本、認可、スマートフォン確認観点を具体化する。 |
| PBI-018 | 一般ユーザとして、読みかけ位置から再開したい | Backend API | P2: Medium | NotReady | 3 | S7: 一覧とビューア | - | `type:feature`, `area:api`, `area:frontend` | 読みかけ位置を保存できる | ユーザ別保存と範囲外ページの扱いをIssue本文へ具体化する。 |
| PBI-019 | 一般ユーザとして、お気に入り登録、解除、一覧表示をしたい | Backend API | P2: Medium | NotReady | 3 | S8: お気に入りとMVP統合 | - | `type:feature`, `area:api`, `area:frontend` | お気に入り登録、解除、一覧表示ができる | お気に入り受入条件と閲覧不可本の扱いをIssue本文へ具体化する。 |
| PBI-020 | 運用者として、本番相当Docker Composeで主要コンポーネントを確認したい | Infrastructure | P1: High | NotReady | 5 | S8: お気に入りとMVP統合 | - | `type:feature`, `area:infra`, `risk:high` | 本番相当Docker Compose統合を確認する | 永続ボリューム、設定差し替え、必須プラグイン確認をIssue本文へ具体化する。 |
| PBI-021 | 開発者として、MVP主要フローのE2Eで回帰を検知したい | Cross-cutting | P1: High | NotReady | 5 | S8: お気に入りとMVP統合 | - | `type:test`, `area:frontend`, `area:api`, `area:worker` | MVP主要フローのE2Eを作る | MVP受入シナリオと権限不足確認をIssue本文へ具体化する。 |

## Beta / v1.0候補Issue一覧

| ID | Issueタイトル | System | Priority | Ready | Size | Sprint | Status | Labels | 関連PBI | Ready条件 / 備考 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| PBI-101 | 管理ユーザとして、失敗した変換ジョブを再実行したい | Conversion Worker | P2: Medium | Draft | Unknown | Backlog | - | `type:feature`, `area:worker` | 変換ジョブの再実行を実装する | 再実行可能状態、生成物扱い、権限制御をリファインメントで具体化する。 |
| PBI-102 | 管理ユーザとして、変換ジョブをキャンセルしたい | Conversion Worker | P2: Medium | Draft | Unknown | Backlog | - | `type:feature`, `area:worker` | 変換ジョブのキャンセルを実装する | 外部プロセス停止、競合、canceled状態をリファインメントで具体化する。 |
| PBI-103 | 一般ユーザとして、検索結果を絞り込み、並び替えたい | Search | P2: Medium | Draft | Unknown | Backlog | - | `type:feature`, `area:search`, `area:frontend` | 検索の絞り込みと並び替えを実装する | 種別、複数条件、ソート、空結果、ページングをリファインメントで具体化する。 |
| PBI-104 | 一般ユーザとして、ビューアで見開き、ページ指定、拡大縮小を使いたい | Frontend | P2: Medium | Draft | Unknown | Backlog | - | `type:feature`, `area:frontend` | ビューア操作を強化する | 見開き、ページ番号指定、拡大縮小、入力操作をリファインメントで具体化する。 |
| PBI-105 | 一般ユーザとして、プロフィールを編集したい | Backend API | P3: Low | Draft | Unknown | Backlog | - | `type:feature`, `area:api`, `area:frontend`, `area:security` | ユーザプロフィール編集を実装する | 入力検証、重複、認可、メール変更有無をリファインメントで具体化する。 |
| PBI-106 | 管理ユーザとして、管理ユーザ管理とロール管理を強化したい | Cross-cutting | P2: Medium | Draft | Unknown | Backlog | - | `type:feature`, `area:api`, `area:frontend`, `area:security` | 管理ユーザ管理とロール管理を強化する | ロール別権限、自己削除防止、監査観点をリファインメントで具体化する。 |
| PBI-107 | 運用者として、監視と障害記録を整えたい | Infrastructure | P3: Low | Draft | Unknown | Backlog | - | `type:feature`, `area:infra`, `type:docs` | 監視と障害記録を整える | ログ、メトリクス、障害テンプレート、通知有無をリファインメントで具体化する。 |
| PBI-108 | 一般ユーザとして、自分のアカウントを退会したい | Cross-cutting | P2: Medium | Draft | Unknown | Backlog | - | `type:feature`, `area:api`, `area:frontend`, `area:security` | 退会を実装する | 本人確認、withdrawn状態、セッション失効、個人機能データの扱いを具体化する。 |
| PBI-109 | 管理ユーザとして、不要になった本を削除したい | Backend API | P2: Medium | Draft | Unknown | Backlog | - | `type:feature`, `area:api`, `area:db`, `area:search`, `area:security` | 本削除を実装する | 論理削除、検索除外、派生ファイル整理余地、整合性確認を具体化する。 |
| PBI-110 | 管理ユーザとして、変換失敗理由、変換状態絞り込み、ページ順を確認したい | Conversion Worker | P2: Medium | Draft | Unknown | Backlog | - | `type:feature`, `area:worker`, `area:api`, `area:frontend` | 変換運用確認を強化する | PBI-012、PBI-101、PBI-102との分割方針を確認する。 |
| PBI-111 | 管理ユーザとして、著者、タグ、シリーズ、種別のマスタ管理をしたい | Backend API | P3: Low | Draft | Unknown | Backlog | - | `type:feature`, `area:api`, `area:frontend`, `area:db` | 著者、タグ、シリーズ、種別のマスタ管理を実装する | PBI-013との境界、表記揺れ統合、参照中データ保護を具体化する。 |
| PBI-112 | 管理ユーザとして、一般ユーザの一覧確認と停止を行いたい | Cross-cutting | P2: Medium | Draft | Unknown | Backlog | - | `type:feature`, `area:api`, `area:frontend`, `area:security` | 一般ユーザ管理を強化する | PBI-106との境界、停止時のセッション失効、監査観点を具体化する。 |

## 依存関係メモ

| 先行Issue | 後続Issue | 理由 |
| --- | --- | --- |
| OPS-001 | 全Issue | Projectフィールド、ラベル、ビューがないと運用状態を一貫して管理できない。 |
| SPIKE-001 | PBI-002, PBI-003, PBI-004 | 認証トークン、チャレンジ、セッションの保存方式と期限管理が未確定だと手戻りが大きい。 |
| PBI-001 | PBI-002以降 | フロントエンド、API、ワーカー、DB、ミドルウェア、メール境界の最小基盤が前提になる。 |
| PBI-005 | PBI-008, PBI-012, PBI-013 | 管理操作は固定ロール認可の境界が必要になる。 |
| PBI-006 | PBI-008, PBI-013, PBI-014, PBI-016 | 書籍メタ情報モデルがアップロード、編集、検索、一覧の土台になる。 |
| PBI-007 | PBI-010, PBI-011, PBI-012 | 変換処理はRabbitMQ配送とPostgreSQL上のジョブ状態を前提にする。 |
| PBI-008, PBI-009 | PBI-010 | 変換はアップロード済み原本ファイルとジョブ作成を前提にする。 |
| SPIKE-002 | PBI-010, PBI-011, PBI-012 | 変換リソース上限と失敗時ジョブ状態が変換処理全体の受入条件になる。 |
| SPIKE-003 | PBI-014, PBI-015, PBI-020 | Elasticsearchプラグイン前提は検索インデックス、検索API、Docker Compose統合に影響する。 |
| PBI-014 | PBI-015 | 基本検索は検索インデックスとOutboxが前提になる。 |
| PBI-016 | PBI-017, PBI-018, PBI-019 | 一覧と詳細からビューア、読みかけ、お気に入りの導線へつながる。 |
| PBI-002, PBI-003, PBI-005, PBI-008, PBI-010, PBI-014, PBI-017 | PBI-021 | MVP主要E2Eは認証、認可、アップロード、変換、検索、閲覧が通ってから成立する。 |
| PBI-013, PBI-014 | PBI-109, PBI-111 | 本削除とマスタ管理は書籍メタ情報、公開状態、検索インデックス更新の土台が前提になる。 |
| PBI-003, PBI-019 | PBI-108 | 退会は認証済み一般ユーザ、セッション失効、お気に入りや閲覧履歴の扱いが前提になる。 |
| PBI-005 | PBI-106, PBI-112 | 管理ユーザ管理、一般ユーザ停止、ロール管理は固定ロール認可の境界が前提になる。 |

## `02_product_backlog.md`との突合結果

| 区分 | 結果 | 備考 |
| --- | --- | --- |
| MVP PBI | 一致 | `02_product_backlog.md` の PBI-001〜PBI-021 をすべてIssue一覧へ反映済み。IssueタイトルはGitHub向けにユーザーストーリー寄りへ言い換えているが、`関連PBI` で正本PBI名を保持する。 |
| Beta / v1.0候補PBI | 一致 | `02_product_backlog.md` の PBI-101〜PBI-112 をすべてIssue一覧へ反映済み。 |
| PBI番号の追加 | 一致 | 追加候補は `02_product_backlog.md` へ PBI-108〜PBI-112 として正式追加し、Issue一覧も同じIDへ振り直した。 |
| 補助Issue | 意図的な追加 | `OPS-001`、`SPIKE-001`〜`SPIKE-003` はProject運用と実装前必須の未決事項を扱うため、PBIとは別枠で追加した。 |

## カバレッジ確認メモ

| 観点 | 確認結果 | 対応Issue |
| --- | --- | --- |
| MVP主導線: 登録、アップロード、変換、検索、閲覧 | カバー済み。MVP PBI-001からPBI-021に対応する。 | PBI-001〜PBI-021 |
| 実装前必須の未決事項 | 認証トークン、変換リソース上限、Elasticsearch必須プラグイン確認を補助Issue化済み。 | SPIKE-001〜SPIKE-003 |
| 会員情報編集、退会 | 会員情報編集と退会をPBI化済み。 | PBI-105, PBI-108 |
| 本削除 | 受入条件とユーザーストーリーに基づきPBI化済み。 | PBI-109 |
| 変換運用改善 | 再実行、キャンセル、失敗理由、状態絞り込み、ページ順確認をPBI化済み。 | PBI-101, PBI-102, PBI-110 |
| 検索改善 | 絞り込みと並び替えはPBI化済み。種別や複数条件の詳細はPBI-103のリファインメントで確認する。 | PBI-103 |
| 管理・マスタ管理 | 管理ユーザ管理、ロール管理、一般ユーザ停止、著者/タグ/シリーズ/種別マスタ管理をPBI化済み。 | PBI-106, PBI-111, PBI-112 |
| 監視、障害記録、運用調整 | Beta以降または運用で確定する項目として候補Issueに含める。 | PBI-107 |

## Issue本文作成時の最小チェック

各IssueをGitHubへ起票するときは、少なくとも次を本文に含める。

- User Story: 誰が、何をしたいか、なぜ必要か。
- 背景: MVP、Beta、v1.0のどの価値に寄与するか。
- 受け入れ条件: 正常系と主要な異常系をチェックリスト化する。
- 対象外: MVP外、Beta以降、別Issueへ逃がすものを明記する。
- TDD観点: 最初に書くテスト、単体、結合、E2Eまたは手動確認のどれで確認するか。
- 関連ドキュメント: `doc/` と `development/` の正本リンクを貼る。
- Ready条件 / 備考: 未確定事項、依存Issue、分割方針を残す。

## 関連ドキュメント

- [プロダクトバックログ初期版](02_product_backlog.md)
- [リリース計画](03_release_plan.md)
- [スプリント計画初期版](04_sprint_plan.md)
- [GitHub Projects運用マニュアル](05_github_projects_manual.md)
- [TODO](../../doc/TODO.md)
- [プロダクトロードマップ](../../doc/01_product/02_product_roadmap.md)
- [ユーザーストーリーマップ](../../doc/01_product/04_user_story_map.md)
- [データモデル](../../doc/04_design/04_data_model.md)
- [画像変換設計](../../doc/04_design/07_image_conversion_design.md)
- [検索設計](../../doc/04_design/05_search_design/01_search_design.md)
- [権限設計](../../doc/04_design/08_authorization_design/01_authorization_design.md)
