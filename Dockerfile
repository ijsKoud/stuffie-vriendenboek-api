# Builds one service: docker build --build-arg APP=hello --build-arg VERSION=1.2.3 .
FROM eclipse-temurin:25-jdk AS build
WORKDIR /src
COPY . .
ARG APP
ARG VERSION
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew --no-daemon ":apps:${APP}:bootJar" "-PreleaseVersion=${VERSION}"

FROM eclipse-temurin:25-jre
ARG APP
COPY --from=build /src/apps/${APP}/build/libs/app.jar /app/app.jar
USER 10001:10001
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
