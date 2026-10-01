#!/usr/bin/env sh
set -e
cd "$(dirname "$0")"

if ! command -v java >/dev/null 2>&1; then
    echo "Java 25 or newer is required. Install a JDK, then run this script again."
    exit 1
fi

JAR=research-pipeline.jar
[ -f "$JAR" ] || JAR=app/target/research-pipeline.jar
if [ ! -f "$JAR" ]; then
    echo "research-pipeline.jar not found. Download the release zip or build with ./mvnw -B package."
    exit 1
fi

URL=http://127.0.0.1:8787
echo "Starting the Analyst Research Pipeline on $URL"
echo "Press Ctrl+C to stop the server."
(
    sleep 3
    if command -v xdg-open >/dev/null 2>&1; then xdg-open "$URL" >/dev/null 2>&1
    elif command -v open >/dev/null 2>&1; then open "$URL"
    fi
) &
exec java -jar "$JAR" --serve
