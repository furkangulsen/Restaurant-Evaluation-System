package gui_testleri;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Yeni değerlendirme ekleme penceresi.
 * Bu sınıf, seçili bir restoran için yeni değerlendirme eklenmesini sağlar.
 * Kullanıcıdan yorum ve puan bilgilerini alır.
 */
public class ReviewAddGUI extends JDialog {
    private static final long serialVersionUID = 1L;
    
    // Form alanları
    private JTextArea commentArea;     // Yorum giriş alanı
    private JTextField ratingField;    // Puan giriş alanı
    
    // Veri yönetimi
    private String selectedRestaurant;  // Seçili restoran adı
    private ReviewManager reviewManager; // Değerlendirme yöneticisi

    /**
     * ReviewAddGUI sınıfının yapıcı metodu.
     * 
     * @param parent Ana pencere referansı
     * @param selectedRestaurant Değerlendirme eklenecek restoran adı
     * @param reviewManager Değerlendirme yöneticisi
     */
    public ReviewAddGUI(JFrame parent, String selectedRestaurant, ReviewManager reviewManager) {
        super(parent, "Değerlendirme Ekle", true);
        this.selectedRestaurant = selectedRestaurant;
        this.reviewManager = reviewManager;

        // Pencere özellikleri
        setSize(400, 300);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Ana panel
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setBackground(new Color(30, 30, 30));

        // Başlık
        JLabel titleLabel = new JLabel("🌟 Değerlendirme Ekle: " + selectedRestaurant, SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        panel.add(titleLabel, BorderLayout.NORTH);

        // Form paneli
        JPanel formPanel = new JPanel(new BorderLayout(10, 10));
        formPanel.setBackground(new Color(30, 30, 30));

        // Yorum alanı
        commentArea = new JTextArea();
        commentArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        commentArea.setBackground(new Color(45, 45, 45));
        commentArea.setForeground(Color.WHITE);
        commentArea.setLineWrap(true);
        commentArea.setWrapStyleWord(true);
        commentArea.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JScrollPane scrollPane = new JScrollPane(commentArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 80)),
            "Yorumunuz",
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
            javax.swing.border.TitledBorder.DEFAULT_POSITION,
            new Font("SansSerif", Font.PLAIN, 12),
            Color.WHITE
        ));
        formPanel.add(scrollPane, BorderLayout.CENTER);

        // Puan giriş alanı
        JPanel ratingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ratingPanel.setBackground(new Color(30, 30, 30));

        JLabel ratingLabel = new JLabel("Puan (1-5):");
        ratingLabel.setForeground(Color.WHITE);
        ratingLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));

        ratingField = new JTextField(5);
        ratingField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        ratingField.setBackground(new Color(45, 45, 45));
        ratingField.setForeground(Color.WHITE);
        ratingField.setCaretColor(Color.WHITE);
        ratingField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 80)),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        ratingPanel.add(ratingLabel);
        ratingPanel.add(ratingField);
        formPanel.add(ratingPanel, BorderLayout.SOUTH);

        panel.add(formPanel, BorderLayout.CENTER);

        // Buton paneli
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(new Color(30, 30, 30));

        JButton saveButton = createStyledButton("💾 Kaydet");
        JButton cancelButton = createStyledButton("❌ İptal");

        saveButton.addActionListener(e -> saveReview());
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        add(panel);
    }

    /**
     * Form verilerini kontrol edip yeni değerlendirme kaydeder.
     * Yorum ve puan alanlarının geçerliliğini kontrol eder.
     * Başarılı kayıt durumunda pencereyi kapatır.
     */
    private void saveReview() {
        String comment = commentArea.getText().trim();
        String ratingText = ratingField.getText().trim();

        // Boş yorum kontrolü
        if (comment.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lütfen bir yorum yazın!", "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Puan değerini kontrol et
            double rating = Double.parseDouble(ratingText);
            if (rating < 1 || rating > 5) {
                JOptionPane.showMessageDialog(this, "Puan 1 ile 5 arasında olmalıdır!", "Hata", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Yeni değerlendirme oluştur ve kaydet
            Review review = new Review(selectedRestaurant, (int)rating, comment);
            reviewManager.addReview(review);
            JOptionPane.showMessageDialog(this, "Değerlendirme başarıyla eklendi!", "Başarılı", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Geçerli bir puan girin!", "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Özel stillendirilmiş buton oluşturur.
     * 
     * @param text Buton metni
     * @return Stillendirilmiş JButton nesnesi
     */
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(50, 150, 250));
        button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
} 