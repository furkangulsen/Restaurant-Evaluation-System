package gui_testleri;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;
import javax.swing.DefaultListModel;
import javax.swing.SwingUtilities;
import java.util.Comparator;
import java.util.ArrayList;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Konsol tabanlı menü sınıfı.
 * Bu sınıf, GUI ile aynı işlevlere sahip bir konsol arayüzü sağlar.
 * Kullanıcı, konsol üzerinden restoranları listeleyebilir, ekleyebilir, düzenleyebilir, silebilir
 * ve değerlendirme ekleyebilir.
 */
public class ConsoleMenu {
    private Scanner scanner;
    private Main mainGUI;
    private DefaultListModel<String> listModel;
    private RestaurantManager restaurantManager;
    private ReviewManager reviewManager;
    
    // Generic servisler - İstatistikler için kullanılacak
    private GenericService<Restaurant, Long> restaurantService;
    private GenericService<Review, Long> reviewService;
    
    // Logger ve ExceptionHandler
    private AppLogger logger;
    private ExceptionHandler exceptionHandler;

    /**
     * ConsoleMenu sınıfının yapıcı metodu.
     * 
     * @param mainGUI Ana pencere referansı
     */
    public ConsoleMenu(Main mainGUI) {
        try {
            // Logger ve ExceptionHandler'ı başlat
            this.logger = AppLogger.getInstance();
            this.exceptionHandler = ExceptionHandler.getInstance();
            logger.info("ConsoleMenu başlatılıyor...");
            
            this.mainGUI = mainGUI;
            this.listModel = mainGUI.getListModel();
            this.restaurantManager = mainGUI.getRestaurantManager();
            this.reviewManager = mainGUI.getReviewManager();
            this.scanner = new Scanner(System.in);
            
            // Generic servisleri başlat
            this.restaurantService = new GenericService<>();
            this.reviewService = new GenericService<>();
            
            // Verileri generic servislere yükle (istatistikler için)
            loadDataToGenericServices();
            
            logger.info("ConsoleMenu başarıyla başlatıldı");
        } catch (Exception e) {
            // Başlatma sırasında bir hata oluşursa, exceptionHandler henüz oluşturulmamış olabilir
            if (exceptionHandler != null) {
                exceptionHandler.handleException(e);
            } else {
                System.err.println("ConsoleMenu başlatılırken hata: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Mevcut verileri generic servislere yükler.
     * Bu metod, istatistik hesaplamaları için generic servisleri hazırlar.
     */
    private void loadDataToGenericServices() {
        try {
            logger.debug("Generic servislere veri yükleniyor...");
            
            // Tüm restoranları generic service'e ekle
            List<Restaurant> restaurants = restaurantManager.getAllRestaurants();
            for (Restaurant restaurant : restaurants) {
                restaurantService.add(restaurant.getId(), restaurant);
            }
            
            // Tüm değerlendirmeleri generic service'e ekle
            List<Review> allReviews = new ArrayList<>();
            for (Restaurant restaurant : restaurants) {
                List<Review> reviews = restaurant.getReviews();
                if (reviews != null) {
                    for (Review review : reviews) {
                        reviewService.add(review.getId(), review);
                    }
                    allReviews.addAll(reviews);
                }
            }
            
            logger.info(String.format("Generic servislere %d restoran ve %d değerlendirme yüklendi", 
                    restaurants.size(), allReviews.size()));
        } catch (Exception e) {
            logger.error("Generic servislere veri yüklenirken hata", e);
            throw new RuntimeException("Generic servislere veri yüklenirken hata: " + e.getMessage(), e);
        }
    }

    /**
     * Menüyü başlatır ve kullanıcıdan komut alır
     */
    public void start() {
        boolean running = true;
        
        // Başlangıçta ekranı temizle
        clearScreen();
        
        logger.info("Konsol menüsü başlatıldı");
        
        while (running) {
            try {
                displayMenu();
                int choice = getUserChoice();
                
                logger.debug("Kullanıcı seçimi: " + choice);
                
                switch (choice) {
                    case 1:
                        listRestaurants();
                        waitForEnter();
                        break;
                    case 2:
                        // Mekan Ara - Şu an için listeleyip kullanıcıya manuel arama yaptırıyoruz
                        System.out.println("\n=== Mekan Ara ===");
                        System.out.println("Tüm mekanlar listeleniyor, aramak istediğinizi bulabilirsiniz:");
                        listRestaurants();
                        waitForEnter();
                        break;
                    case 3:
                        // Yeni Mekan Ekle
                        addRestaurant();
                        waitForEnter();
                        break;
                    case 4:
                        // Mekan Sil
                        deleteRestaurant();
                        waitForEnter();
                        break;
                    case 5:
                        // Mekan Düzenle
                        editRestaurant();
                        waitForEnter();
                        break;
                    case 6:
                        // Değerlendirme Ekle
                        addReview();
                        waitForEnter();
                        break;
                    case 7:
                        // Değerlendirmeleri Görüntüle
                        showReviews();
                        waitForEnter();
                        break;
                    case 8:
                        // İstatistikleri Görüntüle
                        showStatistics();
                        waitForEnter();
                        break;
                    case 9:
                        // Veritabanını Sıfırla - Bu özellik henüz eklenmemiş
                        System.out.println("\n=== Veritabanını Sıfırla ===");
                        System.out.println("Bu özellik henüz eklenmemiştir.");
                        System.out.println("Veritabanı işlemleri GUI üzerinden yapılabilir.");
                        waitForEnter();
                        break;
                    case 0:
                        running = false;
                        logger.info("Konsol menüsü kapatılıyor...");
                        System.out.println("Programdan çıkılıyor...");
                        break;
                    default:
                        logger.warning("Geçersiz seçim: " + choice);
                        System.out.println("Geçersiz seçim! Lütfen tekrar deneyin.");
                        waitForEnter();
                }
                
                // Her işlemden sonra ekranı temizle (çıkış hariç)
                if (running && choice != 0) {
                    clearScreen();
                }
            } catch (Exception e) {
                // Herhangi bir yerde oluşan exception'ları yakala
                exceptionHandler.handleException(e);
                waitForEnter();
                clearScreen();
            }
        }
    }

    /**
     * Ana menüyü gösterir ve kullanıcının seçim yapmasını sağlar.
     */
    private void displayMenu() {
        clearScreen();
        System.out.println("+---------------------------------------------+");
        System.out.println("|  YIYECEK MEKANLARI DEGERLENDIRME SISTEMI    |");
        System.out.println("+---------------------------------------------+");
        System.out.println("| 1. Tum Mekanlari Listele                    |");
        System.out.println("| 2. Mekan Ara                                |");
        System.out.println("| 3. Yeni Mekan Ekle                          |");
        System.out.println("| 4. Mekan Sil                                |");
        System.out.println("| 5. Mekan Duzenle                            |");
        System.out.println("| 6. Degerlendirme Ekle                       |");
        System.out.println("| 7. Degerlendirmeleri Goruntule              |");
        System.out.println("| 8. Istatistikleri Goruntule                 |");
        System.out.println("| 9. Veritabanini Sifirla                     |");
        System.out.println("| 0. Cikis                                    |");
        System.out.println("+---------------------------------------------+");
        System.out.print("Seciminiz: ");
    }

    /**
     * Kullanıcıdan bir seçim alır.
     * 
     * @return Kullanıcının seçimi
     */
    private int getUserChoice() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1; // Geçersiz giriş
        }
    }

    /**
     * Tüm restoranları listeler
     */
    private void listRestaurants() {
        try {
            logger.debug("Restoranlar listeleniyor...");
            System.out.println("\n=== Tüm Restoranlar ===");
            
            // Restoranları al
            List<Restaurant> originalList = restaurantManager.getAllRestaurants();
            if (originalList.isEmpty()) {
                System.out.println("Listelenecek restoran bulunamadı!");
                logger.info("Listelenecek restoran bulunamadı");
                return;
            }
            
            // Değişmez listeyi yeni bir ArrayList'e kopyala ve sonra sırala
            List<Restaurant> restaurants = new ArrayList<>(originalList);
            restaurants.sort(Comparator.comparing(Restaurant::getName));
            
            // Tabloyu yazdır
            System.out.println("------------------------------------------------------");
            System.out.printf("%-20s %-15s %-10s\n", "Ad", "Konum", "Puan");
            System.out.println("------------------------------------------------------");
            
            for (Restaurant restaurant : restaurants) {
                System.out.printf("%-20s %-15s %-10.1f\n", 
                        restaurant.getName(), 
                        restaurant.getLocation(), 
                        restaurant.getRating());
            }
            
            System.out.println("------------------------------------------------------");
            System.out.println("Toplam " + restaurants.size() + " restoran listelendi.");
            
            logger.info(restaurants.size() + " restoran listelendi");
        } catch (Exception e) {
            logger.error("Restoranlar listelenirken hata", e);
            throw new RuntimeException("Restoranlar listelenirken bir hata oluştu: " + e.getMessage(), e);
        }
    }

    /**
     * Yeni bir restoran ekler
     */
    private void addRestaurant() {
        try {
            logger.debug("Restoran ekleme işlemi başlatıldı");
            System.out.println("\n=== Restoran Ekle ===");
            
            // Kullanıcıdan bilgileri al
            System.out.print("Restoran Adı: ");
            String name = scanner.nextLine().trim();
            if (name.isEmpty()) {
                logger.warning("Restoran adı boş olamaz");
                exceptionHandler.handleException(new ValidationException(ValidationException.ValidationErrorCode.REQUIRED_FIELD, "name", "Restoran adı boş olamaz!"));
                return;
            }
            
            // Aynı isimde restoran var mı kontrol et
            if (restaurantManager.getRestaurant(name) != null) {
                logger.warning("Bu isimde bir restoran zaten mevcut: " + name);
                exceptionHandler.handleException(new DatabaseException(DatabaseException.DatabaseErrorCode.DUPLICATE_ENTRY, "Bu isimde bir restoran zaten mevcut: " + name));
                return;
            }
            
            System.out.print("Konum: ");
            String location = scanner.nextLine().trim();
            if (location.isEmpty()) {
                logger.warning("Konum bilgisi boş olamaz");
                exceptionHandler.handleException(new ValidationException(ValidationException.ValidationErrorCode.REQUIRED_FIELD, "location", "Konum bilgisi boş olamaz!"));
                return;
            }
            
            System.out.print("Puan (1-5): ");
            String ratingStr = scanner.nextLine().trim();
            if (ratingStr.isEmpty()) {
                logger.warning("Puan boş olamaz");
                exceptionHandler.handleException(new ValidationException(ValidationException.ValidationErrorCode.REQUIRED_FIELD, "rating", "Puan bilgisi boş olamaz!"));
                return;
            }
            
            double rating;
            try {
                rating = Double.parseDouble(ratingStr);
                if (rating < 1 || rating > 5) {
                    logger.warning("Geçersiz puan aralığı: " + rating);
                    exceptionHandler.handleException(new ValidationException(ValidationException.ValidationErrorCode.INVALID_RANGE, "rating", "Puan 1 ile 5 arasında olmalıdır!"));
                    return;
                }
            } catch (NumberFormatException e) {
                logger.warning("Geçersiz puan formatı: " + ratingStr);
                exceptionHandler.handleException(new ValidationException(ValidationException.ValidationErrorCode.INVALID_FORMAT, "rating", "Lütfen geçerli bir sayı girin!"));
                return;
            }
            
            // Yeni restoran oluştur
            Restaurant restaurant = new Restaurant(name, location, rating);
            
            // Veritabanına ekle
            boolean success = restaurantManager.addRestaurant(restaurant);
            if (success) {
                // Generic servise de ekle
                restaurantService.add(restaurant.getId(), restaurant);
                
                logger.info("Yeni restoran eklendi: " + name);
                System.out.println("\n✓ Restoran başarıyla eklendi!");
                
                // GUI'yi güncelle
                updateGUI();
            } else {
                logger.error("Restoran eklenirken veritabanı hatası");
                exceptionHandler.handleException(new DatabaseException(DatabaseException.DatabaseErrorCode.QUERY_ERROR, "Restoran eklenirken bir hata oluştu."));
            }
        } catch (Exception e) {
            logger.error("Restoran eklenirken beklenmeyen hata", e);
            exceptionHandler.handleException(new RuntimeException("Restoran eklenirken beklenmeyen bir hata oluştu: " + e.getMessage(), e));
        }
    }

    /**
     * Var olan bir restoranı düzenler.
     */
    private void editRestaurant() {
        try {
            System.out.println("\n╔═════════════════════════════════════════════════╗");
            System.out.println("║               RESTORAN DÜZENLE                  ║");
            System.out.println("╠═════════════════════════════════════════════════╣");
            
            // Restoranları al
            List<Restaurant> originalList = restaurantManager.getAllRestaurants();
            
            if (originalList.isEmpty()) {
                System.out.println("║  Düzenlenecek restoran yok!                    ║");
                System.out.println("╚═════════════════════════════════════════════════╝");
                return;
            }
            
            // Değişmez listeyi yeni bir ArrayList'e kopyala
            List<Restaurant> restaurants = new ArrayList<>(originalList);
            
            listRestaurants();
            
            System.out.print("Düzenlemek istediğiniz restoranın numarasını girin: ");
            int index;
            try {
                index = Integer.parseInt(scanner.nextLine()) - 1;
                if (index < 0 || index >= restaurants.size()) {
                    System.out.println("Geçersiz numara!");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("Geçersiz giriş!");
                return;
            }
            
            Restaurant oldRestaurant = restaurants.get(index);
            String oldName = oldRestaurant.getName();
            
            System.out.println("\nYeni bilgileri girin (değiştirmek istemediğiniz alanı boş bırakın):");
            
            System.out.print("Restoran Adı [" + oldRestaurant.getName() + "]: ");
            String name = scanner.nextLine();
            if (name.isEmpty()) {
                name = oldRestaurant.getName();
            }
            
            System.out.print("Konum [" + oldRestaurant.getLocation() + "]: ");
            String location = scanner.nextLine();
            if (location.isEmpty()) {
                location = oldRestaurant.getLocation();
            }
            
            double rating = oldRestaurant.getRating();
            boolean validRating = false;
            
            while (!validRating) {
                System.out.print("Puan (1-5) [" + oldRestaurant.getRating() + "]: ");
                String ratingStr = scanner.nextLine();
                
                if (ratingStr.isEmpty()) {
                    validRating = true;
                } else {
                    try {
                        double newRating = Double.parseDouble(ratingStr);
                        if (newRating >= 1 && newRating <= 5) {
                            rating = newRating;
                            validRating = true;
                        } else {
                            System.out.println("Puan 1 ile 5 arasında olmalıdır!");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Geçersiz puan! Lütfen sayı girin.");
                    }
                }
            }
            
            try {
                Restaurant newRestaurant = new Restaurant(name, location, rating);
                
                // Eğer isim değiştiyse, eski restoranı sil ve yenisini ekle
                boolean success = false;
                if (!name.equals(oldName)) {
                    // Önce değerlendirmeleri güncelle
                    List<Review> reviews = mainGUI.getReviewManager().getReviewsForRestaurant(oldName);
                    mainGUI.getReviewManager().deleteAllReviewsForRestaurant(oldName);
                    
                    for (Review oldReview : reviews) {
                        Review newReview = new Review(name, oldReview.getRating(), oldReview.getComment(), oldReview.getDate());
                        mainGUI.getReviewManager().addReview(newReview);
                    }
                    
                    // Sonra restoranı güncelle
                    restaurantManager.deleteRestaurant(oldName);
                    success = restaurantManager.addRestaurant(newRestaurant);
                } else {
                    success = restaurantManager.updateRestaurant(newRestaurant);
                }
                
                if (success) {
                    System.out.println("\nRestoran başarıyla güncellendi!");
                } else {
                    System.out.println("\nGüncelleme sırasında bir hata oluştu!");
                }
                
                // GUI'yi güncelle
                updateGUI();
            } catch (Exception e) {
                System.out.println("\nRestoran güncellenirken bir hata oluştu: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            logger.error("Restoran düzenlenirken hata", e);
            exceptionHandler.handleException(new RuntimeException("Restoran düzenlenirken bir hata oluştu: " + e.getMessage(), e));
        }
    }

    /**
     * Bir restoranı siler.
     */
    private void deleteRestaurant() {
        try {
            System.out.println("\n╔═════════════════════════════════════════════════╗");
            System.out.println("║                 RESTORAN SİL                    ║");
            System.out.println("╠═════════════════════════════════════════════════╣");
            
            // Restoranları al
            List<Restaurant> originalList = restaurantManager.getAllRestaurants();
            
            if (originalList.isEmpty()) {
                System.out.println("║  Silinecek restoran yok!                       ║");
                System.out.println("╚═════════════════════════════════════════════════╝");
                return;
            }
            
            // Değişmez listeyi yeni bir ArrayList'e kopyala
            List<Restaurant> restaurants = new ArrayList<>(originalList);
            
            listRestaurants();
            
            System.out.print("Silmek istediğiniz restoranın numarasını girin: ");
            int index;
            try {
                index = Integer.parseInt(scanner.nextLine()) - 1;
                if (index < 0 || index >= restaurants.size()) {
                    System.out.println("Geçersiz numara!");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("Geçersiz giriş!");
                return;
            }
            
            Restaurant restaurant = restaurants.get(index);
            
            System.out.println("\nSilmek istediğiniz restoran: " + restaurant.getName());
            System.out.print("Bu restoranı silmek istediğinizden emin misiniz? (E/H): ");
            String confirm = scanner.nextLine().trim().toUpperCase();
            
            if (confirm.equals("E")) {
                try {
                    boolean success = restaurantManager.deleteRestaurant(restaurant.getName());
                    if (success) {
                        System.out.println("\nRestoran başarıyla silindi!");
                    } else {
                        System.out.println("\nSilme sırasında bir hata oluştu!");
                    }
                    
                    // GUI'yi güncelle
                    updateGUI();
                } catch (Exception e) {
                    logger.error("Restoran silinirken hata", e);
                    exceptionHandler.handleException(new RuntimeException("Restoran silinirken bir hata oluştu: " + e.getMessage(), e));
                }
            } else {
                System.out.println("\nSilme işlemi iptal edildi.");
            }
        } catch (Exception e) {
            logger.error("Restoran silme işlemi sırasında hata", e);
            exceptionHandler.handleException(new RuntimeException("Restoran silme işlemi sırasında bir hata oluştu: " + e.getMessage(), e));
        }
    }

    /**
     * Bir restoranın değerlendirmelerini gösterir.
     */
    private void showReviews() {
        try {
            // Restoranları al
            List<Restaurant> originalList = restaurantManager.getAllRestaurants();
            
            if (originalList.isEmpty()) {
                System.out.println("\nHenüz hiç restoran eklenmemiş.");
                return;
            }
            
            // Değişmez listeyi yeni bir ArrayList'e kopyala
            List<Restaurant> restaurants = new ArrayList<>(originalList);
            
            // Restoranları listele
            System.out.println();
            System.out.println("╔══════════════════════════════════════════════════════════╗");
            System.out.println("║                     RESTORANLAR                          ║");
            System.out.println("╠══════════════════════════════════════════════════════════╣");
            
            for (int i = 0; i < restaurants.size(); i++) {
                Restaurant restaurant = restaurants.get(i);
                System.out.printf("║ %2d. %-20s (%-12s) - %.1f★ %12s ║\n", 
                        i + 1, 
                        restaurant.getName(), 
                        restaurant.getLocation(), 
                        restaurant.getRating(),
                        "");
            }
            
            System.out.println("╚══════════════════════════════════════════════════════════╝");
            
            // Kullanıcıdan restoran seçmesini iste
            System.out.print("Değerlendirmelerini görmek istediğiniz restoranın numarasını girin: ");
            int index;
            try {
                index = Integer.parseInt(scanner.nextLine()) - 1;
                if (index < 0 || index >= restaurants.size()) {
                    System.out.println("Geçersiz numara!");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("Geçersiz giriş!");
                return;
            }
            
            Restaurant restaurant = restaurants.get(index);
            String name = restaurant.getName();
            
            try {
                // Doğrudan Main sınıfındaki ReviewManager'ı kullan
                List<Review> reviews = mainGUI.getReviewManager().getReviewsForRestaurant(name);
                
                // Başlık göster
                System.out.println();
                System.out.println("╔══════════════════════════════════════════════════════════╗");
                // Restoran adını başlıkta düzgün göster
                String headerText = name + " DEĞERLENDİRMELERİ";
                int padding = (50 - headerText.length()) / 2;
                String formattedHeader = String.format("║%" + padding + "s%s%" + padding + "s║", 
                                                      "", headerText, "");
                System.out.println(formattedHeader);
                System.out.println("╠══════════════════════════════════════════════════════════╣");
                
                // Değerlendirme yoksa bilgi ver
                if (reviews.isEmpty()) {
                    System.out.println("║ Bu restoran için henüz değerlendirme yapılmamış.        ║");
                    System.out.println("╚══════════════════════════════════════════════════════════╝");
                    return;
                }
                
                // Değerlendirmeleri göster
                for (int i = 0; i < reviews.size(); i++) {
                    Review review = reviews.get(i);
                    
                    // Restoran adını kontrol et ve doğrula
                    if (!review.getRestaurantName().equals(name)) {
                        System.out.println("HATA: Yanlış restoran değerlendirmesi: " + review.getRestaurantName() + " != " + name);
                        continue;
                    }
                    
                    // Tarih ve puan bilgisi
                    System.out.printf("║ %2d. %d★ | %s ║\n", 
                            i + 1, 
                            review.getRating(),
                            review.getDate().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
                    
                    // Yorum
                    System.out.printf("║    Yorum: %-48s ║\n", review.getComment());
                    
                    // Son değerlendirme değilse ayırıcı çizgi ekle
                    if (i < reviews.size() - 1) {
                        System.out.println("║--------------------------------------------------║");
                    }
                }
                
                System.out.println("╚══════════════════════════════════════════════════════════╝");
            } catch (Exception e) {
                System.out.println("\nDeğerlendirmeleri gösterirken bir hata oluştu: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            logger.error("Restoran değerlendirmeleri gösterirken hata", e);
            exceptionHandler.handleException(new RuntimeException("Restoran değerlendirmeleri gösterirken bir hata oluştu: " + e.getMessage(), e));
        }
    }

    /**
     * Bir restoran için değerlendirme ekler.
     */
    private void addReview() {
        try {
            System.out.println("\n╔═════════════════════════════════════════════════╗");
            System.out.println("║             DEĞERLENDİRME EKLE                 ║");
            System.out.println("╠═════════════════════════════════════════════════╣");
            
            // Restoranları al
            List<Restaurant> originalList = restaurantManager.getAllRestaurants();
            
            if (originalList.isEmpty()) {
                System.out.println("║  Değerlendirme eklemek için önce bir restoran   ║");
                System.out.println("║  eklemelisiniz.                                 ║");
                System.out.println("╚═════════════════════════════════════════════════╝");
                return;
            }
            
            // Değişmez listeyi yeni bir ArrayList'e kopyala
            List<Restaurant> restaurants = new ArrayList<>(originalList);
            
            listRestaurants();
            
            System.out.print("Değerlendirmek istediğiniz restoranın numarasını girin: ");
            int index;
            try {
                index = Integer.parseInt(scanner.nextLine()) - 1;
                if (index < 0 || index >= restaurants.size()) {
                    System.out.println("Geçersiz numara!");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("Geçersiz giriş!");
                return;
            }
            
            Restaurant restaurant = restaurants.get(index);
            String name = restaurant.getName();
            
            int rating = 0;
            boolean validRating = false;
            
            while (!validRating) {
                System.out.print("Puan (1-5): ");
                try {
                    rating = Integer.parseInt(scanner.nextLine());
                    if (rating >= 1 && rating <= 5) {
                        validRating = true;
                    } else {
                        System.out.println("Puan 1 ile 5 arasında olmalıdır!");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Geçersiz puan! Lütfen sayı girin.");
                }
            }
            
            System.out.print("Yorum: ");
            String comment = scanner.nextLine();
            
            try {
                // Review nesnesi oluştur
                Review review = new Review(name, rating, comment);
                
                // Doğrudan Main sınıfındaki ReviewManager'ı kullan
                mainGUI.getReviewManager().addReview(review);
                
                // Restoranın ortalama puanını güncelle
                double avgRating = mainGUI.getReviewManager().getAverageRating(name);
                
                // Mevcut restoranı al ve sadece puanını güncelle
                Restaurant existingRestaurant = restaurantManager.getRestaurant(name);
                if (existingRestaurant != null) {
                    // ID'yi koruyarak yeni bir nesne oluştur
                    Restaurant updatedRestaurant = new Restaurant();
                    updatedRestaurant.setId(existingRestaurant.getId());
                    updatedRestaurant.setName(existingRestaurant.getName());
                    updatedRestaurant.setLocation(existingRestaurant.getLocation());
                    updatedRestaurant.setRating(avgRating);
                    
                    // Güncelleme işlemini yap
                    boolean updateSuccess = restaurantManager.updateRestaurant(updatedRestaurant);
                    if (updateSuccess) {
                        System.out.println("\nDeğerlendirme başarıyla eklendi ve restoran puanı güncellendi!");
                    } else {
                        System.out.println("\nDeğerlendirme eklendi fakat restoran puanı güncellenemedi.");
                    }
                } else {
                    System.out.println("\nDeğerlendirme eklendi fakat restoran bulunamadı.");
                }
                
                // GUI'yi güncelle
                updateGUI();
            } catch (Exception e) {
                logger.error("Değerlendirme işlemi sırasında hata", e);
                exceptionHandler.handleException(e);
            }
        } catch (Exception e) {
            logger.error("Değerlendirme eklenirken hata", e);
            exceptionHandler.handleException(new RuntimeException("Değerlendirme eklenirken bir hata oluştu: " + e.getMessage(), e));
        }
    }

    /**
     * GUI'yi günceller
     */
    private void updateGUI() {
        try {
            SwingUtilities.invokeLater(() -> {
                mainGUI.updateRestaurantList();
            });
            logger.debug("GUI güncelleme isteği gönderildi");
        } catch (Exception e) {
            logger.warning("GUI güncellenirken hata: " + e.getMessage());
        }
    }

    /**
     * Ekranı temizler.
     * Windows ve Unix sistemlerinde farklı çalışır.
     */
    private void clearScreen() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            
            if (os.contains("win")) {
                // Windows için
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                // Unix/Linux/Mac için
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            // Ekran temizlenemezse 50 satır boşluk ekle
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
        }
    }
    
    /**
     * Kullanıcıdan Enter tuşuna basmasını bekler
     */
    private void waitForEnter() {
        System.out.println("\nDevam etmek için Enter'a basın...");
        scanner.nextLine();
    }

    // İstatistikleri göster metodu - case 8 için kullanılacak
    private void showStatistics() {
        try {
            logger.debug("İstatistikler görüntüleniyor...");
            System.out.println("\n=== İstatistikler ===");
            
            // Generic servisler kullanarak istatistikler hesaplanıyor
            int restaurantCount = restaurantService.count();
            System.out.println("Toplam Mekan Sayısı: " + restaurantCount);
            
            int reviewCount = reviewService.count();
            System.out.println("Toplam Değerlendirme Sayısı: " + reviewCount);
            
            // Ortalama puanı hesapla
            if (restaurantCount > 0) {
                double totalRating = 0;
                List<Restaurant> allRestaurants = restaurantService.getAll();
                for (Restaurant restaurant : allRestaurants) {
                    totalRating += restaurant.getRating();
                }
                double averageRating = totalRating / restaurantCount;
                System.out.println("Mekanların Ortalama Puanı: " + String.format("%.2f", averageRating));
            }
            
            // En yüksek puanlı mekanları bul (generic predicate kullanımı)
            List<Restaurant> topRatedRestaurants = restaurantService.find(r -> r.getRating() >= 4.5);
            if (!topRatedRestaurants.isEmpty()) {
                System.out.println("\nEn Yüksek Puanlı Mekanlar (4.5+):");
                for (Restaurant restaurant : topRatedRestaurants) {
                    System.out.println("- " + restaurant.getName() + " (" + restaurant.getLocation() + "): " + restaurant.getRating());
                }
            }
            
            logger.info("İstatistikler görüntülendi: " + restaurantCount + " mekan, " + reviewCount + " değerlendirme");
        } catch (Exception e) {
            logger.error("İstatistikler görüntülenirken hata", e);
            throw new RuntimeException("İstatistikler görüntülenirken bir hata oluştu: " + e.getMessage(), e);
        }
    }
} 