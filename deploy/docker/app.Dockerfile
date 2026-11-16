# Image cho gate-agent và central: build giao diện React (target tương ứng) rồi đóng gói vào static/ của Spring Boot.
# Build context = thư mục gốc repo tmc.
#   docker build -f deploy/docker/app.Dockerfile --build-arg MODULE=gate-agent --build-arg WEB_TARGET=gate .
#   docker build -f deploy/docker/app.Dockerfile --build-arg MODULE=central    --build-arg WEB_TARGET=central .

ARG MODULE
ARG WEB_TARGET

# ---- 1. Giao diện ----
FROM node:22-alpine AS web
ARG WEB_TARGET
WORKDIR /web
COPY web/package.json web/package-lock.json* ./
RUN if [ -f package-lock.json ]; then npm ci; else npm install; fi
COPY web/ ./
RUN npx vite build --mode ${WEB_TARGET} --outDir /out --emptyOutDir

# ---- 2. Backend ----
FROM maven:3.9-eclipse-temurin-21 AS build
ARG MODULE
WORKDIR /src
COPY . .
COPY --from=web /out ${MODULE}/src/main/resources/static/
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q -pl ${MODULE} -am package -DskipTests

# ---- 3. Runtime ----
FROM eclipse-temurin:21-jre
ARG MODULE
WORKDIR /app
COPY --from=build /src/${MODULE}/target/${MODULE}.jar app.jar
RUN mkdir -p /data
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
