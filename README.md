ddd-java
---

### Preface

This is a DDD sample implementation using [Spring Boot](http://projects.spring.io/spring-boot/).  
It is not a framework, but a simple example of implementation based on Evans's DDD. This approach is straightforward and based on Evans's book, rather than verbose approaches like Clean Architecture or Hexagonal Architecture.

#### Concept of Layering

This follows the famous three-layer model, but considers the infrastructure layer as a cross-cutting concern.

| Layer          |                                                            |
| -------------- | ----------------------------------------------------------- |
| UI             | Receives use case requests                                  |
| Application    | Use case processing (including external resource access)   |
| Domain         | Pure domain logic (does not depend on external resources)  |
| Infrastructure | DI container, ORM, and various libraries                   |

Usually, the UI layer handles public interactions using Thymeleaf, but this sample assumes the use of different types of clients and only provides APIs through RESTful endpoints.

#### Spring Boot Usage Policy

Spring Boot can be used in various ways, but this sample follows the following policies:

- Use standard configurations as much as possible, such as DB settings.
- Configuration files use YAML. XML files are not used for Bean definitions.
- Exception handling is defined at endpoints (RestErrorAdvice).

#### Java Coding Policy

- Java 21 or higher
- Use Lombok actively to reduce boilerplate code.
- Keep names as brief as possible.
- Do not overuse interfaces.
- DTOs that are part of the domain are defined as inner classes.

#### Resource Structure

Refer to the following for the package and resource structure:

```
main
  java
    sample
      context                         … Infrastructure Layer
      controller                      … UI Layer
      model                           … Domain Layer
      usecase                         … Application Layer
      util                            … Utilities
      - Application.java              … Bootstrap
  resources
    - application.yml                 … Spring Boot Configuration
    - messages-validation.properties  … Validation Message Resources
    - messages.properties             … Label Message Resources
```

## Use Case

Consider the following as a sample use case:

- A customer with an account balance requests a withdrawal. (Event T, Delivery T + 3)
- The system closes the withdrawal request. (Allows cancellation of the request until closing)
- The system advances the business day to the next day.
- The system reflects the cash flow on the delivery date to the account balance.

### Getting Started

This sample uses [Gradle](https://gradle.org/). You can check the operation easily with an IDE or console.

#### Server Start (VSCode DevContainer)

The following steps are required:

- Ensure Docker is installed.
- Ensure VSCode with DevContainer Extension is installed.

Prepare this sample with the following steps:

1. Navigate to the cloned *ddd-java* directory.
1. Run the command `code .`.
1. Choose *Open Container*.

Start the server with the following steps:

1. Open VSCode "Run And Debug".
1. Choose `Run ddd-java`.
1. If the console shows "Started Application", the server has started on port 8080.
1. Run the command `curl http://localhost:8080/actuator/health`.

#### Server Start (Console)

Run the application from a Windows/Mac console using Gradle.

The following steps are required:

- Ensure JDK 21+ is installed.

Start the server with the following steps:

1. Navigate to the cloned *ddd-java* directory.
1. Run the command `./gradlew bootRun --args='--spring.profiles.active=dev'`.
1. If the console shows "Started Application", the server has started on port 8080.
1. Run the command `curl http://localhost:8080/actuator/health`.

### Check Use Case

After launching the server on port 8080, you can test the RESTful API execution by accessing the following URLs from the console:

#### Customer Use Case

- `curl -X POST -H "Content-Type: application/json" -d '{"accountId"  : "sample" , "currency" : "JPY", "absAmount": 1000}' http://localhost:8080/asset/cio/withdraw`
    - Request a withdrawal.
- `curl 'http://localhost:8080/asset/cio/unprocessedOut'`
    - Search for outstanding withdrawal requests.

#### Internal Use Case

- `curl 'http://localhost:8080/admin/asset/cio?updFromDay=yyyy-MM-dd&updToDay=yyyy-MM-dd'`
    - Search for deposit and withdrawal requests.
    - Please set actual dates for upd\*Day parameters.

#### Batch Use Case

- `curl -X POST http://localhost:8080/system/job/daily/closingCashOut`
    - Close withdrawal requests.
- `curl -X POST http://localhost:8080/system/job/daily/processDay`
    - Advance the business day to the next day.
- `curl -X POST http://localhost:8080/system/job/daily/realizeCashflow`
    - Realize cash flow. (Reflects to the balance on the delivery date)

> Please execute these according to the business day appropriately.

### License

The license for this sample code is *MIT License*.
Use it as a base implementation when starting a project using Spring Boot.
