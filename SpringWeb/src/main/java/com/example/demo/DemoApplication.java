package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import java.nio.charset.Charset;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * Spring Boot uygulamasının ana giriş noktası
 */
@SpringBootApplication
public class DemoApplication {

    /**
     * Ana metod - uygulamayı başlatır
     */
    public static void main(String[] args) {
        // Karakter kodlaması ayarları
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("sun.jnu.encoding", "UTF-8");
        
        // Spring Boot uygulamasını başlat
        ApplicationContext ctx = SpringApplication.run(DemoApplication.class, args);
        
        // Environment bilgisini al
        Environment env = ctx.getEnvironment();
        logApplicationStartup(env);
    }
    
    /**
     * Uygulama başlangıç bilgilerini loglar
     */
    private static void logApplicationStartup(Environment env) {
        String protocol = "http";
        if (env.getProperty("server.ssl.key-store") != null) {
            protocol = "https";
        }
        
        String serverPort = env.getProperty("server.port", "8080");
        String contextPath = env.getProperty("server.servlet.context-path", "/");
        if (!contextPath.startsWith("/")) {
            contextPath = "/" + contextPath;
        }
        
        String hostAddress = "localhost";
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            // Sessizce devam et
        }
        
        System.out.println("\n----------------------------------------------------------");
        System.out.println("  Restoran Değerlendirme Sistemi");
        System.out.println("  Spring Boot " + SpringApplication.class.getPackage().getImplementationVersion());
        System.out.println("  Profiller: " + Arrays.toString(env.getActiveProfiles()));
        System.out.println("----------------------------------------------------------");
        System.out.println("  Web arayüzü: " + protocol + "://localhost:" + serverPort + contextPath);
        System.out.println("  Web API: " + protocol + "://localhost:" + serverPort + contextPath + "api/");
        System.out.println("  External: " + protocol + "://" + hostAddress + ":" + serverPort + contextPath);
        System.out.println("  Karakter Kodlaması: " + Charset.defaultCharset());
        System.out.println("----------------------------------------------------------\n");
    }
} 