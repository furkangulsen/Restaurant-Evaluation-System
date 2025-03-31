package gui_testleri;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Uygulama genelinde exception handling işlemlerini yöneten utility sınıfı.
 */
public class ExceptionHandler {
    
    // Singleton instance
    private static ExceptionHandler instance;
    
    // Logger
    private final AppLogger logger;
    
    /**
     * Private constructor - Singleton pattern
     */
    private ExceptionHandler() {
        this.logger = AppLogger.getInstance();
        logger.info("ExceptionHandler initialized");
    }
    
    /**
     * Singleton instance getter
     * 
     * @return ExceptionHandler instance
     */
    public static synchronized ExceptionHandler getInstance() {
        if (instance == null) {
            instance = new ExceptionHandler();
        }
        return instance;
    }
    
    /**
     * Exception'ı yakalar, loglar ve kullanıcıya bilgi verir
     * 
     * @param e Exception
     */
    public void handleException(Exception e) {
        if (e instanceof AppException) {
            handleAppException((AppException) e);
        } else {
            handleGenericException(e);
        }
    }
    
    /**
     * AppException ve türevlerini işler
     * 
     * @param e AppException
     */
    public void handleAppException(AppException e) {
        // Log exception (AppException zaten kendi içinde loglama yapıyor)
        
        // İşlem türüne göre farklı işlemler yap
        switch (e.getErrorCode()) {
            case DATABASE_ERROR:
                handleDatabaseException(e);
                break;
            case VALIDATION_ERROR:
                handleValidationException(e);
                break;
            case IO_ERROR:
                handleIOException(e);
                break;
            case NETWORK_ERROR:
                handleNetworkException(e);
                break;
            default:
                showErrorDialog("Hata", e.getMessage());
                break;
        }
    }
    
    /**
     * Veritabanı hatalarını işler
     * 
     * @param e AppException (veritabanı hatası)
     */
    private void handleDatabaseException(AppException e) {
        String title = "Veritabanı Hatası";
        String message = e.getMessage();
        
        if (e instanceof DatabaseException) {
            DatabaseException dbException = (DatabaseException) e;
            
            switch (dbException.getDatabaseErrorCode()) {
                case CONNECTION_ERROR:
                    message = "Veritabanına bağlanılamadı: " + dbException.getMessage();
                    break;
                case QUERY_ERROR:
                    message = "Sorgu hatası: " + dbException.getMessage();
                    break;
                case TRANSACTION_ERROR:
                    message = "İşlem hatası: " + dbException.getMessage();
                    break;
                case DATA_INTEGRITY_ERROR:
                    message = "Veri bütünlüğü hatası: " + dbException.getMessage();
                    break;
                case DUPLICATE_ENTRY:
                    message = "Bu kayıt zaten mevcut: " + dbException.getMessage();
                    break;
                default:
                    message = "Veritabanı hatası: " + dbException.getMessage();
                    break;
            }
        }
        
        showErrorDialog(title, message);
    }
    
    /**
     * Doğrulama hatalarını işler
     * 
     * @param e AppException (doğrulama hatası)
     */
    private void handleValidationException(AppException e) {
        String title = "Doğrulama Hatası";
        String message = e.getMessage();
        
        if (e instanceof ValidationException) {
            ValidationException valException = (ValidationException) e;
            
            switch (valException.getValidationErrorCode()) {
                case REQUIRED_FIELD:
                    message = "Zorunlu alan hatası: " + valException.getFieldName() + " alanı boş olamaz.";
                    break;
                case INVALID_FORMAT:
                    message = "Geçersiz format: " + valException.getFieldName() + " alanı için geçersiz format.";
                    break;
                case INVALID_RANGE:
                    message = "Geçersiz aralık: " + valException.getFieldName() + " alanı için geçersiz değer.";
                    break;
                case INVALID_LENGTH:
                    message = "Geçersiz uzunluk: " + valException.getFieldName() + " alanı için geçersiz uzunluk.";
                    break;
                case INVALID_TYPE:
                    message = "Geçersiz tür: " + valException.getFieldName() + " alanı için geçersiz tür.";
                    break;
                default:
                    message = "Doğrulama hatası: " + valException.getMessage();
                    break;
            }
        }
        
        showWarningDialog(title, message);
    }
    
    /**
     * IO hatalarını işler
     * 
     * @param e AppException (IO hatası)
     */
    private void handleIOException(AppException e) {
        showErrorDialog("Dosya İşlem Hatası", "Dosya işlemi sırasında bir hata oluştu: " + e.getMessage());
    }
    
    /**
     * Ağ hatalarını işler
     * 
     * @param e AppException (ağ hatası)
     */
    private void handleNetworkException(AppException e) {
        showErrorDialog("Ağ Hatası", "Ağ bağlantısı sırasında bir hata oluştu: " + e.getMessage());
    }
    
    /**
     * Generic exception'ları işler
     * 
     * @param e Exception
     */
    public void handleGenericException(Exception e) {
        // Log exception
        logger.error("Unhandled exception", e);
        
        // Generate a unique error code for tracking
        String errorCode = generateErrorCode();
        
        // Show error dialog
        showErrorDialog("Sistem Hatası", 
                "Beklenmeyen bir hata oluştu. Hata kodu: " + errorCode + "\n" +
                "Lütfen bu kodu sistem yöneticisine bildirin.\n\n" +
                "Hata detayı: " + e.getMessage());
    }
    
    /**
     * Generic Throwable'ları işler
     * 
     * @param t Throwable
     */
    public void handleGenericException(Throwable t) {
        // Log exception
        logger.error("Unhandled throwable", t);
        
        // Generate a unique error code for tracking
        String errorCode = generateErrorCode();
        
        // Show error dialog
        showErrorDialog("Sistem Hatası", 
                "Beklenmeyen bir hata oluştu. Hata kodu: " + errorCode + "\n" +
                "Lütfen bu kodu sistem yöneticisine bildirin.\n\n" +
                "Hata detayı: " + t.getMessage());
    }
    
    /**
     * Izleme için benzersiz bir hata kodu oluşturur
     * 
     * @return Benzersiz hata kodu
     */
    private String generateErrorCode() {
        return "ERR-" + System.currentTimeMillis() % 10000;
    }
    
    /**
     * Hata dialog'u gösterir
     * 
     * @param title Dialog başlığı
     * @param message Dialog mesajı
     */
    public void showErrorDialog(String title, String message) {
        try {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
            });
        } catch (Exception e) {
            // Dialog gösterilemiyorsa, konsola yaz
            System.err.println("ERROR - " + title + ": " + message);
        }
    }
    
    /**
     * Uyarı dialog'u gösterir
     * 
     * @param title Dialog başlığı
     * @param message Dialog mesajı
     */
    public void showWarningDialog(String title, String message) {
        try {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null, message, title, JOptionPane.WARNING_MESSAGE);
            });
        } catch (Exception e) {
            // Dialog gösterilemiyorsa, konsola yaz
            System.err.println("WARNING - " + title + ": " + message);
        }
    }
    
    /**
     * Bilgi dialog'u gösterir
     * 
     * @param title Dialog başlığı
     * @param message Dialog mesajı
     */
    public void showInfoDialog(String title, String message) {
        try {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
            });
        } catch (Exception e) {
            // Dialog gösterilemiyorsa, konsola yaz
            System.out.println("INFO - " + title + ": " + message);
        }
    }
} 