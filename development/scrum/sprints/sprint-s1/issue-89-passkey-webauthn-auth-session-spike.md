# Issue #89 Passkey / WebAuthn認証とセッションの実装詳細

## User Story

開発者として、Passkey / WebAuthn認証とセッションの実装詳細を確定したい。
それは、会員登録、Passkey登録、Passkeyログイン、アカウント復旧、サーバ側セッションを安全かつ一貫した方式で実装するためである。

## 背景

- #45 はパスワード認証、メール2段階認証、パスワードリセットを前提にしたスパイクであるため取り下げる。
- 認証方式は、一般ユーザと管理ユーザともに Passkey / WebAuthn 方式へ統一する。
- 既存ADR-0008は更新せず、旧ADRとして廃止の旨を記載し、新ADRでPasskey / WebAuthn採用への方針変更を記録する。
- PBI-002、PBI-003、PBI-004置換Issue、PBI-005の着手前に、WebAuthn credential、登録 / 認証チャレンジ、復旧、セッション、失効方針を確定する。

## 関連Issue

- 取り下げ元: #45
- 置換対象: #51
- 後続: #49, #50, #52, #68, アカウント復旧Issue

## 受け入れ条件

- [ ] W3C WebAuthn、FIDO Design Guidelines、passkeys.dev、NIST / OWASPの参照範囲を整理する。
- [ ] RP ID、origin、user verification要件、attestation扱い、対応ブラウザ前提を決める。
- [ ] HTTPS / local開発時のRP ID、origin、設定名、環境変数名を整理する。
- [ ] Passkey登録開始 / 完了、Passkey認証開始 / 完了のチャレンジ保存方式を決める。
- [ ] WebAuthn credentialの保存項目を決める。
- [ ] WebAuthn credential、登録チャレンジ、認証チャレンジに必要なDBマイグレーション候補を整理する。
- [ ] 一般ユーザと管理ユーザのcredential登録、無効化、再登録、停止時失効の扱いを決める。
- [ ] 初期super_adminのcredential登録手順を決める。
- [ ] セッションCookie、セッション期限、失効契機、一般 / 管理分離を決める。
- [ ] Passkeyを使えない場合のアカウント復旧Issueへ反映する受け入れ条件を整理する。
- [ ] 既存ADR-0008の廃止記録と、新ADRの追加方針を整理する。
- [ ] #45の取り下げ理由と後継Issueであることを、`development/scrum/06_github_project_issue_list.md`へ反映する。
- [ ] `development/scrum/sprints/sprint-s1/issue-45-auth-token-session-spike.md`を取り下げ扱いにし、後継スパイク文書を作成する。
- [ ] PBI-002、PBI-003、PBI-004置換Issue、PBI-005、PBI-021へ反映すべき受け入れ条件を整理する。

## 対象外

- 認証機能そのものの実装。
- UI実装。
- 本番メール送信基盤の詳細実装。
- 外部IDプロバイダ連携。

## TDD観点

- 最初に書くテスト: WebAuthn登録 / 認証チャレンジ、期限切れ、使用済み、失効済み、試行回数超過のドメイン / サービス単位テスト観点を整理する。
- 単体テスト: challenge検証、credential状態、origin / RP ID検証、セッション失効判定。
- 結合テスト: DB永続化、credential登録、認証成功時のセッション発行、停止 / 退会時の失効。
- E2E / 手動確認: 登録、メール確認、Passkey登録、Passkeyログイン、ログアウト、復旧導線の主要異常系。

## 参照する標準・ガイドライン

