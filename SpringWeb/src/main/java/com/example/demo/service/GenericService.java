package com.example.demo.service;

import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

/**
 * Temel CRUD işlemlerini içeren generic servis arayüzü
 * 
 * @param <T> Entity tipi
 * @param <ID> Entity ID tipi
 */
public interface GenericService<T, ID> {
    
    /**
     * Tüm entity nesnelerini getirir
     * 
     * @return Entity listesi
     */
    List<T> findAll();
    
    /**
     * ID'ye göre entity bulur
     * 
     * @param id Entity ID'si
     * @return Bulunan entity, yoksa Optional.empty()
     */
    Optional<T> findById(ID id);
    
    /**
     * Yeni bir entity ekler
     * 
     * @param entity Eklenecek entity
     * @return Eklenen entity
     */
    @Transactional
    T save(T entity);
    
    /**
     * Bir entity'yi günceller
     * 
     * @param id Güncellenecek entity'nin ID'si
     * @param entity Yeni entity bilgileri
     * @return Güncellenen entity, yoksa Optional.empty()
     */
    @Transactional
    Optional<T> update(ID id, T entity);
    
    /**
     * Bir entity'yi siler
     * 
     * @param id Silinecek entity'nin ID'si
     */
    @Transactional
    void delete(ID id);
    
    /**
     * Bir entity'nin varlığını kontrol eder
     * 
     * @param id Kontrol edilecek entity'nin ID'si
     * @return Entity varsa true, yoksa false
     */
    boolean existsById(ID id);
    
    /**
     * Entity sayısını getirir
     * 
     * @return Toplam entity sayısı
     */
    long count();
} 