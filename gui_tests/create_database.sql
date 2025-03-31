-- Veritabanını oluştur (eğer yoksa)
-- Bu komutu PostgreSQL komut satırında çalıştırın
-- CREATE DATABASE restaurant_db;

-- Tabloları oluştur
-- Bu komutları restaurant_db veritabanına bağlandıktan sonra çalıştırın

-- Restoranlar tablosu
CREATE TABLE IF NOT EXISTS public.restaurants (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    location VARCHAR(100),
    rating DECIMAL(3, 1) DEFAULT 0.0,
    address VARCHAR(255)
);

-- Değerlendirmeler tablosu
CREATE TABLE IF NOT EXISTS public.reviews (
    id SERIAL PRIMARY KEY,
    restaurant_id INTEGER REFERENCES public.restaurants(id) ON DELETE CASCADE,
    restaurant_name VARCHAR(100) NOT NULL,
    comment TEXT,
    rating INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Örnek veriler (isteğe bağlı)
-- Restoranlar
INSERT INTO public.restaurants (name, location, rating, address) VALUES
('Kebapçı Ahmet', 'İstanbul', 4.5, 'Kadıköy Meydan No:12'),
('Pizza Roma', 'Ankara', 4.2, 'Kızılay Caddesi No:45'),
('Sushi House', 'İzmir', 4.8, 'Alsancak Sahil No:23'),
('Burger King', 'İstanbul', 3.9, 'Beşiktaş Meydanı No:5'),
('Çiğköfteci Ali Usta', 'Ankara', 4.1, 'Bahçelievler 7. Cadde No:18');

-- Değerlendirmeler
INSERT INTO public.reviews (restaurant_id, restaurant_name, comment, rating, date) VALUES
(1, 'Kebapçı Ahmet', 'Adana kebap çok lezzetliydi.', 5, CURRENT_TIMESTAMP - INTERVAL '2 DAY'),
(1, 'Kebapçı Ahmet', 'Servis biraz yavaştı ama yemekler güzeldi.', 4, CURRENT_TIMESTAMP - INTERVAL '5 DAY'),
(2, 'Pizza Roma', 'Margarita pizza harika!', 5, CURRENT_TIMESTAMP - INTERVAL '1 DAY'),
(3, 'Sushi House', 'En iyi sushi burada yapılıyor.', 5, CURRENT_TIMESTAMP - INTERVAL '3 DAY'),
(3, 'Sushi House', 'Fiyatlar biraz yüksek ama kalite çok iyi.', 4, CURRENT_TIMESTAMP - INTERVAL '7 DAY'),
(4, 'Burger King', 'Standart fast food, beklediğim gibiydi.', 3, CURRENT_TIMESTAMP - INTERVAL '4 DAY'),
(5, 'Çiğköfteci Ali Usta', 'Acılı çiğköfte muhteşemdi!', 5, CURRENT_TIMESTAMP - INTERVAL '2 DAY');

-- Restoranların ortalama puanlarını güncelle
UPDATE public.restaurants r
SET rating = (
    SELECT COALESCE(AVG(rv.rating), 0)
    FROM public.reviews rv
    WHERE rv.restaurant_name = r.name
); 