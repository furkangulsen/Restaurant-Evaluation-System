package gui_testleri;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Restoran değerlendirmelerini temsil eden sınıf.
 * Bu sınıf, bir değerlendirmenin temel özelliklerini (restoran adı, yorum, puan)
 * ve bu özelliklerin işlenmesi için gerekli metodları içerir.
 */
@Entity
@Table(name = "reviews")
@XmlRootElement
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "restaurant_id", nullable = true)
    private Restaurant restaurant;
    
    @Column(name = "restaurant_name")
    private String restaurantName;  // Değerlendirilen restoranın adı
    
    @Column(name = "comment", length = 1000)
    private String comment;         // Değerlendirme yorumu
    
    @Column(name = "rating")
    private int rating;          // Değerlendirme puanı (1-5 arası)
    
    @Column(name = "date")
    private LocalDateTime date;

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

    private void setRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
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
    
    @XmlElement
    public String getRestaurantName() {
        return restaurantName;
    }
    
    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    @XmlElement
    public String getComment() {
        return comment;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }

    @XmlElement
    public int getRating() {
        return rating;
    }

    @XmlElement
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    public LocalDateTime getDate() {
        return date;
    }
    
    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    /**
     * Değerlendirmeyi metin formatına dönüştürür.
     * Bu format dosyaya kaydetmek için kullanılır.
     * Format: "restoranAdı,yorum,puan"
     */
    @Override
    public String toString() {
        // Puanı tamsayı olarak kaydet, ondalık değil
        return restaurantName + "," + comment + "," + rating + "," + date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    /**
     * Değerlendirmeyi görüntüleme formatına dönüştürür.
     * Bu format JList'te gösterim için kullanılır.
     * Format: "⭐ puan - yorum"
     */
    public String toDisplayString() {
        return String.format("⭐ %d | %s | %s", 
            rating, 
            date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")),
            comment);
    }

    /**
     * Metin formatındaki değerlendirme bilgisini Review nesnesine dönüştürür.
     * 
     * @param line "restoranAdı,yorum,puan" formatında metin
     * @return Review nesnesi, hata durumunda null
     */
    public static Review fromString(String line) {
        String[] parts = line.split(",");
        if (parts.length == 4) {
            try {
                // Restoran adındaki boşlukları temizle
                String restaurantName = parts[0].trim();
                
                // Ondalık sayıları Double.parseDouble ile işle
                double ratingValue = Double.parseDouble(parts[2]);
                int rating = (int) Math.round(ratingValue);
                
                return new Review(
                    restaurantName,
                    rating,
                    parts[1],
                    LocalDateTime.parse(parts[3], DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                );
            } catch (Exception e) {
                System.out.println("Değerlendirme okuma hatası: " + e.getMessage());
                e.printStackTrace(); // Hata detaylarını görmek için
                return null;
            }
        }
        return null;
    }
} 