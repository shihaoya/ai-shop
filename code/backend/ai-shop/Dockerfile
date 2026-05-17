# === Build Stage ===
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app

# 先复制 pom.xml，利用 Docker 层缓存加速依赖下载
COPY pom.xml .
RUN mvn dependency:resolve -B

# 复制源码并打包
COPY src ./src
RUN mvn package -DskipTests -B

# === Runtime Stage ===
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 创建非 root 用户运行
RUN addgroup -S app && adduser -S app -G app

COPY --from=builder --chown=app:app /app/target/*.jar app.jar

USER app
EXPOSE 18780

ENTRYPOINT ["java", "-jar", "app.jar"]
