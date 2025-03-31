package gui_testleri;

/**
 * Veritabanı işlemleri sırasında oluşan hataları temsil eden exception sınıfı.
 */
public class DatabaseException extends AppException {
    private static final long serialVersionUID = 1L;
    
    /**
     * Veritabanı hatalarını detaylandıran alt kodlar
     */
    public enum DatabaseErrorCode {
        CONNECTION_ERROR(2001, "Veritabanı bağlantı hatası"),
        QUERY_ERROR(2002, "Sorgu hatası"),
        TRANSACTION_ERROR(2003, "İşlem hatası"),
        DATA_INTEGRITY_ERROR(2004, "Veri bütünlüğü hatası"),
        DUPLICATE_ENTRY(2005, "Yinelenen kayıt hatası");
        
        private final int code;
        private final String defaultMessage;
        
        DatabaseErrorCode(int code, String defaultMessage) {
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
    
    // Alt hata kodu
    private final DatabaseErrorCode databaseErrorCode;

    /**
     * Constructor
     * 
     * @param databaseErrorCode Veritabanı hata kodu
     */
    public DatabaseException(DatabaseErrorCode databaseErrorCode) {
        super(ErrorCode.DATABASE_ERROR, databaseErrorCode.getDefaultMessage());
        this.databaseErrorCode = databaseErrorCode;
    }
    
    /**
     * Constructor
     * 
     * @param databaseErrorCode Veritabanı hata kodu
     * @param message Hata mesajı
     */
    public DatabaseException(DatabaseErrorCode databaseErrorCode, String message) {
        super(ErrorCode.DATABASE_ERROR, message);
        this.databaseErrorCode = databaseErrorCode;
    }
    
    /**
     * Constructor
     * 
     * @param databaseErrorCode Veritabanı hata kodu
     * @param message Hata mesajı
     * @param cause Hatanın nedeni
     */
    public DatabaseException(DatabaseErrorCode databaseErrorCode, String message, Throwable cause) {
        super(ErrorCode.DATABASE_ERROR, message, cause);
        this.databaseErrorCode = databaseErrorCode;
    }
    
    /**
     * Constructor
     * 
     * @param databaseErrorCode Veritabanı hata kodu
     * @param cause Hatanın nedeni
     */
    public DatabaseException(DatabaseErrorCode databaseErrorCode, Throwable cause) {
        super(ErrorCode.DATABASE_ERROR, databaseErrorCode.getDefaultMessage(), cause);
        this.databaseErrorCode = databaseErrorCode;
    }
    
    /**
     * Veritabanı hata kodunu döndürür
     * 
     * @return Veritabanı hata kodu
     */
    public DatabaseErrorCode getDatabaseErrorCode() {
        return databaseErrorCode;
    }
    
    /**
     * Veritabanı hata kodunun sayısal değerini döndürür
     * 
     * @return Sayısal veritabanı hata kodu
     */
    public int getDatabaseErrorCodeValue() {
        return databaseErrorCode.getCode();
    }
    
    @Override
    public String toString() {
        return "DatabaseException [databaseErrorCode=" + databaseErrorCode.name() + 
               " (" + databaseErrorCode.getCode() + "), message=" + getMessage() + "]";
    }
} 