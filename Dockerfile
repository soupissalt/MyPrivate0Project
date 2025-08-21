#1) Build Stage
FROM gradle:8.7-jdk17 AS builder
WORKDIR /workspace
COPY . .
RUN gradle bootJar --no-daemon

# 2) Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /workspace/build/libs/*.jar app.jar
EXPOSE 8080

# 도커 프로파일로 실행
ENV SPRING_PROFGILES_ACTIVE=docker
# JMV 옵션
ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]