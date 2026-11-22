#!/usr/bin/env bash
set -e

ENV_FILE="../.env"

if [ ! -f "$ENV_FILE" ]; then
  echo "Error: $ENV_FILE not found."
  exit 1
fi

# Strip Windows CRLF (\r) if present so variables don't pick up trailing \r
sed -i 's/\r$//' "$ENV_FILE"

set -a
source "$ENV_FILE"
set +a

# Optional: allow passing a port override as the first argument, e.g. ./run.sh 8079
if [ -n "$1" ]; then
  export SERVER_PORT="$1"
  export AUTH_SERVER_PORT="$1"
  ./mvnw spring-boot:run -Dspring-boot.run.arguments="--server.port=$1"
else
  ./mvnw spring-boot:run
fi