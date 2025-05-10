# Spring for GraphQL TIPS 〜肥大化するGraphQLスキーマへの対処〜

## はじめに

GraphQLのスキーマ定義は、プロダクトの成長に伴い、肥大化していくことは避けられません。  

ここでは、肥大化するスキーマ定義への工夫を2つ紹介します。

なお、この記事は[JJUG CCC 2025 for Spring](https://jjug-ccc-2025-spring.sessionize.com/session/872868)のセッションで公開した[リファレンス実装](https://github.com/kogayushi/spring-for-graphql-tips-by-small-startup)で触れられなかった肥大化するGraphQLスキーマへの対処の解説記事です。



## ファイル分割でスキーマを整理する
### なぜ分割するのか

1ファイルで管理するのはすぐに限界がきますが、分割することでノードごとに1つのファイルにグループ化できます。  
それにより更新対象のスキーマ定義ファイルを見つけやすくなります。

### どうやって分割するか

`resources/graphql`ディレクトリ以下に、ノードごとにスキーマ定義ファイルを分けます。

| ファイル名 | 説明 |
| --- | --- |
| schema.graphqls | 空のQuery, Mutation, Subscriptionだけ定義 |
| article.graphqls | 記事関連の定義 |
| comment.graphqls | コメント関連 |
| user.graphqls | ユーザー関連 |

それぞれ、`extend type Query`や`extend type Mutation`を使ってルート型を拡張しています。

たとえば、`article.graphqls`は以下のようになります。

```graphql
extend type Query {
  articles: [Article!]!
}

extend type Mutation {
  postArticle(input: ArticleInput!): Article!
  editArticle(input: EditArticleInput!): Article!
  likeArticle(input: LikeArticleInput!): Void
  unlikeArticle(input: UnlikeArticleInput!): Void
}

extend type Subscription {
  updatedArticles(articleIds: [ID!]!): Article!
}
```

Spring for GraphQLは、これらを全部読み込んで一つのスキーマとして統合してくれるので、このような分割が可能です。

## グラフ構造の全体像を把握できるように可視化する

スキーマが大きくなってくると、全体像が見えなくなってきます。  
「これとこれがどう繋がってるんだっけ？」みたいなことが起きがちです。  
そのため、スキーマを図にして可視化することを考えました。

手段はいろいろあると思いますが、今回はGraphQL Voyagerを使った例を紹介します。

### Voyagerの導入方法
| ファイル名 | 説明 |
| --- | --- |
| src/main/resources/templates/graphql-voyager.html | VoyagerのHTMLテンプレート |
| GraphQLVoyagerController.kt | `/graphql-voyager`にマッピングしてHTML返すコントローラー |

アクセスすると、テンプレート内のJSがGraphQLエンドポイントにイントロスペクションを投げて、 スキーマの構造がそのまま図として見えるようになります。ノードやフィールドをクリックしてたどれるので、構造の確認がかなりラクになります。

```javascript
// graphql-voyager.html の一部
const { voyagerIntrospectionQuery: query } = GraphQLVoyager;
const response = await fetch('http://localhost:8080/graphql', {
  method: 'post',
  headers: {
    Accept: 'application/json',
    'Content-Type': 'application/json',
  },
  body: JSON.stringify({ query }),
});
const introspection = await response.json();

GraphQLVoyager.renderVoyager(document.getElementById('voyager'), {
  introspection,
});
```

たったこれだけで可視化できます。  
サーバー上で動かす場合は、取得先のURLを適切に変更してください。

## まとめ

プロダクトの成長に伴い、スキーマが肥大化していくことはどうしても避けられません。  
ですので、肥大化したスキーマをどう扱いやすくするかを考えて対処するしかないでしょう。  
他にもいろいろな工夫があると思いますので、これ以外の方法についてご存知の方はぜひ教えてください。
