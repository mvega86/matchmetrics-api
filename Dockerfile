FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

# Cache dependencies separately from source — only re-downloaded when pom.xml changes
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw dependency:go-offline -q

COPY src src
RUN ./mvnw clean package -DskipTests -q

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Xmx384m", "-Xms128m", "-XX:+UseG1GC", "-jar", "app.jar"]
