# Spring for GraphQL Tips by Small Startup
このプロジェクトはJJUG CCC 2025 Springで発表した内容の参照実装です。
発表した内容はすべてこのプロジェクトに含まれているため、具体的な実装を確認したい方はこのプロジェクトを参照してください。

## 起動方法

このプロジェクトはDocker Composeを使用して起動できますが、最初にGradleでDockerイメージをビルドする必要があります。

### 前提条件

- Java 21
- Docker
- Docker Compose

### 起動手順

1. 以下のコマンドを実行して、Dockerイメージをビルドし、Docker Composeで起動します：

```bash
./start-docker.sh
```

このスクリプトは以下の処理を行います：
- Dockerイメージ「kogayushi/graphql-tips」が存在するかチェック
- 存在しない場合は `./gradlew bootBuild` を実行してイメージをビルド
- Docker Composeを使用してアプリケーションを起動

手動で起動する場合は、以下のコマンドを順番に実行します：

```bash
# Dockerイメージをビルド
./gradlew bootBuildImage

# Docker Composeでアプリケーションを起動
docker compose up -d
```

### 起動後のアクセス

- GraphQLアプリケーション: http://localhost:8080
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000
- Zipkin: http://localhost:9411

## 負荷試験の実行方法

このプロジェクトではJMeterを使用して負荷試験を実行できます。

### 前提条件

- Apache JMeter

### 実行手順

1. JMeterを起動します
2. `load-test/graphql-tips-load-test.jmx` ファイルを開きます
3. JMeterのGUIから試験を実行します

## 技術スタック

- Kotlin
- Spring Boot 3.4.2
- GraphQL
- Kafka
- Docker
- Prometheus
- Grafana
- Zipkin
