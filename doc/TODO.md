# アジャイル開発ドキュメント整備 ToDo

## 前提

- 対象システム: 自炊本閲覧Webアプリケーション
- 開発方式: アジャイル開発
- アーキテクチャ方針: パフォーマンスを意識したモジュラーモノリス構成
- ドキュメント方針: 開発前にすべてを固定するのではなく、プロダクト、バックログ、設計判断、運用知識を継続的に更新する
- 想定規模: 小から中規模のWebアプリケーション

作成済みドキュメントの一覧は [doc/README.md](README.md) を参照する。

## ToDo管理ルール

- `[ ]`: 未着手
- `[~]`: 作業中
- `[x]`: 完了
- 各ToDoは、完了時に関連ドキュメントへ反映する
- 完了済みのToDoは、内容が関連ドキュメントへ反映されていることを確認してからこのファイルから削除する
- 仕様判断が必要なものはADRまたは設計ドキュメントに理由を残す
- 実装後に仕様が変わった場合は、該当するユーザーストーリー、受入条件、設計メモ、Runbookを更新する

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
| Passkey / WebAuthn認証とセッションの実装詳細 | RP ID、origin、user verification要件、attestation扱い、対応ブラウザ前提、WebAuthnチャレンジ、credential保存項目、セッションIDのハッシュ方式、pepper、鍵管理、期限、試行回数、再送制限、期限切れレコード削除方針。 | [ADR-0014](03_architecture/03_adr/15_ADR-0014-use-passkey-webauthn-and-server-side-sessions.md)、[データモデル](04_design/04_data_model.md)、[認可設計](04_design/08_authorization_design/01_authorization_design.md)、[アカウントAPI契約](04_design/03_api_contracts/06_account_api.md)、[セキュリティルール](../rules/SECURITY.md)、[Issue #89作業メモ](../development/scrum/sprints/sprint-s1/issue-89-passkey-webauthn-auth-session-spike.md) |
| Elasticsearch必須プラグインのアプリ側検証 | Sprint S0でDocker Composeの `elasticsearch` カスタムイメージに `analysis-kuromoji` と `analysis-icu` を導入し、プラグイン表示まで確認済み。検索インデックス作成時に、未導入時の扱い、起動時またはインデックス作成前チェック、Runbook復旧手順を実装に合わせて確定する。 | [技術スタック](03_architecture/02_technology_stack.md#elasticsearch必須プラグイン)、[Elasticsearchインデックス設計](04_design/05_search_design/02_search_index_design.md#必須プラグイン確認)、[Runbook](07_operations/01_runbook.md)、[Sprint S0テスト結果](../development/scrum/sprints/sprint-s0/test-report.md) |

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
| [技術スタック](03_architecture/02_technology_stack.md#今後詳細化する事項) | ADR、設計、環境設定の具体化。 | Sprint S0で主要ADR、実構成、Elasticsearch必須プラグイン導入確認を反映済み。検索実装時のアプリ側検証は実装前必須。 |
| [コンテナ図](03_architecture/05_container_diagram.md#今後詳細化する事項) | Docker Composeサービス名、ネットワーク、監視、ログ、リソース制限。 | Sprint S0でローカルComposeサービス名とElasticsearch必須プラグイン確認を反映済み。監視、ログ、リソース調整は運用で確定。 |
| [データフロー](03_architecture/06_data_flow.md#今後詳細化する事項) | 変換ジョブ状態、検索更新失敗、再試行、整合性回復。 | ジョブ状態は実装前必須。検索更新責務はOutbox方針を反映済み。整合性回復の運用手順は運用で確定。 |
| [品質特性](03_architecture/07_quality_attributes.md#今後詳細化する事項) | 負荷試験、監視、容量不足時の手順。 | 監視基盤とSLOはBeta以降。容量警告しきい値と手順は運用で確定。 |
| [UIフロー](04_design/01_ui_flows.md#後続で詳細化する事項) / [画面メモ](04_design/02_screen_notes.md#後続で詳細化する事項) | ワイヤーフレーム、見開き、拡大縮小、管理ダッシュボード表示。 | UIの後続機能としてBeta以降。 |
| [データモデル](04_design/04_data_model.md#後続設計で詳細化する事項) | 認証秘密情報、WebAuthn credential、権限、保存、検索更新状態。 | 検索更新状態はOutbox方針を反映済み。Passkey / WebAuthn認証とセッション、権限、保存の拡張は対象機能の実装前に確定する。 |
| [ファイル保存設計](04_design/06_file_storage_design.md#後続設計で詳細化する事項) | 物理削除、再変換差し替え、サムネイル命名。 | 削除・再変換の実装前に確定。用途別サムネイル拡張はBeta以降。 |
| [画像変換設計](04_design/07_image_conversion_design.md#後続設計で詳細化する事項) | サムネイル、WebP条件、RabbitMQ、再実行、Runbook、画像処理ライブラリ。 | 変換リソース上限、RabbitMQ、失敗コード、ライブラリは実装前必須。Runbook細部と同時実行調整は運用で確定。 |
| [検索設計](04_design/05_search_design/01_search_design.md#elasticsearchインデックス設計との関係) / [Elasticsearchインデックス設計](04_design/05_search_design/02_search_index_design.md) | インデックス定義、必須プラグイン、更新失敗、再インデックス、補完。 | 検索更新責務はOutbox方針を反映済み。必須プラグイン導入はSprint S0で確認済み。アプリ側検証とインデックス定義は検索実装前に確定。補完や高度な部分一致はBeta以降。 |
| [API契約](04_design/03_api_contracts/) | 各APIの追加契約、エラー、権限、キャッシュ、管理操作。 | MVP対象APIの実装前に確定。サジェスト、見開き、管理拡張はBeta以降。 |
| [受入条件・受入テスト](02_backlog/03_acceptance_criteria/) / [受入テスト](06_testing/02_acceptance_tests/) | 重複アップロード、冪等性、物理削除、詳細E2E。 | 対象機能の実装前に確定。MVP外の復元や詳細E2EはBeta以降。 |
