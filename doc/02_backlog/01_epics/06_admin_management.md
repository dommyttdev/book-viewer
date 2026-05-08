# エピック: 管理機能

## 目的

管理ユーザが書籍、変換ジョブ、一般ユーザ、管理ユーザ、ロールを適切な権限で管理できるようにする。

このエピックでは、管理ユーザログイン、管理ユーザ管理、ロール設定、権限マトリクス、管理画面上の主要操作を扱う。

## 対象利用者

- 管理ユーザ
- 運用者

## 範囲

### MVP

- 管理ユーザが管理画面へログインできる。
- 管理ユーザに `super_admin`, `admin`, `operator`, `viewer` のロール候補を割り当てられる。
- 管理ユーザのロールに応じて、書籍アップロード、メタ情報編集、変換ジョブ確認、再実行、一般ユーザ参照などの操作可否を制御する。
- 管理ユーザ操作はバックエンドAPI側で権限確認する。
- 一般ユーザが管理APIを実行できない。

### Beta / v1.0

- `super_admin` が管理ユーザを登録、編集、停止できる。
- `super_admin` がロール設定を変更できる。
- `admin` が一般ユーザを閲覧、停止できる。
- `operator` がアップロード、変換ジョブ確認、再実行、再インデックス操作を行える。
- `viewer` は管理画面の参照専用として扱う。
- 権限マトリクスを独立ドキュメントとして整備する。

### 対象外

- 組織やグループ単位のユーザ管理。
- 詳細な監査ログ画面。
- 外部IDプロバイダによる管理者認証。

## 主な成果物

- `doc/02_backlog/02_user_stories/11_admin_login_logout.md`
- `doc/02_backlog/02_user_stories/12_admin_user_management.md`
- `doc/02_backlog/02_user_stories/13_role_management.md`
- `doc/04_design/08_authorization_design/02_permission_matrix.md`
- `doc/04_design/03_api_contracts/07_admin_api.md`
- `doc/04_design/02_screen_notes/01_screen_notes.md` への管理画面メモの反映

## 完了の目安

- 管理ユーザの主要操作がロール別に整理されている。
- 権限マトリクスが、データモデル上の `role`, `permission`, `admin_user_role`, `role_permission` と矛盾しない。
- 管理APIの権限確認単位が明確になっている。
- 一般ユーザと管理ユーザの責務境界が保たれている。

## 関連ドキュメント

- `doc/04_design/08_authorization_design/01_authorization_design.md`
- `doc/04_design/04_data_model/01_data_model.md`
- `doc/01_product/04_user_story_map/01_user_story_map.md`
- `rules/SECURITY.md`

