# Security Policy

## Supported versions

Only the latest commit on `main` is supported.

## Reporting a vulnerability

Please do **not** open a public issue for security problems.

- Email: 1815578857@qq.com
- Or use GitHub's private reporting: **Security → Report a vulnerability**

You can expect an acknowledgement within 7 days.

## Scope notes for this project

The backend is designed for local / trusted-network use. Known properties you should
be aware of when deploying it:

- There is **no authentication** and no CSRF protection; anyone who can reach the
  API can read the stored chat history and trigger model requests.
- `GET /stream?prompt=...` takes the prompt as a query parameter, so prompts end up in
  access logs and browser history.
- The MySQL schema stores messages in plain text.
- The frontend renders model output as Markdown; it is sanitized with DOMPurify before
  being inserted into the DOM.

Do not expose this service directly to the public internet without adding
authentication and a reverse proxy that enforces rate limits.
