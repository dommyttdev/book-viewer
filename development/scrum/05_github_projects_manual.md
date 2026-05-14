# GitHub Projects運用マニュアル

## 目的

このドキュメントは、自炊本閲覧WebアプリケーションのプロダクトバックログとスプリントバックログをGitHub Projectsで管理するための運用ルールを定義する。

GitHub Projectsは、PBIの状態、優先順位、見積もり、スプリント投入状況、作業状態を可視化するために使う。仕様、設計、API、データモデル、受入条件の正本は `doc/` 配下に置き、スクラム運用の方針と記録は `development/` 配下に置く。

## 基本方針

- PBIは原則としてGitHub Issueとして作成する。
- GitHub Projectsは、Issueをプロダクトバックログとスプリントバックログの両方から見られるようにする。
- Issue本文には、ユーザーストーリー、背景、受入条件、対象外、関連ドキュメントを記載する。
- Projectsのカスタムフィールドには、分類、Ready状態、優先順位、見積もり、スプリント、作業状態を記録する。
- Projects上で並び替えや絞り込みに使う値は、ラベルではなくカスタムフィールドを優先する。
- スプリントへ入れるPBIは、Definition of Readyを満たしているものに限定する。
- PBI完了時は、Issue、Projects、関連ドキュメント、テスト記録の整合を確認する。

## Projectの推奨構成

### Project名

```text
Manga Agile Board
```

プロジェクト名は変更してもよい。ただし、プロダクトバックログとスプリントバックログを同じProjectで扱う場合は、用途が分かる名前にする。

### 管理対象

| 対象 | 管理方法 |
| --- | --- |
| PBI | GitHub Issue |
| スパイク、調査 | GitHub Issue |
| 小さな実装タスク | PBI Issue内のタスクリスト、または必要に応じて子Issue |
| 不具合 | GitHub Issue |
| 採用しない候補 | Project上で `Ready = Drop` |
| メモ段階の候補 | ProjectのDraft item、または `Ready = Draft` のIssue |

Draft itemは短期メモに限定する。バックログとして残す判断をしたものはIssueへ変換する。

## カスタムフィールド

### Title

タイプ: GitHub Projects標準フィールド

GitHub Issue標準のTitleを使う。`Title` というText型カスタムフィールドは作成しない。

理由:

- GitHub Issueのタイトルと重複し、更新漏れが起きやすい。
- Projectの一覧、Issue、Pull Request連携で標準Titleの方が自然に使える。
- PBI名はIssueタイトルに置き、詳細はIssue本文に書く方が履歴を追いやすい。

### System

タイプ: `Single select`

システム、領域、または主な責務を選択する。

| 値 | 用途 |
| --- | --- |
| Product | 利用者価値、画面仕様、業務仕様など、特定の技術領域に閉じないPBI |
| Frontend | Next.jsフロントエンド |
| Backend API | Spring Boot API |
| Conversion Worker | Spring Boot変換ワーカー |
| Database | PostgreSQL、マイグレーション、永続化設計 |
| Search | Elasticsearch、検索インデックス、検索API |
| Infrastructure | Docker Compose、CI、環境設定、ミドルウェア、デプロイ |
| Documentation | 設計書、ADR、運用手順、開発手順 |
| Cross-cutting | 認証、認可、ログ、監視、エラーハンドリング、セキュリティなど複数領域にまたがるもの |

複数領域にまたがる場合は、主な価値または主なリスクを表す値を選ぶ。判断できない場合は `Cross-cutting` を使う。

### Ready

タイプ: `Single select`

PBIが着手可能かを表す。

| 値 | 意味 |
| --- | --- |
| Draft | PBI本文を起票、整理中 |
| NotReady | 内容はあるが、着手条件、仕様、依存関係、受入条件などが未解決 |
| Ready | 着手可能 |
| Drop | 検討した結果、プロダクトバックログへ追加しない |

`Ready = NotReady` の場合は、`Ready条件/備考` にReadyになるための条件を書く。

### Ready条件/備考

タイプ: `Text`

Ready状態の補足を記録する。

記載する内容:

