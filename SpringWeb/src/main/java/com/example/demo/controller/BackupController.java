package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
@RequestMapping("/backup")
public class BackupController {

    private static final String BACKUP_DIR = "yedekler";
    private static final String DB_NAME = "restaurant_db";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "123456789";
    private static final String SAMPLE_DATA_FILE = "src/main/resources/sample_data.sql";

    /**
     * Yedekleme işlemlerini yönetecek sayfa
     */
    @GetMapping
    public String showBackupPage(Model model) {
        // Yedekleme işlemi hakkında bilgi ekle
        String backupInfo = "Veritabanı yedekleme, tüm restoran ve değerlendirme verilerini bir dosyaya kaydetmenizi sağlar. Bu dosyayı daha sonra geri yükleyerek verilerinizi kurtarabilirsiniz.\n\n" +
                            "Yedek Al: Mevcut veritabanının tam bir kopyasını oluşturur.\n" +
                            "Geri Yükle: Önceden alınan yedeği mevcut veritabanına geri yükler. Bu işlem mevcut verileri sileceği için dikkatli olunmalıdır.\n" +
                            "Sil: Seçilen yedek dosyasını kalıcı olarak siler.\n" +
                            "Yedek dosyaları yedekler/ klasöründe saklanır ve PostgreSQL'in pg_dump ve pg_restore komutları kullanılarak işlenir.";
        model.addAttribute("backupInfo", backupInfo);

        // Örnek veri seti hakkında bilgi ekle
        String sampleDataInfo = "Örnek veri seti, sisteminize 15 farklı restoran ve 50+ değerlendirme ekler. " +
                                "Bu veriler test ve gösterim amaçlıdır. Mevcut verileriniz silinecektir.";
        model.addAttribute("sampleDataInfo", sampleDataInfo);

        // Yedekler klasörünü kontrol et ve oluştur
        File backupDir = new File(BACKUP_DIR);
        if (!backupDir.exists()) {
            backupDir.mkdir();
        }

        // Mevcut yedekleri listele
        List<Map<String, Object>> backups = listBackups();
        model.addAttribute("backups", backups);
        
        return "backup/index";
    }

    /**
     * Yeni bir yedek oluştur
     */
    @PostMapping("/create")
    public String createBackup(@RequestParam("backupName") String backupName, 
                              RedirectAttributes redirectAttributes) {
        String originalBackupName = backupName;
        try {
            // Yedek ismi boş kontrolü
            if (backupName == null || backupName.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Yedek ismi boş olamaz!");
                return "redirect:/backup";
            }

            // Dosya adı güvenliği için geçersiz karakterleri temizle
            backupName = backupName.replaceAll("[^a-zA-Z0-9_\\-\\.]", "_");
            
            // Tarih ve saat formatı
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm");
            String timestamp = LocalDateTime.now().format(formatter);
            
            // Yedek klasörünü kontrol et ve oluştur
            File backupDir = new File(BACKUP_DIR);
            if (!backupDir.exists()) {
                boolean dirCreated = backupDir.mkdir();
                if (!dirCreated) {
                    throw new IOException("Yedek klasörü oluşturulamadı: " + BACKUP_DIR);
                }
            }
            
            String fileName = BACKUP_DIR + "/" + backupName + "_" + timestamp + ".backup";
            File backupFile = new File(fileName);
            
            // Dosya zaten varsa benzersiz isim oluştur
            int counter = 1;
            while (backupFile.exists()) {
                fileName = BACKUP_DIR + "/" + backupName + "_" + timestamp + "_" + counter + ".backup";
                backupFile = new File(fileName);
                counter++;
            }
            
            // pg_dump komutunun varlığını kontrol et
            try {
                Process checkPgDump = Runtime.getRuntime().exec("pg_dump --version");
                int exitCode = checkPgDump.waitFor();
                if (exitCode != 0) {
                    throw new IOException("pg_dump komutu çalıştırılamadı. PostgreSQL istemci araçlarının yüklü olduğundan emin olun.");
                }
            } catch (IOException e) {
                throw new IOException("pg_dump bulunamadı. PostgreSQL istemci araçlarının yüklü olduğundan emin olun.", e);
            }
            
            // pg_dump komutu ile yedekleme
            ProcessBuilder pb = new ProcessBuilder(
                "pg_dump",
                "-U", DB_USER,
                "-d", DB_NAME,
                "--format=c",  // Custom format
                "--blobs",     // Blob verilerini dahil et
                "--encoding=UTF8", // Karakter kodlaması
                "--verbose",   // Verbose mode
                "--file=" + backupFile.getAbsolutePath()
            );
            
            pb.environment().put("PGPASSWORD", DB_PASSWORD);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            // İşlem çıktısını oku
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                // Yedek dosyasının boyutunu kontrol et
                long fileSize = backupFile.length();
                if (fileSize < 100) { // 100 byte'dan küçük dosyalar muhtemelen boştur
                    redirectAttributes.addFlashAttribute("warning", "Yedek dosyası çok küçük, yedekleme başarısız olmuş olabilir!");
                } else {
                    redirectAttributes.addFlashAttribute("success", 
                        "Veritabanı yedeği başarıyla alındı! Yedek dosyası: " + backupFile.getName() + " (" + (fileSize / 1024) + " KB)");
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "Yedekleme işlemi başarısız oldu! Çıkış kodu: " + exitCode + "\nÇıktı: " + output.toString());
            }
            
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Yedek alınırken I/O hatası oluştu: " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            redirectAttributes.addFlashAttribute("error", "Yedekleme işlemi kesintiye uğradı: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Yedek alınırken beklenmeyen hata oluştu: " + e.getMessage());
            e.printStackTrace();
        }
        
