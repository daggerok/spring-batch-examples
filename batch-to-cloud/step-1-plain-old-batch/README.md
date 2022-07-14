# Spring Batch Micrometer

## Build, run, test

```bash
rm -rf ~/.m2/repository/com/github/daggerok
cd batch-to-cloud/step-1-plain-old-batch
./mvnw clean package install

mvn spring-boot:start -f user-service
./mvnw spring-boot:start -f payment-service
./mvnw spring-boot:start -f app

http post :8080/api/launch-payments-report ; http get :8080/api
http get  :8080/actuator/metrics/app.loadAllPaymentsFlow
http get  :8080/actuator/metrics/app.loadAllUsersFlow
http get  :8080/actuator/metrics/app.enrichReportTaskletStep

./mvnw spring-boot:stop -f app
./mvnw spring-boot:stop -f user-service
./mvnw spring-boot:stop -f payment-service

cat ./app/target/payments-report.csv
```
