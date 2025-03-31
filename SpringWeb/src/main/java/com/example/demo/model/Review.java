package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Restoran değerlendirmelerini temsil eden sınıf.
 * Bu sınıf, bir değerlendirmenin temel özelliklerini (restoran adı, yorum, puan)
 * ve bu özelliklerin işlenmesi için gerekli metodları içerir.
 */
@Entity
@Table(name = "reviews")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE})
    @JoinColumn(name = "restaurant_id", nullable = true, insertable = false, updatable = false)
    private Restaurant restaurant;
    
    @Column(name = "restaurant_id")
    private Long restaurantId;
    
    @Column(name = "restaurant_name")
    private String restaurantName;  // Değerlendirilen restoranın adı
    
    @Column(name = "comment", length = 1000)
    private String comment;         // Değerlendirme yorumu
    
    @Column(name = "rating", nullable = false)
    private Integer rating = 0;          // Değerlendirme puanı (1-5 arası)
    
    @Column(name = "date")
    private LocalDateTime date;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Hibernate için boş yapıcı metod
     */
    public Review() {
    }

    /**
     * Review sınıfının yapıcı metodu.
     * 
     * @param restaurantName Değerlendirilen restoranın adı
     * @param comment Değerlendirme yorumu
     * @param rating Değerlendirme puanı (1-5 arası)
     */
    public Review(String restaurantName, int rating, String comment) {
        this.restaurantName = restaurantName.trim();
        setRating(rating);
        this.comment = comment;
        this.date = LocalDateTime.now();
    }

    public Review(String restaurantName, int rating, String comment, LocalDateTime date) {
        this.restaurantName = restaurantName.trim();
        setRating(rating);
        this.comment = comment;
        this.date = date;
    }

    public Review(Restaurant restaurant, int rating, String comment) {
        this.restaurant = restaurant;
        this.restaurantName = restaurant.getName().trim();
        setRating(rating);
        this.comment = comment;
        this.date = LocalDateTime.now();
    }

    public void setRating(Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Puan 1 ile 5 arasında olmalıdır");
        }
        this.rating = rating;
    }

    // Getter ve Setter metodları
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
        if (restaurant != null) {
            this.restaurantName = restaurant.getName();
        }
    }
    
    public String getRestaurantName() {
        return restaurantName;
    }
    
    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public String getComment() {
        return comment;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }

    public Integer getRating() {
        return rating;
    }

    public LocalDateTime getDate() {
        return date;
    }
    
    public void setDate(LocalDateTime date) {
        this.date = date;
    }
    
    /**
     * Değerlendirmenin oluşturulma tarihini verir
     * Bu metot, date alanını döndürerek getCreatedAt() metoduyla uyumluluk sağlar
     * 
     * @return Değerlendirmenin oluşturulma tarihi
     */
    public LocalDateTime getCreatedAt() {
        return this.date;
    }
    
    /**
     * Değerlendirmenin oluşturulma tarihini ayarlar
     * Bu metot, date alanını güncelleyerek setCreatedAt() metoduyla uyumluluk sağlar
     * 
     * @param createdAt Değerlendirmenin oluşturulma tarihi
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.date = createdAt;
    }
    
    /**
     * Değerlendirmeyi görüntüleme formatına dönüştürür.
     * Format: "⭐ puan | tarih | yorum"
     */
    public String getFormattedDate() {
        if (date == null) return "";
        return date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    }

    /**
     * Değerlendirmenin son güncellenme tarihini getirir
     * 
     * @return Son güncellenme tarihi
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Değerlendirmenin son güncellenme tarihini ayarlar
     * 
     * @param updatedAt Son güncellenme tarihi
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Değerlendirmeyi metin formatına dönüştürür.
     */
    @Override
    public String toString() {
        return "Review{" +
                "id=" + id +
                ", restaurantName='" + restaurantName + '\'' +
                ", comment='" + comment + '\'' +
                ", rating=" + rating +
                ", date=" + date +
                '}';
    }

    public Long getRestaurantId() {
        return restaurantId;
    }
    
    public void setRestaurantId(Long restaurantId) {
        this.restaurantId = restaurantId;
    }
} 