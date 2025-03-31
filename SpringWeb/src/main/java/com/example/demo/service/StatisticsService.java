package com.example.demo.service;

import com.example.demo.model.Restaurant;
import com.example.demo.model.Review;
import com.example.demo.repository.RestaurantRepository;
import com.example.demo.repository.ReviewRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * İstatistik hesaplamaları için servis sınıfı.
 * Bu sınıf, uygulamadaki istatistikleri hesaplar.
 */
@Service
public class StatisticsService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(StatisticsService.class);
    
    private final RestaurantRepository restaurantRepository;
    private final ReviewRepository reviewRepository;
    
    @Autowired
    public StatisticsService(RestaurantRepository restaurantRepository, 
                             ReviewRepository reviewRepository) {
        this.restaurantRepository = restaurantRepository;
        this.reviewRepository = reviewRepository;
    }
    
    /**
     * Tüm istatistikleri hesaplar.
     * 
     * @return İstatistik sonuçları
     */
    public Map<String, Object> calculateStatistics() {
        LOGGER.debug("İstatistik hesaplama başlıyor");
        
        // Farklı istatistikleri hesapla
        Long restaurantCount = restaurantRepository.count();
        Long reviewCount = reviewRepository.count();
        Double averageRating = calculateAverageRating();
        Map<String, Long> reviewsByLocation = getReviewCountByLocation();
        
        Map<String, Object> result = new HashMap<>();
        result.put("restaurantCount", restaurantCount);
        result.put("reviewCount", reviewCount);
        result.put("averageRating", averageRating);
        result.put("reviewsByLocation", reviewsByLocation);
        
        LOGGER.debug("İstatistik hesaplama tamamlandı");
        return result;
    }
    
    /**
     * Ortalama puanı hesaplar.
     * 
     * @return Ortalama puan
     */
    private Double calculateAverageRating() {
        List<Review> allReviews = reviewRepository.findAll();
        if (allReviews.isEmpty()) {
            return 0.0;
        }
        
        return allReviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }
    
    /**
     * Konuma göre değerlendirme sayılarını hesaplar.
     * 
     * @return Konum başına değerlendirme sayısı
     */
    private Map<String, Long> getReviewCountByLocation() {
        List<Restaurant> restaurants = restaurantRepository.findAll();
        Map<String, Long> result = new HashMap<>();
        
        for (Restaurant restaurant : restaurants) {
            String location = restaurant.getLocation();
            if (location == null || location.isEmpty()) {
                continue;
            }
            
            int count = reviewRepository.findByRestaurantId(restaurant.getId()).size();
            Long countLong = Long.valueOf(count);
            
            if (result.containsKey(location)) {
                result.put(location, result.get(location) + countLong);
            } else {
                result.put(location, countLong);
            }
        }
        
        return result;
    }
} 