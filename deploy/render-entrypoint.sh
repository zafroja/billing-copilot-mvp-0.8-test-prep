#!/bin/sh
set -eu
# Parse Render's internal Postgres URL without printing credentials.
: "${RENDER_DATABASE_URL:?Render database URL must be configured}"
: "${DATABASE_USER:?Render database user must be configured}"
: "${DATABASE_PASSWORD:?Render database password must be configured}"
: "${RENDER_EXTERNAL_HOSTNAME:?Render external hostname must be configured}"
: "${PILOT_USERNAME:?Pilot username is required}"
: "${TOKEN_ENCRYPTION_KEY_B64:?Encryption key is required}"
: "${JOBBER_CLIENT_ID:?Jobber client ID is required}"
: "${JOBBER_CLIENT_SECRET:?Jobber client secret is required}"
if [ -z "${PILOT_PASSWORD_BCRYPT:-}" ] && [ -z "${PILOT_PASSWORD:-}" ]; then
  echo "Set PILOT_PASSWORD (16+ characters) or PILOT_PASSWORD_BCRYPT in Render's secret settings." >&2
  exit 1
fi
case "$RENDER_DATABASE_URL" in postgres://*|postgresql://*) ;; *) echo "Invalid Render database URL scheme" >&2; exit 1 ;; esac
authority_and_database="${RENDER_DATABASE_URL#*://}"
case "$authority_and_database" in *@*/*) ;; *) echo "Invalid Render database URL structure" >&2; exit 1 ;; esac
host_and_database="${authority_and_database#*@}"
host_and_database="${host_and_database%%\?*}"
export DATABASE_URL="jdbc:postgresql://${host_and_database}"
export SERVER_BIND_ADDRESS=0.0.0.0
export SERVER_PORT="${PORT:-10000}"
export APP_FRONTEND_ORIGIN="https://${RENDER_EXTERNAL_HOSTNAME}"
export JOBBER_REDIRECT_URI="https://${RENDER_EXTERNAL_HOSTNAME}/api/integrations/jobber/callback"
exec java -jar /app/billing-copilot.jar
