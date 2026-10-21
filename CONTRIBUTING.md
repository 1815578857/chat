# Contributing

Thanks for taking a look! This is a small hobby project, but issues and pull requests
are welcome.

## Getting set up

Prerequisites: JDK 17+, Node.js 20+, MySQL 8+, and Ollama.

```bash
# 1. Database
mysql -u root -p -e "CREATE DATABASE chat_db CHARACTER SET utf8mb4"
mysql -u root -p chat_db < chat-back/src/main/resources/chat_db.sql

# 2. Ollama
ollama serve
ollama pull deepseek-r1:1.5b

# 3. Backend (port 8080)
cd chat-back
DB_PASSWORD=yourpassword ./mvnw spring-boot:run

# 4. Frontend (port 5173)
cd chat-app
npm ci
npm run dev
```

Or run everything with Docker: `docker compose up -d`, then
`docker compose exec ollama ollama pull deepseek-r1:1.5b`.

## Configuration

The backend reads configuration from environment variables, so no secrets belong in
`application.properties`:

| Variable | Default | Purpose |
| --- | --- | --- |
| `SERVER_PORT` | `8080` | HTTP port |
| `DB_URL` | `jdbc:mysql://localhost:3306/chat_db?...` | JDBC URL |
| `DB_USERNAME` / `DB_PASSWORD` | `root` / empty | Database credentials |
| `OLLAMA_BASE_URL` | `http://localhost:11434` | Ollama endpoint |
| `OLLAMA_MODEL` | `deepseek-r1:1.5b` | Model name |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | Comma-separated allowed origins |
| `MAX_PROMPT_LENGTH` | `4000` | Maximum prompt length |

The frontend uses `VITE_API_BASE_URL` (see `chat-app/.env.example`).

## Before opening a pull request

```bash
cd chat-back && ./mvnw verify    # backend tests
cd chat-app && npm run build     # frontend build
```

Both run in CI on every pull request.

## Style

- Keep it simple; there is no service layer ceremony beyond what already exists.
- Backend: 4-space indentation, constructor injection, no `System.out`.
- Frontend: Vue 3 `<script setup>`, and always sanitize rendered HTML.
