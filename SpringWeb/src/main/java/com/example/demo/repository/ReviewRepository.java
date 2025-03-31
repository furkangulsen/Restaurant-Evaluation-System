package com.example.demo.repository;

import com.example.demo.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    /**
     * Restoran adına göre değerlendirmeleri bulur
     * 
     * @param restaurantName Restoran adı
     * @return Restoran değerlendirmelerinin listesi
     */
    List<Review> findByRestaurantName(String restaurantName);
    
    /**
     * Restoran ID'sine göre değerlendirmeleri bulur
     * 
     * @param restaurantId Restoran ID'si
     * @return Restoran değerlendirmelerinin listesi
     */
    List<Review> findByRestaurantId(Long restaurantId);
    
    /**
     * Belirli bir puanın üzerindeki değerlendirmeleri bulur
     * 
     * @param rating Minimum puan
     * @return Puanı belirtilen değerin üzerinde olan değerlendirmelerin listesi
     */
    List<Review> findByRatingGreaterThanEqual(int rating);
    
    /**
     * Bir restoranın ortalama puanını hesaplar
     * 
     * @param restaurantName Restoran adı
     * @return Ortalama puan
     */
    @Query("SELECT COALESCE(AVG(CAST(r.rating AS double)), 0.0) FROM Review r WHERE r.restaurantName = :restaurantName")
    Double calculateAverageRatingByRestaurantName(@Param("restaurantName") String restaurantName);
    
    /**
     * Değerlendirmeleri tarihe göre azalan sırada getirir
     * 
     * @return Tarihe göre sıralanmış değerlendirmelerin listesi
     */
    List<Review> findAllByOrderByDateDesc();
} 