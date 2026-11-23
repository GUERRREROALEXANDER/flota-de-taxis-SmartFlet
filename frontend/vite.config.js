import { defineConfig } from 'vite';

// El backend (Spring Boot) corre en el puerto 8080 y el frontend en el 5173.
// El proxy reenvia toda peticion a /api al backend: para el navegador todo viene del
// mismo sitio, asi que no hay problemas de CORS y el backend no necesita cambios.
// Si el backend usa otro puerto: BACKEND_URL=http://localhost:9090 npm run dev
const backend = process.env.BACKEND_URL ?? 'http://localhost:8080';
const proxyAlBackend = { '/api': backend };

export default defineConfig({
  server: { port: 5173, proxy: proxyAlBackend },
  preview: { port: 4173, proxy: proxyAlBackend },
});
