# Backend image: builds the Maven multi-module project, then runs only the packaged jar.
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY . .
# tests run with `mvn install` on a developer machine; the image build only needs the jar
RUN mvn -q -B -DskipTests package

FROM eclipse-temurin:17-jre
RUN useradd --system --create-home amira
WORKDIR /app
COPY --from=build /build/sbamirams/target/sbamirams-1.0-SNAPSHOT.jar app.jar
USER amira
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
