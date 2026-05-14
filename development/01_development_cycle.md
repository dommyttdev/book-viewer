# 開発サイクル運用手順

## 目的

このドキュメントは、`development/` 配下の成果物を使って、スクラムとTDDのサイクルを継続的に回すための手順を定義する。

`doc/` は設計書の正本、`development/` はアジャイル開発の運用成果物として扱う。スプリントで得た仕様判断や設計変更は、`development/` に記録するだけで終わらせず、必要に応じて `doc/` 配下の該当設計書へ反映する。

## ディレクトリの使い分け

| パス | 用途 |
| --- | --- |
| `development/scrum/02_product_backlog.md` | スプリントへ投入するPBI候補、優先順位、設計参照、TDD観点を管理する。 |
| `development/scrum/04_sprint_plan.md` | MVPまでの初期スプリント見通しを管理する。 |
| `development/scrum/sprints/` | 各スプリントの計画、レビュー、レトロスペクティブ、テスト結果を保存する。 |
| `development/tdd/01_tdd_strategy.md` | TDDの基本方針を管理する。 |
| `development/tdd/02_test_matrix.md` | PBIごとのテスト観点を管理する。 |
| `development/tdd/logs/` | PBIごとのRed / Green / Refactor記録を保存する。 |
| `development/templates/` | スプリント運用とストーリー分解に使うテンプレートを保存する。 |

## 1スプリントの流れ

1. バックログを確認する。
2. スプリント計画を作成する。
3. PBIをTDD単位へ分解する。
4. Red / Green / Refactor / Document のサイクルで実装する。
5. テスト結果と手動確認を記録する。
6. スプリントレビューを行う。
7. レトロスペクティブを行う。
8. 次スプリントへ未完了事項と改善アクションを反映する。

## スプリント開始前

### 1. スプリントディレクトリを作成する

スプリント開始時に、次の形式でディレクトリを作成する。

```text
development/scrum/sprints/sprint-XX/
```

例:

```text
development/scrum/sprints/sprint-01/
```

### 2. テンプレートをコピーする

次のファイルを作成する。

```text
development/scrum/sprints/sprint-XX/planning.md
development/scrum/sprints/sprint-XX/review.md
development/scrum/sprints/sprint-XX/retrospective.md
development/scrum/sprints/sprint-XX/test-report.md
```

コピー元:

| 作成先 | コピー元 |
| --- | --- |
| `planning.md` | `development/templates/sprint_planning_template.md` |
| `review.md` | `development/templates/sprint_review_template.md` |
| `retrospective.md` | `development/templates/retrospective_template.md` |
| `test-report.md` | このドキュメントの「テスト結果記録形式」を使う。 |

## スプリントプランニング

### 1. スプリントゴールを決める

スプリントゴールは、実装タスクの羅列ではなく、利用者価値または技術リスク低減として書く。

例:

- 一般ユーザが会員登録、メール確認、ログイン時メール2段階認証を経てセッションを取得できる。
- zipアーカイブに限定して、アップロードからWebP生成までの縦切りを通す。

### 2. PBIを選ぶ

次を確認して、スプリントへ入れるPBIを選ぶ。

- [プロダクトバックログ](scrum/02_product_backlog.md) の優先度
- [リリース計画](scrum/03_release_plan.md) のスプリント見通し
- [スプリント計画初期版](scrum/04_sprint_plan.md) の対象PBI
- 対応する `doc/` 設計書、受入条件、API契約
- [テスト観点マトリクス](tdd/02_test_matrix.md) の単体、結合、E2E / 手動確認観点

### 3. Definition of Readyを確認する

スプリントへ入れるPBIは、次を満たしていることを確認する。

- 利用者または運用者にとっての価値が説明できる。
- 対応する設計書または受入条件が参照できる。
- TDDで最初に書くテスト観点が1つ以上ある。
- 正常系と主要な異常系が整理されている。
- 権限、入力検証、データ永続化、外部依存の影響が確認されている。
- DBマイグレーション、初期データ、設定値、環境変数の追加有無が確認されている。
- メール送信、ファイル保存、外部プロセス、検索、キューなどの外部境界をどう確認するか決まっている。
- 1スプリント内で完了できる大きさに分割されている。

## PBIのTDD分解

PBIに着手する前に、次のファイルを作成する。

```text
development/tdd/logs/pbi-XXX.md
```

例:

```text
development/tdd/logs/pbi-003.md
```

内容は [ユーザーストーリー分解テンプレート](templates/story_breakdown_template.md) を使う。

### 記録する内容

