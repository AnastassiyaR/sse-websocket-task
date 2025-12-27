import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
    plugins: [vue()],
    define: {
        global: 'globalThis',
    },
    server: {
        proxy: {
            // SSE endpoint
            '/api': {
                target: 'http://localhost:8080',
                changeOrigin: true,
                secure: false,
            },
            // WebSocket STOMP endpoint
            '/ws': {
                target: 'http://localhost:8080',
                changeOrigin: true,
                ws: true,
            }
        }
    }
})