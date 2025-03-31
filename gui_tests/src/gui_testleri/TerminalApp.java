package gui_testleri;

import java.util.Scanner;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Terminal tabanlı restoran değerlendirme sistemi.
 * Veritabanı tabanlı çalışır.
 */
public class TerminalApp {
    private RestaurantManager restaurantManager;
    private ReviewManager reviewManager;
    private DatabaseManager databaseManager;
    private Scanner scanner;

    public TerminalApp() {
        // Hibernate oturumunu başlat
        HibernateUtil.getSessionFactory();
        
        this.restaurantManager = new RestaurantManager(null); // GUI olmadığı için null
        this.reviewManager = new ReviewManager();
        this.reviewManager.setRestaurantManager(restaurantManager);
        this.databaseManager = new DatabaseManager(restaurantManager, reviewManager);
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        boolean running = true;
        
        System.out.println("\n🍽️ RESTORAN DEĞERLENDİRME SİSTEMİ 🍽️");
        System.out.println("Veritabanı bağlantısı başlatılıyor...");
        
        while (running) {
            displayMenu();
            int choice = getUserChoice();
            
            switch (choice) {
                case 1:
                    listRestaurants();
                    break;
                case 2:
                    addRestaurant();
                    break;
                case 3:
                    editRestaurant();
                    break;
                case 4:
                    deleteRestaurant();
                    break;
                case 5:
                    addReview();
                    break;
                case 6:
                    showReviews();
                    break;
                case 7:
                    exportDatabase(); // Veritabanı Yedeği Oluştur
                    break;
                case 8:
                    importDatabase(); // Veritabanını Yedekten Geri Yükle
                    break;
                case 0:
                    System.out.println("\nUygulama kapatılıyor...");
                    running = false;
                    // Hibernate oturumunu kapat
                    HibernateUtil.shutdown();
                    System.out.println("Veritabanı bağlantısı kapatıldı.");
                    System.out.println("İyi günler dileriz! 👋");
                    break;
                default:
                    System.out.println("❌ Geçersiz seçim! Lütfen tekrar deneyin.");
            }
        }
        scanner.close();
    }

    private void displayMenu() {
        System.out.println("\n=== Restoran Değerlendirme Sistemi ===");
        System.out.println("1. Restoranları Listele");
        System.out.println("2. Restoran Ekle");
        System.out.println("3. Restoran Düzenle");
        System.out.println("4. Restoran Sil");
        System.out.println("5. Değerlendirme Ekle");
        System.out.println("6. Değerlendirmeleri Göster");
        System.out.println("\n--- Veritabanı Yönetimi ---");
        System.out.println("7. Veritabanı Yedeği Oluştur (Backup)");
        System.out.println("8. Veritabanını Yedekten Geri Yükle (Restore)");
        System.out.println("\n0. Çıkış");
        System.out.print("Seçiminiz: ");
    }

    private int getUserChoice() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void listRestaurants() {
        restaurantManager.printRestaurants();
    }

    private void addRestaurant() {
        System.out.println("\n=== Restoran Ekle ===");
        System.out.print("Restoran Adı: ");
        String name = scanner.nextLine();
        System.out.print("Konum: ");
        String location = scanner.nextLine();
        System.out.print("Puan (1-5): ");
        double rating = 0;
        try {
            rating = Double.parseDouble(scanner.nextLine());
            if (rating < 1 || rating > 5) {
                System.out.println("Geçersiz puan! 1-5 arası bir değer girin. Varsayılan olarak 3.0 atanıyor.");
                rating = 3.0;
            }
        } catch (NumberFormatException e) {
            System.out.println("Geçersiz puan formatı! Varsayılan olarak 3.0 atanıyor.");
            rating = 3.0;
        }
        
        Restaurant restaurant = new Restaurant(name, location, rating);
        
        if (restaurantManager.addRestaurant(restaurant)) {
            System.out.println("Restoran başarıyla eklendi!");
        } else {
            System.out.println("Restoran eklenirken bir hata oluştu!");
        }
    }

