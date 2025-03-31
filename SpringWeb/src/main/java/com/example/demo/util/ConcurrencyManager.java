package com.example.demo.util;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import org.slf4j.Logger;

/**
 * Uygulama genelinde eşzamanlı işlemleri yönetmek için kullanılan sınıf
 */
public class ConcurrencyManager {
    private static final Logger LOGGER = AppLogger.getLogger(ConcurrencyManager.class);
    
    // Uygulama genelinde ortak bir thread havuzu
    private static final int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    private static final int MAX_POOL_SIZE = CORE_POOL_SIZE * 2;
    private static final long KEEP_ALIVE_TIME = 60L;
    
    private static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(
        CORE_POOL_SIZE,
        MAX_POOL_SIZE,
        KEEP_ALIVE_TIME, TimeUnit.SECONDS,
        new LinkedBlockingQueue<>(100),
        new CustomThreadFactory("RestaurantApp-"),
        new ThreadPoolExecutor.CallerRunsPolicy()
    );
    
    // Özel Thread Factory, iş parçacıklarına anlamlı isimler vermek için
    private static class CustomThreadFactory implements ThreadFactory {
        private final String prefix;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        
        public CustomThreadFactory(String prefix) {
            this.prefix = prefix;
        }
        
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, prefix + threadNumber.getAndIncrement());
            if (thread.isDaemon()) {
                thread.setDaemon(false);
            }
            if (thread.getPriority() != Thread.NORM_PRIORITY) {
                thread.setPriority(Thread.NORM_PRIORITY);
            }
            return thread;
        }
    }
    
    /**
     * Bir görevi eşzamanlı olarak çalıştırır
     * 
     * @param task Çalıştırılacak görev
     * @return Görevin gelecekteki sonucu
     */
    public static <T> CompletableFuture<T> submitTask(Supplier<T> task) {
        return CompletableFuture.supplyAsync(task, EXECUTOR)
            .exceptionally(throwable -> {
                LOGGER.error("Task execution failed: " + throwable.getMessage(), throwable);
                throw new CompletionException(throwable);
            });
    }
    
    /**
     * Bir görevi eşzamanlı olarak çalıştırır (sonuç döndürmeyen)
     * 
     * @param task Çalıştırılacak görev
     * @return Görevin gelecekteki sonucu
     */
    public static CompletableFuture<Void> submitTask(Runnable task) {
        return CompletableFuture.runAsync(task, EXECUTOR)
            .exceptionally(throwable -> {
                LOGGER.error("Task execution failed: " + throwable.getMessage(), throwable);
                throw new CompletionException(throwable);
            });
    }
    
    /**
     * Bir görev listesini paralel olarak çalıştırır ve tüm sonuçları bekler
     * 
     * @param tasks Çalıştırılacak görevler
     * @return Görevlerin sonuçlarının bir dizisi
     */
    @SafeVarargs
    public static <T> CompletableFuture<Void> submitAllTasks(Supplier<T>... tasks) {
        CompletableFuture<?>[] futures = new CompletableFuture[tasks.length];
        for (int i = 0; i < tasks.length; i++) {
            futures[i] = submitTask(tasks[i]);
        }
        return CompletableFuture.allOf(futures);
    }
    
    /**
     * Thread havuzunun mevcut durumunu raporlar
     * 
     * @return Thread havuzunun durum bilgisi
     */
    public static String getPoolStatus() {
        return String.format(
            "ThreadPool Status: [Active: %d, Completed: %d, Task Count: %d, Queue Size: %d]",
            EXECUTOR.getActiveCount(),
            EXECUTOR.getCompletedTaskCount(),
            EXECUTOR.getTaskCount(),
            EXECUTOR.getQueue().size()
        );
    }
    
    /**
     * Uygulama sonlandırılmadan önce çağrılarak, tüm işlemlerin tamamlanmasını bekler
     */
    public static void shutdown() {
        LOGGER.info("Shutting down thread pool...");
        EXECUTOR.shutdown();
        try {
            if (!EXECUTOR.awaitTermination(60, TimeUnit.SECONDS)) {
                EXECUTOR.shutdownNow();
                if (!EXECUTOR.awaitTermination(60, TimeUnit.SECONDS)) {
                    LOGGER.error("Thread pool did not terminate properly");
                }
            }
        } catch (InterruptedException ie) {
            EXECUTOR.shutdownNow();
            Thread.currentThread().interrupt();
        }
        LOGGER.info("Thread pool has been shut down");
    }
    
    /**
     * Uygulamanın süresiz olarak beklemesini sağlar.
     * Otomatik/demo modlarında, uygulamanın kapanmaması için kullanılır.
     * Bu metot, main thread'i bloklar ama diğer thread'lerin çalışmasına engel olmaz.
     */
    public static void waitIndefinitely() {
        LOGGER.info("Application is running in background mode");
        
        // CountDownLatch kullanarak ana thread'i süresiz beklet
        final CountDownLatch latch = new CountDownLatch(1);
        
        // Kapatma kancası ekle, ctrl+c ile sonlandırılabilmesi için
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Shutdown requested, cleaning up resources...");
            shutdown();
            latch.countDown();
        }));
        
        try {
            // Sonsuz bekle (ya da shutdown hook tetiklenene kadar)
            latch.await();
        } catch (InterruptedException e) {
            LOGGER.info("Wait interrupted");
            Thread.currentThread().interrupt();
        }
        
        LOGGER.info("Application is shutting down");
    }
} 