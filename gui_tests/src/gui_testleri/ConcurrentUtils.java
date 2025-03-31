package gui_testleri;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

/**
 * Concurrency işlemleri için yardımcı metotlar ve thread-safe veri yapıları sağlayan utility sınıfı.
 */
public class ConcurrentUtils {
    
    // Singleton instance
    private static ConcurrentUtils instance;
    
    // Logger
    private final AppLogger logger;
    
    /**
     * Private constructor - Singleton pattern
     */
    private ConcurrentUtils() {
        logger = AppLogger.getInstance();
        logger.info("ConcurrentUtils başlatıldı");
    }
    
    /**
     * Singleton instance getter
     * 
     * @return ConcurrentUtils instance
     */
    public static synchronized ConcurrentUtils getInstance() {
        if (instance == null) {
            instance = new ConcurrentUtils();
        }
        return instance;
    }
    
    /**
     * Thread-safe bir Map oluşturur
     * 
     * @param <K> Anahtar tipi
     * @param <V> Değer tipi
     * @return Thread-safe Map
     */
    public <K, V> Map<K, V> createConcurrentMap() {
        return new ConcurrentHashMap<>();
    }
    
    /**
     * Thread-safe bir List oluşturur
     * 
     * @param <T> Liste elemanlarının tipi
     * @return Thread-safe List
     */
    public <T> List<T> createConcurrentList() {
        return new CopyOnWriteArrayList<>();
    }
    
    /**
     * Thread-safe bir Set oluşturur
     * 
     * @param <T> Set elemanlarının tipi
     * @return Thread-safe Set
     */
    public <T> Set<T> createConcurrentSet() {
        return new CopyOnWriteArraySet<>();
    }
    
    /**
     * Atomic boolean değişken oluşturur
     * 
     * @param initialValue Başlangıç değeri
     * @return AtomicBoolean
     */
    public AtomicBoolean createAtomicBoolean(boolean initialValue) {
        return new AtomicBoolean(initialValue);
    }
    
    /**
     * Atomic integer değişken oluşturur
     * 
     * @param initialValue Başlangıç değeri
     * @return AtomicInteger
     */
    public AtomicInteger createAtomicInteger(int initialValue) {
        return new AtomicInteger(initialValue);
    }
    
    /**
     * Atomic long değişken oluşturur
     * 
     * @param initialValue Başlangıç değeri
     * @return AtomicLong
     */
    public AtomicLong createAtomicLong(long initialValue) {
        return new AtomicLong(initialValue);
    }
    
    /**
     * Atomic reference değişken oluşturur
     * 
     * @param <T> Referans tipi
     * @param initialValue Başlangıç değeri
     * @return AtomicReference
     */
    public <T> AtomicReference<T> createAtomicReference(T initialValue) {
        return new AtomicReference<>(initialValue);
    }
    
    /**
     * Reentrant kilit oluşturur
     * 
     * @param fair Adil kilit mi?
     * @return Lock
     */
    public Lock createLock(boolean fair) {
        return new ReentrantLock(fair);
    }
    
    /**
     * Okuma-yazma kilidi oluşturur
     * 
     * @param fair Adil kilit mi?
     * @return ReadWriteLock
     */
    public ReadWriteLock createReadWriteLock(boolean fair) {
        return new ReentrantReadWriteLock(fair);
    }
    
    /**
     * CountDownLatch oluşturur
     * 
     * @param count Sayaç değeri
     * @return CountDownLatch
     */
    public CountDownLatch createCountDownLatch(int count) {
        return new CountDownLatch(count);
    }
    
    /**
     * CyclicBarrier oluşturur
     * 
     * @param parties Beklenen thread sayısı
     * @param barrierAction Bariyer aşıldığında çalışacak eylem
     * @return CyclicBarrier
     */
    public CyclicBarrier createCyclicBarrier(int parties, Runnable barrierAction) {
        return new CyclicBarrier(parties, barrierAction);
    }
    