- `Ready = NotReady` の場合に解消すべき条件
- 依存するIssue、設計書、ADR
- 分割方針
- 判断メモ
- Dropにした理由

例:

```text
画像変換のリソース上限を doc/04_design/07_image_conversion_design.md に反映後、Readyにする。
```

### Size

タイプ: `Single select`

PBIの相対サイズをフィボナッチ数で選択する。

| 値 | 用途 |
| --- | --- |
| 1 | 小さい修正、明確な単一作業 |
| 2 | 小さめのPBI |
| 3 | 標準的なPBI |
| 5 | やや大きいが1スプリント内で完了可能なPBI |
| 8 | 大きいPBI。分割を検討する |
| 13 | 原則として分割候補 |
| Unknown | 見積もり前、または調査しないと判断できない |

`13` はスプリントへ入れる前に分割を検討する。`Unknown` のままスプリントへ入れない。

### Priority

タイプ: `Single select`

プロダクトバックログ上の優先順位を表す。GitHub ProjectsでSort、Group、Filterに使うため、ラベルではなくカスタムフィールドとして管理する。

| 値 | 意味 |
| --- | --- |
| P0: Critical | MVP成立、セキュリティ、データ保護、開発継続に直結する最優先事項 |
| P1: High | MVPの主要導線または高い技術リスクに関わる重要事項 |
| P2: Medium | MVPまたはBetaで必要だが、P0 / P1の後でよい事項 |
| P3: Low | 改善、拡張、利便性向上など、後続スプリントで扱える事項 |
| Unset | 優先順位未設定 |

`Unset` のままスプリントへ入れない。初期運用では、PBIをReadyにする前に `Priority` を設定する。

### Labels

タイプ: `Labels`

PBIの種類、性質、注意点を表す。GitHub IssueのLabelsを使う。

推奨ラベル:

| ラベル | 用途 |
| --- | --- |
| `type:feature` | 新機能 |
| `type:bug` | 不具合修正 |
| `type:docs` | ドキュメント |
| `type:refactor` | 振る舞いを変えない改善 |
| `type:test` | テスト追加、テスト改善 |
| `type:spike` | 調査、検証 |
| `area:frontend` | フロントエンド |
| `area:api` | Backend API |
| `area:worker` | 変換ワーカー |
| `area:db` | PostgreSQL |
| `area:search` | Elasticsearch |
| `area:infra` | インフラ、CI、Docker |
| `area:security` | 認証、認可、入力検証、秘密情報 |
| `risk:high` | 技術リスクまたは仕様リスクが高い |

優先度は `Priority` カスタムフィールドで管理する。ラベルは種類、領域、リスクなど、複数付与したい性質の表現に使う。

### Sprint

タイプ: `Single select`

PBIをどのスプリントで扱うかを表す。

初期値:

| 値 | 用途 |
| --- | --- |
| Backlog | まだスプリント未割当 |
| S0: 実装基盤と開発運用 | 継続的にTDDで実装できる最小のプロジェクト基盤とスクラム運用 |
| S1: 一般ユーザ認証 | 会員登録、メール確認、ログイン時メール2段階認証、セッション |
| S2: 復旧と管理認可 | パスワードリセット、初期 `super_admin`、管理ユーザ固定ロール認可 |
| S3: アップロードとジョブ作成 | アップロード、原本保存、PostgreSQL上のジョブ作成、RabbitMQ配送 |
| S4: zip変換縦切り | zip限定のアップロードからWebP生成、ジョブ完了までの縦切り |
| S5: 7-Zipと変換状態 | rar / 7zip展開、サムネイル生成、変換状態確認 |
| S6: メタ情報と検索 | メタ情報編集、検索Outbox、Elasticsearch検索 |
| S7: 一覧とビューア | 本一覧、詳細、ビューア、ページ送り、読みかけ位置 |
| S8: お気に入りとMVP統合 | お気に入り、Docker Compose統合、MVP主要フローE2E |
| S9: 予備 | 実績に応じた予備または次期スプリント |
| S10: 予備 | 実績に応じた予備または次期スプリント |

`this` のような相対値は使わない。現在スプリントはProjectのビュー名やフィルタで表現する。これにより、完了後もどのスプリントで扱ったかを履歴として残せる。

