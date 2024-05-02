FROM openjdk:21-jdk-slim

COPY target/security-istio-demo-*.jar /app.jar
EXPOSE 8080/tcp
ENTRYPOINT ["java","-jar","/app.jar"]