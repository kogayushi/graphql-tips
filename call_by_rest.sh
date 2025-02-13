#!/bin/bash

# APIのベースURL
BASE_URL="http://localhost:8080/api"

# 1. 記事リストの取得
ARTICLES_JSON=$(curl -s "$BASE_URL/articles")
if [ -z "$ARTICLES_JSON" ] || [ "$ARTICLES_JSON" == "[]" ] || [ "$ARTICLES_JSON" == "null" ]; then
  echo "Error: No articles retrieved from API"
  exit 1
fi

# 2. 記事の ID を取得し、カンマ区切りで結合
ARTICLE_IDS=$(echo "$ARTICLES_JSON" | jq -r 'map(.id) | join(",")')

# 3. 記事ごとのコメントを取得
COMMENTS_JSON=$(curl -s "$BASE_URL/articles/$ARTICLE_IDS/comments")
if [ -z "$COMMENTS_JSON" ] || [ "$COMMENTS_JSON" == "null" ]; then
  COMMENTS_JSON="[]"
fi

# 4. ユーザーIDを抽出 (記事の著者 + コメントの著者 + いいねした人)
USER_IDS=$(jq -n \
  --argjson articles "$ARTICLES_JSON" \
  --argjson comments "$COMMENTS_JSON" \
  '[ $articles[].authorId ] + [ $articles[].likedBy[]? ] + [ $comments[].authorId ] | unique | join(",")' \
  -r
)

# 5. ユーザー情報を取得 (空ならリクエストしない)
if [ -z "$USER_IDS" ]; then
  USERS_JSON="[]"
else
  USERS_JSON=$(curl -s "$BASE_URL/users/$USER_IDS")
fi

# 6. データを結合し、GraphQL 風の JSON に整形 (`likedBy` を削除)
FINAL_JSON=$(jq -n \
  --argjson articles "$ARTICLES_JSON" \
  --argjson comments "$COMMENTS_JSON" \
  --argjson users "$USERS_JSON" \
  '{
    articles: ($articles | map(
      . as $article
      | {
        id: .id,
        title: .title,
        content: .content,
        author: ($users[] | select(.id == $article.authorId)),
        comments: ($comments | map(select(.articleId == $article.id)) | map(
          . as $comment | . + { author: ($users[] | select(.id == $comment.authorId)) }
        )),
        like: {
          whomBy: [$users[] | select(.id as $id | any($article.likedBy[]?; . == $id))]
        }
      }
    ))
  }')

# 8. 整形して出力
echo "$FINAL_JSON" | jq .