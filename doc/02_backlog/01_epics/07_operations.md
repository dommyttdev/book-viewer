# エピック: 運用

## 目的

単一Linuxホスト上のDocker Compose構成で、自炊本閲覧Webアプリケーションを起動、停止、監視、障害対応、再インデックスできるようにする。

このエピックでは、Runbook、バックアップなし方針、監視方針、障害ログ、リリースノート形式を扱う。

## 対象利用者

- 運用者
- 管理ユーザ

運用者は初版ではアプリケーション内ロールではなく、ホストや運用手順にアクセスできる担当者として扱う。

## 範囲

### MVP

- Docker ComposeでNext.js、Spring Boot API、Spring Boot変換ワーカー、PostgreSQL、Elasticsearch、専用キューを起動できる。
- システムの停止、再起動、ログ確認ができる。
- 変換ジョブ失敗時の確認手順を用意する。
- ElasticsearchをPostgreSQLから全件再インデックスする方針を用意する。
- 書籍単位で再インデックスする方針を用意する。
- バックアップを行わない方針、リスク、許容範囲を明記する。

### Beta / v1.0

- API、変換ワーカー、DB、Elasticsearch、ストレージ容量、変換ジョブ滞留、変換失敗数、画像変換処理時間を監視候補として整理する。
- 障害ログの記録形式を用意する。
- リリースノート形式を用意する。
- 既知リスクと運用上の判断を継続的に記録する。

### 対象外

- バックアップ取得、復元の実運用。
- 複数ホスト構成。
- クラウドマネージドサービス利用。
- 自動スケーリング。

## 主な成果物

- `doc/07_operations/01_runbook.md`
- `doc/07_operations/02_backup_restore.md`
- `doc/07_operations/03_monitoring.md`
- `doc/07_operations/05_incident_log_template.md`
- `doc/07_operations/04_release_note_template.md`
- `doc/05_development/03_environment_setup.md`
- `doc/05_development/04_local_development.md`

## 完了の目安

- 単一Linuxホスト上で主要コンポーネントを起動、停止、再起動できる手順がある。
- 変換ジョブ失敗、Elasticsearch不整合、再インデックスの確認手順がRunbookに記録されている。
- バックアップなし運用のリスクと許容範囲が明記されている。
- 監視候補、障害ログ、リリースノート形式が整備されている。

## 関連ドキュメント

- `doc/03_architecture/01_system_overview.md`
- `doc/03_architecture/05_container_diagram.md`
- `doc/03_architecture/07_quality_attributes.md`
- `doc/01_product/02_product_roadmap.md`

