package utilities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseUtils {

    private final DatabaseConnection dbConnection;
    private final CustomLogger logger = new CustomLogger();

    public DatabaseUtils(DatabaseConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    public List<Map<String, Object>> executeQuery(String query, Object... params) {
        List<Map<String, Object>> results = new ArrayList<>();
        Connection conn = dbConnection.getConnection();

        if (conn != null) {
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                for (int i = 0; i < params.length; i++) {
                    pstmt.setObject(i + 1, params[i]);
                }
                try (ResultSet rs = pstmt.executeQuery()) {
                    int columnCount = rs.getMetaData().getColumnCount();
                    while (rs.next()) {
                        Map<String, Object> row = new HashMap<>();
                        for (int i = 1; i <= columnCount; i++) {
                            row.put(rs.getMetaData().getColumnName(i), rs.getObject(i));
                        }
                        results.add(row);
                    }
                }
            } catch (SQLException e) {
                logger.logError("executeQuery failed: " + query, e);
            }
        }
        return results;
    }

    public int executeUpdate(String query, Object... params) {
        Connection conn = dbConnection.getConnection();
        int rowCount = 0;

        if (conn != null) {
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                for (int i = 0; i < params.length; i++) {
                    pstmt.setObject(i + 1, params[i]);
                }
                rowCount = pstmt.executeUpdate();
            } catch (SQLException e) {
                logger.logError("executeUpdate failed: " + query, e);
            }
        }
        return rowCount;
    }
}
