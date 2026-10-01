@echo off
setlocal
cd /d "%~dp0"

where java >nul 2>nul
if errorlevel 1 (
    echo Java 25 or newer is required. Install a JDK, then run this script again.
    pause
    exit /b 1
)

set "JAR=research-pipeline.jar"
if not exist "%JAR%" set "JAR=app\target\research-pipeline.jar"
if not exist "%JAR%" (
    echo research-pipeline.jar not found. Download the release zip or build with mvnw -B package.
    pause
    exit /b 1
)

set "URL=http://127.0.0.1:8787"
echo Starting the Analyst Research Pipeline on %URL%
echo Close this window to stop the server.
start "" /b cmd /c "timeout /t 3 /nobreak >nul & start "" %URL%"
java -jar "%JAR%" --serve
pause
