package com.example.demo.cli;

import com.example.demo.model.Restaurant;
import com.example.demo.model.Review;
import com.example.demo.service.RestaurantService;
import com.example.demo.service.ReviewService;
import com.example.demo.util.AppLogger;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Restoran Değerlendirme Sistemi için Komut Satırı Arayüzü
 * Bu sınıf, "cli" profili etkinleştirildiğinde çalışır
 */
@Component
@Profile("cli")
public class RestaurantCliRunner implements CommandLineRunner {

    private static final Logger LOGGER = AppLogger.getLogger(RestaurantCliRunner.class);
    private final RestaurantService restaurantService;
    private final ReviewService reviewService;
    private final BufferedReader reader;
    private boolean isRunning = true;

    // Konsol UI için kullanılan sabitler
    private static final String BOX_TOP = "╔════════════════════════════════╗";
    private static final String BOX_TITLE = "║           ANA MENÜ            ║";
    private static final String BOX_BOTTOM = "╚════════════════════════════════╝";
    private static final String LINE_SEPARATOR = "\n--------------------------------------------------";

    @Autowired
    public RestaurantCliRunner(RestaurantService restaurantService, ReviewService reviewService) {
        this.restaurantService = restaurantService;
        this.reviewService = reviewService;
        // Türkçe karakter desteği için UTF-8 kodlaması ile reader oluştur
        this.reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        LOGGER.info("CLI Runner başlatıldı - Karakter kodlaması: {}", StandardCharsets.UTF_8);
    }

    @Override
    public void run(String... args) {
        try {
            System.out.println("\n=== RESTORAN DEĞERLENDİRME SİSTEMİ ===");
            System.out.println("Karakter kodlaması: " + StandardCharsets.UTF_8);
            
            // Ana döngü
            while (isRunning) {
                try {
                    showMenu();
                    String input = readInput("");
                    processUserInput(input);
                    Thread.sleep(100); // Kısa bir bekleme
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LOGGER.error("Thread kesintiye uğradı", e);
                } catch (Exception e) {
                    LOGGER.error("Beklenmeyen hata: {}", e.getMessage(), e);
                    System.out.println("Bir hata oluştu: " + e.getMessage());
                    System.out.println("Devam etmek için ENTER tuşuna basın...");
                    readInput("");
                }
            }
        } catch (Exception e) {
            LOGGER.error("CLI uygulaması başlatılırken hata: {}", e.getMessage(), e);
        } finally {
            // Kaynakları temizle
            try {
                reader.close();
            } catch (Exception e) {
                LOGGER.error("Reader kapatılırken hata: {}", e.getMessage(), e);
            }
            LOGGER.info("CLI uygulaması kapatıldı");
        }
    }
    
    /**
     * Ana menüyü gösterir
     */
    private void showMenu() {
        System.out.println("\n" + BOX_TOP);
        System.out.println(BOX_TITLE);
        System.out.println(BOX_BOTTOM);
        System.out.println("  [1] Restoranları Listele");
        System.out.println("  [2] Yeni Restoran Ekle");
        System.out.println("  [3] Restoran Düzenle");
        System.out.println("  [4] Restoran Sil");
        System.out.println("  [5] Değerlendirmeleri Listele");
        System.out.println("  [6] Yeni Değerlendirme Ekle");
        System.out.println("  [7] Değerlendirme Düzenle");
        System.out.println("  [8] Değerlendirme Sil");
        System.out.println("  [9] İstatistikleri Göster");
        System.out.println("  [0] Çıkış");
        System.out.println(BOX_TOP);
        System.out.print("Seçiminiz: ");
    }
    
    /**
     * Kullanıcı girdisini işler
     */
    private void processUserInput(String input) {
        if (input == null || input.trim().isEmpty()) {
            System.out.println("Lütfen bir seçim yapın!");
            return;
        }
        
        input = input.trim();
        
        switch (input) {
            case "1":
                listRestaurants();
                break;
            case "2":
                addRestaurant();
                break;
            case "3":
                editRestaurant();
                break;
            case "4":
                deleteRestaurant();
                break;
            case "5":
                listReviews();
                break;
            case "6":
                addReview();
                break;
            case "7":
                editReview();
                break;
            case "8":
                deleteReview();
                break;
            case "9":
                showStatistics();
                break;
            case "0":
                exitApplication();
                break;
            default:
                System.out.println("Geçersiz seçim! Lütfen tekrar deneyin.");
                waitForEnter();
        }
    }
    
    /**
     * Kullanıcıdan girdi ister ve yanıtı döndürür
     */
    private String readInput(String prompt) {
        try {
            if (!prompt.isEmpty()) {
                System.out.print(prompt);
            }
            return reader.readLine();
        } catch (Exception e) {
            LOGGER.error("Input okuma hatası: {}", e.getMessage(), e);
            return "";
        }
    }
    
