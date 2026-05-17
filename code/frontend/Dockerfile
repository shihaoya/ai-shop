# === Build Stage ===
FROM node:22-alpine AS builder

# 设置 CI 环境，避免 pnpm 在无 TTY 环境下报错
ENV CI=true

# 启用 pnpm
RUN corepack enable && corepack prepare pnpm@latest --activate

WORKDIR /app

# 先复制依赖配置，利用缓存
COPY package.json pnpm-lock.yaml pnpm-workspace.yaml ./
RUN pnpm install --frozen-lockfile

# 复制源码并构建（直接 vite build 跳过 vue-tsc 类型检查，类型检查属于开发阶段）
COPY . .
RUN pnpm exec vite build

# === Runtime Stage ===
FROM nginx:alpine

# 复制构建产物
COPY --from=builder /app/dist /usr/share/nginx/html

# 复制 nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