Single selectの値はProject上で単独表示されるため、`S1` のような番号だけにはしない。値には短い目的名を含め、詳細なゴールは [スプリント計画初期版](04_sprint_plan.md) を正とする。

### Status

タイプ: `Single select`

スプリントバックログでのみ使う作業状態。

| 値 | 意味 |
| --- | --- |
| Todo | スプリントに入っているが未着手 |
| In Progress | 作業中 |
| Review | レビュー、確認、受入待ち |
| Blocked | 外部要因、判断待ち、依存関係により停止中 |
| Done | 完了 |

`Sprint = Backlog` のPBIでは、原則としてStatusを空欄または `Todo` のままにする。プロダクトバックログの状態管理には `Ready` を使う。

## Issueタイトル

Issueタイトルは、カスタムフィールドではなくGitHub Issue標準のTitleを使う。

原則として、ユーザーストーリー形式または成果が分かる形式で書く。

例:

```text
管理ユーザとして、自炊本アーカイブをアップロードしたい
一般ユーザとして、本一覧から読みたい本を開きたい
開発者として、APIとWorkerをローカルで起動できるようにしたい
```

コミットメッセージのような `feat:` 接頭辞はIssueタイトルには必須としない。ラベルで種類を表す。

## Issue本文テンプレート

PBI Issueは次の形式で作成する。

```markdown
## User Story

〇〇として、〇〇したい。
それは、〇〇のためである。

## 背景

-

## 受け入れ条件

- [ ]
- [ ]

## 対象外

-

## TDD観点

- 最初に書くテスト:
- 単体テスト:
- 結合テスト:
- E2E / 手動確認:

## 関連ドキュメント

-

## Ready条件 / 備考

-
```

不具合Issueの場合は、User Storyの代わりに再現手順、期待結果、実際の結果、影響範囲を書く。

## Definition of Ready

`Ready = Ready` にする前に、次を確認する。

- 利用者または運用者にとっての価値が説明できる。
- 受け入れ条件がチェックリストとして書かれている。
- 対応する `doc/` の設計書、受入条件、API契約、ADRを参照できる。
- TDDで最初に書くテスト観点が1つ以上ある。
- 正常系と主要な異常系が整理されている。
- 権限、入力検証、データ永続化、外部依存の影響が確認されている。
- DBマイグレーション、初期データ、設定値、環境変数の追加有無が確認されている。
- メール送信、ファイル保存、7-Zip、RabbitMQ、Elasticsearchなどの外部境界の確認方法が決まっている。
- `Priority` が `Unset` ではない。
- `Size` が `Unknown` ではない。
- `Size = 13` の場合は、分割できない理由または分割方針が記録されている。
- 1スプリント内で完了できる大きさに分割されている。

## Definition of Done

PBIを `Status = Done` にする前に、[Definition of Done](../../doc/05_development/05_definition_of_done.md) に加えて次を確認する。

- 受け入れ条件がすべて満たされている。
- 実行したテスト、未実行のテスト、手動確認結果が記録されている。
- TDDログまたは作業記録に、Red / Green / Refactor / Documentの結果が残っている。
- 仕様、設計、API、データモデル、権限、運用手順に影響する変更は `doc/` へ反映されている。
- Projectsの `Status` が `Done` になっている。
- 完了できなかった作業は、別PBIまたは次スプリント候補として分離されている。

## 推奨ビュー

### Product Backlog

用途: バックログ全体の優先順位、Ready状態、見積もりを確認する。

設定:

| 項目 | 推奨 |
| --- | --- |
| Layout | Table |
| Filter | `Ready` が `Drop` ではない |
| Group by | `Ready` |
| Sort | Priority, Size, 手動順 |
| 表示フィールド | Title, System, Priority, Ready, Ready条件/備考, Size, Labels, Sprint |

### Ready Backlog

用途: 次スプリント候補を確認する。

設定:

| 項目 | 推奨 |
| --- | --- |
| Layout | Table |
| Filter | `Ready = Ready` かつ `Sprint = Backlog` |
| Group by | `System` |
| 表示フィールド | Title, System, Priority, Size, Labels, Ready条件/備考 |

### Refinement