    private void editRestaurant() {
        System.out.println("\n=== Restoran Düzenle ===");
        System.out.print("Düzenlenecek Restoran Adı: ");
        String name = scanner.nextLine();
        
        Restaurant restaurant = restaurantManager.getRestaurant(name);
        if (restaurant != null) {
            System.out.print("Yeni Ad (boş bırakılırsa değişmez): ");
            String newName = scanner.nextLine();
            System.out.print("Yeni Konum (boş bırakılırsa değişmez): ");
            String newLocation = scanner.nextLine();
            System.out.print("Yeni Puan (boş bırakılırsa değişmez): ");
            String newRatingStr = scanner.nextLine();
            
            if (!newName.isEmpty()) restaurant.setName(newName);
            if (!newLocation.isEmpty()) restaurant.setLocation(newLocation);
            if (!newRatingStr.isEmpty()) {
                try {
                    double newRating = Double.parseDouble(newRatingStr);
                    if (newRating >= 1 && newRating <= 5) {
                        restaurant.setRating(newRating);
                    } else {
                        System.out.println("Geçersiz puan! 1-5 arası bir değer girin.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Geçersiz puan formatı!");
                }
            }
            
            restaurantManager.updateRestaurant(restaurant);
            System.out.println("Restoran başarıyla güncellendi!");
        } else {
            System.out.println("Restoran bulunamadı!");
        }
    }

    private void deleteRestaurant() {
        System.out.println("\n=== Restoran Sil ===");
        System.out.print("Silinecek Restoran Adı: ");
        String name = scanner.nextLine();
        
        Restaurant restaurant = restaurantManager.getRestaurant(name);
        if (restaurant == null) {
            System.out.println("❌ HATA: Restoran bulunamadı!");
            return;
        }

        // Kullanıcıdan onay al
        System.out.println("\nDİKKAT: Bu işlem restoranı ve tüm değerlendirmelerini kalıcı olarak silecek!");
        System.out.print("Devam etmek istiyor musunuz? (E/H): ");
        String confirm = scanner.nextLine().trim().toUpperCase();
        
        if (!confirm.equals("E")) {
            System.out.println("İşlem iptal edildi.");
            return;
        }

        try {
            // Önce restorana ait tüm değerlendirmeleri sil
            System.out.println("Değerlendirmeler siliniyor...");
            reviewManager.deleteAllReviewsForRestaurant(name);
            System.out.println("✓ Değerlendirmeler silindi");

            // Sonra restoranı sil
            System.out.println("Restoran siliniyor...");
            if (restaurantManager.deleteRestaurant(name)) {
                System.out.println("✓ Restoran başarıyla silindi!");
            } else {
                throw new RuntimeException("Restoran silinirken bir hata oluştu!");
            }
        } catch (Exception e) {
            System.out.println("\n❌ HATA: Silme işlemi başarısız oldu!");
            System.out.println("Hata detayı: " + e.getMessage());
            System.out.println("\nÖneriler:");
            System.out.println("1. Veritabanı bağlantısını kontrol edin");
            System.out.println("2. Programı yeniden başlatmayı deneyin");
            System.out.println("3. Veritabanı yöneticinize başvurun");
        }
    }

    private void addReview() {
        System.out.println("\n=== Değerlendirme Ekle ===");
        System.out.print("Restoran Adı: ");
        String restaurantName = scanner.nextLine();
        
        if (restaurantManager.getRestaurant(restaurantName) == null) {
            System.out.println("Restoran bulunamadı!");
            return;
        }
        
        System.out.print("Puan (1-5): ");
        try {
            int rating = Integer.parseInt(scanner.nextLine());
            if (rating < 1 || rating > 5) {
                System.out.println("Geçersiz puan! 1-5 arası bir değer girin.");
                return;
            }
            
            System.out.print("Yorum: ");
            String comment = scanner.nextLine();
            
            Review review = new Review(restaurantName, rating, comment);
            reviewManager.addReview(review);
            System.out.println("Değerlendirme başarıyla eklendi!");
        } catch (NumberFormatException e) {
            System.out.println("Geçersiz puan formatı!");
        }
    }

    private void showReviews() {
        System.out.println("\n=== Değerlendirmeleri Göster ===");
        System.out.print("Restoran Adı: ");
        String restaurantName = scanner.nextLine();
        
        java.util.List<Review> reviews = reviewManager.getReviewsForRestaurant(restaurantName);
        if (reviews.isEmpty()) {
            System.out.println("Bu restoran için henüz değerlendirme yok!");
            return;
        }
        
        for (Review review : reviews) {
            System.out.println("Puan: " + review.getRating());
            System.out.println("Yorum: " + review.getComment());
            System.out.println("Tarih: " + review.getDate());
            System.out.println("-------------------");
        }
    }

    private void exportDatabase() {
        System.out.println("\n=== Veritabanı Yedekleme İşlemi ===");
        System.out.print("Yedek dosyası için isim girin (örn: yedek): ");
        String baseFileName = scanner.nextLine().trim();
        
        if (baseFileName.isEmpty()) {
            System.out.println("❌ HATA: Dosya adı boş olamaz!");
            showErrorDialog("Hata", "Dosya adı boş olamaz! Lütfen geçerli bir dosya adı girin.");
            return;
        }

        // Tarih ve saat bilgisini ekle
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm");
        String timestamp = java.time.LocalDateTime.now().format(formatter);
        
        // Yedek klasörünü oluştur (eğer yoksa)
        java.io.File backupDir = new java.io.File("yedekler");
        if (!backupDir.exists()) {
            backupDir.mkdir();
            System.out.println("✓ 'yedekler' klasörü oluşturuldu");
        }
        
        String fileName = "yedekler/" + baseFileName + "_" + timestamp + ".sql";

        // Veritabanı bilgileri
        String dbName = "restaurant_db";
        String dbUser = "postgres";
        String dbPassword = "123456789";

        System.out.println("\nYedekleme işlemi başlatılıyor...");
        System.out.println("Yedek dosyası: " + fileName);
        
        try {
            // Yedekleme öncesi veritabanı bağlantısını kontrol et
            System.out.println("1) Veritabanı bağlantısı kontrol ediliyor...");
            ProcessBuilder checkDb = new ProcessBuilder(
                "psql",
                "--dbname=" + dbName,
                "--username=" + dbUser,
                "-c", "SELECT 1"
            );
            checkDb.environment().put("PGPASSWORD", dbPassword);
            checkDb.redirectErrorStream(true);
            Process checkProcess = checkDb.start();
            
            // İşlem çıktısını oku
            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(checkProcess.getInputStream())
            );
            
            String line;
            StringBuilder output = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            
            if (checkProcess.waitFor() != 0) {
                String errorMsg = "Veritabanına bağlanılamıyor! Lütfen PostgreSQL servisinin çalıştığından emin olun.";
                System.out.println("❌ HATA: " + errorMsg);
                showErrorDialog("Bağlantı Hatası", errorMsg);
                throw new RuntimeException(errorMsg);
            }
            
            System.out.println("✓ Veritabanı bağlantısı başarılı");

            // Aktif bağlantıları kapat
            System.out.println("\n2) Hibernate oturumu kapatılıyor...");
            HibernateUtil.shutdown();
            System.out.println("✓ Hibernate oturumu kapatıldı");
            
            // 2 saniye bekle
            System.out.println("2 saniye bekleniyor...");
            Thread.sleep(2000);

            // pg_dump ile yedekleme yap
            System.out.println("\n3) Veritabanı yedekleniyor...");
            ProcessBuilder pb = new ProcessBuilder(
                "pg_dump",
                "--dbname=" + dbName,
                "--username=" + dbUser,
                "--format=p",  // Plain text format
                "--clean",     // Clean (drop) database objects before recreating
                "--if-exists", // Add IF EXISTS clause
                "--no-owner",  // Don't output commands to set ownership
                "--no-privileges", // Don't include privilege commands
                "--verbose",   // Verbose mode
                "--file=" + fileName
            );
            
            pb.environment().put("PGPASSWORD", dbPassword);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            // İşlem çıktısını oku
            reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(process.getInputStream())
            );
            
            output = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                if (line.contains("ERROR") || line.contains("FATAL")) {
                    System.out.println("❌ " + line);
                } else if (line.contains("dumping") || line.contains("dumped")) {
                    System.out.println("  " + line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                // Yedek dosyasının boyutunu kontrol et
                java.io.File backupFile = new java.io.File(fileName);
                long fileSize = backupFile.length();
                if (fileSize < 100) { // 100 byte'dan küçük dosyalar muhtemelen boştur
                    String errorMsg = "Yedek dosyası çok küçük, yedekleme başarısız olmuş olabilir!";
                    System.out.println("❌ HATA: " + errorMsg);
                    showErrorDialog("Yedekleme Hatası", errorMsg);
                    throw new RuntimeException(errorMsg);
                }
                
                System.out.println("\n✓ Veritabanı başarıyla yedeklendi!");
                System.out.println("✓ Yedek dosyası: " + fileName);
                System.out.println("✓ Dosya boyutu: " + (fileSize / 1024) + " KB");
                
                // Yedek dosyasını doğrula
                System.out.println("\n4) Yedek dosyası doğrulanıyor...");
                ProcessBuilder validatePb = new ProcessBuilder(
                    "pg_restore",
                    "--list",
                    "--dbname=" + dbName,
                    "--username=" + dbUser,
                    fileName
                );
                validatePb.environment().put("PGPASSWORD", dbPassword);
                validatePb.redirectErrorStream(true);
                Process validateProcess = validatePb.start();
                
                boolean isValid = validateProcess.waitFor() == 0;
                if (isValid) {
                    System.out.println("✓ Yedek dosyası doğrulandı ve geçerli");
                } else {
                    System.out.println("⚠️ Yedek dosyası doğrulanamadı, ancak oluşturuldu");
                }
                
                // Başarı mesajı göster
                showInfoDialog("Yedekleme Başarılı", 
                    "Veritabanı başarıyla yedeklendi!\n\n" +
                    "Yedek dosyası: " + fileName + "\n" +
                    "Dosya boyutu: " + (fileSize / 1024) + " KB");
                
            } else {
                String errorMsg = "Yedekleme işlemi başarısız oldu!\n" + output.toString();
                System.out.println("❌ HATA: " + errorMsg);
                showErrorDialog("Yedekleme Hatası", "Yedekleme işlemi başarısız oldu! Çıkış kodu: " + exitCode);
                throw new RuntimeException(errorMsg);
            }
            
            // Hibernate oturumunu yeniden başlat
            System.out.println("\n5) Veritabanı bağlantısı yeniden başlatılıyor...");
            HibernateUtil.getSessionFactory();
            System.out.println("✓ Veritabanı bağlantısı yeniden başlatıldı");
            
        } catch (Exception e) {
            System.out.println("\n❌ HATA: Yedekleme işlemi sırasında bir sorun oluştu:");
            System.out.println(e.getMessage());
            
            // Hata detaylarını göster
            e.printStackTrace();
            
            showErrorDialog("Yedekleme Hatası", 
                "Yedekleme işlemi sırasında bir hata oluştu:\n" + e.getMessage() + 
                "\n\nLütfen PostgreSQL servisinin çalıştığından emin olun ve tekrar deneyin.");
            
            // Hata durumunda Hibernate oturumunu yeniden başlatmayı dene
            try {
                System.out.println("\nVeritabanı bağlantısı yeniden başlatılıyor...");
                HibernateUtil.getSessionFactory();
                System.out.println("✓ Veritabanı bağlantısı yeniden başlatıldı");
            } catch (Exception reconnectError) {
                System.out.println("❌ Veritabanı bağlantısı yeniden başlatılamadı. Lütfen uygulamayı yeniden başlatın.");
                showErrorDialog("Kritik Hata", 
                    "Veritabanı bağlantısı yeniden başlatılamadı: " + reconnectError.getMessage() + 
                    "\n\nLütfen uygulamayı kapatıp yeniden başlatın.");
            }
        }
    }

