package com.example.demo.cli.shell;

import com.example.demo.model.Restaurant;
import com.example.demo.model.Review;
import com.example.demo.service.RestaurantService;
import com.example.demo.service.ReviewService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Sistem istatistikleri için komut satırı komutları.
 * Spring Shell kullanarak istatistik raporları sunar.
 * Not: Sadece CLI profilinde aktiftir.
 */
@ShellComponent
@Profile("cli")
public class StatisticsCommands {

    private final RestaurantService restaurantService;
    private final ReviewService reviewService;
    private final ShellHelper shellHelper;

    @Autowired
    public StatisticsCommands(RestaurantService restaurantService, ReviewService reviewService, ShellHelper shellHelper) {
        this.restaurantService = restaurantService;
        this.reviewService = reviewService;
        this.shellHelper = shellHelper;
    }

    /**
     * Sistem istatistiklerini gösterir
     */
    @ShellMethod(key = "stats", value = "Sistem istatistiklerini gösterir")
    public String showStatistics() {
        List<Restaurant> restaurants = restaurantService.getAllRestaurants();
        List<Review> reviews = reviewService.getAllReviews();
        
        if (restaurants.isEmpty()) {
            return shellHelper.getWarningMessage("Henüz hiç restoran kaydedilmemiş.");
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(shellHelper.getInfoMessage("=== SİSTEM İSTATİSTİKLERİ ===")).append("\n\n");
        
        // Temel istatistikler
        sb.append(shellHelper.getSuccessMessage("Genel İstatistikler:")).append("\n");
        sb.append("Toplam Restoran Sayısı: ").append(restaurants.size()).append("\n");
        sb.append("Toplam Değerlendirme Sayısı: ").append(reviews.size()).append("\n");
        
        if (!reviews.isEmpty()) {
            // Puan istatistikleri
            DoubleSummaryStatistics ratingStats = reviews.stream()
                    .mapToDouble(Review::getRating)
                    .summaryStatistics();
            
            sb.append("\n").append(shellHelper.getSuccessMessage("Puan İstatistikleri:")).append("\n");
            sb.append("Ortalama Puan: ").append(String.format("%.2f", ratingStats.getAverage())).append("\n");
            sb.append("En Düşük Puan: ").append(ratingStats.getMin()).append("\n");
            sb.append("En Yüksek Puan: ").append(ratingStats.getMax()).append("\n");
            
            // Puan dağılımı
            Map<Integer, Long> ratingDistribution = reviews.stream()
                    .collect(Collectors.groupingBy(Review::getRating, Collectors.counting()));
            
            sb.append("\n").append(shellHelper.getSuccessMessage("Puan Dağılımı:")).append("\n");
            for (int i = 5; i >= 1; i--) {
                long count = ratingDistribution.getOrDefault(i, 0L);
                double percentage = (double) count / reviews.size() * 100;
                sb.append(i).append(" Yıldız: ").append(count)
                  .append(" (% ").append(String.format("%.1f", percentage)).append(")")
                  .append("\n");
            }
            
            // En çok değerlendirilen restoranlar
            Map<Restaurant, Long> reviewCountByRestaurant = reviews.stream()
                    .collect(Collectors.groupingBy(Review::getRestaurant, Collectors.counting()));
            
            sb.append("\n").append(shellHelper.getSuccessMessage("En Çok Değerlendirilen Restoranlar:")).append("\n");
            reviewCountByRestaurant.entrySet().stream()
                    .sorted(Map.Entry.<Restaurant, Long>comparingByValue().reversed())
                    .limit(5)
                    .forEach(entry -> sb.append(entry.getKey().getName())
                            .append(": ")
                            .append(entry.getValue())
                            .append(" değerlendirme")
                            .append("\n"));
            
            // En yüksek puanlı restoranlar
            sb.append("\n").append(shellHelper.getSuccessMessage("En Yüksek Puanlı Restoranlar:")).append("\n");
            restaurants.stream()
                    .filter(r -> r.getAverageRating() > 0)
                    .sorted(Comparator.comparing(Restaurant::getAverageRating).reversed())
                    .limit(5)
                    .forEach(r -> sb.append(r.getName())
                            .append(": ")
                            .append(String.format("%.1f", r.getAverageRating()))
                            .append(" / 5.0")
                            .append("\n"));
        } else {
            sb.append("\n").append(shellHelper.getWarningMessage("Henüz hiç değerlendirme yapılmamış."));
        }
        
        return sb.toString();
    }
}
