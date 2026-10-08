package utilities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBDataFetcher {

    private final Connection connection;
    private final CustomLogger logger = new CustomLogger();

    public DBDataFetcher(Connection connection) {
        this.connection = connection;
    }

    public boolean testSpecificLeadByEmail(String testEmail) throws SQLException {
        if (connection == null) {
            throw new SQLException("Database connection is null. Cannot perform lead lookup.");
        }

        // Validate email parameter - check for extra spaces
        String emailToQuery = testEmail.trim();
        if (!emailToQuery.equals(testEmail)) {
            logger.logWarning("Email parameter had leading/trailing spaces. Trimmed from: [" + testEmail + "] to: [" + emailToQuery + "]");
        }
        if (emailToQuery.contains(" ")) {
            logger.logWarning("Email parameter contains internal spaces: [" + emailToQuery + "]");
        }
        logger.logInfo("Email length: " + emailToQuery.length() + " characters");

        logger.logInfo("Querying lead with specific email: " + emailToQuery);
        // log DB catalog/schema for debugging
        try {
            String catalog = connection.getCatalog();
            logger.logInfo("Database catalog: " + catalog);
        } catch (Exception ignored) {
        }

        String query = "SELECT created_at, session_id, vehicle_vin, dealer_code, zip, email, comments, lead_id, lead_source, source_id, additional_details FROM leads WHERE email = ? ORDER BY created_at DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, emailToQuery);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    logger.logInfo("Found records for email: " + emailToQuery);
                    String email = rs.getString("email");
                    String leadId = rs.getString("lead_id");
                    String createdAt = rs.getString("created_at");

                    if (!emailToQuery.equals(email)) {
                        logger.logError("Email doesn't match. Expected: [" + emailToQuery + "], Got: [" + email + "]");
                        return false;
                    }
                    if (leadId == null || leadId.isEmpty()) {
                        logger.logError("Lead ID is missing");
                        return false;
                    }
                    if (createdAt == null || createdAt.isEmpty()) {
                        logger.logError("Created at timestamp is missing");
                        return false;
                    }

                    logger.logStep("Specific lead test passed");
                    return true;
                } else {
                    // fallback: try URL-decoded email (some systems store decoded values)
                    try {
                        String decoded = java.net.URLDecoder.decode(emailToQuery, java.nio.charset.StandardCharsets.UTF_8.name()).trim();
                        if (!decoded.equals(emailToQuery)) {
                            logger.logInfo("No records found for encoded value; trying decoded email: " + decoded);
                            logger.logInfo("Decoded email length: " + decoded.length() + " characters");
                            if (decoded.contains(" ")) {
                                logger.logWarning("Decoded email contains spaces: [" + decoded + "]");
                            }
                            try (PreparedStatement pstmt2 = connection.prepareStatement(query)) {
                                pstmt2.setString(1, decoded);
                                try (ResultSet rs2 = pstmt2.executeQuery()) {
                                    if (rs2.next()) {
                                        logger.logInfo("Found records for decoded email: " + decoded);
                                        String email = rs2.getString("email");
                                        String leadId = rs2.getString("lead_id");
                                        String createdAt = rs2.getString("created_at");
                                        if (!decoded.equals(email)) {
                                            logger.logError("Decoded email doesn't match DB value: expected [" + decoded + "], got [" + email + "]");
                                            return false;
                                        }
                                        if (leadId == null || leadId.isEmpty()) {
                                            logger.logError("Lead ID is missing");
                                            return false;
                                        }
                                        if (createdAt == null || createdAt.isEmpty()) {
                                            logger.logError("Created at timestamp is missing");
                                            return false;
                                        }
                                        logger.logStep("Specific lead test passed (decoded email)");
                                        return true;
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        logger.logError("Error while attempting decoded email fallback: " + e.getMessage());
                    }

                    logger.logError("No records found for email: " + emailToQuery);
                    return false;
                }
            }
        } catch (SQLException e) {
            logger.logError("Error in testSpecificLeadByEmail: " + e.getMessage());
            return false;
        }
    }

    public boolean testSpecificLeadInPrivateOffersByEmail(String testEmail) throws SQLException {
        if (connection == null) {
            throw new SQLException("Database connection is null. Cannot perform private offers lookup.");
        }

        // Validate email parameter - check for extra spaces
        String emailToQuery = testEmail.trim();
        if (!emailToQuery.equals(testEmail)) {
            logger.logWarning("Email parameter had leading/trailing spaces. Trimmed from: [" + testEmail + "] to: [" + emailToQuery + "]");
        }
        if (emailToQuery.contains(" ")) {
            logger.logWarning("Email parameter contains internal spaces: [" + emailToQuery + "]");
        }
        logger.logInfo("Email length: " + emailToQuery.length() + " characters");

        logger.logInfo("Querying private offers with specific email: " + emailToQuery);
        String query = "SELECT * FROM fca_ore_private_offers_details WHERE email = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, emailToQuery);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    logger.logInfo("Found records in private offers for email: " + emailToQuery);
                    String email = rs.getString("email");

                    if (!emailToQuery.equals(email)) {
                        logger.logError("Email doesn't match: expected " + emailToQuery + ", got " + email);
                        return false;
                    }

                    logger.logStep("Private offer verification passed");
                    return true;
                } else {
                    logger.logError("No records found in private offers");
                    return false;
                }
            }
        } catch (SQLException e) {
            logger.logError("Error in testSpecificLeadInPrivateOffersByEmail: " + e.getMessage());
            return false;
        }
    }
}