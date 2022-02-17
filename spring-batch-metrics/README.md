# Spring Batch Micrometer

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
