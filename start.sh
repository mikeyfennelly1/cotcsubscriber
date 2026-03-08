#!/bin/bash

################################################
# SETUP
################################################
OS=$(uname)
if [[ "$OS" == "Darwin" ]]; then
	# OSX uses BSD readlink
	BASEDIR="$(dirname "$0")"
else
	BASEDIR=$(readlink -e "$(dirname "$0")/")
fi
cd "${BASEDIR}"

source "${BASEDIR}"/../.env.local
source "${BASEDIR}"/../scripts/helpers.sh

SERVICE_NAME="web-app"

################################################
# Wait for postgres to be healthy
################################################

: "${POSTGRES_HOST:=localhost}"
: "${POSTGRES_PORT:?POSTGRES_PORT must be set}"
: "${POSTGRES_USER:?POSTGRES_USER must be set}"
echo "Waiting indefinitely for PostgreSQL at ${POSTGRES_HOST}:${POSTGRES_PORT}..."

while true; do
  if nc -z "${POSTGRES_HOST}" "${POSTGRES_PORT}" >/dev/null 2>&1; then
    echo "PostgreSQL is accepting TCP connections on port ${POSTGRES_PORT}"
    break
  fi

  sleep 1
done

################################################
# Start API
################################################
var_must_exist POSTGRES_USER POSTGRES_PASSWORD POSTGRES_DB WEB_APP_PORT
echo "POSTGRES_USER=${POSTGRES_USER}"
echo "POSTGRES_PASSWORD=${POSTGRES_PASSWORD}"
echo "POSTGRES_DB=${POSTGRES_DB}"
echo "WEB_APP_PORT=${WEB_APP_PORT}"

echo "${SERVICE_NAME}: Starting API..."
export POSTGRES_USER POSTGRES_PASSWORD POSTGRES_DB WEB_APP_PORT
"${BASEDIR}"/gradlew bootRun
