FROM amazoncorretto:21 AS build
WORKDIR /app

COPY . .

RUN chmod +x ./gradlew
RUN ./gradlew clean build -x validateStructure -x test

FROM amazoncorretto:21
WORKDIR /app

COPY --from=build /app/applications/app-service/build/libs/credit-aplication-microservice.jar app.jar

EXPOSE 8090

ENTRYPOINT ["java","-jar","app.jar"]
