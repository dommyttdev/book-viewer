# アカウントAPI契約初版

## 目的

このドキュメントは、一般ユーザのアカウント管理API契約初版を定義する。

対象は、会員登録、メール確認、Passkey登録、Passkeyログイン、ログアウト、会員情報取得、会員情報更新、アカウント復旧、退会である。

## 前提

- 一般ユーザは書籍を保持しない。
- 会員登録時にメール確認を行う。
- 認証方式はPasskey / WebAuthn方式を採用する。
- パスワードは保存しない。
- ログイン時メール2段階認証とパスワードリセットは提供しない。
- Passkeyを使えない場合は、パスワードリセットではなくアカウント復旧として扱う。
- 認証トークン、WebAuthnチャレンジ、credential秘密情報、セッションIDはレスポンスやログに露出しない。

## 会員登録

```http
POST /api/v1/auth/register
```

```json
{
  "email": "user@example.com",
  "displayName": "表示名"
}
```

成功時はメール確認待ち状態を返す。

```json
{
  "userId": "user_...",
  "status": "registered",
  "emailVerificationRequired": true
}
```

メールアドレス形式、表示名、重複を検証する。登録済みメールの扱いは、アカウント存在推測を避ける応答にする。

登録時に送信する確認用の平文`token`はDBに保存しない。PostgreSQLの正本は`email_verification_token.token_hash`、`expires_at`、`attempt_count`、`max_attempts`、`used_at`、`revoked_at`、`send_count`であり、APIで受け取った`token`をハッシュ化して照合する。

## メール確認

```http
POST /api/v1/auth/email-verifications/confirm
```

```json
{
  "token": "..."
}
```

トークンは有効期限、使い捨て、試行回数制限、再送制限を持たせる。確認成功時は`email_verification_token.used_at`を設定し、同一目的の未使用トークンは再利用できない状態にする。

メール確認後は、Passkey登録へ進める状態にする。メール確認完了と同時に自動ログインするか、一時的な登録用セッションを発行するかは#89で確定する。

## Passkey登録

登録開始:

```http
POST /api/v1/auth/passkeys/registration-options
```

```json
{
  "userId": "user_..."
}
```

成功時は、ブラウザの`navigator.credentials.create()`へ渡すWebAuthn登録オプションを返す。

```json
{
  "challengeId": "challenge_...",
  "publicKey": {
    "rp": { "id": "example.com", "name": "Book Viewer" },
    "user": {
      "id": "base64url-user-handle",
      "name": "user@example.com",
      "displayName": "表示名"
    },
    "challenge": "base64url-challenge",
    "pubKeyCredParams": [],
    "authenticatorSelection": {
      "residentKey": "preferred",
      "userVerification": "required"
    },
    "attestation": "none"
  }
}
```

登録完了:

```http
POST /api/v1/auth/passkeys/registration
```

```json
{
  "challengeId": "challenge_...",
  "credential": {
    "id": "...",
    "rawId": "...",
    "type": "public-key",
    "response": {
      "clientDataJSON": "...",
      "attestationObject": "..."
    }
  }
}
```

サーバ側は`webauthn_registration_challenge`を検証し、RP ID、origin、challenge、user verification、attestation、credential ID、公開鍵を確認する。成功時は`webauthn_credential`を作成し、登録チャレンジを使用済みにする。

登録完了時に通常セッションを発行するか、改めてPasskeyログインを要求するかは#89で確定する。

## Passkeyログイン

認証開始:

```http
POST /api/v1/auth/passkeys/authentication-options
```

```json
{
  "email": "user@example.com"
}
```

初期MVPでは、メールアドレス入力後にPasskey認証へ進む識別子優先フローを第一候補とする。完全なユーザー名なしログインを採用するかは#89で確定する。

成功時は、ブラウザの`navigator.credentials.get()`へ渡すWebAuthn認証オプションを返す。

```json
{
  "challengeId": "challenge_...",
  "publicKey": {
    "challenge": "base64url-challenge",
    "rpId": "example.com",
    "allowCredentials": [],
    "userVerification": "required"
  }
}
```

認証完了:

```http
POST /api/v1/auth/passkeys/authentication
```

```json
{
  "challengeId": "challenge_...",
  "credential": {
    "id": "...",
    "rawId": "...",
    "type": "public-key",
    "response": {
      "clientDataJSON": "...",
      "authenticatorData": "...",
      "signature": "...",
      "userHandle": "..."
    }
  }
}
```

サーバ側は`webauthn_authentication_challenge`と`webauthn_credential`を検証し、RP ID、origin、challenge、user presence、user verification、署名、必要に応じてsign countを確認する。

成功時はセッションCookieを発行し、ユーザ概要を返す。

```json
{
  "user": {
    "id": "user_...",
    "email": "user@example.com",
    "displayName": "表示名",
    "status": "active"
  }
}
```

セッションCookieに入れるセッションIDはDBへ平文保存しない。PostgreSQLの正本は`session.session_token_hash`、`session_type`, `expires_at`、`revoked_at`、`last_seen_at`であり、一般ユーザ用セッションと管理ユーザ用セッションは分離する。

