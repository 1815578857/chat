# Local AI Chat — Vue 3 + Spring Boot + Ollama

English | [简体中文](README.zh-CN.md)

[![CI](https://github.com/1815578857/chat/actions/workflows/ci.yml/badge.svg)](https://github.com/1815578857/chat/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A small, readable full-stack AI chat app that runs entirely on your own machine.
A Vue 3 frontend streams answers from a local LLM (via [Ollama](https://ollama.com))
through a Spring Boot backend using Server-Sent Events, and chat history is stored in MySQL.

It's meant as a **reference implementation** for a common stack: if you are a Java
developer who wants to see how Spring AI streaming, SSE and a Vue chat UI fit
together end to end, this repo is short enough to read in one sitting.

## Features

- **Token-by-token streaming** — Spring AI `Flux<String>` → SSE → `EventSource` in the browser
- **Reasoning view** — `<think>…</think>` blocks from reasoning models (e.g. DeepSeek-R1) render as a collapsible "thinking" panel
- **Persistent history** — every prompt and full answer is saved to MySQL via MyBatis-Plus
- **Markdown rendering** — sanitized with DOMPurify before it reaches the DOM
- **Private by default** — the model runs locally; no data leaves your machine
- **Configured by env vars** — no secrets in the repo; Docker Compose for one-command setup

## Architecture

```
┌──────────────┐  HTTP / SSE  ┌──────────────────┐   HTTP   ┌──────────────┐
│  Vue 3 UI    │ ───────────► │  Spring Boot 3   │ ───────► │   Ollama     │
│  Element Plus│ ◄─────────── │  Spring AI       │ ◄─────── │  (local LLM) │
│  :5173       │   tokens     │  :8080           │  tokens  │  :11434      │
└──────────────┘              └────────┬─────────┘          └──────────────┘
                                       │ MyBatis-Plus
                                       ▼
                                ┌──────────────┐
                                │   MySQL 8    │
                                └──────────────┘
```

## Quick start (Docker)

```bash
git clone https://github.com/1815578857/chat.git && cd chat
docker compose up -d
docker compose exec ollama ollama pull deepseek-r1:1.5b   # first run only
```

Open <http://localhost:5173>.

## Manual setup

Prerequisites: JDK 17+, Node.js 20+, MySQL 8+, Ollama.

```bash
# Database
mysql -u root -p -e "CREATE DATABASE chat_db CHARACTER SET utf8mb4"
mysql -u root -p chat_db < chat-back/src/main/resources/chat_db.sql

# Model
ollama pull deepseek-r1:1.5b

# Backend → http://localhost:8080
cd chat-back
DB_PASSWORD=yourpassword ./mvnw spring-boot:run

# Frontend → http://localhost:5173
cd chat-app
npm ci && npm run dev
```

## Configuration

| Variable | Default | Purpose |
| --- | --- | --- |
| `DB_URL` | `jdbc:mysql://localhost:3306/chat_db?...` | JDBC URL |
| `DB_USERNAME` / `DB_PASSWORD` | `root` / empty | Database credentials |
| `OLLAMA_BASE_URL` | `http://localhost:11434` | Ollama endpoint |
| `OLLAMA_MODEL` | `deepseek-r1:1.5b` | Any model you have pulled |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | Comma-separated allowed origins |
| `MAX_PROMPT_LENGTH` | `4000` | Prompt length limit |
| `VITE_API_BASE_URL` (frontend) | `http://localhost:8080` | Backend URL, see `chat-app/.env.example` |

## API

| Method | Path | Description |
| --- | --- | --- |
| `GET` | `/messages` | Full chat history |
| `GET` | `/stream?prompt=...` | Streams the answer as SSE events, ending with `[DONE]` |
| `GET` | `/health` | Liveness check |

Swagger UI is available at `/swagger-ui.html` while the backend is running.

## Project layout

```
chat-app/    Vue 3 + Vite frontend (ChatView.vue holds the chat UI and SSE client)
chat-back/   Spring Boot backend (MessageController streams from Ollama and persists messages)
docker-compose.yml   MySQL + Ollama + backend + frontend
```

## Tests

```bash
cd chat-back && ./mvnw verify   # web-layer tests, no MySQL/Ollama required
cd chat-app && npm run build
```

Both run in GitHub Actions on every push and pull request.

## Roadmap

- [ ] Multiple conversations instead of one global history
- [ ] OpenAI-compatible provider support alongside Ollama
- [ ] Stop / regenerate buttons for a streaming answer
- [ ] Move `/stream` to `POST` so prompts don't appear in URLs and logs
- [ ] Optional authentication for non-local deployments
- [ ] Frontend unit tests (Vitest)

## Security

This app has **no authentication** and is intended for local or trusted-network use.
See [SECURITY.md](SECURITY.md) for details and how to report a vulnerability.

## Contributing

Issues and PRs are welcome — see [CONTRIBUTING.md](CONTRIBUTING.md).

## License

[MIT](LICENSE) © 2025 shuishan
