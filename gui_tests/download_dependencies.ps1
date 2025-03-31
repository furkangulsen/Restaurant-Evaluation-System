# Maven bağımlılıklarını indir
Write-Host "Maven bağımlılıkları indiriliyor..."
mvn dependency:copy-dependencies -DoutputDirectory=.

# Başarı mesajı
Write-Host "Bağımlılıklar başarıyla indirildi!" 