package utilities;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Provides a MySQL JDBC connection using credentials from config.properties.
 * Matches Python: DatabaseConnection(MYSQL_CONFIG) where MYSQL_CONFIG = DatabaseConfig(
 * host="eshop-us-uat.cbh6amtvql51.us-east-1.rds.amazonaws.com",
 * database="fca_ore", user="qa-ore", password="kbTzg6nVEmggXn5SzZNV")
 */
public class DatabaseConnection {

    private final CustomLogger logger = new CustomLogger();
    private Connection connection;

    /**
     * Builds the JDBC URL from config.properties entries.
     */
    private static String buildJdbcUrl() {
        return "jdbc:mysql://"
                + ReadProperties.getDbHost()
                + ":" + ReadProperties.getDbPort()
                + "/" + ReadProperties.getDbName()
                + "?useSSL=true&requireSSL=false&connectTimeout=10000&socketTimeout=30000";
    }

    /**
     * Returns a live Connection, creating or re-establishing it if necessary.
     * Matches Python: with db.get_connection() as conn: conn.cursor().execute("SELECT 1")
     */
    public Connection getConnection() {
        try {
            // Reuse existing connection if still valid
            if (connection != null && !connection.isClosed() && connection.isValid(3)) {
                return connection;
            }
        } catch (SQLException ignored) {
        }

        String url = buildJdbcUrl();
        String user = ReadProperties.getDbUser();
        String pass = ReadProperties.getDbPassword();

        logger.logInfo("Connecting to database: jdbc:mysql://"
                + ReadProperties.getDbHost() + ":" + ReadProperties.getDbPort()
                + "/" + ReadProperties.getDbName()
                + " as user=" + user);

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url, user, pass);

            // Python: cursor.execute("SELECT 1") – verify connection is alive
            try (var stmt = connection.createStatement()) {
                stmt.execute("SELECT 1");
            }
            logger.logInfo("Database connection established successfully.");
        } catch (ClassNotFoundException e) {
            logger.logError("MySQL JDBC driver not found on classpath: " + e.getMessage());
            connection = null;
        } catch (SQLException e) {
            logger.logError("Database connection failed: " + e.getMessage()
                    + " | URL=" + url + " | user=" + user);
            connection = null;
        }
        return connection;
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    logger.logInfo("Database connection closed.");
                }
            } catch (SQLException e) {
                logger.logError("Failed to close database connection: " + e.getMessage());
            } finally {
                connection = null;
            }
        }
    }
}
