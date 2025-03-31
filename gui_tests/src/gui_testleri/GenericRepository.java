package gui_testleri;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Generic bir Repository sınıfı.
 * Bu sınıf, veritabanı işlemlerini generic olarak gerçekleştirir.
 * T tipindeki nesnelerin CRUD işlemlerini yönetir.
 * 
 * @param <T> Repository'nin işlem yapacağı veri tipi
 * @param <K> Veri tipinin benzersiz kimlik (ID) tipi
 */
public class GenericRepository<T, K> {
    
    // Verileri depolamak için Map kullanımı
    private Map<K, T> items;
    
    /**
     * Constructor
     */
    public GenericRepository() {
        this.items = new HashMap<>();
    }
    
    /**
     * Yeni bir öğe ekler
     * 
     * @param id Öğenin benzersiz kimliği
     * @param item Eklenecek öğe
     * @return Ekleme işlemi başarılı ise true, değilse false
     */
    public boolean add(K id, T item) {
        if (items.containsKey(id)) {
            return false;
        }
        
        items.put(id, item);
        return true;
    }
    
    /**
     * Var olan bir öğeyi günceller
     * 
     * @param id Güncellenecek öğenin kimliği
     * @param item Yeni öğe değeri
     * @return Güncelleme işlemi başarılı ise true, öğe bulunamazsa false
     */
    public boolean update(K id, T item) {
        if (!items.containsKey(id)) {
            return false;
        }
        
        items.put(id, item);
        return true;
    }
    
    /**
     * Bir öğeyi siler
     * 
     * @param id Silinecek öğenin kimliği
     * @return Silme işlemi başarılı ise true, öğe bulunamazsa false
     */
    public boolean delete(K id) {
        if (!items.containsKey(id)) {
            return false;
        }
        
        items.remove(id);
        return true;
    }
    
    /**
     * Bir öğeyi kimliğine göre getirir
     * 
     * @param id Getirilecek öğenin kimliği
     * @return Bulunan öğe, bulunamazsa null
     */
    public T getById(K id) {
        return items.get(id);
    }
    
    /**
     * Tüm öğeleri getirir
     * 
     * @return Tüm öğelerin listesi
     */
    public List<T> getAll() {
        return new ArrayList<>(items.values());
    }
    
    /**
     * Belirli bir koşula göre öğeleri filtreler
     * 
     * @param predicate Filtreleme koşulu
     * @return Koşulu sağlayan öğelerin listesi
     */
    public List<T> find(Predicate<T> predicate) {
        return items.values().stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }
    
    /**
     * Repository'deki öğe sayısını döndürür
     * 
     * @return Öğe sayısı
     */
    public int count() {
        return items.size();
    }
    
    /**
     * Repository'deki tüm öğeleri temizler
     */
    public void clear() {
        items.clear();
    }
} 