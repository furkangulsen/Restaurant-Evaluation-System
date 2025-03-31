@echo off
setlocal enabledelayedexpansion

echo ==========================================
echo =                                        =
echo =     GELISMIS CLI SERVISI BASLATILIYOR  =
echo =                                        =
echo ==========================================
echo.

:: Java kontrolü
where java >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo Java kurulu degil! Lutfen Java kurun ve tekrar deneyin.
    exit /b 1
)

:: Kodlama ayarları ve Spring ayarları (Spring Shell için gerekli)
set JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstdin.encoding=UTF-8
set SPRING_OUTPUT_ANSI_ENABLED=always
set SPRING_PROFILES_ACTIVE=cli
set SPRING_SHELL_INTERACTIVE=true
set SPRING_SHELL_HISTORY_ENABLED=true
set SPRING_SHELL_HISTSIZE=200
set SPRING_MAIN_ALLOW_BEAN_DEFINITION_OVERRIDING=true

echo Spring Boot Shell CLI uygulamasi baslatiliyor...
echo Profil: cli
echo Karakter kodlamasi: UTF-8
echo.

:: Gradle ile uygulamayı başlat
cd %~dp0
gradlew bootRun --args="--spring.profiles.active=cli --cli --spring.main.allow-bean-definition-overriding=true" -Dorg.jline.terminal.type=xterm-256color

:: Hata kontrolü
if %ERRORLEVEL% neq 0 (
    echo.
    echo CLI uygulamasi calisirken bir hata olustu! (Hata kodu: %ERRORLEVEL%)
    echo Lutfen loglari kontrol edin.
    pause
    exit /b 1
)

echo.
echo CLI uygulamasi kapatildi.
pause
exit /b 0
