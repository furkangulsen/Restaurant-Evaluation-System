package com.example.demo.cli.shell;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;
import org.springframework.context.annotation.Lazy;

import java.io.IOException;
import java.util.Scanner;
import java.util.List;

import com.example.demo.model.Restaurant;
import com.example.demo.model.Review;
import com.example.demo.service.RestaurantService;
import com.example.demo.service.ReviewService;

/**
 * Ana menü komut sınıfı.
 * Geleneksel numaralı menü arayüzünü sağlar.
 * Not: Sadece CLI profilinde aktiftir.
 */
@ShellComponent
@Profile("cli")
public class MainMenuCommand {

    private final ShellHelper shellHelper;
    private final RestaurantCommands restaurantCommands;
    private final ReviewCommands reviewCommands;
    private final StatisticsCommands statisticsCommands;
    private final Terminal terminal;
    private final Scanner scanner;
    private final RestaurantService restaurantService;
    private final ReviewService reviewService;
    
    @Autowired
    public MainMenuCommand(ShellHelper shellHelper, 
                         RestaurantCommands restaurantCommands, 
                         ReviewCommands reviewCommands,
                         StatisticsCommands statisticsCommands,
                         RestaurantService restaurantService,
                         ReviewService reviewService,
                         @Lazy Terminal terminal) {
        this.shellHelper = shellHelper;
        this.restaurantCommands = restaurantCommands;
        this.reviewCommands = reviewCommands;
        this.statisticsCommands = statisticsCommands;
        this.terminal = terminal;
        this.restaurantService = restaurantService;
        this.reviewService = reviewService;
        this.scanner = new Scanner(System.in);
    }

    /**
     * Ana menüyü gösterir ve kullanıcı seçimine göre ilgili komutu çalıştırır
     */
    @ShellMethod(key = "menu", value = "Ana menüyü göster")
    public String showMenu() {
        boolean exitMenu = false;
        
        while (!exitMenu) {
            clearScreen();
            
            // Menü başlığı - ASCII karakterlerle
            System.out.println("-----------------------------------");
            System.out.println("|            ANA MENU            |");
            System.out.println("-----------------------------------");
            
            // Menü seçenekleri
            System.out.println("[1] Restoranlari Listele");
            System.out.println("[2] Yeni Restoran Ekle");
            System.out.println("[3] Restoran Duzenle");
            System.out.println("[4] Restoran Sil");
            System.out.println("[5] Degerlendirmeleri Listele");
            System.out.println("[6] Yeni Degerlendirme Ekle");
            System.out.println("[7] Degerlendirme Duzenle");
            System.out.println("[8] Degerlendirme Sil");
            System.out.println("[9] Istatistikleri Goster");
            System.out.println("[0] Cikis");
            
            System.out.print("\nSeciminiz: ");
            
            try {
                String input = scanner.nextLine().trim();
                
                switch (input) {
                    case "1":
                        clearScreen();
                        displayRestaurantListDirect();
                        break;
                    case "2":
                        clearScreen();
                        handleAddRestaurant();
                        break;
                    case "3":
                        clearScreen();
                        handleEditRestaurant();
                        break;
                    case "4":
                        clearScreen();
                        handleDeleteRestaurant();
                        break;
                    case "5":
                        clearScreen();
                        handleListReviews();
                        break;
                    case "6":
                        clearScreen();
                        handleAddReview();
                        break;
                    case "7":
                        clearScreen();
                        handleEditReview();
                        break;
                    case "8":
                        clearScreen();
                        handleDeleteReview();
                        break;
                    case "9":
                        clearScreen();
                        displayStatistics(statisticsCommands.showStatistics());
                        break;
                    case "0":
                        exitMenu = true;
                        System.out.println("Programdan cikiliyor...");
                        continue;
                    default:
                        System.out.println("Gecersiz secim! Lutfen tekrar deneyin.");
                }
            } catch (Exception e) {
                System.out.println("Bir hata olustu: " + e.getMessage());
                e.printStackTrace();
            }
            
            if (!exitMenu) {
                System.out.println("\nDevam etmek icin ENTER tusuna basin...");
                try {
                    scanner.nextLine();
                } catch (Exception e) {
                    System.out.println("Girdi okuma hatasi: " + e.getMessage());
                }
            }
        }
        
        return ""; // Spring Shell normal olarak bir String geri döndürmelidir
    }
    
