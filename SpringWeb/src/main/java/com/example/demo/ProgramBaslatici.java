package com.example.demo;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.awt.Desktop;
import java.util.concurrent.TimeUnit;
import javax.swing.Timer;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.FontMetrics;
import java.util.Scanner;

/**
 * Bu sınıf, kullanıcının CLI veya Spring Web uygulamasını başlatabilmesi için
 * modern ve kullanıcı dostu bir arayüz sağlar.
 */
public class ProgramBaslatici extends JFrame {

    private static final long serialVersionUID = 1L;
    private JLabel statusLabel;
    private Timer statusTimer;
    private JPanel loadingPanel;
    private JProgressBar progressBar;

    public ProgramBaslatici() {
        // Temel pencere özellikleri
        setTitle("Restoran Değerlendirme Sistemi - Başlatıcı");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Arka plan rengi
        getContentPane().setBackground(new Color(240, 240, 245));
        
        // Ana panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(0, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        mainPanel.setBackground(new Color(240, 240, 245));
        
        // Başlık paneli
        JPanel headerPanel = createHeaderPanel();
        
        // Butonlar paneli
        JPanel buttonPanel = createButtonPanel();
        
        // Durum paneli
        JPanel statusPanel = createStatusPanel();
        
        // Yükleme paneli (başlangıçta gizli)
        loadingPanel = createLoadingPanel();
        loadingPanel.setVisible(false);
        
        // Alt bilgi paneli
        JPanel footerPanel = createFooterPanel();
        
        // Ana panele bileşenleri ekle
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        mainPanel.add(statusPanel, BorderLayout.SOUTH);
        mainPanel.add(loadingPanel, BorderLayout.SOUTH);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);
        
        // Ana pencereye paneli ekle
        add(mainPanel);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(new Color(240, 240, 245));
        
