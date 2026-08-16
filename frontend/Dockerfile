FROM node:22.14.0-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
RUN npx ngcc --properties es2023 browser module main --first-only --create-ivy-entry-points
COPY . .

ARG HOST_SERVER_IP

RUN npm run build -- --define GATEWAY_URL=\"http://${HOST_SERVER_IP}:12344\"

FROM nginx:stable-alpine
COPY --from=build /app/dist/frontend/browser/ /usr/share/nginx/html
EXPOSE 80
