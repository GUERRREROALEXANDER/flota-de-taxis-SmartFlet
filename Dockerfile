# SmartFlet en una sola imagen: el backend Spring Boot sirve tambien el frontend.
#   Etapa 1 (node)  → compila el frontend con Vite (frontend/dist)
#   Etapa 2 (maven) → copia dist/ a src/main/resources/static y arma el .jar
#   Etapa 3 (jre)   → imagen final pequena, solo con Java y el .jar
#
# Construir y probar en tu PC (si tienes Docker):  docker compose up --build

# ---------- 1. Frontend ----------
FROM node:22-alpine AS frontend
WORKDIR /app
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci --no-audit --no-fund
COPY frontend/ ./
RUN npm run build

# ---------- 2. Backend ----------
FROM maven:3.9-eclipse-temurin-21 AS backend
WORKDIR /app
# Primero solo el pom: Docker reutiliza las dependencias descargadas si el pom no cambia.
COPY backend/pom.xml ./
RUN mvn -q -B dependency:go-offline
COPY backend/src ./src
# Spring Boot sirve automaticamente lo que haya en resources/static (index.html en "/").
COPY --from=frontend /app/dist ./src/main/resources/static
# Las pruebas se corren en tu PC (./mvnw test); aqui se omiten porque descargan un MongoDB de ~600 MB.
RUN mvn -q -B package -DskipTests

# ---------- 3. Imagen final ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# No correr como root dentro del contenedor.
RUN addgroup -S smartflet && adduser -S smartflet -G smartflet
COPY --from=backend /app/target/*.jar app.jar
USER smartflet
EXPOSE 8080
# JAVA_OPTS se define en docker-compose.yml (limite de memoria para instancias pequenas).
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
