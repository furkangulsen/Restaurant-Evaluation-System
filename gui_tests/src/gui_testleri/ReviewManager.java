package gui_testleri;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

/**
 * Değerlendirme verilerini yöneten sınıf.
 * Bu sınıf, değerlendirmelerin eklenmesi, silinmesi ve
 * veritabanından okunup veritabanına yazılması işlemlerini gerçekleştirir.
 */
public class ReviewManager {
    // Değerlendirme listesi
    private List<Review> reviews;
    private RestaurantManager restaurantManager;

    /**
     * ReviewManager sınıfının yapıcı metodu.
     * Değerlendirmeleri veritabanından yükler.
     */
    public ReviewManager() {
        this.reviews = new ArrayList<>();
        loadReviews();
    }
    
    /**
     * RestaurantManager referansını ayarlar.
     * Bu, değerlendirmelerin ilgili restoranlarla ilişkilendirilmesi için gereklidir.
     * 
     * @param restaurantManager RestaurantManager nesnesi
     */
    public void setRestaurantManager(RestaurantManager restaurantManager) {
        this.restaurantManager = restaurantManager;
    }

    /**
     * Yeni bir değerlendirme ekler.
     * 
     * @param review Eklenecek değerlendirme nesnesi
     * @return Ekleme işleminin başarılı olup olmadığı
     */
    public boolean addReview(Review review) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            // Önce restaurant_id'yi set et
            if (review.getRestaurant() == null && review.getRestaurantName() != null) {
                Restaurant restaurant = restaurantManager.getRestaurant(review.getRestaurantName());
                if (restaurant != null) {
                    review.setRestaurant(restaurant);
                    review.setRestaurantName(restaurant.getName());
                } else {
                    System.err.println("Restoran bulunamadı: " + review.getRestaurantName());
                    return false;
                }
            }
            
            // Değerlendirmeyi kaydet
            session.save(review);
            transaction.commit();
            
            // Belleğe ekle
            reviews.add(review);
            
            // Restoranın ortalama puanını güncelle
            if (review.getRestaurant() != null) {
                Restaurant restaurant = review.getRestaurant();
                double avgRating = getAverageRating(restaurant.getName());
                restaurant.setRating(avgRating);
                restaurantManager.updateRestaurant(restaurant);
            }
            
            return true;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Bir restorana ait tüm değerlendirmeleri siler.
     * 
     * @param restaurantName Değerlendirmeleri silinecek restoran adı
     */
    public void deleteAllReviewsForRestaurant(String restaurantName) {
        // Restoran adını temizle
        String cleanName = restaurantName.trim();
        
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            // Silinecek değerlendirmeleri bul
            Query<Review> query = session.createQuery(
                "FROM Review WHERE restaurantName = :name", Review.class);
            query.setParameter("name", cleanName);
            List<Review> reviewsToDelete = query.list();
            
            // Değerlendirmeleri sil
            for (Review review : reviewsToDelete) {
                session.delete(review);
            }
            
            transaction.commit();
            
            // Belleği güncelle
            reviews.removeIf(review -> review.getRestaurantName().trim().equals(cleanName));
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    /**
     * Belirli bir değerlendirmeyi siler.
     * 
     * @param review Silinecek değerlendirme
     */
    public void deleteReview(Review review) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            // Değerlendirmeyi sil
            session.delete(review);
            transaction.commit();
            
            // Belleği güncelle
            reviews.remove(review);
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    /**
     * Bir değerlendirmeyi günceller.
     * 
     * @param oldReview Güncellenecek değerlendirme
     * @param newReview Yeni değerlendirme bilgileri
     */
    public void updateReview(Review oldReview, Review newReview) {
        int index = reviews.indexOf(oldReview);
        if (index == -1) return;
        
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            // ID'yi koru
            newReview.setId(oldReview.getId());
            
            // Restaurant referansını koru
            if (oldReview.getRestaurant() != null) {
                newReview.setRestaurant(oldReview.getRestaurant());
            }
            
            // Güncelle
            session.update(newReview);
            transaction.commit();
            
            // Belleği güncelle
            reviews.set(index, newReview);
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    /**
     * Bir restorana ait tüm değerlendirmeleri getirir.
     * 
     * @param restaurantName Değerlendirmeleri getirilecek restoran adı
     * @return Restoran değerlendirmelerinin listesi
     */
    public List<Review> getReviewsForRestaurant(String restaurantName) {
        // Restoran adını temizle
        String cleanName = restaurantName.trim();
        System.out.println("Şu restoran için değerlendirmeler alınıyor: " + cleanName);
        
        // Veritabanından doğrudan sorgula
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Review> query = session.createQuery(
                "FROM Review r WHERE r.restaurantName = :name", 
                Review.class);
            query.setParameter("name", cleanName);
            List<Review> result = query.list();
            System.out.println("Veritabanından " + result.size() + " değerlendirme bulundu.");
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Değerlendirmeler alınırken hata: " + e.getMessage());
            
            // Hata durumunda bellekteki listeyi filtrele
            List<Review> filteredReviews = reviews.stream()
                    .filter(r -> r.getRestaurantName().trim().equals(cleanName))
                    .collect(Collectors.toList());
            System.out.println("Bellekten " + filteredReviews.size() + " değerlendirme bulundu.");
            return filteredReviews;
        }
    }

    /**
     * Değerlendirmeleri veritabanından yükler.
     */
    private void loadReviews() {
        System.out.println("Değerlendirmeler veritabanından yükleniyor...");
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Önce mevcut listeyi temizle
            reviews.clear();
            
            // Veritabanından tüm değerlendirmeleri çek
            Query<Review> query = session.createQuery("FROM Review", Review.class);
            List<Review> loadedReviews = query.list();
            
            System.out.println("Veritabanından " + loadedReviews.size() + " değerlendirme yüklendi.");
            
            // Değerlendirmeleri listeye ekle
            for (Review review : loadedReviews) {
                System.out.println("Değerlendirme yüklendi: ID: " + review.getId() + 
                                  ", Restoran: " + review.getRestaurantName() + 
                                  ", Puan: " + review.getRating());
                reviews.add(review);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Değerlendirmeler yüklenirken hata: " + e.getMessage());
            reviews = new ArrayList<>();
        }
    }

    /**
     * Bir restoranın ortalama puanını hesaplar.
     * 
     * @param restaurantName Ortalama puanı hesaplanacak restoran adı
     * @return Ortalama puan, değerlendirme yoksa 0.0
     */
    public double getAverageRating(String restaurantName) {
        // Restoran adını temizle
        String cleanName = restaurantName.trim();
        
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Ortalama puanı hesapla
            Query<Double> query = session.createQuery(
                "SELECT AVG(r.rating) FROM Review r WHERE r.restaurantName = :name", 
                Double.class);
            query.setParameter("name", cleanName);
            Double avgRating = query.uniqueResult();
            
            if (avgRating == null) {
                return 0.0;
            }
            
            return avgRating;
        } catch (Exception e) {
            e.printStackTrace();
            
            // Hata durumunda bellekteki değerlendirmelerden hesapla
            List<Review> restaurantReviews = reviews.stream()
                    .filter(r -> r.getRestaurantName().trim().equals(cleanName))
                    .collect(Collectors.toList());
            
            if (restaurantReviews.isEmpty()) {
                return 0.0;
            }
            
            double sum = 0.0;
            for (Review review : restaurantReviews) {
                sum += review.getRating();
            }
            
            return sum / restaurantReviews.size();
        }
    }

    public List<Review> getAllReviews() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Review> reviews = session.createQuery("FROM Review", Review.class).list();
        session.close();
        return reviews;
    }
} 