@echo off
setlocal EnableDelayedExpansion
cd /d "%~dp0"

set "MIN_JAVA=25"
set "JAVA_EXE="
set "VERSION_FILE=%TEMP%\research-pipeline-java-version.txt"

if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" call :try "%JAVA_HOME%\bin\java.exe"
if not defined JAVA_EXE call :try java
if not defined JAVA_EXE for /d %%D in ("%USERPROFILE%\.jdks\*" "%ProgramFiles%\Java\*" "%ProgramFiles%\Eclipse Adoptium\*" "%ProgramFiles%\Microsoft\*" "%ProgramFiles%\Zulu\*" "%ProgramFiles%\Amazon Corretto\*" "%LOCALAPPDATA%\Programs\Eclipse Adoptium\*") do (
    if not defined JAVA_EXE if exist "%%~D\bin\java.exe" call :try "%%~D\bin\java.exe"
)
if not defined JAVA_EXE (
    echo Java %MIN_JAVA% or newer was not found on PATH, in JAVA_HOME or in the usual install folders.
    echo Install a JDK from https://adoptium.net/temurin/releases/?version=%MIN_JAVA% or set JAVA_HOME, then run this script again.
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
echo Using %JAVA_EXE% ^(Java !FOUND_VERSION!^)
echo Starting the Analyst Research Pipeline on %URL%
echo Close this window to stop the server.
start "" /b cmd /c "timeout /t 3 /nobreak >nul & start "" %URL%"
"%JAVA_EXE%" -jar "%JAR%" --serve
pause
exit /b

:try
set "CANDIDATE=%~1"
set "MAJOR="
"%CANDIDATE%" -version > "%VERSION_FILE%" 2>&1
if errorlevel 1 exit /b
for /f "tokens=3" %%V in ('findstr /i "version" "%VERSION_FILE%"') do (
    set "VERSION=%%~V"
    for /f "delims=." %%M in ("!VERSION!") do set "MAJOR=%%M"
)
if not defined MAJOR exit /b
if !MAJOR! GEQ %MIN_JAVA% (
    set "JAVA_EXE=%CANDIDATE%"
    set "FOUND_VERSION=!VERSION!"
)
exit /b
