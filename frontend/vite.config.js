import react from '@vitejs/plugin-react'
import process from 'node:process'
import { defineConfig, loadEnv } from 'vite'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  // dev only: forward API calls to the Spring Boot backend so the browser stays same-origin
  const target = loadEnv(mode, process.cwd(), '').VITE_DEV_PROXY_TARGET || 'http://localhost:8080'

  return {
    plugins: [react()],
    server: {
      proxy: {
        '/api': target,
        '/user': target,
      },
    },
  }
})
