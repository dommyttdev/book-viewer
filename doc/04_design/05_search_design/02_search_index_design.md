# Elasticsearchインデックス設計

## 目的

このドキュメントは、自炊本閲覧Webアプリケーションの検索用Elasticsearchインデックスを設計する。

対象は、index name、alias、mapping、analyzer、ICU normalizer、edge n-gram系フィールド、検索対象、ソート対象、boost、更新タイミング、再インデックス方針である。

## 前提

- PostgreSQLを正本データストアとする。
- Elasticsearchは検索用の派生インデックスであり、PostgreSQLから再構築可能とする。
- Elasticsearchの必須プラグインは [技術スタックのElasticsearch必須プラグイン](../../03_architecture/02_technology_stack.md#elasticsearch必須プラグイン) を正本とする。
- この設計は、技術スタックで定義された必須プラグインが導入済みであることを前提にする。
- クライアントからElasticsearch Query DSLを直接受け取らない。
- 物理ファイルパスや内部ストレージキーは検索結果へ露出しない。

## インデックス名とエイリアス

| 種別 | 名前 | 用途 |
| --- | --- | --- |
| write alias | `books_search_write` | 通常更新と書籍単位再インデックスの書き込み先。 |
| read alias | `books_search_read` | 検索APIの読み取り先。 |
| physical index | `books_search_v1_yyyyMMddHHmmss` | 実体インデックス。mapping変更や全件再構築時に新規作成する。 |

初期実装では単一インデックスに書籍検索ドキュメントを格納する。mapping変更を伴う全件再インデックスでは、新しいphysical indexを作成し、投入完了後にread aliasとwrite aliasを切り替える。

## ドキュメント単位

Elasticsearchの1ドキュメントは1書籍を表す。

書籍に紐づく著者、タグ、シリーズ、種別、サムネイル参照、表示状態は、検索結果表示と検索条件に必要な範囲で書籍ドキュメントへ非正規化する。

PostgreSQLの正本更新時は、差分更新だけに依存せず、対象書籍の最新状態から検索ドキュメントを再生成できるようにする。

## ドキュメント構造

```json
{
  "bookId": "book_...",
  "title": "サンプル本",
  "description": "説明文",
  "bookType": {
    "id": "type_...",
    "code": "comic",
    "name": "単行本",
    "displayOrder": 10
  },
  "series": {
    "id": "series_...",
    "name": "サンプルシリーズ",
    "nameKana": "さんぷるしりーず",
    "description": "シリーズ概要"
  },
  "seriesOrder": 1,
  "authors": [
    {
      "id": "author_...",
      "name": "山田太郎",
      "nameKana": "やまだたろう",
      "role": "author",
      "displayOrder": 1
    }
  ],
  "tags": [
    {
      "id": "tag_...",
      "name": "日常"
    }
  ],
  "visibilityStatus": "available",
  "conversionStatus": "completed",
  "pageCount": 120,
  "thumbnailBookFileId": "file_...",
  "createdAt": "2026-05-08T10:00:00Z",
  "updatedAt": "2026-05-08T10:00:00Z",
  "deleted": false
}
```

## analyzerとnormalizer

### 方針

| 名前 | 種別 | 用途 |
| --- | --- | --- |
| `ja_search_analyzer` | analyzer | 通常検索用。kuromojiを使い、日本語テキストを検索しやすくする。 |
| `ja_index_analyzer` | analyzer | インデックス時の日本語解析用。検索時と同じ正規化方針にする。 |
| `ja_edge_ngram_analyzer` | analyzer | 補完、前方一致、部分一致候補用。 |
| `keyword_normalizer` | normalizer | 完全一致、ソート、集約用keywordフィールドの正規化。 |

### settings案

```json
{
  "settings": {
    "analysis": {
      "filter": {
        "icu_normalize_filter": {
          "type": "icu_normalizer",
          "name": "nfkc",
          "mode": "compose"
        },
        "ja_pos_filter": {
          "type": "kuromoji_part_of_speech",
          "stoptags": [
            "助詞-格助詞-一般",
            "助詞-終助詞"
          ]
        },
        "ja_readingform": {
          "type": "kuromoji_readingform",
          "use_romaji": false
        },
        "ja_baseform": {
          "type": "kuromoji_baseform"
        },
        "edge_ngram_filter": {
          "type": "edge_ngram",
          "min_gram": 1,
          "max_gram": 20
        }
      },
      "analyzer": {
        "ja_index_analyzer": {
          "type": "custom",
          "tokenizer": "kuromoji_tokenizer",
          "filter": ["icu_normalize_filter", "ja_baseform", "ja_pos_filter", "lowercase"]
        },
        "ja_search_analyzer": {
          "type": "custom",
          "tokenizer": "kuromoji_tokenizer",
          "filter": ["icu_normalize_filter", "ja_baseform", "ja_pos_filter", "lowercase"]
        },
        "ja_edge_ngram_analyzer": {
          "type": "custom",
          "tokenizer": "kuromoji_tokenizer",
          "filter": ["icu_normalize_filter", "ja_baseform", "lowercase", "edge_ngram_filter"]
        }
      },
      "normalizer": {
        "keyword_normalizer": {
          "type": "custom",
          "filter": ["icu_normalize_filter", "lowercase"]
        }
      }
    }
  }
}
```

実装時にElasticsearchのバージョンやプラグイン制約でfilter名が変わる場合は、同等の正規化、kuromoji解析、edge n-gramを維持する。`icu_normalizer`が利用できない環境ではインデックス定義を作成しない。

### 必須プラグイン確認

インデックス作成前またはバックエンドAPI起動時に、Elasticsearchの`_nodes/plugins`または同等のAPIで、[技術スタックで定義された必須プラグイン](../../03_architecture/02_technology_stack.md#elasticsearch必須プラグイン) が導入済みであることを確認する。

不足がある場合は、検索インデックス作成を失敗させる。失敗時はログと管理向け状態に不足プラグイン名を記録し、RunbookのElasticsearch必須プラグイン復旧手順に従って環境を修正してから再実行する。

## mapping

```json
{
  "mappings": {
    "dynamic": "strict",
    "properties": {
      "bookId": { "type": "keyword" },
      "title": {
        "type": "text",
        "analyzer": "ja_index_analyzer",
        "search_analyzer": "ja_search_analyzer",
        "fields": {
          "keyword": { "type": "keyword", "normalizer": "keyword_normalizer" },
          "suggest": {
            "type": "text",
            "analyzer": "ja_edge_ngram_analyzer",
            "search_analyzer": "ja_search_analyzer"
          }
        }
      },
      "description": {
        "type": "text",
        "analyzer": "ja_index_analyzer",
        "search_analyzer": "ja_search_analyzer"
      },
      "bookType": {
        "properties": {
          "id": { "type": "keyword" },
          "code": { "type": "keyword", "normalizer": "keyword_normalizer" },
          "name": {
            "type": "text",
            "analyzer": "ja_index_analyzer",
            "search_analyzer": "ja_search_analyzer",
            "fields": {
              "keyword": { "type": "keyword", "normalizer": "keyword_normalizer" }
            }
          },
          "displayOrder": { "type": "integer" }
        }
      },
      "series": {
        "properties": {
          "id": { "type": "keyword" },
          "name": {
            "type": "text",
            "analyzer": "ja_index_analyzer",
            "search_analyzer": "ja_search_analyzer",
            "fields": {
              "keyword": { "type": "keyword", "normalizer": "keyword_normalizer" },
              "suggest": {
                "type": "text",
                "analyzer": "ja_edge_ngram_analyzer",
                "search_analyzer": "ja_search_analyzer"
              }
            }
          },
          "nameKana": {
            "type": "text",
            "analyzer": "ja_index_analyzer",
            "search_analyzer": "ja_search_analyzer",
            "fields": {
              "keyword": { "type": "keyword", "normalizer": "keyword_normalizer" }
            }
          },
          "description": {
            "type": "text",
            "analyzer": "ja_index_analyzer",
            "search_analyzer": "ja_search_analyzer"
          }
        }
      },
      "seriesOrder": { "type": "integer" },
      "authors": {
        "type": "nested",
        "properties": {
          "id": { "type": "keyword" },
          "name": {
            "type": "text",
            "analyzer": "ja_index_analyzer",
            "search_analyzer": "ja_search_analyzer",
            "fields": {
              "keyword": { "type": "keyword", "normalizer": "keyword_normalizer" },
              "suggest": {
                "type": "text",
                "analyzer": "ja_edge_ngram_analyzer",
                "search_analyzer": "ja_search_analyzer"
              }
            }
          },
          "nameKana": {
            "type": "text",
            "analyzer": "ja_index_analyzer",
            "search_analyzer": "ja_search_analyzer",
            "fields": {
              "keyword": { "type": "keyword", "normalizer": "keyword_normalizer" }
            }
          },
          "role": { "type": "keyword", "normalizer": "keyword_normalizer" },
          "displayOrder": { "type": "integer" }
        }
      },
      "tags": {
        "type": "nested",
        "properties": {
          "id": { "type": "keyword" },
          "name": {
            "type": "text",
            "analyzer": "ja_index_analyzer",
            "search_analyzer": "ja_search_analyzer",
            "fields": {
              "keyword": { "type": "keyword", "normalizer": "keyword_normalizer" },
              "suggest": {
                "type": "text",
                "analyzer": "ja_edge_ngram_analyzer",
                "search_analyzer": "ja_search_analyzer"
              }
            }
          }
        }
      },
      "visibilityStatus": { "type": "keyword", "normalizer": "keyword_normalizer" },
      "conversionStatus": { "type": "keyword", "normalizer": "keyword_normalizer" },
      "pageCount": { "type": "integer" },
      "thumbnailBookFileId": { "type": "keyword" },
      "createdAt": { "type": "date" },
      "updatedAt": { "type": "date" },
      "deleted": { "type": "boolean" }
    }
  }
}
```

## 検索対象フィールド

| 用途 | フィールド | 方針 |
| --- | --- | --- |
| キーワード検索 | `title`, `authors.name`, `tags.name`, `series.name`, `bookType.name`, `series.description`, `description` | `multi_match`を基本にする。nested項目はnested queryで扱う。 |
| タイトル検索 | `title`, `title.suggest`, `title.keyword` | 完全一致、通常検索、補完候補を分ける。 |
| 著者検索 | `authors.name`, `authors.nameKana`, `authors.name.suggest`, `authors.id` | 著者ID指定は完全一致、文字列指定はnested queryで扱う。 |
| タグ検索 | `tags.id`, `tags.name`, `tags.name.suggest` | タグID絞り込みはnested queryのAND条件を初期方針にする。 |
| シリーズ検索 | `series.id`, `series.name`, `series.nameKana`, `series.description`, `series.name.suggest` | シリーズIDは完全一致、シリーズ概要は低いboostで扱う。 |
| 種別検索 | `bookType.code`, `bookType.name` | 種別コードはfilter、種別名は補助的なキーワード検索対象にする。 |
| 表示可否 | `visibilityStatus`, `conversionStatus`, `deleted` | 一般向け検索ではfilterで閲覧可能な書籍だけに絞る。 |

## ソート対象フィールド

| API sort | Elasticsearch field | 方針 |
| --- | --- | --- |
| `relevance.desc` | `_score` | キーワードありの既定。 |
| `updatedAt.desc` | `updatedAt` | キーワードなしの既定。 |
| `createdAt.desc` | `createdAt` | 新着順。 |
| `title.asc` | `title.keyword` | 正規化済みkeywordで昇順。 |
| `series.asc` | `series.name.keyword`, `seriesOrder`, `title.keyword` | シリーズ名、シリーズ内順、タイトルの順で安定させる。 |

未定義のソート項目はAPI層で拒否する。深いページングが問題になった場合は、安定したソートキーを使って`search_after`を検討する。

## boost設定

初期boostは次の値を候補とする。検索ログや受入テストの結果に応じて調整する。

| フィールド | boost | 理由 |
| --- | --- | --- |
| `title.keyword` | 12 | タイトル完全一致を最優先する。 |
| `title` | 8 | タイトルの通常一致を主検索対象にする。 |
| `title.suggest` | 5 | 入力途中や前方一致を補助する。 |
| `authors.name` | 6 | 著者一致はタイトルに次ぐ主要条件にする。 |
| `authors.nameKana` | 5 | 読み検索を著者検索の主要補助にする。 |
| `tags.name` | 4 | 分類一致を結果に反映する。 |
| `series.name` | 4 | シリーズ一致を結果に反映する。 |
| `series.nameKana` | 3 | シリーズ読み検索を補助する。 |
| `bookType.name` | 2 | 種別名一致は補助に留める。 |
| `series.description` | 1 | 説明文一致だけで上位になりすぎないようにする。 |
| `description` | 1 | 書籍説明は補助検索対象にする。 |

一般向け検索では、boostよりも先に`deleted=false`、`visibilityStatus=available`、`conversionStatus=completed`をfilterで適用する。

## 更新タイミング

| 更新契機 | 処理 |
| --- | --- |
| 書籍作成 | PostgreSQL登録後、書籍ドキュメントを生成してwrite aliasへ登録する。 |
| 書籍メタ情報更新 | 対象書籍の最新正本からドキュメントを再生成して更新する。 |
| 著者更新 | 対象著者に紐づく書籍を再インデックスする。 |
| タグ更新 | 対象タグに紐づく書籍を再インデックスする。 |
| シリーズ更新 | 対象シリーズに紐づく書籍を再インデックスする。 |
| 種別更新 | 対象種別に紐づく書籍を再インデックスする。 |
| 変換完了 | `conversionStatus`、`pageCount`、`thumbnailBookFileId`、`visibilityStatus`を更新する。 |
| 書籍非表示 | 一般向け検索から除外されるように`visibilityStatus`を更新する。 |
| 書籍論理削除 | `deleted=true`へ更新するか、ドキュメントを削除する。全件再構築時は除外できるようにする。 |

Elasticsearch更新失敗時はPostgreSQLの検索インデックス更新状態へ失敗を記録し、再試行キューまたは管理コマンドで最新正本から再生成する。

## 書籍単位再インデックス

書籍単位再インデックスは、特定書籍の検索結果、サムネイル参照、表示状態、関連メタ情報が疑わしい場合に実行する。

手順方針は次のとおり。

1. 対象書籍IDを受け取る。
2. PostgreSQLから書籍、著者、タグ、シリーズ、種別、変換状態、サムネイル参照を読み直す。
3. 論理削除済みまたは検索対象外の場合は、Elasticsearch上の該当ドキュメントを削除または検索対象外状態へ更新する。
4. 検索対象の場合は、最新正本から1書籍ドキュメントを生成する。
5. write aliasへupsertする。
6. 処理結果、失敗理由、再試行可否をログとPostgreSQLの検索インデックス更新状態へ記録する。

実装時の管理コマンド候補は次のとおり。

```bash
docker compose exec api ./app-admin search reindex-book --book-id <book-id>
```

## 全件再インデックス

全件再インデックスは、mapping変更、analyzer変更、Elasticsearch障害、検索結果の広範な不整合がある場合に実行する。

手順方針は次のとおり。

1. 新しいphysical indexを作成する。
2. PostgreSQLから検索対象書籍をページングしながら読み取る。
3. 書籍ごとに最新正本から検索ドキュメントを生成する。
4. bulk APIで新しいphysical indexへ投入する。
5. 件数、失敗件数、スキップ件数を確認する。
6. 主要な検索条件で検証する。
7. 問題がなければread aliasとwrite aliasを新しいphysical indexへ切り替える。
8. 旧physical indexは一定期間保持し、問題がなければ削除する。

実装時の管理コマンド候補は次のとおり。

```bash
docker compose exec api ./app-admin search reindex-all
```

alias切り替えは可能な限りatomicに実行する。切り替えに失敗した場合は、read aliasを旧インデックスへ戻し、検索APIの読み取り先を不整合な新インデックスに向けない。

## セキュリティと運用上の注意

- 検索ドキュメントに秘密情報、パスワード、トークン、内部物理パスを含めない。
- 一般ユーザ向け検索では閲覧可能状態をfilterで制御し、必要に応じてPostgreSQLで正本確認する。
- 管理向け検索条件は管理ユーザの権限に応じて許可する。
- 検索キーワード、ページサイズ、ソート、ID条件はAPI層で検証する。
- 更新失敗理由には、内部パスや不要な個人情報を残さない。
- Elasticsearchは正本ではないため、障害時はPostgreSQLから再構築する。

## 更新方針

index name、mapping、analyzer、boost、検索対象、更新タイミング、再インデックス方式が変わった場合は、このドキュメントを更新する。

API契約、データモデル、Runbook、受入テストに影響する場合は、関連ドキュメントも同じ変更で更新する。
