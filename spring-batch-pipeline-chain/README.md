# Spring Batch pipeline chain processing

## Overview
TODD

## Build, run, test

```bash
rm -rf ~/.m2/repository/com/github/daggerok/batch
mvn clean package install

mvn spring-boot:start -f apps/user-service
mvn spring-boot:start -f apps/payment-service
mvn spring-boot:start -f apps/app

http post :8080/api/launch-payments-report ; http get :8080/api

mvn spring-boot:stop -f apps/app
mvn spring-boot:stop -f apps/user-service
mvn spring-boot:stop -f apps/payment-service

cat ./apps/app/target/payments-report.csv
```

<!--

```bash
cd spring-batch-pipeline-chain ; ./mvnw -f docker -P down ; ./mvnw -f docker -P up
rm -rf ~/.m2/repository/com/github/daggerok/batch ; ./mvnw clean install
./mvnw -f apps/app spring-boot:run
./mvnw -f docker -P down
```

```bash
cd spring-batch-pipeline-chain ; ./mvnw clean test
```

### Reference Documentation

For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.5.5/maven-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/2.5.5/maven-plugin/reference/html/#build-image)
* [Spring Batch](https://docs.spring.io/spring-boot/docs/2.5.5/reference/htmlsingle/#howto-batch-applications)

### Guides

The following guides illustrate how to use some features concretely:

* [Creating a Batch Service](https://spring.io/guides/gs/batch-processing/)

-->
