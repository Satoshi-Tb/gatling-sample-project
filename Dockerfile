FROM maven:3.9-eclipse-temurin-17 AS builder

# 作業ディレクトリを指定
WORKDIR /app

# Mavenローカルキャッシュを活用するために volume を使うとビルド高速化できる
VOLUME /root/.m2

# Mavenプロジェクトファイル（pom.xml）とソースコードをコピー
COPY . .

# Mavenを使用してプロジェクトをパッケージ
RUN mvn clean package

# ホストからプロジェクトをマウントして mvn 実行する想定
CMD ["./mvnw", "gatling:test"]