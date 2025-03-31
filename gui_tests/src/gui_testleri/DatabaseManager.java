package gui_testleri;

import java.io.*;
import java.util.List;
import java.util.ArrayList;
import org.hibernate.Session;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;

public class DatabaseManager {
    private RestaurantManager restaurantManager;
    private ReviewManager reviewManager;

    public DatabaseManager(RestaurantManager restaurantManager, ReviewManager reviewManager) {
        this.restaurantManager = restaurantManager;
        this.reviewManager = reviewManager;
    }

    public void exportToFile(String fileName) throws IOException {
        try {
            DatabaseData data = new DatabaseData();
            data.setRestaurants(restaurantManager.getAllRestaurants());
            data.setReviews(reviewManager.getAllReviews());

            JAXBContext context = JAXBContext.newInstance(DatabaseData.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(data, new File(fileName));
        } catch (JAXBException e) {
            throw new IOException("XML dışa aktarma hatası: " + e.getMessage(), e);
        }
    }

    public void importFromFile(String fileName) throws IOException {
        try {
            JAXBContext context = JAXBContext.newInstance(DatabaseData.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            DatabaseData data = (DatabaseData) unmarshaller.unmarshal(new File(fileName));
            
            Session session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();
            
            // Mevcut verileri temizle
            session.createQuery("DELETE FROM Review").executeUpdate();
            session.createQuery("DELETE FROM Restaurant").executeUpdate();
            
            // Yeni verileri ekle
            for (Restaurant restaurant : data.getRestaurants()) {
                session.save(restaurant);
            }
            
            for (Review review : data.getReviews()) {
                session.save(review);
            }
            
            session.getTransaction().commit();
            session.close();
        } catch (JAXBException e) {
            throw new IOException("XML içe aktarma hatası: " + e.getMessage(), e);
        }
    }

    @XmlRootElement(name = "database")
    private static class DatabaseData {
        private List<Restaurant> restaurants = new ArrayList<>();
        private List<Review> reviews = new ArrayList<>();

        @XmlElement(name = "restaurants")
        public List<Restaurant> getRestaurants() {
            return restaurants;
        }

        public void setRestaurants(List<Restaurant> restaurants) {
            this.restaurants = restaurants;
        }

        @XmlElement(name = "reviews")
        public List<Review> getReviews() {
            return reviews;
        }

        public void setReviews(List<Review> reviews) {
            this.reviews = reviews;
        }
    }
} 