# Vue
FROM node:20-alpine AS frontend-build
WORKDIR /frontend
COPY frontend/package*.json ./
RUN npm install
COPY frontend/ ./
RUN npm run build

# Spring Boot
FROM eclipse-temurin:21-jdk-alpine AS backend-build
WORKDIR /app
COPY backend/ ./
RUN chmod +x ./gradlew
RUN ./gradlew clean build -x test

# Nginx + Java
FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache nginx gettext && \
    mkdir -p /var/log/nginx /run/nginx

COPY nginx/nginx.conf /etc/nginx/nginx.conf.template
COPY --from=frontend-build /frontend/dist /usr/share/nginx/html
COPY --from=backend-build /app/build/libs/*.jar /app/app.jar

RUN printf '#!/bin/sh\n\
export PORT=${PORT:-10000}\n\
envsubst "\\$PORT" < /etc/nginx/nginx.conf.template > /etc/nginx/nginx.conf\n\
nginx\n\
exec java -Dserver.port=8080 -jar /app/app.jar\n' > /start.sh && \
    chmod +x /start.sh

EXPOSE ${PORT:-10000}

CMD ["/start.sh"]