-- Örnek veri dosyası: Restoran Değerlendirme Sistemi
-- Bu dosya, sistem için örnek veri seti içerir

-- Tablolarda mevcut verileri temizle
TRUNCATE TABLE reviews CASCADE;
TRUNCATE TABLE restaurants CASCADE;

-- Restart sequence
ALTER SEQUENCE restaurants_id_seq RESTART WITH 1;
ALTER SEQUENCE reviews_id_seq RESTART WITH 1;

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
('Anadolu Lezzetleri', 'Eskişehir/Tepebaşı', 4.4),
('Veggie Garden', 'İstanbul/Kadıköy', 4.1),
('Steak House', 'Ankara/Kızılay', 4.7),
('Sushi Bar', 'İstanbul/Levent', 4.0),
('Pide Salonu', 'Trabzon/Merkez', 4.6),
('Mantı Evi', 'Kayseri/Melikgazi', 4.8);

-- Değerlendirmeler tablosuna örnek veriler ekle
-- Kebapçı Mehmet için değerlendirmeler
INSERT INTO reviews (restaurant_id, restaurant_name, rating, comment, date) VALUES 
(1, 'Kebapçı Mehmet', 5, 'Adana kebap çok lezzetli, kesinlikle tavsiye ederim!', '2023-06-15 14:30:00'),
(1, 'Kebapçı Mehmet', 4, 'Et kalitesi ve pişirme çok iyi, ancak servis biraz yavaş.', '2023-07-20 19:15:00'),
(1, 'Kebapçı Mehmet', 5, 'Urfa kebap muhteşemdi, fiyat/performans çok iyi.', '2023-08-10 20:45:00');

-- Balıkçı Ahmet için değerlendirmeler
INSERT INTO reviews (restaurant_id, restaurant_name, rating, comment, date) VALUES 
(2, 'Balıkçı Ahmet', 5, 'Taze balık, harika manzara! Levrek çok lezzetliydi.', '2023-05-25 13:20:00'),
(2, 'Balıkçı Ahmet', 3, 'Balıklar taze ama fiyatlar biraz yüksek.', '2023-06-30 21:10:00'),
(2, 'Balıkçı Ahmet', 5, 'Çupra ızgara mükemmeldi, tavsiye ederim.', '2023-07-15 20:30:00');

-- İtalyan Mutfağı için değerlendirmeler
INSERT INTO reviews (restaurant_id, restaurant_name, rating, comment, date) VALUES 
(3, 'İtalyan Mutfağı', 4, 'Pizza hamuru çok lezzetli, içindekiler taze.', '2023-06-10 19:45:00'),
(3, 'İtalyan Mutfağı', 5, 'Makarnalar tam kıvamında, soslar ev yapımı gibi.', '2023-07-05 20:15:00');

-- Çin Lokantası için değerlendirmeler
INSERT INTO reviews (restaurant_id, restaurant_name, rating, comment, date) VALUES 
(4, 'Çin Lokantası', 4, 'Pekin ördeği çok lezzetli, porsiyonlar doyurucu.', '2023-05-15 18:30:00'),
(4, 'Çin Lokantası', 3, 'Yemekler lezzetli ama bazı soslar çok baharatlı.', '2023-06-20 19:00:00');

-- Ev Yemekleri için değerlendirmeler
INSERT INTO reviews (restaurant_id, restaurant_name, rating, comment, date) VALUES 
(5, 'Ev Yemekleri', 5, 'Gerçekten ev yemeği tadında, kuru fasulye harika!', '2023-04-25 13:45:00'),
(5, 'Ev Yemekleri', 5, 'Sarma ve dolmalar el yapımı, çok lezzetli.', '2023-05-30 14:30:00'),
(5, 'Ev Yemekleri', 5, 'İmam bayıldı ve karnıyarık muhteşemdi.', '2023-06-15 13:00:00'),
(5, 'Ev Yemekleri', 4, 'Yemekler lezzetli ama servis biraz yavaş.', '2023-07-10 14:15:00');

-- Fast Food Corner için değerlendirmeler
INSERT INTO reviews (restaurant_id, restaurant_name, rating, comment, date) VALUES 
(6, 'Fast Food Corner', 3, 'Hamburger ortalama ama patates kızartması iyi.', '2023-07-01 15:30:00'),
(6, 'Fast Food Corner', 4, 'Cheeseburger çok lezzetli, soslar özel yapım.', '2023-07-15 16:45:00');

-- Kahvaltı Dünyası için değerlendirmeler
INSERT INTO reviews (restaurant_id, restaurant_name, rating, comment, date) VALUES 
(7, 'Kahvaltı Dünyası', 5, 'Serpme kahvaltı çok zengin ve lezzetli!', '2023-04-10 10:15:00'),
(7, 'Kahvaltı Dünyası', 5, 'Kahvaltı tabağı dört kişiye yetti, çok dolu.', '2023-05-20 09:30:00'),
(7, 'Kahvaltı Dünyası', 4, 'Kahvaltı çeşitleri çok iyi ama fiyatlar biraz yüksek.', '2023-06-05 11:00:00');

