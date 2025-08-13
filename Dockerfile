FROM openjdk:17-slim
WORKDIR /app
COPY target/YoutubeCommentFetchApplication-1.0-SNAPSHOT-jar-with-dependencies.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-cp", "/app.jar", "org.myapps.youtube.commentranker.YoutubeCommentRankerApp"]