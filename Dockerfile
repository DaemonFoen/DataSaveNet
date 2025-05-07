FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /build

COPY . .

RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY --from=build /build/target/*.jar app.jar

RUN echo "Some data to test" > /test_data.txt

ENTRYPOINT ["java", "-jar", "app.jar"]
