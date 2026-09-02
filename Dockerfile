FROM eclipse-temurin:21-jdk AS build

WORKDIR /workspace
COPY backend/ ./

RUN ./gradlew bootJar --no-daemon \
    && cp "$(find build/libs -maxdepth 1 -type f -name '*.jar' ! -name '*-plain.jar' -print -quit)" \
     /tmp/ledger-service.jar

FROM eclipse-temurin:21-jre

RUN useradd --system --create-home spring

WORKDIR /app
COPY --from=build /tmp/ledger-service.jar app.jar

USER spring
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
