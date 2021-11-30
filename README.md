# spring-batch-examples [![CI](https://github.com/daggerok/spring-batch-examples/actions/workflows/ci.yaml/badge.svg)](https://github.com/daggerok/spring-batch-examples/actions/workflows/ci.yaml)

## spring-batch-example
```bash
cd spring-batch-example ; ./gradlew clean bootRun
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

## rtfm
* [Spring Batch Retry](https://sysout.ru/otkazoustojchivost-v-spring-batch-retry-i-skip/)
* [Retry rollbackOnly Transactions 1](https://stackoverflow.com/questions/19349898/unexpectedrollbackexception-transaction-rolled-back-because-it-has-been-marked)
* [Retry rollbackOnly Transactions 2](https://stackoverflow.com/questions/34902380/commit-failed-while-step-execution-data-was-already-updated)
* https://www.baeldung.com/spring-retry
* [Spring Batch example with Gradle 5.4.1](./spring-batch-example/)
