ddd-java
---

### Preface

It is DDD sample implementation from [Spring Boot](http://projects.spring.io/spring-boot/).  
It is not a framework, This is a simple example of the implementation based on Evans's DDD.

Refer to [sample-boot-hibernate (JP/EN)](https://github.com/jkazama/sample-boot-hibernate) for a more practical implementation sample including authentication.

Refer to [sample-ui-vue (JP)](https://github.com/jkazama/sample-ui-vue) / [sample-ui-react (JP)](https://github.com/jkazama/sample-ui-react) for the implementation sample on the UI side.  

#### Concept of Layering

It is three levels of famous models, but considers the infrastructure layer as cross-sectional interpretation.

| Layer          |                                                            |
| -------------- | ----------------------------------------------------------- |
| UI             | Receive use case request                                    |
| Application    | Use case processing (including the outside resource access) |
| Domain         | Pure domain logic (not depend on the outside resource) |
| Infrastructure | DI container and ORM, various libraries |

Usually perform public handling of UI layer using Thymeleaf or JSP, but this sample assume use of different types of clients and perform only API offer in RESTfulAPI.

#### Use policy of Spring Boot

Spring Boot is available for various usage, but uses it in the following policy with this sample.

- Use standard definitions as much as possible, such as DB settings.
- The configuration file uses yml. do not use xml files for Bean definition.
- The exception handling defines it in a endpoint (RestErrorAdvice / RestErrorCotroller). The whitelabel function disabled it.

#### Use policy of Java coding

- Java8 over
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

- A customer with an account balance requests withdrawal. (Accrual T, Delivery T + 3)
- The system closes the withdrawal request. (Allows cancellation of request until closing)
- The system sets the business day to the forward day.
- The system reflects the cash flow on delivery date to the account balance.

### Getting Started

This sample uses [Gradle](https://gradle.org/), you can check the operation without trouble with IDE and a console.

#### Server Start (Eclipse)

It is necessary to do the following step.

- Check Instablled JDK8+.
- Apply a patch of [Lombok](http://projectlombok.org/download.html)

Do the preparations for this sample in the next step.

1. Choose "right-click - Import - Project" among package Explorer.
1. Choose Gradle Project* and push down *Next*
1. Choose cloned *ddd-java* in *Project root directory* and push down *Next*
1. Push down *Next* in *Import Options*
1. If *ddd-java* is displayed by *Gradle project structure*, push down *Finish*
    -  dependency library downloading is carried out here

Do the server start in the next step.

1. Do "right-click - Run As - Java Application" for *Application.java*
1. If console show "Started Application", start is completed in port 8080
1. Start a browser and confirm a state in "http://localhost:8080/api/management/health"

> You can easily run the main class via Boot Dashboard of GUI when you use plug in of STS (Spring Tool Suite).

#### Server Start (Console)

Run application from a console of Windows / Mac in Gradle.

It is necessary to do the following step.

- Check Instablled JDK8+.

Do the server start in the next step.

1. You move to the cloned *ddd-java* directory.
1. Run command "gradlew bootRun".
1. If console show "Started Application", start is completed in port 8080

### Check Use Case

After launching the server on port 8080 with Eclipse or console, you can test execution of RESTful API by accessing the following URL from a browser.

> Normally, information update processing should be handled by POST, but GET access is allowed for demos without UI.
> Connect parameters with `?key=value&key=value`.

#### Customer Use Case

- http://localhost:8080/asset/cio/withdraw
    - Request for withdrawal. [accountId: sample, currency: JPY, absAmount: 1000]
- http://localhost:8080/asset/cio/unprocessedOut
    - Search for outstanding withdrawal requests

#### Internal Use Case

- http://localhost:8080/admin/asset/cio
    - Search for deposit and withdrawal requests. [updFromDay: yyyyMMdd, updToDay: yyyyMMdd]
    - Please set real date for upd\*Day

#### Batch Use Case

- http://localhost:8080/system/job/daily/closingCashOut
    - Close the withdrawal request.
- http://localhost:8080/system/job/daily/processDay
    - Set the business day to the next day.
- http://localhost:8080/system/job/daily/realizeCashflow
    - Realize cash flow. (Reflected to the balance on the delivery date)

> Please execute according to the business day appropriately

### License

The license of this sample includes a code and is all *MIT License*.
Use it as a base implementation at the time of the project start using Spring Boot.
