package gui_testleri;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import com.formdev.flatlaf.FlatDarkLaf;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import org.hibernate.Session;
import org.hibernate.Query;
import org.hibernate.Transaction;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Ana uygulama penceresi sınıfı.
 * Bu sınıf, uygulamanın temel kullanıcı arayüzünü ve ana işlevlerini içerir.
 * JFrame'den kalıtım alarak bir pencere oluşturur.
 */
public class Main extends JFrame {
    // Sınıfın seri versiyon numarası (Serializable için gerekli)
    private static final long serialVersionUID = 1L;
    
    // Restoran listesini tutan model (JList için veri kaynağı)
    private DefaultListModel<String> listModel;
    
    // Restoran ve değerlendirme işlemlerini yöneten nesneler
    private RestaurantManager restaurantManager;
    private ReviewManager reviewManager;
    
    // Restoranları görüntüleyen liste bileşeni
    private JList<String> restaurantList;

    // Modern renk paleti
    private Color primaryColor = new Color(103, 58, 183); // Material Purple
    private Color accentColor = new Color(156, 39, 176); // Material Deep Purple
    private Color backgroundColor = new Color(18, 18, 18); // Material Dark Background
    private Color surfaceColor = new Color(30, 30, 30); // Material Dark Surface
    private Color textColor = new Color(255, 255, 255, 230); // Material Light Text
    private Color secondaryTextColor = new Color(255, 255, 255, 160); // Material Secondary Text

    private Point initialClick;
    private boolean isDragging = false;
    private boolean isDarkMode = false;

