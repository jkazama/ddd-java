ddd-java
---

### Preface

It is DDD sample implementation from [Spring Boot](http://projects.spring.io/spring-boot/).  
It is not a framework, This is a simple example of the implementation based on Evans's DDD.


#### Concept of Layering

It is three levels of famous models, but considers the infrastructure layer as cross-sectional interpretation.

| Layer          |                                                            |
| -------------- | ----------------------------------------------------------- |
| UI             | Receive use case request                                    |
| Application    | Use case processing (including the outside resource access) |
| Domain         | Pure domain logic (not depend on the outside resource) |
| Infrastructure | DI container and ORM, various libraries |

Usually perform public handling of UI layer using Thymeleaf, but this sample assume use of different types of clients and perform only API offer in RESTfulAPI.

#### Use policy of Spring Boot

Spring Boot is available for various usage, but uses it in the following policy with this sample.

- Use standard definitions as much as possible, such as DB settings.
- The configuration file uses yml. do not use xml files for Bean definition.
- The exception handling defines it in a endpoint (RestErrorAdvice).

#### Use policy of Java coding

- Java17 over
- Use Lombok positively and remove diffuseness.
- The name as possible briefly.
- Do not abuse the interface.
- DTO becoming a part of the domain defines it in an internal class.

#### Resource

Refer to the following for the package / resource constitution.

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

Consider the following as a sample use case.

- A customer with an account balance requests withdrawal. (Event T, Delivery T + 3)
- The system closes the withdrawal request. (Allows cancellation of request until closing)
- The system sets the business day to the forward day.
- The system reflects the cash flow on delivery date to the account balance.

### Getting Started

This sample uses [Gradle](https://gradle.org/), you can check the operation without trouble with IDE and a console.

#### Server Start (VSCode DevContainer)

It is necessary to do the following step.

- Check Instablled Docker.
- Check Instablled VSCode with DevContainer Extension.

Do the preparations for this sample in the next step.

1. You move to the cloned *ddd-java* directory.
1. Run command `code .`.
1. Choose *Open Container*

Do the server start in the next step.

1. Open VSCode "Run And Debug".
1. Choose `Run ddd-java`.
1. If console show "Started Application", start is completed in port 8080.
1. Run command `curl http://localhost:8080/actuator/health`

#### Server Start (Console)

Run application from a console of Windows / Mac in Gradle.

It is necessary to do the following step.

- Check Instablled JDK17+.

Do the server start in the next step.

1. You move to the cloned *ddd-java* directory.
1. Run command `./gradlew bootRun --args='--spring.profiles.active=dev'`.
1. If console show "Started Application", start is completed in port 8080
1. Run command `curl http://localhost:8080/actuator/health`

### Check Use Case

After launching the server on port 8080, you can test execution of RESTful API by accessing the following URL from console.

#### Customer Use Case

- `curl -X POST -H "Content-Type: application/json" -d '{"accountId"  : "sample" , "currency" : "JPY", "absAmount": 1000}' http://localhost:8080/asset/cio/withdraw`
    - Request for withdrawal.
- `curl 'http://localhost:8080/asset/cio/unprocessedOut'`
    - Search for outstanding withdrawal requests

#### Internal Use Case

- `curl 'http://localhost:8080/admin/asset/cio?updFromDay=yyyy-MM-dd&updToDay=yyyy-MM-dd'`
    - Search for deposit and withdrawal requests.
    - Please set real date for upd\*Day

#### Batch Use Case

- `curl -X POST http://localhost:8080/system/job/daily/closingCashOut`
    - Close the withdrawal request.
- `curl -X POST http://localhost:8080/system/job/daily/processDay`
    - Set the business day to the next day.
- `curl -X POST http://localhost:8080/system/job/daily/realizeCashflow`
    - Realize cash flow. (Reflected to the balance on the delivery date)

> Please execute according to the business day appropriately

### License

The license of this sample includes a code and is all *MIT License*.
Use it as a base implementation at the time of the project start using Spring Boot.
