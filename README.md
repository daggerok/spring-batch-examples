# spring-batch-examples [![CI](https://github.com/daggerok/spring-batch-examples/actions/workflows/ci.yaml/badge.svg)](https://github.com/daggerok/spring-batch-examples/actions/workflows/ci.yaml)

## TODO:
- [Spring Tips: Spring Batch: file to pojo to jdbc import](https://www.youtube.com/watch?v=x4nBNLoizOc)
- [High Performance Batch Processing](https://www.youtube.com/watch?v=J6IPlfm7N6w)
- [Batch Processing in 2019](https://www.youtube.com/watch?v=bhFBtNiZYYY)
- [Cloud Native Batch Processing](https://www.youtube.com/watch?v=1NZVwv1cmMc)
- [Batching for the Modern Enterprise](https://www.youtube.com/watch?v=dIx81HYdpq4)

## spring-batch-example
```bash
cd spring-batch-example ; ./gradlew clean bootRun
```

## spring-batch-job-scheduling
```bash
mvn -f spring-batch-job-scheduling spring-boot:start
sleep 10s
mvn -f spring-batch-job-scheduling spring-boot:stop
```

## spring-batch-pipeline-chain
```bash
rm -rf ~/.m2/repository/com/github/daggerok
mvn clean package install -f spring-batch-pipeline-chain

mvn spring-boot:start -f spring-batch-pipeline-chain/apps/user-service
mvn spring-boot:start -f spring-batch-pipeline-chain/apps/payment-service
mvn spring-boot:start -f spring-batch-pipeline-chain/apps/app

http post :8080/api/launch-payments-report ; http get :8080/api

mvn spring-boot:stop -f spring-batch-pipeline-chain/apps/app
mvn spring-boot:stop -f spring-batch-pipeline-chain/apps/user-service
mvn spring-boot:stop -f spring-batch-pipeline-chain/apps/payment-service

cat ./spring-batch-pipeline-chain/apps/app/target/payments-report.csv
```

## spring-batch-retry
```bash
rm -rf ~/.m2/repository/com/github/daggerok ./target ./spring-batch-retry/apps/app/target/payments-report.csv
mvn clean package install -f spring-batch-retry

mvn spring-boot:start -f spring-batch-retry/apps/user-service
mvn spring-boot:start -f spring-batch-retry/apps/payment-service
mvn spring-boot:start -f spring-batch-retry/apps/app

http post :8080/api/launch-payments-report ; sleep 20s ; http get :8080/api

mvn spring-boot:stop -f spring-batch-retry/apps/app
mvn spring-boot:stop -f spring-batch-retry/apps/user-service
mvn spring-boot:stop -f spring-batch-retry/apps/payment-service

cat ./spring-batch-retry/apps/app/target/payments-report.csv
```

## txt-to-csv-converter
```bash
cd txt-to-csv-converter ; ./mvnw clean compile spring-boot:run
```

## read-process-write-with-tasklets
```bash
cd read-process-write-with-tasklets ; ./mvnw spring-boot:start
http post :8080/api/launch-my-job
cd read-process-write-with-tasklets ; ./mvnw spring-boot:stop
```

## rtfm
* [Spring Batch Retry](https://sysout.ru/otkazoustojchivost-v-spring-batch-retry-i-skip/)
* [Retry rollbackOnly Transactions 1](https://stackoverflow.com/questions/19349898/unexpectedrollbackexception-transaction-rolled-back-because-it-has-been-marked)
* [Retry rollbackOnly Transactions 2](https://stackoverflow.com/questions/34902380/commit-failed-while-step-execution-data-was-already-updated)
* https://www.baeldung.com/spring-retry
* [Spring Batch example with Gradle 5.4.1](./spring-batch-example/)
