# Issue #87 最小テストと確認コマンドを整備する

## 目的

issue #48 / PBI-001 のsub-issueとして、Sprint S0で作成したフロントエンド、API、Worker、Docker Composeミドルウェアを継続開発で確認できる最小コマンドとして整理する。

この文書はSprint S0の作業成果物である。実装では、フロントエンドに不足していた `typecheck` script を追加し、各確認コマンドと実行結果をこの文書、`test-report.md`、ローカル開発手順へ反映する。
最終更新日: 2026-05-16

## GitHub Issue

- Issue: #87 最小テストと確認コマンドを整備する
- Parent: #48 開発者として、フロントエンド、API、ワーカーをローカルで起動できるようにしたい
- Labels: `type:test`, `area:frontend`, `area:api`, `area:worker`

## 受け入れ条件

| 条件 | 判定方法 | 現時点 |
| --- | --- | --- |
| フロントエンドの最小確認コマンドが用意されている。 | `apps/frontend/package.json` とこの文書に最小確認コマンドがある。 | 完了。 |
| フロントエンドの最小確認コマンドとして `lint`、`typecheck`、`build`、開発サーバ起動確認が用意されている。 | `npm.cmd run lint`、`npm.cmd run typecheck`、`npm.cmd run build`、`npm.cmd run dev` を定義し、手順に記録する。 | 完了。`typecheck` scriptを追加。 |
| APIの最小テストまたは起動確認コマンドが用意されている。 | `.\gradlew.bat :apps:api:test` と `:apps:api:bootRun`、health確認を記録する。 | 完了。 |
| Workerの最小テストまたは起動確認コマンドが用意されている。 | `.\gradlew.bat :apps:worker:test` と `:apps:worker:bootRun` を記録する。 | 完了。 |
| Docker Composeミドルウェアの確認コマンドが用意されている。 | `docker compose config`、起動、状態確認、疎通、ログ、停止を記録する。 | 完了。 |
| 実行結果をIssueまたはPull Requestへ記録できる。 | `test-report.md` と、この文書の確認結果欄に記録する形式を用意する。 | 完了。 |

## 採用方針

| 項目 | 方針 |
| --- | --- |
| 最小確認の目的 | 後続PBIの着手前に、基盤の破損を短時間で検出できることを優先する。 |
| フロントエンド | `lint`、`typecheck`、`build` を自動確認の最小セットとし、`dev` は手動の起動確認として扱う。 |
| API | 外部依存なしの `test` を最小自動確認とし、ローカルミドルウェア起動後に `local` profileの起動とhealthを手動確認する。 |
| Worker | 外部依存なしの `test` を最小自動確認とし、ローカルミドルウェア起動後に `local` profileの起動ログを手動確認する。 |
| Docker Compose | `config`、`up`、`ps`、個別疎通、`logs`、`down` を最小確認コマンドとする。 |
| 記録先 | Sprint S0中は `development/scrum/sprints/sprint-s0/test-report.md` を実行結果の正本にする。IssueまたはPull Requestへは、この文書の「実行結果記録テンプレート」を貼り付けられる形にする。 |

## 最小確認コマンド

### フロントエンド

Windows PowerShellでは `npm.ps1` が実行ポリシーでブロックされる場合があるため、Sprint S0の確認コマンドは `npm.cmd` を正として記録する。

```powershell
Set-Location apps/frontend
npm.cmd run lint
npm.cmd run typecheck
npm.cmd run build
npm.cmd run dev
```

開発サーバ起動後、`http://localhost:3000` へアクセスし、トップページがHTTP 200で応答することを確認する。

### API

```powershell
.\gradlew.bat :apps:api:test
.\gradlew.bat :apps:api:bootRun --args='--spring.profiles.active=local --server.port=18081 --debug=false'
Invoke-RestMethod http://localhost:18081/actuator/health
```

APIの `local` profile起動確認は、先にDocker Composeミドルウェアを起動してから実行する。

### Worker

```powershell
.\gradlew.bat :apps:worker:test
.\gradlew.bat :apps:worker:bootRun --args='--spring.profiles.active=local --debug=false'
```