    private void terminateActiveConnections() {
        System.out.println("Aktif veritabanı bağlantıları kontrol ediliyor...");
        
        // Önce aktif bağlantıları listele
        String listSql = "SELECT pid, usename, application_name, client_addr, state, query_start, query " +
                         "FROM pg_stat_activity " +
                         "WHERE datname = 'restaurant_db' AND pid <> pg_backend_pid();";
        
        try {
            // Önce PostgreSQL servisinin çalışıp çalışmadığını kontrol et
            ProcessBuilder checkPb = new ProcessBuilder(
                "psql",
                "-U", "postgres",
                "-d", "postgres",
                "-c", "SELECT 1"
            );
            checkPb.environment().put("PGPASSWORD", "123456789");
            checkPb.redirectErrorStream(true);
            
            Process checkProcess = checkPb.start();
            int exitCode = checkProcess.waitFor();
            
            if (exitCode != 0) {
                System.out.println("⚠️ PostgreSQL servisine bağlanılamıyor! Servisin çalıştığından emin olun.");
                System.out.println("İşleme devam ediliyor, ancak sorunlar oluşabilir...");
                return;
            }
            
            // Aktif bağlantıları listele
            ProcessBuilder listPb = new ProcessBuilder(
                "psql",
                "-U", "postgres",
                "-d", "postgres", // Ana veritabanına bağlan
                "-c", listSql
            );
            listPb.environment().put("PGPASSWORD", "123456789");
            listPb.redirectErrorStream(true);
            Process listProcess = listPb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(listProcess.getInputStream()));
            String line;
            boolean hasConnections = false;
            int lineCount = 0;
            
            while ((line = reader.readLine()) != null) {
                lineCount++;
                if (lineCount > 2) { // Başlık satırlarını atla
                    hasConnections = true;
                    System.out.println("  " + line);
                }
            }
            
            if (!hasConnections) {
                System.out.println("✓ Aktif bağlantı bulunamadı, devam ediliyor...");
                return;
            }
            
            // Aktif bağlantıları sonlandır
            System.out.println("Aktif bağlantılar sonlandırılıyor...");
            
            // Daha güvenli bir SQL sorgusu kullan - sadece kendi bağlantımız dışındakileri sonlandır
            String terminateSql = "SELECT pg_terminate_backend(pid) FROM pg_stat_activity " +
                                 "WHERE datname = 'restaurant_db' AND pid <> pg_backend_pid();";
            
            ProcessBuilder terminatePb = new ProcessBuilder(
                "psql",
                "-U", "postgres",
                "-d", "postgres", // Ana veritabanına bağlan
                "-c", terminateSql
            );
            terminatePb.environment().put("PGPASSWORD", "123456789");
            terminatePb.redirectErrorStream(true);
            Process terminateProcess = terminatePb.start();
            reader = new BufferedReader(new InputStreamReader(terminateProcess.getInputStream()));
            
            int successCount = 0;
            while ((line = reader.readLine()) != null) {
                if (line.trim().equalsIgnoreCase("t")) {
                    successCount++;
                }
            }
            
            exitCode = terminateProcess.waitFor();
            if (exitCode != 0) {
                System.out.println("⚠️ Bağlantıları sonlandırırken bir sorun oluştu (Çıkış kodu: " + exitCode + ")");
                System.out.println("İşleme devam ediliyor...");
            } else {
                System.out.println("✓ " + successCount + " aktif bağlantı başarıyla sonlandırıldı");
            }
            
            // Bağlantıların gerçekten kapandığını doğrula
            Thread.sleep(1000); // Kısa bir süre bekle
            
            ProcessBuilder verifyPb = new ProcessBuilder(
                "psql",
                "-U", "postgres",
                "-d", "postgres",
                "-c", listSql
            );
            verifyPb.environment().put("PGPASSWORD", "123456789");
            verifyPb.redirectErrorStream(true);
            Process verifyProcess = verifyPb.start();
            reader = new BufferedReader(new InputStreamReader(verifyProcess.getInputStream()));
            
            hasConnections = false;
            lineCount = 0;
            while ((line = reader.readLine()) != null) {
                lineCount++;
                if (lineCount > 2) { // Başlık satırlarını atla
                    hasConnections = true;
                    System.out.println("  Hala aktif bağlantı: " + line);
                }
            }
            
            if (hasConnections) {
                System.out.println("⚠️ Bazı bağlantılar hala aktif, ancak işleme devam ediliyor...");
                
                // Son bir deneme daha yap
                System.out.println("Son bir deneme daha yapılıyor...");
                
                ProcessBuilder finalPb = new ProcessBuilder(
                    "psql",
                    "-U", "postgres",
                    "-d", "postgres",
                    "-c", terminateSql
                );
                finalPb.environment().put("PGPASSWORD", "123456789");
                finalPb.redirectErrorStream(true);
                Process finalProcess = finalPb.start();
                finalProcess.waitFor();
                
                Thread.sleep(1000); // Kısa bir süre bekle
            } else {
                System.out.println("✓ Tüm bağlantılar başarıyla sonlandırıldı");
            }
            
        } catch (Exception e) {
            System.out.println("⚠️ Aktif bağlantılar kontrol edilirken hata: " + e.getMessage());
            System.out.println("İşleme devam ediliyor...");
        }
    }

    private void dropAndRecreateDatabase() {
        System.out.println("\nVeritabanı siliniyor ve yeniden oluşturuluyor...");
        try {
            ProcessBuilder dropDb = new ProcessBuilder(
                "dropdb",
                "restaurant_db",
                "--if-exists"
            );
            dropDb.environment().put("PGPASSWORD", "123456789");
            dropDb.redirectErrorStream(true);
            Process dropProcess = dropDb.start();
            dropProcess.waitFor();

            ProcessBuilder createDb = new ProcessBuilder(
                "createdb",
                "restaurant_db"
            );
            createDb.environment().put("PGPASSWORD", "123456789");
            createDb.redirectErrorStream(true);
            Process createProcess = createDb.start();
            createProcess.waitFor();

            System.out.println("✓ Veritabanı başarıyla silindi ve yeniden oluşturuldu");
        } catch (Exception e) {
            System.out.println("❌ HATA: Veritabanı silinirken veya oluşturulurken hata: " + e.getMessage());
        }
    }

    private void restoreDatabase(String backupFile) {
        System.out.println("\nVeritabanı geri yükleniyor...");
        try {
            ProcessBuilder restoreDb = new ProcessBuilder(
                "pg_restore",
                "--clean",
                "--if-exists",
                "--dbname=restaurant_db",
                backupFile
            );
            restoreDb.environment().put("PGPASSWORD", "123456789");
            restoreDb.redirectErrorStream(true);
            Process restoreProcess = restoreDb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(restoreProcess.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            int exitCode = restoreProcess.waitFor();
            if (exitCode == 0) {
                System.out.println("✓ Veritabanı başarıyla geri yüklendi");
            } else {
                System.out.println("❌ HATA: Veritabanı geri yüklenirken hata oluştu");
            }
        } catch (Exception e) {
            System.out.println("❌ HATA: Veritabanı geri yüklenirken hata: " + e.getMessage());
        }
    }

    private void importDatabase() {
        System.out.println("\n=== Veritabanı Geri Yükleme İşlemi ===");
        
        // Yedek klasörünü kontrol et
        java.io.File backupDir = new java.io.File("yedekler");
        if (!backupDir.exists() || !backupDir.isDirectory()) {
            System.out.println("❌ HATA: 'yedekler' klasörü bulunamadı!");
            System.out.println("Önce bir yedek oluşturmanız gerekiyor.");
            showErrorDialog("Hata", "'yedekler' klasörü bulunamadı! Önce bir yedek oluşturmanız gerekiyor.");
            return;
        }
        
        // Mevcut yedek dosyalarını listele
        java.io.File[] backupFiles = backupDir.listFiles((dir, name) -> name.endsWith(".sql"));
        
        if (backupFiles == null || backupFiles.length == 0) {
            System.out.println("❌ HATA: 'yedekler' klasöründe hiç yedek dosyası bulunamadı!");
            System.out.println("Önce bir yedek oluşturmanız gerekiyor.");
            showErrorDialog("Hata", "'yedekler' klasöründe hiç yedek dosyası bulunamadı! Önce bir yedek oluşturmanız gerekiyor.");
            return;
        }

        // Yedek dosyalarını tarihe göre sırala (en yenisi en üstte)
        java.util.Arrays.sort(backupFiles, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));

        System.out.println("\nMevcut yedek dosyaları (en yeniden en eskiye):");
        for (int i = 0; i < backupFiles.length; i++) {
            // Dosya tarihini formatla
            java.util.Date fileDate = new java.util.Date(backupFiles[i].lastModified());
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = sdf.format(fileDate);
            
            System.out.printf("%d. %s (%d KB) - %s%n", 
                i + 1, 
                backupFiles[i].getName(), 
                backupFiles[i].length() / 1024,
                formattedDate
            );
        }

        System.out.print("\nGeri yüklemek istediğiniz dosyanın numarasını girin (1-" + backupFiles.length + "): ");
        int choice;
        try {
            choice = Integer.parseInt(scanner.nextLine());
            if (choice < 1 || choice > backupFiles.length) {
                System.out.println("❌ HATA: Geçersiz seçim!");
                showErrorDialog("Hata", "Geçersiz seçim! Lütfen 1 ile " + backupFiles.length + " arasında bir sayı girin.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ HATA: Geçersiz giriş!");
            showErrorDialog("Hata", "Geçersiz giriş! Lütfen bir sayı girin.");
            return;
        }

        java.io.File selectedFile = backupFiles[choice - 1];
        System.out.println("\nSeçilen yedek dosyası: " + selectedFile.getName());
        System.out.println("⚠️ DİKKAT: Bu işlem mevcut verileri silecek ve yerine yedekteki verileri yükleyecek!");
        System.out.println("⚠️ Bu işlem geri alınamaz ve mevcut verileriniz kaybolacaktır!");
        System.out.print("Devam etmek istiyor musunuz? (E/H): ");
        String confirm = scanner.nextLine().trim().toUpperCase();
        
        if (!confirm.equals("E")) {
            System.out.println("İşlem iptal edildi.");
            return;
        }

        // Veritabanı bilgileri
        String dbName = "restaurant_db";
        String dbUser = "postgres";
        String dbPassword = "123456789";

        System.out.println("\nGeri yükleme işlemi başlatılıyor...");
        
        try {
            // 1. Adım: Hibernate oturumunu kapat
            System.out.println("\n1) Hibernate oturumu kapatılıyor...");
            HibernateUtil.shutdown();
            System.out.println("✓ Hibernate oturumu kapatıldı");
            
            // 2 saniye bekle
            System.out.println("2 saniye bekleniyor...");
            Thread.sleep(2000);
            
            // 2. Adım: Aktif bağlantıları sonlandır
            System.out.println("\n2) Aktif veritabanı bağlantıları kontrol ediliyor...");
            terminateActiveConnections();
            
            // 3. Adım: Yedek dosyasını doğrula
            System.out.println("\n3) Yedek dosyası doğrulanıyor...");
            
            // Önce dosyanın varlığını ve okunabilirliğini kontrol et
            if (!selectedFile.exists() || !selectedFile.canRead()) {
                String errorMsg = "Seçilen yedek dosyası bulunamadı veya okunamıyor: " + selectedFile.getAbsolutePath();
                System.out.println("❌ HATA: " + errorMsg);
                showErrorDialog("Yedek Dosyası Hatası", errorMsg);
                throw new RuntimeException(errorMsg);
            }
            
            // Dosya içeriğini kontrol et (en azından INSERT komutları var mı?)
            boolean hasInsertCommands = false;
            try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(selectedFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.trim().toUpperCase().startsWith("INSERT")) {
                        hasInsertCommands = true;
                        break;
                    }
                }
            }
            
            if (!hasInsertCommands) {
                String errorMsg = "Yedek dosyası geçerli veri içermiyor. Dosyada INSERT komutları bulunamadı.";
                System.out.println("❌ HATA: " + errorMsg);
                showErrorDialog("Yedek Dosyası Hatası", errorMsg);
                throw new RuntimeException(errorMsg);
            }
            
            System.out.println("✓ Yedek dosyası doğrulandı ve geçerli");
            
            // 4. Adım: Tabloları temizle (verileri sil, tabloları silme)
            System.out.println("\n4) Tablolardaki verileri temizleme...");
            
            // Tablo temizleme SQL betiği oluştur
            java.io.File cleanupScript = new java.io.File("cleanup_data.sql");
            try (java.io.PrintWriter writer = new java.io.PrintWriter(cleanupScript)) {
                writer.println("-- FK kısıtlamalarını geçici olarak devre dışı bırak");
                writer.println("SET session_replication_role = 'replica';");
                writer.println();
                writer.println("-- Tablolardaki verileri temizle (tabloları silmeden)");
                writer.println("TRUNCATE TABLE public.reviews CASCADE;");
                writer.println("TRUNCATE TABLE public.restaurants CASCADE;");
                writer.println();
                writer.println("-- FK kısıtlamalarını yeniden etkinleştir");
                writer.println("SET session_replication_role = 'origin';");
                writer.println();
                writer.println("\\echo 'Veriler başarıyla temizlendi'");
            }
            
            // Temizleme betiğini çalıştır
            ProcessBuilder cleanupProcess = new ProcessBuilder(
                "psql",
                "-U", dbUser,
                "-d", dbName,
                "-f", cleanupScript.getAbsolutePath()
            );
            cleanupProcess.environment().put("PGPASSWORD", dbPassword);
            cleanupProcess.redirectErrorStream(true);
            
            Process cleanProcess = cleanupProcess.start();
            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(cleanProcess.getInputStream())
            );
            
            StringBuilder cleanOutput = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                cleanOutput.append(line).append("\n");
                if (line.contains("ERROR") || line.contains("FATAL")) {
                    System.out.println("❌ " + line);
                } else if (line.contains("TRUNCATE") || line.contains("SET")) {
                    System.out.println("  " + line);
                }
            }
            
            int exitCode = cleanProcess.waitFor();
            if (exitCode != 0) {
                System.out.println("⚠️ Tabloları temizlerken uyarılar oluştu, ancak devam ediliyor...");
                System.out.println("Çıkış kodu: " + exitCode);
            } else {
                System.out.println("✓ Tablolardaki veriler başarıyla temizlendi");
            }
            
            // Temizleme betiğini sil
            cleanupScript.delete();
            
            // 2 saniye bekle
            System.out.println("2 saniye bekleniyor...");
            Thread.sleep(2000);
            
            // 5. Adım: Verileri geri yükle
            System.out.println("\n5) Yedekten veri yükleniyor...");
            
            // Sadece INSERT komutlarını içeren bir dosya oluştur
            java.io.File insertScript = new java.io.File("data_only.sql");
            try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(selectedFile));
                 java.io.PrintWriter writer = new java.io.PrintWriter(insertScript)) {
                
                writer.println("-- FK kısıtlamalarını geçici olarak devre dışı bırak");
                writer.println("SET session_replication_role = 'replica';");
                writer.println();
                
                int insertCount = 0;
                String currentLine;
                while ((currentLine = br.readLine()) != null) {
                    // Sadece INSERT komutlarını dahil et
                    if (currentLine.trim().toUpperCase().startsWith("INSERT")) {
                        writer.println(currentLine);
                        insertCount++;
                    }
                    // Sequence değerlerini ayarlayan komutları da dahil et
                    else if (currentLine.trim().toUpperCase().startsWith("SELECT SETVAL")) {
                        writer.println(currentLine);
                    }
                }
                
                if (insertCount == 0) {
                    String errorMsg = "Yedek dosyasında hiç INSERT komutu bulunamadı!";
                    System.out.println("❌ HATA: " + errorMsg);
                    showErrorDialog("Yedek Dosyası Hatası", errorMsg);
                    throw new RuntimeException(errorMsg);
                }
                
                writer.println();
                writer.println("-- FK kısıtlamalarını yeniden etkinleştir");
                writer.println("SET session_replication_role = 'origin';");
                writer.println();
                writer.println("\\echo 'Veriler başarıyla yüklendi'");
            }
            
            // INSERT komutlarını çalıştır
            ProcessBuilder insertProcess = new ProcessBuilder(
                "psql",
                "-U", dbUser,
                "-d", dbName,
                "-f", insertScript.getAbsolutePath()
            );
            insertProcess.environment().put("PGPASSWORD", dbPassword);
            insertProcess.redirectErrorStream(true);
            
            Process dataProcess = insertProcess.start();
            reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(dataProcess.getInputStream())
            );
            
            boolean hasErrors = false;
            StringBuilder dataOutput = new StringBuilder();
            int insertCount = 0;
            while ((line = reader.readLine()) != null) {
                dataOutput.append(line).append("\n");
                if (line.contains("ERROR") || line.contains("FATAL")) {
                    hasErrors = true;
                    System.out.println("❌ " + line);
                } else if (line.contains("INSERT")) {
                    insertCount++;
                    if (insertCount % 10 == 0) {
                        System.out.println("  " + insertCount + " kayıt eklendi...");
                    }
                }
            }
            
            exitCode = dataProcess.waitFor();
            
            // INSERT betiğini sil
            insertScript.delete();
            
            if (exitCode != 0) {
                if (hasErrors) {
                    System.out.println("\n⚠️ Veri yükleme sırasında hatalar oluştu (Çıkış kodu: " + exitCode + ")");
                    System.out.println("Ancak işlem tamamlandı ve bazı veriler yüklenmiş olabilir.");
                    
                    if (insertCount == 0) {
                        String errorMsg = "Hiç veri yüklenemedi! Geri yükleme başarısız oldu.";
                        System.out.println("❌ HATA: " + errorMsg);
                        showErrorDialog("Geri Yükleme Hatası", errorMsg);
                        throw new RuntimeException(errorMsg);
                    }
                } else {
                    System.out.println("\n⚠️ Veri yükleme işlemi tamamlandı ancak çıkış kodu: " + exitCode);
                    System.out.println("  " + insertCount + " kayıt eklendi, devam ediliyor...");
                }
            } else {
                System.out.println("\n✓ Toplam " + insertCount + " kayıt başarıyla geri yüklendi!");
            }
            
            // 6. Adım: Sequence değerlerini düzelt
            System.out.println("\n6) Sequence değerleri düzeltiliyor...");
            
            java.io.File seqScript = new java.io.File("fix_sequences.sql");
            try (java.io.PrintWriter writer = new java.io.PrintWriter(seqScript)) {
                writer.println("-- Sequence değerlerini tablolarda maksimum ID değerlerine göre ayarla");
                writer.println("SELECT setval('public.restaurants_id_seq', COALESCE((SELECT MAX(id) FROM public.restaurants), 1), true);");
                writer.println("SELECT setval('public.reviews_id_seq', COALESCE((SELECT MAX(id) FROM public.reviews), 1), true);");
                writer.println("\\echo 'Sequence değerleri düzeltildi'");
            }
            
            ProcessBuilder seqProcess = new ProcessBuilder(
                "psql",
                "-U", dbUser,
                "-d", dbName,
                "-f", seqScript.getAbsolutePath()
            );
            seqProcess.environment().put("PGPASSWORD", dbPassword);
            seqProcess.redirectErrorStream(true);
            
            Process fixSeqProcess = seqProcess.start();
            reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(fixSeqProcess.getInputStream())
            );
            
            StringBuilder seqOutput = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                seqOutput.append(line).append("\n");
                if (line.contains("setval")) {
                    System.out.println("  " + line);
                }
            }
            
            exitCode = fixSeqProcess.waitFor();
            
            // Sequence betiğini sil
            seqScript.delete();
            
            if (exitCode != 0) {
                System.out.println("⚠️ Sequence değerleri düzeltilirken uyarılar oluştu (Çıkış kodu: " + exitCode + ")");
                System.out.println("Ancak işleme devam ediliyor...");
            } else {
                System.out.println("✓ Sequence değerleri düzeltildi");
            }
            
            // 3 saniye bekle
            System.out.println("3 saniye bekleniyor...");
            Thread.sleep(3000);
            
            // 7. Adım: Hibernate Oturumunu Yeniden Başlat
            System.out.println("\n7) Veritabanı bağlantısı yeniden başlatılıyor...");
            
            // Hibernate oturumunu yeniden başlat ve verileri yeniden yükle
            try {
                // Önce SessionFactory'yi yeniden oluştur
                HibernateUtil.getSessionFactory();
                
                // Sonra RestaurantManager ve ReviewManager'ı yeniden başlat
                System.out.println("8) Verileri hafızaya yükleme...");
                
                // Eski nesneleri temizle
                if (this.restaurantManager != null) {
                    this.restaurantManager = null;
                }
                if (this.reviewManager != null) {
                    this.reviewManager = null;
                }
                
                // Yeni nesneleri oluştur
                this.restaurantManager = new RestaurantManager(null);
                this.reviewManager = new ReviewManager();
                this.reviewManager.setRestaurantManager(restaurantManager);
                
                // Verileri kontrol et
                int restoranSayisi = this.restaurantManager.getAllRestaurants().size();
                int degerlendirmeSayisi = this.reviewManager.getAllReviews().size();
                
                if (restoranSayisi == 0 && insertCount > 0) {
                    String errorMsg = "Veriler geri yüklendi ancak hafızaya yüklenemedi! Lütfen uygulamayı yeniden başlatın.";
                    System.out.println("❌ HATA: " + errorMsg);
                    showErrorDialog("Geri Yükleme Hatası", errorMsg);
                    throw new RuntimeException(errorMsg);
                }
                
                System.out.println("✓ Veritabanı bağlantısı başarıyla yenilendi");
                System.out.println("✓ " + restoranSayisi + " restoran ve " + degerlendirmeSayisi + " değerlendirme hafızaya yüklendi");
                System.out.println("\n✅ GERİ YÜKLEME İŞLEMİ BAŞARIYLA TAMAMLANDI!");
                System.out.println("✅ Yedekteki veriler başarıyla geri yüklendi ve görüntülenmeye hazır");
                
                // Başarı mesajı göster
                showInfoDialog("Başarılı", "Geri yükleme işlemi başarıyla tamamlandı!\n" + 
                                          restoranSayisi + " restoran ve " + 
                                          degerlendirmeSayisi + " değerlendirme geri yüklendi.");
                
                // Restoranları listele
                System.out.println("\nGeri yüklenen restoranlar:");
                listRestaurants();
                
            } catch (Exception e) {
                String errorMsg = "Veritabanı bağlantısı yeniden başlatılırken hata: " + e.getMessage();
                System.out.println("❌ " + errorMsg);
                System.out.println("Uygulamayı yeniden başlatmanız gerekiyor.");
                
                // Kullanıcıya daha fazla bilgi ver
                System.out.println("\nHata detayları:");
                e.printStackTrace();
                
                showErrorDialog("Bağlantı Hatası", errorMsg + "\n\nLütfen uygulamayı yeniden başlatın.");
                
                // Yeniden bağlanmayı dene
                System.out.println("\nVeritabanı bağlantısını yeniden kurmayı deniyorum...");
                try {
                    // Hibernate oturumunu tamamen kapat ve yeniden başlat
                    HibernateUtil.shutdown();
                    Thread.sleep(2000);
                    HibernateUtil.getSessionFactory();
                    
                    // Yöneticileri yeniden oluştur
                    this.restaurantManager = new RestaurantManager(null);
                    this.reviewManager = new ReviewManager();
                    this.reviewManager.setRestaurantManager(restaurantManager);
                    
                    System.out.println("✓ Veritabanı bağlantısı yeniden kuruldu!");
                    System.out.println("✓ Veriler başarıyla yüklendi");
                    
                    // Restoranları listele
                    System.out.println("\nGeri yüklenen restoranlar:");
                    listRestaurants();
                    
                } catch (Exception reconnectError) {
                    String reconnectErrorMsg = "Veritabanı bağlantısı yeniden kurulamadı: " + reconnectError.getMessage();
                    System.out.println("❌ " + reconnectErrorMsg);
                    System.out.println("Lütfen uygulamayı kapatıp yeniden başlatın.");
                    
                    showErrorDialog("Kritik Hata", reconnectErrorMsg + "\n\nLütfen uygulamayı kapatıp yeniden başlatın.");
                }
            }
        } catch (Exception e) {
            String errorMsg = e.getMessage();
            System.out.println("\n❌ HATA: " + errorMsg);
            
            // Hata detaylarını göster
            System.out.println("\nHata detayları:");
            e.printStackTrace();
            
            System.out.println("\nÖneriler:");
            System.out.println("1. PostgreSQL servisinin çalıştığından emin olun");
            System.out.println("2. Veritabanı kullanıcı adı ve şifresinin doğru olduğunu kontrol edin");
            System.out.println("3. Yedek dosyasının geçerli olduğunu kontrol edin");
            System.out.println("4. Uygulamayı yeniden başlatıp tekrar deneyin");
            
            showErrorDialog("Geri Yükleme Hatası", 
                "Geri yükleme işlemi sırasında bir hata oluştu:\n" + errorMsg + 
                "\n\nÖneriler:\n" +
                "1. PostgreSQL servisinin çalıştığından emin olun\n" +
                "2. Veritabanı kullanıcı adı ve şifresinin doğru olduğunu kontrol edin\n" +
                "3. Yedek dosyasının geçerli olduğunu kontrol edin\n" +
                "4. Uygulamayı yeniden başlatıp tekrar deneyin");
            
            // Hata durumunda Hibernate oturumunu yeniden başlatmayı dene
            try {
                System.out.println("\nVeritabanı bağlantısını yeniden kurmayı deniyorum...");
                
                // Hibernate oturumunu tamamen kapat ve yeniden başlat
                HibernateUtil.shutdown();
                Thread.sleep(2000);
                HibernateUtil.getSessionFactory();
                
                // Yöneticileri yeniden oluştur
                this.restaurantManager = new RestaurantManager(null);
                this.reviewManager = new ReviewManager();
                this.reviewManager.setRestaurantManager(restaurantManager);
                
                System.out.println("✓ Veritabanı bağlantısı yeniden kuruldu!");
                System.out.println("✓ Ancak geri yükleme işlemi başarısız oldu. Lütfen tekrar deneyin.");
                
                showInfoDialog("Bağlantı Yenilendi", 
                    "Veritabanı bağlantısı yeniden kuruldu, ancak geri yükleme işlemi başarısız oldu.\nLütfen tekrar deneyin.");
                
            } catch (Exception reconnectError) {
                String reconnectErrorMsg = "Veritabanı bağlantısı yeniden kurulamadı: " + reconnectError.getMessage();
                System.out.println("❌ " + reconnectErrorMsg);
                System.out.println("Lütfen uygulamayı kapatıp yeniden başlatın.");
                
                showErrorDialog("Kritik Hata", reconnectErrorMsg + "\n\nLütfen uygulamayı kapatıp yeniden başlatın.");
            }
        }
    }

    // Hata mesajı göstermek için yardımcı metod
    private void showErrorDialog(String title, String message) {
        try {
            // Swing bileşenlerini kullan
            javax.swing.JOptionPane.showMessageDialog(
                null,
                message,
                title,
                javax.swing.JOptionPane.ERROR_MESSAGE
            );
        } catch (Exception e) {
            // Swing kullanılamıyorsa konsola yaz
            System.out.println("\n❌ " + title + ": " + message);
        }
    }
    
    // Bilgi mesajı göstermek için yardımcı metod
    private void showInfoDialog(String title, String message) {
        try {
            // Swing bileşenlerini kullan
            javax.swing.JOptionPane.showMessageDialog(
                null,
                message,
                title,
                javax.swing.JOptionPane.INFORMATION_MESSAGE
            );
        } catch (Exception e) {
            // Swing kullanılamıyorsa konsola yaz
            System.out.println("\n✓ " + title + ": " + message);
        }
    }

    public static void main(String[] args) {
        new TerminalApp().start();
    }
} 