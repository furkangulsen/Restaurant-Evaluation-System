@echo off
setlocal

REM 8080 portunu kullanan işlemi daha güvenli şekilde kapat
echo Sistemin hazirlanmasi icin islem yapiliyor...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8080" ^| findstr "LISTENING"') do (
    if "%%a" NEQ "0" (
        echo 8080 portunu kullanan işlem kapatılıyor (PID: %%a)
        taskkill /f /pid %%a >nul 2>nul
    )
)

REM GUI'yi başlat
echo GUI baslatiliyor...
java -Dfile.encoding=UTF-8 -Dswing.aatext=true -Dawt.useSystemAAFontSettings=on -cp "build/libs/*;build/classes/java/main;build/resources/main" com.example.demo.gui.BasitGui

endlocal
pause