-- Tatlıcı Hasan için değerlendirmeler
INSERT INTO reviews (restaurant_id, restaurant_name, rating, comment, date) VALUES 
(8, 'Tatlıcı Hasan', 5, 'Baklava çok taze ve lezzetli, şerbeti tam kıvamında.', '2023-07-15 16:30:00'),
(8, 'Tatlıcı Hasan', 4, 'Künefe çok güzel ama biraz geç servis edildi.', '2023-07-25 15:45:00');

-- Akdeniz Mutfağı için değerlendirmeler
INSERT INTO reviews (restaurant_id, restaurant_name, rating, comment, date) VALUES 
(9, 'Akdeniz Mutfağı', 4, 'Humus ve tabule çok lezzetli, tavsiye ederim.', '2023-06-30 19:30:00'),
(9, 'Akdeniz Mutfağı', 5, 'Falafel ve şakşuka harika, lezzetler çok otantik.', '2023-07-10 20:15:00'),
(9, 'Akdeniz Mutfağı', 4, 'Meze tabağı çeşitli ve lezzetli.', '2023-07-20 21:00:00');

-- Anadolu Lezzetleri için değerlendirmeler
INSERT INTO reviews (restaurant_id, restaurant_name, rating, comment, date) VALUES 
(10, 'Anadolu Lezzetleri', 5, 'Kuzu tandır muhteşemdi, et ağızda dağılıyor.', '2023-05-15 14:00:00'),
(10, 'Anadolu Lezzetleri', 4, 'İçli köfte çok lezzetli ama biraz küçüktü.', '2023-06-10 13:30:00'),
(10, 'Anadolu Lezzetleri', 4, 'Hünkar beğendi ve Ali Nazik harika.', '2023-07-05 19:45:00');

-- Veggie Garden için değerlendirmeler
INSERT INTO reviews (restaurant_id, restaurant_name, rating, comment, date) VALUES 
(11, 'Veggie Garden', 4, 'Sebze burger çok lezzetli, vegan seçenekler zengin.', '2023-06-01 13:00:00'),
(11, 'Veggie Garden', 5, 'Salataları çok taze ve lezzetli.', '2023-06-15 14:30:00'),
(11, 'Veggie Garden', 3, 'Yemekler lezzetli ama porsiyonlar küçük.', '2023-07-01 12:45:00');

-- Steak House için değerlendirmeler
INSERT INTO reviews (restaurant_id, restaurant_name, rating, comment, date) VALUES 
(12, 'Steak House', 5, 'T-Bone biftek mükemmeldi, tam istediğim pişirmede geldi.', '2023-05-25 20:15:00'),
(12, 'Steak House', 4, 'Entrecote çok lezzetli, yanındaki soslar harika.', '2023-06-10 21:30:00'),
(12, 'Steak House', 5, 'Dallas steak muhteşemdi, tam kıvamında pişmiş.', '2023-07-05 19:45:00');

-- Sushi Bar için değerlendirmeler
INSERT INTO reviews (restaurant_id, restaurant_name, rating, comment, date) VALUES 
(13, 'Sushi Bar', 4, 'Sushi çeşitleri taze ve lezzetli.', '2023-06-15 19:30:00'),
(13, 'Sushi Bar', 3, 'California roll iyiydi ama wasabi biraz sertti.', '2023-06-30 20:45:00'),
(13, 'Sushi Bar', 5, 'Nigiri sushi çok taze, tavsiye ederim.', '2023-07-15 21:00:00');

-- Pide Salonu için değerlendirmeler
INSERT INTO reviews (restaurant_id, restaurant_name, rating, comment, date) VALUES 
(14, 'Pide Salonu', 5, 'Kuşbaşılı pide muhteşemdi, hamur çok lezzetli.', '2023-04-20 14:15:00'),
(14, 'Pide Salonu', 4, 'Kaşarlı pide çok güzeldi ama biraz yağlıydı.', '2023-05-10 13:30:00'),
(14, 'Pide Salonu', 5, 'Kıymalı pide tam kıvamında, hamur harika.', '2023-06-05 15:00:00');

-- Mantı Evi için değerlendirmeler
INSERT INTO reviews (restaurant_id, restaurant_name, rating, comment, date) VALUES 
(15, 'Mantı Evi', 5, 'Kayseri mantısı harika, tam el yapımı lezzet.', '2023-05-05 12:30:00'),
(15, 'Mantı Evi', 5, 'Mantılar çok inceydi, yoğurt ve sos mükemmeldi.', '2023-06-15 13:45:00'),
(15, 'Mantı Evi', 4, 'Mantı çeşitleri lezzetli ama servis biraz yavaş.', '2023-07-10 14:30:00');

-- Restoranların puan ortalamasını güncelle
UPDATE restaurants r
SET rating = (
    SELECT COALESCE(AVG(CAST(rev.rating AS double precision)), 0.0) 
    FROM reviews rev 
    WHERE rev.restaurant_id = r.id
); 