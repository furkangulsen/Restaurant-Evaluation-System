package gui_testleri;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.util.List;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.hibernate.Session;
import org.hibernate.query.Query;

/**
 * Restoran değerlendirmelerini gösteren pencere.
 * Bu sınıf, bir restorana ait tüm değerlendirmeleri listeler ve
 * değerlendirmelerin eklenmesi, düzenlenmesi ve silinmesi işlemlerini sağlar.
 */
public class ReviewsGUI extends JDialog {
    private static final long serialVersionUID = 1L;
    
    // GUI bileşenleri
    private DefaultListModel<String> reviewListModel;  // Değerlendirme listesi modeli
    private JList<String> reviewList;                  // Değerlendirme listesi
    private JLabel titleLabel;                         // Başlık etiketi
    
    // Veri yönetimi
    private ReviewManager reviewManager;               // Değerlendirme yöneticisi
    private String restaurantName;                     // Restoran adı
    private List<Review> currentReviews;              // Mevcut değerlendirmeler

    /**
     * ReviewsGUI sınıfının yapıcı metodu.
     * 
     * @param parent Ana pencere referansı
     * @param restaurantName Değerlendirmeleri gösterilecek restoran adı
     * @param reviewManager Değerlendirme yöneticisi
     */
    public ReviewsGUI(JFrame parent, String restaurantName, ReviewManager reviewManager) {
        super(parent, restaurantName + " - Değerlendirmeler", true);
        this.restaurantName = restaurantName;
        this.reviewManager = reviewManager;
        this.currentReviews = new ArrayList<>();

        // Pencere özellikleri
        setSize(500, 400);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Ana panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(30, 30, 30));

        // Başlık ve ortalama puan
        double averageRating = reviewManager.getAverageRating(restaurantName);
        titleLabel = new JLabel(String.format("🏠 %s | Ortalama: %.1f ⭐", 
            restaurantName, averageRating), SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Değerlendirmeler listesi
        reviewListModel = new DefaultListModel<>();
        reviewList = new JList<>(reviewListModel);
        reviewList.setFont(new Font("SansSerif", Font.PLAIN, 14));
        reviewList.setBackground(new Color(45, 45, 45));
        reviewList.setForeground(Color.WHITE);
        reviewList.setSelectionBackground(new Color(70, 130, 180));
        reviewList.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Sağ tık menüsü
        reviewList.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleMouseEvent(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                handleMouseEvent(e);
            }

            private void handleMouseEvent(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int index = reviewList.locationToIndex(e.getPoint());
                    if (index != -1) {
                        reviewList.setSelectedIndex(index);
                        showPopupMenu(e.getX(), e.getY());
                    }
                }
            }
        });

        // Liste kaydırma paneli
        JScrollPane scrollPane = new JScrollPane(reviewList);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80)));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Buton paneli
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(new Color(30, 30, 30));

        JButton addButton = createStyledButton("➕ Değerlendirme Ekle");
        JButton closeButton = createStyledButton("✖️ Kapat");

        addButton.addActionListener(e -> {
            ReviewAddGUI addReviewGui = new ReviewAddGUI(
                (JFrame) getParent(), restaurantName, reviewManager);
            addReviewGui.setVisible(true);
            updateReviewsAndTitle();
        });

        closeButton.addActionListener(e -> dispose());

        buttonPanel.add(addButton);
        buttonPanel.add(closeButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
        loadReviews();
    }

    /**
     * Seçili değerlendirmeyi düzenler.
     * Kullanıcıdan yeni yorum ve puan alır, geçerlilik kontrolü yapar.
     */
    private void editSelectedReview() {
        int selectedIndex = reviewList.getSelectedIndex();
        if (selectedIndex == -1) return;

        Review selectedReview = currentReviews.get(selectedIndex);
        
        String newComment = JOptionPane.showInputDialog(this, "Yorumunuz:", selectedReview.getComment());
        if (newComment == null || newComment.trim().isEmpty()) return;

        String ratingStr = JOptionPane.showInputDialog(this, "Puan (1-5):", selectedReview.getRating());
        if (ratingStr == null || ratingStr.trim().isEmpty()) return;

        try {
            double newRating = Double.parseDouble(ratingStr);
            if (newRating < 1 || newRating > 5) {
                JOptionPane.showMessageDialog(this, "Puan 1 ile 5 arasında olmalıdır!", "Hata", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Review updatedReview = new Review(restaurantName, (int)newRating, newComment.trim(), selectedReview.getDate());
            reviewManager.updateReview(selectedReview, updatedReview);
            updateReviewsAndTitle();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Geçerli bir puan girin!", "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Seçili değerlendirmeyi siler.
     * Kullanıcıdan onay alır ve silme işlemini gerçekleştirir.
     */
    private void deleteSelectedReview() {
        int selectedIndex = reviewList.getSelectedIndex();
        if (selectedIndex == -1) return;

        Review selectedReview = currentReviews.get(selectedIndex);

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Bu değerlendirmeyi silmek istediğinize emin misiniz?",
            "Değerlendirme Sil",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            reviewManager.deleteReview(selectedReview);
            updateReviewsAndTitle();
        }
    }

    /**
     * Değerlendirme listesini ve başlık bilgisini günceller.
     * Ortalama puanı yeniden hesaplar ve görüntüler.
     */
    private void updateReviewsAndTitle() {
        loadReviews();
        
        // Ortalama puanı doğrudan veritabanından hesapla
        double newAverageRating = 0.0;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Double> query = session.createQuery(
                "SELECT AVG(r.rating) FROM Review r WHERE r.restaurantName = :name", 
                Double.class);
            query.setParameter("name", restaurantName);
            
            Double avgRating = query.uniqueResult();
            if (avgRating != null) {
                newAverageRating = avgRating;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        titleLabel.setText(String.format("🏠 %s | Ortalama: %.1f ⭐", 
            restaurantName, newAverageRating));
    }

    /**
     * Değerlendirmeleri yükler ve liste modelini günceller.
     */
    private void loadReviews() {
        reviewListModel.clear();
        currentReviews.clear();
        
        // Doğrudan veritabanından değerlendirmeleri al
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Review> query = session.createQuery(
                "FROM Review r WHERE r.restaurantName = :name ORDER BY r.date DESC", 
                Review.class);
            query.setParameter("name", restaurantName);
            
            List<Review> loadedReviews = query.list();
            currentReviews.addAll(loadedReviews);
            
            // Liste modeline ekle
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
            for (Review review : currentReviews) {
                String dateStr = review.getDate() != null ? 
                    review.getDate().format(formatter) : "Tarih yok";
                
                String displayText = String.format("%d ★ | %s | %s", 
                    review.getRating(), 
                    review.getComment(),
                    dateStr);
                
                reviewListModel.addElement(displayText);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Değerlendirmeler yüklenirken hata: " + e.getMessage(), 
                "Veritabanı Hatası", 
                JOptionPane.ERROR_MESSAGE);
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

    /**
     * Sağ tık menüsünü gösterir.
     * 
     * @param x Menünün x koordinatı
     * @param y Menünün y koordinatı
     */
    private void showPopupMenu(int x, int y) {
        JPopupMenu popupMenu = new JPopupMenu();
        
        JMenuItem editItem = new JMenuItem("✏️ Düzenle");
        editItem.addActionListener(e -> editSelectedReview());
        
        JMenuItem deleteItem = new JMenuItem("🗑️ Sil");
        deleteItem.addActionListener(e -> deleteSelectedReview());
        
        popupMenu.add(editItem);
        popupMenu.add(deleteItem);
        
        popupMenu.show(reviewList, x, y);
    }
} 