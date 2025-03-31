#!/bin/bash

echo "=========================================="
echo "=                                        ="
echo "=        WEB SERVISI BASLATILIYOR        ="
echo "=                                        ="
echo "=========================================="
echo

# Environment değişkenleri ayarla
export SPRING_OUTPUT_ANSI_ENABLED=ALWAYS
export SPRING_PROFILES_ACTIVE=web
export SERVER_PORT=8080

# Java kontrolü
if ! command -v java &> /dev/null; then
    echo "[HATA] Java bulunamadi!"
    echo "Lutfen Java'yi yukleyin ve PATH'e ekleyin."
    echo
    exit 1
fi

# Port kontrolü (macOS için)
if lsof -i :8080 > /dev/null 2>&1; then
    echo "[UYARI] 8080 portu zaten kullanımda."
    echo "Önce mevcut uygulamayı kapatmayı deneyin..."
    lsof -ti:8080 | xargs kill -9 2>/dev/null
    sleep 2
    if lsof -i :8080 > /dev/null 2>&1; then
        echo "[HATA] Port 8080 hala kullanımda. Lütfen önce bu portu kullanan uygulamayı manuel olarak kapatın."
        exit 1
    fi
fi

# Spring Boot Web uygulamasını başlat - Gradle ile
echo "Spring Boot Web uygulamasi baslatiliyor..."
echo "Profil: $SPRING_PROFILES_ACTIVE"
echo "Port: $SERVER_PORT"
echo

# Gradle ile başlat - web profili ve port ayarlanarak
./gradlew bootRun --args="--server.port=$SERVER_PORT --spring.profiles.active=$SPRING_PROFILES_ACTIVE" --console=plain --no-daemon

# Hata kontrolü
if [ $? -ne 0 ]; then
    echo
    echo "[HATA] Web uygulamasi baslatilirken bir hata olustu!"
    echo "Hata kodu: $?"
    echo
    exit $?
fi 