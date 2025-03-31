package com.example.demo.service;

import com.example.demo.model.Restaurant;
import com.example.demo.repository.RestaurantRepository;
import com.example.demo.util.AppLogger;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.HashMap;

@Service
public class RestaurantService implements GenericService<Restaurant, Long> {

    private static final Logger LOGGER = AppLogger.getLogger(RestaurantService.class);
    private final RestaurantRepository restaurantRepository;
    private final ReviewService reviewService;

    @Autowired
    public RestaurantService(RestaurantRepository restaurantRepository, ReviewService reviewService) {
        this.restaurantRepository = restaurantRepository;
        this.reviewService = reviewService;
        LOGGER.info("RestaurantService başlatıldı");
    }

    /**
     * Tüm restoranları getirir
     * 
     * @return Restoranların listesi
     */
    @Override
    public List<Restaurant> findAll() {
        LOGGER.debug("Tüm restoranlar getiriliyor");
        return restaurantRepository.findAll();
    }

    /**
     * Tüm restoranları getirir (eski metot adı korundu)
     */
    public List<Restaurant> getAllRestaurants() {
        return findAll();
    }
    
    /**
     * Restoranları puana göre sıralı getirir
     * 
     * @return Puana göre sıralanmış restoranların listesi
     */
    public List<Restaurant> getAllRestaurantsByRating() {
        LOGGER.debug("Restoranlar puana göre getiriliyor");
        return restaurantRepository.findAllByOrderByRatingDesc();
    }

    /**
     * ID'ye göre restoran bulur
     * 
     * @param id Restoran ID'si
     * @return Bulunan restoran, yoksa Optional.empty()
     */
    @Override
    public Optional<Restaurant> findById(Long id) {
        LOGGER.debug("ID'ye göre restoran aranıyor: {}", id);
        return restaurantRepository.findById(id);
    }

    /**
     * ID'ye göre restoran bulur (eski metot adı korundu)
     */
    public Optional<Restaurant> getRestaurantById(Long id) {
        return findById(id);
    }

    /**
     * İsme göre restoran bulur
     * 
     * @param name Restoran adı
     * @return Bulunan restoran, yoksa Optional.empty()
     */
    public Optional<Restaurant> getRestaurantByName(String name) {
        LOGGER.debug("İsme göre restoran aranıyor: {}", name);
        return restaurantRepository.findByName(name);
    }

