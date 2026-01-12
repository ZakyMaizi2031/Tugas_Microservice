FROM openjdk:17.0.1-jdk-oracle

# direktori
WORKDIR /app

# copy jar file
COPY target/*.jar app.jar

# expose
EXPOSE 8082

# run
CMD ["java", "-jar", "app.jar"]