- 参照する `doc/` 設計書
- 受入条件
- 最初に書く失敗テスト
- 単体テスト、結合テスト、E2E / 手動確認の対象
- Red / Green / Refactor の履歴
- 実装中に発生した設計判断
- `doc/` へ反映が必要な変更

## TDD実装中

### Red

最初に、期待する振る舞いを表す失敗テストを書く。

記録例:

```text
Red:
- メール未確認ユーザがログイン確認を完了できないテストを追加した。
- 期待どおり失敗した。
```

### Green

テストを通す最小実装を行う。

記録例:

```text
Green:
- user.status が active でない場合にログイン確認を拒否する実装を追加した。
- 対象テストが通った。
```

### Refactor

責務、命名、重複、例外処理、ログを見直す。

記録例:

```text
Refactor:
- 認証状態判定を AuthenticatedSubjectPolicy に分離した。
- コントローラから状態判定ロジックを取り除いた。
```

### Document

仕様、設計、API、データモデル、運用に影響する場合は `doc/` を更新する。

記録例:

```text
Document:
- ログイン失敗時のエラー応答を doc/04_design/03_api_contracts/06_account_api.md に反映した。
```

## スプリント中の毎日の確認

作業日ごとに、次を確認する。

- スプリントゴールに近づいているか。
- 先に書くべきテストが実装後回しになっていないか。
- ブロッカーがあるか。
- `doc/` へ反映すべき設計変更が発生しているか。
- PBIが大きすぎる場合、分割できるか。

軽いメモは `planning.md` のタスク欄または各 `development/tdd/logs/pbi-XXX.md` に追記する。

## テスト結果記録形式

`development/scrum/sprints/sprint-XX/test-report.md` は、次の形式で作成する。

```markdown
# Sprint XX テスト結果

## 実行したテスト

| 種別 | コマンドまたは確認内容 | 結果 |
| --- | --- | --- |
| 単体テスト |  |  |
| 結合テスト |  |  |
| E2E |  |  |
| 手動確認 |  |  |

## 未実行のテスト

| テスト | 未実行理由 | 次の扱い |
| --- | --- | --- |
|  |  |  |

## 確認した異常系

- 

## 残リスク

- 

## 更新したドキュメント

- 
```

## スプリントレビュー

スプリント終了時に `review.md` を更新する。

確認する内容:

- スプリントゴールを満たしたか。
- 完成したPBIはどれか。
- デモできる動作は何か。
- 受入条件を満たしたか。
- 実行したテストと未実行のテストは何か。
- `doc/` の更新漏れがないか。
- 未完了PBIをどう扱うか。

未完了PBIは完了扱いにせず、次スプリント候補へ戻す。

## レトロスペクティブ

スプリント終了時に `retrospective.md` を更新する。

確認する内容:

- よかったこと
- 問題だったこと
- TDDがうまく回った箇所
- 先にテストを書きにくかった箇所
- PBI分割が大きすぎた箇所
- 次スプリントで試す改善アクション

改善アクションは、次スプリントの `planning.md` に転記する。

## 次スプリントへの反映

レビューとレトロスペクティブ後、次を更新する。

- `development/scrum/02_product_backlog.md`
  - PBI追加、分割、優先度変更があれば更新する。
- `development/scrum/04_sprint_plan.md`
  - スプリント見通しが変わった場合に更新する。
- `development/tdd/02_test_matrix.md`
  - 新しいテスト観点が増えた場合に更新する。
- `doc/` 配下の設計書
  - 仕様、API、データモデル、権限、運用手順が変わった場合に更新する。

## 完了判定

PBIを完了にする前に、次を確認する。

- 受入条件を満たしている。
- 失敗するテストを先に書いた、または先に書けなかった理由が記録されている。
- 必要な単体テスト、結合テスト、E2Eまたは手動確認を実行した。
- 未実行テストと残リスクを記録した。
- 権限、入力検証、ログ、外部依存失敗を確認した。
- `doc/` へ反映すべき変更を反映した。
- `development/tdd/logs/pbi-XXX.md` を更新した。
- スプリントの `test-report.md` に結果を記録した。

## 運用上の注意

- スプリント中に大きな設計変更が必要になった場合は、該当PBIを止め、設計書更新またはADR追加を先に行う。
- テストしにくい処理は、先に確認手順を明文化してから実装する。
- 外部サービス、ファイルシステム、7-Zip、RabbitMQ、Elasticsearchは、単体テストで無理に実物を使わず、境界の単体テストと結合確認を分ける。
- 1つのPBIがスプリント内で終わらない場合は、横に分けず、利用者価値または技術リスクが確認できる縦切りへ分割する。
- `development/` の記録と `doc/` の設計が矛盾した場合は、設計の正本である `doc/` を更新するか、`development/` 側の計画を修正して整合させる。