    /**
     * Ekranı temizler (ANSI kodları kullanmadan)
     */
    private void clearScreen() {
        // Boş satırlarla ekranı "temizle" - ANSI kod kullanmadan basit yöntem
        for (int i = 0; i < 50; i++) {
            System.out.println();
        }
        
        /* 
        // ANSI escape kodları - bazı terminallerde çalışmıyor 
        // System.out.print("\033[H\033[2J");
        // System.out.flush();
        */
    }
    
    /**
     * Restoranları doğrudan servis katmanından alarak listeler
     */
    private void displayRestaurantListDirect() {
        List<Restaurant> restaurants = restaurantService.getAllRestaurants();
        
        System.out.println("RESTORANLAR LISTESI");
        System.out.println("--------------------------------------------");
        System.out.println("| ID | Restoran Adi         | Konum   | Puan  |");
        System.out.println("--------------------------------------------");
        
        if (restaurants.isEmpty()) {
            System.out.println("| Henuz hic restoran kaydedilmemis          |");
            System.out.println("--------------------------------------------");
            return;
        }
        
        for (Restaurant restaurant : restaurants) {
            String id = restaurant.getId().toString();
            String name = restaurant.getName();
            String location = restaurant.getLocation();
            String rating = restaurant.getAverageRating() > 0 
                    ? String.format("%.1f", restaurant.getAverageRating()) 
                    : "Yok";
            
            // Maksimum uzunlukları kontrol et
            if (name.length() > 20) name = name.substring(0, 17) + "...";
            if (location.length() > 7) location = location.substring(0, 4) + "...";
            
            System.out.printf("| %-2s | %-20s | %-7s | %-5s |%n", 
                    id, name, location, rating);
        }
        
        System.out.println("--------------------------------------------");
    }
    
    /**
     * İstatistikleri düzenli bir şekilde gösterir
     */
    private void displayStatistics(String statisticsOutput) {
        System.out.println("ISTATISTIKLER");
        System.out.println("--------------------------------------");
        
        // ANSI renk kodlarını temizle
        statisticsOutput = statisticsOutput.replaceAll("\u001B\\[[;\\d]*m", "");
        
        String[] lines = statisticsOutput.split("\n");
        for (String line : lines) {
            // Özel semboller ve boş satırları atla
            if (line.contains("═") || line.contains("╔") || line.contains("╚") || 
                line.trim().isEmpty() || line.contains("İstatistikler:")) {
                continue;
            }
            
            // Satırı temizle ve göster
            String cleanLine = line.replace("║", "").trim();
            System.out.println("| " + cleanLine + " |");
        }
        
        System.out.println("--------------------------------------");
    }
    
    // Yardımcı metotlar
    
    private void handleAddRestaurant() {
        String name = readInput("Restoran Adi: ");
        if (name.isEmpty()) {
            System.out.println("Hata: Restoran adi bos olamaz!");
            return;
        }
        
        String location = readInput("Lokasyon: ");
        if (location.isEmpty()) {
            System.out.println("Hata: Lokasyon bos olamaz!");
            return;
        }
        
        String result = restaurantCommands.addRestaurant(name, location);
        // ANSI renk kodlarını temizle
        result = result.replaceAll("\u001B\\[[;\\d]*m", "");
        System.out.println(result);
    }
    
    private void handleEditRestaurant() {
        // Önce mevcut restoranları göster
        System.out.println("\nMevcut Restoranlar:");
        displayRestaurantListDirect();
        
        Long id = readLongInput("\nDuzenlenecek Restoran ID: ");
        if (id == null) {
            System.out.println("Hata: Gecersiz ID!");
            return;
        }
        
        String name = readInput("Yeni Restoran Adi (degismeyecekse bos birakin): ");
        String location = readInput("Yeni Lokasyon (degismeyecekse bos birakin): ");
        
        if (name.isEmpty() && location.isEmpty()) {
            System.out.println("Bilgi: Degisiklik yapmak icin en az bir alani doldurmaniz gerekiyor.");
            return;
        }
        
        String result = restaurantCommands.updateRestaurant(id, name, location);
        // ANSI renk kodlarını temizle
        result = result.replaceAll("\u001B\\[[;\\d]*m", "");
        System.out.println(result);
    }
    
