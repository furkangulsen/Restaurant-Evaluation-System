@echo off
setlocal enabledelayedexpansion

echo ==========================================
echo =                                        =
echo =        WEB SERVISI BASLATILIYOR        =
echo =                                        =
echo ==========================================
echo.

REM Environment değişkenleri ayarla
set SPRING_OUTPUT_ANSI_ENABLED=ALWAYS
set SPRING_PROFILES_ACTIVE=web
set SERVER_PORT=8080

REM Java kontrolü
where java >nul 2>nul
if errorlevel 1 (
    color 0C
    echo [HATA] Java bulunamadi! 
    echo Lutfen Java'yi yukleyin ve PATH'e ekleyin.
    echo.
    pause
    exit /b 1
)

REM Port kontrolü
netstat -ano | findstr ":8080" | findstr "LISTENING"
if %ERRORLEVEL% EQU 0 (
    echo [UYARI] 8080 portu zaten kullanımda.
    echo Önce mevcut uygulamayı kapatmayı deneyin...
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8080" ^| findstr "LISTENING"') do (
        if "%%a" NEQ "0" (
            echo 8080 portunu kullanan işlem kapatılıyor (PID: %%a)
            taskkill /f /pid %%a >nul 2>nul
            REM Portu tekrar kontrol et, kapanmazsa çık
            timeout /t 2 >nul
            netstat -ano | findstr ":8080" | findstr "LISTENING" >nul
            if !ERRORLEVEL! EQU 0 (
                echo [HATA] Port 8080 hala kullanımda. Lütfen önce bu portu kullanan uygulamayı manuel olarak kapatın.
                pause
                exit /b 1
            )
        )
    )
)

REM Spring Boot Web uygulamasını başlat - Gradle ile
echo Spring Boot Web uygulamasi baslatiliyor...
echo Profil: %SPRING_PROFILES_ACTIVE%
echo Port: %SERVER_PORT%
echo.

REM Gradle ile başlat - web profili ve port ayarlanarak
call .\gradlew.bat bootRun --args="--server.port=%SERVER_PORT% --spring.profiles.active=%SPRING_PROFILES_ACTIVE%" --console=plain --no-daemon

REM Hata kontrolü
if errorlevel 1 (
    echo.
    echo [HATA] Web uygulamasi baslatilirken bir hata olustu!
    echo Hata kodu: %ERRORLEVEL%
    echo.
    pause
    exit /b %ERRORLEVEL%
)

endlocal
pause 