-- Restoranların puanlarını değerlendirmelere göre güncelle
UPDATE public.restaurants AS r
SET rating = (
    SELECT COALESCE(AVG(CAST(rv.rating AS double precision)), 0.0)
    FROM reviews rv
    WHERE rv.restaurant_name = r.name
);

-- Değerlendirme sayısını console çıktısı için göster
SELECT r.name, r.rating, COUNT(rv.id) as review_count
FROM public.restaurants r
LEFT JOIN reviews rv ON rv.restaurant_name = r.name
GROUP BY r.name, r.rating
ORDER BY r.name; 