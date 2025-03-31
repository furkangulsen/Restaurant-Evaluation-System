package com.example.demo.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Map;

/**
 * Restoran Değerlendirme Sistemi - Modern Başlatıcı
 * Bu sınıf, Web ve CLI uygulamalarını başlatmak için kullanıcı dostu bir arayüz sağlar.
 */
public class BasitGui extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final int WEB_PORT = 8080;
    private static final int TIMEOUT_SECONDS = 60;
    private static final String WEB_URL = "http://localhost:" + WEB_PORT;
    
    // UI bileşenleri
    private JTextArea logArea;
    private JProgressBar progressBar;
    private JButton webButton;
    private JButton cliButton;
    private JLabel statusLabel;
    
    // İşlem yönetimi
    private Process currentProcess;
    private final AtomicBoolean appStarted = new AtomicBoolean(false);
    private ScheduledExecutorService scheduler;
    
    /**
     * Ana constructor - GUI'yi oluşturur
     */
    public BasitGui() {
        setupLookAndFeel();
        setupWindowProperties();
        
        JPanel mainPanel = createMainPanel();
        add(mainPanel);
    }
    
    /**
     * Look and Feel ayarlarını yapar
     */
    private void setupLookAndFeel() {
        try {
            // Modern görünüm için sistem L&F kullan
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
            // Yazı tipi yumuşatma ayarları
            System.setProperty("awt.useSystemAAFontSettings", "on");
            System.setProperty("swing.aatext", "true");
        } catch (Exception e) {
            System.err.println("Look and Feel ayarlanamadı: " + e.getMessage());
        }
    }
    
    /**
     * Pencere özelliklerini ayarlar
     */
    private void setupWindowProperties() {
        setTitle("Restoran Değerlendirme Sistemi");
        setSize(700, 550);
        setMinimumSize(new Dimension(600, 450));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Kapanış olayını yakalamak ve işlemleri sonlandırmak için
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                killCurrentProcess();
            }
        });
    }
    
    /**
     * Ana panel oluşturur
     */
    private JPanel createMainPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(new Color(240, 240, 245));
        
        // Panel içeriğini oluştur
        panel.add(createHeaderPanel(), BorderLayout.NORTH);
        panel.add(createContentPanel(), BorderLayout.CENTER);
        panel.add(createStatusPanel(), BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Başlık paneli oluşturur
     */
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(new Color(240, 240, 245));
        panel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        // Başlık
        JLabel titleLabel = new JLabel("Restoran Değerlendirme Sistemi", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(70, 70, 70));
        
        // Logo/ikon paneli
        JPanel iconPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        iconPanel.setBackground(new Color(240, 240, 245));
        
        // Uygulama ikonu
        JLabel iconLabel = new JLabel(createApplicationIcon());
        iconPanel.add(iconLabel);
        
        // Ana panele ekle
        panel.add(iconPanel, BorderLayout.CENTER);
        panel.add(titleLabel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Uygulama ikonu oluşturur
     */
    private ImageIcon createApplicationIcon() {
        // Basit bir ikon oluştur
        int size = 48;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        
        // Kaliteli çizim için antialiasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Arkaplan oluştur
        g2d.setColor(new Color(46, 204, 113));
        g2d.fillRoundRect(0, 0, size, size, 10, 10);
        
        // "R" harfi
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 32));
        g2d.drawString("R", 15, 35);
        
        g2d.dispose();
        return new ImageIcon(img);
    }
    
    /**
     * Ana içerik paneli oluşturur
     */
    private JPanel createContentPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBackground(new Color(240, 240, 245));
        
        // Buton paneli
        JPanel buttonPanel = createButtonPanel();
        
        // Log paneli
        JPanel logPanel = createLogPanel();
        
        // İçerik paneline ekle
        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(logPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Butonlar paneli oluşturur
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 1, 0, 15));
        panel.setBackground(new Color(240, 240, 245));
        
        // Web Uygulama Butonu
        webButton = createStyledButton(
                "Web Uygulamasını Başlat", 
                "Spring Boot web arayüzünü başlatır", 
                new Color(76, 175, 80)
        );
        webButton.addActionListener(e -> runBatchFile("start_web_service.sh"));
        
        // CLI Uygulama Butonu
        cliButton = createStyledButton(
                "CLI Uygulamasını Başlat", 
                "Komut satırı uygulamasını başlatır", 
                new Color(33, 150, 243)
        );
        cliButton.addActionListener(e -> runBatchFile("start_cli_service.sh"));
        
        // Ekle
        panel.add(webButton);
        panel.add(cliButton);
        
        return panel;
    }
    
    /**
     * Stilize edilmiş buton oluşturur
     */
    private JButton createStyledButton(String title, String description, Color themeColor) {
        JButton button = new JButton();
        button.setLayout(new BorderLayout(10, 0));
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(themeColor, 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        button.setBackground(Color.WHITE);
        button.setFocusPainted(false);
        
        // İkon oluştur
        JLabel iconLabel = new JLabel(createButtonIcon(themeColor));
        iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 10));
        button.add(iconLabel, BorderLayout.WEST);
        
        // Başlık ve açıklama paneli
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(new Color(100, 100, 100));
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(3));
        textPanel.add(descLabel);
        
        button.add(textPanel, BorderLayout.CENTER);
        
        // Hover efektleri
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(245, 245, 245));
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(Color.WHITE);
                button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });
        
        return button;
    }
    
    /**
     * Buton ikonu oluşturur
     */
    private ImageIcon createButtonIcon(Color color) {
        int size = 32;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Dairesel arkaplan
        g2d.setColor(color);
        g2d.fillOval(0, 0, size, size);
        
        // İkon içindeki sembol
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2));
        
        // Basit bir "oynat" ikonu
        int[] xPoints = {10, 10, 22};
        int[] yPoints = {8, 24, 16};
        g2d.fillPolygon(xPoints, yPoints, 3);
        
        g2d.dispose();
        return new ImageIcon(img);
    }
    
    /**
     * Durum paneli oluşturur
     */
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setBackground(new Color(240, 240, 245));
        
        // Durum etiketi
        statusLabel = new JLabel("Hazır");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        // İlerleme çubuğu
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        
        panel.add(statusLabel, BorderLayout.WEST);
        panel.add(progressBar, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * Log paneli oluşturur
     */
    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setBackground(new Color(240, 240, 245));
        
        // Başlık
        JLabel logTitleLabel = new JLabel("İşlem Çıktısı");
        logTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        // Log alanı
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setBackground(new Color(245, 245, 245));
        logArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setPreferredSize(new Dimension(680, 200));
        
        panel.add(logTitleLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Shell script'i çalıştırır
     */
    private void runBatchFile(String scriptName) {
        // Önceki işlem çalışıyorsa sonlandır
        if (isProcessRunning()) {
            killCurrentProcess();
        }
        
        // İlgili işleve yönlendir
        if (scriptName.equals("start_web_service.sh")) {
            startWebApplication();
        } else if (scriptName.equals("start_cli_service.sh")) {
            startCliApplication();
        }
    }
    
    /**
     * Web uygulamasını başlatır - shell script ile
     */
    private void startWebApplication() {
        // Önceki işlem çalışıyorsa sonlandır
        if (isProcessRunning()) {
            killCurrentProcess();
        }
        
        // Log alanını temizle
        logArea.setText("");
        logToConsole("Web uygulaması başlatma işlemi başlatılıyor...");
        
        // Port kontrolü
        if (!isPortAvailable(WEB_PORT)) {
            String errorMsg = WEB_PORT + " portu başka bir uygulama tarafından kullanılıyor!";
            logToConsole("UYARI: " + errorMsg);
            logToConsole("İpucu: Shell script portu serbest bırakmayı deneyecek.");
        }
        
        // Arayüz güncellemesi
        updateUIState(true, "Web uygulaması başlatılıyor...");
        
        // İşlemi başlat
        new Thread(() -> {
            try {
                setupTimeoutTimer();
                
                // Shell script'i kullanarak başlat
                ProcessBuilder pb = new ProcessBuilder("bash", "start_web_service.sh");
                setupProcessBuilder(pb);
                
                logToConsole("Web uygulaması başlatılıyor: start_web_service.sh");
                
                currentProcess = pb.start();
                processWebOutput(currentProcess);
                
            } catch (IOException e) {
                handleProcessError(e, "Web uygulaması başlatılamadı");
            }
        }).start();
    }
    
    /**
     * CLI uygulamasını başlatır
     */
    private void startCliApplication() {
        // Önceki işlem çalışıyorsa sonlandır
        if (isProcessRunning()) {
            killCurrentProcess();
        }
        
        // Log alanını temizle
        logArea.setText("");
        logToConsole("CLI uygulaması başlatma işlemi başlatılıyor...");
        
        // Arayüz güncellemesi
        updateUIState(true, "CLI uygulaması başlatılıyor...");
        
        // İşlemi başlat
        new Thread(() -> {
            try {
                setupTimeoutTimer();
                
                // Shell script'i kullanarak başlat
                ProcessBuilder pb = new ProcessBuilder("bash", "start_cli_service.sh");
                setupProcessBuilder(pb);
                
                logToConsole("CLI uygulaması başlatılıyor: start_cli_service.sh");
                
                currentProcess = pb.start();
                processCliOutput(currentProcess);
                
            } catch (IOException e) {
                handleProcessError(e, "CLI uygulaması başlatılamadı");
            }
        }).start();
    }
    
    /**
     * ProcessBuilder için genel ayarları yapar
     */
    private void setupProcessBuilder(ProcessBuilder pb) {
        // Çalışma dizinini ayarla
        pb.directory(new File(System.getProperty("user.dir")));
        
        // Çıktıları birleştir
        pb.redirectErrorStream(true);
        
        // Ortam değişkenlerini ayarla
        Map<String, String> env = pb.environment();
        env.put("JAVA_HOME", System.getProperty("java.home"));
        env.put("SPRING_OUTPUT_ANSI_ENABLED", "ALWAYS");
        env.put("SPRING_PROFILES_ACTIVE", "default");
    }
    
    /**
     * Zaman aşımı zamanlayıcısını ayarlar
     */
    private void setupTimeoutTimer() {
        appStarted.set(false);
        
        // Önceki zamanlayıcıyı temizle
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
        
        // Yeni zamanlayıcı oluştur
        scheduler = Executors.newScheduledThreadPool(1);
        
        // Zaman aşımı kontrolü
        scheduler.schedule(() -> {
            if (!appStarted.get() && isProcessRunning()) {
                SwingUtilities.invokeLater(() -> {
                    logToConsole("\nUYARI: Uygulama " + TIMEOUT_SECONDS + 
                        " saniye içinde başlatılamadı!");
                    logToConsole("Uygulama hala başlatılmaya devam ediyor olabilir.");
                    updateUIState(false, "Zaman aşımı! (Uygulama hala başlıyor olabilir)");
                });
            }
        }, TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }
    
    /**
     * Web uygulaması çıktısını işler
     */
    private void processWebOutput(Process process) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            
            String line;
            boolean springBootStarted = false;
            
            while ((line = reader.readLine()) != null) {
                final String logLine = line;
                SwingUtilities.invokeLater(() -> logArea.append(logLine + "\n"));
                
                // Spring Boot başlatma göstergeleri
                if ((logLine.contains("Started") && logLine.contains("Application")) || 
                    logLine.contains("Tomcat started on port") || 
                    logLine.contains("Restoran Değerlendirme Sistemi") ||
                    logLine.contains("Web arayüzü:") ||
                    logLine.contains("Uygulama Baslatildi")) {
                    
                    springBootStarted = true;
                    
                    if (!appStarted.getAndSet(true)) {
                        SwingUtilities.invokeLater(() -> {
                            updateUIState(false, "Web uygulaması çalışıyor");
                            logToConsole("\nBilgi: Web uygulaması başarıyla başlatıldı!");
                            logToConsole("Web adresi: " + WEB_URL);
                            logToConsole("Tarayıcı otomatik açılıyor...");
                            
                            // Tarayıcıyı aç
                            openWebBrowser(2000);
                        });
                    }
                }
                
                // Spring Boot hata göstergeleri
                if (logLine.contains("APPLICATION FAILED TO START") || 
                    logLine.contains("Failed to start") || 
                    (logLine.contains("Error") && logLine.contains("starting"))) {
                    
                    SwingUtilities.invokeLater(() -> {
                        updateUIState(false, "Uygulama başlatılamadı");
                        logToConsole("\nHATA: Spring Boot uygulaması başlatılamadı!");
                        logToConsole("Lütfen günlükteki hata mesajlarını kontrol edin.");
                    });
                }
            }
            
            // Spring Boot başlatma göstergesini yakalayamadıysak ve işlem sonlandıysa
            if (!springBootStarted) {
                SwingUtilities.invokeLater(() -> {
                    updateUIState(false, "Uygulama beklenmedik şekilde sonlandı");
                    logToConsole("\nUYARI: Spring Boot başlatma mesajı görülmedi.");
                    logToConsole("Uygulama düzgün başlatılamadı olabilir.");
                });
            }
            
            // İşlem tamamlandığında
            int exitCode = process.waitFor();
            SwingUtilities.invokeLater(() -> {
                logToConsole("İşlem tamamlandı. Çıkış kodu: " + exitCode);
                updateUIState(false, exitCode == 0 ? "İşlem normal şekilde tamamlandı" : 
                    "İşlem hata ile sonlandı (Kod: " + exitCode + ")");
            });
            
        } catch (IOException | InterruptedException e) {
            if (!(e instanceof InterruptedException)) {
                handleProcessError(e, "Web uygulama çıktısı okunurken hata oluştu");
            }
        }
    }
    
    /**
     * CLI uygulaması çıktısını işler
     */
    private void processCliOutput(Process process) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                final String logLine = line;
                SwingUtilities.invokeLater(() -> logArea.append(logLine + "\n"));
                
                // Uygulama başlatıldı mı kontrolü
                if ((logLine.contains("Started") && logLine.contains("Application")) || 
                    logLine.contains("Restoran Değerlendirme Sistemi - CLI")) {
                    
                    if (!appStarted.getAndSet(true)) {
                        SwingUtilities.invokeLater(() -> {
                            updateUIState(false, "CLI uygulaması çalışıyor");
                            logToConsole("\nBilgi: CLI uygulaması başarıyla başlatıldı!");
                            logToConsole("Terminal penceresine geçiş yaparak komutları girebilirsiniz.");
                        });
                    }
                }
            }
            
            // İşlem tamamlandığında
            int exitCode = process.waitFor();
            SwingUtilities.invokeLater(() -> {
                logToConsole("İşlem tamamlandı. Çıkış kodu: " + exitCode);
                updateUIState(false, exitCode == 0 ? "İşlem normal şekilde tamamlandı" : 
                    "İşlem hata ile sonlandı (Kod: " + exitCode + ")");
            });
            
        } catch (IOException | InterruptedException e) {
            if (!(e instanceof InterruptedException)) {
                handleProcessError(e, "CLI uygulama çıktısı okunurken hata oluştu");
            }
        }
    }
    
    /**
     * Web tarayıcısını açar
     */
    private void openWebBrowser(int delayMs) {
        new Thread(() -> {
            try {
                // Belirtilen süre kadar bekle
                Thread.sleep(delayMs);
                
                // Tarayıcıyı aç
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI(WEB_URL));
                    logToConsole("Tarayıcı açıldı: " + WEB_URL);
                } else {
                    logToConsole("Uyarı: Tarayıcı otomatik açılamadı.");
                    logToConsole("Lütfen manuel olarak şu adresi ziyaret edin: " + WEB_URL);
                }
            } catch (InterruptedException | IOException | URISyntaxException e) {
                logToConsole("Tarayıcı açılırken hata: " + e.getMessage());
            }
        }).start();
    }
    
    /**
     * Çalışan işlemi sonlandırır
     */
    private void killCurrentProcess() {
        if (isProcessRunning()) {
            try {
                logToConsole("Çalışan uygulama kapatılıyor...");
                
                // Önce normal sonlandırma dene
                currentProcess.destroy();
                
                if (currentProcess.waitFor(3, TimeUnit.SECONDS)) {
                    logToConsole("Uygulama başarıyla kapatıldı.");
                } else {
                    // Zorla sonlandır
                    currentProcess.destroyForcibly();
                    currentProcess.waitFor(2, TimeUnit.SECONDS);
                    logToConsole("Uygulama zorla kapatıldı.");
                }
            } catch (InterruptedException e) {
                logToConsole("Uygulama kapatılırken hata: " + e.getMessage());
            }
        }
        
        // Zamanlayıcıyı kapat
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
        
        // UI'ı sıfırla
        updateUIState(false, "Hazır");
    }
    
    /**
     * İşlem hatalarını yönetir
     */
    private void handleProcessError(Exception e, String message) {
        SwingUtilities.invokeLater(() -> {
            logToConsole("HATA: " + message);
            logToConsole("Detay: " + e.getMessage());
            updateUIState(false, "Hata oluştu");
            
            showErrorDialog(message + "\n" + e.getMessage());
        });
    }
    
    /**
     * Hata iletişim kutusu gösterir
     */
    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(
            this,
            message,
            "Hata",
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    /**
     * UI durumunu günceller
     */
    private void updateUIState(boolean isLoading, String statusText) {
        webButton.setEnabled(!isLoading);
        cliButton.setEnabled(!isLoading);
        progressBar.setVisible(isLoading);
        progressBar.setString(statusText);
        statusLabel.setText(statusText);
    }
    
    /**
     * Belirtilen port müsait mi kontrol eder
     */
    private boolean isPortAvailable(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            return true; // Port müsait
        } catch (IOException e) {
            return false; // Port kullanımda
        }
    }
    
    /**
     * İşlem çalışıyor mu kontrol eder
     */
    private boolean isProcessRunning() {
        return currentProcess != null && currentProcess.isAlive();
    }
    
    /**
     * Log mesajı ekler
     */
    private void logToConsole(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            // Otomatik kaydırma
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
    
    /**
     * Ana metod
     */
    public static void main(String[] args) {
        // EDT'de çalıştır
        SwingUtilities.invokeLater(() -> {
            try {
                BasitGui gui = new BasitGui();
                gui.setVisible(true);
                
                System.out.println("Restoran Değerlendirme Sistemi Başlatıcı hazır.");
            } catch (Exception e) {
                System.err.println("GUI başlatılırken hata: " + e.getMessage());
                e.printStackTrace();
                
                // Kritik hata durumunda kullanıcıya bilgi ver
                JOptionPane.showMessageDialog(
                    null, 
                    "Uygulama başlatılamadı: " + e.getMessage(),
                    "Kritik Hata", 
                    JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }
}