用途: Draft / NotReadyを整理する。

設定:

| 項目 | 推奨 |
| --- | --- |
| Layout | Table |
| Filter | `Ready = Draft` または `Ready = NotReady` |
| Group by | `Ready` |
| 表示フィールド | Title, System, Priority, Ready, Ready条件/備考, Size, Labels |

### Current Sprint

用途: 現在スプリントの進捗をカンバンで確認する。

設定:

| 項目 | 推奨 |
| --- | --- |
| Layout | Board |
| Filter | `Sprint = Sx: スプリント名` |
| Group by | `Status` |
| 表示フィールド | Title, System, Priority, Size, Labels |

`Sx: スプリント名` は現在のスプリント値へ置き換える。

### Sprint Plan

用途: スプリント開始前に投入候補と容量を確認する。

設定:

| 項目 | 推奨 |
| --- | --- |
| Layout | Table |
| Filter | `Sprint = Sx: スプリント名` |
| Group by | `System` |
| 表示フィールド | Title, System, Priority, Ready, Size, Status, Labels |

### Done by Sprint

用途: 完了したPBIをスプリント別に振り返る。

設定:

| 項目 | 推奨 |
| --- | --- |
| Layout | Table |
| Filter | `Status = Done` |
| Group by | `Sprint` |
| 表示フィールド | Title, System, Priority, Size, Labels |

## PBI起票手順

1. GitHub Issueを作成する。
2. Issue本文テンプレートに沿って、User Story、背景、受け入れ条件、対象外、TDD観点、関連ドキュメントを書く。
3. Projectへ追加する。
4. `System` を設定する。
5. `Ready` を `Draft` または `NotReady` にする。
6. 必要な `Labels` を付ける。
7. 優先順位未設定の場合は `Priority = Unset` にする。
8. 見積もり前は `Size = Unknown` にする。
9. スプリント未割当の場合は `Sprint = Backlog` にする。

起票直後に無理に `Ready` にしない。受け入れ条件、設計参照、TDD観点が揃ってからReadyにする。

## バックログリファインメント手順

1. `Refinement` ビューを開く。
2. `Draft` のIssueについて、バックログに残すかDropするかを決める。
3. 残すものはIssue本文をテンプレートに沿って整える。
4. `NotReady` のIssueについて、Ready条件を確認する。
5. 必要に応じて `doc/` の設計書、ADR、受入条件を更新する。
6. PBIが大きい場合は、縦に薄く動く単位へ分割する。
7. `Priority` を設定する。
8. `Size` を見積もる。
9. Definition of Readyを満たしたら `Ready = Ready` にする。
10. 採用しないものは `Ready = Drop` にし、理由を `Ready条件/備考` に残す。

分割時は、元Issueに分割先Issueのリンクを残す。分割後の元Issueは、必要に応じて `Drop` または親Issueとして扱う。

## スプリントプランニング手順

1. `Ready Backlog` ビューを開く。
2. スプリントゴールを決める。
3. ゴールに貢献するPBIを選ぶ。
4. 各PBIの `Ready = Ready`、`Priority != Unset`、`Size != Unknown` を確認する。
5. 対象PBIの `Sprint` を現在スプリント値に変更する。
6. 対象PBIの `Status` を `Todo` にする。
7. `Current Sprint` ビューでカンバン表示を確認する。
8. `development/scrum/sprints/sprint-XX/planning.md` を更新する。

スプリント容量は、最初の3スプリントでは厳密に計算しない。完了PBI数、未完了PBI数、Size合計を記録し、4スプリント目以降の参考にする。

## スプリント中の運用

### 作業開始

- 着手するIssueを自分にAssignする。
- `Status = In Progress` にする。
- 必要であればIssue本文またはTDDログに最初のRedを書ける状態にする。

### レビュー待ち

- 実装、テスト、ドキュメント更新が一通り終わったら `Status = Review` にする。
- Pull Requestがある場合はIssueとPRをリンクする。
- 実行したテストと確認内容をPRまたはIssueへ記録する。

### ブロック

- 作業が止まった場合は `Status = Blocked` にする。
- ブロック理由、解除条件、必要な判断をIssueコメントまたは `Ready条件/備考` に書く。
- ブロックが長引く場合は、スプリントスコープから外すか、別PBIへ分割する。

