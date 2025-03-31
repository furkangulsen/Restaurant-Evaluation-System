package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Spring Shell için CLI profili yapılandırması.
 * Bu konfigürasyon sadece CLI profili etkinken yüklenir.
 */
@Configuration
@Profile("cli")
public class ShellConfiguration {
    // Bu sınıf CLI profilinde Spring Shell bileşenlerini yapılandırır
} 