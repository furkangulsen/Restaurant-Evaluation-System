package com.example.demo.cli.shell;

import com.example.demo.model.Restaurant;
import com.example.demo.service.RestaurantService;
import com.example.demo.util.AppLogger;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.shell.table.ArrayTableModel;
import org.springframework.shell.table.BorderStyle;
import org.springframework.shell.table.TableBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Restoran işlemleri için komut satırı komutları.
 * Spring Shell kullanarak CRUD işlemlerini sağlar.
 * Not: Sadece CLI profilinde aktiftir.
 */
@ShellComponent
@Profile("cli")
public class RestaurantCommands {

    private static final Logger LOGGER = AppLogger.getLogger(RestaurantCommands.class);
    private final RestaurantService restaurantService;
    private final ShellHelper shellHelper;

    @Autowired
    public RestaurantCommands(RestaurantService restaurantService, ShellHelper shellHelper) {
        this.restaurantService = restaurantService;
        this.shellHelper = shellHelper;
        LOGGER.info("Restoran komutları başlatıldı");
    }

    /**
     * Tüm restoranları listeler
     */
    @ShellMethod(key = "list-restaurants", value = "Tüm restoranları listeler")
    public String listRestaurants() {
        List<Restaurant> restaurants = restaurantService.getAllRestaurants();
        
        if (restaurants.isEmpty()) {
            return shellHelper.getWarningMessage("Henüz hiç restoran kaydedilmemiş.");
        }
        
        // Tablo başlıkları
        String[] headers = {"ID", "Restoran Adı", "Konum", "Ortalama Puan"};
        
        // Tablo verilerini hazırla
        List<String> rows = new ArrayList<>();
        
        // Başlık satırını ekle
        StringBuilder headerRow = new StringBuilder();
        headerRow.append("|");
        for (String header : headers) {
            headerRow.append(" ").append(header).append(" |");
        }
        rows.add(headerRow.toString());
        
        // Restoran verilerini ekle
        for (Restaurant restaurant : restaurants) {
            String id = restaurant.getId().toString();
            String name = restaurant.getName();
            String location = restaurant.getLocation();
            String averageRating = restaurant.getAverageRating() > 0 
                    ? String.format("%.1f", restaurant.getAverageRating()) 
                    : "Değerlendirme yok";
            
            StringBuilder row = new StringBuilder();
            row.append("|");
            row.append(" ").append(id).append(" |");
            row.append(" ").append(name).append(" |");
            row.append(" ").append(location).append(" |");
            row.append(" ").append(averageRating).append(" |");
            
            rows.add(row.toString());
        }
        
        // Tüm çıktıyı birleştir
        StringBuilder result = new StringBuilder();
        result.append(shellHelper.getInfoMessage("Restoranlar:")).append("\n");
        
        // Tablonun tüm satırlarını ekle
        for (String row : rows) {
            result.append(row).append("\n");
        }
        
        return result.toString();
    }
    
    /**
     * Yeni bir restoran ekler
     */
    @ShellMethod(key = "add-restaurant", value = "Yeni bir restoran ekler")
    public String addRestaurant(
            @ShellOption(help = "Restoran adı") String name,
            @ShellOption(help = "Restoran konumu") String location) {
        
        if (name.trim().isEmpty()) {
            return shellHelper.getErrorMessage("Restoran adı boş olamaz!");
        }
        
        if (location.trim().isEmpty()) {
            return shellHelper.getErrorMessage("Konum boş olamaz!");
        }
        
        Restaurant restaurant = new Restaurant();
        restaurant.setName(name);
        restaurant.setLocation(location);
        
        try {
            restaurantService.saveRestaurant(restaurant);
            LOGGER.info("Yeni restoran eklendi: {}", restaurant.getName());
            return shellHelper.getSuccessMessage("Restoran başarıyla eklendi! ID: " + restaurant.getId());
        } catch (Exception e) {
            LOGGER.error("Restoran eklenirken hata oluştu", e);
            return shellHelper.getErrorMessage("Restoran eklenirken bir hata oluştu: " + e.getMessage());
        }
    }
    
