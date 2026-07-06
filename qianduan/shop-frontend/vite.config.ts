import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'node:path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
    },
  },
  server: {
    host: '0.0.0.0', // 必须加上这行，允许通过 Docker 容器端口映射访问
    port: 5173,
    proxy: {
      '/api': {
        // 关键改动：将 localhost 改为你在 docker-compose.yml 中定义的后端服务名 app
        target: 'http://127.0.0.1:8080',
        changeOrigin: true,
        // 如果你的后端接口路径本来就带 /api（例如 @RequestMapping("/api/user")），下面这行就注释掉
        // 如果后端接口路径不带 /api（例如直接是 @RequestMapping("/user")），则需要取消下面这行的注释来重写路径
        // rewrite: (path) => path.replace(/^\/api/, '')
      },
    },
  },
})