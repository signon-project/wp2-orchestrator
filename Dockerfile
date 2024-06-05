FROM openjdk:11
COPY target/*.jar signon-orchestrator.jar
ENTRYPOINT ["java", "-jar", "/signon-orchestrator.jar"]