    /**
     * Semaphore oluşturur
     * 
     * @param permits İzin sayısı
     * @param fair Adil semaphore mu?
     * @return Semaphore
     */
    public Semaphore createSemaphore(int permits, boolean fair) {
        return new Semaphore(permits, fair);
    }
    
    /**
     * Bir işlemi kilit altında çalıştırır
     * 
     * @param lock Kullanılacak kilit
     * @param task Çalıştırılacak görev
     */
    public void withLock(Lock lock, Runnable task) {
        lock.lock();
        try {
            task.run();
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Bir işlemi kilit altında çalıştırır ve sonuç döndürür
     * 
     * @param <T> Sonuç tipi
     * @param lock Kullanılacak kilit
     * @param supplier Sonuç üreten işlev
     * @return İşlevin sonucu
     */
    public <T> T withLock(Lock lock, Supplier<T> supplier) {
        lock.lock();
        try {
            return supplier.get();
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Bir işlemi okuma kilidi altında çalıştırır
     * 
     * @param lock Kullanılacak okuma-yazma kilidi
     * @param task Çalıştırılacak görev
     */
    public void withReadLock(ReadWriteLock lock, Runnable task) {
        Lock readLock = lock.readLock();
        readLock.lock();
        try {
            task.run();
        } finally {
            readLock.unlock();
        }
    }
    
    /**
     * Bir işlemi okuma kilidi altında çalıştırır ve sonuç döndürür
     * 
     * @param <T> Sonuç tipi
     * @param lock Kullanılacak okuma-yazma kilidi
     * @param supplier Sonuç üreten işlev
     * @return İşlevin sonucu
     */
    public <T> T withReadLock(ReadWriteLock lock, Supplier<T> supplier) {
        Lock readLock = lock.readLock();
        readLock.lock();
        try {
            return supplier.get();
        } finally {
            readLock.unlock();
        }
    }
    
    /**
     * Bir işlemi yazma kilidi altında çalıştırır
     * 
     * @param lock Kullanılacak okuma-yazma kilidi
     * @param task Çalıştırılacak görev
     */
    public void withWriteLock(ReadWriteLock lock, Runnable task) {
        Lock writeLock = lock.writeLock();
        writeLock.lock();
        try {
            task.run();
        } finally {
            writeLock.unlock();
        }
    }
    
    /**
     * Bir işlemi yazma kilidi altında çalıştırır ve sonuç döndürür
     * 
     * @param <T> Sonuç tipi
     * @param lock Kullanılacak okuma-yazma kilidi
     * @param supplier Sonuç üreten işlev
     * @return İşlevin sonucu
     */
    public <T> T withWriteLock(ReadWriteLock lock, Supplier<T> supplier) {
        Lock writeLock = lock.writeLock();
        writeLock.lock();
        try {
            return supplier.get();
        } finally {
            writeLock.unlock();
        }
    }
    
    /**
     * Bir koleksiyonu thread-safe bir koleksiyona dönüştürür
     * 
     * @param <T> Koleksiyon elemanlarının tipi
     * @param collection Dönüştürülecek koleksiyon
     * @return Thread-safe koleksiyon
     */
    public <T> List<T> toThreadSafeList(Collection<T> collection) {
        return new CopyOnWriteArrayList<>(collection);
    }
    
    /**
     * Bir koleksiyonu thread-safe bir set'e dönüştürür
     * 
     * @param <T> Koleksiyon elemanlarının tipi
     * @param collection Dönüştürülecek koleksiyon
     * @return Thread-safe set
     */
    public <T> Set<T> toThreadSafeSet(Collection<T> collection) {
        return new CopyOnWriteArraySet<>(collection);
    }
    
    /**
     * Bir map'i thread-safe bir map'e dönüştürür
     * 
     * @param <K> Anahtar tipi
     * @param <V> Değer tipi
     * @param map Dönüştürülecek map
     * @return Thread-safe map
     */
    public <K, V> Map<K, V> toThreadSafeMap(Map<K, V> map) {
        return new ConcurrentHashMap<>(map);
    }
} 