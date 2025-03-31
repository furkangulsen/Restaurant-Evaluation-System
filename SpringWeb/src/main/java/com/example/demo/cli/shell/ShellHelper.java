package com.example.demo.cli.shell;

import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Spring Shell CLI için yardımcı sınıf.
 * Renkli ve stilize edilmiş çıktılar oluşturmak için kullanılır.
 * Not: Sadece CLI profilinde aktiftir.
 */
@Component
@Profile("cli")
public class ShellHelper {

    private Terminal terminal;

    @Value("${shell.out.info:CYAN}")
    public String infoColor;
    
    @Value("${shell.out.success:GREEN}")
    public String successColor;
    
    @Value("${shell.out.warning:YELLOW}")
    public String warningColor;
    
    @Value("${shell.out.error:RED}")
    public String errorColor;

    @Autowired(required = false) // Terminal bulunamazsa da çalışabilmesi için
    public ShellHelper(@Qualifier("customTerminal") Terminal terminal) {
        this.terminal = terminal;
    }
    
    // Fallback constructor - Terminal bean'i yoksa
    public ShellHelper() {
        this.terminal = null;
    }

    /**
     * Verilen metni belirtilen renkte döndürür
     */
    public String getColored(String message, PromptColor color) {
        if (terminal != null) {
            return new AttributedString(message, AttributedStyle.DEFAULT.foreground(color.toJLineAttributedStyle()))
                    .toAnsi();
        } else {
            return message; // Terminal yoksa düz metin döndür
        }
    }
    
    /**
     * Terminal rengi desteği olup olmadığını kontrol eder
     */
    public boolean isTerminalColorSupported() {
        return terminal != null;
    }

    /**
     * Bilgi mesajı formatında renkli metin döndürür
     */
    public String getInfoMessage(String message) {
        return getColored(message, PromptColor.valueOf(infoColor));
    }

    /**
     * Başarı mesajı formatında renkli metin döndürür
     */
    public String getSuccessMessage(String message) {
        return getColored(message, PromptColor.valueOf(successColor));
    }

    /**
     * Uyarı mesajı formatında renkli metin döndürür
     */
    public String getWarningMessage(String message) {
        return getColored(message, PromptColor.valueOf(warningColor));
    }

    /**
     * Hata mesajı formatında renkli metin döndürür
     */
    public String getErrorMessage(String message) {
        return getColored(message, PromptColor.valueOf(errorColor));
    }
    
    /**
     * Terminal genişliğini alır
     */
    public int getTerminalWidth() {
        return terminal.getWidth();
    }
    
    /**
     * Terminale renkli ve yan yana bilgiler basar
     */
    public String getInfoTable(String label, String value) {
        return String.format("%-15s: %s", getInfoMessage(label), value);
    }
    
    /**
     * Terminal için prompt oluşturur
     */
    public String prompt(String prompt) {
        return getColored(prompt + ": ", PromptColor.valueOf(infoColor));
    }
}
