package com.example.demo.service;

import com.example.demo.model.Restaurant;
import com.example.demo.model.Review;
import com.example.demo.repository.RestaurantRepository;
import com.example.demo.repository.ReviewRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReviewService.class);

    private final ReviewRepository reviewRepository;
    private final RestaurantRepository restaurantRepository;

    @Autowired
    public ReviewService(ReviewRepository reviewRepository, RestaurantRepository restaurantRepository) {
        this.reviewRepository = reviewRepository;
        this.restaurantRepository = restaurantRepository;
    }

    /**
     * Tüm değerlendirmeleri getirir
     * 
     * @return Değerlendirmelerin listesi
     */
    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }
    
    /**
     * Değerlendirmeleri tarihe göre sıralı getirir
     * 
     * @return Tarihe göre sıralanmış değerlendirmelerin listesi
     */
    public List<Review> getAllReviewsByDate() {
        return reviewRepository.findAllByOrderByDateDesc();
    }

    /**
     * ID'ye göre değerlendirme bulur
     * 
     * @param id Değerlendirme ID'si
     * @return Bulunan değerlendirme, yoksa Optional.empty()
     */
    public Optional<Review> getReviewById(Long id) {
        LOGGER.debug("ID'ye göre değerlendirme aranıyor: {}", id);
        return reviewRepository.findById(id);
    }

    /**
     * Restoran adına göre değerlendirmeleri bulur
     * 
     * @param restaurantName Restoran adı
     * @return Restoran değerlendirmelerinin listesi
     */
    public List<Review> getReviewsByRestaurantName(String restaurantName) {
        return reviewRepository.findByRestaurantName(restaurantName);
    }

    /**
     * Restoran ID'sine göre değerlendirmeleri bulur
     * 
     * @param restaurantId Restoran ID'si
     * @return Restoran değerlendirmelerinin listesi
     */
    public List<Review> getReviewsByRestaurantId(Long restaurantId) {
        // Önce restoran varlığını kontrol et
        return restaurantRepository.findById(restaurantId)
            .map(restaurant -> reviewRepository.findByRestaurantName(restaurant.getName()))
            .orElse(List.of());
    }

    /**
     * Yeni bir değerlendirme ekler
     * 
     * @param review Eklenecek değerlendirme
     * @return Eklenen değerlendirme
     */
    @Transactional
    public Review saveReview(Review review) {
        LOGGER.debug("Değerlendirme kaydediliyor: {}", review);
        
        // Eksik bilgilerin kontrolü
        if (review.getRestaurantName() == null || review.getRestaurantName().isEmpty()) {
            // RestaurantId varsa, bu ID'yi kullanarak restoran ismini ayarla
            if (review.getRestaurantId() != null) {
                Optional<Restaurant> restaurantOpt = restaurantRepository.findById(review.getRestaurantId());
                if (restaurantOpt.isPresent()) {
                    review.setRestaurantName(restaurantOpt.get().getName());
                }
            }
        }
        
        // Tarih bilgisi eksikse şimdiki zamanı ayarla
        if (review.getCreatedAt() == null) {
            review.setCreatedAt(LocalDateTime.now());
        }
        
        // Değerlendirmeyi kaydet
        Review savedReview = reviewRepository.save(review);
        
        // Restoranın ortalama puanını güncelle
        if (review.getRestaurantId() != null) {
            Optional<Restaurant> restaurantOpt = restaurantRepository.findById(review.getRestaurantId());
            if (restaurantOpt.isPresent()) {
                Restaurant restaurant = restaurantOpt.get();
                
                // Bu restoranın tüm değerlendirmelerini al
                List<Review> allReviews = reviewRepository.findByRestaurantId(review.getRestaurantId());
                
                // Değerlendirmeleri manuel olarak hesapla
                double totalRating = 0;
                for (Review r : allReviews) {
                    totalRating += r.getRating();
                }
                double newAverageRating = totalRating / allReviews.size();
                
                // Restoranın puanını güncelle
                restaurant.setRating(newAverageRating);
                restaurantRepository.save(restaurant);
            }
        }
        
        return savedReview;
    }
    
    /**
     * Yeni bir değerlendirme ekler
     * 
     * @param review Eklenecek değerlendirme
     * @return Eklenen değerlendirme
     */
    @Transactional
    public Review addReview(Review review) {
        // Mevcut metot ismi değişti
        return saveReview(review);
    }
    
    /**
     * Bir değerlendirmeyi günceller
     * 
     * @param id Güncellenecek değerlendirmenin ID'si
     * @param reviewDetails Yeni değerlendirme bilgileri
     * @return Güncellenen değerlendirme, null ise bulunamadı
     */
    @Transactional
    public Review updateReview(Long id, Review reviewDetails) {
        Optional<Review> optionalReview = reviewRepository.findById(id);
        if (optionalReview.isEmpty()) {
            return null;
        }
        
        Review review = optionalReview.get();
        
        // Değerlendirmeyi güncelle
        if (reviewDetails.getRating() >= 1 && reviewDetails.getRating() <= 5) {
            review.setRating(reviewDetails.getRating());
        }
        
        if (reviewDetails.getComment() != null) {
            review.setComment(reviewDetails.getComment());
        }
        
        // Tarihi güncelle
        review.setDate(LocalDateTime.now());
        
        // Güncellenmiş değerlendirmeyi kaydet
        Review savedReview = reviewRepository.save(review);
        
        // Restoran puanını güncelle
        updateRestaurantRating(review.getRestaurantName());
        
        return savedReview;
    }

    /**
     * Bir değerlendirmeyi siler
     * 
     * @param id Silinecek değerlendirmenin ID'si
     */
    @Transactional
    public void deleteReview(Long id) {
        reviewRepository.findById(id).ifPresent(review -> {
            String restaurantName = review.getRestaurantName();
            reviewRepository.delete(review);
            
            // Restoranın ortalama puanını güncelle
            updateRestaurantRating(restaurantName);
        });
    }

    /**
     * Restoran adına göre tüm değerlendirmeleri siler
     * 
     * @param restaurantName Değerlendirmeleri silinecek restoran adı
     */
    @Transactional
    public void deleteReviewsByRestaurantName(String restaurantName) {
        List<Review> reviews = reviewRepository.findByRestaurantName(restaurantName);
        if (!reviews.isEmpty()) {
            reviews.forEach(reviewRepository::delete);
        }
    }

    /**
     * Bir restoranın ortalama puanını hesaplar ve günceller
     * 
     * @param restaurantName Restoran adı
     */
    @Transactional
    public void updateRestaurantRating(String restaurantName) {
        System.out.println("Restoran puanı güncelleniyor: " + restaurantName);
        
        List<Review> reviews = reviewRepository.findByRestaurantName(restaurantName);
        System.out.println("Bulunan değerlendirme sayısı: " + reviews.size());
        
        if (!reviews.isEmpty()) {
            double totalRating = 0.0;
            for (Review review : reviews) {
                totalRating += review.getRating();
            }
            double averageRating = totalRating / reviews.size();
            
            System.out.println("Hesaplanan ortalama puan: " + averageRating);
            
            restaurantRepository.findByName(restaurantName).ifPresent(restaurant -> {
                restaurant.setRating(averageRating);
                restaurantRepository.save(restaurant);
                System.out.println("Restoran puanı başarıyla güncellendi: " + restaurantName + " - Yeni puan: " + averageRating);
            });
        } else {
            restaurantRepository.findByName(restaurantName).ifPresent(restaurant -> {
                restaurant.setRating(0.0);
                restaurantRepository.save(restaurant);
                System.out.println("Değerlendirme bulunamadı, restoran puanı sıfırlandı: " + restaurantName);
            });
        }
    }

    /**
     * Belirli bir puanın üzerindeki değerlendirmeleri bulur
     * 
     * @param rating Minimum puan
     * @return Puanı belirtilen değerin üzerinde olan değerlendirmelerin listesi
     */
    public List<Review> getReviewsByMinimumRating(int rating) {
        return reviewRepository.findByRatingGreaterThanEqual(rating);
    }

    /**
     * Toplam değerlendirme sayısını getirir
     * 
     * @return Toplam değerlendirme sayısı
     */
    public long countReviews() {
        return reviewRepository.count();
    }
    
    /**
     * Tüm değerlendirmelerin ortalama puanını getirir
     * 
     * @return Ortalama puan
     */
    public double getAverageRating() {
        List<Review> allReviews = reviewRepository.findAll();
        if (allReviews.isEmpty()) {
            return 0.0;
        }
        
        double totalRating = 0.0;
        for (Review review : allReviews) {
            totalRating += review.getRating();
        }
        
        return totalRating / allReviews.size();
    }

    /**
     * Toplam değerlendirme sayısını getirir
     * 
     * @return Toplam değerlendirme sayısı
     */
    public long getReviewCount() {
        return countReviews();
    }

    /**
     * Belirli bir restoran için değerlendirme sayısını getirir
     * 
     * @param restaurantId Restoran ID'si
     * @return Restoran için değerlendirme sayısı
     */
    public long getReviewCountForRestaurant(Long restaurantId) {
        Optional<Restaurant> restaurantOpt = restaurantRepository.findById(restaurantId);
        
        if (restaurantOpt.isEmpty()) {
            return 0;
        }
        
        String restaurantName = restaurantOpt.get().getName();
        List<Review> reviews = reviewRepository.findByRestaurantName(restaurantName);
        
        return reviews.size();
    }
} 