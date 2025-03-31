package gui_testleri;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import com.formdev.flatlaf.FlatDarkLaf;

/**
 * Yeni restoran ekleme penceresi.
 * Bu sınıf, kullanıcının yeni bir restoran eklemesi için
 * gerekli form alanlarını ve kontrolleri içerir.
 */
public class RestaurantAddGUI extends JDialog {
    private static final long serialVersionUID = 1L;
    
    // Form alanları
    private JTextField nameField;      // Restoran adı giriş alanı
    private JTextField locationField;  // Konum giriş alanı
    private JTextField ratingField;    // Puan giriş alanı
    
    // Ana pencere referansı
    private Main mainWindow;

    /**
     * RestaurantAddGUI sınıfının yapıcı metodu.
     * 
     * @param parent Ana pencere referansı
     */
    public RestaurantAddGUI(Main parent) {
        super(parent, "Yeni Restoran Ekle", true);  // Modal dialog
        this.mainWindow = parent;

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
        JLabel titleLabel = new JLabel("Yeni Restoran Ekle", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Form alanları
        JLabel nameLabel = createLabel("Restoran Adı:");
        JLabel locationLabel = createLabel("Konum:");
        JLabel ratingLabel = createLabel("Puan (1-5):");

        nameField = createTextField("");
        locationField = createTextField("");
        ratingField = createTextField("");

        formPanel.add(nameLabel);
        formPanel.add(nameField);
        formPanel.add(locationLabel);
        formPanel.add(locationField);
        formPanel.add(ratingLabel);
        formPanel.add(ratingField);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Buton paneli
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(new Color(30, 30, 30));

        JButton saveButton = createButton("Kaydet");
        JButton cancelButton = createButton("İptal");

        saveButton.addActionListener(e -> saveRestaurant());
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    /**
     * Özel stillendirilmiş etiket oluşturur.
     */
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("SansSerif", Font.PLAIN, 14));
        return label;
    }

    /**
     * Özel stillendirilmiş metin alanı oluşturur.
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
     * Form verilerini kontrol edip yeni restoran kaydeder.
     */
    private void saveRestaurant() {
        // Form alanlarını al
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

            // Yeni restoran oluştur ve kaydet
            Restaurant restaurant = new Restaurant(name, location, rating);
            mainWindow.getRestaurantManager().addRestaurant(restaurant);
            dispose();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "Lütfen geçerli bir puan girin!",
                "Hata",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("selam");
        }

        SwingUtilities.invokeLater(() -> new RestaurantAddGUI(null).setVisible(true));
        
    }
}
