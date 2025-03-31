-- Restoran Değerlendirme Sistemi: Veritabanı Şeması ve Örnek Veriler
-- -------------------------------------------------------------------------

-- Tabloların varsa silinmesi
DROP TABLE IF EXISTS reviews CASCADE;
DROP TABLE IF EXISTS restaurants CASCADE;

-- Restaurants (Restoranlar) tablosunun oluşturulması
CREATE TABLE restaurants (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    location VARCHAR(100) NOT NULL,
    rating DOUBLE PRECISION DEFAULT 0.0
);

-- Reviews (Değerlendirmeler) tablosunun oluşturulması
CREATE TABLE reviews (
    id SERIAL PRIMARY KEY,
    restaurant_id BIGINT,
    restaurant_name VARCHAR(100) NOT NULL,
    comment TEXT,
    rating INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE
);

-- Restoranlar tablosuna örnek veriler ekle
INSERT INTO restaurants (name, location, rating) VALUES 
('Kebapçı Mehmet', 'İstanbul/Beyoğlu', 4.7),
('Balıkçı Ahmet', 'İstanbul/Kadıköy', 4.3),
('İtalyan Mutfağı', 'İzmir/Konak', 4.5),
('Çin Lokantası', 'Ankara/Çankaya', 3.8),
('Ev Yemekleri', 'Bursa/Nilüfer', 4.9),
('Fast Food Corner', 'İstanbul/Şişli', 3.5),
('Kahvaltı Dünyası', 'Antalya/Muratpaşa', 4.8),
('Tatlıcı Hasan', 'İstanbul/Beşiktaş', 4.6),
('Akdeniz Mutfağı', 'İzmir/Karşıyaka', 4.2),
('Anadolu Lezzetleri', 'Eskişehir/Tepebaşı', 4.4);

-- Değerlendirmeler tablosuna örnek veriler ekle
-- Kebapçı Mehmet için değerlendirmeler
INSERT INTO reviews (restaurant_id, restaurant_name, rating, comment, date) VALUES 
(1, 'Kebapçı Mehmet', 5, 'Adana kebap çok lezzetli, kesinlikle tavsiye ederim!', '2023-06-15 14:30:00'),
(1, 'Kebapçı Mehmet', 4, 'Et kalitesi ve pişirme çok iyi, ancak servis biraz yavaş.', '2023-07-20 19:15:00');

-- Balıkçı Ahmet için değerlendirmeler
INSERT INTO reviews (restaurant_id, restaurant_name, rating, comment, date) VALUES 
(2, 'Balıkçı Ahmet', 5, 'Taze balık, harika manzara! Levrek çok lezzetliydi.', '2023-05-25 13:20:00'),
(2, 'Balıkçı Ahmet', 3, 'Balıklar taze ama fiyatlar biraz yüksek.', '2023-06-30 21:10:00');

-- İtalyan Mutfağı için değerlendirmeler
INSERT INTO reviews (restaurant_id, restaurant_name, rating, comment, date) VALUES 
(3, 'İtalyan Mutfağı', 4, 'Pizza hamuru çok lezzetli, içindekiler taze.', '2023-06-10 19:45:00'),
(3, 'İtalyan Mutfağı', 5, 'Makarnalar tam kıvamında, soslar ev yapımı gibi.', '2023-07-05 20:15:00');

-- Çin Lokantası için değerlendirmeler
INSERT INTO reviews (restaurant_id, restaurant_name, rating, comment, date) VALUES 
(4, 'Çin Lokantası', 4, 'Pekin ördeği çok lezzetli, porsiyonlar doyurucu.', '2023-05-15 18:30:00');

-- Ev Yemekleri için değerlendirmeler
INSERT INTO reviews (restaurant_id, restaurant_name, rating, comment, date) VALUES 
(5, 'Ev Yemekleri', 5, 'Gerçekten ev yemeği tadında, kuru fasulye harika!', '2023-04-25 13:45:00'),
(5, 'Ev Yemekleri', 5, 'Sarma ve dolmalar el yapımı, çok lezzetli.', '2023-05-30 14:30:00');

-- Fast Food Corner için değerlendirmeler
INSERT INTO reviews (restaurant_id, restaurant_name, rating, comment, date) VALUES 
(6, 'Fast Food Corner', 3, 'Hamburger ortalama ama patates kızartması iyi.', '2023-07-01 15:30:00');

-- Kahvaltı Dünyası için değerlendirmeler
INSERT INTO reviews (restaurant_id, restaurant_name, rating, comment, date) VALUES 
(7, 'Kahvaltı Dünyası', 5, 'Serpme kahvaltı çok zengin ve lezzetli!', '2023-04-10 10:15:00');

-- Tatlıcı Hasan için değerlendirmeler
INSERT INTO reviews (restaurant_id, restaurant_name, rating, comment, date) VALUES 
(8, 'Tatlıcı Hasan', 5, 'Baklava çok taze ve lezzetli, şerbeti tam kıvamında.', '2023-07-15 16:30:00');

-- Akdeniz Mutfağı için değerlendirmeler
INSERT INTO reviews (restaurant_id, restaurant_name, rating, comment, date) VALUES 
(9, 'Akdeniz Mutfağı', 4, 'Humus ve tabule çok lezzetli, tavsiye ederim.', '2023-06-30 19:30:00');

-- Anadolu Lezzetleri için değerlendirmeler
INSERT INTO reviews (restaurant_id, restaurant_name, rating, comment, date) VALUES 
(10, 'Anadolu Lezzetleri', 5, 'Kuzu tandır muhteşemdi, et ağızda dağılıyor.', '2023-05-15 14:00:00');

-- Restoranların puan ortalamasını güncelle
UPDATE restaurants r
SET rating = (
    SELECT COALESCE(AVG(CAST(rev.rating AS double precision)), 0.0) 
    FROM reviews rev 
    WHERE rev.restaurant_id = r.id
);

-- Örnek sorgu: En yüksek puanlı 5 restoran
-- SELECT name, location, rating FROM restaurants ORDER BY rating DESC LIMIT 5;

-- Örnek sorgu: Bir restoranın tüm değerlendirmeleri
-- SELECT r.restaurant_name, r.rating, r.comment, r.date FROM reviews r WHERE r.restaurant_id = 1 ORDER BY r.date DESC; 