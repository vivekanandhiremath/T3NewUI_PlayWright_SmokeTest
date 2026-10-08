package utilities;

import com.microsoft.playwright.Page;

import java.util.List;

public class EShopCAEnvironmentDetector {

    private static final String PROD_SCRIPT_URL = "https://dealeradmin.stellantisdigital.ca/js/t3-redesign/app-eshopca-redesign-prod.js";
    private static final String UAT_SCRIPT_URL = "https://dealeradmin.stellantisdigital.ca/js/t3-redesign/app-eshopca-redesign-.js";

    private final Page page;
    private final CustomLogger logger;

    public EShopCAEnvironmentDetector(Page page, CustomLogger logger) {
        this.page = page;
        this.logger = logger;
    }

    public DetectedEnvironment detect() {
        try {
            List<String> scriptSrcs = (List<String>) page.evaluate(
                    "() => Array.from(document.querySelectorAll('script[src]')).map(s => s.src)"
            );

            if (scriptSrcs == null || scriptSrcs.isEmpty()) {
                logger.logWarning("No script tags found on the page.");
                return DetectedEnvironment.UNKNOWN;
            }

            logger.logInfo("Found " + scriptSrcs.size() + " script tags on the page.");

            boolean foundProd = false;
            boolean foundUat = false;

            for (String src : scriptSrcs) {
                if (src == null) continue;

                if (src.contains("app-eshopca-redesign-prod.js")) {
                    foundProd = true;
                    logger.logInfo("PROD script detected: " + src);
                } else if (src.contains("app-eshopca-redesign-.js") && !src.contains("app-eshopca-redesign-prod.js")) {
                    foundUat = true;
                    logger.logInfo("UAT script detected: " + src);
                }
            }

            for (String src : scriptSrcs) {
                if (src != null && src.contains("dealeradmin.stellantisdigital.ca/js/t3-redesign")) {
                    logger.logInfo("EShop CA script found: " + src);
                }
            }

            if (foundProd) {
                logger.logInfo("Environment detected as PRODUCTION.");
                return DetectedEnvironment.PROD;
            } else if (foundUat) {
                logger.logInfo("Environment detected as UAT.");
                return DetectedEnvironment.UAT;
            } else {
                logger.logWarning("Neither PROD nor UAT EShop CA script found on the page.");
                return DetectedEnvironment.UNKNOWN;
            }
        } catch (Exception e) {
            logger.logError("Failed to detect environment from scripts: " + e.getMessage());
            return DetectedEnvironment.UNKNOWN;
        }
    }

    public enum DetectedEnvironment {
        PROD,
        UAT,
        UNKNOWN
    }
}
