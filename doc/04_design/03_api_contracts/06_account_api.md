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

## メール確認

```http
POST /api/v1/auth/email-verifications/confirm
```

```json
{
  "token": "..."
}
```

トークンは有効期限、使い捨て、再送制限を持たせる。

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

ログイン失敗にはレート制限または一時ロックを適用する。

## ログアウト

```http
POST /api/v1/auth/logout
```

現在のセッションを無効化する。成功時は`204 No Content`を返す。

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

成功時は`204 No Content`を返す。

## 主なエラー

| 状態 | エラー |
| --- | --- |
| 入力不正 | `400 Bad Request` |
| メール未確認 | `403 Forbidden`または状態付き応答 |
| 認証失敗 | `401 Unauthorized` |
| レート制限 | `429 Too Many Requests` |
| トークン期限切れ | `422 Unprocessable Entity` |

## 後続で詳細化する事項

- トークンの有効期限、再送制限、保存方式。
- 退会後のメールアドレス再利用可否。
- メールアドレス変更API。
- CSRF対策の具体方式。