    /**
     * Ana pencereyi oluşturan yapıcı metod.
     * Tüm GUI bileşenlerini başlatır ve yerleştirir.
     */
    public Main() {
        setTitle("Yiyecek Mekanları");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        // Modern görünüm için FlatLaf ayarları
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
            UIManager.put("Button.arc", 20);
            UIManager.put("Component.arc", 20);
            UIManager.put("ProgressBar.arc", 20);
            UIManager.put("TextComponent.arc", 20);
            UIManager.put("ScrollBar.width", 12);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Ana panel
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(backgroundColor);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                g2.dispose();
            }
        };
        mainPanel.setLayout(new BorderLayout(0, 0));
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Başlık çubuğu
        JPanel titleBar = createTitleBar();
        mainPanel.add(titleBar, BorderLayout.NORTH);

        // İçerik paneli
        JPanel contentPanel = new JPanel(new BorderLayout(20, 20));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

        // Başlık ve alt başlık
        JPanel headerPanel = createHeaderPanel();
        contentPanel.add(headerPanel, BorderLayout.NORTH);

        // Liste paneli
        JPanel listPanel = createListPanel();
        contentPanel.add(listPanel, BorderLayout.CENTER);

        // Butonlar paneli
        JPanel buttonPanel = createButtonPanel();
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.add(contentPanel, BorderLayout.CENTER);
        setContentPane(mainPanel);

        // Pencere sürükleme işlemleri
        addWindowDragListener();

        // İlk veri yüklemesi
        updateRestaurantList();
    }

    private JPanel createTitleBar() {
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setOpaque(false);
        titleBar.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // Logo ve başlık
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("Yiyecek Mekanları");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(textColor);
        leftPanel.add(titleLabel);
        
        titleBar.add(leftPanel, BorderLayout.WEST);

        // Kontrol butonları
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        controlPanel.setOpaque(false);

        JButton minimizeButton = createControlButton("─");
        JButton maximizeButton = createControlButton("□");
        JButton closeButton = createControlButton("×");

        minimizeButton.addActionListener(e -> setState(Frame.ICONIFIED));
        maximizeButton.addActionListener(e -> {
            if ((getExtendedState() & Frame.MAXIMIZED_BOTH) == 0) {
                setExtendedState(Frame.MAXIMIZED_BOTH);
            } else {
                setExtendedState(Frame.NORMAL);
            }
        });
        closeButton.addActionListener(e -> {
            HibernateUtil.shutdown();
            System.exit(0);
        });

        controlPanel.add(minimizeButton);
        controlPanel.add(maximizeButton);
        controlPanel.add(closeButton);
        titleBar.add(controlPanel, BorderLayout.EAST);

        return titleBar;
    }

    private JButton createControlButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setForeground(secondaryTextColor);
        button.setPreferredSize(new Dimension(45, 30));
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setContentAreaFilled(true);
                button.setBackground(new Color(255, 255, 255, 30));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setContentAreaFilled(false);
            }
        });

        return button;
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(0, 10));
        headerPanel.setOpaque(false);

        JLabel mainTitle = new JLabel("Yiyecek Mekanları", SwingConstants.CENTER);
        mainTitle.setFont(new Font("Segoe UI", Font.BOLD, 32));
        mainTitle.setForeground(textColor);

        JLabel subtitle = new JLabel("En iyi mekanları keşfedin ve değerlendirin", SwingConstants.CENTER);
        subtitle.setFont(new Font("Segoe UI Light", Font.PLAIN, 16));
        subtitle.setForeground(secondaryTextColor);

        headerPanel.add(mainTitle, BorderLayout.CENTER);
        headerPanel.add(subtitle, BorderLayout.SOUTH);

        return headerPanel;
    }

    private JPanel createListPanel() {
        // Liste modeli ve yöneticiler
        listModel = new DefaultListModel<>();
        restaurantManager = new RestaurantManager(listModel);
        reviewManager = new ReviewManager();
        reviewManager.setRestaurantManager(restaurantManager);

        // Liste bileşeni
        restaurantList = new JList<>(listModel);
        restaurantList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        restaurantList.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        restaurantList.setBackground(surfaceColor);
        restaurantList.setForeground(textColor);
        restaurantList.setSelectionBackground(primaryColor);
        restaurantList.setSelectionForeground(textColor);
        restaurantList.setFixedCellHeight(60);
        restaurantList.setBorder(null);

        // Özel liste görünümü
        restaurantList.setCellRenderer(new DefaultListCellRenderer() {
            private final Color starColor = new Color(255, 215, 0); // Altın sarısı
            private final Font nameFont = new Font("Segoe UI Semibold", Font.PLAIN, 16);
            private final Font locationFont = new Font("Segoe UI", Font.PLAIN, 14);
            private final Font ratingFont = new Font("Segoe UI", Font.BOLD, 15);

            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, 
                    int index, boolean isSelected, boolean cellHasFocus) {
                JPanel panel = new JPanel(new BorderLayout(10, 0));
                panel.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
                
                if (isSelected) {
                    panel.setBackground(primaryColor);
                } else {
                    panel.setBackground(index % 2 == 0 ? surfaceColor : 
                            new Color(surfaceColor.getRed() + 5, surfaceColor.getGreen() + 5, surfaceColor.getBlue() + 5));
                }

                String text = value.toString();
                String[] parts = text.split(" \\(|\\) - |★");
                
                if (parts.length >= 3) {
                    String name = parts[0];
                    String location = parts[1];
                    String rating = parts[2];

                    // Sol panel (isim ve konum)
                    JPanel leftPanel = new JPanel(new GridLayout(2, 1, 0, 2));
                    leftPanel.setOpaque(false);

                    JLabel nameLabel = new JLabel(name);
                    nameLabel.setFont(nameFont);
                    nameLabel.setForeground(textColor);

                    JLabel locationLabel = new JLabel("(" + location + ")");
                    locationLabel.setFont(locationFont);
                    locationLabel.setForeground(secondaryTextColor);

                    leftPanel.add(nameLabel);
                    leftPanel.add(locationLabel);

                    // Sağ panel (puan)
                    JPanel ratingPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
                    ratingPanel.setOpaque(false);

                    JLabel ratingLabel = new JLabel(rating);
                    ratingLabel.setFont(ratingFont);
                    ratingLabel.setForeground(textColor);

                    // Yıldız ikonu
                    JLabel starLabel = new JLabel("\u2B50");
                    starLabel.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 14));
                    starLabel.setForeground(starColor);

                    ratingPanel.add(ratingLabel);
                    ratingPanel.add(starLabel);

                    panel.add(leftPanel, BorderLayout.WEST);
                    panel.add(ratingPanel, BorderLayout.EAST);
                }

                return panel;
            }
        });

        // Kaydırma paneli
        JScrollPane scrollPane = new JScrollPane(restaurantList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBackground(surfaceColor);
        scrollPane.getViewport().setBackground(surfaceColor);

        // Özel kaydırma çubuğu
        scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = primaryColor;
                this.trackColor = surfaceColor;
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }

            private JButton createZeroButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                return button;
            }
        });

        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setOpaque(false);
        listPanel.add(scrollPane, BorderLayout.CENTER);

        return listPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        buttonPanel.setOpaque(false);

        // Üst sıra butonları
        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        topRow.setOpaque(false);
        
        // Alt sıra butonları
        JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        bottomRow.setOpaque(false);

        // Ana işlem butonları üst sıraya
        String[][] topButtons = {
            {"Yeni Mekan", "add"},
            {"Düzenle", "edit"},
            {"Sil", "delete"},
            {"Yenile", "refresh"},
            {"Değerlendirme Ekle", "review"},
            {"Değerlendirmeler", "view"}
        };

        // Sadece yedekleme butonları alt sıraya
        String[][] bottomButtons = {
            {"Yedek Al", "exportDatabase"},
            {"Yedekten Geri Yükle", "importDatabase"}
        };

        // Üst sıra butonlarını ekle
        for (String[] buttonInfo : topButtons) {
            JButton button = createActionButton(buttonInfo[0], buttonInfo[1]);
            topRow.add(button);
        }

        // Alt sıra butonlarını ekle
        for (String[] buttonInfo : bottomButtons) {
            JButton button = createActionButton(buttonInfo[0], buttonInfo[1]);
            bottomRow.add(button);
        }

        buttonPanel.add(topRow);
        buttonPanel.add(bottomRow);

        return buttonPanel;
    }

    private JButton createActionButton(String text, String iconName) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(textColor);
        button.setBackground(primaryColor);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(primaryColor, 2, true),
            BorderFactory.createEmptyBorder(12, 25, 12, 25)
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);

        // Hover efekti
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(accentColor);
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(accentColor, 2, true),
                    BorderFactory.createEmptyBorder(12, 25, 12, 25)
                ));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(primaryColor);
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(primaryColor, 2, true),
                    BorderFactory.createEmptyBorder(12, 25, 12, 25)
                ));
            }

            @Override
            public void mousePressed(MouseEvent e) {
                button.setBackground(primaryColor.darker());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                button.setBackground(primaryColor);
            }
        });

        // Buton işlevleri
        switch (iconName) {
            case "add":
                button.addActionListener(e -> {
                    RestaurantAddGUI addGui = new RestaurantAddGUI(this);
                    addGui.setResizable(false);
                    addGui.setVisible(true);
                });
                break;
            case "edit":
                button.addActionListener(e -> editSelectedRestaurant());
                break;
            case "delete":
                button.addActionListener(e -> deleteSelectedRestaurant());
                break;
            case "refresh":
                button.addActionListener(e -> updateRestaurantList());
                break;
            case "review":
                button.addActionListener(e -> {
                    String selectedRestaurant = restaurantList.getSelectedValue();
                    if (selectedRestaurant != null) {
                        String restaurantName = selectedRestaurant.split(" \\(")[0].trim();
                        ReviewAddGUI addReviewGui = new ReviewAddGUI(this, restaurantName, reviewManager);
                        addReviewGui.setVisible(true);
                    } else {
                        showWarning("Lütfen değerlendirme eklemek için bir mekan seçin.");
                    }
                });
                break;
            case "view":
                button.addActionListener(e -> {
                    String selectedRestaurant = restaurantList.getSelectedValue();
                    if (selectedRestaurant != null) {
                        String restaurantName = selectedRestaurant.split(" \\(")[0].trim();
                        ReviewsGUI reviewsGui = new ReviewsGUI(this, restaurantName, reviewManager);
                        reviewsGui.setVisible(true);
                    } else {
                        showWarning("Lütfen değerlendirmeleri görmek için bir mekan seçin.");
                    }
                });
                break;
            case "exportDatabase":
                button.addActionListener(e -> exportDatabase());
                break;
            case "importDatabase":
                button.addActionListener(e -> importDatabase());
                break;
        }

        return button;
    }

    private void addWindowDragListener() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                initialClick = e.getPoint();
                getComponentAt(initialClick);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                initialClick = null;
                isDragging = false;
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (!isDragging && initialClick != null) {
                    isDragging = true;
                }

                if (isDragging) {
                    int thisX = getLocation().x;
                    int thisY = getLocation().y;

                    int xMoved = (thisX + e.getX()) - (thisX + initialClick.x);
                    int yMoved = (thisY + e.getY()) - (thisY + initialClick.y);

                    int X = thisX + xMoved;
                    int Y = thisY + yMoved;
                    setLocation(X, Y);
                }
            }
        });
    }

    private void showWarning(String message) {
        // Özel stil ayarları
        UIManager.put("OptionPane.background", backgroundColor);
        UIManager.put("Panel.background", backgroundColor);
        UIManager.put("OptionPane.messageForeground", textColor);
        UIManager.put("Button.background", primaryColor);
        UIManager.put("Button.foreground", textColor);
        UIManager.put("Button.arc", 10);
        UIManager.put("Button.margin", new Insets(5, 15, 5, 15));

        JOptionPane.showMessageDialog(
            this,
            message,
            "Uyarı",
            JOptionPane.WARNING_MESSAGE
        );

        // Stil ayarlarını sıfırla
        UIManager.put("OptionPane.background", null);
        UIManager.put("Panel.background", null);
        UIManager.put("OptionPane.messageForeground", null);
        UIManager.put("Button.background", null);
        UIManager.put("Button.foreground", null);
    }

    /**
     * Liste modelini döndüren getter metodu
     */
    public DefaultListModel<String> getListModel() {
        return listModel;
    }

    /**
     * RestaurantManager nesnesini döndüren getter metodu
     */
    public RestaurantManager getRestaurantManager() {
        return restaurantManager;
    }

    /**
     * ReviewManager nesnesini döndüren getter metodu
     */
    public ReviewManager getReviewManager() {
        return reviewManager;
    }

    /**
     * Seçili restoranı düzenleme işlemini gerçekleştiren metod
     */
    private void editSelectedRestaurant() {
        String selectedRestaurant = restaurantList.getSelectedValue();
        if (selectedRestaurant == null) {
            showWarning("Lütfen düzenlemek için bir mekan seçin.");
            return;
        }

        try {
            // Mekan adını ayıkla (örnek: "Mantı Evi (Çarşı) - 4.5" -> "Mantı Evi")
            String restaurantName = selectedRestaurant.split(" \\(")[0].trim();
            
            // Veritabanından restoranı çek
            Session session = HibernateUtil.getSessionFactory().openSession();
            try {
                Query<Restaurant> query = session.createQuery(
                    "FROM Restaurant WHERE name = :name", Restaurant.class);
                query.setParameter("name", restaurantName);
                Restaurant restaurant = query.uniqueResult();

                if (restaurant != null) {
                    RestaurantEditGUI editGui = new RestaurantEditGUI(this, restaurant, restaurantManager);
                    editGui.setVisible(true);
                    // Düzenleme penceresini kapatınca listeyi güncelle
                    editGui.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosed(WindowEvent e) {
                            updateRestaurantList();
                        }
                    });
                } else {
                    showWarning("Mekan bulunamadı: " + restaurantName);
                }
            } finally {
                session.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showWarning("Düzenleme işlemi sırasında bir hata oluştu: " + e.getMessage());
        }
    }

    /**
     * Seçili restoranı silme işlemini gerçekleştiren metod
     */
    private void deleteSelectedRestaurant() {
        String selectedRestaurant = restaurantList.getSelectedValue();
        if (selectedRestaurant == null) {
            showWarning("Lütfen silmek için bir mekan seçin.");
            return;
        }

        try {
            String restaurantName = selectedRestaurant.split(" \\(")[0].trim();
            
            // Basit onay dialog'u
            UIManager.put("OptionPane.yesButtonText", "Evet");
            UIManager.put("OptionPane.noButtonText", "Hayır");
            
            int result = JOptionPane.showConfirmDialog(
                this,
                String.format("<html><body style='width: 300px; padding: 10px;'>" +
                    "<div style='margin-bottom: 15px;'>" +
                    "<b>%s</b> mekanını ve tüm değerlendirmelerini silmek istediğinize emin misiniz?" +
                    "</div>" +
                    "<div style='color: #FF6B6B; font-size: 12px;'>" +
                    "⚠️ Bu işlem geri alınamaz!" +
                    "</div></body></html>", 
                    restaurantName),
                "Mekan Sil",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            
            // Stil ayarlarını sıfırla
            UIManager.put("OptionPane.yesButtonText", null);
            UIManager.put("OptionPane.noButtonText", null);
            
            if (result == JOptionPane.YES_OPTION) {
                reviewManager.deleteAllReviewsForRestaurant(restaurantName);
                restaurantManager.deleteRestaurant(restaurantName);
                updateRestaurantList();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showWarning("Silme işlemi sırasında bir hata oluştu: " + e.getMessage());
        }
    }

    /**
     * Seçili restoranın değerlendirmelerini gösteren metod
     */
    private void showReviewsForSelectedRestaurant() {
        // Seçili öğe kontrolü
        int selectedIndex = restaurantList.getSelectedIndex();
        if (selectedIndex == -1) return;

        // Seçili restoranın adını al ve değerlendirme penceresini aç
        String selectedItem = restaurantList.getModel().getElementAt(selectedIndex);
        String restaurantName = selectedItem.split(" \\(")[0].trim();
        ReviewsGUI reviewsGui = new ReviewsGUI(this, restaurantName, reviewManager);
        reviewsGui.setVisible(true);
    }

    /**
     * Restoran listesini günceller.
     * Bu metod, veritabanından en güncel restoran listesini çeker ve UI'ı günceller.
     */
    public void updateRestaurantList() {
        // Önce listeyi temizle
        listModel.clear();
        
        // Thread havuzunu kullanarak işlemi gerçekleştir
        ThreadPoolManager.getInstance().executeIOTask(() -> {
            try {
                // Geçici bir liste oluştur
                java.util.List<String> tempList = new java.util.ArrayList<>();
                
                // SessionFactory'nin hazır olmasını bekle
                int retryCount = 0;
                boolean success = false;
                Exception lastError = null;
                
                while (!success && retryCount < 3) {
                    Session session = null;
                    try {
                        // Veritabanı bağlantısını al
                        session = HibernateUtil.getSessionFactory().openSession();
                        
                        @SuppressWarnings("unchecked")
                        List<Object[]> results = session.createNativeQuery(
                            "SELECT id, name, location, rating FROM public.restaurants ORDER BY name")
                            .list();
                        
                        // Verileri geçici listeye ekle
                        for (Object[] row : results) {
                            try {
                                Long id = row[0] == null ? null : Long.valueOf(row[0].toString());
                                String name = row[1] == null ? "" : row[1].toString();
                                String location = row[2] == null ? "" : row[2].toString();
                                double rating = row[3] == null ? 0.0 : Double.parseDouble(row[3].toString());
                                
                                String displayText = name + " (" + location + ") - " + 
                                                  String.format("%.1f", rating);
                                tempList.add(displayText);
                            } catch (Exception ex) {
                                System.err.println("Veri dönüşüm hatası: " + ex.getMessage());
                            }
                        }
                        
                        success = true;
                    } catch (Exception e) {
                        lastError = e;
                        System.err.println("Veritabanı sorgu hatası (Deneme " + (retryCount + 1) + "): " + e.getMessage());
                        e.printStackTrace();
                        
                        // Hata sonrası Hibernate'i yeniden başlatma
                        if (retryCount < 2) { // Son denemede yeniden başlatma
                            try {
                                Thread.sleep(1000); // Bekle
                                HibernateUtil.restart(); // Yeniden başlat
                                Thread.sleep(1000); // Yeniden başlatma sonrası bekle
                            } catch (Exception restartError) {
                                System.err.println("Hibernate yeniden başlatılamadı: " + restartError.getMessage());
                            }
                        }
                        
                        retryCount++;
                    } finally {
                        // Session'ı kapat
                        if (session != null && session.isOpen()) {
                            try {
                                session.close();
                            } catch (Exception e) {
                                System.err.println("Session kapatılırken hata: " + e.getMessage());
                            }
                        }
                    }
                }
                
                // Başarısız olursa ve hata varsa
                if (!success && lastError != null) {
                    throw lastError; // Son hatayı fırlat
                }
                
                // EDT'de UI güncellemesi yap
                final java.util.List<String> finalTempList = tempList;
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // Geçici listeyi model'e aktar
                            listModel.clear();
                            for (String item : finalTempList) {
                                listModel.addElement(item);
                            }
                            
                            // UI bileşenlerini güncelle
                            if (restaurantList != null) {
                                restaurantList.revalidate();
                                restaurantList.repaint();
                            }
                            
                            // Sonuç bildirimi
                            System.out.println("Restoran listesi güncellendi. Toplam restoran sayısı: " + listModel.size());
                        } catch (Exception e) {
                            System.err.println("Liste güncelleme hatası: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                });
            } catch (Exception e) {
                System.err.println("Veritabanı sorgu hatası: " + e.getMessage());
                e.printStackTrace();
                
                // Hata durumunda UI güncellemesi
                final Exception finalError = e;
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        listModel.clear();
                        listModel.addElement("Veritabanına bağlanılamadı, lütfen yenileyin veya uygulamayı yeniden başlatın.");
                        
                        if (restaurantList != null) {
                            restaurantList.revalidate();
                            restaurantList.repaint();
                        }
                        
                        JOptionPane.showMessageDialog(Main.this,
                            "Restoran listesi güncellenirken hata oluştu: " + finalError.getMessage() +
                            "\nLütfen 'Yenile' butonuna tıklayın veya uygulamayı yeniden başlatın.",
                            "Güncelleme Hatası",
                            JOptionPane.ERROR_MESSAGE);
                    }
                });
            }
        });
    }

    private void exportDatabase() {
        // Yedek almanın kolay yöntemi - otomatik klasör ve dosya adı
        // Tarih ve saat bilgisini ekle
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm");
        String timestamp = java.time.LocalDateTime.now().format(formatter);
        
        // Önce kullanıcıdan yedek adını alalım
        String baseFileName = JOptionPane.showInputDialog(this, 
            "Yedek dosyası için bir isim girin (örn: yedek):", 
            "Yedek İsmi", 
            JOptionPane.QUESTION_MESSAGE);
        
        if (baseFileName == null) {
            return; // Kullanıcı iptal etti
        }
        
        if (baseFileName.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Dosya adı boş olamaz!",
                "Hata",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Yedek klasörünü oluştur (eğer yoksa)
        File backupDir = new File("yedekler");
        if (!backupDir.exists()) {
            backupDir.mkdir();
            System.out.println("✓ 'yedekler' klasörü oluşturuldu");
        }
        
        String fileName = "yedekler/" + baseFileName + "_" + timestamp + ".backup";
        File backupFile = new File(fileName);
        
        // İşlem başlatıldı
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        // İşlem sürecini göstermek için ilerleme çubuğu
        JDialog progressDialog = new JDialog(this, "Yedekleme İşlemi", true);
        progressDialog.setSize(400, 150);
        progressDialog.setLocationRelativeTo(this);
        progressDialog.setLayout(new BorderLayout());
        
        JLabel statusLabel = new JLabel("İşlem başlatılıyor...", SwingConstants.CENTER);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(15, 10, 10, 10));
        
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setBorder(BorderFactory.createEmptyBorder(10, 30, 15, 30));
        
        progressDialog.add(statusLabel, BorderLayout.CENTER);
        progressDialog.add(progressBar, BorderLayout.SOUTH);
        
        // Thread'i başlat ve progress dialog'u göster
        new Thread(() -> {
            try {
                // Veritabanı bilgileri
                String dbName = "restaurant_db";
                String dbUser = "postgres";
                String dbPassword = "123456789";
                
                // 1. Veritabanı bağlantısını kontrol et
                SwingUtilities.invokeLater(() -> statusLabel.setText("Veritabanı bağlantısı kontrol ediliyor..."));
                ProcessBuilder checkDb = new ProcessBuilder(
                    "psql",
                    "-U", dbUser,
                    "-d", dbName,
                    "-c", "SELECT 1"
                );
                checkDb.environment().put("PGPASSWORD", dbPassword);
                Process checkProcess = checkDb.start();
                if (checkProcess.waitFor() != 0) {
                    throw new Exception("Veritabanına bağlanılamıyor! Lütfen PostgreSQL servisinin çalıştığından emin olun.");
                }
                
                // 2. pg_dump ile yedekleme yap
                SwingUtilities.invokeLater(() -> statusLabel.setText("Veritabanı yedekleniyor..."));
                ProcessBuilder pb = new ProcessBuilder(
                    "pg_dump",
                    "-U", dbUser,
                    "-d", dbName,
                    "--format=c",  // Custom format - daha güvenilir
                    "--blobs",     // Blob verilerini dahil et
                    "--encoding=UTF8", // Karakter kodlamasını belirt
                    "--verbose",   // Verbose mode
                    "--file=" + backupFile.getAbsolutePath()
                );
                
                pb.environment().put("PGPASSWORD", dbPassword);
                pb.redirectErrorStream(true);
                Process process = pb.start();
                
                // İşlem çıktısını oku
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                StringBuilder output = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                    final String currentLine = line;
                    SwingUtilities.invokeLater(() -> {
                        if (currentLine.contains("dumping") || currentLine.contains("dumped")) {
                            statusLabel.setText(currentLine);
                        }
                    });
                }
                
                int exitCode = process.waitFor();
                
                // İşlem tamamlandığında dialog'u kapat ve kullanıcıya bildir
                SwingUtilities.invokeLater(() -> {
                    progressDialog.dispose();
                    setCursor(Cursor.getDefaultCursor());
                    
                    if (exitCode == 0) {
                        // Yedek dosyasının boyutunu kontrol et
                        long fileSize = backupFile.length();
                        if (fileSize < 100) { // 100 byte'dan küçük dosyalar muhtemelen boştur
                            JOptionPane.showMessageDialog(this,
                                "Yedek dosyası çok küçük, yedekleme başarısız olmuş olabilir!",
                                "Uyarı",
                                JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        
                        JOptionPane.showMessageDialog(this,
                            "Veritabanı yedeği başarıyla alındı!\n\n" +
                            "Yedek dosyası: " + fileName + "\n" +
                            "Dosya boyutu: " + (fileSize / 1024) + " KB",
                            "Başarılı",
                            JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this,
                            "Yedekleme işlemi başarısız oldu! Çıkış kodu: " + exitCode,
                            "Hata",
                            JOptionPane.ERROR_MESSAGE);
                    }
                });
                
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    progressDialog.dispose();
                    setCursor(Cursor.getDefaultCursor());
                    
                    JOptionPane.showMessageDialog(Main.this,
                        "Yedek alınırken hata oluştu: " + e.getMessage(),
                        "Hata",
                        JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                });
            }
        }).start();
        
        progressDialog.setVisible(true);
    }

    private void importDatabase() {
        // Önce 'yedekler' klasörünü kontrol et
        File backupDir = new File("yedekler");
        if (!backupDir.exists() || !backupDir.isDirectory()) {
            JOptionPane.showMessageDialog(this,
                "'yedekler' klasörü bulunamadı! Önce bir yedek oluşturmanız gerekiyor.",
                "Hata",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Klasör içindeki yedek dosyalarını listele
        File[] backupFiles = backupDir.listFiles((dir, name) -> 
            name.toLowerCase().endsWith(".backup") || 
            name.toLowerCase().endsWith(".sql") || 
            name.toLowerCase().endsWith(".dump")
        );
        
        if (backupFiles == null || backupFiles.length == 0) {
            JOptionPane.showMessageDialog(this,
                "'yedekler' klasöründe hiç yedek dosyası bulunamadı! Önce bir yedek oluşturmanız gerekiyor.",
                "Hata",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Yedek dosyalarını tarihe göre sırala (en yenisi en üstte)
        java.util.Arrays.sort(backupFiles, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));

        // Kullanıcıya dosyaları göster ve seçim yaptır
        String[] fileNames = new String[backupFiles.length];
        for (int i = 0; i < backupFiles.length; i++) {
            java.util.Date fileDate = new java.util.Date(backupFiles[i].lastModified());
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = sdf.format(fileDate);
            
            fileNames[i] = backupFiles[i].getName() + " - " + formattedDate + 
                          " (" + (backupFiles[i].length() / 1024) + " KB)";
        }
        
        String selectedFileName = (String) JOptionPane.showInputDialog(this,
            "Geri yüklemek istediğiniz yedek dosyasını seçin:",
            "Yedek Seçimi",
            JOptionPane.QUESTION_MESSAGE,
            null,
            fileNames,
            fileNames[0]);
        
        if (selectedFileName == null) {
            return; // Kullanıcı iptal etti
        }
        
        // Seçilen dosyayı bul
        int selectedIndex = -1;
        for (int i = 0; i < fileNames.length; i++) {
            if (fileNames[i].equals(selectedFileName)) {
                selectedIndex = i;
                break;
            }
        }
        
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(this,
                "Seçim hatası oluştu.",
                "Hata",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        File selectedFile = backupFiles[selectedIndex];
        
        // Yedek dosyasının türünü kontrol et
        boolean isCustomFormat = selectedFile.getName().toLowerCase().endsWith(".backup") || 
                                selectedFile.getName().toLowerCase().endsWith(".dump");
                                
        // SQL formatındaki dosyaları analiz et
        if (!isCustomFormat) {
            String sqlAnalysis = analyzeSqlFile(selectedFile);
            System.out.println("\n=== SQL Dosya Analizi ===");
            System.out.println(sqlAnalysis);
            
            // SQL dosyasında veri var mı kontrol et
            if (!sqlAnalysis.contains("INSERT komutları: 0") && 
                !sqlAnalysis.contains("Restaurant INSERT komutları var mı: true")) {
                JOptionPane.showMessageDialog(this,
                    "Bu yedek dosyasında restoran verisi bulunamadı! Lütfen başka bir yedek dosyası seçin.",
                    "Geçersiz Yedek",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        
        // Onay iste
        int confirm = JOptionPane.showConfirmDialog(this,
            "⚠️ DİKKAT: Bu işlem mevcut verileri silecek ve yerine yedekteki verileri yükleyecek!\n" +
            "⚠️ Bu işlem geri alınamaz ve mevcut verileriniz kaybolacaktır!\n\n" +
            "Devam etmek istiyor musunuz?",
            "Onay",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm != JOptionPane.YES_OPTION) {
            return; // Kullanıcı iptal etti
        }
        
        // İşlem başlatıldı
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        // İşlem sürecini göstermek için ilerleme çubuğu
        JDialog progressDialog = new JDialog(this, "Geri Yükleme İşlemi", true);
        progressDialog.setSize(400, 150);
        progressDialog.setLocationRelativeTo(this);
        progressDialog.setLayout(new BorderLayout());
        
        JLabel statusLabel = new JLabel("İşlem başlatılıyor...", SwingConstants.CENTER);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(15, 10, 10, 10));
        
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setBorder(BorderFactory.createEmptyBorder(10, 30, 15, 30));
        
        progressDialog.add(statusLabel, BorderLayout.CENTER);
        progressDialog.add(progressBar, BorderLayout.SOUTH);
        
        // Thread'i başlat ve progress dialog'u göster
        Thread importThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Veritabanı bilgileri
                    String dbName = "restaurant_db";
                    String dbUser = "postgres";
                    String dbPassword = "123456789";
                    
                    // 1. Hibernate oturumunu kapat ve bekle
                    final JLabel statusLabelFinal = statusLabel; // final referans
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            statusLabelFinal.setText("Veritabanı bağlantısı kapatılıyor...");
                        }
                    });
                    HibernateUtil.shutdown();
                    Thread.sleep(1000);
                    
                    // 2. Aktif bağlantıları sonlandır
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            statusLabelFinal.setText("Aktif veritabanı bağlantıları sonlandırılıyor...");
                        }
                    });
                    terminateActiveConnections();
                    Thread.sleep(1000);
                    
                    // 3. Tablo yapısını hazırla (Özellikle SQL formatı için)
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            statusLabelFinal.setText("Veritabanı yapısı hazırlanıyor...");
                        }
                    });
                    
                    if (!isCustomFormat) {
                        // Tablo yapısını oluştur
                        File schemaScript = new File("schema.sql");
                        try (java.io.PrintWriter writer = new java.io.PrintWriter(schemaScript)) {
                            writer.println("-- Tabloları oluştur (eğer yoksa)");
                            writer.println("CREATE TABLE IF NOT EXISTS public.restaurants (");
                            writer.println("    id SERIAL PRIMARY KEY,");
                            writer.println("    name VARCHAR(255) NOT NULL,");
                            writer.println("    location VARCHAR(255),");
                            writer.println("    rating FLOAT DEFAULT 0.0,");
                            writer.println("    address TEXT");
                            writer.println(");");
                            writer.println();
                            writer.println("CREATE TABLE IF NOT EXISTS public.reviews (");
                            writer.println("    id SERIAL PRIMARY KEY,");
                            writer.println("    restaurant_id BIGINT,");
                            writer.println("    restaurant_name VARCHAR(255),");
                            writer.println("    rating FLOAT DEFAULT 0.0,");
                            writer.println("    comment TEXT,");
                            writer.println("    date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,");
                            writer.println("    CONSTRAINT fk_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE");
                            writer.println(");");
                        }
                        
                        ProcessBuilder schemaPb = new ProcessBuilder(
                            "psql",
                            "-U", dbUser,
                            "-d", dbName,
                            "-f", schemaScript.getAbsolutePath()
                        );
                        schemaPb.environment().put("PGPASSWORD", dbPassword);
                        schemaPb.redirectErrorStream(true);
                        Process schemaProcess = schemaPb.start();
                        
                        BufferedReader schemaReader = new BufferedReader(new InputStreamReader(schemaProcess.getInputStream()));
                        String schemaLine;
                        while ((schemaLine = schemaReader.readLine()) != null) {
                            System.out.println("Schema: " + schemaLine);
                        }
                        schemaProcess.waitFor();
                        schemaScript.delete();
                    }
                    
                    // 4. Mevcut verileri temizle
                    SwingUtilities.invokeLater(() -> statusLabel.setText("Mevcut verileri temizleme..."));
                    
                    // Temizleme betiği
                    File cleanupScript = new File("cleanup_data.sql");
                    try (java.io.PrintWriter writer = new java.io.PrintWriter(cleanupScript)) {
                        writer.println("-- FK kısıtlamalarını geçici olarak devre dışı bırak");
                        writer.println("SET session_replication_role = 'replica';");
                        writer.println();
                        writer.println("-- Tablolardaki verileri temizle (tabloları silmeden)");
                        writer.println("TRUNCATE TABLE public.reviews CASCADE;");
                        writer.println("TRUNCATE TABLE public.restaurants CASCADE;");
                        writer.println();
                        writer.println("-- FK kısıtlamalarını yeniden etkinleştir");
                        writer.println("SET session_replication_role = 'origin';");
                    }
                    
                    ProcessBuilder cleanupPb = new ProcessBuilder(
                        "psql",
                        "-U", dbUser,
                        "-d", dbName,
                        "-f", cleanupScript.getAbsolutePath()
                    );
                    cleanupPb.environment().put("PGPASSWORD", dbPassword);
                    cleanupPb.redirectErrorStream(true);
                    Process cleanupProcess = cleanupPb.start();
                    cleanupProcess.waitFor();
                    cleanupScript.delete();
                    
                    // 5. Verileri geri yükle - format bazlı
                    int insertCount = 0;
                    
                    if (isCustomFormat) {
                        // Custom format için pg_restore kullan
                        SwingUtilities.invokeLater(() -> statusLabel.setText("Custom format yedeği geri yükleniyor..."));
                        
                        ProcessBuilder restorePb = new ProcessBuilder(
                            "pg_restore",
                            "--clean",
                            "--if-exists",
                            "-U", dbUser,
                            "-d", dbName,
                            "--verbose",
                            selectedFile.getAbsolutePath()
                        );
                        restorePb.environment().put("PGPASSWORD", dbPassword);
                        restorePb.redirectErrorStream(true);
                        Process restoreProcess = restorePb.start();
                        
                        BufferedReader restoreReader = new BufferedReader(new InputStreamReader(restoreProcess.getInputStream()));
                        String restoreLine;
                        while ((restoreLine = restoreReader.readLine()) != null) {
                            final String lineText = restoreLine; // final kopyasını oluştur
                            System.out.println(lineText);
                            if (lineText.contains("processing")) {
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        statusLabelFinal.setText(lineText);
                                    }
                                });
                            }
                        }
                        
                        int exitCode = restoreProcess.waitFor();
                        if (exitCode != 0) {
                            System.err.println("pg_restore hata kodu: " + exitCode);
                        }
                        
                        insertCount = 1; // Başarılı varsay
                    } else {
                        // SQL formatı için psql kullan
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                statusLabelFinal.setText("SQL dosyasından veri yükleniyor...");
                            }
                        });
                        
                        ProcessBuilder sqlPb = new ProcessBuilder(
                            "psql",
                            "-U", dbUser,
                            "-d", dbName,
                            "-f", selectedFile.getAbsolutePath()
                        );
                        sqlPb.environment().put("PGPASSWORD", dbPassword);
                        sqlPb.redirectErrorStream(true);
                        Process sqlProcess = sqlPb.start();
                        
                        BufferedReader sqlReader = new BufferedReader(new InputStreamReader(sqlProcess.getInputStream()));
                        String sqlLine;
                        while ((sqlLine = sqlReader.readLine()) != null) {
                            System.out.println(sqlLine);
                            if (sqlLine.contains("INSERT 0 1")) {
                                insertCount++;
                                if (insertCount % 10 == 0) {
                                    final int currentCount = insertCount;
                                    SwingUtilities.invokeLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            statusLabelFinal.setText("Veri yükleniyor: " + currentCount + " kayıt");
                                        }
                                    });
                                }
                            }
                        }
                        
                        int exitCode = sqlProcess.waitFor();
                        if (exitCode != 0) {
                            System.err.println("psql hata kodu: " + exitCode);
                        }
                    }
                    
                    // 6. Sequence değerlerini düzelt
                    SwingUtilities.invokeLater(() -> statusLabel.setText("Veritabanı sequence değerleri düzeltiliyor..."));
                    
                    File seqScript = new File("fix_sequences.sql");
                    try (java.io.PrintWriter writer = new java.io.PrintWriter(seqScript)) {
                        writer.println("-- Sequence değerlerini düzelt");
                        writer.println("SELECT setval('public.restaurants_id_seq', COALESCE((SELECT MAX(id) FROM public.restaurants), 1), true);");
                        writer.println("SELECT setval('public.reviews_id_seq', COALESCE((SELECT MAX(id) FROM public.reviews), 1), true);");
                    }
                    
                    ProcessBuilder seqPb = new ProcessBuilder(
                        "psql",
                        "-U", dbUser,
                        "-d", dbName,
                        "-f", seqScript.getAbsolutePath()
                    );
                    seqPb.environment().put("PGPASSWORD", dbPassword);
                    Process seqProcess = seqPb.start();
                    seqProcess.waitFor();
                    seqScript.delete();
                    
                    // 7. Hibernate oturumunu yeniden başlat
                    SwingUtilities.invokeLater(() -> statusLabel.setText("Veritabanı bağlantısı yeniden başlatılıyor..."));
                    Thread.sleep(1000);
                    
                    // Tam bir yeniden başlatma için restart kullan
                    HibernateUtil.restart();
                    Thread.sleep(1000);
                    
                    // Veritabanı tabloları doğru oluşturuldu mu kontrol et
                    SwingUtilities.invokeLater(() -> statusLabel.setText("Veritabanı tabloları kontrol ediliyor..."));
                    
                    // 8. Uygulama veri yapılarını yeniden başlat ve kontrol et
                    SwingUtilities.invokeLater(() -> {
                        try {
                            // ProgressDialog'u güncelle
                            statusLabel.setText("Uygulama verilerini yükleme...");
                            
                            // Verileri sıfırla
                            listModel.clear();
                            
                            // RestaurantManager ve ReviewManager'ı yeniden oluştur
                            if (restaurantManager != null) {
                                restaurantManager = null;
                            }
                            if (reviewManager != null) {
                                reviewManager = null;
                            }
                            
                            // GC çağır (yardımcı olabilir)
                            System.gc();
                            Thread.sleep(500);
                            
                            // Yeni yöneticileri oluştur
                            restaurantManager = new RestaurantManager(listModel);
                            reviewManager = new ReviewManager();
                            reviewManager.setRestaurantManager(restaurantManager);
                            
                            // Veritabanını kontrol et
                            Session session = HibernateUtil.getSessionFactory().openSession();
                            try {
                                Number restaurantCount = (Number) session.createNativeQuery(
                                    "SELECT COUNT(*) FROM public.restaurants").uniqueResult();
                                Number reviewCount = (Number) session.createNativeQuery(
                                    "SELECT COUNT(*) FROM public.reviews").uniqueResult();
                                
                                System.out.println("\nVeritabanı durumu:");
                                System.out.println("- Restoran sayısı: " + restaurantCount);
                                System.out.println("- Değerlendirme sayısı: " + reviewCount);
                                
                                // Liste modelini yenile
                                updateRestaurantList();
                                
                                // Dialog'u kapat
                                progressDialog.dispose();
                                setCursor(Cursor.getDefaultCursor());
                                
                                // Başarı mesajı
                                String message = "Veritabanı yedeği başarıyla geri yüklendi!\n\n" +
                                               "Restoran sayısı: " + restaurantCount + "\n" +
                                               "Değerlendirme sayısı: " + reviewCount;
                                
                                if (restaurantCount.intValue() == 0) {
                                    message += "\n\nDİKKAT: Veritabanında hiç restoran verisi bulunamadı!";
                                    JOptionPane.showMessageDialog(Main.this, message, "Uyarı", JOptionPane.WARNING_MESSAGE);
                                } else {
                                    JOptionPane.showMessageDialog(Main.this, message, "Başarılı", JOptionPane.INFORMATION_MESSAGE);
                                }
                                
                            } finally {
                                session.close();
                            }
                            
                        } catch (Exception e) {
                            System.err.println("Uygulama verilerini yükleme hatası: " + e.getMessage());
                            e.printStackTrace();
                            
                            progressDialog.dispose();
                            setCursor(Cursor.getDefaultCursor());
                            
                            JOptionPane.showMessageDialog(Main.this,
                                "Veritabanı başarıyla geri yüklendi ancak uygulama verilerini yüklerken hata oluştu: " + e.getMessage() +
                                "\n\nLütfen uygulamayı yeniden başlatın.",
                                "Kısmi Başarı",
                                JOptionPane.WARNING_MESSAGE);
                        }
                    });
                    
                } catch (Exception e) {
                    System.err.println("Geri yükleme hatası: " + e.getMessage());
                    e.printStackTrace();
                    
                    // UI thread'inde hata mesajını göster
                    SwingUtilities.invokeLater(() -> {
                        progressDialog.dispose();
                        setCursor(Cursor.getDefaultCursor());
                        
                        JOptionPane.showMessageDialog(Main.this,
                            "Yedek geri yüklenirken hata oluştu: " + e.getMessage() +
                            "\n\nLütfen şunları kontrol edin:\n" +
                            "- PostgreSQL servisinin çalıştığından\n" +
                            "- Veritabanı kullanıcı adı/şifrenin doğru olduğundan\n" +
                            "- Yedeğin geçerli olduğundan",
                            "Geri Yükleme Hatası",
                            JOptionPane.ERROR_MESSAGE);
                        
                        // Bağlantıyı yeniden kurmayı dene
                        try {
                            HibernateUtil.restart();
                            restaurantManager = new RestaurantManager(listModel);
                            reviewManager = new ReviewManager();
                            reviewManager.setRestaurantManager(restaurantManager);
                            updateRestaurantList();
                        } catch (Exception reconnectError) {
                            System.err.println("Yeniden bağlanma hatası: " + reconnectError.getMessage());
                        }
                    });
                }
            }
        });
        
        importThread.start();
        
        progressDialog.setVisible(true);
    }

    private void showHelp() {
        String helpMessage = "<html><body style='width: 400px; padding: 10px;'>" +
            "<h2>Program Kullanım Kılavuzu</h2>" +
            "<p><b>Temel İşlemler:</b></p>" +
            "<ul>" +
            "<li><b>Yeni Mekan Ekle:</b> 'Yeni Mekan' butonuna tıklayarak yeni bir mekan ekleyebilirsiniz.</li>" +
            "<li><b>Mekan Düzenle:</b> Listeden bir mekan seçip 'Düzenle' butonuna tıklayarak mekanı düzenleyebilirsiniz.</li>" +
            "<li><b>Mekan Sil:</b> Listeden bir mekan seçip 'Sil' butonuna tıklayarak mekanı silebilirsiniz.</li>" +
            "</ul>" +
            "<p><b>Değerlendirmeler:</b></p>" +
            "<ul>" +
            "<li><b>Değerlendirme Ekle:</b> Bir mekan seçip 'Değerlendirme Ekle' ile yorum ve puan ekleyebilirsiniz.</li>" +
            "<li><b>Değerlendirmeleri Gör:</b> Bir mekan seçip 'Değerlendirmeler' ile tüm yorumları görebilirsiniz.</li>" +
            "</ul>" +
            "<p><b>Yedekleme İşlemleri:</b></p>" +
            "<ul>" +
            "<li><b>Yedek Al:</b> Tüm mekan ve değerlendirmeleri bir dosyaya yedekler.</li>" +
            "<li><b>Yedekten Geri Yükle:</b> Önceden alınmış bir yedeği sisteme geri yükler.</li>" +
            "</ul>" +
            "<p><b>Ayarlar:</b></p>" +
            "<ul>" +
            "<li><b>Ayarları Dışa/İçe Aktar:</b> Program ayarlarını yedekleyip geri yükleyebilirsiniz.</li>" +
            "</ul>" +
            "</body></html>";

        // Özel stil ayarları
        UIManager.put("OptionPane.background", backgroundColor);
        UIManager.put("Panel.background", backgroundColor);
        UIManager.put("OptionPane.messageForeground", textColor);

        JOptionPane.showMessageDialog(
            this,
            helpMessage,
            "Program Kullanım Kılavuzu",
            JOptionPane.INFORMATION_MESSAGE
        );

        // Stil ayarlarını sıfırla
        UIManager.put("OptionPane.background", null);
        UIManager.put("Panel.background", null);
        UIManager.put("OptionPane.messageForeground", null);
    }

    @XmlRootElement(name = "settings")
    private static class Settings {
        private boolean darkMode;
        private int windowWidth;
        private int windowHeight;
        private int windowX;
        private int windowY;

        @XmlElement
        public boolean isDarkMode() {
            return darkMode;
        }

        public void setDarkMode(boolean darkMode) {
            this.darkMode = darkMode;
        }

        @XmlElement
        public int getWindowWidth() {
            return windowWidth;
        }

        public void setWindowWidth(int windowWidth) {
            this.windowWidth = windowWidth;
        }

        @XmlElement
        public int getWindowHeight() {
            return windowHeight;
        }

        public void setWindowHeight(int windowHeight) {
            this.windowHeight = windowHeight;
        }

        @XmlElement
        public int getWindowX() {
            return windowX;
        }

        public void setWindowX(int windowX) {
            this.windowX = windowX;
        }

        @XmlElement
        public int getWindowY() {
            return windowY;
        }

        public void setWindowY(int windowY) {
            this.windowY = windowY;
        }
    }

    private void updateTheme() {
        if (isDarkMode) {
            // Koyu tema renkleri
            backgroundColor = new Color(18, 18, 18);
            surfaceColor = new Color(30, 30, 30);
            textColor = new Color(255, 255, 255, 230);
            secondaryTextColor = new Color(255, 255, 255, 160);
        } else {
            // Açık tema renkleri
            backgroundColor = new Color(245, 245, 245);
            surfaceColor = new Color(255, 255, 255);
            textColor = new Color(0, 0, 0, 230);
            secondaryTextColor = new Color(0, 0, 0, 160);
        }

        // Pencere arka planını güncelle
        SwingUtilities.invokeLater(() -> {
            // Ana panel arka planını güncelle
            Container contentPane = getContentPane();
            if (contentPane instanceof JPanel) {
                contentPane.setBackground(backgroundColor);
            }

            // Liste arka planını güncelle
            if (restaurantList != null) {
                restaurantList.setBackground(surfaceColor);
                restaurantList.setForeground(textColor);
            }

            // Tüm bileşenleri yeniden çiz
            repaint();
            revalidate();
        });
    }

    /**
     * SQL dosyasını analiz eder ve içeriğindeki komutları tarar
     * @param sqlFile İncelenecek SQL dosyası
     * @return Dosya içeriği hakkında özet bilgi
     */
    private String analyzeSqlFile(File sqlFile) {
        StringBuilder report = new StringBuilder();
        int insertCount = 0;
        int copyCount = 0;
        int createCount = 0;
        int alterCount = 0;
        int selectCount = 0;
        int otherCount = 0;
        boolean hasRestaurantInserts = false;
        boolean hasReviewInserts = false;
        boolean hasSequenceCommands = false;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(sqlFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                
                if (trimmed.isEmpty() || trimmed.startsWith("--")) {
                    continue; // Boş satır veya yorum satırı
                }
                
                if (trimmed.toUpperCase().startsWith("INSERT")) {
                    insertCount++;
                    if (trimmed.contains("restaurants")) {
                        hasRestaurantInserts = true;
                    }
                    if (trimmed.contains("reviews")) {
                        hasReviewInserts = true;
                    }
                } else if (trimmed.toUpperCase().startsWith("COPY")) {
                    copyCount++;
                } else if (trimmed.toUpperCase().startsWith("CREATE")) {
                    createCount++;
                } else if (trimmed.toUpperCase().startsWith("ALTER")) {
                    alterCount++;
                } else if (trimmed.toUpperCase().startsWith("SELECT")) {
                    selectCount++;
                    if (trimmed.contains("setval")) {
                        hasSequenceCommands = true;
                    }
                } else {
                    otherCount++;
                }
            }
            
            report.append("SQL Dosya Analizi: ").append(sqlFile.getName()).append("\n");
            report.append("Dosya boyutu: ").append(sqlFile.length() / 1024).append(" KB\n");
            report.append("INSERT komutları: ").append(insertCount).append("\n");
            report.append("COPY komutları: ").append(copyCount).append("\n");
            report.append("CREATE komutları: ").append(createCount).append("\n");
            report.append("ALTER komutları: ").append(alterCount).append("\n");
            report.append("SELECT komutları: ").append(selectCount).append("\n");
            report.append("Diğer komutlar: ").append(otherCount).append("\n");
            report.append("Restaurant INSERT komutları var mı: ").append(hasRestaurantInserts).append("\n");
            report.append("Review INSERT komutları var mı: ").append(hasReviewInserts).append("\n");
            report.append("Sequence komutları var mı: ").append(hasSequenceCommands).append("\n");
            
        } catch (IOException e) {
            report.append("Dosya okunamadı: ").append(e.getMessage());
        }
        
        return report.toString();
    }

    /**
     * Aktif veritabanı bağlantılarını sonlandırır
     */
    private void terminateActiveConnections() {
        System.out.println("Aktif veritabanı bağlantıları kontrol ediliyor...");
        
        // Önce aktif bağlantıları listele
        String listSql = "SELECT pid, usename, application_name, client_addr, state, query_start, query " +
                         "FROM pg_stat_activity " +
                         "WHERE datname = 'restaurant_db' AND pid <> pg_backend_pid();";
        
        try {
            // Önce PostgreSQL servisinin çalışıp çalışmadığını kontrol et
            ProcessBuilder checkPb = new ProcessBuilder(
                "psql",
                "-U", "postgres",
                "-d", "postgres",
                "-c", "SELECT 1"
            );
            checkPb.environment().put("PGPASSWORD", "123456789");
            checkPb.redirectErrorStream(true);
            
            Process checkProcess = checkPb.start();
            int exitCode = checkProcess.waitFor();
            
            if (exitCode != 0) {
                System.out.println("⚠️ PostgreSQL servisine bağlanılamıyor! Servisin çalıştığından emin olun.");
                System.out.println("İşleme devam ediliyor, ancak sorunlar oluşabilir...");
                return;
            }
            
            // Aktif bağlantıları listele
            ProcessBuilder listPb = new ProcessBuilder(
                "psql",
                "-U", "postgres",
                "-d", "postgres", // Ana veritabanına bağlan
                "-c", listSql
            );
            listPb.environment().put("PGPASSWORD", "123456789");
            listPb.redirectErrorStream(true);
            Process listProcess = listPb.start();
            BufferedReader listReader = new BufferedReader(new InputStreamReader(listProcess.getInputStream()));
            String line;
            boolean hasConnections = false;
            int lineCount = 0;
            
            while ((line = listReader.readLine()) != null) {
                lineCount++;
                if (lineCount > 2) { // Başlık satırlarını atla
                    hasConnections = true;
                    System.out.println("  Aktif bağlantı: " + line);
                }
            }
            
            if (!hasConnections) {
                System.out.println("✓ Aktif bağlantı bulunamadı, devam ediliyor...");
                return;
            }
            
            // Aktif bağlantıları sonlandır
            System.out.println("Aktif bağlantılar sonlandırılıyor...");
            
            // Daha güvenli bir SQL sorgusu kullan - sadece kendi bağlantımız dışındakileri sonlandır
            String terminateSql = "SELECT pg_terminate_backend(pid) FROM pg_stat_activity " +
                                 "WHERE datname = 'restaurant_db' AND pid <> pg_backend_pid();";
            
            ProcessBuilder terminatePb = new ProcessBuilder(
                "psql",
                "-U", "postgres",
                "-d", "postgres", // Ana veritabanına bağlan
                "-c", terminateSql
            );
            terminatePb.environment().put("PGPASSWORD", "123456789");
            terminatePb.redirectErrorStream(true);
            Process terminateProcess = terminatePb.start();
            BufferedReader terminateReader = new BufferedReader(new InputStreamReader(terminateProcess.getInputStream()));
            
            int successCount = 0;
            while ((line = terminateReader.readLine()) != null) {
                if (line.trim().equalsIgnoreCase("t")) {
                    successCount++;
                }
            }
            
            exitCode = terminateProcess.waitFor();
            if (exitCode != 0) {
                System.out.println("⚠️ Bağlantıları sonlandırırken bir sorun oluştu (Çıkış kodu: " + exitCode + ")");
                System.out.println("İşleme devam ediliyor...");
            } else {
                System.out.println("✓ " + successCount + " aktif bağlantı başarıyla sonlandırıldı");
            }
            
            // Bağlantıların gerçekten kapandığını doğrula
            Thread.sleep(1000); // Kısa bir süre bekle
            
            ProcessBuilder verifyPb = new ProcessBuilder(
                "psql",
                "-U", "postgres",
                "-d", "postgres",
                "-c", listSql
            );
            verifyPb.environment().put("PGPASSWORD", "123456789");
            verifyPb.redirectErrorStream(true);
            Process verifyProcess = verifyPb.start();
            BufferedReader verifyReader = new BufferedReader(new InputStreamReader(verifyProcess.getInputStream()));
            
            hasConnections = false;
            lineCount = 0;
            while ((line = verifyReader.readLine()) != null) {
                lineCount++;
                if (lineCount > 2) { // Başlık satırlarını atla
                    hasConnections = true;
                    System.out.println("  Hala aktif bağlantı: " + line);
                }
            }
            
            if (hasConnections) {
                System.out.println("⚠️ Bazı bağlantılar hala aktif, ancak işleme devam ediliyor...");
                
                // Son bir deneme daha yap
                System.out.println("Son bir deneme daha yapılıyor...");
                
                ProcessBuilder finalPb = new ProcessBuilder(
                    "psql",
                    "-U", "postgres",
                    "-d", "postgres",
                    "-c", terminateSql
                );
                finalPb.environment().put("PGPASSWORD", "123456789");
                finalPb.redirectErrorStream(true);
                Process finalProcess = finalPb.start();
                finalProcess.waitFor();
                
                Thread.sleep(1000); // Kısa bir süre bekle
            } else {
                System.out.println("✓ Tüm bağlantılar başarıyla sonlandırıldı");
            }
            
        } catch (Exception e) {
            System.out.println("⚠️ Aktif bağlantılar kontrol edilirken hata: " + e.getMessage());
            System.out.println("İşleme devam ediliyor...");
        }
    }

    /**
     * Uygulamayı başlatan main metodu.
     */
    public static void main(String[] args) {
        try {
            // Logger ve ExceptionHandler'ı başlat
            AppLogger logger = AppLogger.getInstance();
            ExceptionHandler exceptionHandler = ExceptionHandler.getInstance();
            logger.info("Uygulama başlatılıyor...");
            
            // Thread havuzunu başlat
            ThreadPoolManager threadPool = ThreadPoolManager.getInstance();
            logger.info("Thread havuzu başlatıldı");
            
            // Look and Feel ayarları
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            logger.debug("UI Look and Feel ayarlandı: " + UIManager.getSystemLookAndFeelClassName());
            
            // Hibernate'i başlat
            System.out.println("Veritabanı bağlantısı başlatılıyor...");
            logger.info("Veritabanı bağlantısı başlatılıyor...");
            try {
                HibernateUtil.getSessionFactory();
                System.out.println("Veritabanı bağlantısı başarılı.");
                logger.info("Veritabanı bağlantısı başarılı");
            } catch (Exception e) {
                logger.error("Veritabanı bağlantısı başarısız", e);
                System.err.println("Veritabanı bağlantısı başarısız: " + e.getMessage());
                JOptionPane.showMessageDialog(null,
                    "Veritabanı bağlantısı kurulamadı!\n\nHata: " + e.getMessage(),
                    "Veritabanı Hatası",
                    JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
            
            // GUI veya konsol modu belirleme
            final boolean consoleOnly = (args.length > 0 && args[0].equals("--console"));
            logger.info("Uygulama modu: " + (consoleOnly ? "Sadece Konsol" : "GUI + Konsol"));
            
            if (consoleOnly) {
                System.out.println("\n+---------------------------------------------+");
                System.out.println("|  YIYECEK MEKANLARI DEGERLENDIRME SISTEMI    |");
                System.out.println("|  KONSOL MODU (GUI ILE CALISTIRILDI)         |");
                System.out.println("+---------------------------------------------+");
                System.out.println("|  Devam etmek icin Enter'e basin...          |");
                System.out.println("+---------------------------------------------+");
                
                // Kullanıcı giriş bekle
                try {
                    System.in.read();
                } catch (IOException e) {
                    logger.error("Kullanıcı giriş hatası", e);
                }
                
                // ConsoleMenu'yü başlat
                logger.info("ConsoleMenu başlatılıyor...");
                Main frame = new Main();
                frame.setVisible(false);
                frame.setLocationRelativeTo(null);
                
                ConsoleMenu menu = new ConsoleMenu(frame);
                menu.start();
                
                // Program çıkışında kaynakları temizle
                try {
                    logger.info("Uygulama kapatılıyor...");
                    threadPool.shutdown(); // Thread havuzunu kapat
                    HibernateUtil.shutdown();
                    AppLogger.getInstance().close();
                } catch (Exception e) {
                    logger.error("Kaynaklar temizlenirken hata", e);
                }
                System.exit(0);
            } else {
                // GUI modu (+ Konsol menüsü)
                logger.info("GUI modu başlatılıyor...");
                
                // Global exception handler
                Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
                    logger.error("Yakalanmamış exception: " + thread.getName(), throwable);
                    exceptionHandler.handleGenericException(
                        new RuntimeException("Beklenmeyen bir hata oluştu. Lütfen uygulamayı yeniden başlatın.", throwable)
                    );
                });
                
                // Ana pencereyi oluştur ve göster
                SwingUtilities.invokeLater(() -> {
                    try {
                        Main frame = new Main();
                        frame.setVisible(true);
                        frame.setLocationRelativeTo(null);
                        
                        // GUI başarıyla başlatıldı
                        logger.info("GUI başarıyla başlatıldı.");
                        System.out.println("GUI başarıyla başlatıldı.");
                        
                        // Uygulama kapatıldığında kaynakları temizle
                        frame.addWindowListener(new WindowAdapter() {
                            @Override
                            public void windowClosing(WindowEvent e) {
                                try {
                                    logger.info("Uygulama kapatılıyor...");
                                    threadPool.shutdown(); // Thread havuzunu kapat
                                    HibernateUtil.shutdown();
                                    AppLogger.getInstance().close();
                                } catch (Exception ex) {
                                    logger.error("Kaynaklar temizlenirken hata", ex);
                                }
                            }
                        });
                        
                        // Konsol menüsünü ayrı thread'de başlat
                        new Thread(() -> {
                            try {
                                System.out.println("\n+---------------------------------------------+");
                                System.out.println("|  YIYECEK MEKANLARI DEGERLENDIRME SISTEMI    |");
                                System.out.println("|  KONSOL MODU (GUI ILE CALISTIRILDI)         |");
                                System.out.println("+---------------------------------------------+");
                                System.out.println("|  Devam etmek icin Enter'e basin...          |");
                                System.out.println("+---------------------------------------------+");
                                
                                // Enter tuşu bekleniyor
                                System.in.read();
                                while (System.in.available() > 0) {
                                    System.in.read();
                                }
                                
                                // Konsol menüsünü başlat
                                ConsoleMenu menu = new ConsoleMenu(frame);
                                menu.start();
                            } catch (Exception e) {
                                logger.error("Konsol başlatılamadı", e);
                                System.err.println("Konsol başlatılamadı: " + e.getMessage());
                            }
                        }).start();
                    } catch (Exception e) {
                        logger.error("GUI başlatılırken hata", e);
                        exceptionHandler.handleException(e);
                    }
                });
            }
        } catch (Exception e) {
            // En üst seviye hata yakalama
            System.err.println("Kritik hata: " + e.getMessage());
            e.printStackTrace();
            
            // Logger başlatılamadıysa direkt ekrana göster
            try {
                AppLogger.getInstance().error("Kritik başlatma hatası", e);
                ExceptionHandler.getInstance().handleGenericException(e);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null,
                    "Uygulama başlatılırken kritik bir hata oluştu:\n" + e.getMessage(),
                    "Kritik Hata",
                    JOptionPane.ERROR_MESSAGE);
            }
            
            System.exit(1);
        }
    }
}