    /**
     * Var olan bir restoranı günceller
     */
    @ShellMethod(key = "update-restaurant", value = "Var olan bir restoranı günceller")
    public String updateRestaurant(
            @ShellOption(help = "Restoran ID'si") Long id,
            @ShellOption(help = "Yeni restoran adı (değişmeyecekse boş bırakabilirsiniz)", defaultValue = "") String name,
            @ShellOption(help = "Yeni konum (değişmeyecekse boş bırakabilirsiniz)", defaultValue = "") String location) {
        
        Optional<Restaurant> restaurantOpt = restaurantService.getRestaurantById(id);
        
        if (restaurantOpt.isEmpty()) {
            return shellHelper.getErrorMessage("ID: " + id + " ile bir restoran bulunamadı!");
        }
        
        Restaurant restaurant = restaurantOpt.get();
        boolean updated = false;
        
        if (!name.trim().isEmpty()) {
            restaurant.setName(name);
            updated = true;
        }
        
        if (!location.trim().isEmpty()) {
            restaurant.setLocation(location);
            updated = true;
        }
        
        if (!updated) {
            return shellHelper.getWarningMessage("Güncelleme yapılmadı çünkü değişiklik belirtilmedi.");
        }
        
        try {
            restaurantService.saveRestaurant(restaurant);
            LOGGER.info("Restoran güncellendi: {}", restaurant.getName());
            return shellHelper.getSuccessMessage("Restoran başarıyla güncellendi! ID: " + restaurant.getId());
        } catch (Exception e) {
            LOGGER.error("Restoran güncellenirken hata oluştu", e);
            return shellHelper.getErrorMessage("Restoran güncellenirken bir hata oluştu: " + e.getMessage());
        }
    }
    
    /**
     * Bir restoranı siler
     */
    @ShellMethod(key = "delete-restaurant", value = "Bir restoranı siler")
    public String deleteRestaurant(@ShellOption(help = "Silinecek restoran ID'si") Long id) {
        Optional<Restaurant> restaurantOpt = restaurantService.getRestaurantById(id);
        
        if (restaurantOpt.isEmpty()) {
            return shellHelper.getErrorMessage("ID: " + id + " ile bir restoran bulunamadı!");
        }
        
        Restaurant restaurant = restaurantOpt.get();
        
        try {
            restaurantService.deleteRestaurant(id);
            LOGGER.info("Restoran silindi: {}", restaurant.getName());
            return shellHelper.getSuccessMessage("\"" + restaurant.getName() + "\" restoranı başarıyla silindi!");
        } catch (Exception e) {
            LOGGER.error("Restoran silinirken hata oluştu", e);
            return shellHelper.getErrorMessage("Restoran silinirken bir hata oluştu: " + e.getMessage());
        }
    }
    
    /**
     * Bir restoranın detaylarını gösterir
     */
    @ShellMethod(key = "show-restaurant", value = "Bir restoranın detaylarını gösterir")
    public String showRestaurantDetails(@ShellOption(help = "Restoran ID'si") Long id) {
        Optional<Restaurant> restaurantOpt = restaurantService.getRestaurantById(id);
        
        if (restaurantOpt.isEmpty()) {
            return shellHelper.getErrorMessage("ID: " + id + " ile bir restoran bulunamadı!");
        }
        
        Restaurant restaurant = restaurantOpt.get();
        
        StringBuilder sb = new StringBuilder();
        sb.append(shellHelper.getInfoMessage("Restoran Detayları:"))
          .append("\n")
          .append(shellHelper.getSuccessMessage("ID: "))
          .append(restaurant.getId())
          .append("\n")
          .append(shellHelper.getSuccessMessage("Ad: "))
          .append(restaurant.getName())
          .append("\n")
          .append(shellHelper.getSuccessMessage("Konum: "))
          .append(restaurant.getLocation())
          .append("\n");
        
        if (restaurant.getAverageRating() > 0) {
            sb.append(shellHelper.getSuccessMessage("Ortalama Puan: "))
              .append(String.format("%.1f", restaurant.getAverageRating()))
              .append(" / 5.0");
        } else {
            sb.append(shellHelper.getWarningMessage("Henüz değerlendirme yapılmamış"));
        }
        
        return sb.toString();
    }
}
