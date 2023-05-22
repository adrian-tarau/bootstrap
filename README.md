# Bootstrap

## Introduction
Building blocks for Spring Boot projects. Although Spring Boot (and the rest of the Spring projects) are coming with many services, this project provides some custom services and components to speed up the development process.

_Bootstrap_ is very opinionated when it comes to building (web) application in Java based on Spring Boot. It's opinions are based on personal experience of the creators.

It is recommended to read the following documents to get familiar with Spring Boot and related extensions/plugins:

* [Spring Boot Reference Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/index.html)

## Getting Started

## Build & Run

The project requires Java 17 to develop and run and uses Spring Boot 3.X. The latest version can be downloaded from https://adoptium.net/

Once it is installed, check the version from the shell:

```
java --version
```

The output must show a text similar to the one bellow (maybe a newer version is acceptable):

```
openjdk 17.0.6 2023-01-17
OpenJDK Runtime Environment Temurin-17.0.6+10 (build 17.0.6+10)
OpenJDK 64-Bit Server VM Temurin-17.0.6+10 (build 17.0.6+10, mixed mode, sharing)
```

### IDE

Load the project in any IDE which support Apache Maven (Eclipse, IntelliJ, VS Code). There is a main class called
`DemoApplication` in the `demo` module, just run it, and it will start a Demo application the Spring Boot. Access the application at http://localhost:8080

### Shell

Apache Maven is used to build the project.

`mvn clean install -DskipTests` can be used to compile the application.

`mvn spring-boot:run` can be used to run the demo application using Apache Maven.

### Tests

`mvn clean test` can be used to compile and run tests only.

## MVC & Templates

_Bootstrap_ uses [Thymeleaf](https://www.thymeleaf.org/) as a template engine and expects the following template configurations:

```
spring.mvc.view.prefix=resources/templates
spring.mvc.view.suffix=.html
```

## Database

_Bootstrap_ relies on [Flyway](https://flywaydb.org/) for automatic database migration.

_Bootstrap_ expects that Flyway will be configured to expect all migration files separated by vendor.

```
spring.flyway.locations=classpath:db/migration/{vendor}
```

## Demo

The demo application uses MySQL database. Run the following statements (under `root` user) to create an empty database
and a user for application access (change the database & user if desired, recommended to use `demo`):

```sql
CREATE USER 'demo'@'%' IDENTIFIED BY 'f2RODmy3j1Cq'; 
CREATE DATABASE demo CHARACTER SET utf8 COLLATE utf8_bin; 
GRANT ALL ON demo.* TO 'demo'@'%'; 
FLUSH PRIVILEGES; 
```