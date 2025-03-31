package gui_testleri;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Uygulama genelinde thread pool yönetimini sağlayan sınıf.
 * Singleton pattern ile uygulanmıştır.
 */
public class ThreadPoolManager {
    
    // Singleton instance
    private static ThreadPoolManager instance;
    
    // Thread havuzları
    private final ExecutorService mainPool;
    private final ExecutorService ioPool;
    private final ExecutorService computePool;
    private final ScheduledExecutorService scheduledPool;
    
    // Logger
    private final AppLogger logger;
    
    /**
     * Private constructor - Singleton pattern
     */
    private ThreadPoolManager() {
        logger = AppLogger.getInstance();
        logger.info("ThreadPoolManager başlatılıyor...");
        
        // Ana thread havuzu - Genel amaçlı işlemler için
        mainPool = Executors.newCachedThreadPool(new NamedThreadFactory("Main-Pool"));
        
        // I/O işlemleri için thread havuzu (dosya, ağ, veritabanı)
        ioPool = Executors.newFixedThreadPool(
            Math.max(2, Runtime.getRuntime().availableProcessors()), 
            new NamedThreadFactory("IO-Pool")
        );
        
        // Hesaplama yoğun işlemler için thread havuzu
        computePool = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors(), 
            new NamedThreadFactory("Compute-Pool")
        );
        
        // Zamanlanmış görevler için thread havuzu
        scheduledPool = Executors.newScheduledThreadPool(
            2, 
            new NamedThreadFactory("Scheduled-Pool")
        );
        
