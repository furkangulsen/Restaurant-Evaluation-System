# Restoran Değerlendirme Sistemi

Bu uygulama, restoranları ve değerlendirmeleri yönetmek için PostgreSQL veritabanı kullanan bir terminal uygulamasıdır.

## Gereksinimler

- Java 8 veya üzeri
- PostgreSQL 10 veya üzeri
- Hibernate ORM
- PostgreSQL JDBC Sürücüsü

## Veritabanı Kurulumu

1. PostgreSQL'i başlatın:
   ```
   psql -U postgres
   ```

2. `restaurant_db` veritabanını oluşturun:
   ```sql
   CREATE DATABASE restaurant_db;
   ```

3. Veritabanına bağlanın:
   ```
   \c restaurant_db
   ```

4. `create_database.sql` dosyasındaki SQL komutlarını çalıştırın:
   ```
   psql -U postgres -d restaurant_db -f create_database.sql
   ```

## Hibernate Yapılandırması

`src/hibernate.cfg.xml` dosyasında veritabanı bağlantı bilgilerinizi kontrol edin ve gerekirse güncelleyin:

```xml
<property name="hibernate.connection.url">jdbc:postgresql://localhost:5432/restaurant_db</property>
<property name="hibernate.connection.username">postgres</property>
<property name="hibernate.connection.password">123456789</property>
```

## Uygulamayı Çalıştırma

1. Projeyi derleyin
2. `TerminalApp` sınıfını çalıştırın

## Özellikler

- Restoranları listeleme
- Restoran ekleme, düzenleme ve silme
- Değerlendirme ekleme ve görüntüleme
- Veritabanı tabanlı veri saklama

## Veritabanı Şeması

### restaurants Tablosu
- `id`: Otomatik artan birincil anahtar
- `name`: Restoran adı (benzersiz)
- `location`: Restoran konumu
- `rating`: Restoran puanı (1-5 arası)
- `address`: Restoran adresi

### reviews Tablosu
- `id`: Otomatik artan birincil anahtar
- `restaurant_id`: Restoran tablosuna referans (yabancı anahtar)
- `restaurant_name`: Değerlendirilen restoran adı
- `comment`: Değerlendirme yorumu
- `rating`: Değerlendirme puanı (1-5 arası)
- `date`: Değerlendirme tarihi

## Notlar

- Uygulama, veritabanı bağlantısı için Hibernate ORM kullanır
- Tüm veriler PostgreSQL veritabanında saklanır
- Değerlendirmeler eklendiğinde, ilgili restoranın ortalama puanı otomatik olarak güncellenir 