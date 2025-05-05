# Use an official Java runtime as a base image
FROM openjdk:22-jdk-slim

# Set the working directory inside the container
WORKDIR /app/auth_service

# Copy the .env file into the container's user_service directory
COPY .env /app/auth_service/.env

# Set read/write permissions for the .env file
RUN chmod 644 /app/auth_service/.env

# Copy the pre-built JAR file into the container
COPY AuthService/target/AuthService-1.0-SNAPSHOT.jar /app/auth_service/AuthService-1.0-SNAPSHOT.jar

# Expose the port your app runs on
EXPOSE 7001

# Run the application
CMD ["java", "-jar", "/app/auth_service/AuthService-1.0-SNAPSHOT.jar"]
