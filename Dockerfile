FROM openjdk:17
EXPOSE 8080
ARG JAR_FILE=./*-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]