# Hibernate ve PostgreSQL JAR dosyalarını indirme scripti

# JAR dosyalarının indirileceği URL'ler
$jars = @{
    "antlr-2.7.7.jar" = "https://repo1.maven.org/maven2/antlr/antlr/2.7.7/antlr-2.7.7.jar"
    "byte-buddy-1.12.18.jar" = "https://repo1.maven.org/maven2/net/bytebuddy/byte-buddy/1.12.18/byte-buddy-1.12.18.jar"
    "classmate-1.5.1.jar" = "https://repo1.maven.org/maven2/com/fasterxml/classmate/1.5.1/classmate-1.5.1.jar"
    "hibernate-commons-annotations-5.1.2.Final.jar" = "https://repo1.maven.org/maven2/org/hibernate/common/hibernate-commons-annotations/5.1.2.Final/hibernate-commons-annotations-5.1.2.Final.jar"
    "hibernate-core-5.6.15.Final.jar" = "https://repo1.maven.org/maven2/org/hibernate/hibernate-core/5.6.15.Final/hibernate-core-5.6.15.Final.jar"
    "jandex-2.4.2.Final.jar" = "https://repo1.maven.org/maven2/org/jboss/jandex/2.4.2.Final/jandex-2.4.2.Final.jar"
    "javassist-3.28.0-GA.jar" = "https://repo1.maven.org/maven2/org/javassist/javassist/3.28.0-GA/javassist-3.28.0-GA.jar"
    "javax.persistence-api-2.2.jar" = "https://repo1.maven.org/maven2/javax/persistence/javax.persistence-api/2.2/javax.persistence-api-2.2.jar"
    "javax.transaction-api-1.3.jar" = "https://repo1.maven.org/maven2/javax/transaction/javax.transaction-api/1.3/javax.transaction-api-1.3.jar"
    "jboss-logging-3.4.3.Final.jar" = "https://repo1.maven.org/maven2/org/jboss/logging/jboss-logging/3.4.3.Final/jboss-logging-3.4.3.Final.jar"
    # JAXB JAR'ları
    "jaxb-api-2.3.1.jar" = "https://repo1.maven.org/maven2/javax/xml/bind/jaxb-api/2.3.1/jaxb-api-2.3.1.jar"
    "jaxb-runtime-2.3.1.jar" = "https://repo1.maven.org/maven2/org/glassfish/jaxb/jaxb-runtime/2.3.1/jaxb-runtime-2.3.1.jar"
    "istack-commons-runtime-3.0.7.jar" = "https://repo1.maven.org/maven2/com/sun/istack/istack-commons-runtime/3.0.7/istack-commons-runtime-3.0.7.jar"
    "activation-1.1.1.jar" = "https://repo1.maven.org/maven2/javax/activation/activation/1.1.1/activation-1.1.1.jar"
}

Write-Host "JAR dosyaları indiriliyor..."

# Her bir JAR dosyasını indir
foreach ($jar in $jars.GetEnumerator()) {
    $fileName = $jar.Key
    $url = $jar.Value
    
    Write-Host "İndiriliyor: $fileName"
    try {
        Invoke-WebRequest -Uri $url -OutFile $fileName
        Write-Host "Başarıyla indirildi: $fileName"
    }
    catch {
        Write-Host "Hata: $fileName indirilemedi"
        Write-Host $_.Exception.Message
    }
}

Write-Host "Tüm JAR dosyaları indirildi!" 