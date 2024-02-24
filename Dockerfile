FROM gradle:latest AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN ./gradlew jar

FROM amazoncorretto:21-alpine AS jar
RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/*.jar /app/app.jar
EXPOSE 8000
CMD java -jar /app/app.jar