FROM eclipse-temurin:8-jre

WORKDIR /app

COPY target/binary-text-api-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]
