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

All commands should be executed from project root directory unless otherwise specified.
- run local db by using :
 ```
docker-compose up -d account_service_postgreql
```
  where `up` brings up the containers and `-d` runs them in the background.<br>
  You can check that containers are up and running with command: 
  ```
  docker ps
  ```
  In order to stop containers run: 
  ```
  docker-compose down
  ```
- build application

```
./gradlew clean build
```

- start application with `local` profile activated. Or you can execute main function in [AccountBalancerApplication.kt](src%2Fmain%2Fkotlin%2Fcom%2Faccount_balancer%2FAccountBalancerApplication.kt) from your IDE .

```
 ./gradlew bootRun --args='--spring.profiles.active=local'
```
- you can also run application as docker container as well:
```
docker-compose up -d
```
this command deploy locally both application and database containers. <br>
- also you can build a new docker image and run it locally:
```
docker build -t accounts_service .
```
other information about building, pushing and deploying docker images can be found here https://docs.docker.com/get-started/02_our_app/