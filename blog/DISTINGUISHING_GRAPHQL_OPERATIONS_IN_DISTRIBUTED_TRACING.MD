# Spring for GraphQL TIPS〜分散トレーシングでGraphQL操作を区別する方法〜

## はじめに

GraphQLは単一エンドポイントで複数の操作を処理するため、分散トレーシングにおいて異なるGraphQL操作を区別することが難しくなります。例えば、すべてのGraphQL操作が同じエンドポイント（通常は`/graphql`）にリクエストされるため、トレースでは単に「/graphql」としか表示されず、どの操作が実行されているのかを識別できません。

この記事では、Spring Boot アプリケーションで分散トレーシングを使用する際に、GraphQLの異なる操作を区別する方法について説明します。具体的には、`OperationNameServerRequestObservationConvention`クラスを使用して、トレースにGraphQLの操作名を含める方法を紹介します。

なお、この記事は[JJUG CCC 2025 for Spring](https://jjug-ccc-2025-spring.sessionize.com/session/872868)のセッションで公開した[リファレンス実装](https://github.com/kogayushi/spring-for-graphql-tips-by-small-startup)で触れられなかった肥大化するGraphQLスキーマへの対処の解説記事です。

## 問題：GraphQLの単一エンドポイント

GraphQLの主な特徴の一つは、すべての操作が単一のエンドポイントを通じて処理されることです。これはRESTful APIとは大きく異なる点で、RESTful APIでは異なるエンドポイントが異なる操作を表します。

分散トレーシングの観点からは、この単一エンドポイントの特性が問題となります。すべてのGraphQL操作が単に「/graphql」として記録されるため、どの操作が実行されているのかを特定することが困難になります。

## 解決策：OperationNameServerRequestObservationConvention

Spring Bootの観測機能（Observability）を拡張することで、この問題を解決できます。具体的には、`DefaultServerRequestObservationConvention`をカスタマイズして、GraphQLの操作名をトレースに含めることができます。

以下が`OperationNameServerRequestObservationConvention`クラスの実装です。

```kotlin
@Component
class OperationNameServerRequestObservationConvention(
    private val graphQlProperties: GraphQlProperties,
) : DefaultServerRequestObservationConvention() {
    override fun getContextualName(context: ServerRequestObservationContext): String {
        val contextualName = super.getContextualName(context)
        val operationName =
            if (context.pathPattern == graphQlProperties.path) {
                context.carrier.queryString
                    ?.split("&")
                    ?.find {
                        it.lowercase().startsWith("$QUERY_NAME_NO_DELIMITER=") ||
                            it.lowercase().startsWith("$QUERY_NAME_SNAKE=") ||
                            it.lowercase().startsWith("$QUERY_NAME_KEBAB=")
                    }?.let {
                        val value = it.split("=")[1]
                        "?$QUERY_NAME_KEBAB=$value"
                    } ?: ""
            } else {
                ""
            }
        return "$contextualName$operationName"
    }

    private companion object {
        private const val QUERY_NAME_NO_DELIMITER = "operationname"
        private const val QUERY_NAME_SNAKE = "operation_name"
        private const val QUERY_NAME_KEBAB = "operation-name"
    }
}
```

このクラスの主な機能は以下の通りです。

1. `DefaultServerRequestObservationConvention`を拡張して、デフォルトの観測機能を継承
2. `getContextualName`メソッドをオーバーライドして、GraphQLリクエストの場合に操作名を追加
3. リクエストパスがGraphQLのパス（通常は`/graphql`）と一致する場合のみ処理
4. クエリ文字列から操作名を抽出（異なる命名規則に対応）
5. 抽出した操作名をコンテキスト名に追加して返す

## 動作の仕組み

このクラスは以下のように動作します：

1. すべてのHTTPリクエストに対して、Spring Bootの観測機能が`ServerRequestObservationContext`を作成
2. GraphQLリクエストの場合、クエリパラメータから操作名を抽出
3. 操作名が見つかった場合、それをコンテキスト名に追加
4. 結果として、トレースには「/graphql?operation-name=GetArticles」のような形式で記録される

操作名の抽出では、以下の3つの命名規則に対応しています：
- `operationname`（区切り文字なし）
- `operation_name`（スネークケース）
- `operation-name`（ケバブケース）

これにより、さまざまなGraphQLクライアントやツールからのリクエストに対応できます。

## 実装のポイント

### 1. 依存関係の注入

このクラスは`GraphQlProperties`を注入することで、アプリケーションで設定されたGraphQLのパスを取得しています。これにより、`application.properties`や`application.yml`でGraphQLのパスをカスタマイズしても正しく動作します。

### 2. 複数の命名規則への対応

GraphQLクライアントによって、操作名のパラメータ名が異なる場合があります。このクラスでは、一般的な3つの命名規則（区切り文字なし、スネークケース、ケバブケース）に対応しています。

### 3. コンポーネントとしての登録

`@Component`アノテーションを使用することで、このクラスはSpring Bootによって自動的に検出され、デフォルトの`DefaultServerRequestObservationConvention`の代わりに使用されます。

## 利点

この実装には以下の利点があります：

1. **操作の可視性向上**: 各GraphQL操作を個別に識別できるため、パフォーマンスの問題を特定しやすくなります
2. **トラブルシューティングの効率化**: 問題が発生した場合、特定の操作に絞ってトレースを確認できます
3. **既存のコードへの影響なし**: GraphQLの実装コードを変更することなく、観測機能のみを拡張しています

## 使用例

この機能を使用すると、以下のようなメリットがあります。

### Zipkinでのトレース表示

Zipkinなどのトレースビューアでは、操作名を含むエンドポイントが表示されるため、特定の操作のトレースを簡単に見つけることができます。

## まとめ

GraphQLの単一エンドポイント特性は、分散トレーシングにおいて課題となりますが、`OperationNameServerRequestObservationConvention`を使用することでこの問題を解決できます。このクラスは、GraphQLリクエストの操作名を抽出し、トレースに含めることで、異なるGraphQL操作を区別可能にします。

この実装は、コードの変更を最小限に抑えながら、観測性を大幅に向上させる効果的な方法です。GraphQLを使用するSpring Bootアプリケーションで分散トレーシングを活用する場合は、ぜひこのアプローチを検討してみてください。
