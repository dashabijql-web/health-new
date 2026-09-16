import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, '.', '')

  return {
    plugins: [vue()],
    server: {
      host: '127.0.0.1',
      port: 5173,
      proxy: {
        '/dev-api': {
          target: env.VITE_API_TARGET || 'http://127.0.0.1:8081',
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/dev-api/, '/health'),
        },
      },
    },
  }
})
