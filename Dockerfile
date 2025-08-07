FROM maven:3.8.5-openjdk-17 AS dependencies
WORKDIR /app
COPY pom.xml .
RUN mvn -B -e org.apache.maven.plugins:maven-dependency-plugin:3.1.2:go-offline

FROM maven:3.8.5-openjdk-17 AS builder
WORKDIR /app
COPY --from=DEPENDENCIES /root/.m2 /root/.m2
COPY --from=DEPENDENCIES /app/ /app
COPY src /app/src
RUN mvn -B -e clean install -DskipTests

FROM openjdk:17-slim
WORKDIR /app
COPY --from=BUILDER /app/target/*.jar /app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar", "org.myapps.youtube.commentranker.YoutubeCommentRankerApp"]