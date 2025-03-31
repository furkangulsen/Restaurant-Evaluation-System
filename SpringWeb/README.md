# Restoran Değerlendirme Sistemi

Bu proje, kullanıcıların restoranlar hakkında değerlendirmeler yapabildiği ve puanlayabildiği bir web ve terminal tabanlı uygulamadır.

## Özellikler

- Web ve terminal üzerinden kullanılabilir arayüz
- Restoran ekleme, düzenleme, silme
- Değerlendirme ekleme, düzenleme, silme
- Puan sistemi (1-5 yıldız)
- Yalnızca-okunur REST API
- Eşzamanlı işleme (Concurrency) desteği
- Kapsamlı loglama
- Generic tipler ve koleksiyonlar

## Teknolojiler

- Java 17
- Spring Boot 3
- Spring MVC
- Spring Data JPA
- Thymeleaf (web arayüzü için)
- PostgreSQL (veritabanı)
- SLF4J / Logback (loglama)
- Maven (bağımlılık yönetimi)

## Başlarken

### Gereksinimler

- Java 17 veya üzeri
- Maven 3.6 veya üzeri
- PostgreSQL veritabanı

### Derleme ve Çalıştırma

Projeyi derlemek için:

```bash
mvn clean package
```

Web uygulaması olarak çalıştırmak için:

```bash
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

Terminal uygulaması olarak çalıştırmak için:

```bash
java -jar target/demo-0.0.1-SNAPSHOT.jar --spring.profiles.active=cli
```

Veya Windows'ta terminal uygulaması için:

```
start_cli.bat
```

## REST API Kullanımı

Aşağıdaki REST API endpoint'leri mevcuttur (sadece GET işlemleri):

### Restoranlar

- `GET /api/restaurants` - Tüm restoranları listele
- `GET /api/restaurants/{id}` - ID'ye göre restoran getir
- `GET /api/restaurants/search/name/{name}` - İsme göre restoran ara
- `GET /api/restaurants/search/rating/{rating}` - Minimum puana göre restoranları filtrele
- `GET /api/restaurants/search/location/{location}` - Konuma göre restoranları filtrele
- `GET /api/restaurants/stats` - Restoran istatistiklerini getir

### Değerlendirmeler

- `GET /api/reviews` - Tüm değerlendirmeleri listele
- `GET /api/reviews/{id}` - ID'ye göre değerlendirme getir
- `GET /api/reviews/restaurant/{restaurantName}` - Restoran adına göre değerlendirmeleri listele
- `GET /api/reviews/search/rating/{rating}` - Minimum puana göre değerlendirmeleri filtrele
- `GET /api/reviews/stats` - Değerlendirme istatistiklerini getir

## Proje Yapısı

```
.
├── src/main/java/com/example/demo/
│   ├── cli/                    # Terminal arayüzü
│   ├── controller/             # Web kontrolcüleri
│   │   └── api/                # REST API kontrolcüleri
│   ├── model/                  # Veri modelleri
│   ├── repository/             # Veritabanı erişim katmanı
│   ├── service/                # İş mantığı
│   └── util/                   # Yardımcı sınıflar
└── src/main/resources/
    ├── application.properties  # Uygulama ayarları
    ├── application-cli.properties # CLI profili ayarları
    └── templates/              # Thymeleaf şablonları
```

## Generic Tipler ve Koleksiyonlar

Projede, generic tiplerle ilgili temel bileşenler:

- `GenericService<T, ID>` - Tüm servisler için ortak arayüz
- `RestaurantService`, `ReviewService` - Generic servis uygulamaları
- `List<Restaurant>`, `List<Review>` - Entity listeleri
- `Optional<T>` - Null kontrolü için wrapper

## Concurrency Kullanımı

Projede, eşzamanlı işleme için:

- `ConcurrencyManager` - Thread havuzu yönetimi
- `CompletableFuture<T>` - Asenkron işlemler
- `ThreadPoolExecutor` - Özelleştirilmiş thread havuzu 