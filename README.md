# Account-service
## Description
The microservice is intented to provide functionality to manage user accounts balances.

## Getting Started
### Documentation
API endpoints documentation for local deployment [API docs](http://localhost:8080/actuator/swagger-ui.html?configUrl=/actuator/api-docs/swagger-config#/).<br>
Replace `localhost` with your host if you are running application on remote server.

### Monitoring
- Health check endpoint http://localhost:8080/actuator/health

### Prerequisites
- Java 17
- Docker

### Run project locally

- prepare local db `docker-compose up -d` where `up` brings up the containers and `-d` runs them in the background.<br>
  You can check that containers are up and running with `docker ps` command.<br>
  In order to stop containers run `docker-compose down`
- build application

```
./gradlew clean build
```

- start application with `local` profile activated. Or you can execute main function in [AccountBalancerApplication.kt](src%2Fmain%2Fkotlin%2Fcom%2Faccount_balancer%2FAccountBalancerApplication.kt) from your IDE .

```
 ./gradlew bootRun --args='--spring.profiles.active=local'
```