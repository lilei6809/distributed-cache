FROM openjdk:11-jre-slim
WORKDIR /app
COPY target/*.jar app.jar
RUN ls -l /app
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
EXPOSE 8080