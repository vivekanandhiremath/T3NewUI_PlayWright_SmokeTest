package utilities;

import com.microsoft.playwright.Page;

public class EncryptEmail {

    private final DriverUtils driverUtils;
    private final CustomLogger logger = new CustomLogger();
    private Page page;

    public EncryptEmail(boolean headless) {
        this.driverUtils = new DriverUtils(headless);
    }

    public void initializeDriver() {
        if (this.page == null) {
            this.page = driverUtils.initializeDriver();
        }
    }

    public String getEncryptedEmail(String plainEmail, String baseUrl) {
        try {
            // Lazy-start encryption browser only when encryption is actually invoked.
            initializeDriver();
            logger.logInfo("Attempting to get encrypted email");
            page.navigate(baseUrl);
            logger.logInfo("Successfully navigated to " + baseUrl);

            page.locator("//input[@id='plain_text']").fill(plainEmail);
            logger.logInfo("Entered email in input field");

            page.locator("//input[@value='encrypt']").click();
            logger.logInfo("Clicked encrypt button");

            String encryptedValue = page.locator("//h3[contains(text(), 'Encrypted Value:')]/following-sibling::p").textContent().trim();

            if (encryptedValue.isEmpty()) {
                throw new RuntimeException("Empty encrypted value received");
            }

            // Remove any extra spaces within the encrypted value
            encryptedValue = encryptedValue.replaceAll("\\s+", "");
            logger.logInfo("Successfully encrypted email: " + encryptedValue);
            logger.logInfo("Encrypted email length: " + encryptedValue.length() + " characters");
            return encryptedValue;
        } catch (Exception e) {
            logger.logError("Error in getEncryptedEmail: " + e.getMessage());
            throw new RuntimeException("All attempts to encrypt email failed", e);
        }
    }

    public void close() {
        if (driverUtils != null) {
            driverUtils.quitDriver();
        }
    }
}
