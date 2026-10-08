package utilities;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PhotosGallaryApiHandler {

    private final Page page;
    private final CustomLogger logger;

    public PhotosGallaryApiHandler(Page page, CustomLogger logger) {
        this.page = page;
        this.logger = logger;
    }

    public PhotosGallaryResult captureAndValidate(Runnable ctaClickAction) {
        try {
            Response photosResponse = page.waitForResponse(
                    resp -> resp.url().contains("dlr.apicarzato.com/api/photos_gallary"),
                    ctaClickAction
            );

            if (photosResponse == null || !photosResponse.ok()) {
                logger.logWarning("photos_gallary API response not captured or not OK.");
                return new PhotosGallaryResult(false, photosResponse != null ? photosResponse.status() : 0,
                        "", 0, new ArrayList<>(), null, "", "", "", "");
            }

            String responseText = photosResponse.text();
            Map<String, Object> jsonBody = (Map<String, Object>) page.evaluate("json => JSON.parse(json)", responseText);

            String apiStatus = (String) jsonBody.getOrDefault("status", "");
            int apiCode = ((Number) jsonBody.getOrDefault("code", 0)).intValue();
            List<String> messages = (List<String>) jsonBody.getOrDefault("messages", new ArrayList<>());
            Map<String, Object> result = (Map<String, Object>) jsonBody.get("result");

            String creditFlag = "";
            String make = "";
            String stockNumber = "";
            if (result != null) {
                creditFlag = (String) result.getOrDefault("credit_flag", "");
                make = (String) result.getOrDefault("make", "");
                stockNumber = (String) result.getOrDefault("stock_number", "");
            }

            return new PhotosGallaryResult(true, photosResponse.status(), apiStatus, apiCode,
                    messages, result, creditFlag, make, stockNumber, responseText);

        } catch (Exception e) {
            logger.logWarning("photos_gallary API capture failed: " + e.getMessage());
            return new PhotosGallaryResult(false, 0, "", 0, new ArrayList<>(), null, "", "", "", "");
        }
    }

    public boolean validateApiResponse(PhotosGallaryResult apiResult) {
        boolean allValid = true;

        if (!apiResult.captured) {
            logger.logError("API response was not captured.");
            return false;
        }

        logger.logInfo("HTTP status: " + apiResult.httpStatus);
        logger.logInfo("API status: " + apiResult.apiStatus + " | code: " + apiResult.apiCode);

        if (!"OK".equals(apiResult.apiStatus)) {
            logger.logError("API status is not OK: " + apiResult.apiStatus);
            allValid = false;
        }

        if (apiResult.apiCode != 200) {
            logger.logError("API code is not 200: " + apiResult.apiCode);
            allValid = false;
        }

        if (apiResult.messages != null && !apiResult.messages.isEmpty()) {
            logger.logWarning("API messages is not empty: " + apiResult.messages);
        }

        if (apiResult.result == null) {
            logger.logError("API result object is null.");
            return false;
        }

        logger.logInfo("credit_flag: " + apiResult.creditFlag);
        logger.logInfo("Make: " + apiResult.make + " | Stock: " + apiResult.stockNumber);

        return allValid;
    }

    public boolean isDt2026CreditFlag(PhotosGallaryResult apiResult) {
        return apiResult.captured && "dt2026".equals(apiResult.creditFlag);
    }

    public boolean verifyDt2026CreditFormLoaded() {
        try {
            logger.logInfo("Scanning page and frames for dt2026 credit form (#dtform-final-submit)...");
            // Check main page first
            if (page.locator("#dtform-final-submit").count() > 0) {
                logger.logInfo("dt2026 credit application form found on main page.");
                return true;
            }
            // Check all frames
            for (com.microsoft.playwright.Frame frame : page.frames()) {
                try {
                    if (frame.locator("#dtform-final-submit").count() > 0) {
                        logger.logInfo("dt2026 credit application form found in iframe: " + frame.url());
                        return true;
                    }
                } catch (Exception ignored) {
                }
            }
            // If not found immediately, wait up to 15 seconds, checking every 1 second
            for (int i = 0; i < 15; i++) {
                page.waitForTimeout(1000);
                if (page.locator("#dtform-final-submit").count() > 0) {
                    logger.logInfo("dt2026 credit application form found on main page after waiting.");
                    return true;
                }
                for (com.microsoft.playwright.Frame frame : page.frames()) {
                    try {
                        if (frame.locator("#dtform-final-submit").count() > 0) {
                            logger.logInfo("dt2026 credit application form found in iframe after waiting: " + frame.url());
                            return true;
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
            logger.logWarning("dt2026 credit application form (#dtform-final-submit) not found in page or any iframe.");
            return false;
        } catch (Exception e) {
            logger.logWarning("Error searching for dt2026 credit application form: " + e.getMessage());
            return false;
        }
    }

    public static class PhotosGallaryResult {
        public final boolean captured;
        public final int httpStatus;
        public final String apiStatus;
        public final int apiCode;
        public final List<String> messages;
        public final Map<String, Object> result;
        public final String creditFlag;
        public final String make;
        public final String stockNumber;
        public final String rawJson;

        public PhotosGallaryResult(boolean captured, int httpStatus, String apiStatus, int apiCode,
                                   List<String> messages, Map<String, Object> result,
                                   String creditFlag, String make, String stockNumber, String rawJson) {
            this.captured = captured;
            this.httpStatus = httpStatus;
            this.apiStatus = apiStatus;
            this.apiCode = apiCode;
            this.messages = messages;
            this.result = result;
            this.creditFlag = creditFlag;
            this.make = make;
            this.stockNumber = stockNumber;
            this.rawJson = rawJson;
        }
    }
}