    private void handleDeleteRestaurant() {
        // Önce mevcut restoranları göster
        System.out.println("\nMevcut Restoranlar:");
        displayRestaurantListDirect();
        
        Long id = readLongInput("\nSilinecek Restoran ID: ");
        if (id == null) {
            System.out.println("Hata: Gecersiz ID!");
            return;
        }
        
        String confirm = readInput("Silmek istediginizden emin misiniz? (E/H): ");
        if (!"E".equalsIgnoreCase(confirm) && !"EVET".equalsIgnoreCase(confirm)) {
            System.out.println("Bilgi: Silme islemi iptal edildi.");
            return;
        }
        
        String result = restaurantCommands.deleteRestaurant(id);
        // ANSI renk kodlarını temizle
        result = result.replaceAll("\u001B\\[[;\\d]*m", "");
        System.out.println(result);
    }
    
    private void handleListReviews() {
        // Önce restoranları listele
        System.out.println("\nMevcut Restoranlar:");
        displayRestaurantListDirect();
        
        // Hangi restoranın değerlendirmelerini listelemek istediğini sor
        Long restaurantId = readLongInput("\nHangi restoranin degerlendirilmelerini gormek istiyorsunuz? (ID): ");
        if (restaurantId == null) {
            System.out.println("Hata: Gecersiz ID!");
            return;
        }
        
        System.out.println("\nSeçilen Restoranın Değerlendirmeleri:");
        String result = reviewCommands.listReviewsByRestaurant(restaurantId);
        // ANSI renk kodlarını temizle
        result = result.replaceAll("\u001B\\[[;\\d]*m", "");
        System.out.println(result);
    }
    
    private void handleAddReview() {
        // Önce restoranları listele
        System.out.println("\nMevcut Restoranlar:");
        displayRestaurantListDirect();
        
        // Hangi restoran için değerlendirme yapılacak
        Long restaurantId = readLongInput("\nHangi restoran icin degerlendirme eklemek istiyorsunuz? (ID): ");
        if (restaurantId == null) {
            System.out.println("Hata: Gecersiz ID!");
            return;
        }
        
        // Değerlendirme bilgilerini iste
        String comment = readInput("Yorum: ");
        if (comment.isEmpty()) {
            System.out.println("Hata: Yorum bos olamaz!");
            return;
        }
        
        Integer rating = readIntegerInput("Puan (1-5): ", 1, 5);
        if (rating == null) {
            return;
        }
        
        String result = reviewCommands.addReview(restaurantId, rating, comment);
        // ANSI renk kodlarını temizle
        result = result.replaceAll("\u001B\\[[;\\d]*m", "");
        System.out.println(result);
    }
    
