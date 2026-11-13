# syntax=docker/dockerfile:1
FROM node:26.13.1-alpine AS build
WORKDIR /web
COPY package.json package-lock.json ./
RUN --mount=type=cache,target=/root/.npm \
    npm install --global "npm@$(node -p "require('./package.json').packageManager.split('@')[1]")" \
 && npm ci --no-audit --no-fund
COPY . .
RUN npx ng build

FROM node:26.13.1-alpine
WORKDIR /app
COPY --from=build /web/dist/web/ ./
USER node
ENV NODE_ENV=production PORT=8080
EXPOSE 8080
CMD ["node", "server/server.mjs"]
