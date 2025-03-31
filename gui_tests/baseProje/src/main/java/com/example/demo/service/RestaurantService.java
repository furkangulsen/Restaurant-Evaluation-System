package com.example.demo.service;

import com.example.demo.model.Restaurant;
import com.example.demo.model.Review;
import com.example.demo.repository.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class RestaurantService {
    
    @Autowired
    private RestaurantRepository restaurantRepository;
    
    public List<Restaurant> getAllRestaurants() {
        return restaurantRepository.findAll();
    }
    
    public Optional<Restaurant> getRestaurantById(Long id) {
        return restaurantRepository.findById(id);
    }
    
    @Transactional
    public Restaurant saveRestaurant(Restaurant restaurant) {
        return restaurantRepository.save(restaurant);
    }
    
    @Transactional
    public void deleteRestaurant(Long id) {
        restaurantRepository.deleteById(id);
    }
    
    @Transactional
    public void addReview(Long restaurantId, Review review) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow(() -> new RuntimeException("Restaurant not found"));
        review.setRestaurant(restaurant);
        restaurant.getReviews().add(review);
        updateRestaurantRating(restaurant);
        restaurantRepository.save(restaurant);
    }
    
    private void updateRestaurantRating(Restaurant restaurant) {
        double averageRating = restaurant.getReviews().stream()
            .mapToInt(Review::getRating)
            .average()
            .orElse(0.0);
        restaurant.setRating(averageRating);
    }
} 