ログイン失敗にはレート制限または一時ロックを適用する。

## ログアウト

```http
POST /api/v1/auth/logout
```

現在のセッションを無効化する。対象`session.revoked_at`を設定し、成功時は`204 No Content`を返す。

## 会員情報取得

```http
GET /api/v1/me
```

```json
{
  "id": "user_...",
  "email": "user@example.com",
  "displayName": "表示名",
  "status": "active",
  "emailVerifiedAt": "2026-05-08T00:00:00Z"
}
```

## 会員情報更新

```http
PATCH /api/v1/me
```

```json
{
  "displayName": "新しい表示名"
}
```

メールアドレス変更を提供する場合は、変更先メール確認と再認証フローを別途定義する。初版では表示名更新を主対象とする。

## Passkey管理

```http
GET /api/v1/me/passkeys
POST /api/v1/me/passkeys/registration-options
POST /api/v1/me/passkeys
DELETE /api/v1/me/passkeys/{credentialId}
```

登録済みPasskey credentialの一覧、追加、無効化を扱う。credential秘密情報、公開鍵の生値、セッションIDはレスポンスに含めない。

credentialを無効化する場合、対象credentialで認証中のセッションや既存セッションをどう扱うかは#89で確定する。

## アカウント復旧

```http
POST /api/v1/auth/account-recovery-requests
POST /api/v1/auth/account-recovery/confirm
```

要求:

```json
{
  "email": "user@example.com"
}
```

確認:

```json
{
  "token": "...",
  "credential": {
    "id": "...",
    "rawId": "...",
    "type": "public-key",
    "response": {
      "clientDataJSON": "...",
      "attestationObject": "..."
    }
  }
}
```

アカウント復旧要求の応答は、メールアドレスの登録有無を推測しにくい形にする。

確認用の平文`token`はDBに保存しない。PostgreSQLの正本は`account_recovery_challenge.token_hash`、`expires_at`、`attempt_count`、`max_attempts`、`send_count`、`used_at`、`revoked_at`である。

復旧成功時は新しいPasskey credentialを登録できる。既存セッション、未使用WebAuthnチャレンジ、既存credentialの扱いは#89と#90で具体化する。

## 退会

```http
POST /api/v1/me/withdraw
```

```json
{
  "reauthenticationRequired": true
}
```

退会実行前の本人確認は、Passkey再認証または現在セッション確認で行う。具体方式は#89で確定する。

一般ユーザを`withdrawn`状態へ遷移させ、退会後はログインできない。一般ユーザは書籍を保持しないため、アップロード済み書籍削除は対象外である。お気に入りと閲覧履歴は無効化する。

退会完了時は対象一般ユーザの既存`session`、未使用`email_verification_token`、未使用`webauthn_registration_challenge`、未使用`webauthn_authentication_challenge`、未使用`account_recovery_challenge`を失効する。credentialを失効するか保持するかは#89で確定する。

成功時は`204 No Content`を返す。

## 主なエラー

| 状態 | エラー |
| --- | --- |
| 入力不正 | `400 Bad Request` |
| メール未確認 | `403 Forbidden`または状態付き応答 |
| 認証失敗 | `401 Unauthorized` |
| 権限不足 | `403 Forbidden` |
| レート制限 | `429 Too Many Requests` |
| チャレンジ期限切れ | `422 Unprocessable Entity` |

## 認証データモデルとの対応

| APIで扱う値 | 正本テーブル | 保存、検証方針 |
| --- | --- | --- |
| メール確認`token` | `email_verification_token` | 平文保存せず`token_hash`で照合する。有効期限、試行回数、使用済み、失効、再送回数を確認する。 |
| Passkey登録`challengeId` / `challenge` | `webauthn_registration_challenge` | 平文チャレンジは保存せず`challenge_hash`で照合する。RP ID、origin、user verification、attestationを検証する。 |
| Passkey認証`challengeId` / `challenge` | `webauthn_authentication_challenge` | 平文チャレンジは保存せず`challenge_hash`で照合する。RP ID、origin、署名、user presence、user verificationを検証する。 |
| Passkey credential | `webauthn_credential` | credential ID、公開鍵、sign count、transport、状態を保存する。秘密鍵は保存しない。 |
| アカウント復旧`token` | `account_recovery_challenge` | 平文保存せず`token_hash`で照合する。有効期限、試行回数、使用済み、失効、再送回数を確認する。 |
| セッションCookie | `session` | Cookie内のセッションIDは平文保存せず`session_token_hash`で照合する。期限、失効、主体の状態を確認する。 |

## 後続で詳細化する事項

- RP ID、origin、user verification要件、attestation扱い、対応ブラウザ前提。
- local開発時のRP ID、origin、設定名、環境変数名。
- WebAuthnチャレンジ、復旧チャレンジ、セッションの有効期限、試行回数、再送制限の具体値。
- 登録完了時のセッション発行有無。
- 退会前の本人確認方式。
- 退会後のメールアドレス再利用可否。
- メールアドレス変更API。
- CSRF対策の具体方式。
