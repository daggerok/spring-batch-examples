# Spring Batch Micrometer

## Build, run, test

```bash
rm -rf ~/.m2/repository/com/github/daggerok/batch/parallel
cd run-steps-in-parallel-with-flow-split
./mvnw clean package install

mvn spring-boot:start -f apps/user-service
./mvnw spring-boot:start -f apps/payment-service
./mvnw spring-boot:start -f apps/app

http post :8080/api/launch-payments-report ; http get :8080/api
http get  :8080/actuator/metrics/app.loadAllPaymentsFlow
http get  :8080/actuator/metrics/app.loadAllUsersFlow
http get  :8080/actuator/metrics/app.enrichReportTaskletStep

./mvnw spring-boot:stop -f apps/app
./mvnw spring-boot:stop -f apps/user-service
./mvnw spring-boot:stop -f apps/payment-service

cat ./apps/app/target/payments-report.csv
```
