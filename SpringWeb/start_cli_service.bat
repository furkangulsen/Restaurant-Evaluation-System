@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ==========================================
echo =                                        =
echo =        STARTING CLI SERVICE            =
echo =                                        =
echo ==========================================
echo.

REM Set environment variables
set SPRING_OUTPUT_ANSI_ENABLED=ALWAYS
set SPRING_PROFILES_ACTIVE=cli
set FILE_ENCODING=UTF-8
set JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8

REM Check Java
where java >nul 2>nul
if errorlevel 1 (
    color 0C
    echo [ERROR] Java not found! 
    echo Please install Java and add it to PATH.
    echo.
    pause
    exit /b 1
)

echo Starting Spring Boot CLI application...
echo Profile: %SPRING_PROFILES_ACTIVE%
echo Character set: %FILE_ENCODING%
echo.

REM Start with Gradle 
cd /d %~dp0
call gradlew.bat bootRun ^
  -Dspring.profiles.active=cli ^
  -Dspring.main.allow-circular-references=true ^
  -Dspring.main.allow-bean-definition-overriding=true ^
  -Dfile.encoding=UTF-8 ^
  -Dsun.jnu.encoding=UTF-8 ^
  -Dstdout.encoding=UTF-8 ^
  -Dstdin.encoding=UTF-8 ^
  --console=plain ^
  --no-daemon

REM Error check
if errorlevel 1 (
    echo.
    echo [ERROR] Failed to start CLI application
    echo Error code: %ERRORLEVEL%
    echo.
    pause
    exit /b %ERRORLEVEL%
)

endlocal
pause 