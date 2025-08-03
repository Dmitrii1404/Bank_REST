FROM maven:3.9.4-eclipse-temurin-21-alpine AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

FROM eclipse-temurin:21-jdk-alpine
COPY --from=build /app/target/bankRest-0.0.1-SNAPSHOT.jar /app/app.jar

WORKDIR /app
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
