package com.example.demo.repository;

import com.example.demo.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    
    /**
     * Restoran adına göre arama yapar
     * 
     * @param name Restoran adı
     * @return Bulunan restoran, yoksa Optional.empty()
     */
    Optional<Restaurant> findByName(String name);
    
    /**
     * Konuma göre restoranları bulur
     * 
     * @param location Konum
     * @return Konumdaki restoranların listesi
     */
    List<Restaurant> findByLocation(String location);
    
    /**
     * Belirli bir puanın üzerindeki restoranları bulur
     * 
     * @param rating Minimum puan
     * @return Puanı belirtilen değerin üzerinde olan restoranların listesi
     */
    List<Restaurant> findByRatingGreaterThanEqual(double rating);
    
    /**
     * Restoranları puana göre azalan sırada getirir
     * 
     * @return Puana göre sıralanmış restoranların listesi
     */
    List<Restaurant> findAllByOrderByRatingDesc();
    
    /**
     * Konuma ve minimum puana göre restoranları bulur
     * 
     * @param location Konum
     * @param rating Minimum puan
     * @return Belirtilen konumdaki ve minimum puanın üzerindeki restoranların listesi
     */
    List<Restaurant> findByLocationAndRatingGreaterThanEqual(String location, double rating);
} 