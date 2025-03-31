package com.example.demo.cli.shell;

import com.example.demo.model.Restaurant;
import com.example.demo.model.Review;
import com.example.demo.service.RestaurantService;
import com.example.demo.service.ReviewService;
import com.example.demo.util.AppLogger;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.shell.table.ArrayTableModel;
import org.springframework.shell.table.BorderStyle;
import org.springframework.shell.table.TableBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Değerlendirme işlemleri için komut satırı komutları.
 * Spring Shell kullanarak CRUD işlemlerini sağlar.
 * Not: Sadece CLI profilinde aktiftir.
 */
@ShellComponent
@Profile("cli")
public class ReviewCommands {

    private static final Logger LOGGER = AppLogger.getLogger(ReviewCommands.class);
    private final ReviewService reviewService;
    private final RestaurantService restaurantService;
    private final ShellHelper shellHelper;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @Autowired
    public ReviewCommands(ReviewService reviewService, RestaurantService restaurantService, ShellHelper shellHelper) {
        this.reviewService = reviewService;
        this.restaurantService = restaurantService;
        this.shellHelper = shellHelper;
        LOGGER.info("Değerlendirme komutları başlatıldı");
    }

    /**
     * Tüm değerlendirmeleri listeler
     */
    @ShellMethod(key = "list-reviews", value = "Tüm değerlendirmeleri veya belirli bir restoranın değerlendirmelerini listeler")
    public String listReviews(
            @ShellOption(help = "Restoran ID'si (tüm değerlendirmeler için boş bırakın)", defaultValue = ShellOption.NULL) Long restaurantId) {
        
        List<Review> reviews;
        String title;
        
        if (restaurantId == null) {
            reviews = reviewService.getAllReviews();
            title = "Tüm Değerlendirmeler";
        } else {
            Optional<Restaurant> restaurantOpt = restaurantService.getRestaurantById(restaurantId);
            
            if (restaurantOpt.isEmpty()) {
                return shellHelper.getErrorMessage("ID: " + restaurantId + " ile bir restoran bulunamadı!");
            }
            
            Restaurant restaurant = restaurantOpt.get();
            reviews = reviewService.getReviewsByRestaurantId(restaurantId);
            title = restaurant.getName() + " Restoranı Değerlendirmeleri";
        }
        
        if (reviews.isEmpty()) {
            return shellHelper.getWarningMessage("Henüz hiç değerlendirme yapılmamış.");
        }
        
        // Tablo başlıkları
        String[] headers = {"ID", "Restoran", "Puan", "Yorum", "Tarih"};
        
        // Tablo verilerini hazırla
        List<Object[]> data = new ArrayList<>();
        data.add(headers);
        
        for (Review review : reviews) {
            data.add(new Object[]{
                review.getId().toString(),
                review.getRestaurant().getName(),
                review.getRating() + "/5",
                review.getComment(),
                review.getCreatedAt().format(DATE_FORMATTER)
            });
        }
        
        // Tablo modelini oluştur
        ArrayTableModel tableModel = new ArrayTableModel(data.toArray(new Object[0][]));
        TableBuilder tableBuilder = new TableBuilder(tableModel);
        tableBuilder.addFullBorder(BorderStyle.fancy_light);
        
        return shellHelper.getInfoMessage(title + ":") + "\n" + tableBuilder.build().render(100);
    }
    
    /**
     * Yeni bir değerlendirme ekler
     */
    @ShellMethod(key = "add-review", value = "Yeni bir değerlendirme ekler")
    public String addReview(
            @ShellOption(help = "Restoran ID'si") Long restaurantId,
            @ShellOption(help = "Puan (1-5)") Integer rating,
            @ShellOption(help = "Yorum") String comment) {
        
        Optional<Restaurant> restaurantOpt = restaurantService.getRestaurantById(restaurantId);
        
        if (restaurantOpt.isEmpty()) {
            return shellHelper.getErrorMessage("ID: " + restaurantId + " ile bir restoran bulunamadı!");
        }
        
        if (rating < 1 || rating > 5) {
            return shellHelper.getErrorMessage("Puan 1 ile 5 arasında olmalıdır!");
        }
        
        Restaurant restaurant = restaurantOpt.get();
        
        Review review = new Review();
        review.setRestaurantId(restaurantId);
        review.setRating(rating);
        review.setComment(comment);
        review.setCreatedAt(LocalDateTime.now());
        review.setRestaurantName(restaurant.getName());
        
        try {
            reviewService.saveReview(review);
            LOGGER.info("Yeni değerlendirme eklendi: Restoran={}, Puan={}", restaurant.getName(), rating);
            return shellHelper.getSuccessMessage("Değerlendirme başarıyla eklendi! ID: " + review.getId());
        } catch (Exception e) {
            LOGGER.error("Değerlendirme eklenirken hata oluştu", e);
            return shellHelper.getErrorMessage("Değerlendirme eklenirken bir hata oluştu: " + e.getMessage());
        }
    }
    
