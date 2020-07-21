# opendmp-processor

This service is responsible for executing individual processors

odmp-processor is a Spring Boot app making heavy use of Apache Camel

It receives requests and reports results via Apache Pulsar.

## Dependencies

- JDK 11
- Apache Maven
- Apache Pulsar (runtime)

## Build, Run, Test

Build: `mvn install`

Run: `mvn spring-boot:run`

Tests: `mvn test`