        // Logo/ikon
        ImageIcon icon = createAppIcon();
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Başlık
        JLabel titleLabel = new JLabel("Restoran Değerlendirme Sistemi", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(new Color(50, 50, 50));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 15, 0));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Alt başlık
        JLabel subtitleLabel = new JLabel("Lütfen başlatmak istediğiniz uygulamayı seçin:", JLabel.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitleLabel.setForeground(new Color(80, 80, 80));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        headerPanel.add(Box.createVerticalStrut(10));
        headerPanel.add(iconLabel);
        headerPanel.add(Box.createVerticalStrut(10));
        headerPanel.add(titleLabel);
        headerPanel.add(subtitleLabel);
        headerPanel.add(Box.createVerticalStrut(20));
        
        return headerPanel;
    }
    
    private ImageIcon createAppIcon() {
        // Basit bir ikon oluşturalım (gerçek bir proje için resim dosyası kullanılabilir)
        int size = 64;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        
        // Arka plan
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(46, 204, 113));
        g2.fillRoundRect(0, 0, size, size, 15, 15);
        
        // "R" harfi
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 40));
        g2.drawString("R", 18, 45);
        
        g2.dispose();
        return new ImageIcon(image);
    }
    
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 1, 0, 25));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 50, 15, 50));
        buttonPanel.setBackground(new Color(240, 240, 245));
        
        // Web Uygulaması Butonu
        JButton webButton = createStyledButton("Web Uygulaması (Spring)", 
                                               new Color(46, 204, 113),
                                               "Tarayıcı arayüzü ile verileri yönetin");
        webButton.setIcon(createButtonIcon(new Color(46, 204, 113), "W"));
        webButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                baslatSpringWeb();
            }
        });
        
        // CLI Uygulaması Butonu
        JButton cliButton = createStyledButton("Terminal Uygulaması (CLI)", 
                                               new Color(30, 144, 255),
                                               "Komut satırı arayüzü ile verileri yönetin");
        cliButton.setIcon(createButtonIcon(new Color(30, 144, 255), "C"));
        cliButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                baslatCLI();
            }
        });
        
        buttonPanel.add(webButton);
        buttonPanel.add(cliButton);
        
        return buttonPanel;
    }
    
    private ImageIcon createButtonIcon(Color color, String text) {
        int size = 32;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        g2.fillOval(0, 0, size, size);
        
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 20));
        FontMetrics fm = g2.getFontMetrics();
        int x = (size - fm.stringWidth(text)) / 2;
        int y = ((size - fm.getHeight()) / 2) + fm.getAscent();
        g2.drawString(text, x, y);
        
        g2.dispose();
        return new ImageIcon(image);
    }
    
    private JPanel createStatusPanel() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(new Color(240, 240, 245));
        
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        statusLabel.setForeground(new Color(100, 100, 100));
        statusLabel.setHorizontalAlignment(JLabel.CENTER);
        
        statusPanel.add(statusLabel, BorderLayout.CENTER);
        
        return statusPanel;
    }
    
    private JPanel createLoadingPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(240, 240, 245));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 50, 10, 50));
        
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setStringPainted(true);
        progressBar.setString("Başlatılıyor...");
        progressBar.setForeground(new Color(46, 204, 113));
        progressBar.setBackground(new Color(230, 230, 230));
        progressBar.setBorder(BorderFactory.createEmptyBorder());
        
        panel.add(progressBar, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(new Color(240, 240, 245));
        footerPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        
        JLabel footerLabel = new JLabel("© 2025 Restoran Değerlendirme Sistemi", JLabel.CENTER);
        footerLabel.setForeground(new Color(120, 120, 120));
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        footerPanel.add(footerLabel, BorderLayout.SOUTH);
        
        return footerPanel;
    }
    
    private JButton createStyledButton(String text, Color baseColor, String tooltip) {
        JButton button = new JButton();
        button.setLayout(new BorderLayout(10, 0));
        
        // Ana metin
        JLabel mainLabel = new JLabel(text);
        mainLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        mainLabel.setForeground(Color.BLACK);
        
        // Açıklama metni
        JLabel descLabel = new JLabel(tooltip);
        descLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        descLabel.setForeground(new Color(100, 100, 100));
        
        // İç panel
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false); 
        textPanel.add(mainLabel);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(descLabel);
        
        button.add(textPanel, BorderLayout.CENTER);
        
        // Stil ayarları
        button.setBackground(new Color(250, 250, 250));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(baseColor, 3),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        // Hover efekti
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(240, 240, 240));
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(baseColor, 4),
                    BorderFactory.createEmptyBorder(14, 15, 14, 15)
                ));
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(250, 250, 250));
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(baseColor, 3),
                    BorderFactory.createEmptyBorder(15, 15, 15, 15)
                ));
                button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
            
            public void mousePressed(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(230, 230, 230));
            }
            
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                if (button.getMousePosition() != null) {
                    button.setBackground(new Color(240, 240, 240));
                } else {
                    button.setBackground(new Color(250, 250, 250));
                }
            }
        });
        
        return button;
    }
    
    // CLI uygulamasını başlat
    private void baslatCLI() {
        try {
            showLoading("Terminal uygulaması başlatılıyor...");
            
            // Gradlew ile Spring Boot uygulamasını CLI profilini aktif ederek başlat
            ProcessBuilder pb = new ProcessBuilder(
                "cmd.exe", 
                "/c", 
                "start", 
                "cmd.exe", 
                "/c", 
                "chcp 65001 && gradlew.bat bootRun --args=\"--spring.profiles.active=cli\""
            );
            pb.directory(new File(System.getProperty("user.dir")));
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            // Kısa bir süre bekle ve uygulamayı kapat
            new Timer(3000, e -> {
                ((Timer)e.getSource()).stop();
                System.exit(0);
            }).start();
            
        } catch (Exception e) {
            e.printStackTrace();
            hideLoading();
            JOptionPane.showMessageDialog(this, 
                "CLI uygulaması başlatılırken hata oluştu: " + e.getMessage(),
                "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Spring Web uygulamasını başlat
    private void baslatSpringWeb() {
        try {
            showLoading("Web uygulaması başlatılıyor...");
            
            // Gradlew ile Spring Boot uygulamasını başlat
            ProcessBuilder pb = new ProcessBuilder(
                "cmd.exe", 
                "/c", 
                "start", 
                "cmd.exe", 
                "/c", 
                "gradlew.bat bootRun --args=\"--server.port=8080\""
            );
            pb.directory(new File(System.getProperty("user.dir")));
            pb.redirectErrorStream(true);
            pb.start();
            
            // Tarayıcıyı otomatik aç
            new Thread(() -> {
                try {
                    // Spring Boot'un başlaması için biraz bekle
                    Thread.sleep(10000);
                    
                    // Varsayılan tarayıcıda uygulamayı aç
                    Desktop.getDesktop().browse(new URI("http://localhost:8080"));
                } catch (Exception e) {
                    // Tarayıcı açılmazsa sessizce devam et
                    e.printStackTrace();
                }
            }).start();
            
            // Kısa bir süre bekle ve uygulamayı kapat
            new Timer(3000, e -> {
                ((Timer)e.getSource()).stop();
                System.exit(0);
            }).start();
            
        } catch (Exception e) {
            e.printStackTrace();
            hideLoading();
            JOptionPane.showMessageDialog(this, 
                "Web uygulaması başlatılırken hata oluştu: " + e.getMessage(),
                "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showLoading(String message) {
        statusLabel.setVisible(false);
        loadingPanel.setVisible(true);
        progressBar.setString(message);
        validate();
        repaint();
    }
    
    private void hideLoading() {
        loadingPanel.setVisible(false);
        statusLabel.setVisible(true);
        validate();
        repaint();
    }
    
    public static void main(String[] args) {
        try {
            // Look and Feel'i sistem görünümüne ayarla
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
            // Yazı tipi yumuşatma
            System.setProperty("awt.useSystemAAFontSettings", "on");
            System.setProperty("swing.aatext", "true");
            
            System.out.println("GUI başlatıcı hazırlanıyor...");
        } catch (Exception e) {
            System.err.println("GUI başlatıcı ayarları yapılandırılırken hata: " + e.getMessage());
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("GUI arayüzü oluşturuluyor...");
                    ProgramBaslatici app = new ProgramBaslatici();
                    app.setVisible(true);
                    System.out.println("GUI başlatıldı.");
                } catch (Exception e) {
                    System.err.println("GUI oluşturulurken hata: " + e.getMessage());
                    e.printStackTrace();
                    
                    // Basit metin tabanlı alternatif menü
                    System.out.println("\n=== RESTORAN DEĞERLENDİRME SİSTEMİ ===");
                    System.out.println("[1] Web uygulamasını başlat");
                    System.out.println("[2] CLI uygulamasını başlat");
                    System.out.println("[0] Çıkış");
                    System.out.print("Seçiminiz: ");
                    
                    try (Scanner scanner = new Scanner(System.in)) {
                        int choice = scanner.nextInt();
                        if (choice == 1) {
                            new ProcessBuilder("cmd.exe", "/c", "start_web.bat").start();
                        } else if (choice == 2) {
                            new ProcessBuilder("cmd.exe", "/c", "start_cli.bat").start();
                        }
                    } catch (Exception ex) {
                        System.err.println("Seçim yapılırken hata: " + ex.getMessage());
                    }
                }
            }
        });
    }
} 