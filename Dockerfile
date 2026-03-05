FROM gradle:8.10.2-jdk21-alpine AS build
WORKDIR /src
COPY . .
RUN gradle --no-daemon bootJar -x test

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN apk add --no-cache tzdata curl && \
    cp /usr/share/zoneinfo/Asia/Seoul /etc/localtime && echo "Asia/Seoul" > /etc/timezone

ENV TZ=Asia/Seoul
COPY --from=build /src/build/libs/*.jar /app/app.jar
EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75 -Dfile.encoding=UTF-8 -Duser.timezone=Asia/Seoul"
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app/app.jar"]
