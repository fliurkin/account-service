FROM amazoncorretto:17-alpine

RUN apk --update add curl unzip bash

RUN mkdir /opt/app
COPY build/libs/AccountService.jar /opt/app
CMD ["java", "-XX:+UseContainerSupport", "-XX:InitialRAMPercentage=70.0", "-XX:MaxRAMPercentage=70.0", "-jar", "/opt/app/AccountService.jar"]
