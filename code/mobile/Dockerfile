# === Build Stage ===
FROM node:22-alpine AS builder

ENV CI=true

RUN corepack enable && corepack prepare pnpm@latest --activate

WORKDIR /app

COPY package.json pnpm-lock.yaml pnpm.workspace.yaml ./
RUN pnpm install --frozen-lockfile

COPY . .
RUN pnpm exec vite build

# === Runtime Stage ===
FROM nginx:alpine

COPY --from=builder /app/dist /usr/share/nginx/html

COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]