package gui_testleri;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Uygulama genelinde kullanılacak loglama mekanizmasını sağlayan sınıf.
 * Java logging API ve dosya bazlı loglama özelliklerini içerir.
 */
public class AppLogger {
    
    // Singleton instance
    private static AppLogger instance;
    
    // Java Logger
    private Logger logger;
    
    // Log dosyası yazıcısı
    private PrintWriter logFileWriter;
    
    // Log seviyesi
    private Level logLevel = Level.INFO;
    
    // Log dosyası adı
    private static final String LOG_FILE = "application.log";
    
    // Log klasörü
    private static final String LOG_DIRECTORY = "logs";
    
    /**
     * Private constructor - Singleton pattern
     */
    private AppLogger() {
        try {
            // Log klasörünün varlığını kontrol et, yoksa oluştur
            File logDir = new File(LOG_DIRECTORY);
            if (!logDir.exists()) {
                logDir.mkdir();
            }
            
            // Java Logger'ı yapılandır
            logger = Logger.getLogger("RestaurantApp");
            logger.setUseParentHandlers(false);
            
            // Console Handler
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(logLevel);
            logger.addHandler(consoleHandler);
            
            // File Handler
            FileHandler fileHandler = new FileHandler(LOG_DIRECTORY + File.separator + "java_" + LOG_FILE, true);
            fileHandler.setFormatter(new SimpleFormatter());
            fileHandler.setLevel(logLevel);
            logger.addHandler(fileHandler);
            
            logger.setLevel(logLevel);
            
            // Özel log dosyası yazıcısı
            logFileWriter = new PrintWriter(new FileWriter(LOG_DIRECTORY + File.separator + LOG_FILE, true), true);
            
            info("AppLogger initialized successfully");
        } catch (IOException e) {
            System.err.println("Error initializing AppLogger: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Singleton instance getter
     * 
     * @return AppLogger instance
     */
    public static synchronized AppLogger getInstance() {
        if (instance == null) {
            instance = new AppLogger();
        }
        return instance;
    }
    
    /**
     * Logger'ı kapatır ve kaynakları serbest bırakır
     */
    public void close() {
        if (logFileWriter != null) {
            logFileWriter.close();
        }
        
        for (Handler handler : logger.getHandlers()) {
            handler.close();
        }
    }
    
    /**
     * Log seviyesini değiştirir
     * 
     * @param newLevel Yeni log seviyesi
     */
    public void setLogLevel(Level newLevel) {
        this.logLevel = newLevel;
        logger.setLevel(newLevel);
        
        for (Handler handler : logger.getHandlers()) {
            handler.setLevel(newLevel);
        }
    }
    
    /**
     * INFO seviyesinde log kaydı oluşturur
     * 
     * @param message Log mesajı
     */
    public void info(String message) {
        logger.info(message);
        writeToLogFile("INFO", message);
    }
    
    /**
     * WARNING seviyesinde log kaydı oluşturur
     * 
     * @param message Log mesajı
     */
    public void warning(String message) {
        logger.warning(message);
        writeToLogFile("WARNING", message);
    }
    
    /**
     * SEVERE seviyesinde log kaydı oluşturur
     * 
     * @param message Log mesajı
     */
    public void error(String message) {
        logger.severe(message);
        writeToLogFile("ERROR", message);
    }
    
    /**
     * Exception ile birlikte SEVERE seviyesinde log kaydı oluşturur
     * 
     * @param message Log mesajı
     * @param e Exception
     */
    public void error(String message, Exception e) {
        logger.log(Level.SEVERE, message, e);
        writeToLogFile("ERROR", message + " - Exception: " + e.getMessage());
    }
    
    /**
     * Throwable ile birlikte SEVERE seviyesinde log kaydı oluşturur
     * 
     * @param message Log mesajı
     * @param t Throwable
     */
    public void error(String message, Throwable t) {
        logger.log(Level.SEVERE, message, t);
        writeToLogFile("ERROR", message + " - Throwable: " + t.getMessage());
    }
    
    /**
     * DEBUG seviyesinde log kaydı oluşturur
     * 
     * @param message Log mesajı
     */
    public void debug(String message) {
        logger.fine(message);
        if (logLevel.intValue() <= Level.FINE.intValue()) {
            writeToLogFile("DEBUG", message);
        }
    }
    
    /**
     * Özel log dosyasına yazma işlemi
     * 
     * @param level Log seviyesi
     * @param message Log mesajı
     */
    private void writeToLogFile(String level, String message) {
        if (logFileWriter != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            String timestamp = dateFormat.format(new Date());
            logFileWriter.println(timestamp + " [" + level + "] " + message);
        }
    }
} 