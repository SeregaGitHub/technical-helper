# FROM node:alpine3.22 AS build

# WORKDIR /usr/local/app

# COPY ./ /usr/local/app/

# RUN npm install

# RUN npm run build



# FROM nginx:stable

# COPY --from=build /usr/local/app/dist/frontend /usr/share/nginx/html

# EXPOSE 80


# Work only localhost
# FROM node:alpine3.22 AS build

# WORKDIR /app

# COPY package*.json ./
# # # Чтобы вызвать ng serve, нужно глобально установить @angular/cli
# RUN npm install -g @angular/cli@19

# RUN npm ci

# COPY . .

# EXPOSE 12345

# CMD ["ng", "serve", "--host", "0.0.0.0"]


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
# FROM nginx:alpine

# # Меняем конфиг nginx-а на собственный
# COPY nginx.conf /etc/nginx/nginx.conf

# # Копируем собранное приложение из предыдущего этапа в рабочую директорию nginx
# COPY --from=build /dist/frontend/browser /usr/share/nginx/html

# EXPOSE 80

# Запускаем nginx
#CMD ["nginx", "-g", "daemon off;"]


# youtube
# FROM node:alpine3.22 AS build

# RUN mkdir -p /app

# WORKDIR /app

# COPY . .

# RUN npm install -g @angular/cli@19
# RUN npm run build --prod

# CMD ["npm", "start"]




# youtube - 1

# FROM node:alpine3.22 AS build-stage
# WORKDIR /app
# COPY package*.json ./
# RUN npm install
# COPY .. .
# RUN npm run build

# FROM nginx:alpine
# COPY --from=build-stage /app/dist/frontend /usr/share/nginx/html
# COPY nginx.conf /etc/nginx/conf.d/default.conf


# youtube - 1

FROM node:22.14.0-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
RUN npx ngcc --properties es2023 browser module main --first-only --create-ivy-entry-points   
COPY . .
RUN npm run build

FROM nginx:stable-alpine
COPY --from=build /app/dist/frontend/browser/ /usr/share/nginx/html
EXPOSE 80
# COPY nginx.conf /etc/nginx/conf.d/default.conf