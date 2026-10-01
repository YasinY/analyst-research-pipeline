#!/usr/bin/env sh
set -e
cd "$(dirname "$0")"

MIN_JAVA=25
URL=http://127.0.0.1:8787

major_of() {
    "$1" -version 2>&1 | head -n 1 | sed -E 's/.*"([0-9]+)[^"]*".*/\1/'
}

find_java() {
    for candidate in "${JAVA_HOME:-/nonexistent}/bin/java" "$(command -v java 2>/dev/null || echo /nonexistent)" \
        "$HOME"/.jdks/*/bin/java "$HOME"/.sdkman/candidates/java/*/bin/java \
        /usr/lib/jvm/*/bin/java /Library/Java/JavaVirtualMachines/*/Contents/Home/bin/java \
        /opt/homebrew/opt/openjdk*/bin/java /usr/local/opt/openjdk*/bin/java; do
        [ -x "$candidate" ] || continue
        major="$(major_of "$candidate" || true)"
        case "$major" in
            ''|*[!0-9]*) continue ;;
        esac
        if [ "$major" -ge "$MIN_JAVA" ]; then
            echo "$candidate"
            return 0
        fi
    done
    return 1
}

JAVA_EXE="$(find_java || true)"
if [ -z "$JAVA_EXE" ]; then
    echo "Java $MIN_JAVA or newer was not found on PATH, in JAVA_HOME or in the usual install folders."
    echo "Install a JDK from https://adoptium.net/temurin/releases/?version=$MIN_JAVA or set JAVA_HOME, then run this script again."
    exit 1
fi

JAR=research-pipeline.jar
[ -f "$JAR" ] || JAR=app/target/research-pipeline.jar
if [ ! -f "$JAR" ]; then
    echo "research-pipeline.jar not found. Download the release zip or build with ./mvnw -B package."
    exit 1
fi

echo "Using $JAVA_EXE (Java $(major_of "$JAVA_EXE"))"
echo "Starting the Analyst Research Pipeline on $URL"
echo "Press Ctrl+C to stop the server."
(
    sleep 3
    if command -v xdg-open >/dev/null 2>&1; then xdg-open "$URL" >/dev/null 2>&1
    elif command -v open >/dev/null 2>&1; then open "$URL"
    fi
) &
exec "$JAVA_EXE" -jar "$JAR" --serve