    private void handleEditReview() {
        // Önce restoranları listele
        System.out.println("\nMevcut Restoranlar:");
        displayRestaurantListDirect();
        
        // Hangi restoran için değerlendirme düzenlenecek
        Long restaurantId = readLongInput("\nHangi restoranin degerlendirmelerini duzenlemek istiyorsunuz? (ID): ");
        if (restaurantId == null) {
            System.out.println("Hata: Gecersiz ID!");
            return;
        }
        
        // O restoranın değerlendirmelerini listele
        System.out.println("\nSeçilen Restoranın Değerlendirmeleri:");
        String reviewsOutput = reviewCommands.listReviewsByRestaurant(restaurantId);
        // ANSI renk kodlarını temizle
        reviewsOutput = reviewsOutput.replaceAll("\u001B\\[[;\\d]*m", "");
        System.out.println(reviewsOutput);
        
        // Hangi değerlendirmeyi düzenleyecek
        Long reviewId = readLongInput("\nHangi degerlendirmeyi duzenlemek istiyorsunuz? (ID): ");
        if (reviewId == null) {
            System.out.println("Hata: Gecersiz ID!");
            return;
        }
        
        // Yeni değerlendirme bilgilerini iste
        String comment = readInput("Yeni Yorum (degismeyecekse bos birakin): ");
        
        // Puan sorusu, daha iyi kullanıcı deneyimi için opsiyonel yapıldı
        Integer rating = null;
        String ratingInput = readInput("Yeni Puan (1-5, degismeyecekse bos birakin): ");
        if (!ratingInput.isEmpty()) {
            try {
                int value = Integer.parseInt(ratingInput);
                if (value >= 1 && value <= 5) {
                    rating = value;
                } else {
                    System.out.println("Hata: Puan 1-5 araliginda olmalidir!");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("Hata: Gecersiz puan formati!");
                return;
            }
        }
        
        String result = reviewCommands.updateReview(reviewId, rating, comment);
        // ANSI renk kodlarını temizle
        result = result.replaceAll("\u001B\\[[;\\d]*m", "");
        System.out.println(result);
    }
    
    private void handleDeleteReview() {
        // Önce restoranları listele
        System.out.println("\nMevcut Restoranlar:");
        displayRestaurantListDirect();
        
        // Hangi restoran için değerlendirme silinecek
        Long restaurantId = readLongInput("\nHangi restoranin degerlendirmelerini silmek istiyorsunuz? (ID): ");
        if (restaurantId == null) {
            System.out.println("Hata: Gecersiz ID!");
            return;
        }
        
        // O restoranın değerlendirmelerini listele
        System.out.println("\nSeçilen Restoranın Değerlendirmeleri:");
        String reviewsOutput = reviewCommands.listReviewsByRestaurant(restaurantId);
        // ANSI renk kodlarını temizle
        reviewsOutput = reviewsOutput.replaceAll("\u001B\\[[;\\d]*m", "");
        System.out.println(reviewsOutput);
        
        // Hangi değerlendirmeyi silecek
        Long reviewId = readLongInput("\nHangi degerlendirmeyi silmek istiyorsunuz? (ID): ");
        if (reviewId == null) {
            System.out.println("Hata: Gecersiz ID!");
            return;
        }
        
        String confirm = readInput("Bu degerlendirmeyi silmek istediginize emin misiniz? (E/H): ");
        if (!"E".equalsIgnoreCase(confirm) && !"EVET".equalsIgnoreCase(confirm)) {
            System.out.println("Bilgi: Silme islemi iptal edildi.");
            return;
        }
        
        String result = reviewCommands.deleteReview(reviewId);
        // ANSI renk kodlarını temizle
        result = result.replaceAll("\u001B\\[[;\\d]*m", "");
        System.out.println(result);
    }
    
    // Yardımcı girdi okuma metotları
    
    /**
     * Kullanıcıdan girdi okuyan yardımcı metot
     */
    private String readInput(String prompt) {
        System.out.print(prompt);
        try {
            return scanner.nextLine().trim();
        } catch (Exception e) {
            System.out.println("Girdi okuma hatasi: " + e.getMessage());
            return "";
        }
    }
    
    /**
     * Kullanıcıdan sayı girdi okuyan yardımcı metot 
     */
    private Long readLongInput(String prompt) {
        String input = readInput(prompt);
        return parseLong(input);
    }
    
    /**
     * Kullanıcıdan sınırlı bir aralıkta tam sayı girdi okuyan yardımcı metot
     */
    private Integer readIntegerInput(String prompt, int min, int max) {
        while (true) {
            try {
                String input = readInput(prompt);
                int value = Integer.parseInt(input);
                
                if (value >= min && value <= max) {
                    return value;
                } else {
                    System.out.println("Lutfen " + min + " ile " + max + " arasinda bir deger girin.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Gecersiz sayi formati! Lutfen bir sayi girin.");
            }
        }
    }
    
    private Long parseLong(String input) {
        try {
            return Long.parseLong(input);
        } catch (NumberFormatException e) {
            System.out.println(shellHelper.getErrorMessage("Geçersiz ID formatı! Sayısal bir değer girin."));
            return null;
        }
    }
    
    /**
     * CLI uygulamasını başlatır ve doğrudan ana menüye girer
     */
    @ShellMethod(key = {"start", "başlat"}, value = "CLI uygulamasını başlat")
    public String start() {
        return showMenu();
    }
}
