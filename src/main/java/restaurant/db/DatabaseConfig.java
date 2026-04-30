package restaurant.db;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class DatabaseConfig {
    private static final String CONFIG_FILE = "/database.properties";
    private static final Properties PROPERTIES = loadProperties();

    private DatabaseConfig() {
    }

    public static String getUrl() {
        return PROPERTIES.getProperty("db.url", "jdbc:mysql://localhost:3306/restaurant_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
    }

    public static String getUser() {
        return PROPERTIES.getProperty("db.user", "root");
    }

    public static String getPassword() {
        return PROPERTIES.getProperty("db.password", "");
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream input = DatabaseConfig.class.getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Could not load database.properties", ex);
        }
        return properties;
    }
}
