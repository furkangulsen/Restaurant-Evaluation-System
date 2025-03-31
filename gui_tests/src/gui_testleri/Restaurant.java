package gui_testleri;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Restoran veri modelini temsil eden sınıf.
 * Bu sınıf, bir restoranın temel özelliklerini (ad, konum, puan)
 * ve bu özelliklerin işlenmesi için gerekli metodları içerir.
 */
@Entity
@Table(name = "restaurants", schema = "public")
@XmlRootElement
public class Restaurant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "name")
    private String name;        // Restoran adı
    
    @Column(name = "location")
    private String location;    // Restoran konumu
    
    @Column(name = "rating")
    private double rating;      // Restoran puanı (1-5 arası)
    
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    /**
     * Hibernate için boş yapıcı metod
     */
    public Restaurant() {
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
        this.rating = rating;
    }

    // Getter ve Setter metodları
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    @XmlElement
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @XmlElement
    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }
    
    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    /**
     * Restoranı metin formatına dönüştürür.
     * Bu format dosyaya kaydetmek için kullanılır.
     * Format: "ad,konum,puan"
     */
    @Override
    public String toString() {
        return name + "," + location + "," + rating;
    }

    /**
     * Restoranı görüntüleme formatına dönüştürür.
     * Bu format JList'te gösterim için kullanılır.
     * Format: "ad (konum) - puan*"
     */
    public String toDisplayString() {
        return name + " (" + location + ") - " + rating + "*";
    }

    /**
     * Metin formatındaki restoran bilgisini Restaurant nesnesine dönüştürür.
     * 
     * @param line "ad,konum,puan" formatında metin
     * @return Restaurant nesnesi, hata durumunda null
     */
    public static Restaurant fromString(String line) {
        try {
            String[] parts = line.split(",");
            return new Restaurant(parts[0], parts[1], Double.parseDouble(parts[2]));
        } catch (Exception e) {
            return null;
        }
    }
} 