Workerの `local` profile起動確認は、先にDocker Composeミドルウェアを起動してから実行する。起動ログで `manga-worker local dependency health: db=UP, elasticsearch=UP, rabbit=UP` を確認する。

### Docker Composeミドルウェア

```powershell
docker compose config
docker compose up -d postgres elasticsearch rabbitmq
docker compose ps
docker compose exec postgres pg_isready -U manga -d manga
Invoke-RestMethod http://localhost:9200
docker compose exec elasticsearch bin/elasticsearch-plugin list
docker compose exec rabbitmq rabbitmqctl status
docker compose logs --tail=200 postgres
docker compose logs --tail=200 elasticsearch
docker compose logs --tail=200 rabbitmq
docker compose down
```

## 実行順序

1. フロントエンドの `lint`、`typecheck`、`build` を実行する。
2. API / Workerの `test` を実行する。
3. Docker Composeミドルウェアを起動し、状態と疎通を確認する。
4. APIを `local` profileで起動し、health詳細を確認する。
5. Workerを `local` profileで起動し、外部依存healthログを確認する。
6. フロントエンド開発サーバを起動し、HTTP 200を確認する。
7. `test-report.md` と、この文書の確認結果を更新する。

## 実行結果記録テンプレート

IssueまたはPull Requestへ記録する場合は、次の形式を使う。

```markdown
## #87 確認結果

- フロントエンド: `npm.cmd run lint` / `npm.cmd run typecheck` / `npm.cmd run build` / `npm.cmd run dev`
- API: `.\gradlew.bat :apps:api:test` / `:apps:api:bootRun` / `/actuator/health`
- Worker: `.\gradlew.bat :apps:worker:test` / `:apps:worker:bootRun`
- Docker Compose: `config` / `up` / `ps` / PostgreSQL / Elasticsearch / RabbitMQ / `logs` / `down`
- 未実行または注意点:
```

## 実装タスク

- [x] Red: `apps/frontend/package.json` に `typecheck` scriptがなく、#87のフロントエンド最小確認コマンドを満たせないことを確認する。
- [x] Green: `typecheck` scriptとして `tsc --noEmit` を追加する。
- [x] Green: フロントエンド、API、Worker、Docker Composeの最小確認コマンドをこの文書へ記録する。
- [x] Document: `doc/05_development/04_local_development.md` と `test-report.md` へ#87の確認コマンドと結果を反映する。

## 対象外

- PlaywrightなどのE2Eテスト導入。
- Jest、Vitest、React Testing Libraryなどのフロントエンド単体テスト導入。
- 業務API、変換ジョブ、RabbitMQ listener、DB migration、Elasticsearch index作成の詳細テスト。
- CIワークフロー作成。CI化は後続の運用または開発基盤issueで扱う。

## 実行結果

詳細は `test-report.md` を正本とする。

| 観点 | 結果 |
| --- | --- |
| フロントエンドlint | `npm.cmd run lint` 成功。 |
| フロントエンド型チェック | `npm.cmd run typecheck` 成功。 |
| フロントエンドビルド | 初回は `next/font/google` のGoogle Fonts取得で失敗。外部フォント依存を外した後、`npm.cmd run build` 成功。 |
| フロントエンド開発サーバ | `npm.cmd run dev -- --hostname 127.0.0.1 --port 3000` で起動し、`http://127.0.0.1:3000` がHTTP 200を返すことを確認。確認後にプロセスを停止。 |
| API / Worker最小テスト | 初回はGradle Wrapper配布物取得がネットワーク制限で失敗。権限付き再実行で `.\gradlew.bat :apps:api:test :apps:worker:test` 成功。 |
| Docker Compose設定確認 | `docker compose config` 成功。Docker設定ファイルへのアクセス拒否警告は継続。 |
| ミドルウェア実疎通 | #85 / #86で確認済みの `up`、`ps`、PostgreSQL、Elasticsearch、RabbitMQ、API health、Worker local health loggerを#87の最小確認コマンドとして採用。 |
