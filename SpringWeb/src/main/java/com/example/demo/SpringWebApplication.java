package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.example.demo.gui.BasitGui;

/**
 * Web uygulaması için başlangıç sınıfı.
 * Bu sınıf DemoApplication'ı kullanarak uygulamayı başlatır.
 */
@SpringBootApplication
public class SpringWebApplication {

	public static void main(String[] args) {
		// Komut satırı parametrelerini kontrol et
		boolean startGui = false;
		
		for (String arg : args) {
			if (arg.equals("--gui")) {
				startGui = true;
				break;
			}
		}
		
		// Eğer --gui parametresi verilmişse, GUI'yi başlat
		if (startGui) {
			BasitGui.main(args);
		}
		// Eğer doğrudan bu sınıf çalıştırıldıysa ve argüman yoksa, başlatıcıyı göster
		else if (args.length == 0) {
			BasitGui.main(args);
		} else {
			// Ana uygulama sınıfını kullanarak Spring Boot uygulamasını başlat
			DemoApplication.main(args);
		}
	}

}