    /**
     * Var olan bir değerlendirmeyi günceller
     */
    @ShellMethod(key = "update-review", value = "Var olan bir değerlendirmeyi günceller")
    public String updateReview(
            @ShellOption(help = "Değerlendirme ID'si") Long id,
            @ShellOption(help = "Yeni puan (1-5) (değişmeyecekse 0)", defaultValue = "0") Integer rating,
            @ShellOption(help = "Yeni yorum (değişmeyecekse boş bırakabilirsiniz)", defaultValue = "") String comment) {
        
        Optional<Review> reviewOpt = reviewService.getReviewById(id);
        
        if (reviewOpt.isEmpty()) {
            return shellHelper.getErrorMessage("ID: " + id + " ile bir değerlendirme bulunamadı!");
        }
        
        Review review = reviewOpt.get();
        boolean updated = false;
        
        if (rating != 0) {
            if (rating < 1 || rating > 5) {
                return shellHelper.getErrorMessage("Puan 1 ile 5 arasında olmalıdır!");
            }
            review.setRating(rating);
            updated = true;
        }
        
        if (!comment.trim().isEmpty()) {
            review.setComment(comment);
            updated = true;
        }
        
        if (!updated) {
            return shellHelper.getWarningMessage("Güncelleme yapılmadı çünkü değişiklik belirtilmedi.");
        }
        
        try {
            reviewService.saveReview(review);
            LOGGER.info("Değerlendirme güncellendi: ID={}", review.getId());
            return shellHelper.getSuccessMessage("Değerlendirme başarıyla güncellendi! ID: " + review.getId());
        } catch (Exception e) {
            LOGGER.error("Değerlendirme güncellenirken hata oluştu", e);
            return shellHelper.getErrorMessage("Değerlendirme güncellenirken bir hata oluştu: " + e.getMessage());
        }
    }
    
    /**
     * Bir değerlendirmeyi siler
     */
    @ShellMethod(key = "delete-review", value = "Bir değerlendirmeyi siler")
    public String deleteReview(@ShellOption(help = "Silinecek değerlendirme ID'si") Long id) {
        Optional<Review> reviewOpt = reviewService.getReviewById(id);
        
        if (reviewOpt.isEmpty()) {
            return shellHelper.getErrorMessage("ID: " + id + " ile bir değerlendirme bulunamadı!");
        }
        
        try {
            reviewService.deleteReview(id);
            LOGGER.info("Değerlendirme silindi: ID={}", id);
            return shellHelper.getSuccessMessage("Değerlendirme başarıyla silindi!");
        } catch (Exception e) {
            LOGGER.error("Değerlendirme silinirken hata oluştu", e);
            return shellHelper.getErrorMessage("Değerlendirme silinirken bir hata oluştu: " + e.getMessage());
        }
    }
    
    /**
     * Bir değerlendirmenin detaylarını gösterir
     */
    @ShellMethod(key = "show-review", value = "Bir değerlendirmenin detaylarını gösterir")
    public String showReviewDetails(@ShellOption(help = "Değerlendirme ID'si") Long id) {
        Optional<Review> reviewOpt = reviewService.getReviewById(id);
        
        if (reviewOpt.isEmpty()) {
            return shellHelper.getErrorMessage("ID: " + id + " ile bir değerlendirme bulunamadı!");
        }
        
        Review review = reviewOpt.get();
        
        StringBuilder sb = new StringBuilder();
        sb.append(shellHelper.getInfoMessage("Değerlendirme Detayları:"))
          .append("\n")
          .append(shellHelper.getSuccessMessage("ID: "))
          .append(review.getId())
          .append("\n")
          .append(shellHelper.getSuccessMessage("Restoran: "))
          .append(review.getRestaurant().getName())
          .append("\n")
          .append(shellHelper.getSuccessMessage("Puan: "))
          .append(review.getRating())
          .append("/5")
          .append("\n")
          .append(shellHelper.getSuccessMessage("Yorum: "))
          .append(review.getComment())
          .append("\n")
          .append(shellHelper.getSuccessMessage("Tarih: "))
          .append(review.getCreatedAt().format(DATE_FORMATTER));
        
        return sb.toString();
    }

    /**
     * Bir restorana ait değerlendirmeleri listeler
     */
    @ShellMethod(key = "list-reviews-by-restaurant", value = "Bir restorana ait değerlendirmeleri listeler")
    public String listReviewsByRestaurant(@ShellOption(help = "Restoran ID'si") Long restaurantId) {
        if (restaurantId == null) {
            return shellHelper.getErrorMessage("Restoran ID'si belirtilmelidir!");
        }
        
        List<Review> reviews = reviewService.getReviewsByRestaurantId(restaurantId);
        if (reviews.isEmpty()) {
            return shellHelper.getWarningMessage("Bu restoran için henüz hiç değerlendirme yapılmamış.");
        }
        
        // Tablo başlıkları
        String[] headers = {"ID", "Puan", "Yorum", "Tarih"};
        
        // Tablo verilerini hazırla
        List<String> rows = new ArrayList<>();
        
        // Başlık satırını ekle
        StringBuilder headerRow = new StringBuilder();
        headerRow.append("|");
        for (String header : headers) {
            headerRow.append(" ").append(header).append(" |");
        }
        rows.add(headerRow.toString());
        
        // Değerlendirme verilerini ekle
        for (Review review : reviews) {
            String id = review.getId().toString();
            String rating = String.valueOf(review.getRating());
            String comment = review.getComment();
            String date = review.getCreatedAt().toString();
            
            StringBuilder row = new StringBuilder();
            row.append("|");
            row.append(" ").append(id).append(" |");
            row.append(" ").append(rating).append(" |");
            row.append(" ").append(comment).append(" |");
            row.append(" ").append(date).append(" |");
            
            rows.add(row.toString());
        }
        
        // Tüm çıktıyı birleştir
        StringBuilder result = new StringBuilder();
        result.append(shellHelper.getInfoMessage("Değerlendirmeler:")).append("\n");
        
        // Tablonun tüm satırlarını ekle
        for (String row : rows) {
            result.append(row).append("\n");
        }
        
        return result.toString();
    }
}