        return "redirect:/backup";
    }

    /**
     * Seçilen yedeği geri yükle
     */
    @PostMapping("/restore")
    public String restoreBackup(@RequestParam("backupFile") String backupFileName,
                              RedirectAttributes redirectAttributes) {
        try {
            // Güvenlik kontrolü - dosya adında tehlikeli karakterler olmamalı
            if (backupFileName.contains("..") || backupFileName.contains("/") || backupFileName.contains("\\")) {
                redirectAttributes.addFlashAttribute("error", "Geçersiz dosya adı!");
                return "redirect:/backup";
            }
            
            // Dosya kontrolü
            File backupFile = new File(BACKUP_DIR + "/" + backupFileName);
            if (!backupFile.exists() || !backupFile.canRead()) {
                redirectAttributes.addFlashAttribute("error", "Seçilen yedek dosyası bulunamadı veya okunamıyor!");
                return "redirect:/backup";
            }
            
            // Dosya uzantı kontrolü
            String lowerCaseFileName = backupFileName.toLowerCase();
            if (!lowerCaseFileName.endsWith(".backup") && !lowerCaseFileName.endsWith(".sql") && !lowerCaseFileName.endsWith(".dump")) {
                redirectAttributes.addFlashAttribute("error", "Geçersiz yedek dosyası formatı!");
                return "redirect:/backup";
            }
            
            // pg_restore komutunun varlığını kontrol et
            try {
                Process checkPgRestore = Runtime.getRuntime().exec("pg_restore --version");
                int exitCode = checkPgRestore.waitFor();
                if (exitCode != 0) {
                    throw new IOException("pg_restore komutu çalıştırılamadı. PostgreSQL istemci araçlarının yüklü olduğundan emin olun.");
                }
            } catch (IOException e) {
                throw new IOException("pg_restore bulunamadı. PostgreSQL istemci araçlarının yüklü olduğundan emin olun.", e);
            }
            
            // pg_restore komutu ile geri yükleme
            ProcessBuilder pb = new ProcessBuilder(
                "pg_restore",
                "--clean",       // Mevcut verileri temizle
                "--if-exists",   // Varsa sil
                "-U", DB_USER,
                "-d", DB_NAME,
                backupFile.getAbsolutePath()
            );
            
            pb.environment().put("PGPASSWORD", DB_PASSWORD);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            // İşlem çıktısını oku
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                redirectAttributes.addFlashAttribute("success", "Veritabanı başarıyla geri yüklendi!");
            } else {
                // Bazı durumlarda pg_restore hata dönse bile işlem kısmen başarılı olabilir
                if (output.toString().contains("error")) {
                    redirectAttributes.addFlashAttribute("warning", 
                        "Geri yükleme işlemi tamamlandı ancak bazı hatalar oluştu. Veritabanınız kısmen güncellenmiş olabilir.");
                } else {
                    redirectAttributes.addFlashAttribute("error", 
                        "Geri yükleme işlemi başarısız oldu! Çıkış kodu: " + exitCode + "\nDetaylar: " + output.toString());
                }
            }
            
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Geri yükleme sırasında I/O hatası: " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            redirectAttributes.addFlashAttribute("error", "Geri yükleme işlemi kesintiye uğradı: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Geri yükleme işlemi sırasında beklenmeyen hata: " + e.getMessage());
            e.printStackTrace();
        }
        
        return "redirect:/backup";
    }

    /**
     * Seçilen yedeği sil
     */
    @PostMapping("/delete")
    public String deleteBackup(@RequestParam("backupFile") String backupFileName,
                             RedirectAttributes redirectAttributes) {
        try {
            // Güvenlik kontrolü - dosya adında tehlikeli karakterler olmamalı
            if (backupFileName.contains("..") || backupFileName.contains("/") || backupFileName.contains("\\")) {
                redirectAttributes.addFlashAttribute("error", "Geçersiz dosya adı!");
                return "redirect:/backup";
            }
            
            // Dosya uzantı kontrolü
            String lowerCaseFileName = backupFileName.toLowerCase();
            if (!lowerCaseFileName.endsWith(".backup") && !lowerCaseFileName.endsWith(".sql") && !lowerCaseFileName.endsWith(".dump")) {
                redirectAttributes.addFlashAttribute("error", "Geçersiz yedek dosyası formatı!");
                return "redirect:/backup";
            }
            
            // Dosya kontrolü ve silme işlemi
            File backupFile = new File(BACKUP_DIR + "/" + backupFileName);
            if (!backupFile.exists()) {
                redirectAttributes.addFlashAttribute("error", "Yedek dosyası bulunamadı: " + backupFileName);
                return "redirect:/backup";
            }
            
            if (!backupFile.isFile()) {
                redirectAttributes.addFlashAttribute("error", "Belirtilen yol bir dosya değil: " + backupFileName);
                return "redirect:/backup";
            }
            
            boolean deleted = backupFile.delete();
            if (deleted) {
                redirectAttributes.addFlashAttribute("success", "Yedek dosyası başarıyla silindi: " + backupFileName);
            } else {
                // Dosya silinemedi - yetki sorunu veya kilitli dosya olabilir
                redirectAttributes.addFlashAttribute("error", "Yedek dosyası silinemedi. Dosya kilitli veya erişim izni yok: " + backupFileName);
            }
            
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute("error", "Dosya silme işlemi için yetki sorunu: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Yedek silinirken beklenmeyen hata oluştu: " + e.getMessage());
            e.printStackTrace();
        }
        
        return "redirect:/backup";
    }

    /**
     * Örnek veri setini yükle
     */
    @PostMapping("/load-sample-data")
    public String loadSampleData(RedirectAttributes redirectAttributes) {
        try {
            // Örnek veri dosyasının varlığını kontrol et
            File sampleDataFile = new File(SAMPLE_DATA_FILE);
            if (!sampleDataFile.exists() || !sampleDataFile.canRead()) {
                redirectAttributes.addFlashAttribute("error", "Örnek veri dosyası bulunamadı: " + SAMPLE_DATA_FILE);
                return "redirect:/backup";
            }
            
            // psql komutunun varlığını kontrol et
            try {
                Process checkPsql = Runtime.getRuntime().exec("psql --version");
                int exitCode = checkPsql.waitFor();
                if (exitCode != 0) {
                    throw new IOException("psql komutu çalıştırılamadı. PostgreSQL istemci araçlarının yüklü olduğundan emin olun.");
                }
            } catch (IOException e) {
                throw new IOException("psql bulunamadı. PostgreSQL istemci araçlarının yüklü olduğundan emin olun.", e);
            }
            
            // psql komutu ile örnek veriyi yükle
            ProcessBuilder pb = new ProcessBuilder(
                "psql",
                "-U", DB_USER,
                "-d", DB_NAME,
                "-f", sampleDataFile.getAbsolutePath()
            );
            
            pb.environment().put("PGPASSWORD", DB_PASSWORD);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            // İşlem çıktısını oku
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                redirectAttributes.addFlashAttribute("success", 
                    "Örnek veri seti başarıyla yüklendi! Artık 15 restoran ve 50+ değerlendirme içeren bir sisteme sahipsiniz.");
            } else {
                // Çıktıda hata mesajlarını ara
                if (output.toString().contains("error") || output.toString().contains("ERROR")) {
                    redirectAttributes.addFlashAttribute("error", 
                        "Örnek veri seti yüklenirken hata oluştu! Detaylar: " + output.toString());
                } else {
                    redirectAttributes.addFlashAttribute("warning", 
                        "Örnek veri seti yüklendi ancak bazı uyarılar oluştu. Detaylar: " + output.toString());
                }
            }
            
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Örnek veri seti yüklenirken I/O hatası: " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            redirectAttributes.addFlashAttribute("error", "Örnek veri seti yükleme işlemi kesintiye uğradı: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Örnek veri seti yüklenirken beklenmeyen hata: " + e.getMessage());
            e.printStackTrace();
        }
        
        return "redirect:/backup";
    }

    /**
     * Mevcut yedekleri listele
     */
    private List<Map<String, Object>> listBackups() {
        List<Map<String, Object>> backups = new ArrayList<>();
        
        try {
            File backupDir = new File(BACKUP_DIR);
            if (!backupDir.exists() || !backupDir.isDirectory()) {
                return backups;
            }
            
            // Yedek dosyalarını bul
            File[] backupFiles = backupDir.listFiles((dir, name) -> 
                name.toLowerCase().endsWith(".backup") || 
                name.toLowerCase().endsWith(".sql") || 
                name.toLowerCase().endsWith(".dump")
            );
            
            if (backupFiles == null || backupFiles.length == 0) {
                return backups;
            }
            
            // Dosyaları tarihe göre sırala (en yenisi en üstte)
            Arrays.sort(backupFiles, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
            
            // Dosya bilgilerini hazırla
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            
            for (File file : backupFiles) {
                try {
                    Map<String, Object> backup = new HashMap<>();
                    backup.put("fileName", file.getName());
                    backup.put("size", file.length() / 1024); // KB cinsinden
                    
                    try {
                        LocalDateTime dateTime = LocalDateTime.ofInstant(
                            Files.getLastModifiedTime(file.toPath()).toInstant(), 
                            java.time.ZoneId.systemDefault()
                        );
                        backup.put("date", dateTime.format(formatter));
                    } catch (IOException e) {
                        // Dosya zaman bilgisi alınamazsa şimdiki zamanı kullan
                        LocalDateTime now = LocalDateTime.now();
                        backup.put("date", now.format(formatter) + " (yaklaşık)");
                    }
                    
                    backups.add(backup);
                } catch (Exception e) {
                    // Herhangi bir dosya işleme hatası olursa bu dosyayı atla
                    System.err.println("Dosya işlenirken hata: " + file.getName() + " - " + e.getMessage());
                }
            }
        } catch (Exception e) {
            // Genel hata durumunda boş liste döndür
            System.err.println("Yedekleri listelerken hata: " + e.getMessage());
        }
        
        return backups;
    }
} 