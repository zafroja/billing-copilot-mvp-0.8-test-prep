#!/bin/sh
set -eu
# Render's PostgreSQL URL is not a JDBC URL. Remove its userinfo only
# (credentials are supplied separately via DATABASE_USER/PASSWORD).
: "${RENDER_DATABASE_URL:?Render database URL must be configured}"
: "${DATABASE_USER:?Render database user must be configured}"
: "${DATABASE_PASSWORD:?Render database password must be configured}"
case "$RENDER_DATABASE_URL" in
  postgres://*|postgresql://*) ;;
  *) echo "Unexpected Render database URL scheme" >&2; exit 1 ;;
esac
HOST_AND_DATABASE="${RENDER_DATABASE_URL#*@}"
if [ "$HOST_AND_DATABASE" = "$RENDER_DATABASE_URL" ]; then
  echo "Render database URL is missing expected credentials/host format" >&2
  exit 1
fi
export DATABASE_URL="jdbc:postgresql://${HOST_AND_DATABASE}"
exec java -jar /app/billing-copilot.jar
