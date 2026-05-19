# Issue #82 Next.jsフロントエンド最小構成

## 目的

issue #48 / PBI-001 のsub-issueとして、`apps/frontend/` にNext.jsフロントエンドの最小構成を作成するための実装方針、受入条件、確認コマンド、残リスクを整理する。

この成果物はSprint S0の作業成果物である。Next.js最小構成、依存関係インストール、開発サーバ起動、HTTP応答、API接続先設定の確認結果をこの文書と `test-report.md` に記録した。

## GitHub Issue

- Issue: #82 Next.jsフロントエンドの最小構成を作成する
- Parent: #48 開発者として、フロントエンド、API、ワーカーをローカルで起動できるようにしたい
- Labels: `type:feature`, `area:frontend`

## 受け入れ条件

| 条件 | 判定方法 | 現時点 |
| --- | --- | --- |
| Next.jsアプリケーションの最小構成が作成されている。 | `apps/frontend/` に `package.json`、Next.js設定、App Routerの最小ページがある。 | 完了。`create-next-app` により `src/app/` 構成で作成済み。 |
| 依存関係をインストールできる。 | `cd apps/frontend && npm install` が成功する。 | 完了。`node_modules/` と `package-lock.json` が作成済み。 |
| ローカルで開発サーバを起動できる。 | `npm run dev` が成功し、ローカルURLへアクセスできる。 | 完了。`npm.cmd run dev` で `http://localhost:3000` が起動。 |
| 最小画面をブラウザまたは確認コマンドで確認できる。 | ブラウザ表示、または `curl http://localhost:3000` でHTML応答を確認する。 | 完了。`GET / 200` を確認。 |

## 採用方針

| 項目 | 方針 |
| --- | --- |
| 配置 | `apps/frontend/` |
| フレームワーク | Next.js |
| 言語 | TypeScript |
| ルーティング | App Router |
| パッケージ管理 | npm |
| ロックファイル | `apps/frontend/package-lock.json` |
| Node.js | ローカル確認済みのv22系を前提にする。実装結果ではNode.js v22.14.0、npm 11.14.1を確認済み。 |
| スタイリング | 実装結果ではTailwind CSS 4系関連パッケージが生成されている。Sprint S0では業務UIを作り込まず、生成直後の最小スタイルに留める。 |
| API接続先 | `NEXT_PUBLIC_API_BASE_URL` で設定する。既定値はローカルAPIを想定するが、実値は環境変数で差し替える。 |

Sprint S0では、認証、一覧、検索、ビューア、管理画面などの業務画面は作らない。最小画面は「フロントエンドが起動し、API接続先設定を読み込める」ことの確認に限定する。

## 推奨ディレクトリ構成

```text
apps/frontend/
├── src/
│   └── app/
│       ├── favicon.ico
│       ├── globals.css
│       ├── layout.tsx
│       └── page.tsx
├── public/
├── eslint.config.mjs
├── next.config.ts
├── package.json
├── package-lock.json
├── postcss.config.mjs
├── tsconfig.json
├── AGENTS.md
└── CLAUDE.md
```

`apps/frontend/.env.local` はGit管理外にする。公開してよい値だけを `NEXT_PUBLIC_` 接頭辞付きで扱う。

当初は `app/` 直下構成を想定したが、実際の `create-next-app` 実行結果は `src/app/` 構成である。Next.jsの最小構成として問題ないため、Sprint S0では生成結果を採用する。

## 最小画面の要件

- `/` にアクセスすると、アプリケーションが起動していることを確認できる。
- 画面内または開発ログで、`NEXT_PUBLIC_API_BASE_URL` の読み込み先を確認できる。
- APIが未起動でもフロントエンド自体は起動できる。
- シークレット、トークン、パスワード、Cookie値を画面やログへ出力しない。
- 業務機能が未実装であることを、実装済み機能のように見せない。

## npm scripts

`package.json` には少なくとも次を用意する。

| script | 用途 |
| --- | --- |
| `dev` | 開発サーバ起動 |
| `build` | 本番ビルド確認 |
| `start` | ビルド済みアプリ起動 |
| `lint` | Next.js / ESLint確認 |
| `typecheck` | TypeScript型チェック。#87で `tsc --noEmit` として追加済み。 |

確認コマンド:

```bash
cd apps/frontend
npm install
npm run lint
npm run typecheck
npm run build
npm run dev
```

Windows PowerShellで `npm.ps1` が実行ポリシーによりブロックされる場合は、次を使用する。

```powershell
cd apps/frontend
npm.cmd install
npm.cmd run lint
npm.cmd run typecheck
npm.cmd run build
npm.cmd run dev
```

