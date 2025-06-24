#!/bin/bash

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
