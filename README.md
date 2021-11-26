# spring-batch-examples [![CI](https://github.com/daggerok/spring-batch-examples/actions/workflows/ci.yaml/badge.svg)](https://github.com/daggerok/spring-batch-examples/actions/workflows/ci.yaml)

<!-- ord Travis CI status: 
[![Build Status](https://travis-ci.org/daggerok/spring-batch-example.svg?branch=master)](https://travis-ci.org/daggerok/spring-batch-example)
-->

## spring-batch-example
```bash
cd spring-batch-example ; ./gradlew clean bootRun
```

## spring-batch-pipeline-chain
```bash
mvn -f spring-batch-job-scheduling spring-boot:start
sleep 10s
mvn -f spring-batch-job-scheduling spring-boot:stop
```

## spring-batch-pipeline-chain
```bash
rm -rf ~/.m2/repository/com/github/daggerok/batch
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

## rtfm
* [Spring Batch example with Gradle 5.4.1](./spring-batch-example/)
