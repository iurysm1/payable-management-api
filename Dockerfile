FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /workspace

COPY .mvn .mvn
COPY mvnw pom.xml ./

COPY src src

RUN --mount=type=cache,target=/root/.m2 \
    chmod +x mvnw \
    && ./mvnw -DskipTests package \
    && cp target/payable-management-api-*.jar /workspace/application.jar

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S payable \
    && adduser -S payable -G payable \
    && mkdir -p /app/data/imports \
    && chown -R payable:payable /app

COPY --from=builder --chown=payable:payable /workspace/application.jar /app/application.jar

USER payable

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/application.jar"]
