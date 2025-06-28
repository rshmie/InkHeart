#!/bin/bash

ONLY_CLI=false

# Parse flag
if [[ "$1" == "--only-cli" ]]; then
  ONLY_CLI=true
elif [[ -n "$1" ]]; then
  echo "Unknown option: $1"
  echo "Usage: ./start-inkHeart.sh [--only-cli]"
  exit 1
fi

if ! $ONLY_CLI; then
  echo "### Building the InkHeart Project with the Maven Wrapper..."
  chmod +x ./mvnw
  ./mvnw clean install

  if [ $? -ne 0 ]; then
    echo "### Build failed."
    exit 1
  fi

  echo "### Starting Spring Boot backend."
  nohup java -jar inkHeart-backend/target/inkHeart-backend-*.jar \
    --spring.config.location=inkHeart-backend/jwt.properties \
    > backend.log 2>&1 < /dev/null &

  BACKEND_PID=$!
  echo $BACKEND_PID > .backend.pid
  echo $BACKEND_PID > .backend.pidjps

  echo "### Backend started with PID $BACKEND_PID"
  sleep 10
fi

echo "### Starting InkHeart CLI..."
java -jar inkHeart-cli/target/inkHeart-cli.jar

echo "### CLI exited."

echo -n "Stop the backend now? (y/n): "
read choice
if [[ "$choice" =~ ^[Yy]$ ]]; then
    ./stop-backend.sh
else
    echo "### Backend still running. Use ./stop-backend.sh to stop it."
fi
