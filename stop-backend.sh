#!/bin/bash

if [ ! -f .backend.pid ]; then
  echo "### No .backend.pid found. Searching by process name..."

  # Fallback: Kill by jar name if known
  PID=$(pgrep -f 'inkHeart-backend-.*\.jar')
  if [ -z "$PID" ]; then
    echo "### Could not find backend process."
    exit 1
  fi
else
  PID=$(cat .backend.pid)
fi

echo "### Killing backend process PID: $PID"
kill "$PID" 2>/dev/null || echo "### Warning: Failed to kill PID $PID (may not exist)"
rm -f .backend.pid
echo "### Backend stopped."
