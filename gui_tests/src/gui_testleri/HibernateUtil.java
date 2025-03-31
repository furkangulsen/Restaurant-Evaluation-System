package gui_testleri;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Hibernate oturum fabrikasını yöneten yardımcı sınıf.
 * Bu sınıf, singleton tasarım desenini kullanarak tek bir SessionFactory örneği sağlar.
 */
public class HibernateUtil {
    private static SessionFactory sessionFactory;
    private static final AtomicBoolean isInitializing = new AtomicBoolean(false);
    private static final AtomicBoolean isShuttingDown = new AtomicBoolean(false);

    /**
     * SessionFactory örneğini döndürür.
     * @return SessionFactory örneği
     */
    public static synchronized SessionFactory getSessionFactory() {
        if (isShuttingDown.get()) {
            try {
                System.out.println("Hibernate başlatma için shutdown işleminin bitmesi bekleniyor...");
                Thread.sleep(1000); // Kapatma işleminin tamamlanması için kısa bir bekleme
                isShuttingDown.set(false);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        if (sessionFactory == null) {
            // Eğer başka bir thread zaten başlatıyorsa bekle
            if (isInitializing.getAndSet(true)) {
                try {
                    System.out.println("Başka bir thread SessionFactory'yi başlatıyor, bekleniyor...");
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                isInitializing.set(false);
                return sessionFactory;
            }
            
            try {
                System.out.println("Hibernate SessionFactory başlatılıyor...");
                
                // Hibernate yapılandırmasını yükle
                Configuration configuration = new Configuration();
                configuration.configure("hibernate.cfg.xml");
                
                // Entity sınıflarını ekle
                configuration.addAnnotatedClass(Restaurant.class);
                configuration.addAnnotatedClass(Review.class);
                
                // SessionFactory'yi oluştur
                sessionFactory = configuration.buildSessionFactory();
                
                // Test bağlantısı
                try {
                    sessionFactory.openSession().close();
                    System.out.println("Veritabanı bağlantısı başarılı.");
                } catch (Exception e) {
                    System.err.println("Test bağlantısı başarısız: " + e.getMessage());
                    throw e;
                }
            } catch (Exception e) {
                System.err.println("SessionFactory oluşturulurken hata: " + e.getMessage());
                isInitializing.set(false);
                throw new ExceptionInInitializerError(e);
            } finally {
                isInitializing.set(false);
            }
        }
        return sessionFactory;
    }

    /**
     * Uygulama kapatılırken SessionFactory'yi temizler.
     */
    public static synchronized void shutdown() {
        if (isInitializing.get()) {
            try {
                System.out.println("Hibernate shutdown için initialization işleminin bitmesi bekleniyor...");
                Thread.sleep(1000); // Başlatma işleminin tamamlanması için kısa bir bekleme
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        isShuttingDown.set(true);
        
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            try {
                System.out.println("Hibernate SessionFactory kapatılıyor...");
                sessionFactory.close();
                sessionFactory = null; // Referansı temizle ki GC temizleyebilsin
                System.out.println("Hibernate SessionFactory başarıyla kapatıldı.");
            } catch (Exception e) {
                System.err.println("SessionFactory kapatılırken hata: " + e.getMessage());
                e.printStackTrace();
            } finally {
                isShuttingDown.set(false);
            }
        } else {
            System.out.println("SessionFactory zaten kapalı veya null.");
            sessionFactory = null; // Emin olmak için referansı temizle
        }
    }

    /**
     * SessionFactory'yi kapatıp tamamen yeniden başlatır
     */
    public static synchronized void restart() {
        System.out.println("Hibernate SessionFactory yeniden başlatılıyor...");
        shutdown();
        
        // Kısa bir bekleme süresi
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Yeniden başlat
        getSessionFactory();
        System.out.println("Hibernate SessionFactory yeniden başlatıldı.");
    }
} 