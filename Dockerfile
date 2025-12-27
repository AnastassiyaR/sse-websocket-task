# ============================================
# Этап 1: Собираем фронтенд (Vue)
# ============================================
FROM node:20-alpine AS frontend-build
WORKDIR /frontend
COPY frontend/package*.json ./
RUN npm install
COPY frontend/ ./
RUN npm run build

# ============================================
# Этап 2: Собираем бэкенд (Spring Boot)
# ============================================
FROM eclipse-temurin:21-jdk-alpine AS backend-build
WORKDIR /app
COPY backend/ ./
RUN chmod +x ./gradlew
RUN ./gradlew clean build -x test

# ============================================
# Этап 3: Финальный образ с Nginx + Java
# ============================================
FROM eclipse-temurin:21-jre-alpine

# Устанавливаем Nginx и зависимости
RUN apk add --no-cache nginx gettext

# Копируем шаблон конфига Nginx
COPY nginx/nginx.conf /etc/nginx/nginx.conf.template

# Копируем собранный фронтенд в папку Nginx
COPY --from=frontend-build /frontend/dist /usr/share/nginx/html

# Копируем собранный бэкенд
WORKDIR /app
COPY --from=backend-build /app/build/libs/*.jar app.jar

# Создаём папки для логов Nginx
RUN mkdir -p /var/log/nginx && \
    mkdir -p /run/nginx

# Создаём стартовый скрипт
RUN echo '#!/bin/sh' > /start.sh && \
    echo 'export PORT=${PORT:-10000}' >> /start.sh && \
    echo 'envsubst "\$PORT" < /etc/nginx/nginx.conf.template > /etc/nginx/nginx.conf' >> /start.sh && \
    echo 'nginx' >> /start.sh && \
    echo 'exec java -Dserver.port=8080 -jar /app/app.jar' >> /start.sh && \
    chmod +x /start.sh

EXPOSE ${PORT:-10000}

CMD ["/start.sh"]
