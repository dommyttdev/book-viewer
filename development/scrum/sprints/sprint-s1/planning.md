# Sprint S1 プランニング

## スプリント

- Sprint: S1
- 期間: 2026-05-19 から。終了日は実装進捗とレビュータイミングに合わせて確定する。
- スプリントゴール: 一般ユーザが会員登録、メール確認、Passkey登録、Passkeyログインを経て、一般ユーザ用セッションを取得できる最小スライスを作る。

## GitHub Project / Issue

- GitHub Project: Manga Agile Board
- Current Sprint view: `S1: 一般ユーザ認証`
- 最初に着手するIssue: #89 Passkey / WebAuthn認証とセッションの実装詳細を確定する
- 取り下げIssue: #45 認証トークンとセッションの実装詳細を確定する
- 対象PBI:
  - #49 / PBI-002: 一般ユーザとして、メール確認後に会員登録を完了したい
  - #50 / PBI-003: 利用者として、Passkeyでログインして安全なセッションを取得したい

## 対象Issue

| Issue | 内容 | Sprint S1での扱い | Ready | Status |
| --- | --- | --- | --- | --- |
| #89 | Passkey / WebAuthn認証とセッションの実装詳細を確定する | PBI-002 / PBI-003 / PBI-004R / PBI-005 / PBI-021 の実装前必須スパイク。最初に完了させる。 | Ready | Todo |
| #49 | 一般ユーザとして、メール確認後に会員登録を完了したい | #89完了後、メール確認とPasskey登録順序、受入条件、TDD観点を具体化して実装する。 | Ready | Todo |
| #50 | 利用者として、Passkeyでログインして安全なセッションを取得したい | #89と#49の結果を踏まえ、Passkey認証、セッション発行、ログアウトを実装する。 | Ready | Todo |

## 作業順序

| 順序 | Issue | 作業 | 完了条件 |
| --- | --- | --- | --- |
| 1 | #89 | RP ID、origin、user verification、attestation、登録 / 認証チャレンジ、credential、セッション、復旧、初期super_admin登録方式を確定する。 | ADR、データモデル、API契約、認可設計、Sprint / PBI文書へ反映されている。 |
| 2 | #49 | 会員登録、メール確認、Passkey登録のAPI、データモデル、最小UIまたは確認導線を実装する。 | 登録、重複、メール確認、Passkey登録、期限切れ、使用済み、試行回数超過の主要テストが通る。 |
| 3 | #50 | Passkey認証、一般ユーザ用セッション、ログアウトを実装する。 | WebAuthn認証、セッション発行、失効、メール未確認拒否、停止 / 退会済み拒否の主要テストが通る。 |
| 4 | #49 / #50 | ドキュメントとGitHub Projectの状態を更新する。 | API契約、データモデル、権限設計、TODO、Issueチェックリストが実装結果と整合している。 |

## #89で確定する事項

| 分類 | 確定する内容 | 反映先 |
| --- | --- | --- |
| WebAuthn前提 | RP ID、origin、user verification要件、attestation扱い、対応ブラウザ前提。 | `doc/03_architecture/03_adr/15_ADR-0014-use-passkey-webauthn-and-server-side-sessions.md`, `doc/04_design/03_api_contracts/06_account_api.md` |
| local開発設定 | HTTPS / local開発時のRP ID、origin、設定名、環境変数名。 | `doc/05_development/03_environment_setup.md`, `doc/05_development/04_local_development.md` |
| チャレンジ | WebAuthn登録 / 認証チャレンジ、復旧チャレンジの生成、保存、照合方式。 | `doc/04_design/04_data_model.md`, `doc/04_design/03_api_contracts/06_account_api.md` |
| credential | credential ID、公開鍵、sign count、transport、状態、無効化、再登録、停止時失効。 | `doc/04_design/04_data_model.md`, `doc/04_design/08_authorization_design/01_authorization_design.md` |
| セッション | Cookie値、DB正本、Spring Security / Spring Sessionとの境界、一般 / 管理分離。 | `doc/04_design/08_authorization_design/01_authorization_design.md` |
| 復旧 | #90へ反映する受入条件、対象外、TDD観点。 | `development/scrum/sprints/sprint-s1/issue-89-passkey-webauthn-auth-session-spike.md`, GitHub Issue #90 |
| TDD観点 | PBI-002 / PBI-003 / PBI-004R / PBI-005 / PBI-021の最初に書くテスト、単体、結合、手動確認。 | `development/scrum/sprints/sprint-s1/pbi-002-003-breakdown.md`, `development/tdd/02_test_matrix.md` |

## TDD方針

- RedはAPIユースケースまたはドメインサービス単位で、WebAuthnブラウザ連携や実Cookie発行の前に失敗条件を明確にする。
- GreenはPostgreSQLを正本とする最小実装を優先し、ElasticsearchやWorkerには触れない。
- メール送信はメール確認と復旧用の境界に限定し、本番SMTPの詳細はハードコードしない。
- 平文トークン、WebAuthnチャレンジ、credential秘密情報、セッションID、ハッシュ値、pepperはログ、レスポンス、テスト失敗メッセージへ出さない。
- 一般ユーザ用セッションと管理ユーザ用セッションは、`session_type` と入口の責務を分ける。

## リスク

| リスク | 対応 |
| --- | --- |
| #89を後回しにすると、PBI-002 / PBI-003 / PBI-004R / PBI-005でcredential保存や失効条件の手戻りが大きい。 | #89をS1の最初の作業として扱い、設計書へ反映してから実装へ進む。 |
| Spring Session JDBCと独自`session`モデルの責務が混ざる可能性がある。 | #89でCookie値、DB正本、Spring Security / Spring Sessionとの境界を整理する。 |
| WebAuthnのRP ID、origin、local開発設定が曖昧だと、開発環境と本番環境で認証がずれる。 | API契約、環境構築手順、ローカル開発手順に設定名と確認方法を明記する。 |
| アカウント復旧が弱いと、Passkey方式でも乗っ取りリスクが高くなる。 | #90へ復旧トークン、レート制限、既存セッション失効、credential扱いを反映する。 |
| Cookie利用によりCSRF / SameSite / CORS設定が必要になる。 | #50実装前にAPI契約とセキュリティ設計へ確認観点を追加する。 |

## 確認予定

- API単体テスト: 入力検証、メール確認トークン状態、WebAuthnチャレンジ状態、credential状態、セッション状態。
- API結合テスト: PostgreSQL永続化、メール送信境界、Passkey登録、Passkey認証、セッションCookie発行、ログアウト失効。
- フロントエンド確認: 登録、メール確認、Passkey登録、Passkeyログイン、ログアウトの最小導線。
- 手動確認: local profileでログに秘密情報が出ていないこと、開発環境でメール確認とPasskey認証の確認導線を使えること。

## 開始時点の決定

- S1は #89 から開始する。
- #89完了までは #49 / #50 の実装に入らず、受入条件とTDD観点の具体化を優先する。
- S1中に確定した認証仕様は、Issue本文だけでなく `doc/` 配下の設計書を正本として更新する。
