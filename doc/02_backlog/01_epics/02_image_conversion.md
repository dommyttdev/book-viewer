# エピック: 画像変換

## 目的

アップロードされた自炊本アーカイブを非同期に展開し、Web閲覧向けのWebP画像とサムネイルを生成できるようにする。

このエピックでは、変換ワーカー、専用キュー、アーカイブ展開、画像判定、ページ順序、WebP変換、サムネイル生成、変換ジョブ状態、再変換を扱う。

## 対象利用者

- 管理ユーザ
- 運用者

一般ユーザは変換処理を直接操作しないが、変換済みページを閲覧する利用者として影響を受ける。

## 範囲

### MVP

- API処理と重い変換処理を分離し、変換ワーカーで非同期処理する。
- 専用キューを使って変換ジョブを配送する。
- zip / rar / 7zip 形式を扱う。
- rar / 7zip は 7-Zip for Linux コンソール版で展開する。
- ジョブごとの専用作業ディレクトリで展開、変換する。
- ページ画像をWebPへ変換する。
- WebP品質値80を既定とし、application propertiesで変更可能にする。
- サムネイルを生成する。
- `queued`, `extracting`, `converting`, `completed`, `failed`, `canceled` のジョブ状態を扱う。
- 変換失敗時に状態と診断に必要な失敗情報を残す。

### Beta / v1.0

- 失敗した変換ジョブを手動再実行できる。
- 変換中、完了、失敗などの状態で絞り込みできる。
- 変換結果のページ順を確認できる。
- タイムアウト、キャンセル、再実行時の既存生成物の扱いを具体化する。
- 変換ジョブ滞留、失敗数、処理時間を監視対象として扱う。

### 対象外

- 本ごとの変換設定。
- 一部ページのみの再変換。
- OCRや本文抽出。
- 変換ワーカーの水平スケール実装。

## 主な成果物

- `doc/04_design/07_image_conversion_design/01_image_conversion_design.md`
- `doc/04_design/04_data_model/01_data_model.md` への変換ジョブ状態の反映
- `doc/04_design/03_api_contracts/03_conversion_job_api.md`
- `doc/06_testing/02_acceptance_tests/02_image_conversion_acceptance_tests.md`
- `doc/07_operations/01_runbook/01_runbook.md` への変換ジョブ失敗時確認手順の反映

## 完了の目安

- 管理ユーザがアップロードした原本ファイルから、閲覧に必要なWebP画像とサムネイルを生成できる。
- 変換処理の状態が管理画面またはAPIから追跡できる。
- 変換失敗、再実行、タイムアウトの扱いが設計に記録されている。
- 7-Zip外部プロセス呼び出しの入力検証、パストラバーサル対策、ログ方針が整理されている。

## 関連ドキュメント

- `doc/03_architecture/03_adr/07_ADR-0006-extract-archive-files.md`
- `doc/03_architecture/03_adr/08_ADR-0007-use-async-conversion-worker.md`
- `doc/04_design/07_image_conversion_design/01_image_conversion_design.md`
- `doc/04_design/06_file_storage_design/01_file_storage_design.md`
- `doc/03_architecture/06_data_flow/01_data_flow.md`

