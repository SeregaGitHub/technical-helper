FROM amazoncorretto:21-alpine-jdk
# WORKDIR /app
COPY target/*.jar app.jar
# EXPOSE 12301
ENTRYPOINT ["java","-jar","/app.jar"]