FROM maven:3.8.4-jdk-8-alpine as builder
LABEL authors="82750"

# Copy local code to the container image
WORKDIR /app
COPY pom.xml .
COPY src ./src

# build a release artifact
RUN mvn package -DskipTests

# Run the web service on container startup
CMD ["java","-jar","/app.target/friendMatch-0.0.1-SNAPSHOT.jar","--spring.profiles.active=prod"]