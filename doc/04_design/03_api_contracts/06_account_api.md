# アカウントAPI契約初版

## 目的

このドキュメントは、一般ユーザのアカウント管理API契約初版を定義する。

対象は、会員登録、ログイン、ログアウト、会員情報取得、会員情報更新、退会である。

## 前提

- 一般ユーザは書籍を保持しない。
- 会員登録時にメール認証を行う。
- ログイン時の2段階認証にもメールを活用する。
- パスワードリセットを提供する。
- パスワード、認証トークン、セッションIDはレスポンスやログに露出しない。

## 会員登録

```http
POST /api/v1/auth/register
```

```json
{
  "email": "user@example.com",
  "password": "password",
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

メールアドレス形式、パスワード強度、重複を検証する。登録済みメールの扱いは、アカウント存在推測を避ける応答にする。

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

## ログイン

```http
POST /api/v1/auth/login
```

```json
{
  "email": "user@example.com",
  "password": "password"
}
```

初回認証に成功した場合、メール2段階認証を要求する。

```json
{
  "twoFactorRequired": true,
  "challengeId": "challenge_..."
}
```

`challengeId`は`login_challenge.challenge_id`に対応する公開識別子であり、ワンタイムコードそのものではない。DBには`code_hash`を保存し、平文コードは保存しない。

2段階認証確認:

```http
POST /api/v1/auth/login/confirm
```

```json
{
  "challengeId": "challenge_...",
  "code": "123456"
}
```

確認時は`challengeId`で有効な`login_challenge`を特定し、入力された`code`をハッシュ化して照合する。`expires_at`、`attempt_count`、`max_attempts`、`used_at`、`revoked_at`を確認し、期限切れ、試行上限超過、使用済み、失効済みのチャレンジは拒否する。

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

メールアドレス変更を提供する場合は、変更先メール確認フローを別途定義する。初版では表示名更新を主対象とする。

## パスワードリセット

```http
POST /api/v1/auth/password-resets
POST /api/v1/auth/password-resets/confirm
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
  "newPassword": "new-password"
}
```

パスワードリセット要求の応答は、メールアドレスの登録有無を推測しにくい形にする。

確認用の平文`token`はDBに保存しない。PostgreSQLの正本は`password_reset_token.token_hash`、`expires_at`、`attempt_count`、`max_attempts`、`send_count`、`used_at`、`revoked_at`であり、リセット完了時は対象主体の未使用リセットトークンと既存セッションを失効する。

## 退会

```http
POST /api/v1/me/withdraw
```

```json
{
  "password": "password"
}
```

一般ユーザを`withdrawn`状態へ遷移させ、退会後はログインできない。一般ユーザは書籍を保持しないため、アップロード済み書籍削除は対象外である。お気に入りと閲覧履歴は無効化する。

退会完了時は対象一般ユーザの既存`session`、未使用`email_verification_token`、未使用`login_challenge`、未使用`password_reset_token`を失効する。

成功時は`204 No Content`を返す。

## 主なエラー

| 状態 | エラー |
| --- | --- |
| 入力不正 | `400 Bad Request` |
| メール未確認 | `403 Forbidden`または状態付き応答 |
| 認証失敗 | `401 Unauthorized` |
| レート制限 | `429 Too Many Requests` |
| トークン期限切れ | `422 Unprocessable Entity` |

## 認証データモデルとの対応

| APIで扱う値 | 正本テーブル | 保存、検証方針 |
| --- | --- | --- |
| メール確認`token` | `email_verification_token` | 平文保存せず`token_hash`で照合する。有効期限、試行回数、使用済み、失効、再送回数を確認する。 |
| ログイン確認`challengeId` | `login_challenge` | `challenge_id`でチャレンジを特定する。ワンタイムコードは`code_hash`で照合する。 |
| ログイン確認`code` | `login_challenge` | 平文保存せず`code_hash`で照合する。試行回数と上限を更新、確認する。 |
| パスワードリセット`token` | `password_reset_token` | 平文保存せず`token_hash`で照合する。有効期限、試行回数、使用済み、失効、再送回数を確認する。 |
| セッションCookie | `session` | Cookie内のセッションIDは平文保存せず`session_token_hash`で照合する。期限、失効、主体の状態を確認する。 |

## 後続で詳細化する事項

- トークン、ワンタイムコード、セッションの有効期限、試行回数、再送制限の具体値。
- 退会後のメールアドレス再利用可否。
- メールアドレス変更API。
- CSRF対策の具体方式。
