FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml ./
COPY src ./src
RUN mvn -B -ntp verify
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN groupadd -r app && useradd -r -g app app
COPY --from=build /app/target/backend-*.jar /app/billing-copilot.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/billing-copilot.jar"]
