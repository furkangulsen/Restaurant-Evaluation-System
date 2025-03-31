package gui_testleri;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import java.util.List;

public class TerminalAppTest {
    private RestaurantManager restaurantManager;
    private ReviewManager reviewManager;

    @Before
    public void setUp() {
        restaurantManager = new RestaurantManager(null);
        reviewManager = new ReviewManager();
    }

    @Test
    public void testAddAndGetRestaurant() {
        // Test restaurant ekleme ve alma işlemi
        Restaurant testRestaurant = new Restaurant("Test Restaurant", "Test Location", 4.5);
        restaurantManager.addRestaurant(testRestaurant);
        
        Restaurant retrieved = restaurantManager.getRestaurant("Test Restaurant");
        assertNotNull("Eklenen restoran null olmamalı", retrieved);
        assertEquals("Restoran adı eşleşmeli", "Test Restaurant", retrieved.getName());
        assertEquals("Restoran konumu eşleşmeli", "Test Location", retrieved.getLocation());
        assertEquals("Restoran puanı eşleşmeli", 4.5, retrieved.getRating(), 0.01);
    }

    @Test
    public void testAddAndGetReviews() {
        // Test restaurant için önce restoranı ekle
        Restaurant testRestaurant = new Restaurant("Review Test Restaurant", "Test Location", 4.0);
        restaurantManager.addRestaurant(testRestaurant);
        
        // Test değerlendirmesi ekle
        Review testReview = new Review("Review Test Restaurant", 5, "Harika bir yer!");
        reviewManager.addReview(testReview);
        
        // Değerlendirmeleri al ve kontrol et
        List<Review> reviews = reviewManager.getReviewsForRestaurant("Review Test Restaurant");
        assertFalse("Değerlendirme listesi boş olmamalı", reviews.isEmpty());
        assertEquals("Değerlendirme puanı eşleşmeli", 5, reviews.get(0).getRating());
        assertEquals("Değerlendirme yorumu eşleşmeli", "Harika bir yer!", reviews.get(0).getComment());
    }
} 