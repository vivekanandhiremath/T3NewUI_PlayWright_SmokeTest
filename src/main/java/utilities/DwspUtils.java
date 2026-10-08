package utilities;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class DwspUtils {

    private Page page;
    private CustomLogger logger;

    public DwspUtils(Page page, CustomLogger logger) {
        this.page = page;
        this.logger = logger;
    }

    /**
     * Get the new vehicles path based on DWSP
     */
    public static String getNewVehiclesPath(String dwsp) {
        if (dwsp == null) dwsp = "";
        String lower = dwsp.toLowerCase();

        if (lower.contains("dealer on")) return "searchnew.aspx";
        if (lower.contains("team velocity")) return "inventory/new";
        if (lower.contains("dealer inspire")) return "new-vehicles/";
        if (lower.contains("dealer.com")) return "new-inventory/index.htm";

        return "new-vehicles/";
    }

    /**
     * Get the used vehicles path based on DWSP
     */
    public static String getUsedVehiclesPath(String dwsp) {
        if (dwsp == null) dwsp = "";
        String lower = dwsp.toLowerCase();

        if (lower.contains("dealer on")) return "searchused.aspx";
        if (lower.contains("team velocity")) return "inventory/used";
        if (lower.contains("dealer inspire")) return "used-vehicles/";
        if (lower.contains("dealer.com")) return "used-inventory/index.htm";

        return "used-vehicles/";
    }

    /**
     * Extract base URL (scheme + host only)
     */
    public static String extractBaseUrl(String url) {
        try {
            java.net.URL parsed = new java.net.URL(url);
            return parsed.getProtocol() + "://" + parsed.getHost();
        } catch (Exception e) {
            return url;
        }
    }

    /**
     * Append path to URL intelligently
     */
    public static String appendPathToUrl(String url, String path) {
        String pathWithoutTrailingSlash = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
        if (url.contains(pathWithoutTrailingSlash)) {
            return url.endsWith("/") ? url : url + "/";
        }
        return url.endsWith("/") ? url + path : url + "/" + path;
    }

    /**
     * Detects the DWSP (Dealer Website Service Provider)
     */
    public String checkDwsp() {
        page.waitForTimeout(1500);

        String currentUrl;
        try {
            currentUrl = page.url();
        } catch (Exception e) {
            logger.logWarning("Driver state check failed: " + e.getMessage());
            return "Unknown";
        }

        // Scroll to load dynamic content
        try {
            logger.logInfo("Scrolling page to load DWSP content...");
            page.evaluate("() => window.scrollTo(0, 0)");
            page.waitForTimeout(500);
            page.evaluate("() => window.scrollTo(0, document.body.scrollHeight)");
            page.waitForTimeout(1000);
            page.evaluate("() => window.scrollTo(0, document.body.scrollHeight / 2)");
            page.waitForTimeout(500);
        } catch (Exception e) {
            logger.logWarning("Scrolling failed: " + e.getMessage());
        }

        // 1. DealerOn detection
        try {
            page.evaluate("() => window.scrollTo(0, document.body.scrollHeight)");
            page.waitForTimeout(400);
            Locator dealerOnLogo = page.locator("img[alt='DealerOn Logo'], a[href*='dealeron.com/do-info']").first();
            dealerOnLogo.waitFor(new Locator.WaitForOptions().setTimeout(2000));
            if (dealerOnLogo.isVisible()) {
                logger.logInfo("DealerOn detected");
                return "dealer on";
            }
        } catch (Exception e) {
            logger.logWarning("DealerOn detection error: " + e.getMessage());
        }

        // 2. Team Velocity
        try {
            if (isTeamVelocityDetected()) {
                logger.logInfo("Team Velocity detected");
                return "Team Velocity";
            }
        } catch (Exception e) {
            logger.logWarning("Team Velocity detection error: " + e.getMessage());
        }

        // 3. Dealer Inspire
        try {
            if (isDealerInspireDetected()) {
                logger.logInfo("Dealer Inspire detected");
                return "dealer inspire";
            }
        } catch (Exception e) {
            logger.logWarning("Dealer Inspire detection error: " + e.getMessage());
        }

        // 4. Dealer.com
        try {
            if (isDealerDotComDetected()) {
                logger.logInfo("Dealer.com detected");
                return "dealer.com";
            }
        } catch (Exception e) {
            logger.logWarning("Dealer.com detection error: " + e.getMessage());
        }

        logger.logWarning("DWSP not detected, returning Unknown");
        return "Unknown";
    }

    private boolean isTeamVelocityDetected() {
        try {
            Object result = page.evaluate(
                    "() => document.querySelector('a[href*=\"teamvelocitymarketing.com\"]') !== null || " +
                            "document.querySelector('a[href*=\"teamvelocity.com\"]') !== null"
            );
            return Boolean.TRUE.equals(result);
        } catch (Exception ignored) {
        }
        return false;
    }

    private boolean isDealerInspireDetected() {
        try {
            Object result = page.evaluate(
                    "() => document.querySelector('a[href*=\"dealerinspire.com\"]') !== null || " +
                            "document.querySelector('a[href*=\"www.dealerinspire.com\"]') !== null"
            );
            return Boolean.TRUE.equals(result);
        } catch (Exception ignored) {
        }
        return false;
    }

    private boolean isDealerDotComDetected() {
        try {
            Object result = page.evaluate(
                    "() => document.querySelector('a[href*=\"dealer.com\"]') !== null || " +
                            "document.querySelector('a[href*=\"www.dealer.com\"]') !== null"
            );
            return Boolean.TRUE.equals(result);
        } catch (Exception ignored) {
        }
        return false;
    }

    /**
     * Build full URL for new vehicles based on DWSP
     */
    public String buildNewVehiclesUrl(String baseUrl, String dwsp) {
        String newPath = getNewVehiclesPath(dwsp);
        String fullUrl = appendPathToUrl(baseUrl, newPath);
        logger.logInfo("New vehicles URL: " + fullUrl);
        return fullUrl;
    }

    /**
     * Build full URL for used vehicles based on DWSP
     */
    public String buildUsedVehiclesUrl(String baseUrl, String dwsp) {
        String usedPath = getUsedVehiclesPath(dwsp);
        String fullUrl = appendPathToUrl(baseUrl, usedPath);
        logger.logInfo("Used vehicles URL: " + fullUrl);
        return fullUrl;
    }
}

