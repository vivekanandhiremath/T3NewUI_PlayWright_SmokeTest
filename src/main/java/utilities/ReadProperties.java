package utilities;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ReadProperties {

    private static final String CONFIG_FILE = "config/config.properties"; // Updated to use .properties file
    private static final CustomLogger logger = new CustomLogger();
    private static Properties properties;

    private static void loadProperties() {
        if (properties == null) {
            properties = new Properties();
            try (InputStream input = new FileInputStream(CONFIG_FILE)) {
                properties.load(input);
            } catch (IOException ex) {
                logger.logError("Unable to load application config from: " + CONFIG_FILE, ex);
            }
        }
    }

    public static boolean isLeadVerificationEnabled() {
        loadProperties();
        return Boolean.parseBoolean(properties.getProperty("lead.verification.enabled", "true"));
    }

    public static String getApplicationT3URL() {
        loadProperties();
        return properties.getProperty("t3BaseURL");
    }

    public static String getApplicationT1URL() {
        loadProperties();
        return properties.getProperty("t1BaseURL");
    }

    public static String getApplicationOREURL() {
        loadProperties();
        return properties.getProperty("oreBaseURL");
    }

    public static String getApplicationEncryptedURL() {
        loadProperties();
        return properties.getProperty("encryptedURL");
    }

    public static String getBrowser() {
        loadProperties();
        return properties.getProperty("browser", "chrome").toLowerCase();
    }

    public static String getVehicleType() {
        loadProperties();
        return properties.getProperty("vehicleType");
    }

    // ── Database properties (matches Python confiy/db_config.py MYSQL_CONFIG) ──

//    public static String getDbHost() {
//        loadProperties();
//        return properties.getProperty("db.host", "localhost");
//    }

    public static int getDbPort() {
        loadProperties();
        return Integer.parseInt(properties.getProperty("db.port", "3306"));
    }

    public static String getDbName() {
        loadProperties();
        return properties.getProperty("db.name", "");
    }

    public static String getDbUser() {
        loadProperties();
        return properties.getProperty("db.user", "");
    }

    public static String getDbPassword() {
        loadProperties();
        String envPassword = System.getenv("DB_PASSWORD");
        if (envPassword != null && !envPassword.trim().isEmpty()) {
            return envPassword.trim();
        }
        return properties.getProperty("db.password", "");
    }

    public static String getEnv() {
        loadProperties();
        return properties.getProperty("env");
    }

    public static String getDbHost() {
        loadProperties();

        String env = getEnv(); // UAT or PROD

        if ("PROD".equalsIgnoreCase(env)) {
            return properties.getProperty("db.host.prod");
        } else {
            return properties.getProperty("db.host.uat");
        }
    }
}