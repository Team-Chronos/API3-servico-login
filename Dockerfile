FROM maven:3.9-eclipse-temurin-25 AS builder
WORKDIR /build

COPY pom.xml .
RUN mvn dependency:resolve

COPY src ./src
RUN mvn clean package -DskipTests

# Runtime
FROM eclipse-temurin:25-jre-noble
WORKDIR /app

COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
