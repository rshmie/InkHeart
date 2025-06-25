#!/bin/bash

# Read .env file if it exists
if [ -f .env ]; then
  echo "Loading environment variables from .env file..."
  export $(grep -v '^#' .env | xargs)
fi

# Check if JWT_SECRET is set
if [ -z "$JWT_SECRET" ]; then
    echo "Error: JWT_SECRET environment variable is not set."
    echo "Please set it before running, or add it to a .env file."
    exit 1
fi

echo "### Building the InkHeart Project with the Maven Wrapper..."
chmod +x ./mvnw
./mvnw clean install

if [ $? -ne 0 ]; then
  echo "### Build failed."
  exit 1
fi

echo "### Starting Spring Boot backend."
nohup java -jar inkHeart-backend/target/inkHeart-backend-*.jar > backend.log 2>&1 < /dev/null &
echo $! > .backend.pid

BACKEND_PID=$!
echo $BACKEND_PID > .backend.pidjps

echo "### Backend started with PID $BACKEND_PID"
sleep 15


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
