# 管理API契約初版

## 目的

このドキュメントは、管理機能に関するAPI契約初版を定義する。

対象は、管理ユーザ一覧、管理ユーザ登録、管理ユーザ編集、管理ユーザ削除、ロール一覧、ロール設定である。

## 前提

- 管理APIは`/api/v1/admin`配下に置く。
- 一般ユーザは管理APIを実行できない。
- 管理ユーザの操作可否は`super_admin`, `admin`, `operator`, `viewer`などのロールと権限で制御する。
- 管理ユーザ削除は監査と過去操作主体参照のため、論理削除または停止を基本とする。

## 管理ユーザログイン

管理ユーザの認証は一般ユーザと分離する。

```http
POST /api/v1/admin/auth/login
POST /api/v1/admin/auth/login/confirm
POST /api/v1/admin/auth/logout
```

メールアドレス、パスワード、メール2段階認証を使う。成功時は管理ユーザ用セッションCookieを発行する。

## 管理ユーザ一覧

```http
GET /api/v1/admin/admin-users
```

| クエリ | 内容 |
| --- | --- |
| `status` | `active`, `suspended`, `deleted` |
| `role` | ロールコード |
| `page`, `size`, `sort` | ページング、ソート |

```json
{
  "items": [
    {
      "id": "admin_user_...",
      "email": "admin@example.com",
      "displayName": "管理者",
      "status": "active",
      "roles": [{ "code": "admin", "name": "管理者" }],
      "lastLoginAt": "2026-05-08T00:00:00Z"
    }
  ],
  "page": { "number": 1, "size": 20, "totalItems": 1, "totalPages": 1 }
}
```

## 管理ユーザ登録

```http
POST /api/v1/admin/admin-users
```

`super_admin`のみ実行できる。

```json
{
  "email": "admin@example.com",
  "displayName": "管理者",
  "roleCodes": ["admin"]
}
```

初期パスワード設定方法は、招待メールまたは初回設定トークン方式を候補とする。平文パスワードを管理者に通知しない。

## 管理ユーザ編集

```http
PATCH /api/v1/admin/admin-users/{adminUserId}
```

`super_admin`のみ実行できる。

```json
{
  "displayName": "更新後管理者",
  "status": "active",
  "roleCodes": ["admin", "operator"]
}
```

自分自身の`super_admin`権限を失わせる操作や、最後の`super_admin`を停止する操作は拒否する。

## 管理ユーザ削除 / 停止

```http
DELETE /api/v1/admin/admin-users/{adminUserId}
```

`super_admin`のみ実行できる。実体は論理削除または`suspended`への状態遷移とする。

成功時は`204 No Content`を返す。

## ロール一覧

```http
GET /api/v1/admin/roles
```

```json
{
  "items": [
    {
      "code": "super_admin",
      "name": "スーパー管理者",
      "permissions": ["admin_user.manage", "role.manage"]
    }
  ]
}
```

## ロール設定

```http
PUT /api/v1/admin/roles/{roleCode}/permissions
```

`super_admin`のみ実行できる。初版では固定ロールを基本とし、ロール設定変更APIはBeta / v1.0候補として扱う。

```json
{
  "permissionCodes": ["book.upload", "book.update"]
}
```

## 一般ユーザ管理

管理機能の一部として、一般ユーザ参照と停止を扱う。

```http
GET /api/v1/admin/users
GET /api/v1/admin/users/{userId}
POST /api/v1/admin/users/{userId}/suspend
POST /api/v1/admin/users/{userId}/activate
```

`super_admin`と`admin`は一般ユーザを参照、停止できる。`viewer`は参照のみ候補とする。

## 権限

| 操作 | 必要権限 |
| --- | --- |
| 管理ユーザ一覧 / 詳細 | `admin_user.read` |
| 管理ユーザ登録 / 編集 / 削除 | `admin_user.manage` |
| ロール一覧 | `role.read` |
| ロール設定 | `role.manage` |
| 一般ユーザ一覧 / 詳細 | `user.read` |
| 一般ユーザ停止 / 復帰 | `user.manage` |

## 主なエラー

| 状態 | エラー |
| --- | --- |
| 一般ユーザによるアクセス | `403 Forbidden` |
| 権限不足 | `403 Forbidden` |
| 最後の`super_admin`停止 | `409 Conflict` |
| 重複メール | `409 Conflict` |
| 入力不正 | `400 Bad Request` |

## 後続で詳細化する事項

- 管理ユーザ招待、初回パスワード設定フロー。
- 権限マトリクス独立ドキュメントとの同期。
- 管理操作の監査ログ。
- 固定ロールとカスタムロールの境界。
