# Yiyecek Mekanları Değerlendirme Sistemi

Bu uygulama, restoranları listelemenize, eklemenize, düzenlemenize, silmenize ve değerlendirmenize olanak tanır. Hem grafiksel kullanıcı arayüzü (GUI) hem de konsol arayüzü sunar.

## Özellikler

- Restoran ekleme, düzenleme ve silme
- Restoranları puanlama ve değerlendirme
- Restoran değerlendirmelerini görüntüleme
- Hem GUI hem de konsol arayüzü

## Başlatma

Uygulamayı iki farklı modda başlatabilirsiniz:

### GUI Modu

GUI modunda başlatmak için:

```
java -jar gui_testleri.jar
```

veya argüman olmadan:

```
java gui_testleri.Main
```

### Konsol Modu

Konsol modunda başlatmak için:

```
java -jar gui_testleri.jar --console
```

veya:

```
java gui_testleri.Main --console
```

## Konsol Menüsü Kullanımı

Konsol menüsünde aşağıdaki işlemleri yapabilirsiniz:

1. Restoranları Listele
2. Restoran Ekle
3. Restoran Düzenle
4. Restoran Sil
5. Değerlendirmeleri Göster
6. Değerlendirme Ekle
0. Çıkış

İstediğiniz işlemin numarasını girerek o işlemi gerçekleştirebilirsiniz.

## Gereksinimler

- Java 8 veya üzeri
- FlatLaf kütüphanesi (GUI teması için)

## Notlar

- Konsol menüsünden yapılan değişiklikler GUI'ye de yansır ve tam tersi.
- Veriler "restaurants.txt" ve "reviews.txt" dosyalarında saklanır. 