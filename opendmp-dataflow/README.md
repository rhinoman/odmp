# opendmp-dataflow

The main dataflow service for OpenDMP.

odmp-dataflow is a Spring Boot app written in Kotlin using WebFlux and Camel.

## Dependencies

- JDK 11
- Apache Maven
- MongoDB Server (runtime)
- Keycloak Server (runtime)
- Apache Pulsar (runtime)

## Build, Run, Test

Build: `mvn install`

Run: `mvn spring-boot:run`

Tests: `mvn test`

## Usage
Exposes a REST API on port 8080

API docs at `/dataflow_api/doc/api-docs`

Swagger UI available at `/dataflow_api/doc/swagger/swagger-ui.html`