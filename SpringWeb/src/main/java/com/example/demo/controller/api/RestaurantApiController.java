package com.example.demo.controller.api;

import com.example.demo.model.Restaurant;
import com.example.demo.service.RestaurantService;
import com.example.demo.util.AppLogger;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Restoranlar için REST API kontrolcüsü
 * Sadece okuma işlemlerini destekler (GET)
 */
@RestController
@RequestMapping("/api/restaurants")
public class RestaurantApiController {

    private static final Logger LOGGER = AppLogger.getLogger(RestaurantApiController.class);
    private final RestaurantService restaurantService;

    @Autowired
    public RestaurantApiController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
        LOGGER.info("RestaurantApiController başlatıldı");
    }

    /**
     * Tüm restoranları getirir
     * 
     * @return Restoranların listesi
     */
    @GetMapping
    public ResponseEntity<List<Restaurant>> getAllRestaurants() {
        LOGGER.info("API: Tüm restoranlar getiriliyor");
        List<Restaurant> restaurants = restaurantService.getAllRestaurants();
        return ResponseEntity.ok(restaurants);
    }

    /**
     * Eşzamanlı olarak tüm restoranları getirir
     * 
     * @return Restoranların listesi
     */
    @GetMapping("/async")
    public ResponseEntity<List<Restaurant>> getAllRestaurantsAsync() {
        LOGGER.info("API: Tüm restoranlar asenkron olarak getiriliyor");
        try {
            CompletableFuture<List<Restaurant>> future = restaurantService.getRestaurantsAsync();
            List<Restaurant> restaurants = future.get();
            return ResponseEntity.ok(restaurants);
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Asenkron restoran getirme hatası: {}", e.getMessage(), e);
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * ID'ye göre restoran getirir
     * 
     * @param id Restoran ID'si
     * @return Bulunan restoran veya 404 hatası
     */
    @GetMapping("/{id}")
    public ResponseEntity<Restaurant> getRestaurantById(@PathVariable Long id) {
        LOGGER.info("API: ID'ye göre restoran getiriliyor: {}", id);
        return restaurantService.getRestaurantById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * İsme göre restoran arar
     * 
     * @param name Restoran adı
     * @return Bulunan restoran veya 404 hatası
     */
    @GetMapping("/search/name/{name}")
    public ResponseEntity<Restaurant> getRestaurantByName(@PathVariable String name) {
        LOGGER.info("API: İsme göre restoran aranıyor: {}", name);
        return restaurantService.getRestaurantByName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Minimum puana göre restoranları filtreler
     * 
     * @param rating Minimum puan
     * @return Filtrelenmiş restoranların listesi
     */
    @GetMapping("/search/rating/{rating}")
    public ResponseEntity<List<Restaurant>> getRestaurantsByMinimumRating(@PathVariable double rating) {
        LOGGER.info("API: Minimum puana göre restoranlar getiriliyor: {}", rating);
        List<Restaurant> restaurants = restaurantService.getRestaurantsByMinimumRating(rating);
        return ResponseEntity.ok(restaurants);
    }

    /**
     * Konuma göre restoranları filtreler
     * 
     * @param location Konum
     * @return Filtrelenmiş restoranların listesi
     */
    @GetMapping("/search/location/{location}")
    public ResponseEntity<List<Restaurant>> getRestaurantsByLocation(@PathVariable String location) {
        LOGGER.info("API: Konuma göre restoranlar getiriliyor: {}", location);
        List<Restaurant> restaurants = restaurantService.getRestaurantsByLocation(location);
        return ResponseEntity.ok(restaurants);
    }

    /**
     * Restoran istatistiklerini getirir
     * 
     * @return İstatistik bilgileri
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getRestaurantStats() {
        LOGGER.info("API: Restoran istatistikleri getiriliyor");
        long count = restaurantService.count();
        List<Restaurant> topRated = restaurantService.getAllRestaurantsByRating();
        double avgRating = topRated.stream()
                .mapToDouble(Restaurant::getRating)
                .average()
                .orElse(0.0);
        
        Map<String, Object> stats = Map.of(
            "totalRestaurants", count,
            "averageRating", avgRating,
            "topRatedRestaurant", topRated.isEmpty() ? null : topRated.get(0)
        );
        
        return ResponseEntity.ok(stats);
    }
} 