    /**
     * İşlem tamamlandıktan sonra devam etmek için ENTER tuşuna basılmasını bekler
     */
    private void waitForEnter() {
        System.out.println("\nDevam etmek için ENTER tuşuna basın...");
        readInput("");
    }
    
    // Menu İşlev Metodları
    
    private void listRestaurants() {
        List<Restaurant> restaurants = restaurantService.getAllRestaurants();
        
        if (restaurants.isEmpty()) {
            System.out.println("Henüz hiç restoran kaydedilmemiş.");
        } else {
            System.out.println("\n=== RESTORANLAR ===");
            System.out.println("ID\tAD\t\tLOKASYON\t\tORTALAMA PUAN");
            System.out.println(LINE_SEPARATOR);
            
            for (Restaurant restaurant : restaurants) {
                System.out.printf("%-5d\t%-15s\t%-15s\t%.1f\n", 
                    restaurant.getId(),
                    restaurant.getName(),
                    restaurant.getLocation(),
                    restaurant.getAverageRating());
            }
        }
        
        waitForEnter();
    }
    
    private void addRestaurant() {
        System.out.println("\n=== YENİ RESTORAN EKLE ===");
        
        String name = readInput("Restoran Adı: ");
        if (name == null || name.trim().isEmpty()) {
            System.out.println("Restoran adı boş olamaz!");
            waitForEnter();
            return;
        }
        
        String location = readInput("\nLokasyon: ");
        if (location == null || location.trim().isEmpty()) {
            System.out.println("Lokasyon boş olamaz!");
            waitForEnter();
            return;
        }
        
        Restaurant restaurant = new Restaurant();
        restaurant.setName(name.trim());
        restaurant.setLocation(location.trim());
        
        Restaurant savedRestaurant = restaurantService.save(restaurant);
        System.out.println("\nRestoran başarıyla eklendi! ID: " + savedRestaurant.getId());
        
        waitForEnter();
    }
    
    private void editRestaurant() {
        List<Restaurant> restaurants = restaurantService.getAllRestaurants();
        
        if (restaurants.isEmpty()) {
            System.out.println("Henüz hiç restoran kaydedilmemiş.");
            waitForEnter();
            return;
        }
        
        listRestaurants();
        
        String idStr = readInput("\nDüzenlenecek Restoran ID: ");
        
        try {
            Long id = Long.parseLong(idStr.trim());
            Optional<Restaurant> restaurantOpt = restaurantService.getRestaurantById(id);
            
            if (restaurantOpt.isEmpty()) {
                System.out.println("Bu ID'ye sahip bir restoran bulunamadı!");
                waitForEnter();
                return;
            }
            
            Restaurant restaurant = restaurantOpt.get();
            System.out.println("\n=== RESTORAN DÜZENLE ===");
            System.out.println("Güncellemek istemediğiniz alanları boş bırakın");
            
            String name = readInput("Restoran Adı [" + restaurant.getName() + "]: ");
            if (name != null && !name.trim().isEmpty()) {
                restaurant.setName(name.trim());
            }
            
            String location = readInput("Lokasyon [" + restaurant.getLocation() + "]: ");
            if (location != null && !location.trim().isEmpty()) {
                restaurant.setLocation(location.trim());
            }
            
            restaurantService.save(restaurant);
            System.out.println("\nRestoran başarıyla güncellendi!");
            
        } catch (NumberFormatException e) {
            System.out.println("Geçersiz ID formatı! Sayısal bir değer girin.");
        }
        
        waitForEnter();
    }
    
    private void deleteRestaurant() {
        listRestaurants();
        
        String idStr = readInput("\nSilinecek Restoran ID: ");
        
        try {
            Long id = Long.parseLong(idStr.trim());
            Optional<Restaurant> restaurantOpt = restaurantService.getRestaurantById(id);
            
            if (restaurantOpt.isEmpty()) {
                System.out.println("Bu ID'ye sahip bir restoran bulunamadı!");
                waitForEnter();
                return;
            }
            
            String confirm = readInput("\"" + restaurantOpt.get().getName() + "\" restoranını silmek istediğinize emin misiniz? (E/H): ");
            
            if ("E".equalsIgnoreCase(confirm) || "EVET".equalsIgnoreCase(confirm)) {
                restaurantService.deleteRestaurant(id);
                System.out.println("\nRestoran başarıyla silindi!");
            } else {
                System.out.println("\nSilme işlemi iptal edildi.");
            }
            
        } catch (NumberFormatException e) {
            System.out.println("Geçersiz ID formatı! Sayısal bir değer girin.");
        }
        
        waitForEnter();
    }
    