    /**
     * Yeni bir restoran ekler veya günceller
     * 
     * @param restaurant Eklenecek/güncellenecek restoran
     * @return Eklenen/güncellenen restoran
     * @throws IllegalArgumentException geçersiz veri durumunda
     */
    @Transactional
    public Restaurant saveRestaurant(Restaurant restaurant) {
        if (restaurant == null) {
            throw new IllegalArgumentException("Restoran bilgisi boş olamaz");
        }

        // Ad ve konum validasyonu
        if (restaurant.getName() == null || restaurant.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Restoran adı boş olamaz");
        }
        if (restaurant.getLocation() == null || restaurant.getLocation().trim().isEmpty()) {
            throw new IllegalArgumentException("Restoran konumu boş olamaz");
        }

        // Türkçe karakterleri temizle ve normalize et
        String originalName = restaurant.getName();
        String originalLocation = restaurant.getLocation();
        
        restaurant.setName(cleanString(restaurant.getName()));
        restaurant.setLocation(cleanString(restaurant.getLocation()));
        
        // Debug logları (Türkçe karakterlerin korunduğunu görme)
        LOGGER.debug("Orijinal restoran adı: {}", originalName);
        LOGGER.debug("Temizlenmiş restoran adı: {}", restaurant.getName());
        LOGGER.debug("Orijinal konum: {}", originalLocation);
        LOGGER.debug("Temizlenmiş konum: {}", restaurant.getLocation());

        // Mevcut restoran kontrolü (güncelleme durumu için)
        boolean isUpdate = restaurant.getId() != null;
        if (!isUpdate) {
            // Yeni ekleme durumunda isim kontrolü - temizlenmiş isimle kontrol et
            try {
                Optional<Restaurant> existingRestaurant = restaurantRepository.findByName(restaurant.getName());
                if (existingRestaurant.isPresent()) {
                    throw new IllegalArgumentException("Bu isimde bir restoran zaten mevcut: " + restaurant.getName());
                }
            } catch (Exception e) {
                LOGGER.warn("Mevcut restoran kontrolünde sorun: {}", e.getMessage());
                // İsim kontrolü yapılamadı, devam et
            }
        }

        try {
            // Kaydedilecek verileri logla
            LOGGER.info("Restoran kaydediliyor: {}", restaurant.getName());
            
            // Kaydet
            Restaurant savedRestaurant = restaurantRepository.save(restaurant);
            LOGGER.info("Restoran başarıyla kaydedildi: {}", savedRestaurant.getId());
            return savedRestaurant;
        } catch (IllegalArgumentException e) {
            LOGGER.error("Restoran validasyon hatası: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            LOGGER.error("Restoran kaydedilirken hata: {}", e.getMessage(), e);
            throw new RuntimeException("Restoran kaydedilirken bir hata oluştu: " + e.getMessage(), e);
        }
    }

    /**
     * Bir restoranı günceller
     * 
     * @param id Güncellenecek restoranın ID'si
     * @param restaurantDetails Yeni restoran bilgileri
     * @return Güncellenen restoran, yoksa Optional.empty()
     */
    @Override
    @Transactional
    public Optional<Restaurant> update(Long id, Restaurant restaurantDetails) {
        LOGGER.info("Restoran güncelleniyor, ID: {}", id);
        return restaurantRepository.findById(id)
                .map(restaurant -> {
                    restaurant.setName(restaurantDetails.getName());
                    restaurant.setLocation(restaurantDetails.getLocation());
                    restaurant.setRating(restaurantDetails.getRating());
                    LOGGER.debug("Restoran güncellendi: {}", restaurant.getName());
                    return restaurantRepository.save(restaurant);
                });
    }

    /**
     * Bir restoranı günceller (eski metot adı korundu)
     */
    @Transactional
    public Optional<Restaurant> updateRestaurant(Long id, Restaurant restaurantDetails) {
        return update(id, restaurantDetails);
    }

    /**
     * Bir restoranı siler
     * 
     * @param id Silinecek restoranın ID'si
     */
    @Override
    @Transactional
    public void delete(Long id) {
        LOGGER.info("Restoran siliniyor, ID: {}", id);
        restaurantRepository.findById(id).ifPresent(restaurant -> {
            // Önce restorana ait tüm değerlendirmeleri sil
            reviewService.deleteReviewsByRestaurantName(restaurant.getName());
            // Sonra restoranı sil
            restaurantRepository.delete(restaurant);
            LOGGER.debug("Restoran silindi: {}", restaurant.getName());
        });
    }

    /**
     * Bir restoranı siler (eski metot adı korundu)
     */
    @Transactional
    public void deleteRestaurant(Long id) {
        delete(id);
    }

    /**
     * Eşzamanlı olarak restoranları getiren bir async metot
     */
    public CompletableFuture<List<Restaurant>> getRestaurantsAsync() {
        LOGGER.debug("Restoranlar async olarak getiriliyor");
        return CompletableFuture.supplyAsync(this::getAllRestaurants);
    }

    /**
     * Belirli bir puanın üzerindeki restoranları bulur
     * 
     * @param rating Minimum puan
     * @return Puanı belirtilen değerin üzerinde olan restoranların listesi
     */
    public List<Restaurant> getRestaurantsByMinimumRating(double rating) {
        LOGGER.debug("Minimum puana göre restoranlar getiriliyor: {}", rating);
        return restaurantRepository.findByRatingGreaterThanEqual(rating);
    }

    /**
     * Konuma göre restoranları bulur
     * 
     * @param location Konum
     * @return Konumdaki restoranların listesi
     */
    public List<Restaurant> getRestaurantsByLocation(String location) {
        LOGGER.debug("Konuma göre restoranlar getiriliyor: {}", location);
        return restaurantRepository.findByLocation(location);
    }

    /**
     * Konuma ve minimum puana göre restoranları bulur
     * 
     * @param location Konum
     * @param rating Minimum puan
     * @return Belirtilen konumdaki ve minimum puanın üzerindeki restoranların listesi
     */
    public List<Restaurant> getRestaurantsByLocationAndMinimumRating(String location, double rating) {
        LOGGER.debug("Konum ve minimum puana göre restoranlar getiriliyor: {}, {}", location, rating);
        return restaurantRepository.findByLocationAndRatingGreaterThanEqual(location, rating);
    }

    /**
     * Bir restoranın varlığını kontrol eder
     * 
     * @param id Kontrol edilecek restoranın ID'si
     * @return Restoran varsa true, yoksa false
     */
    @Override
    public boolean existsById(Long id) {
        return restaurantRepository.existsById(id);
    }

    /**
     * Restoran sayısını getirir
     * 
     * @return Toplam restoran sayısı
     */
    @Override
    public long count() {
        return restaurantRepository.count();
    }

    /**
     * Yeni bir restoran ekler
     * 
     * @param restaurant Eklenecek restoran
     * @return Eklenen restoran
     */
    @Override
    @Transactional
    public Restaurant save(Restaurant restaurant) {
        LOGGER.info("Restoran kaydediliyor: {}", restaurant.getName());
        return restaurantRepository.save(restaurant);
    }

    /**
     * Şehirlere göre restoran sayılarını getirir
     * 
     * @return Şehir adı ve restoran sayısı eşleşmeleri
     */
    public Map<String, Long> getRestaurantCountsByLocation() {
        List<Restaurant> restaurants = restaurantRepository.findAll();
        Map<String, Long> locationCounts = new HashMap<>();
        
        for (Restaurant restaurant : restaurants) {
            String location = restaurant.getLocation();
            if (location != null && !location.isEmpty()) {
                locationCounts.put(location, locationCounts.getOrDefault(location, 0L) + 1);
            }
        }
        
        return locationCounts;
    }

    /**
     * String değerlerini temizler ve güvenli hale getirir.
     * Null byte karakterlerini temizler ve Türkçe karakterleri korur.
     * 
     * @param input Temizlenecek string
     * @return Temizlenmiş string
     */
    private String cleanString(String input) {
        if (input == null) {
            return null;
        }
        
        // Önce log kaydı (giriş)
        LOGGER.debug("Temizleme öncesi: [{}]", input);
        
        try {
            // Null byte karakterlerini temizle
            String cleaned = input.replace("\u0000", "");
            
            // Boşlukları düzenle
            cleaned = cleaned.trim().replaceAll("\\s+", " ");
            
            // Temizleme sonrası log
            LOGGER.debug("Temizleme sonrası: [{}]", cleaned);
            
            return cleaned;
        } catch (Exception e) {
            LOGGER.warn("String temizleme sırasında hata: {}", e.getMessage());
            // Hata durumunda orijinal değeri döndür
            return input.trim();
        }
    }

    /**
     * Toplam restoran sayısını getirir
     * 
     * @return Toplam restoran sayısı
     */
    public long getRestaurantCount() {
        return count();
    }

    /**
     * En yüksek puanlı restoranları getirir
     * 
     * @param limit Getirilecek restoran sayısı
     * @return Puana göre sıralanmış restoranların listesi
     */
    public List<Restaurant> getTopRatedRestaurants(int limit) {
        LOGGER.debug("En yüksek puanlı {} restoran getiriliyor", limit);
        return restaurantRepository.findAllByOrderByRatingDesc().stream()
                .limit(limit)
                .toList();
    }

    /**
     * En çok değerlendirilen restoranları getirir
     * 
     * @param limit Getirilecek restoran sayısı
     * @return Değerlendirme sayısına göre sıralanmış restoranların listesi
     */
    public List<Restaurant> getMostReviewedRestaurants(int limit) {
        LOGGER.debug("En çok değerlendirilen {} restoran getiriliyor", limit);
        return getAllRestaurants().stream()
                .sorted((r1, r2) -> {
                    long r1ReviewCount = reviewService.getReviewCountForRestaurant(r1.getId());
                    long r2ReviewCount = reviewService.getReviewCountForRestaurant(r2.getId());
                    return Long.compare(r2ReviewCount, r1ReviewCount);
                })
                .limit(limit)
                .toList();
    }
} 