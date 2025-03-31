package com.example.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/**
 * Restoran veri modelini temsil eden sınıf.
 * Bu sınıf, bir restoranın temel özelliklerini (ad, konum, puan)
 * ve bu özelliklerin işlenmesi için gerekli metodları içerir.
 */
@Entity
@Table(name = "restaurants", schema = "public")
public class Restaurant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @NotBlank(message = "Restoran adı boş olamaz")
    @Size(min = 2, max = 100, message = "Restoran adı 2-100 karakter arasında olmalıdır")
    @Column(name = "name", unique = true, nullable = false, length = 100)
    private String name;        // Restoran adı
    
    @NotBlank(message = "Restoran konumu boş olamaz")
    @Size(min = 2, max = 100, message = "Restoran konumu 2-100 karakter arasında olmalıdır")
    @Column(name = "location", nullable = false, length = 100)
    private String location;    // Restoran konumu
    
    @Min(value = 0, message = "Puan 0'dan küçük olamaz")
    @Max(value = 5, message = "Puan 5'ten büyük olamaz")
    @Column(name = "rating", columnDefinition = "double precision default 0.0")
    private double rating;      // Restoran puanı (1-5 arası)
    
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    /**
     * Hibernate için boş yapıcı metod
     */
    public Restaurant() {
        this.rating = 0.0;
    }

    /**
     * Restaurant sınıfının yapıcı metodu.
     * 
     * @param name Restoran adı
     * @param location Restoran konumu
     * @param rating Restoran puanı (1-5 arası)
     */
    public Restaurant(String name, String location, double rating) {
        this.name = name;
        this.location = location;
        setRating(rating);
    }

    // Getter ve Setter metodları
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        // Null kontrolü ve boşluk temizleme. Türkçe karakterleri koruyoruz
        this.name = name != null ? name.trim() : null;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        // Null kontrolü ve boşluk temizleme. Türkçe karakterleri koruyoruz
        this.location = location != null ? location.trim() : null;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        // Puanı 0-5 aralığında tut
        this.rating = Math.max(0.0, Math.min(5.0, rating));
    }
    
    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews != null ? reviews : new ArrayList<>();
    }

    /**
     * Restoranın ortalama puanını verir
     * Bu metot, rating alanındaki değeri döndürür
     * 
     * @return Restoranın ortalama puanı
     */
    public double getAverageRating() {
        return this.rating;
    }

    /**
     * Restoranı metin formatına dönüştürür.
     */
    @Override
    public String toString() {
        return "Restaurant{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", location='" + location + '\'' +
                ", rating=" + String.format("%.1f", rating) +
                '}';
    }
} 