    private void listReviews() {
        String idStr = readInput("\nHangi restoranın değerlendirmelerini görmek istiyorsunuz? (ID girin, tümü için 0): ");
        
        try {
            Long id = Long.parseLong(idStr.trim());
            List<Review> reviews;
            
            if (id == 0) {
                reviews = reviewService.getAllReviews();
                System.out.println("\n=== TÜM DEĞERLENDİRMELER ===");
            } else {
                Optional<Restaurant> restaurantOpt = restaurantService.getRestaurantById(id);
                
                if (restaurantOpt.isEmpty()) {
                    System.out.println("Bu ID'ye sahip bir restoran bulunamadı!");
                    waitForEnter();
                    return;
                }
                
                reviews = reviewService.getReviewsByRestaurantId(id);
                System.out.println("\n=== " + restaurantOpt.get().getName() + " DEĞERLENDİRMELERİ ===");
            }
            
            if (reviews.isEmpty()) {
                System.out.println("Henüz hiç değerlendirme yapılmamış.");
            } else {
                System.out.println("ID\tRESTORAN\t\tPUAN\tYORUM\t\tTARİH");
                System.out.println(LINE_SEPARATOR);
                
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
                
                for (Review review : reviews) {
                    System.out.printf("%-5d\t%-15s\t%-5d\t%-20s\t%s\n", 
                        review.getId(),
                        review.getRestaurant().getName(),
                        review.getRating(),
                        review.getComment(),
                        review.getCreatedAt().format(formatter));
                }
            }
            
        } catch (NumberFormatException e) {
            System.out.println("Geçersiz ID formatı! Sayısal bir değer girin.");
        }
        
        waitForEnter();
    }
    