| 区分 | 資料 | #89で確認する観点 |
| --- | --- | --- |
| 標準仕様 | [W3C Web Authentication Level 3](https://www.w3.org/TR/webauthn-3/) | 登録 / 認証ceremony、RP ID、origin、user verification、credential検証、discoverable credential。 |
| 標準仕様 | [FIDO User Authentication Specifications](https://fidoalliance.org/specifications/) | FIDO2 / CTAP / 認証器種別、Passkey / security keyの扱い、将来の認証器制約。 |
| UXガイドライン | [FIDO Alliance Design Guidelines](https://www.passkeycentral.org/design-guidelines/) | Passkey登録 / ログイン画面、識別子優先フロー、Passkey説明、Passkey管理画面、アクセシビリティ。 |
| 実装ガイド | [passkeys.dev](https://passkeys.dev/) | 実装パターン、bootstrap、reauth、related origins、ライブラリ選定。 |
| 実装ガイド | [Google Passkeys developer guide for relying parties](https://developers.google.com/identity/passkeys/developer-guides) | RP ID、challenge、excludeCredentials、credential ID、public key保存、署名検証。 |
| セキュリティガイドライン | [NIST SP 800-63B-4](https://pages.nist.gov/800-63-4/sp800-63b.html) | syncable passkeyの位置づけ、AAL、phishing resistance、復旧、鍵同期時の制約。 |
| セキュリティガイドライン | [OWASP Authentication Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html) / [OWASP Multifactor Authentication Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Multifactor_Authentication_Cheat_Sheet.html) | セッション管理、再認証、アカウント復旧、ログ安全性、レート制限。 |

## 初期調査メモ

- FIDO Design Guidelinesは、Passkey導入UXの中心資料として扱う。Passkey作成をいきなりOS / ブラウザのダイアログへ飛ばさず、アプリ側で事前説明と結果表示を行う。
- 新規登録とログインは、パスワード欄を出さない識別子優先フローを基本候補にする。完全なユーザー名なしログインにするか、メールアドレス入力後にPasskey認証へ進めるかは、このIssueで決める。
- ユーザーにはPasskeyを抽象概念のまま見せず、アカウント設定でPasskeyをカードとして表示し、登録済みcredentialの管理、無効化、復旧導線を用意する。
- Passkeyはdiscoverable credentialとして扱えるため、ユーザー名入力なし認証も候補になる。ただし実装とUXの複雑度が上がるため、初期MVPでは識別子優先フローを候補にする。
- サーバ側は、登録時にcredential IDと公開鍵を保存し、認証時はRP ID、origin、challenge、user presence / user verification、署名、必要に応じてsign countを検証する。
- 一般向けPasskeyは同期可能なcredentialを前提にできるが、AAL3相当の非エクスポート性は初期MVPの前提にしない。
- アカウント復旧は、パスワードリセットの置換として#90で設計する。Passkey紛失、端末移行、Passkey同期不可、credential削除、メールアドレス変更、管理ユーザのcredential喪失を確認対象に含める。

## 正本ドキュメントへの反映対象

| ドキュメント | #89で整理する反映内容 |
| --- | --- |
| `doc/03_architecture/03_adr/09_ADR-0008-use-email-authentication-and-server-side-sessions.md` | 旧ADRとして廃止の旨を記載する。 |
| `doc/03_architecture/03_adr/15_ADR-0014-use-passkey-webauthn-and-server-side-sessions.md` | Passkey / WebAuthn採用とサーバ側セッション継続を新ADRとして記録する。 |
| `doc/04_design/04_data_model.md` | `password_hash`、`login_challenge`、`password_reset_token`を見直し、WebAuthn credential、登録チャレンジ、認証チャレンジを正本モデルへ追加または再定義する。 |
| `doc/04_design/03_api_contracts/06_account_api.md` | Passkey登録開始 / 完了、Passkey認証開始 / 完了、アカウント復旧APIへ変更する。 |
| `doc/04_design/03_api_contracts/07_admin_api.md` | 管理ユーザもPasskey / WebAuthn方式を使う前提へ変更し、初期super_adminのcredential登録手順を決める。 |
| `doc/04_design/08_authorization_design/01_authorization_design.md` | WebAuthn credentialの登録、無効化、再登録、復旧、停止時失効、セッション失効を整理する。 |
| `doc/04_design/01_ui_flows.md` / `doc/04_design/02_screen_notes.md` | パスワード入力とメール2段階認証画面をPasskey登録 / 認証フローへ置換する。 |
| `rules/SECURITY.md` | ログ禁止、失効条件、チャレンジ、credential、セッションの扱いをPasskey前提へ整理する。 |
| `doc/TODO.md` | 認証トークン / セッション未決事項とPBI-004の前提を更新する。 |

## Sprint / PBI / GitHub Issueへの反映対象

| 対象 | #89後に反映する内容 |
| --- | --- |
| `development/scrum/sprints/sprint-s1/planning.md` | S1ゴールと#50説明をPasskeyログインへ変更する。 |
| `development/scrum/sprints/sprint-s1/pbi-002-003-breakdown.md` | PBI-002をメール確認後のPasskey登録、PBI-003をPasskey認証 / セッション発行へ変更する。 |
| `development/scrum/sprints/sprint-s1/test-report.md` | PBI-003のテスト計画をWebAuthnチャレンジ、credential、セッション発行の確認へ変更する。 |
| `development/scrum/02_product_backlog.md` | PBI-003をPasskeyログインへ変更し、PBI-004をアカウント復旧へ置換する。 |
| `development/scrum/03_release_plan.md` | Sprint 1 / Sprint 2 / MVP受入をPasskeyログインとアカウント復旧へ変更する。 |
| `development/scrum/04_sprint_plan.md` | S1 / S2の目的、受入条件、完了条件をPasskey / WebAuthnへ変更する。 |
| `development/scrum/06_github_project_issue_list.md` | SPIKE-004、PBI-003、PBI-004R、依存関係をGitHub Issue本文と合わせる。 |
| `development/tdd/02_test_matrix.md` | PBI-003をPasskeyログイン、PBI-004Rをアカウント復旧のテスト観点へ変更する。 |
| GitHub Issue #49 | メール確認とPasskey登録の順序を具体化する。 |
| GitHub Issue #50 | タイトルと本文をPasskeyログインでセッションを取得するIssueへ変更する。Spring Session JDBCの引き継ぎは維持する。 |
| GitHub Issue #52 | 初期super_adminのPasskey登録手順、credential喪失時の復旧手段を具体化する。 |
| GitHub Issue #68 | MVP認証E2Eを登録、メール確認、Passkey登録、Passkeyログイン、ログアウトへ変更する。 |
| GitHub Issue #90 | #89の結果に合わせて復旧条件、対象外、TDD観点を具体化する。 |

## 作業順

1. #89をGitHub Project上で着手状態にする。
2. WebAuthnのRP ID、origin、user verification要件、attestation扱い、対応ブラウザ前提を決める。
3. HTTPS / local開発時のRP ID、origin、設定名、環境変数名を決める。
4. 登録チャレンジ、認証チャレンジ、credential保存項目、DBマイグレーション候補を決める。
5. 初期super_adminのcredential登録手順、管理ユーザcredential喪失時の分離方針を決める。
6. セッションCookie、セッション期限、失効契機、一般 / 管理分離を決める。
7. ADRと主要設計ドキュメントへ反映する。
8. Sprint / PBI / TDD文書へ反映する。
9. GitHub Issue #49、#50、#52、#68、#90を更新する。
10. #89を完了状態にし、一時メモの扱いを完了記録または削除候補として記録する。

## 関連ドキュメント

- `development/scrum/sprints/sprint-s1/passwordless-auth-impact-temporary.md`
- `development/scrum/sprints/sprint-s1/planning.md`
- `development/scrum/sprints/sprint-s1/pbi-002-003-breakdown.md`
- `development/scrum/06_github_project_issue_list.md`
- `doc/03_architecture/03_adr/09_ADR-0008-use-email-authentication-and-server-side-sessions.md`
- `doc/03_architecture/03_adr/` のPasskey / WebAuthn採用ADR
- `doc/04_design/04_data_model.md`
- `doc/04_design/03_api_contracts/06_account_api.md`
- `doc/04_design/03_api_contracts/07_admin_api.md`
- `doc/04_design/08_authorization_design/01_authorization_design.md`
- `rules/SECURITY.md`

## Ready条件 / 備考

- #45を取り下げた後継スパイクとして扱う。
- このIssue完了後に、PBI-002 / PBI-003 / アカウント復旧Issue / PBI-005 / PBI-021の受け入れ条件とTDD観点を具体化する。

## 開始メモ

- 2026-05-20: #45を取り下げ、Passkey / WebAuthn方式を前提とする後継Issueとして#89を起票した。GitHub Project `Manga Agile Board` では `System=Cross-cutting`、`Priority=P0: Critical`、`Ready=Ready`、`Size=5`、`Sprint=S1: 一般ユーザ認証`、`Status=Todo` として扱う。
- 2026-05-20: `development/scrum/sprints/sprint-s1/passwordless-auth-impact-temporary.md` から参照資料、初期調査メモ、正本ドキュメント / Sprint / PBI / GitHub Issueへの反映対象、作業順を移した。GitHub Project上の#89は`In Progress`へ更新済み。
