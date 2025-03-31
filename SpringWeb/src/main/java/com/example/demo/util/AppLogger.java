package com.example.demo.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * Uygulama genelinde kullanılacak Logger sınıfı
 * Bu sınıf, SLF4J üzerinden farklı bileşenler için logger sağlar
 */
public class AppLogger {

    private static final Map<String, Logger> loggerCache = new HashMap<>();

    /**
     * Belirtilen sınıf için bir logger döndürür
     * 
     * @param clazz Logger'ın oluşturulacağı sınıf
     * @return Logger nesnesi
     */
    public static <T> Logger getLogger(Class<T> clazz) {
        return loggerCache.computeIfAbsent(clazz.getName(), LoggerFactory::getLogger);
    }

    /**
     * Belirtilen isim için bir logger döndürür
     * 
     * @param name Logger ismi
     * @return Logger nesnesi
     */
    public static Logger getLogger(String name) {
        return loggerCache.computeIfAbsent(name, LoggerFactory::getLogger);
    }

    // Bilgi mesajı yazma
    public static void info(Class<?> clazz, String message) {
        getLogger(clazz).info(message);
    }

    // Bilgi mesajı yazma (formatlanmış)
    public static void info(Class<?> clazz, String format, Object... args) {
        getLogger(clazz).info(format, args);
    }

    // Hata mesajı yazma
    public static void error(Class<?> clazz, String message) {
        getLogger(clazz).error(message);
    }

    // Hata mesajı ve exception yazma
    public static void error(Class<?> clazz, String message, Throwable throwable) {
        getLogger(clazz).error(message, throwable);
    }

    // Uyarı mesajı yazma
    public static void warn(Class<?> clazz, String message) {
        getLogger(clazz).warn(message);
    }

    // Debug mesajı yazma
    public static void debug(Class<?> clazz, String message) {
        getLogger(clazz).debug(message);
    }

    // Trace mesajı yazma
    public static void trace(Class<?> clazz, String message) {
        getLogger(clazz).trace(message);
    }
} 