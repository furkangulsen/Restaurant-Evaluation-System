package gui_testleri;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import javax.swing.DefaultListModel;
import javax.swing.SwingUtilities;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

/**
 * Restoran verilerini yöneten sınıf.
 * Bu sınıf, restoranların eklenmesi, silinmesi, güncellenmesi ve
 * veritabanından okunup veritabanına yazılması işlemlerini gerçekleştirir.
 * Thread-safe olarak tasarlanmıştır.
 */
public class RestaurantManager {
    // Veri yapıları
    private final Map<String, Restaurant> restaurantMap;  // Ad -> Restoran eşlemesi için ConcurrentHashMap
    private final DefaultListModel<String> listModel;     // GUI liste modeli (opsiyonel)
    
    // Senkronizasyon için kilitler
    private final ReadWriteLock rwLock;
    
    // Thread havuzu ve concurrency yardımcıları
    private final ThreadPoolManager threadPool;
    private final ConcurrentUtils concurrentUtils;
    
    // Logger
    private final AppLogger logger;
    
    /**
     * RestaurantManager sınıfının yapıcı metodu.
     * Liste modelini alır ve restoranları veritabanından yükler.
     * 
     * @param listModel JList için veri modeli (konsol modu için null olabilir)
     */
    public RestaurantManager(DefaultListModel<String> listModel) {
        logger = AppLogger.getInstance();
        logger.info("RestaurantManager başlatılıyor...");
        
        this.listModel = listModel;
        this.restaurantMap = new ConcurrentHashMap<>();
        this.rwLock = new ReentrantReadWriteLock();
        this.threadPool = ThreadPoolManager.getInstance();
        this.concurrentUtils = ConcurrentUtils.getInstance();
        
        // Veritabanından restoranları yükle
        loadRestaurants();
        
        logger.info("RestaurantManager başlatıldı, toplam restoran sayısı: " + restaurantMap.size());
    }

