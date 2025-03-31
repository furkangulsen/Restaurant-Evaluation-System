package com.example.demo.cli;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import com.example.demo.cli.shell.MainMenuCommand;

/**
 * CLI profilinde otomatik olarak menüyü başlatır.
 * CommandLineRunner kullanarak Spring Boot başlangıcında çalışır.
 */
@Component
@Profile("cli")
public class CliAutoStarter implements CommandLineRunner {

    private final MainMenuCommand mainMenuCommand;
    
    @Autowired
    public CliAutoStarter(MainMenuCommand mainMenuCommand) {
        this.mainMenuCommand = mainMenuCommand;
    }
    
    @Override
    public void run(String... args) throws Exception {
        // Spring Boot başladıktan sonra otomatik olarak ana menüyü başlat
        System.out.println("\n\n");
        System.out.println("════════════════════════════════════════════════════");
        System.out.println("    RESTORAN DEĞERLENDİRME SİSTEMİ - CLI SÜRÜMÜ    ");
        System.out.println("════════════════════════════════════════════════════");
        System.out.println("Ana menü başlatılıyor...\n");
        
        // Menüyü başlat
        mainMenuCommand.showMenu();
    }
} 