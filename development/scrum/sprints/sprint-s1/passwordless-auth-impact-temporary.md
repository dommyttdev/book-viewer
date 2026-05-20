# パスワードレス認証変更の影響範囲メモ

## 位置づけ

この文書は、Issue #45 の取り下げと、後継Issue #89 の起票に伴う一時調査メモである。

現状の設計とGitHub Issueは、メールアドレス、パスワード、ログイン時メール2段階認証、パスワードリセットを前提にしている。今後の方針としてパスワードレス認証へ変更する場合、正本ドキュメントとGitHub Issueの更新範囲を先に整理する。

この文書は恒久的な設計正本ではない。方式決定後は、ADR、データモデル、API契約、認可設計、Sprint / PBI文書へ反映し、このメモは完了記録または削除候補として扱う。

## 前提

- 調査日: 2026-05-20
- 対象リポジトリ: `dommyttdev/book-viewer`
- 取り下げ元Issue: #45 認証トークンとセッションの実装詳細を確定する
- 後継Issue: #89 Passkey / WebAuthn認証とセッションの実装詳細を確定する
- 変更方針: パスワード認証 + メール2段階認証から、パスワードレス認証へ変更する。
- パスワードレス方式: Passkey / WebAuthn方式を採用する。

決定済み事項:

- 一般ユーザと管理ユーザの認証方式は、Passkey / WebAuthn方式に統一する。
- #51「一般ユーザとして、パスワードを忘れたときに再設定したい」は廃止し、アカウント復旧Issueを新たに起票する。
- 既存ADR-0008は更新しない。新ADRでPasskey / WebAuthn採用への方針変更を記録し、旧ADRには廃止の旨を記載する。

採用しない方式:

- メールマジックリンク方式
- メールワンタイムコード方式

以降の調査では、WebAuthnの登録チャレンジ、認証チャレンジ、credential公開鍵、credential ID、sign count、RP ID、origin、user verification要件、既存セッション発行、復旧手段を確認対象にする。

## 参考にする標準・ガイドライン

Passkey / WebAuthn方式の設計では、次の標準・ガイドラインを優先して参照する。

