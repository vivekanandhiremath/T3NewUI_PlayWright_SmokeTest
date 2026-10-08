package utilities;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Reads email settings from config/email_config.properties.
 * Keys use flat dot-notation (email.provider, email.sender.address, etc.)
 * matching the updated email_config.properties format.
 */
public class EmailConfigManager {

    private static final String CONFIG_FILE = "config/email_config.properties";
    private final CustomLogger logger = new CustomLogger();
    private final Properties properties = new Properties();

    public EmailConfigManager() {
        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            properties.load(input);
            logger.logInfo("Email configuration loaded from: " + CONFIG_FILE);
        } catch (IOException ex) {
            logger.logWarning("Failed to load email config: " + ex.getMessage());
        }
    }

    public boolean isEmailEnabled() {
        return Boolean.parseBoolean(properties.getProperty("email.enabled", "true"));
    }

    public String getProvider() {
        return properties.getProperty("email.provider", "gmail");
    }

    public String getSenderEmail() {
        return properties.getProperty("email.sender.address", "");
    }

    public String getSenderPassword() {
        String envPassword = System.getenv("EMAIL_APP_PASSWORD");
        if (envPassword != null && !envPassword.trim().isEmpty()) {
            return envPassword.trim();
        }
        return properties.getProperty("email.sender.password", "");
    }

    public String getSubjectPrefix() {
        return properties.getProperty("email.subject.prefix", "Test Report");
    }

    // ── Default recipients ────────────────────────────────────────────────────

    public List<String> getRecipientsTo() {
        return splitEmails(properties.getProperty("email.recipients.to", ""));
    }

    public List<String> getRecipientsCc() {
        return splitEmails(properties.getProperty("email.recipients.cc", ""));
    }

    public List<String> getRecipientsBcc() {
        return splitEmails(properties.getProperty("email.recipients.bcc", ""));
    }

    // ── Status-specific recipients (override defaults) ────────────────────────

    /**
     * Returns TO list for the given status ("pass", "fail", "error"). Falls back to default TO.
     */
    public List<String> getRecipientsToForStatus(String status) {
        String key = "email.recipients." + status.toLowerCase() + ".to";
        String val = properties.getProperty(key, "").trim();
        return val.isEmpty() ? getRecipientsTo() : splitEmails(val);
    }

    /**
     * Returns CC list for the given status. Falls back to default CC.
     */
    public List<String> getRecipientsCcForStatus(String status) {
        String key = "email.recipients." + status.toLowerCase() + ".cc";
        String val = properties.getProperty(key, "").trim();
        return val.isEmpty() ? getRecipientsCc() : splitEmails(val);
    }

    public boolean isSendOnPass() {
        return Boolean.parseBoolean(properties.getProperty("email.on.test.pass", "true"));
    }

    public boolean isSendOnFail() {
        return Boolean.parseBoolean(properties.getProperty("email.on.test.fail", "true"));
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private List<String> splitEmails(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
