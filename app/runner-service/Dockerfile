# syntax=docker/dockerfile:1
FROM eclipse-temurin:25.0.4_7-jdk-noble AS build
WORKDIR /workspace
COPY . .
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew :app:runner-service:bootJar --no-daemon --console=plain \
 && java -Djarmode=tools -jar app/runner-service/build/libs/*.jar extract --layers --launcher --destination /extracted

FROM eclipse-temurin:25.0.4_7-jre-noble
RUN useradd --system --uid 10001 --user-group --no-create-home spring
COPY --from=docker:29.9.3-cli /usr/local/bin/docker /usr/local/bin/docker
WORKDIR /app
COPY --from=build /extracted/dependencies/ ./
COPY --from=build /extracted/spring-boot-loader/ ./
COPY --from=build /extracted/snapshot-dependencies/ ./
COPY --from=build /extracted/application/ ./
USER 10001
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