| 区分 | 資料 | 位置づけ | このプロジェクトで見る観点 |
| --- | --- | --- | --- |
| 標準仕様 | [W3C Web Authentication Level 3](https://www.w3.org/TR/webauthn-3/) | WebAuthn APIの標準仕様。公開鍵credential、RP、authenticator、user verification、discoverable credentialなどの正本。 | API契約、登録/認証チャレンジ、RP ID、origin、user verification、credential検証。 |
| 標準仕様 | [FIDO User Authentication Specifications](https://fidoalliance.org/specifications/) | FIDO2 / CTAP / 認証器まわりの仕様入口。WebAuthnと合わせてFIDO認証の技術前提になる。 | 認証器種別、Passkey / security keyの扱い、将来の認証器制約。 |
| UXガイドライン | [FIDO Alliance Design Guidelines](https://www.passkeycentral.org/design-guidelines/) | FIDO Alliance UX Working GroupによるPasskey導入UXガイドライン。consumer向けPasskey UXの主要な業界標準。 | 登録/ログイン画面、識別子優先フロー、Passkey説明、Passkey管理画面、アクセシビリティ。 |
| 実装ガイド | [passkeys.dev](https://passkeys.dev/) | W3C WebAuthn Community Adoption GroupとFIDO Allianceメンバーが管理する開発者向けリソース。 | 実装時の設計パターン、bootstrap、reauth、related origins、ライブラリ選定。 |
| 実装ガイド | [Google Passkeys developer guide for relying parties](https://developers.google.com/identity/passkeys/developer-guides) | RP実装の実務ガイド。登録/認証に必要なパラメータとサーバ側検証観点が整理されている。 | RP ID、challenge、excludeCredentials、credential ID、public key保存、署名検証。 |
| セキュリティガイドライン | [NIST SP 800-63B-4](https://pages.nist.gov/800-63-4/sp800-63b.html) | 米国NISTの認証・ライフサイクル管理ガイドライン。syncable authenticatorsを含む。 | syncable passkeyの位置づけ、AAL、phishing resistance、復旧、鍵同期時の制約。 |
| セキュリティガイドライン | [OWASP Authentication Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html) / [OWASP Multifactor Authentication Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Multifactor_Authentication_Cheat_Sheet.html) | Webアプリ認証全般の実装安全性ガイド。Passkey固有ではないが、セッション、再認証、アカウント復旧の一般原則に使う。 | セッション管理、再認証、アカウント復旧、ログ安全性、レート制限。 |
| 参考記事 | [パスキー認証のUXガイドライン by FIDO を読む](https://zenn.dev/geb/articles/230916_passkey_ux_fido_guideline) | FIDO UXガイドラインの日本語要約記事。一次情報ではないが、論点の把握に有用。 | FIDO UXガイドラインを読む前の要点整理。最終判断はFIDO公式を参照する。 |

調査メモ:

- FIDO Design Guidelinesは、Passkey導入UXの事実上の中心資料として扱う。パスキー作成をいきなりOS/ブラウザのダイアログに飛ばさず、アプリ側で事前説明と結果表示を行う。
- 新規登録とログインは、パスワード欄を出さない識別子優先フローを基本候補にする。ただし、完全なユーザー名なしログインにするか、メールアドレス入力後にPasskey認証へ進めるかは、#89で決める。
- ユーザーにはPasskeyを抽象概念のまま見せず、アカウント設定でPasskeyをカードとして表示し、登録済みcredentialの管理、無効化、復旧導線を用意する。
- W3C WebAuthn Level 3では、Passkeyはdiscoverable credentialとして扱える。ユーザー名入力なしの認証も可能だが、実装とUXの複雑度が上がるため、初期MVPでは識別子優先フローを候補にする。
- サーバ側は、登録時にcredential IDと公開鍵を保存し、認証時はRP ID、origin、challenge、user presence / user verification、署名、必要に応じてsign countを検証する。
- NIST SP 800-63B-4では、syncable authenticatorsの扱いが整理されている。一般向けPasskeyは同期可能なcredentialを前提にできるが、AAL3相当の非エクスポート性は前提にしない。
- アカウント復旧は、パスワードリセットの置換として別Issueで設計する。Passkey紛失、端末移行、メールアドレス変更、管理ユーザのcredential喪失を対象に含める。

## 主要な正本ドキュメントへの影響

| ドキュメント | 影響箇所 | 対応方針 |
| --- | --- | --- |
| `doc/03_architecture/03_adr/09_ADR-0008-use-email-authentication-and-server-side-sessions.md` | 初期認証方式、パスワード認証成功後のメール2段階認証、パスワードリセット | 旧ADRとして廃止の旨を記載する。Passkey / WebAuthn採用は新ADRへ記録する。 |
| `doc/04_design/04_data_model.md` | `user.password_hash`、`admin_user.password_hash`、`login_challenge`、`password_reset_token`、WebAuthn credential | パスワード保存列とリセットトークンの扱いを見直す。WebAuthn credential、登録チャレンジ、認証チャレンジの正本テーブルを追加または再定義する。 |
| `doc/04_design/03_api_contracts/06_account_api.md` | 登録、ログイン、ログイン確認、パスワードリセット、退会確認 | パスワード入力を削除し、Passkey登録開始/完了、Passkey認証開始/完了APIへ変更する。パスワードリセットAPIは廃止し、アカウント復旧APIを別途設計する。 |
| `doc/04_design/03_api_contracts/07_admin_api.md` | 管理ログイン | 管理ユーザもPasskey / WebAuthn方式を使う前提へ変更する。初期super_adminのcredential登録手順を別途決める。 |
| `doc/04_design/08_authorization_design/01_authorization_design.md` | 未ログイン利用者の操作、認証方式、失効条件、ログ禁止項目 | パスワードリセット、パスワード変更、2段階認証の記述を見直す。WebAuthn credentialの登録、無効化、再登録、復旧、停止時失効を整理する。 |
| `doc/04_design/01_ui_flows.md` | 登録、ログイン、管理ログイン、パスワードリセット導線 | パスワード入力と2段階認証画面を、Passkey登録/認証フローへ置換する。 |
| `doc/04_design/02_screen_notes.md` | ログイン画面、2段階認証コード、パスワードリセット導線 | 画面項目と状態をPasskey登録/認証前提へ変更する。 |
| `rules/SECURITY.md` | トークン、コード、パスワード、セッション失効、WebAuthn credential | パスワード関連のログ禁止と失効条件を、WebAuthnチャレンジ、credential、セッション中心へ整理する。 |
| `doc/TODO.md` | 認証トークン / セッション未決事項 | PBI-004やパスワードリセット前提を変更する。 |

## Sprint / PBI文書への影響

| ドキュメント | 影響箇所 | 対応方針 |
| --- | --- | --- |
| `development/scrum/sprints/sprint-s1/issue-45-auth-token-session-spike.md` | 目的、対象、確定項目、TDD観点、成果物 | #45を取り下げ扱いにし、取り下げ理由と後継Issue #89を記録する。 |
| `development/scrum/sprints/sprint-s1/issue-89-passkey-webauthn-auth-session-spike.md` | 目的、対象、確定項目、TDD観点、成果物 | #89の作業メモとして、Passkey / WebAuthn認証スパイクの詳細を記録する。 |
| `development/scrum/sprints/sprint-s1/planning.md` | S1ゴール、#50説明、リスク、確認観点 | 「ログイン時メール2段階認証」を「Passkeyログイン」へ変更する。 |
| `development/scrum/sprints/sprint-s1/pbi-002-003-breakdown.md` | PBI-002登録条件、PBI-003ログイン条件、対象外 | 登録時パスワードとパスワード認証成功後チャレンジを削除し、Passkey登録/認証へ置換する。 |
| `development/scrum/sprints/sprint-s1/test-report.md` | PBI-003テスト計画、ログ禁止項目 | パスワードとパスワードリセットトークンを見直し、WebAuthnチャレンジ、credential、セッション発行の確認へ変更する。 |
| `development/scrum/02_product_backlog.md` | PBI-003、PBI-004 | PBI-003をPasskeyログインへ変更する。PBI-004は廃止し、アカウント復旧PBIまたはIssueを新設する。 |
| `development/scrum/03_release_plan.md` | Sprint 1、Sprint 2、MVP受入 | パスワードリセットをMVP/S2から外し、アカウント復旧機能へ置換する。 |
| `development/scrum/04_sprint_plan.md` | S1/S2の目的、受入条件、完了条件 | パスワード認証と2段階認証の前提をPasskey / WebAuthnへ変更する。 |
| `development/scrum/06_github_project_issue_list.md` | SPIKE-001、SPIKE-004、PBI-003、PBI-004、依存関係 | GitHub Issue本文と合わせて更新する。 |
| `development/tdd/02_test_matrix.md` | PBI-003、PBI-004 | Passkeyログインとアカウント復旧のテスト観点へ変更する。 |

## Open Issueへの影響

| Issue | 影響度 | 影響箇所 | 対応方針 |
| --- | --- | --- | --- |
| #45 認証トークンとセッションの実装詳細を確定する | 直接 / 取り下げ | User Story、背景、受入条件、TDD観点、関連PBI | パスワード認証、メール2段階認証、パスワードリセットを前提にしているため取り下げる。後継は#89。 |
| #89 Passkey / WebAuthn認証とセッションの実装詳細を確定する | 直接 / 最大 | User Story、背景、受入条件、TDD観点、関連PBI | Passkey / WebAuthnの登録チャレンジ、認証チャレンジ、credential、セッション、失効条件を確定する後継スパイクとして扱う。 |
| #49 一般ユーザとして、メール確認後に会員登録を完了したい | 直接 | 登録時パスワード入力、メール確認後の状態、Passkey登録 | パスワードを登録しない前提で、メール確認とPasskey登録の順序を具体化する。 |
| #50 利用者として、メール2段階認証でログインして安全なセッションを取得したい | 直接 / 最大 | タイトル、User Story、関連PBI、受入条件、#86引き継ぎ | Passkeyログインでセッションを取得するIssueへ変更する。Spring Session JDBC引き継ぎは維持する。 |
| #51 一般ユーザとして、パスワードを忘れたときに再設定したい | 直接 / 廃止 | Issue全体 | Passkey方式では不要。`not planned`でcloseし、Passkey紛失時のアカウント復旧Issueを新たに起票する。 |
| #52 管理者として、初期super_adminと固定ロールで管理操作を制御したい | 直接 | 初期super_admin作成手順、管理ログイン前提 | 初期super_adminのPasskey登録手順、登録済みcredential喪失時の復旧手段を決める。 |
| #68 開発者として、MVP主要フローのE2Eで回帰を検知したい | 直接 | MVP認証E2Eシナリオ | 登録、メール確認、Passkey登録、Passkeyログイン、ログアウトのE2Eへ変更する。 |
| #73 一般ユーザとして、プロフィールを編集したい | 間接 | パスワード変更導線、メール変更時の再確認 | パスワード変更を対象外にし、メール変更と再認証を整理する。 |
| #74 管理ユーザとして、管理ユーザ管理とロール管理を強化したい | 間接 | 管理ユーザ招待、認証手段、停止時失効 | 管理ユーザ招待、Passkey credential登録/無効化、未使用WebAuthnチャレンジ失効を検討する。 |
| #76 一般ユーザとして、自分のアカウントを退会したい | 間接 | 本人確認、退会時失効対象 | パスワード再入力ではなく、Passkey再認証または現在セッション確認へ変更する。 |
| #80 管理ユーザとして、一般ユーザの一覧確認と停止を行いたい | 間接 | 停止時の認証状態失効 | 停止時に既存セッション、Passkey credential、未使用WebAuthnチャレンジをどう扱うか決める。 |

## 新規起票Issue候補

既存Issueを確認した結果、Passkey / WebAuthn方式におけるアカウント復旧を直接扱うIssueは存在しない。

#51「一般ユーザとして、パスワードを忘れたときに再設定したい」は、パスワードリセット専用のIssueであるため流用しない。#51は`not planned`でcloseし、置換Issueとして#90を起票済みである。

### #90 一般ユーザとして、Passkeyを使えないときにアカウントを復旧したい

#### User Story

一般ユーザとして、登録済みPasskeyを使えないときにアカウントを復旧したい。
それは、端末紛失、端末変更、Passkey同期不可、credential削除などが起きても、本人確認を経て安全に利用を再開できるようにするためである。

#### 背景

- PBI-004「パスワードリセットを実装する」の置換Issueとして起票済み。
- Passkey / WebAuthn方式ではパスワードを保持しないため、パスワードリセットではなくアカウント復旧として扱う。
- 復旧フローはアカウント乗っ取りの主要リスクになるため、#89でWebAuthn credential、復旧トークン、セッション失効、レート制限、ログ安全性を整理してから詳細化する。

#### 受け入れ条件

- [ ] Passkeyを使えない利用者が復旧要求を開始できる。
- [ ] 復旧要求の応答は、メールアドレスの登録有無を推測されにくい。
- [ ] 復旧用トークンまたはチャレンジは平文保存せず、期限、使用済み、失効、試行回数、再送制限を持つ。
- [ ] 復旧成功時に新しいPasskey credentialを登録できる。
- [ ] 復旧成功時に既存セッションと未使用WebAuthnチャレンジの扱いが定義されている。
- [ ] 復旧失敗、期限切れ、使用済み、試行回数超過、停止済みユーザを安全に拒否できる。
- [ ] レスポンス、ログ、エラー、監査メモに復旧トークン、credential秘密情報、セッションID、不要な個人情報を出力しない。
- [ ] 管理ユーザのcredential喪失は、このIssueで扱うか別Issueに分離するかを明記する。

#### 対象外

- パスワードリセット。
- パスワードの設定、保存、変更。
- 本人確認手段の外部サービス連携。
- 管理ユーザ専用の緊急復旧手順。ただし、分離方針はこのIssueで確認する。

#### TDD観点

- 最初に書くテスト: 復旧要求が登録有無を推測させない応答を返す。
- 単体テスト: 復旧トークンまたはチャレンジの期限切れ、使用済み、失効済み、試行回数超過判定。
- 結合テスト: 復旧成功時のPasskey credential登録、既存セッション失効、未使用チャレンジ失効。
- E2E / 手動確認: Passkeyを使えない状態から復旧し、新しいPasskeyでログインできる。

#### 関連ドキュメント

- `development/scrum/sprints/sprint-s1/passwordless-auth-impact-temporary.md`
- `development/scrum/sprints/sprint-s1/issue-89-passkey-webauthn-auth-session-spike.md`
- `doc/04_design/03_api_contracts/06_account_api.md`
- `doc/04_design/04_data_model.md`
- `doc/04_design/08_authorization_design/01_authorization_design.md`
- `rules/SECURITY.md`

#### Ready条件 / 備考

- #89完了後、Passkey / WebAuthnのcredential保存、復旧用トークンまたはチャレンジ、レート制限、既存セッション失効の方針をIssue本文へ反映する。

次のIssueは、認証済み主体や権限確認には依存するが、Issue本文上はパスワード方式を直接持たないため、現時点の修正優先度は低い。

- #55 管理ユーザとして、自炊本アーカイブをアップロードしたい
- #57 管理ユーザとして、zipアップロードからWebP変換完了まで確認したい
- #58 管理ユーザとして、rarと7zipのアーカイブも変換したい
- #60 管理ユーザとして、書籍メタ情報を編集したい
- #66 一般ユーザとして、お気に入り登録、解除、一覧表示をしたい
- #67 運用者として、本番相当Docker Composeで主要コンポーネントを確認したい
- #77 管理ユーザとして、不要になった本を削除したい

その他のopen Issueは、今回の認証方式変更による直接修正は不要と判断する。

## Closed Issueへの影響と再open要否

完了済みIssueは、成果物が現在の正本ドキュメントに残っているため影響はある。ただし、過去の作業履歴としては完了済みであり、今回の変更は新しい仕様変更としてopen Issue側で扱うのが適切である。そのため、再openは不要と判断する。

| Issue | 影響箇所 | 再open要否 |
| --- | --- | --- |
| #1 エピック定義を整備する | `05_account_management.md` の2段階認証、パスワードリセット記述 | 不要 |
| #4 データモデル初版を作成する | `password_hash`、`login_challenge`、`password_reset_token` | 不要 |
| #8 権限設計初版を作成する | 認証方式、未ログイン操作、失効条件 | 不要 |
| #9 API設計方針とAPI契約を作成する | `06_account_api.md`、`07_admin_api.md` のログイン/リセットAPI | 不要 |
| #10 UIフローと画面メモを作成する | ログイン画面、2段階認証画面、パスワードリセット導線 | 不要 |
| #17 アカウント管理のユーザーストーリーと認証仕様を作成する | ログイン、2段階認証、パスワードリセット | 不要 |
| #18 管理機能のユーザーストーリーと権限マトリクスを作成する | 管理ログインのパスワード + 2段階認証前提 | 不要 |
| #20 テスト戦略と主要受入テストを作成する | 認証回帰テスト、受入テストのログイン/リセット観点 | 不要 |
| #24 ユーザーストーリーマップを作成する | ログイン、パスワード再設定の表現 | 不要 |
| #25 ロードマップ初版を作成する | MVP/S2のパスワードリセット項目 | 不要 |
| #32 認証とアカウントに関する基本決定事項を記録する | ログイン時2段階認証、パスワードリセット提供の決定 | 不要。新ADRで方針変更を記録する。 |
| #37 認証トークンとセッションの正本データモデルを定義する | `login_challenge`、`password_reset_token`、失効条件、WebAuthn credential未定義 | 不要。ただし#89の主要更新対象にする。 |
| #43 未ADR化の主要設計判断をADRとして整理する | ADR-0008の廃止記録と新ADR追加が必要 | 不要 |
| #48 開発者として、フロントエンド、API、ワーカーをローカルで起動できるようにしたい | #50へのSpring Session JDBC引き継ぎ | 不要。引き継ぎは有効。 |
| #86 APIとWorkerのローカル設定と外部依存の疎通確認を整える | #50へのSpring Session JDBC引き継ぎ | 不要。引き継ぎは有効。 |

## 次に決めること

1. WebAuthnのRP ID、origin、user verification要件、attestation扱い、対応ブラウザ前提を決める。
2. 初期super_adminのcredential登録手順を決める。
3. #90の受入条件と対象外を、#89の結果に合わせて具体化する。
4. ADR-0008へ廃止の旨を追記し、Passkey / WebAuthn採用の新ADRを追加する。
5. #89の作業結果を `development/scrum/sprints/sprint-s1/issue-89-passkey-webauthn-auth-session-spike.md` へ記録する。
6. #49、#50、#51、#52、#68と、新規アカウント復旧Issueの本文を更新する。
