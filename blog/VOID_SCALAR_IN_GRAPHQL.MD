# Spring for GraphQL TIPS〜Mutationの戻り値の型をVoidにする〜
## はじめに
GraphQLでは、Mutationの戻り値として何も返す必要がない場合があります。例えばいいねやパスワードリセットなどの操作では、操作の成功/失敗は重要ですが、具体的なデータを返す必要はありません。このような場合に役立つのが「Void」スカラー型です。

なお、この記事は[JJUG CCC 2025 for Spring](https://jjug-ccc-2025-spring.sessionize.com/session/872868)のセッションで公開した[リファレンス実装](https://github.com/kogayushi/spring-for-graphql-tips-by-small-startup)で触れられなかったMutationの戻り値の型をVoidにすることについての解説記事です。

## Voidスカラー型とは

GraphQLの仕様では、すべてのフィールドは何らかの値を返す必要があります。ただし、仕様上「常にnullを返す」ための型を定義する手段は存在しません。これは、GraphQLではnullは「値」であり「型」ではないためです。したがって、「何も返さない」ことをスキーマ上で表現したい場合でも、通常のスカラー型やオブジェクト型では代用できず、型としてのVoidを標準で持ちません。

こうした背景から、例えば「いいね」やパスワードリセットのように具体的な返却データが不要な操作においても、GraphQLではスキーマ上の整合性を保つために何らかの型を割り当てる必要があります。そこで有効なのが、常にnullを返すよう設計されたカスタムスカラー型「Void」です。Void型を使うことで、「このMutationはデータを返さない」ことを明確かつ仕様に準拠した形で表現できます。

## Voidスカラー型の実装方法

このアプリケーションでは、`VoidScalar`クラスを使用してVoidスカラー型を実装しています。

```kotlin
object VoidScalar {
    val INSTANCE: GraphQLScalarType =
        GraphQLScalarType.newScalar().name("Void")
            .description("represents NULL values")
            .coercing(VoidCoercing())
            .build()

    private class VoidCoercing : Coercing<Unit, Any?> {
        @Throws(CoercingSerializeException::class)
        override fun serialize(
            dataFetcherResult: Any,
            graphQLContext: GraphQLContext,
            locale: Locale,
        ): Any? {
            return null
        }

        @Throws(CoercingParseValueException::class)
        override fun parseValue(
            input: Any,
            context: GraphQLContext,
            locale: Locale,
        ) {
        }

        @Throws(CoercingParseLiteralException::class)
        override fun parseLiteral(
            input: Value<*>,
            variables: CoercedVariables,
            graphQLContext: GraphQLContext,
            locale: Locale,
        ) {
        }
    }
}
```

このクラスの特徴は以下の通りです：

1. `GraphQLScalarType`のインスタンスを作成し、名前を「Void」に設定
2. `VoidCoercing`クラスを実装して、常に`null`を返すようにオーバーライド
3. `serialize`メソッドは常に`null`を返す
4. `parseValue`と`parseLiteral`メソッドは空の実装（入力値としてVoidは使用しないため）

## スカラー型の登録

カスタムスカラー型を使用するには、GraphQLスキーマに登録する必要があります。このアプリケーションでは、Spring GraphQLの`RuntimeWiringConfigurer`を使用して登録しています：

```kotlin
@Configuration
class GraphQLScalarConfiguration {
    @Bean
    fun runtimeWiringConfigurer(): RuntimeWiringConfigurer =
        RuntimeWiringConfigurer { builder ->
            builder.scalar(VoidScalar.INSTANCE)
        }
}
```

そして、GraphQLスキーマファイルで以下のように宣言します：

```graphql
# schema.graphqls
scalar Void
```

## Voidスカラー型の使用例

### いいね

このアプリケーションでは、記事に「いいね」する操作でVoidスカラー型を使用しています。

```graphql
extend type Mutation {
    likeArticle(input: LikeArticleInput!): Void
    unlikeArticle(input: UnlikeArticleInput!): Void
}
```

コントローラーの実装では、戻り値の型を明示的に宣言していません。Kotlinでは、戻り値の型を宣言せず、returnステートメントがない関数は暗黙的に`Unit`型（Javaの`void`に相当）を返します。GraphQLスキーマでは`Void`型として定義されているため、クライアントには`null`が返されます：

```kotlin
@MutationMapping
fun likeArticle(
    @Validated @Argument input: LikeArticleInput
) {
    val inputData = LikeArticleInputData(
        articleId = input.articleIdAsNotNull,
        userId = UserRepository.USER_ID_3
    )
    likeArticle.handle(inputData)
}
```

## Voidスカラー型を使用する利点

1. **明確な意図の伝達**: 「何も返さない」という意図を明確に伝えることができます
2. **スキーマの一貫性**: すべてのフィールドが何らかの型を持つという一貫性を保ちながら、「何も返さない」操作を表現できます
3. **クライアント側の処理の簡素化**: クライアント側では、返り値を処理する必要がなく、操作の成功/失敗のみを考慮すればよくなります

## まとめ

Voidスカラー型は、GraphQLの制約（すべてのフィールドは何らかの値を返す必要がある）を守りながら、「何も返さない」操作を表現するための優れた方法です。このアプリケーションでは、`VoidScalar`クラスを実装し、「いいね」の操作で活用しています。

パスワードリセット、ログアウト、データ削除など、結果データよりも操作の成功/失敗が重要な場合には、Voidスカラー型の使用を検討してみてください。これにより、APIの意図が明確にすることができます。
