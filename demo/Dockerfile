FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY build/libs/demo-0.0.1-SNAPSHOT.jar message-based-architecture-demo.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "message-based-architecture-demo.jar"]