## TDD / 確認観点

| 観点 | 最初に確認すること | 種別 |
| --- | --- | --- |
| 正常系 | `npm run build` が成功し、`/` の最小ページを生成できる。 | ビルド確認 |
| 入力検証 | `NEXT_PUBLIC_API_BASE_URL` が未設定でもローカル既定値で起動できる。 | 手動 / 型確認 |
| 権限 | Sprint S0では業務権限なし。権限制御をフロントエンドだけに置かない方針を維持する。 | 設計確認 |
| 異常系 | API未起動でもフロントエンド起動が失敗しない。 | 手動確認 |
| 外部依存 | API接続先URLは環境変数で差し替えられる。 | 設定確認 |

## 実装タスク

- [x] Red: `apps/frontend/` が存在しない、または `npm run build` を実行できない状態を確認する。
- [x] Green: Next.js最小構成を追加し、`npm install`、`npm run lint`、`npm run build`、`npm run dev` が成功する状態にする。`typecheck` scriptは#87で追加済み。
- [x] Refactor: 最小画面、環境変数名、npm scriptsが後続の業務画面実装を妨げないか確認する。
- [x] Document: 実構成に合わせて `development/scrum/sprints/sprint-s0/test-report.md`、`doc/05_development/03_environment_setup.md`、`doc/05_development/04_local_development.md` を更新する。

## 対象外

- 会員登録、ログイン、認可、書籍一覧、検索、ビューア、管理画面の実装。
- UIコンポーネントライブラリ、状態管理ライブラリ、E2Eテスト基盤の導入。
- APIクライアント生成、OpenAPI連携、認証Cookie処理。
- 本番向けDockerfile、CDN、TLS、ドメイン設定。

## 完了時に記録する内容

実環境構築後、次を追記する。

- 使用したNode.js / npmバージョン。
- 作成されたNext.js / React / TypeScriptの主要バージョン。
- 実行したコマンドと結果。
- 開発サーバのURL。
- 画面確認方法。
- 未実行の確認と理由。
- ログに秘密情報が出ていないことの確認結果。

## 実行結果

| 項目 | 結果 |
| --- | --- |
| 作成コマンド | `npx create-next-app@latest . --typescript --eslint --app --src-dir false --import-alias "@/*" --use-npm` |
| Node.js | v22.14.0 |
| npm | 11.14.1 |
| Next.js | 16.2.6 |
| React | 19.2.4 |
| TypeScript | `^5` |
| `npm.cmd run lint` | 成功。ESLintエラーなし。 |
| `npm.cmd run typecheck` | 成功。`tsc --noEmit` で型エラーなし。 |
| `npm.cmd run build` | 成功。Turbopackで `/` と `/_not-found` の静的生成を確認。 |
| `npm.cmd run dev` | 成功。Local URLは `http://localhost:3000`、Network URLは `http://192.168.0.10:3000`。 |
| 最小画面応答 | 成功。`GET / 200` を確認。 |
| Git ignore | `node_modules/`、`.next/`、`next-env.d.ts` は `apps/frontend/.gitignore` でignore済み。 |

## 現時点の完了メモ

- 実行したテスト: `npm.cmd run lint`、`npm.cmd run typecheck`、`npm.cmd run build`、`npm.cmd run dev`。
- 手動確認: GitHub Issue #82の本文、受入条件、親Issue #48との関係、生成ディレクトリ構成、ignore設定を確認した。
- 未対応事項: GitHub Issueチェックリスト更新。`vercel.svg` の画像比率警告は生成直後テンプレート由来で、S0の起動基盤としてはブロッカーにしない。
- 更新したドキュメント:
  - `development/scrum/sprints/sprint-s0/issue-82-frontend-minimal.md`
  - `development/scrum/sprints/sprint-s0/test-report.md`
  - `doc/05_development/03_environment_setup.md`
  - `doc/05_development/04_local_development.md`

## 後続Issueへの引き継ぎ

| 項目 | 引き継ぎ先 | 理由 | 推奨する受け入れ条件 |
| --- | --- | --- | --- |
| `NEXT_PUBLIC_API_BASE_URL` の最小設定 | #88 ローカル開発手順とTODOを実装結果に合わせて更新する | フロントエンド単体起動は完了しており、API接続先はローカル開発手順、`.env.example`、実構成の整合として扱うのが自然である。 | フロントエンドのローカルAPI接続先を `NEXT_PUBLIC_API_BASE_URL` で差し替えられることが `.env.example` とローカル開発手順に記録されている。 |

#86はAPIとWorkerのローカル設定、外部依存疎通を主対象にしているため、フロントエンド公開環境変数は#88へ寄せる。
