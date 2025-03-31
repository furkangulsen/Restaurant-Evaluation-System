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

import java.util.List;

@Controller
@RequestMapping("/restaurants")
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final ReviewService reviewService;

    @Autowired
    public RestaurantController(RestaurantService restaurantService, ReviewService reviewService) {
        this.restaurantService = restaurantService;
        this.reviewService = reviewService;
    }

    /**
     * Tüm restoranları listeler ve filtreleme yapar
     */
    @GetMapping
    public String listAllRestaurants(
            @RequestParam(required = false, name = "location") String location,
            @RequestParam(required = false, name = "minRating") String minRating,
            Model model) {
        
        List<Restaurant> restaurants;
        
        // Filtreleme işlemi
        if (location != null && !location.isEmpty() && minRating != null && !minRating.isEmpty()) {
            // Hem konum hem de minimum puan filtresi var
            double rating = Double.parseDouble(minRating);
            restaurants = restaurantService.getRestaurantsByLocationAndMinimumRating(location, rating);
        } else if (location != null && !location.isEmpty()) {
            // Sadece konum filtresi var
            restaurants = restaurantService.getRestaurantsByLocation(location);
        } else if (minRating != null && !minRating.isEmpty()) {
            // Sadece minimum puan filtresi var
            double rating = Double.parseDouble(minRating);
            restaurants = restaurantService.getRestaurantsByMinimumRating(rating);
        } else {
            // Filtre yok, tüm restoranları getir
            restaurants = restaurantService.getAllRestaurantsByRating();
        }
        
        // Her restoranın değerlendirme sayısını hesapla ve ekle
        for (Restaurant restaurant : restaurants) {
            // Değerlendirme sayısını kontrol et ve ekle
            if (restaurant.getReviews() == null || restaurant.getReviews().isEmpty()) {
                List<Review> reviews = reviewService.getReviewsByRestaurantName(restaurant.getName());
                // Doğru puan için ortalamayı kontrol et
                if (!reviews.isEmpty()) {
                    double totalRating = 0;
                    for (Review review : reviews) {
                        totalRating += review.getRating();
                    }
                    double averageRating = totalRating / reviews.size();
                    
                    // Eğer hesaplanan ortalama, kaydedilen ortalamadan farklıysa güncelle
                    if (Math.abs(averageRating - restaurant.getRating()) > 0.01) {
                        restaurant.setRating(averageRating);
                        restaurantService.saveRestaurant(restaurant);
                        System.out.println("Restoran puanı güncellendi: " + restaurant.getName() + 
                                 " - Yeni puan: " + averageRating + 
                                 " - Değerlendirme sayısı: " + reviews.size());
                    }
                }
            }
        }
        
        model.addAttribute("restaurants", restaurants);
        return "restaurants/list";
    }

    /**
     * Restoran detaylarını gösterir
     */
    @GetMapping("/{id}")
    public String showRestaurantDetails(@PathVariable("id") Long id, Model model) {
        return restaurantService.getRestaurantById(id)
                .map(restaurant -> {
                    // Restoranı modele ekle
                    model.addAttribute("restaurant", restaurant);
                    
                    // Değerlendirmeleri al
                    List<Review> reviews = reviewService.getReviewsByRestaurantName(restaurant.getName());
                    model.addAttribute("reviews", reviews);
                    
                    // Değerlendirme varsa puanı yeniden hesapla ve güncelle
                    if (!reviews.isEmpty()) {
                        double totalRating = 0;
                        for (Review review : reviews) {
                            totalRating += review.getRating();
                        }
                        double averageRating = totalRating / reviews.size();
                        
                        // Eğer hesaplanan ortalama, kaydedilen ortalamadan farklıysa güncelle
                        if (Math.abs(averageRating - restaurant.getRating()) > 0.01) {
                            restaurant.setRating(averageRating);
                            restaurantService.saveRestaurant(restaurant);
                            System.out.println("Restoran puanı detay sayfasında güncellendi: " + restaurant.getName() + 
                                     " - Yeni puan: " + averageRating + 
                                     " - Değerlendirme sayısı: " + reviews.size());
                        }
                    }
                    
                    return "restaurants/details";
                })
                .orElse("redirect:/restaurants");
    }

    /**
     * Yeni restoran ekleme formunu gösterir
     */
    @GetMapping("/new")
    public String showNewRestaurantForm(Model model) {
        model.addAttribute("restaurant", new Restaurant());
        return "restaurants/form";
    }

    /**
     * Yeni restoran ekler
     */
    @PostMapping
    public String addRestaurant(@ModelAttribute Restaurant restaurant, RedirectAttributes redirectAttributes) {
        try {
            restaurantService.saveRestaurant(restaurant);
            redirectAttributes.addFlashAttribute("successMessage", "Restoran başarıyla eklendi.");
            return "redirect:/restaurants";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Restoran eklenirken hata oluştu: " + e.getMessage());
            return "redirect:/restaurants/new";
        }
    }

    /**
     * Restoran düzenleme formunu gösterir
     */
    @GetMapping("/{id}/edit")
    public String showEditRestaurantForm(@PathVariable("id") Long id, Model model) {
        return restaurantService.getRestaurantById(id)
                .map(restaurant -> {
                    model.addAttribute("restaurant", restaurant);
                    return "restaurants/form";
                })
                .orElse("redirect:/restaurants");
    }

    /**
     * Restoranı günceller
     */
    @PostMapping("/{id}")
    public String updateRestaurant(@PathVariable("id") Long id, @ModelAttribute Restaurant restaurant, 
                                  RedirectAttributes redirectAttributes) {
        try {
            restaurantService.updateRestaurant(id, restaurant);
            redirectAttributes.addFlashAttribute("successMessage", "Restoran başarıyla güncellendi.");
            return "redirect:/restaurants";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Restoran güncellenirken hata oluştu: " + e.getMessage());
            return "redirect:/restaurants/" + id + "/edit";
        }
    }

    /**
     * Restoranı siler
     */
    @GetMapping("/{id}/delete")
    public String deleteRestaurant(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            restaurantService.deleteRestaurant(id);
            redirectAttributes.addFlashAttribute("successMessage", "Restoran başarıyla silindi.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Restoran silinirken hata oluştu: " + e.getMessage());
        }
        return "redirect:/restaurants";
    }
} 