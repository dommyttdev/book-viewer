# Sprint S0 レビュー

## スプリント

- Sprint: S0
- スプリントゴール: 後続PBIをTDDで継続実装できるよう、フロントエンド、API、Worker、ローカルミドルウェアの最小基盤を構築するための作業順序と完了判定を明確にする。
- レビュー日: 2026-05-15

## 完成した成果

| PBI | 結果 | デモ内容 | 受入判断 |
| --- | --- | --- | --- |
| PBI-001 | 実装前のスプリント成果物を作成。 | issue #48の受入条件、sub-issue #81から#88、TDD開始観点、未実行テスト、残リスクを文書化した。 | 実装は未実施のためPBI完了ではない。着手可能性を高めるためのプランニング完了として扱う。 |
| #81 | プロジェクト構成とビルド方針を決定し、`doc/` へ反映。 | `issue-81-project-structure.md` の決定事項を `doc/05_development/00_project_structure.md` に正本として記録し、README、環境構築手順、ローカル開発手順から参照できるようにした。 | 設計判断とドキュメント化は完了。GitHub Issue #81のチェックリストはProject運用上の更新タイミングで反映する。 |

## テストと確認

| 種別 | 内容 | 結果 |
| --- | --- | --- |
| 単体テスト | 未実行。 | 実装を行わない依頼のため対象外。 |
| 結合テスト | 未実行。 | Docker Composeとアプリケーション構成未作成のため対象外。 |
| E2E | 未実行。 | Sprint S0では基盤起動確認が主対象。 |
| 手動確認 | Java、Node.js、Docker、Docker Compose、npm、Gradle、issue #48、development配下の運用ドキュメントを確認。 | 結果は `test-report.md` に記録。 |

## 未完了事項

| 項目 | 理由 | 次の扱い |
| --- | --- | --- |
| #81のGitHub Issueチェックリスト更新 | GitHub Project運用上の書き込みは今回未実施。 | Project運用タイミングで、README / ローカル開発手順への反映済みとして更新する。 |
| #82 Next.js最小構成 | 実装未実施。 | #81の構成決定後に実施する。 |
| #83 Spring Boot API最小構成 | 実装未実施。 | APIヘルスチェックのRedから開始する。 |
| #84 Spring Boot Worker最小構成 | 実装未実施。 | APIとバックエンド構成確定後に実施する。 |
| #85 Docker Composeミドルウェア | 実装未実施。 | PostgreSQL、Elasticsearch、RabbitMQの起動とログ確認を実施する。 |
| #86 ローカル設定と疎通確認 | 実装未実施。 | #83から#85の成果に合わせて実施する。 |
| #87 最小テストと確認コマンド | 完了。 | `issue-87-minimal-test-commands.md` に確認コマンドを固定し、フロントエンド `typecheck` scriptを追加した。 |
| #88 開発手順とTODO更新 | 実構成未確定。 | 実装結果に合わせて `doc/` とissueチェックリストを更新する。 |

## フィードバック

- 親Issue #48は完了条件が広いため、sub-issue単位で順序づけるのが妥当である。
- Sprint S0で検索、変換、ジョブ配送の詳細へ踏み込みすぎると後続PBIと重複する。S0は起動、設定、疎通確認、ログ安全性に集中する。
- 実装時は、最初にAPI起動テストまたはヘルスチェックをRedとして書くことで、TDD運用をS0から定着させる。
- 設計判断を先行するsub-issueでは、[Definition of Done](../../../../doc/05_development/05_definition_of_done.md#設計判断を先行するsub-issueの扱い) に従い、「設計判断完了」と「Issue完了」を分けて扱う。

## ドキュメント更新

- 更新した設計書:
  - `README.md`
  - `doc/05_development/00_project_structure.md`
  - `doc/05_development/03_environment_setup.md`
  - `doc/05_development/04_local_development.md`
  - `doc/05_development/05_definition_of_done.md`
- 更新した開発成果物:
  - `development/scrum/sprints/sprint-s0/planning.md`
  - `development/scrum/sprints/sprint-s0/issue-81-project-structure.md`
  - `development/scrum/sprints/sprint-s0/issue-87-minimal-test-commands.md`
  - `development/scrum/sprints/sprint-s0/pbi-001-breakdown.md`
  - `development/scrum/sprints/sprint-s0/test-report.md`
  - `development/scrum/sprints/sprint-s0/review.md`
  - `development/scrum/sprints/sprint-s0/retrospective.md`
