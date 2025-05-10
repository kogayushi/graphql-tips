# Spring for GraphQL TIPS〜部分更新したい〜

## はじめに

ブログを想像してください。
あなたは、ブログの記事タイトルだけを更新したいとします。  
でもAPIの仕様によっては、タイトルだけじゃなく本文とかも全部送らないといけないことがありえます。
「変えたいのはタイトルだけなのに他のフィールドも送らなきゃなのはナンセンス」って考えたりしませんか？

また他にも新しいフィールドが追加されたときに生じる問題もあります。  
たとえば、予約投稿のために`scheduledPublishDate`というフィールドを追加したとします。  
古いクライアントはその存在を知らないので送ってきません。  
でもサーバーが「フィールドが送られてこなかった＝nullで更新」と解釈すると、  
意図せず`scheduledPublishDate`の値が消えるという事故が起きます。

これを防ぐには、「送られてきたフィールドだけを更新する」ための部分更新の仕組みが必要です。  
つまり、RESTでいうPATCH相当のことをGraphQLでもやりたい、という話です。

なお、この記事は[JJUG CCC 2025 for Spring](https://jjug-ccc-2025-spring.sessionize.com/session/872868)のセッションで公開した[リファレンス実装](https://github.com/kogayushi/spring-for-graphql-tips-by-small-startup)で触れられなかった部分更新についての解説記事です。

## Spring for GraphQLでどうやるのか

GraphQLでは、更新対象外のフィールドをundefinedで表現する事が可能です。  
ここで重要なのは、サーバー側がundefinedを識別して、「undefinedのフィールドは変更しない」ように実装することです。


### `ArgumentValue` で`null`と`undefined`を区別する

Spring GraphQLの`ArgumentValue<T>`を使うことで実現できます。  
このクラスを使うことで以下の3つの状態を区別可能になります。

| 状態        | 説明            | 処理    |
|-----------|---------------|-------|
| undefined | リクエストに含まれていない | 更新対象外 |
| null      | 明示的に送られた      | 値を削除  |
| 値         | リクエストに含まれている  | 値を更新  |

このように分けて扱えるようにすることで、更新・削除・対象顔のフィールドを明示的に区別できます。

### Inputの定義

記事編集用のInputはこのようになります。

```kotlin
data class EditArticleInput(
    @field:NotNull
    val articleId: UUID?,

    val title: ArgumentValue<
        @NotBlank
        @Size(min = 3, max = 100)
        String
    >,

    val content: ArgumentValue<
        @NotBlank
        @Size(min = 10, max = 5000)
        String
    >,

    val scheduledPublishDate: ArgumentValue<LocalDateTime?>
) {
    val articleIdAsNotNull by lazy { articleId!! }
}
```

各フィールドを ArgumentValue<T> でラップすることで、「送られてきたか？」「nullか？」「値があるか？」を区別可能になります。


### Beanバリデーション

バリデーションもちゃんと効きます。
たとえば`@NotBlank`や`@Size`を`ArgumentValue`のジェネリクスの中に書いておけば、 値が送られてきたときだけバリデーションされるようになります。

undefinedのときはバリデーションはスキップされます。

## まとめ

`ArgumentValue`を使うと、Spring for GraphQLでも以下のことが実現できました。

- 更新したいフィールドだけ送る
- 送られてこなかったフィールド(undefined)はそのまま保持
- nullのときは削除する