        logger.info("ThreadPoolManager başlatıldı. " +
                "CPU çekirdek sayısı: " + Runtime.getRuntime().availableProcessors());
    }
    
    /**
     * Singleton instance getter
     * 
     * @return ThreadPoolManager instance
     */
    public static synchronized ThreadPoolManager getInstance() {
        if (instance == null) {
            instance = new ThreadPoolManager();
        }
        return instance;
    }
    
    /**
     * Thread havuzlarını kapatır ve kaynakları serbest bırakır
     */
    public void shutdown() {
        logger.info("ThreadPoolManager kapatılıyor...");
        
        shutdownPool(mainPool, "Main Pool");
        shutdownPool(ioPool, "IO Pool");
        shutdownPool(computePool, "Compute Pool");
        shutdownPool(scheduledPool, "Scheduled Pool");
        
        logger.info("ThreadPoolManager kapatıldı.");
    }
    
    /**
     * Belirtilen thread havuzunu düzgün şekilde kapatır
     * 
     * @param pool Kapatılacak thread havuzu
     * @param poolName Havuz adı (log için)
     */
    private void shutdownPool(ExecutorService pool, String poolName) {
        try {
            pool.shutdown();
            if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                logger.warning(poolName + " 5 saniye içinde kapatılamadı, zorla kapatılıyor...");
                pool.shutdownNow();
                
                if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                    logger.error(poolName + " zorla kapatılamadı!");
                }
            }
        } catch (InterruptedException e) {
            logger.error(poolName + " kapatılırken hata", e);
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Genel amaçlı thread havuzunda bir görev çalıştırır
     * 
     * @param task Çalıştırılacak görev
     */
    public void executeTask(Runnable task) {
        mainPool.execute(() -> {
            try {
                task.run();
            } catch (Exception e) {
                logger.error("Görev çalıştırılırken hata", e);
                ExceptionHandler.getInstance().handleException(e);
            }
        });
    }
    
    /**
     * I/O işlemleri için thread havuzunda bir görev çalıştırır
     * 
     * @param task Çalıştırılacak görev
     */
    public void executeIOTask(Runnable task) {
        ioPool.execute(() -> {
            try {
                task.run();
            } catch (Exception e) {
                logger.error("I/O görevi çalıştırılırken hata", e);
                ExceptionHandler.getInstance().handleException(e);
            }
        });
    }
    
    /**
     * Hesaplama yoğun işlemler için thread havuzunda bir görev çalıştırır
     * 
     * @param task Çalıştırılacak görev
     */
    public void executeComputeTask(Runnable task) {
        computePool.execute(() -> {
            try {
                task.run();
            } catch (Exception e) {
                logger.error("Hesaplama görevi çalıştırılırken hata", e);
                ExceptionHandler.getInstance().handleException(e);
            }
        });
    }
    
    /**
     * Belirli bir süre sonra çalışacak bir görev planlar
     * 
     * @param task Çalıştırılacak görev
     * @param delay Gecikme süresi
     * @param unit Zaman birimi
     */
    public void scheduleTask(Runnable task, long delay, TimeUnit unit) {
        scheduledPool.schedule(() -> {
            try {
                task.run();
            } catch (Exception e) {
                logger.error("Zamanlanmış görev çalıştırılırken hata", e);
                ExceptionHandler.getInstance().handleException(e);
            }
        }, delay, unit);
    }
    
    /**
     * Belirli aralıklarla tekrarlanan bir görev planlar
     * 
     * @param task Çalıştırılacak görev
     * @param initialDelay İlk çalıştırma için gecikme
     * @param period Tekrarlama aralığı
     * @param unit Zaman birimi
     */
    public void scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit) {
        scheduledPool.scheduleAtFixedRate(() -> {
            try {
                task.run();
            } catch (Exception e) {
                logger.error("Periyodik görev çalıştırılırken hata", e);
                ExceptionHandler.getInstance().handleException(e);
            }
        }, initialDelay, period, unit);
    }
    
    /**
     * Asenkron bir işlem başlatır ve sonucu CompletableFuture olarak döndürür
     * 
     * @param <T> Sonuç tipi
     * @param supplier Sonuç üreten işlev
     * @return CompletableFuture nesnesi
     */
    public <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, mainPool);
    }
    
    /**
     * I/O işlemleri için asenkron bir işlem başlatır ve sonucu CompletableFuture olarak döndürür
     * 
     * @param <T> Sonuç tipi
     * @param supplier Sonuç üreten işlev
     * @return CompletableFuture nesnesi
     */
    public <T> CompletableFuture<T> supplyAsyncIO(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, ioPool);
    }
    
    /**
     * Hesaplama işlemleri için asenkron bir işlem başlatır ve sonucu CompletableFuture olarak döndürür
     * 
     * @param <T> Sonuç tipi
     * @param supplier Sonuç üreten işlev
     * @return CompletableFuture nesnesi
     */
    public <T> CompletableFuture<T> supplyAsyncCompute(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, computePool);
    }
    
    /**
     * Asenkron bir işlem başlatır (sonuç döndürmeyen)
     * 
     * @param runnable Çalıştırılacak işlev
     * @return CompletableFuture nesnesi
     */
    public CompletableFuture<Void> runAsync(Runnable runnable) {
        return CompletableFuture.runAsync(runnable, mainPool);
    }
    
    /**
     * I/O işlemleri için asenkron bir işlem başlatır (sonuç döndürmeyen)
     * 
     * @param runnable Çalıştırılacak işlev
     * @return CompletableFuture nesnesi
     */
    public CompletableFuture<Void> runAsyncIO(Runnable runnable) {
        return CompletableFuture.runAsync(runnable, ioPool);
    }
    
    /**
     * Hesaplama işlemleri için asenkron bir işlem başlatır (sonuç döndürmeyen)
     * 
     * @param runnable Çalıştırılacak işlev
     * @return CompletableFuture nesnesi
     */
    public CompletableFuture<Void> runAsyncCompute(Runnable runnable) {
        return CompletableFuture.runAsync(runnable, computePool);
    }
    
    /**
     * Thread Factory sınıfı - özel isimlendirilmiş thread'ler oluşturur
     */
    private static class NamedThreadFactory implements ThreadFactory {
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;
        
        NamedThreadFactory(String namePrefix) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            this.namePrefix = namePrefix + "-thread-";
        }
        
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }
} 