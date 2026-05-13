# エピック: 本の閲覧

## 目的

一般ユーザが登録済みの自炊本を一覧や詳細から開き、ブラウザ上のビューアで読み、読みかけ位置やお気に入りから再閲覧できるようにする。

このエピックでは、本一覧、ビューア表示、ビューア操作、閲覧履歴、お気に入りを扱う。

## 対象利用者

- 一般ユーザ
- 管理ユーザ

管理ユーザも書籍参照や動作確認のために閲覧できるが、主な利用者は一般ユーザとする。

## 範囲

### MVP

- 一般ユーザが本一覧から閲覧可能な書籍を見つけられる。
- 一般ユーザがビューアでページを表示できる。
- 一般ユーザが次ページ、前ページへ移動できる。
- 一般ユーザの読みかけ位置を保存できる。
- 一般ユーザが本をお気に入り登録、解除できる。
- 一般ユーザがお気に入り一覧から本を再閲覧できる。
- 同じ本のお気に入り重複登録を防止する。
- 変換済みで閲覧可能な書籍だけを閲覧対象にする。

### Beta / v1.0

- 見開き表示を利用できる。
- ページ番号指定、最初のページ、最後のページへの移動ができる。
- 拡大縮小を利用できる。
- キーボード操作とスマートフォン操作を改善する。
- 検索結果、閲覧履歴、お気に入りからの再閲覧体験をそろえる。

### 対象外

- オフライン閲覧。
- 読書進捗の詳細分析。
- お気に入り共有。
- メモや分類付きのお気に入り。

## 主な成果物

- [doc/02_backlog/02_user_stories/03_book_list.md](../02_user_stories/03_book_list.md)
- [doc/02_backlog/02_user_stories/04_book_viewing.md](../02_user_stories/04_book_viewing.md)
- [doc/02_backlog/02_user_stories/05_favorite.md](../02_user_stories/05_favorite.md)
- [doc/02_backlog/03_acceptance_criteria/02_favorite.md](../03_acceptance_criteria/02_favorite.md)
- [doc/04_design/01_ui_flows.md](../../04_design/01_ui_flows.md) へのビューア操作仕様の反映
- [doc/04_design/03_api_contracts/05_viewer_api.md](../../04_design/03_api_contracts/05_viewer_api.md)
- [doc/06_testing/02_acceptance_tests/04_viewer_acceptance_tests.md](../../06_testing/02_acceptance_tests/04_viewer_acceptance_tests.md)

## 完了の目安

- 一般ユーザが本一覧または検索結果から本を開き、ページを閲覧できる。
- 読みかけ位置を保存し、再開できる。
- お気に入り登録、解除、一覧表示、重複防止が受入条件として整理されている。
- スマートフォン閲覧を前提に、画像表示と操作の基本方針が画面仕様に反映されている。

## 関連ドキュメント

- [doc/01_product/04_user_story_map.md](../../01_product/04_user_story_map.md)
- [doc/01_product/02_product_roadmap.md](../../01_product/02_product_roadmap.md)
- [doc/04_design/04_data_model.md](../../04_design/04_data_model.md)
- [doc/04_design/08_authorization_design/01_authorization_design.md](../../04_design/08_authorization_design/01_authorization_design.md)

