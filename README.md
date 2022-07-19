# spring-batch-examples [![CI](https://github.com/daggerok/spring-batch-examples/actions/workflows/ci.yaml/badge.svg)](https://github.com/daggerok/spring-batch-examples/actions/workflows/ci.yaml)

## TODO:
- [Spring Batch for cloud](https://www.youtube.com/watch?v=-Icd-s2JoAw&t=1926s)
- [Spring Tips: Spring Batch: file to pojo to jdbc import](https://www.youtube.com/watch?v=x4nBNLoizOc)
- [High Performance Batch Processing](https://www.youtube.com/watch?v=J6IPlfm7N6w)
- [Batch Processing in 2019](https://www.youtube.com/watch?v=bhFBtNiZYYY)
- [Cloud Native Batch Processing](https://www.youtube.com/watch?v=1NZVwv1cmMc)
- [Batching for the Modern Enterprise](https://www.youtube.com/watch?v=dIx81HYdpq4)

## spring-batch-example [![Spring Batch Gradle](https://github.com/daggerok/spring-batch-examples/actions/workflows/spring-batch-gradle.yaml/badge.svg)](https://github.com/daggerok/spring-batch-examples/actions/workflows/spring-batch-gradle.yaml)
```bash
cd spring-batch-example ; ./gradlew clean bootRun
```

## spring-batch-job-scheduling [![Spring Batch job scheduling](https://github.com/daggerok/spring-batch-examples/actions/workflows/spring-batch-job-scheduling.yaml/badge.svg)](https://github.com/daggerok/spring-batch-examples/actions/workflows/spring-batch-job-scheduling.yaml)
```bash
mvn -f spring-batch-job-scheduling spring-boot:start
sleep 10s
mvn -f spring-batch-job-scheduling spring-boot:stop
```

## spring-batch-metrics [![Spring Batch Metrics](https://github.com/daggerok/spring-batch-examples/actions/workflows/spring-batch-metrics.yaml/badge.svg)](https://github.com/daggerok/spring-batch-examples/actions/workflows/spring-batch-metrics.yaml)
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

## spring-batch-retry [![Spring Batch Retry](https://github.com/daggerok/spring-batch-examples/actions/workflows/spring-batch-retry.yaml/badge.svg)](https://github.com/daggerok/spring-batch-examples/actions/workflows/spring-batch-retry.yaml)
```bash
rm -rf ~/.m2/repository/com/github/daggerok
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

## txt-to-csv-converter [![Text to CSV converter](https://github.com/daggerok/spring-batch-examples/actions/workflows/txt-to-csv-converter.yaml/badge.svg)](https://github.com/daggerok/spring-batch-examples/actions/workflows/txt-to-csv-converter.yaml)
```bash
cd txt-to-csv-converter ; ./mvnw clean compile spring-boot:run
```

## read-process-write-with-tasklets [![Read process writer taskets](https://github.com/daggerok/spring-batch-examples/actions/workflows/read-process-writer-tasklets.yaml/badge.svg)](https://github.com/daggerok/spring-batch-examples/actions/workflows/read-process-writer-tasklets.yaml)
```bash
cd read-process-write-with-tasklets ; ./mvnw spring-boot:start
http post :8080/api/launch-my-job
cd read-process-write-with-tasklets ; ./mvnw spring-boot:stop
```

## spring-batch-metrics [![Spring Batch Metrics](https://github.com/daggerok/spring-batch-examples/actions/workflows/spring-batch-metrics.yaml/badge.svg)](https://github.com/daggerok/spring-batch-examples/actions/workflows/spring-batch-metrics.yaml)
```bash
rm -rf ~/.m2/repository/com/github/daggerok
mvn clean package install -f spring-batch-metrics

mvn spring-boot:start -f spring-batch-metrics/apps/user-service
mvn spring-boot:start -f spring-batch-metrics/apps/payment-service
mvn spring-boot:start -f spring-batch-metrics/apps/app

http post :8080/api/launch-payments-report ; http get :8080/api
http get  :8080/actuator/metrics/app.EnrichUsersDataProcessor

mvn spring-boot:stop -f spring-batch-metrics/apps/app
mvn spring-boot:stop -f spring-batch-metrics/apps/user-service
mvn spring-boot:stop -f spring-batch-metrics/apps/payment-service

cat ./spring-batch-metrics/apps/app/target/payments-report.csv
```

## run-steps-in-parallel-with-flow-split [![Run steps in parallel with flow split](https://github.com/daggerok/spring-batch-examples/actions/workflows/run-steps-in-parallel-with-flow-split.yaml/badge.svg)](https://github.com/daggerok/spring-batch-examples/actions/workflows/run-steps-in-parallel-with-flow-split.yaml)
```bash
rm -rf ~/.m2/repository/com/github/daggerok
mvn clean package install -f run-steps-in-parallel-with-flow-split

mvn spring-boot:start -f run-steps-in-parallel-with-flow-split/apps/user-service
mvn spring-boot:start -f run-steps-in-parallel-with-flow-split/apps/payment-service
mvn spring-boot:start -f run-steps-in-parallel-with-flow-split/apps/app

http post :8080/api/launch-payments-report ; http get :8080/api
http get  :8080/actuator/metrics/app.loadAllPaymentsFlow
http get  :8080/actuator/metrics/app.loadAllUsersFlow
http get  :8080/actuator/metrics/app.enrichReportTaskletStep

mvn spring-boot:stop -f run-steps-in-parallel-with-flow-split/apps/app
mvn spring-boot:stop -f run-steps-in-parallel-with-flow-split/apps/user-service
mvn spring-boot:stop -f run-steps-in-parallel-with-flow-split/apps/payment-service

cat ./run-steps-in-parallel-with-flow-split/apps/app/target/payments-report.csv
```

## batch-to-cloud: step-1-plain-old-batch [![Spring Batch for cloud](https://github.com/daggerok/spring-batch-examples/actions/workflows/batch-to-cloud-1.yaml/badge.svg)](https://github.com/daggerok/spring-batch-examples/actions/workflows/batch-to-cloud-1.yaml)
```bash
rm -rf ~/.m2/repository/com/github/daggerok
mvn clean package install -f batch-to-cloud/step-1-plain-old-batch

mvn spring-boot:start -f batch-to-cloud/step-1-plain-old-batch/user-service
mvn spring-boot:start -f batch-to-cloud/step-1-plain-old-batch/payment-service
mvn spring-boot:start -f batch-to-cloud/step-1-plain-old-batch/app

http post :8080/api/launch-payments-report ; http get :8080/api
http get  :8080/actuator/metrics/app.loadAllPaymentsFlow
http get  :8080/actuator/metrics/app.loadAllUsersFlow
http get  :8080/actuator/metrics/app.enrichReportTaskletStep

mvn spring-boot:stop -f batch-to-cloud/step-1-plain-old-batch/app
mvn spring-boot:stop -f batch-to-cloud/step-1-plain-old-batch/user-service
mvn spring-boot:stop -f batch-to-cloud/step-1-plain-old-batch/payment-service

cat ./batch-to-cloud/step-1-plain-old-batch/app/target/payments-report.csv
```

## rtfm
* [Spring Batch testing](https://docs.spring.io/spring-batch/docs/current/reference/html/testing.html)
* [Spring Batch Retry](https://sysout.ru/otkazoustojchivost-v-spring-batch-retry-i-skip/)
* [Retry rollbackOnly Transactions 1](https://stackoverflow.com/questions/19349898/unexpectedrollbackexception-transaction-rolled-back-because-it-has-been-marked)
* [Retry rollbackOnly Transactions 2](https://stackoverflow.com/questions/34902380/commit-failed-while-step-execution-data-was-already-updated)
* https://www.baeldung.com/spring-retry
* [Spring Batch example with Gradle 5.4.1](./spring-batch-example/)
