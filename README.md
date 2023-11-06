# Account-service
## Description
The microservice is intended to provide functionality to manage user accounts balances.

## Getting Started
### Documentation
API endpoints documentation for local deployment [API docs](http://localhost:8080/actuator/swagger-ui.html?configUrl=/actuator/api-docs/swagger-config#/).<br>
Replace `localhost` with your host if you are running application on remote server.

### Monitoring
- Health check endpoint http://localhost:8080/actuator/health

### Prerequisites
- Java 17
- Docker

## Run project locally

All commands should be executed from project root directory unless otherwise specified.
- #### run local db by using :
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
- #### build application

```
./gradlew clean build
```

- ####  start application with `local` profile activated. Or you can execute main function in [AccountBalancerApplication.kt](src%2Fmain%2Fkotlin%2Fcom%2Faccount_balancer%2FAccountBalancerApplication.kt) from your IDE .

```
 ./gradlew bootRun --args='--spring.profiles.active=local'
```
- ####  you can also run application as docker container as well:
```
docker-compose up -d
```
this command deploy locally both application and database containers. <br>
- also you can build a new docker image and run it locally:
```
docker build -t accounts_service .
```
other information about building, pushing and deploying docker images can be found here https://docs.docker.com/get-started/02_our_app/

### Features
- Users can create new accounts
- Users can view account balance
- Users can book money for a specific account and tenant
- Users can view money transactions history by different filters
Information endpoints can be found in open-api auto-generated documentation mentioned above. 

### Further improvements
- The process of money transfer can eventually become more complicated and require more steps. 
Current implementation it's just a quick demo of the idea. 
Further development might require decoupling the process by using some kind of message broker. 
Decoupling this process into separate steps will require implementation of some kind of saga pattern to handle possible failures.
MoneyBookingStatus enum will have to be extended with more statuses to handle all possible scenarios.
- Booking, cancelling, and viewing account balance operation utilizing REPEATABLE_READ isolation level in order to avoid read skews and lost updates, 
but it might not be enough for all write skews scenarios. 
If that's the case we might need to use some kind of explicit locking mechanism to prevent those anomalies.
- ledger_entry_audit is supposed to be immutable, there are rules to ignore updates and deletes, but it might be a better idea to throw an error.
- there is an audit mechanism to catch all updates to tables. Consider implementing alerting mechanism, since those tables are not supposed to be updated manually.
- It might be a good idea in function audit_tables() capture what exactly changed: what value was deleted, what was the value before the update, what is the value after the update
- Auditing mechanism using triggers might be not reliable, it might be a good idea to implement event sourcing pattern to capture all events.