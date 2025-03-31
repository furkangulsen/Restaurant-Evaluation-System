package gui_testleri;

import java.util.List;
import java.util.function.Predicate;

/**
 * Generic bir Service sınıfı.
 * Bu sınıf, GenericRepository'yi kullanarak üst düzey işlemleri gerçekleştirir.
 * 
 * @param <T> Service'in işlem yapacağı veri tipi
 * @param <K> Veri tipinin benzersiz kimlik (ID) tipi
 */
public class GenericService<T, K> {
    
    private GenericRepository<T, K> repository;
    
    /**
     * Constructor
     */
    public GenericService() {
        this.repository = new GenericRepository<>();
    }
    
    /**
     * Yeni bir öğe ekler
     * 
     * @param id Öğenin benzersiz kimliği
     * @param item Eklenecek öğe
     * @return Ekleme işlemi başarılı ise true, değilse false
     */
    public boolean add(K id, T item) {
        return repository.add(id, item);
    }
    
    /**
     * Var olan bir öğeyi günceller
     * 
     * @param id Güncellenecek öğenin kimliği
     * @param item Yeni öğe değeri
     * @return Güncelleme işlemi başarılı ise true, öğe bulunamazsa false
     */
    public boolean update(K id, T item) {
        return repository.update(id, item);
    }
    
    /**
     * Bir öğeyi siler
     * 
     * @param id Silinecek öğenin kimliği
     * @return Silme işlemi başarılı ise true, öğe bulunamazsa false
     */
    public boolean delete(K id) {
        return repository.delete(id);
    }
    
    /**
     * Bir öğeyi kimliğine göre getirir
     * 
     * @param id Getirilecek öğenin kimliği
     * @return Bulunan öğe, bulunamazsa null
     */
    public T getById(K id) {
        return repository.getById(id);
    }
    
    /**
     * Tüm öğeleri getirir
     * 
     * @return Tüm öğelerin listesi
     */
    public List<T> getAll() {
        return repository.getAll();
    }
    
    /**
     * Belirli bir koşula göre öğeleri filtreler
     * 
     * @param predicate Filtreleme koşulu
     * @return Koşulu sağlayan öğelerin listesi
     */
    public List<T> find(Predicate<T> predicate) {
        return repository.find(predicate);
    }
    
    /**
     * Service'deki öğe sayısını döndürür
     * 
     * @return Öğe sayısı
     */
    public int count() {
        return repository.count();
    }
    
    /**
     * Service'deki tüm öğeleri temizler
     */
    public void clear() {
        repository.clear();
    }
} 