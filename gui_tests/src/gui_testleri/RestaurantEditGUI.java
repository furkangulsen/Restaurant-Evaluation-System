package gui_testleri;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.EmptyBorder;
import org.hibernate.Session;
import org.hibernate.Query;

/**
 * Restoran düzenleme penceresi.
 * Bu sınıf, var olan bir restoranın bilgilerinin düzenlenmesi için
 * gerekli form alanlarını ve kontrolleri içerir.
 */
public class RestaurantEditGUI extends JDialog {
    private static final long serialVersionUID = 1L;
    
    // Form alanları
    private JTextField nameField;      // Restoran adı giriş alanı
    private JTextField locationField;  // Konum giriş alanı
    private JTextField ratingField;    // Puan giriş alanı
    
    // Düzenlenen restoran ve yönetici referansları
    private Restaurant restaurant;
    private RestaurantManager restaurantManager;
    
    // Değişikliklerin kaydedilip kaydedilmediğini takip eden bayrak
    private boolean saved = false;

    /**
     * RestaurantEditGUI sınıfının yapıcı metodu.
     * 
     * @param parent Ana pencere referansı
     * @param restaurant Düzenlenecek restoran nesnesi
     * @param restaurantManager Restoran yöneticisi
     */
    public RestaurantEditGUI(JFrame parent, Restaurant restaurant, RestaurantManager restaurantManager) {
        super(parent, "Restoran Düzenle", true);
        this.restaurant = restaurant;
        this.restaurantManager = restaurantManager;

        // Pencere özellikleri
        setSize(400, 300);
        setLocationRelativeTo(parent);
        setResizable(false);

        // Ana panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(30, 30, 30));

        // Form paneli
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        formPanel.setBackground(new Color(30, 30, 30));

        // Başlık
        JLabel titleLabel = new JLabel("Restoran Bilgilerini Düzenle", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Form alanları
        JLabel nameLabel = createLabel("Restoran Adı:");
        JLabel locationLabel = createLabel("Konum:");
        JLabel ratingLabel = createLabel("Puan (1-5):");

        nameField = createTextField(restaurant.getName());
        locationField = createTextField(restaurant.getLocation());
        ratingField = createTextField(String.valueOf(restaurant.getRating()));

        formPanel.add(nameLabel);
        formPanel.add(nameField);
        formPanel.add(locationLabel);
        formPanel.add(locationField);
        formPanel.add(ratingLabel);
        formPanel.add(ratingField);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Butonlar
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(new Color(30, 30, 30));

        JButton saveButton = createButton("Kaydet");
        JButton cancelButton = createButton("İptal");

        saveButton.addActionListener(e -> saveChanges());
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    /**
     * Özel stillendirilmiş etiket oluşturur.
     * 
     * @param text Etiket metni
     * @return Stillendirilmiş JLabel nesnesi
     */
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("SansSerif", Font.PLAIN, 14));
        return label;
    }

    /**
     * Özel stillendirilmiş metin alanı oluşturur.
     * 
     * @param text Başlangıç metni
     * @return Stillendirilmiş JTextField nesnesi
     */
    private JTextField createTextField(String text) {
        JTextField field = new JTextField(text);
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setBackground(new Color(45, 45, 45));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 60)),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        return field;
    }

    /**
     * Özel stillendirilmiş buton oluşturur.
     * 
     * @param text Buton metni
     * @return Stillendirilmiş JButton nesnesi
     */
    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(50, 150, 250));
        button.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    /**
     * Form verilerini kontrol edip restoran bilgilerini günceller.
     * Tüm alanların dolu olduğunu ve puanın geçerli bir değer olduğunu kontrol eder.
     * Başarılı güncelleme durumunda pencereyi kapatır.
     */
    private void saveChanges() {
        String name = nameField.getText().trim();
        String location = locationField.getText().trim();
        String ratingStr = ratingField.getText().trim();

        // Boş alan kontrolü
        if (name.isEmpty() || location.isEmpty() || ratingStr.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Lütfen tüm alanları doldurun!",
                "Hata",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Puan değerini kontrol et
            double rating = Double.parseDouble(ratingStr);
            if (rating < 1 || rating > 5) {
                JOptionPane.showMessageDialog(this,
                    "Puan 1 ile 5 arasında olmalıdır!",
                    "Hata",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Önce RestaurantManager üzerinden güncelleme yap
            Restaurant updatedRestaurant = new Restaurant();
            updatedRestaurant.setId(restaurant.getId());
            updatedRestaurant.setName(name);
            updatedRestaurant.setLocation(location);
            updatedRestaurant.setRating(rating);

            if (restaurantManager.updateRestaurant(updatedRestaurant)) {
                saved = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Güncelleme sırasında bir hata oluştu!",
                    "Hata",
                    JOptionPane.ERROR_MESSAGE);
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "Lütfen geçerli bir puan girin!",
                "Hata",
                JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Güncelleme sırasında bir hata oluştu: " + e.getMessage(),
                "Hata",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Değişikliklerin kaydedilip kaydedilmediğini döndürür.
     * 
     * @return Değişiklikler kaydedildiyse true, aksi halde false
     */
    public boolean isSaved() {
        return saved;
    }
} 