### 完了

- Definition of Doneを確認する。
- Issueの受け入れ条件をチェックする。
- `Status = Done` にする。
- IssueをCloseする。

## スプリントレビュー手順

1. `Current Sprint` ビューで `Done` と未完了を確認する。
2. スプリントゴールを満たしたかを確認する。
3. 完了PBIの動作をデモまたは確認する。
4. 未完了PBIを完了扱いにせず、次スプリント候補へ戻す。
5. 実行したテスト、未実行テスト、残リスクを整理する。
6. `development/scrum/sprints/sprint-XX/review.md` と `test-report.md` を更新する。
7. 必要に応じて `development/scrum/02_product_backlog.md` と `04_sprint_plan.md` を更新する。

## レトロスペクティブ手順

1. `Done by Sprint` ビューで完了実績を見る。
2. 未完了PBI、Blocked、Review停滞の理由を確認する。
3. PBIサイズ、Ready条件、TDDの進め方に問題がなかったかを確認する。
4. 次スプリントで試す改善アクションを1つ以上決める。
5. `development/scrum/sprints/sprint-XX/retrospective.md` に記録する。
6. 改善アクションを次スプリントの `planning.md` へ転記する。

## PBIとドキュメントの整合

GitHub Projectsは運用状況の可視化に使う。仕様の正本はIssueコメントではなく、必要に応じて `doc/` へ反映する。

| 変更内容 | 更新先 |
| --- | --- |
| プロダクト範囲、用語、ロードマップ | `doc/01_product/` |
| エピック、ユーザーストーリー、受入条件 | `doc/02_backlog/` |
| アーキテクチャ判断 | `doc/03_architecture/03_adr/` |
| API、データモデル、権限、検索、変換、ファイル保存 | `doc/04_design/` |
| 開発ルール、ブランチ、環境、DoD | `doc/05_development/` |
| テスト方針、受入テスト、回帰テスト | `doc/06_testing/` |
| 運用手順、監視、バックアップ、リリース | `doc/07_operations/` |
| スクラム運用、PBI計画、スプリント計画 | `development/scrum/` |
| TDD記録、テスト観点 | `development/tdd/` |

Issueで仕様判断が発生した場合は、Issueコメントだけで完結させない。該当する設計書またはADRへ反映し、Issueからリンクする。

## 初期セットアップチェックリスト

- [x] Projectを作成した。
- [x] `System` フィールドを作成した。
- [x] `Ready` フィールドを作成した。
- [x] `Ready条件/備考` フィールドを作成した。
- [x] `Size` フィールドを作成した。
- [x] `Priority` フィールドを作成した。
- [x] `Sprint` フィールドを作成した。
- [x] `Status` フィールドを作成した。
- [x] 推奨ラベルを作成した。
- [x] `Product Backlog` ビューを作成した。
- [x] `Ready Backlog` ビューを作成した。
- [x] `Refinement` ビューを作成した。
- [x] `Current Sprint` ビューを作成した。
- [x] `Sprint Plan` ビューを作成した。
- [x] `Done by Sprint` ビューを作成した。
- [x] 既存のPBI候補をIssueとして登録した。
- [x] 各IssueをProjectへ追加した。
- [x] 初回スプリント対象のPBIをReadyにした。

## 運用上の注意

- Projectのフィールドを増やしすぎない。新しいフィールドは、ビューや判断に継続して使う場合だけ追加する。
- `Ready` と `Status` を混同しない。`Ready` はスプリント投入前の状態、`Status` はスプリント中の作業状態を表す。
- `Sprint` は履歴なので、完了後に別スプリントへ書き換えない。
- `Drop` は削除ではなく、判断履歴として残す。
- 大きいPBIは、技術レイヤー別ではなく、利用者価値または技術リスクが確認できる縦切りで分割する。
- セキュリティ、認可、ファイル処理、外部プロセス、検索、キューに関わるPBIは、受入条件と異常系をReady前に明確にする。
- スプリント中に設計変更が確定した場合は、Issueだけでなく `doc/` を更新する。
