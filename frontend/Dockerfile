# FROM node:alpine3.22 AS build

# WORKDIR /usr/local/app

# COPY ./ /usr/local/app/

# RUN npm install

# RUN npm run build



# FROM nginx:stable

# COPY --from=build /usr/local/app/dist/frontend /usr/share/nginx/html

# EXPOSE 80


FROM node:alpine3.22
WORKDIR /app

COPY package*.json ./
# Чтобы вызвать ng serve, нужно глобально установить @angular/cli
RUN npm install -g @angular/cli@19
RUN npm ci
COPY . .

EXPOSE 12345
CMD ["ng", "serve", "--host", "0.0.0.0"]


# frontend/Dockerfile

# Stage 1: Сборка приложения
# FROM node:alpine3.22 AS build
# WORKDIR /app

# COPY package*.json ./
# RUN npm install -g @angular/cli@19
# RUN npm ci
# COPY . .
# RUN ng build

# # Stage 2: Запуск nginx
# FROM nginx:stable

# # Меняем конфиг nginx-а на собственный
# COPY nginx.conf /etc/nginx/nginx.conf

# # Копируем собранное приложение из предыдущего этапа в рабочую директорию nginx
# COPY --from=build /app/dist/frontend /usr/share/nginx/html
# EXPOSE 12345

# # Запускаем nginx
# CMD ["nginx", "-g", "daemon off;"]