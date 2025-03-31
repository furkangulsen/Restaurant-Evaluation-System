package com.example.demo.controller;

import com.example.demo.model.Restaurant;
import com.example.demo.model.Review;
import com.example.demo.service.RestaurantService;
import com.example.demo.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final RestaurantService restaurantService;

    @Autowired
    public ReviewController(ReviewService reviewService, RestaurantService restaurantService) {
        this.reviewService = reviewService;
        this.restaurantService = restaurantService;
    }

    /**
     * Tüm değerlendirmeleri listeler
     */
    @GetMapping
    public String listAllReviews(Model model) {
        List<Review> reviews = reviewService.getAllReviewsByDate();
        model.addAttribute("reviews", reviews);
        return "reviews/list";
    }

    /**
     * Değerlendirme detaylarını gösterir
     */
    @GetMapping("/{id}")
    public String showReviewDetails(@PathVariable("id") Long id, Model model) {
        return reviewService.getReviewById(id)
                .map(review -> {
                    model.addAttribute("review", review);
                    return "reviews/details";
                })
                .orElse("redirect:/reviews");
    }

    /**
     * Yeni değerlendirme ekleme formunu gösterir
     */
    @GetMapping("/new")
    public String showNewReviewForm(Model model, @RequestParam(required = false, name = "restaurantId") Long restaurantId) {
        Review review = new Review();
        review.setDate(LocalDateTime.now()); // Varsayılan tarih ayarla
        review.setRating(3); // Varsayılan puan olarak 3 atıyoruz (orta)
        
        if (restaurantId != null) {
            Optional<Restaurant> restaurant = restaurantService.getRestaurantById(restaurantId);
            restaurant.ifPresent(r -> {
                review.setRestaurant(r);
                review.setRestaurantName(r.getName());
            });
        }
        
        List<Restaurant> restaurants = restaurantService.getAllRestaurants();
        if (restaurants.isEmpty()) {
            // Eğer hiç restoran yoksa, kullanıcıyı bilgilendir
            model.addAttribute("errorMessage", "Önce restoran eklemelisiniz!");
            return "redirect:/reviews";
        }
        
        model.addAttribute("review", review);
        model.addAttribute("restaurants", restaurants);
        return "reviews/form";
    }

    /**
     * Yeni değerlendirme ekler
     */
    @PostMapping
    public String addReview(@ModelAttribute Review review, RedirectAttributes redirectAttributes) {
        try {
            // Tarih belirtilmemişse şu anki zamanı kullan
            if (review.getDate() == null) {
                review.setDate(LocalDateTime.now());
            }
            
            // Restoran referansını ayarla
            if (review.getRestaurant() == null && review.getRestaurantName() != null) {
                Optional<Restaurant> restaurant = restaurantService.getRestaurantByName(review.getRestaurantName());
                restaurant.ifPresent(review::setRestaurant);
            }
            
            reviewService.saveReview(review);
            redirectAttributes.addFlashAttribute("successMessage", "Değerlendirme başarıyla eklendi.");
            
            // Eğer restoran detay sayfasından geldiyse, o sayfaya geri dön
            if (review.getRestaurant() != null) {
                return "redirect:/restaurants/" + review.getRestaurant().getId();
            }
            
            return "redirect:/reviews";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Değerlendirme eklenirken hata oluştu: " + e.getMessage());
            return "redirect:/reviews/new";
        }
    }

    /**
     * Değerlendirme düzenleme formunu gösterir
     */
    @GetMapping("/{id}/edit")
    public String showEditReviewForm(@PathVariable("id") Long id, Model model) {
        return reviewService.getReviewById(id)
                .map(review -> {
                    model.addAttribute("review", review);
                    model.addAttribute("restaurants", restaurantService.getAllRestaurants());
                    return "reviews/form";
                })
                .orElse("redirect:/reviews");
    }

    /**
     * Değerlendirmeyi günceller
     */
    @PostMapping("/{id}")
    public String updateReview(@PathVariable("id") Long id, @ModelAttribute Review review, 
                              RedirectAttributes redirectAttributes) {
        try {
            reviewService.updateReview(id, review);
            redirectAttributes.addFlashAttribute("successMessage", "Değerlendirme başarıyla güncellendi.");
            
            // Eğer restoran detay sayfasından geldiyse, o sayfaya geri dön
            if (review.getRestaurant() != null) {
                return "redirect:/restaurants/" + review.getRestaurant().getId();
            }
            
            return "redirect:/reviews";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Değerlendirme güncellenirken hata oluştu: " + e.getMessage());
            return "redirect:/reviews/" + id + "/edit";
        }
    }

    /**
     * Değerlendirmeyi siler
     */
    @GetMapping("/{id}/delete")
    public String deleteReview(@PathVariable("id") Long id, @RequestParam(required = false, name = "restaurantId") Long restaurantId, 
                              RedirectAttributes redirectAttributes) {
        try {
            reviewService.deleteReview(id);
            redirectAttributes.addFlashAttribute("successMessage", "Değerlendirme başarıyla silindi.");
            
            // Eğer restoran detay sayfasından geldiyse, o sayfaya geri dön
            if (restaurantId != null) {
                return "redirect:/restaurants/" + restaurantId;
            }
            
            return "redirect:/reviews";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Değerlendirme silinirken hata oluştu: " + e.getMessage());
            return "redirect:/reviews";
        }
    }
} 