    private void addReview() {
        List<Restaurant> restaurants = restaurantService.getAllRestaurants();
        
        if (restaurants.isEmpty()) {
            System.out.println("Değerlendirme yapmak için önce restoran eklemelisiniz.");
            waitForEnter();
            return;
        }
        
        listRestaurants();
        
        String idStr = readInput("\nDeğerlendirme yapılacak Restoran ID: ");
        
        try {
            Long id = Long.parseLong(idStr.trim());
            Optional<Restaurant> restaurantOpt = restaurantService.getRestaurantById(id);
            
            if (restaurantOpt.isEmpty()) {
                System.out.println("Bu ID'ye sahip bir restoran bulunamadı!");
                waitForEnter();
                return;
            }
            
            Restaurant restaurant = restaurantOpt.get();
            System.out.println("\n=== YENİ DEĞERLENDİRME EKLE: " + restaurant.getName() + " ===");
            
            int rating = 0;
            boolean validRating = false;
            
            while (!validRating) {
                String ratingStr = readInput("Puan (1-5): ");
                
                try {
                    rating = Integer.parseInt(ratingStr.trim());
                    if (rating >= 1 && rating <= 5) {
                        validRating = true;
                    } else {
                        System.out.println("Puan 1-5 arasında olmalıdır!");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Geçersiz puan! Sayısal bir değer girin.");
                }
            }
            
            String comment = readInput("Yorum: ");
            
            Review review = new Review();
            review.setRestaurant(restaurant);
            review.setRating(rating);
            review.setComment(comment);
            review.setCreatedAt(LocalDateTime.now());
            
            reviewService.saveReview(review);
            System.out.println("\nDeğerlendirme başarıyla eklendi! ID: " + review.getId());
            
        } catch (NumberFormatException e) {
            System.out.println("Geçersiz ID formatı! Sayısal bir değer girin.");
        }
        
        waitForEnter();
    }
    
    private void editReview() {
        List<Review> reviews = reviewService.getAllReviews();
        
        if (reviews.isEmpty()) {
            System.out.println("Henüz hiç değerlendirme yapılmamış.");
            waitForEnter();
            return;
        }
        
        listReviews();
        
        String idStr = readInput("\nDüzenlenecek Değerlendirme ID: ");
        
        try {
            Long id = Long.parseLong(idStr.trim());
            Optional<Review> reviewOpt = reviewService.getReviewById(id);
            
            if (reviewOpt.isEmpty()) {
                System.out.println("Bu ID'ye sahip bir değerlendirme bulunamadı!");
                waitForEnter();
                return;
            }
            
            Review review = reviewOpt.get();
            System.out.println("\n=== DEĞERLENDİRME DÜZENLE ===");
            System.out.println("Güncellemek istemediğiniz alanları boş bırakın");
            
            int rating = review.getRating();
            boolean validRating = false;
            
            while (!validRating) {
                String ratingStr = readInput("Puan (1-5) [" + review.getRating() + "]: ");
                
                if (ratingStr == null || ratingStr.trim().isEmpty()) {
                    validRating = true; // Değişiklik yapılmadı
                } else {
                    try {
                        int newRating = Integer.parseInt(ratingStr.trim());
                        if (newRating >= 1 && newRating <= 5) {
                            rating = newRating;
                            validRating = true;
                        } else {
                            System.out.println("Puan 1-5 arasında olmalıdır!");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Geçersiz puan! Sayısal bir değer girin.");
                    }
                }
            }
            
            String comment = readInput("Yorum [" + review.getComment() + "]: ");
            
            review.setRating(rating);
            if (comment != null && !comment.trim().isEmpty()) {
                review.setComment(comment.trim());
            }
            review.setUpdatedAt(LocalDateTime.now());
            
            reviewService.saveReview(review);
            System.out.println("\nDeğerlendirme başarıyla güncellendi!");
            
        } catch (NumberFormatException e) {
            System.out.println("Geçersiz ID formatı! Sayısal bir değer girin.");
        }
        
        waitForEnter();
    }
    
    private void deleteReview() {
        List<Review> reviews = reviewService.getAllReviews();
        
        if (reviews.isEmpty()) {
            System.out.println("Henüz hiç değerlendirme yapılmamış.");
            waitForEnter();
            return;
        }
        
        listReviews();
        
        String idStr = readInput("\nSilinecek Değerlendirme ID: ");
        
        try {
            Long id = Long.parseLong(idStr.trim());
            Optional<Review> reviewOpt = reviewService.getReviewById(id);
            
            if (reviewOpt.isEmpty()) {
                System.out.println("Bu ID'ye sahip bir değerlendirme bulunamadı!");
                waitForEnter();
                return;
            }
            
            Review review = reviewOpt.get();
            String confirm = readInput(review.getRestaurant().getName() + " için yapılan değerlendirmeyi silmek istediğinize emin misiniz? (E/H): ");
            
            if ("E".equalsIgnoreCase(confirm) || "EVET".equalsIgnoreCase(confirm)) {
                reviewService.deleteReview(id);
                System.out.println("\nDeğerlendirme başarıyla silindi!");
            } else {
                System.out.println("\nSilme işlemi iptal edildi.");
            }
            
        } catch (NumberFormatException e) {
            System.out.println("Geçersiz ID formatı! Sayısal bir değer girin.");
        }
        
        waitForEnter();
    }
    
    private void showStatistics() {
        System.out.println("\n=== SİSTEM İSTATİSTİKLERİ ===");
        
        long restaurantCount = restaurantService.count();
        long reviewCount = reviewService.countReviews();
        
        System.out.println("Toplam Restoran Sayısı: " + restaurantCount);
        System.out.println("Toplam Değerlendirme Sayısı: " + reviewCount);
        
        if (restaurantCount > 0) {
            List<Restaurant> topRatedRestaurants = restaurantService.getAllRestaurantsByRating().stream()
                    .limit(3)
                    .toList();
            
            System.out.println("\nEn Yüksek Puanlı Restoranlar:");
            System.out.println("AD\t\tLOKASYON\t\tORTALAMA PUAN");
            System.out.println(LINE_SEPARATOR);
            
            for (Restaurant restaurant : topRatedRestaurants) {
                System.out.printf("%-15s\t%-15s\t%.1f\n", 
                    restaurant.getName(),
                    restaurant.getLocation(),
                    restaurant.getAverageRating());
            }
            
            // En çok değerlendirilen restoranları bulmak için manuel hesaplama
            List<Restaurant> allRestaurants = restaurantService.getAllRestaurants();
            allRestaurants.sort((r1, r2) -> {
                int r1ReviewCount = reviewService.getReviewsByRestaurantId(r1.getId()).size();
                int r2ReviewCount = reviewService.getReviewsByRestaurantId(r2.getId()).size();
                return Integer.compare(r2ReviewCount, r1ReviewCount); // Azalan sıralama
            });
            
            List<Restaurant> mostReviewedRestaurants = allRestaurants.stream()
                    .limit(3)
                    .toList();
            
            System.out.println("\nEn Çok Değerlendirilen Restoranlar:");
            System.out.println("AD\t\tLOKASYON\t\tDEĞERLENDİRME SAYISI");
            System.out.println(LINE_SEPARATOR);
            
            for (Restaurant restaurant : mostReviewedRestaurants) {
                int reviewCountForRestaurant = reviewService.getReviewsByRestaurantId(restaurant.getId()).size();
                System.out.printf("%-15s\t%-15s\t%d\n", 
                    restaurant.getName(),
                    restaurant.getLocation(),
                    reviewCountForRestaurant);
            }
        }
        
        waitForEnter();
    }
    
    private void exitApplication() {
        String confirm = readInput("\nUygulamadan çıkmak istediğinize emin misiniz? (E/H): ");
        
        if ("E".equalsIgnoreCase(confirm) || "EVET".equalsIgnoreCase(confirm)) {
            System.out.println("\nRestoran Değerlendirme Sistemi kapatılıyor...");
            isRunning = false;
        }
    }
}