#!/usr/bin/env sh
set -eu

SERVICE="${1:-}"
TAIL="${TAIL:-200}"

if [ -n "$SERVICE" ]; then
  docker compose --env-file .env logs -f --tail "$TAIL" "$SERVICE"
else
  docker compose --env-file .env logs -f --tail "$TAIL"
fi
