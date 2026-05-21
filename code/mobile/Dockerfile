# === Build Stage ===
FROM node:22-alpine AS builder

ENV CI=true
ENV PNPM_ENABLE_PRE_POST_SCRIPTS=true

RUN corepack enable && corepack prepare pnpm@9.0.0 --activate

WORKDIR /app

COPY package.json .npmrc ./
RUN pnpm install

COPY . .
RUN pnpm exec vite build

# === Runtime Stage ===
FROM nginx:alpine

COPY --from=builder /app/dist /usr/share/nginx/html

COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]