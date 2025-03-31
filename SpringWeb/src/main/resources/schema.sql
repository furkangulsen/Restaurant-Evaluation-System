-- Değerlendirme tablosunu kontrol et ve yoksa oluştur
CREATE TABLE IF NOT EXISTS reviews (
    id SERIAL PRIMARY KEY,
    comment TEXT,
    date TIMESTAMP,
    rating INTEGER NOT NULL,
    restaurant_id BIGINT,
    restaurant_name VARCHAR(255)
) WITH (OIDS=FALSE);

-- Restoran tablosunu kontrol et ve yoksa oluştur
CREATE TABLE IF NOT EXISTS restaurants (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE,
    location VARCHAR(255),
    rating DOUBLE PRECISION DEFAULT 0.0
) WITH (OIDS=FALSE);

-- Karakter kodlamasını ayarla (PostgreSQL'in desteklediği karşılaştırma kullanılıyor)
-- Türkçe karakter desteği için UTF-8 kullanılıyor
ALTER TABLE restaurants ALTER COLUMN name TYPE VARCHAR(255);
ALTER TABLE restaurants ALTER COLUMN location TYPE VARCHAR(255);
ALTER TABLE reviews ALTER COLUMN comment TYPE TEXT;
ALTER TABLE reviews ALTER COLUMN restaurant_name TYPE VARCHAR(255);

-- Restoran puanlarını değerlendirmelere göre güncelle
UPDATE restaurants AS r
SET rating = (
    SELECT COALESCE(AVG(rv.rating), 0.0)
    FROM reviews rv
    WHERE rv.restaurant_name = r.name
); 