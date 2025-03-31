package gui_testleri;

/**
 * Uygulama genelinde kullanılacak temel exception sınıfı.
 * Tüm özel exception'lar bu sınıftan türetilmelidir.
 */
public class AppException extends Exception {
    private static final long serialVersionUID = 1L;
    
    /**
     * Error kodunu tanımlayan sabitler
     */
    public enum ErrorCode {
        GENERAL_ERROR(1000, "Genel hata"),
        DATABASE_ERROR(2000, "Veritabanı hatası"),
        VALIDATION_ERROR(3000, "Doğrulama hatası"),
        IO_ERROR(4000, "Giriş/Çıkış hatası"),
        NETWORK_ERROR(5000, "Ağ hatası");
        
        private final int code;
        private final String defaultMessage;
        
        ErrorCode(int code, String defaultMessage) {
            this.code = code;
            this.defaultMessage = defaultMessage;
        }
        
        public int getCode() {
            return code;
        }
        
        public String getDefaultMessage() {
            return defaultMessage;
        }
    }
    
    // Hata kodu
    private final ErrorCode errorCode;
    
    /**
     * Constructor
     * 
     * @param errorCode Hata kodu
     */
    public AppException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
        
        // Log exception
        AppLogger.getInstance().error("AppException: " + errorCode.name() + " (" + errorCode.getCode() + "): " + errorCode.getDefaultMessage());
    }
    
    /**
     * Constructor
     * 
     * @param errorCode Hata kodu
     * @param message Hata mesajı
     */
    public AppException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        
        // Log exception
        AppLogger.getInstance().error("AppException: " + errorCode.name() + " (" + errorCode.getCode() + "): " + message);
    }
    
    /**
     * Constructor
     * 
     * @param errorCode Hata kodu
     * @param message Hata mesajı
     * @param cause Hatanın nedeni
     */
    public AppException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        
        // Log exception
        AppLogger.getInstance().error("AppException: " + errorCode.name() + " (" + errorCode.getCode() + "): " + message + " - Caused by: " + cause.getMessage());
    }
    
    /**
     * Constructor
     * 
     * @param errorCode Hata kodu
     * @param cause Hatanın nedeni
     */
    public AppException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getDefaultMessage(), cause);
        this.errorCode = errorCode;
        
        // Log exception
        AppLogger.getInstance().error("AppException: " + errorCode.name() + " (" + errorCode.getCode() + "): " + errorCode.getDefaultMessage() + " - Caused by: " + cause.getMessage());
    }
    
    /**
     * Hata kodunu döndürür
     * 
     * @return Hata kodu
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }
    
    /**
     * Hata kodunun sayısal değerini döndürür
     * 
     * @return Sayısal hata kodu
     */
    public int getErrorCodeValue() {
        return errorCode.getCode();
    }
    
    @Override
    public String toString() {
        return "AppException [errorCode=" + errorCode.name() + 
               " (" + errorCode.getCode() + "), message=" + getMessage() + "]";
    }
} 