    /**
     * Yeni bir restoran ekler.
     * 
     * @param restaurant Eklenecek restoran nesnesi
     * @return Ekleme başarılı ise true, restoran zaten varsa false
     */
    public boolean addRestaurant(Restaurant restaurant) {
        // Önce map'te bu isimde bir restoran var mı kontrol et
        if (restaurantMap.containsKey(restaurant.getName())) {
            return false;
        }
        
        // Yazma kilidi al
        return concurrentUtils.withWriteLock(rwLock, () -> {
            Transaction transaction = null;
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                transaction = session.beginTransaction();
                session.save(restaurant);
                transaction.commit();
                
                // Map'e ekle
                restaurantMap.put(restaurant.getName(), restaurant);
                
                // UI güncellemesini EDT'de yap
                updateListModel();
                
                logger.info("Yeni restoran eklendi: " + restaurant.getName());
                return true;
            } catch (Exception e) {
                if (transaction != null && transaction.isActive()) {
                    transaction.rollback();
                }
                logger.error("Restoran eklenirken hata", e);
                return false;
            }
        });
    }

    /**
     * Var olan bir restoranı günceller.
     * 
     * @param restaurant Güncellenecek restoran
     * @return Güncelleme başarılı ise true, değilse false
     */
    public boolean updateRestaurant(Restaurant restaurant) {
        if (restaurant == null) return false;

        // Yazma kilidi al
        return concurrentUtils.withWriteLock(rwLock, () -> {
            Session session = null;
            Transaction tx = null;
            try {
                session = HibernateUtil.getSessionFactory().openSession();
                tx = session.beginTransaction();
                
                // Veritabanından mevcut restoranı al
                Restaurant existingRestaurant = null;
                
                // ID varsa ID'ye göre, yoksa isme göre ara
                if (restaurant.getId() != null) {
                    existingRestaurant = session.get(Restaurant.class, restaurant.getId());
                } else {
                    // ID yoksa isim bazlı arama yap
                    String hql = "FROM Restaurant WHERE name = :name";
                    Query<Restaurant> query = session.createQuery(hql, Restaurant.class);
                    query.setParameter("name", restaurant.getName());
                    List<Restaurant> results = query.list();
                    
                    if (!results.isEmpty()) {
                        existingRestaurant = results.get(0);
                    }
                }
                
                if (existingRestaurant == null) {
                    logger.warning("Güncellenecek restoran bulunamadı: " + 
                                  (restaurant.getId() != null ? "ID=" + restaurant.getId() : "name=" + restaurant.getName()));
                    return false;
                }

                // Değerleri güncelle
                existingRestaurant.setName(restaurant.getName());
                existingRestaurant.setLocation(restaurant.getLocation());
                existingRestaurant.setRating(restaurant.getRating());

                // Değişiklikleri kaydet
                session.update(existingRestaurant);
                tx.commit();

                // Bellekteki haritayı güncelle
                // Önce eski isimle kaydı kaldır (isim değişmiş olabilir)
                String oldName = null;
                for (Map.Entry<String, Restaurant> entry : restaurantMap.entrySet()) {
                    if (entry.getValue().getId().equals(existingRestaurant.getId())) {
                        oldName = entry.getKey();
                        break;
                    }
                }
                
                if (oldName != null && !oldName.equals(existingRestaurant.getName())) {
                    restaurantMap.remove(oldName);
                }
                
                // Yeni isimle ekle
                restaurantMap.put(existingRestaurant.getName(), existingRestaurant);

                // GUI listesini güncelle
                updateListModel();
                
                logger.info("Restoran güncellendi: " + existingRestaurant.getName());
                return true;
            } catch (Exception e) {
                if (tx != null) tx.rollback();
                logger.error("Restoran güncellenirken hata", e);
                return false;
            } finally {
                if (session != null && session.isOpen()) {
                    session.close();
                }
            }
        });
    }

    /**
     * Bir restoranı ismine göre siler.
     * 
     * @param name Silinecek restoranın adı
     * @return Silme başarılı ise true, restoran bulunamazsa false
     */
    public boolean deleteRestaurant(String name) {
        logger.debug("Silme işlemi başladı: " + name);
        
        // Yazma kilidi al
        return concurrentUtils.withWriteLock(rwLock, () -> {
            Restaurant restaurant = restaurantMap.get(name);
            if (restaurant == null) {
                logger.warning("Silinecek restoran bulunamadı: " + name);
                return false;
            }
            
            Transaction transaction = null;
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                transaction = session.beginTransaction();
                
                // Önce restoranı veritabanından getir
                Restaurant managedRestaurant = session.get(Restaurant.class, restaurant.getId());
                if (managedRestaurant != null) {
                    // İlişkili değerlendirmeleri temizle
                    managedRestaurant.getReviews().clear();
                    session.flush();
                    
                    // Restoranı sil
                    session.delete(managedRestaurant);
                    transaction.commit();
                    
                    // Belleği güncelle
                    restaurantMap.remove(name);
                    
                    // UI güncellemesini EDT'de yap
                    updateListModel();
                    
                    logger.info("Restoran ve ilişkili değerlendirmeleri silindi: " + name);
                    return true;
                } else {
                    logger.warning("Restoran veritabanında bulunamadı: " + name);
                    return false;
                }
            } catch (Exception e) {
                if (transaction != null && transaction.isActive()) {
                    transaction.rollback();
                }
                logger.error("Restoran silme hatası", e);
                return false;
            }
        });
    }

    /**
     * Bir restoranı ismine göre bulur.
     * 
     * @param name Aranacak restoran adı
     * @return Bulunan restoran nesnesi, bulunamazsa null
     */
    public Restaurant getRestaurant(String name) {
        // Okuma kilidi al
        return concurrentUtils.withReadLock(rwLock, () -> restaurantMap.get(name));
    }

    /**
     * Tüm restoranların listesini döndürür.
     * 
     * @return Restoran listesi
     */
    public List<Restaurant> getAllRestaurants() {
        // Okuma kilidi al
        return concurrentUtils.withReadLock(rwLock, () -> {
            // Değişmez (immutable) bir liste döndür
            return Collections.unmodifiableList(new ArrayList<>(restaurantMap.values()));
        });
    }

    /**
     * Restoranları veritabanından yükler.
     */
    public void loadRestaurants() {
        logger.info("Restoranlar veritabanından yükleniyor...");
        
        // Yazma kilidi al
        concurrentUtils.withWriteLock(rwLock, () -> {
            // Önce haritayı temizle
            restaurantMap.clear();
            
            // Veritabanı işlemini asenkron olarak gerçekleştir
            threadPool.supplyAsyncIO(() -> {
                Session session = null;
                try {
                    session = HibernateUtil.getSessionFactory().openSession();
                    
                    // Doğrudan native SQL sorgusu kullan
                    @SuppressWarnings("unchecked")
                    List<Object[]> results = session.createNativeQuery(
                        "SELECT id, name, location, rating FROM public.restaurants ORDER BY name")
                        .list();
                    
                    logger.info("Veritabanından " + results.size() + " restoran bulundu.");
                    
                    // Geçici bir map oluştur
                    Map<String, Restaurant> tempMap = new HashMap<>();
                    
                    for (Object[] row : results) {
                        try {
                            Restaurant restaurant = new Restaurant();
                            restaurant.setId(row[0] == null ? null : Long.valueOf(row[0].toString()));
                            restaurant.setName(row[1] == null ? "" : row[1].toString());
                            restaurant.setLocation(row[2] == null ? "" : row[2].toString());
                            restaurant.setRating(row[3] == null ? 0.0 : Double.parseDouble(row[3].toString()));
                            
                            tempMap.put(restaurant.getName(), restaurant);
                        } catch (Exception ex) {
                            logger.error("Veri dönüşüm hatası", ex);
                        }
                    }
                    
                    return tempMap;
                } catch (Exception e) {
                    logger.error("Restoranlar yüklenirken kritik hata", e);
                    
                    // Hata durumunda yeniden deneme (son çare)
                    try {
                        Thread.sleep(1000); // Kısa bir süre bekle
                        
                        // SessionFactory'yi yeniden başlatmayı dene
                        HibernateUtil.shutdown();
                        Thread.sleep(1000);
                        HibernateUtil.getSessionFactory();
                        
                        // Yeniden yüklemeyi dene
                        logger.info("Restoranları yeniden yüklemeyi deniyorum...");
                        
                        session = HibernateUtil.getSessionFactory().openSession();
                        @SuppressWarnings("unchecked")
                        List<Restaurant> restaurants = session.createQuery("FROM Restaurant ORDER BY name").list();
                        
                        Map<String, Restaurant> recoveryMap = new HashMap<>();
                        for (Restaurant r : restaurants) {
                            recoveryMap.put(r.getName(), r);
                        }
                        
                        return recoveryMap;
                    } catch (Exception retryEx) {
                        logger.error("Yeniden yükleme denemesi başarısız", retryEx);
                        return new HashMap<String, Restaurant>();
                    } finally {
                        if (session != null && session.isOpen()) {
                            session.close();
                        }
                    }
                } finally {
                    if (session != null && session.isOpen()) {
                        session.close();
                    }
                }
            }).thenAcceptAsync(tempMap -> {
                // Yazma kilidi al ve ana map'i güncelle
                concurrentUtils.withWriteLock(rwLock, () -> {
                    restaurantMap.clear();
                    restaurantMap.putAll(tempMap);
                    
                    // UI güncellemesini EDT'de yap
                    updateListModel();
                    
                    logger.info("Restoranlar başarıyla yüklendi. Toplam: " + restaurantMap.size());
                });
            });
        });
    }

    /**
     * Liste modelini günceller.
     * Bu metod, GUI thread'inde (EDT) çalıştırılmalıdır.
     */
    private void updateListModel() {
        if (listModel == null) return;
        
        // UI güncellemelerini EDT'de yap
        if (SwingUtilities.isEventDispatchThread()) {
            updateListModelInternal();
        } else {
            SwingUtilities.invokeLater(this::updateListModelInternal);
        }
    }
    
    /**
     * Liste modelini güncelleyen iç metod.
     * Bu metod EDT'de çalıştırılmalıdır.
     */
    private void updateListModelInternal() {
        // Okuma kilidi al
        concurrentUtils.withReadLock(rwLock, () -> {
            listModel.clear();
            
            // Restoranları ada göre sırala
            List<Restaurant> sortedList = restaurantMap.values().stream()
                .sorted(Comparator.comparing(Restaurant::getName))
                .collect(Collectors.toList());
            
            // Listeye ekle
            for (Restaurant restaurant : sortedList) {
                listModel.addElement(restaurant.toDisplayString());
            }
            
            logger.debug("Liste modeli güncellendi. Eleman sayısı: " + listModel.size());
        });
    }

    /**
     * Restoranları konsola yazdırır.
     */
    public void printRestaurants() {
        // Okuma kilidi al
        concurrentUtils.withReadLock(rwLock, () -> {
            List<Restaurant> sortedRestaurants = new ArrayList<>(restaurantMap.values());
            sortedRestaurants.sort(Comparator.comparing(Restaurant::getName));
    
            System.out.println("\n=== Restoranlar ===");
            System.out.println("ID\tAd\t\tKonum\t\tPuan");
            System.out.println("--------------------------------------------------------------------------------");
            
            for (Restaurant restaurant : sortedRestaurants) {
                System.out.printf("%d\t%-15s\t%-15s\t%.1f%n",
                    restaurant.getId(),
                    restaurant.getName(),
                    restaurant.getLocation(),
                    restaurant.getRating());
            }
            System.out.println("--------------------------------------------------------------------------------");
        });
    }
} 