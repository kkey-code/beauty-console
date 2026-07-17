FROM maven:3-eclipse-temurin-17 AS build

WORKDIR /workspace
COPY . .
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -pl store-server -am clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine

RUN apk add --no-cache curl \
    && addgroup -S spring \
    && adduser -S spring -G spring

WORKDIR /app
COPY --from=build /workspace/store-server/target/store-server-*.jar app.jar

USER spring
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
