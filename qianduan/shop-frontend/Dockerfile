FROM node:20-alpine

WORKDIR /app

# 复制依赖描述文件
COPY package*.json ./

# 安装依赖（开发环境可用 npm install）
RUN npm install

# 暴露 Vite 默认的开发端口
EXPOSE 5173

# 启动开发服务器（--host 允许外部/容器外网络访问）
CMD ["npm", "run", "dev", "--", "--host"]