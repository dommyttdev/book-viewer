# エピック: アカウント管理

## 目的

一般ユーザが安全に登録、認証、会員情報管理、退会を行えるようにする。

このエピックでは、一般ユーザの会員登録、メール認証、ログイン、ログアウト、2段階認証、パスワードリセット、会員情報編集、退会を扱う。管理ユーザのロールや管理操作は「管理機能」エピックで扱う。

## 対象利用者

- 未ログイン利用者
- 一般ユーザ

## 範囲

### MVP

- 未ログイン利用者が一般ユーザとして会員登録できる。
- 会員登録時にメール認証を行う。
- メール確認が完了するまで通常利用を許可しない。
- 一般ユーザがログイン、ログアウトできる。
- ログイン時の2段階認証にメールを活用する。
- 一般ユーザがパスワードリセットできる。
- 認証状態に応じて、本一覧、検索、閲覧、お気に入りなどの利用可否を制御する。

### Beta / v1.0

- 一般ユーザが会員情報を編集できる。
- 一般ユーザが退会できる。
- ログイン失敗制限、再送制限、トークン有効期限を具体化する。
- 退会済みユーザのメールアドレス再利用可否を検討する。

### 対象外

- 外部IDプロバイダ連携。
- 端末ごとのログイン状態管理。
- 一般ユーザによる書籍所有やアップロード。

## 主な成果物

- `doc/02_backlog/02_user_stories/07_user_registration.md`
- `doc/02_backlog/02_user_stories/08_user_login_logout.md`
- `doc/02_backlog/02_user_stories/09_user_profile_edit.md`
- `doc/02_backlog/02_user_stories/10_user_withdrawal.md`
- `doc/04_design/08_authorization_design/01_authorization_design.md` への必要な反映
- `doc/04_design/03_api_contracts/06_account_api.md`
- `doc/06_testing/02_acceptance_tests/05_authorization_acceptance_tests.md`

## 完了の目安

- 一般ユーザの登録、メール認証、ログイン、ログアウト、パスワードリセットの仕様がユーザーストーリーとAPI契約に整理されている。
- 2段階認証、ログイン失敗制限、認証エラーの扱いが設計に記録されている。
- 退会時のお気に入り、閲覧履歴、ユーザ状態の扱いが整理されている。
- 一般ユーザが書籍を保持しない前提と矛盾しない。

## 関連ドキュメント

- `doc/04_design/08_authorization_design/01_authorization_design.md`
- `doc/04_design/04_data_model/01_data_model.md`
- `doc/01_product/04_user_story_map/01_user_story_map.md`
- `rules/SECURITY.md`

