package com.example.demo.controller.api;

import com.example.demo.model.Review;
import com.example.demo.service.ReviewService;
import com.example.demo.util.AppLogger;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Değerlendirmeler için REST API kontrolcüsü
 * Sadece okuma işlemlerini destekler (GET)
 */
@RestController
@RequestMapping("/api/reviews")
public class ReviewApiController {

    private static final Logger LOGGER = AppLogger.getLogger(ReviewApiController.class);
    private final ReviewService reviewService;

    @Autowired
    public ReviewApiController(ReviewService reviewService) {
        this.reviewService = reviewService;
        LOGGER.info("ReviewApiController başlatıldı");
    }

    /**
     * Tüm değerlendirmeleri getirir
     * 
     * @return Değerlendirmelerin listesi
     */
    @GetMapping
    public ResponseEntity<List<Review>> getAllReviews() {
        LOGGER.info("API: Tüm değerlendirmeler getiriliyor");
        List<Review> reviews = reviewService.getAllReviews();
        return ResponseEntity.ok(reviews);
    }

    /**
     * ID'ye göre değerlendirme getirir
     * 
     * @param id Değerlendirme ID'si
     * @return Bulunan değerlendirme veya 404 hatası
     */
    @GetMapping("/{id}")
    public ResponseEntity<Review> getReviewById(@PathVariable Long id) {
        LOGGER.info("API: ID'ye göre değerlendirme getiriliyor: {}", id);
        return reviewService.getReviewById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Restoran adına göre değerlendirmeleri getirir
     * 
     * @param restaurantName Restoran adı
     * @return Değerlendirmelerin listesi
     */
    @GetMapping("/restaurant/{restaurantName}")
    public ResponseEntity<List<Review>> getReviewsByRestaurantName(@PathVariable String restaurantName) {
        LOGGER.info("API: Restoran adına göre değerlendirmeler getiriliyor: {}", restaurantName);
        List<Review> reviews = reviewService.getReviewsByRestaurantName(restaurantName);
        return ResponseEntity.ok(reviews);
    }

    /**
     * Minimum puana göre değerlendirmeleri filtreler
     * 
     * @param rating Minimum puan
     * @return Filtrelenmiş değerlendirmelerin listesi
     */
    @GetMapping("/search/rating/{rating}")
    public ResponseEntity<List<Review>> getReviewsByMinimumRating(@PathVariable int rating) {
        LOGGER.info("API: Minimum puana göre değerlendirmeler getiriliyor: {}", rating);
        List<Review> reviews = reviewService.getReviewsByMinimumRating(rating);
        return ResponseEntity.ok(reviews);
    }

    /**
     * Değerlendirme istatistiklerini getirir
     * 
     * @return İstatistik bilgileri
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getReviewStats() {
        LOGGER.info("API: Değerlendirme istatistikleri getiriliyor");
        List<Review> reviews = reviewService.getAllReviews();
        long count = reviews.size();
        double avgRating = reviews.stream()
                .mapToDouble(Review::getRating)
                .average()
                .orElse(0.0);
        
        Map<String, Object> stats = Map.of(
            "totalReviews", count,
            "averageRating", avgRating
        );
        
        return ResponseEntity.ok(stats);
    }
} 