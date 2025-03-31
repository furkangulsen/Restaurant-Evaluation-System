package gui_testleri;

/**
 * Veri doğrulama işlemleri sırasında oluşan hataları temsil eden exception sınıfı.
 */
public class ValidationException extends AppException {
    private static final long serialVersionUID = 1L;
    
    /**
     * Doğrulama hatalarını detaylandıran alt kodlar
     */
    public enum ValidationErrorCode {
        REQUIRED_FIELD(3001, "Zorunlu alan hatası"),
        INVALID_FORMAT(3002, "Geçersiz format"),
        INVALID_RANGE(3003, "Geçersiz aralık"),
        INVALID_LENGTH(3004, "Geçersiz uzunluk"),
        INVALID_TYPE(3005, "Geçersiz tür");
        
        private final int code;
        private final String defaultMessage;
        
        ValidationErrorCode(int code, String defaultMessage) {
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
    private final ValidationErrorCode validationErrorCode;
    
    // Hatanın oluştuğu alan
    private final String fieldName;

    /**
     * Constructor
     * 
     * @param validationErrorCode Doğrulama hata kodu
     * @param fieldName Hatanın oluştuğu alan
     */
    public ValidationException(ValidationErrorCode validationErrorCode, String fieldName) {
        super(ErrorCode.VALIDATION_ERROR, validationErrorCode.getDefaultMessage() + ": " + fieldName);
        this.validationErrorCode = validationErrorCode;
        this.fieldName = fieldName;
    }
    
    /**
     * Constructor
     * 
     * @param validationErrorCode Doğrulama hata kodu
     * @param fieldName Hatanın oluştuğu alan
     * @param message Hata mesajı
     */
    public ValidationException(ValidationErrorCode validationErrorCode, String fieldName, String message) {
        super(ErrorCode.VALIDATION_ERROR, message);
        this.validationErrorCode = validationErrorCode;
        this.fieldName = fieldName;
    }
    
    /**
     * Doğrulama hata kodunu döndürür
     * 
     * @return Doğrulama hata kodu
     */
    public ValidationErrorCode getValidationErrorCode() {
        return validationErrorCode;
    }
    
    /**
     * Doğrulama hata kodunun sayısal değerini döndürür
     * 
     * @return Sayısal doğrulama hata kodu
     */
    public int getValidationErrorCodeValue() {
        return validationErrorCode.getCode();
    }
    
    /**
     * Hatanın oluştuğu alanı döndürür
     * 
     * @return Alan adı
     */
    public String getFieldName() {
        return fieldName;
    }
    
    @Override
    public String toString() {
        return "ValidationException [validationErrorCode=" + validationErrorCode.name() + 
               " (" + validationErrorCode.getCode() + "), fieldName=" + fieldName + 
               ", message=" + getMessage() + "]";
    }
} 