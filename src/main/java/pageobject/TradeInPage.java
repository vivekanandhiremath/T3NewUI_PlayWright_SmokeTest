package pageobject;

import com.microsoft.playwright.Frame;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import utilities.CustomLogger;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TradeInPage extends BasePage {

    private static final String CURRENT_VEHICLE =
            "input[placeholder='Enter Vehicle Year, Brand, and Model']";
    private static final String AUTOCOMPLETE_ITEMS =
            "ul.aa-List li.aa-Item";
    private static final String MILEAGE = "input[name='mileage']";
    private static final String ZIP = "input[name='zip']";
    private static final String NEXT_BUTTON = "button#describe-your-vehicle-button";
    private static final String GET_ESTIMATE_BUTTON = "button:has-text('Get Your Estimate')";
    private static final String TRADE_IN_PRICE = "span.fw-extrabold.mb-1.fs-px-42";
    private static final String TRADE_IN_PRICE_MAIN = "span.fw-extrabold.mb-1.fs-px-42";
    private static final String NO_IDONT_BUTTON = "button:has-text(\"No, I don't\")";
    private static final String TAB_LABELS = ".mat-button-toggle-label-content";
    private static final String ACTIVE_TAB = ".mat-button-toggle.mat-button-toggle-checked .mat-button-toggle-label-content";
    private static final String OVERLAYS = ".cdk-overlay-backdrop.cdk-overlay-dark-backdrop.cdk-overlay-backdrop-showing, .cdk-overlay-backdrop";
    private static final String TP_CURRENT_VEHICLE = "#content1";
    private static final String TP_AUTOCOMPLETE_ITEMS = "li.autocomplete-result";
    private static final String TP_ZIP = "#zipCode";
    private static final String TP_MILEAGE = "#mileage";
    private static final String TP_NO_OWE = "#oweNo";
    private static final String TP_SUBMIT = "#nextgetEstimateBtn";
    private static final String TP_PRICE = "span.gcss-typography-utility-heading-1.estimate-price-s";
    private static final String TP_RESULT_HEADING = "text=Your Trade-In Estimate";
    private static final String KBB_SKIP_STEP = "text=Skip this step";
    private static final String KBB_MILEAGE_INPUT = "input#mileage";
    private static final String KBB_ZIP_INPUT = "input#zipCode";
    private static final String KBB_MILEAGE_NEXT = "#nextModule";
    private static final String KBB_RANGE_HEADING = "text=Kelley Blue Book";
    private static final List<String> TRADE_IN_INPUT_LOCATORS = Arrays.asList(
            "xpath=(//input[@id='trade_in_lease'])[2]",
            "xpath=(//input[@id='trade_in_finance'])[2]",
            "xpath=(//input[@id='trade_in_cash'])[2]",
            "xpath=//input[contains(@id, 'trade_in_')]",
            "css=input[id*='trade_in_']",
            "css=.trade-in-value"
    );
    private static final int DEFAULT_WAIT_MS = 10_000;
    private final CustomLogger logger = new CustomLogger();

    public TradeInPage(Page page) {
        super(page);
    }

    private String cleanNumeric(String raw) {
        return raw == null ? "" : raw.replaceAll("[^0-9.]", "");
    }

    private boolean safeClick(Locator locator, double timeoutMs) {
        try {
            locator.first().waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(timeoutMs));
            locator.first().scrollIntoViewIfNeeded();
            locator.first().click(new Locator.ClickOptions().setTimeout(timeoutMs));
            return true;
        } catch (Exception ignored) {
            try {
                locator.first().click(new Locator.ClickOptions().setForce(true).setTimeout(timeoutMs));
                return true;
            } catch (Exception ignoredAgain) {
                return false;
            }
        }
    }

    // -----------------------
    // frame detection
    // -----------------------

    public Frame waitForIframeBySrc(String srcSubstring, int timeoutMs) {
        logger.logInfo("Waiting for iframe containing: " + srcSubstring);
        long start = System.currentTimeMillis();

        try {
            page.url();
        } catch (Exception browserError) {
            logger.logWarning("Browser stability check failed: " + browserError.getMessage());
            return null;
        }

        boolean loggedOnce = false;
        while (System.currentTimeMillis() - start < timeoutMs) {
            try {
                page.url();
            } catch (Exception browserError) {
                logger.logWarning("Browser stability check failed: " + browserError.getMessage());
                return null;
            }

            List<Frame> frames = page.frames();

            // Log all frame sources once for debugging (Python parity – debug only)
            if (!loggedOnce) {
                loggedOnce = true;
                logger.logInfo("Found " + frames.size() + " frame(s) on page");
                for (int i = 0; i < frames.size(); i++) {
                    try {
                        logger.logInfo("Frame " + (i + 1) + " src: " + frames.get(i).url());
                    } catch (Exception ignored) {
                    }
                }
            }

            for (Frame frame : frames) {
                try {
                    String url = frame.url();
                    if (url != null && url.contains(srcSubstring)) {
                        logger.logInfo("Found matching iframe: " + url);
                        return frame;
                    }
                } catch (Exception ignored) {
                }
            }

            page.waitForTimeout(300);
        }

        logger.logWarning("No iframe found with src containing: " + srcSubstring);
        try {
            logger.logInfo("Current frame count: " + page.frames().size());
        } catch (Exception ignored) {
        }
        return null;
    }

    // -----------------------
    // iframe form flow
    // -----------------------

    public void selectCurrentVehicle(Frame frame, String searchText) {
        try {
            frame.locator(CURRENT_VEHICLE).fill(searchText);
            frame.waitForTimeout(400);

            // Wait for loading spinner to disappear (if present)
            try {
                frame.locator(".aa-InputWrapperSuffix .aa-LoadingIndicator")
                        .first()
                        .waitFor(new Locator.WaitForOptions()
                                .setState(WaitForSelectorState.HIDDEN)
                                .setTimeout(DEFAULT_WAIT_MS));
            } catch (Exception ignored) {
                logger.logInfo("No loading indicator found or it did not appear");
            }

            // Wait for autocomplete items
            try {
                frame.locator(AUTOCOMPLETE_ITEMS).first().waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(DEFAULT_WAIT_MS));
            } catch (Exception ignored) {
                logger.logInfo("Autocomplete items did not appear within timeout, trying fallback");
            }

            // Primary path: visible non-hidden items
            Locator visibleItems = frame.locator("ul.aa-List li.aa-Item:not([hidden])");
            page.waitForTimeout(500);

            if (visibleItems.count() > 0) {
                if (!safeClick(visibleItems.first(), DEFAULT_WAIT_MS)) {
                    throw new IllegalStateException("Could not click autocomplete item for: " + searchText);
                }
                logger.logInfo("Selected vehicle from autocomplete: " + searchText);
            } else {
                // Fallback path (Python parity): try first-word exact match on aa-Item spans
                String firstWord = searchText.contains(" ") ? searchText.split(" ")[0] : searchText;
                Locator exactMatch = frame.locator(
                        "//li[contains(@class,'aa-Item')]//span[contains(.,'" + firstWord + "')]"
                );
                if (exactMatch.count() > 0) {
                    if (!safeClick(exactMatch.first(), DEFAULT_WAIT_MS)) {
                        throw new IllegalStateException("Fallback exact-match click failed for: " + searchText);
                    }
                    logger.logInfo("Selected exact match for: " + searchText);
                } else {
                    throw new IllegalStateException("No matching vehicles found in autocomplete for: " + searchText);
                }
            }

            // Wait for dropdown to close
            try {
                frame.locator("ul.aa-List").first().waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.HIDDEN)
                        .setTimeout(DEFAULT_WAIT_MS));
            } catch (Exception ignored) {
                logger.logWarning("Autocomplete dropdown did not close after selection");
            }

        } catch (Exception e) {
            logger.logError("Error selecting vehicle", e);
            throw e;
        }
    }

    public void clickNoIDontButton(Frame frame, int maxAttempts) {
        int attempts = 0;
        while (attempts < maxAttempts) {
            try {
                Locator button = frame.locator(NO_IDONT_BUTTON).first();
                if (safeClick(button, DEFAULT_WAIT_MS)) {
                    page.waitForTimeout(500);
                    logger.logInfo("Clicked 'No, I don't' button");
                    return;
                }
            } catch (Exception ignored) {
                // Retry.
            }
            attempts++;
            page.waitForTimeout(1000);
        }
        throw new IllegalStateException("Failed to click 'No, I don't' button after " + maxAttempts + " attempts");
    }

    public String getTradeInPrice(Frame frame) {
        String priceText = frame.locator(TRADE_IN_PRICE).first().textContent();
        String cleanPrice = cleanNumeric(priceText);
        logger.logInfo("Clean numeric price from UI: " + cleanPrice);
        return cleanPrice;
    }

    public String fillBlackBookIframeForm(Frame frame, String vehicle, String mileage, String zipCode) {
        try {
            selectCurrentVehicle(frame, vehicle);
            frame.locator(MILEAGE).fill(mileage);
            frame.locator(ZIP).fill(zipCode);

            safeClick(frame.locator(NEXT_BUTTON).first(), DEFAULT_WAIT_MS);
            page.waitForTimeout(6000);

            try {
                safeClick(frame.locator(GET_ESTIMATE_BUTTON).first(), DEFAULT_WAIT_MS);
            } catch (Exception ignored) {
                logger.logWarning("Could not click Get Estimate button");
            }

            page.waitForTimeout(6000);
            clickNoIDontButton(frame, 3);

            try {
                frame.evaluate("() => window.scrollTo({top: 0, behavior: 'smooth'})");
                page.waitForTimeout(1000);
            } catch (Exception ignored) {
                // Continue.
            }

            String price = getTradeInPrice(frame);
            logger.logInfo("Submitted trade-in form inside Black Book iframe");
            return price;
        } catch (Exception e) {
            logger.logError("Error filling Black Book iframe form", e);
            return "";
        }
    }

    public String fillTradePendingIframeForm(Frame frame, String vehicle, String mileage, String zipCode) {
        try {
            // Step 1: Make/Model search (default mode) with autocomplete
            Locator currentVehicle = frame.locator(TP_CURRENT_VEHICLE).first();
            currentVehicle.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(DEFAULT_WAIT_MS));
            currentVehicle.click();
            currentVehicle.fill(vehicle);
            page.waitForTimeout(1000);

            // Step 2: wait for autocomplete and click first item
            Locator autoComplete = frame.locator(TP_AUTOCOMPLETE_ITEMS).first();
            autoComplete.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(DEFAULT_WAIT_MS));
            if (!safeClick(autoComplete, DEFAULT_WAIT_MS)) {
                throw new IllegalStateException("Could not click TradePending autocomplete item for: " + vehicle);
            }
            logger.logInfo("Selected vehicle from TradePending autocomplete: " + vehicle);
            page.waitForTimeout(1000);

            // Step 3: ZIP and Mileage
            frame.locator(TP_ZIP).first().fill(zipCode);
            frame.locator(TP_MILEAGE).first().fill(mileage);
            logger.logInfo("Filled TradePending ZIP and Mileage");

            // Step 4: select "No, I don't" for owed money
            Locator oweNo = frame.locator(TP_NO_OWE).first();
            oweNo.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(DEFAULT_WAIT_MS));
            oweNo.check(new Locator.CheckOptions().setForce(true));
            logger.logInfo("Selected 'No, I don't' in TradePending form");

            // Step 5: submit and wait for the estimate result
            safeClick(frame.locator(TP_SUBMIT).first(), DEFAULT_WAIT_MS);
            frame.locator(TP_RESULT_HEADING).first()
                    .waitFor(new Locator.WaitForOptions()
                            .setState(WaitForSelectorState.VISIBLE)
                            .setTimeout(20_000));
            page.waitForTimeout(1000);

            String price = getTradePendingPrice(frame);
            logger.logInfo("Submitted trade-in form inside TradePending iframe. Price: " + price);
            return price;
        } catch (Exception e) {
            logger.logError("Error filling TradePending iframe form", e);
            return "";
        }
    }

    public String getTradePendingPrice(Frame frame) {
        try {
            Locator price = frame.locator(TP_PRICE).first();
            price.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(DEFAULT_WAIT_MS));
            String raw = price.textContent();
            String clean = cleanNumeric(raw);
            logger.logInfo("TradePending price from UI: " + clean);
            return clean;
        } catch (Exception e) {
            logger.logWarning("Could not extract TradePending price: " + e.getMessage());
            return "";
        }
    }

    private boolean pickKbbOption(Frame frame, String text) {
        // KBB renders every step's options (Year, Make, Model, ...) as <a role="button"> links.
        String selector = "a[role='button']:has-text('" + text + "')";
        for (int attempt = 0; attempt < 6; attempt++) {
            try {
                Locator option = frame.locator(selector).last();
                option.waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(8000));
                option.click(new Locator.ClickOptions().setTimeout(8000));
                page.waitForTimeout(2500);
                return true;
            } catch (Exception ignored) {
                page.waitForTimeout(2000);
            }
        }
        logger.logWarning("Could not select KBB option: " + text);
        return false;
    }

    public String fillKBBIframeForm(Frame frame, String year, String make, String model, String style, String engine, String transmission, String mileage) {
        try {
            // Step 0: skip the optional contact/lead step
            try {
                Locator skip = frame.locator(KBB_SKIP_STEP).first();
                skip.waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(8000));
                skip.click();
                page.waitForTimeout(2000);
                logger.logInfo("Skipped KBB contact step");
            } catch (Exception ignored) {
                logger.logInfo("KBB contact step not present or already skipped");
            }

            // Step 1-6: sequential option pickers (Year, Make, Model, Style, Engine, Transmission)
            boolean ok = pickKbbOption(frame, year);
            ok = pickKbbOption(frame, make) && ok;
            ok = pickKbbOption(frame, model) && ok;
            ok = pickKbbOption(frame, style) && ok;
            ok = pickKbbOption(frame, engine) && ok;
            ok = pickKbbOption(frame, transmission) && ok;
            if (!ok) {
                logger.logWarning("KBB option selection incomplete - flow may not complete");
            }

            // Step 7: mileage input + ZIP + Next
            try {
                Locator mileageInput = frame.locator(KBB_MILEAGE_INPUT).first();
                mileageInput.waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(8000));
                mileageInput.fill(mileage);
                try {
                    Locator zip = frame.locator(KBB_ZIP_INPUT).first();
                    if (zip.isVisible()) {
                        zip.fill("10001");
                    }
                } catch (Exception ignored) {
                }
                page.waitForTimeout(1500);
                Locator next = frame.locator(KBB_MILEAGE_NEXT).first();
                next.waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(8000));
                next.click(new Locator.ClickOptions().setTimeout(8000));
                page.waitForTimeout(4000);
                logger.logInfo("Entered KBB mileage and clicked Next");
            } catch (Exception e) {
                logger.logWarning("Could not enter KBB mileage (Next stayed disabled): " + e.getMessage());
            }

            // Step 8: equipment + color + condition, then "Get Your Report"
            try {
                selectKbbColor(frame, "Black");
                pickKbbInputByLabel(frame, "Good");
                page.waitForTimeout(1000);
                Locator getReport = frame.locator("//button[@id='nextModule']").first();
                getReport.waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(8000));
                getReport.click(new Locator.ClickOptions().setTimeout(8000));
                page.waitForTimeout(5000);
                logger.logInfo("Selected KBB color/condition and clicked Get Your Report");
            } catch (Exception e) {
                logger.logWarning("Could not complete KBB equipment/color/condition step: " + e.getMessage());
            }

            // Step 9: capture the trade-in range price
            String price = getKBBTradeInPrice(frame);
            logger.logInfo("KBB trade-in range price: " + price);
            return price;
        } catch (Exception e) {
            logger.logError("Error filling KBB iframe form", e);
            return "";
        }
    }

    public String getKBBTradeInPrice(Frame frame) {
        try {
            page.waitForTimeout(3000);
            Object value = null;
            for (int i = 0; i < 6 && (value == null || value.toString().isEmpty()); i++) {
                value = frame.evaluate("""
                            () => {
                                // Only consider visible elements (offsetParent not null) to skip hidden <b> labels.
                                const visibles = Array.from(document.querySelectorAll('span, div, p, h1, h2, h3, h4, h5, strong'))
                                    .filter(e => e.offsetParent !== null);
                                for (const e of visibles) {
                                    const t = (e.textContent || '').trim();
                                    if (/\\$\\s?\\d/.test(t) && t.length < 30) {
                                        return t;
                                    }
                                }
                                const m = (document.body.innerText || '').match(/\\$\\s?([\\d,]+)/);
                                return m ? m[0] : '';
                            }
                        """);
                if (value == null || value.toString().isEmpty()) {
                    page.waitForTimeout(3000);
                }
            }
            String price = value == null ? "" : value.toString();
            String clean = cleanNumeric(price);
            logger.logInfo("KBB trade-in range price from UI: " + clean);
            return clean;
        } catch (Exception e) {
            logger.logWarning("Could not extract KBB trade-in price: " + e.getMessage());
            return "";
        }
    }

    private void selectKbbColor(Frame frame, String color) {
        for (int attempt = 0; attempt < 4; attempt++) {
            try {
                Locator dropdown = frame.locator("#colorDropdown").first();
                dropdown.waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(6000));
                dropdown.selectOption(color);
                logger.logInfo("Selected KBB color: " + color);
                page.waitForTimeout(500);
                return;
            } catch (Exception e) {
                page.waitForTimeout(1000);
            }
        }
        logger.logWarning("Could not select KBB color: " + color);
    }

    private void pickKbbInputByLabel(Frame frame, String labelText) {
        // Color/condition options are rendered as inputs with a visible label. Click the element
        // whose text matches (parent input or its label wrapper).
        for (int attempt = 0; attempt < 4; attempt++) {
            try {
                Locator el = frame.locator(
                        "//label[normalize-space(.)='" + labelText + "'] | //*[@class and contains(.,'" + labelText + "') and not(.//*[contains(.,'" + labelText + "')])]//input").first();
                if (el.count() == 0) {
                    el = frame.locator("input[id*='" + labelText.toLowerCase() + "']").first();
                }
                if (el.count() == 0) {
                    logger.logWarning("Could not find KBB input for label: " + labelText);
                    return;
                }
                el.waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(6000));
                el.check(new Locator.CheckOptions().setForce(true));
                logger.logInfo("Selected KBB option: " + labelText);
                page.waitForTimeout(500);
                return;
            } catch (Exception e) {
                page.waitForTimeout(1000);
            }
        }
        logger.logWarning("Could not select KBB input: " + labelText);
    }

    // -----------------------
    // main entry
    // -----------------------

    public Map<String, String> handleTradeIn() {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("provider", "None");
        result.put("price", "");

        try {
            try {
                page.url();
            } catch (Exception browserError) {
                logger.logError("Browser session is not active", browserError);
                return result;
            }

            // Switch back to default content before looking for iframes (Python parity)
            logger.logInfo("Switched to default content before iframe search");
            page.waitForTimeout(5000);

            // --- Black Book ---
            Frame blackBook = waitForIframeBySrc("app.blackbookinformation.com", 10_000);
            if (blackBook != null) {
                logger.logInfo("Black Book iframe found");
                // Check browser stability before interacting
                try {
                    page.url();
                } catch (Exception e) {
                    logger.logError("Browser became unstable before Black Book interaction", e);
                    return result;
                }
                String price = fillBlackBookIframeForm(blackBook, "123", "1000", "10001");
                result.put("provider", "Black Book");
                result.put("price", price);
                logger.logInfo("Completed Trade-In flow for Black Book");
                return result;
            }

            // --- KBB ---
            Frame kbb = waitForIframeBySrc("tradeinadvisor.kbb.com", 10_000);
            if (kbb != null) {
                logger.logInfo("KBB iframe found");
                try {
                    page.url();
                } catch (Exception e) {
                    logger.logError("Browser became unstable before KBB interaction", e);
                    return result;
                }
                String price = fillKBBIframeForm(kbb, "2020", "Jeep", "Wrangler Unlimited",
                        "Rubicon Sport Utility 4D", "V6, VVT, 3.6 Liter", "Automatic, 8-Spd", "10000");
                result.put("provider", "Kelley Blue Book");
                result.put("price", price);
                logger.logInfo("Completed Trade-In flow for Kelley Blue Book");
                return result;
            }

            // --- TradePending ---
            Frame tradePending = waitForIframeBySrc("apicarzato.com", 10_000);
            if (tradePending != null) {
                logger.logInfo("TradePending iframe found");
                try {
                    page.url();
                } catch (Exception e) {
                    logger.logError("Browser became unstable before TradePending interaction", e);
                    return result;
                }
                String price = fillTradePendingIframeForm(tradePending, "2020 Jeep Wrangler", "1000", "10001");
                result.put("provider", "TradePending");
                result.put("price", price);
                logger.logInfo("Completed Trade-In flow for TradePending");
                return result;
            }

            // --- Fallback: neither Black Book nor KBB nor TradePending found – log and continue, do NOT stop execution ---
            logger.logWarning("Neither Black Book, KBB nor TradePending trade-in iframe found. Skipping trade-in flow and continuing test.");
            return result;
        } catch (Exception e) {
            logger.logError("Error in Trade-In iframe interaction", e);
            return result;
        }
    }

    // -----------------------
    // tab + value comparison
    // -----------------------

    public boolean switchToTabByLabel(String tabLabel) {
        try {
            try {
                Locator overlays = page.locator(OVERLAYS);
                if (overlays.count() > 0) {
                    page.evaluate("() => document.querySelectorAll('.cdk-overlay-backdrop').forEach(o => o.style.display = 'none')");
                    page.waitForTimeout(500);
                }
            } catch (Exception ignored) {
                // Continue without overlay handling.
            }

            Locator labels = page.locator(TAB_LABELS);
            int count = labels.count();
            for (int i = 0; i < count; i++) {
                Locator label = labels.nth(i);
                String text = (label.textContent() == null) ? "" : label.textContent().trim();
                if (text.equalsIgnoreCase(tabLabel)) {
                    if (safeClick(label, 5000)) {
                        page.waitForTimeout(1000);
                        logger.logInfo("Switched to tab: " + tabLabel);
                        return true;
                    }
                }
            }

            logger.logWarning("Tab not found with label: " + tabLabel);
            return false;
        } catch (Exception e) {
            logger.logError("Error switching tab by label", e);
            return false;
        }
    }

    public boolean switchToTab(String tabName) {
        String name = tabName == null ? "" : tabName.trim();
        if (!("Lease".equalsIgnoreCase(name) || "Finance".equalsIgnoreCase(name) || "Cash".equalsIgnoreCase(name))) {
            logger.logWarning("Invalid tab name: " + tabName);
            return false;
        }
        return switchToTabByLabel(name);
    }

    public String getActiveTabName() {
        try {
            // Strategy 1: checked mat-button-toggle CSS (primary)
            Locator active = page.locator(ACTIVE_TAB).first();
            if (active.count() > 0 && active.isVisible()) {
                String text = active.textContent();
                String tab = text == null ? "" : text.trim();
                if (!tab.isEmpty()) {
                    logger.logInfo("Currently active tab: " + tab);
                    return tab;
                }
            }
        } catch (Exception ignored) {
        }

        try {
            // Strategy 2: Python parity - find tab labels and check for mat-pseudo-checkbox-checked sibling
            Locator tabLabels = page.locator("span.mat-button-toggle-label-content");
            int count = tabLabels.count();
            for (int i = 0; i < count; i++) {
                Locator label = tabLabels.nth(i);
                String text = label.textContent() == null ? "" : label.textContent().trim();
                if (!text.equals("Lease") && !text.equals("Finance") && !text.equals("Cash")) continue;
                // Check if the ancestor toggle has checked state
                Locator ancestor = label.locator("xpath=./ancestor::mat-button-toggle[1]");
                if (ancestor.count() > 0) {
                    String cls = ancestor.first().getAttribute("class");
                    if (cls != null && cls.contains("mat-button-toggle-checked")) {
                        logger.logInfo("Currently active tab (via ancestor class): " + text);
                        return text;
                    }
                }
                // Check aria-pressed on the inner button
                Locator innerBtn = label.locator("xpath=./ancestor::button[1]");
                if (innerBtn.count() > 0) {
                    String pressed = innerBtn.first().getAttribute("aria-pressed");
                    if ("true".equalsIgnoreCase(pressed)) {
                        logger.logInfo("Currently active tab (via aria-pressed): " + text);
                        return text;
                    }
                }
            }
        } catch (Exception e) {
            logger.logWarning("Could not determine active tab: " + e.getMessage());
        }
        return "";
    }

    private String getTradeInInputValue(String selector) {
        try {
            Locator input = page.locator(selector).first();

            // Use ATTACHED (not VISIBLE) – Angular Material hides these inputs visually
            // but they are always present in the DOM with a value.
            input.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.ATTACHED)
                    .setTimeout(5000));

            // Scroll into view & highlight (Python parity)
            try {
                input.evaluate("el => { el.scrollIntoView({block:'center'}); el.style.border='2px solid red'; }");
            } catch (Exception ignored) {
            }

            // 1) JS value – most reliable for Angular reactive forms (hidden inputs still have .value)
            String value = null;
            try {
                Object jsVal = input.evaluate("el => el.value || ''");
                if (jsVal != null) value = jsVal.toString().trim();
            } catch (Exception ignored) {
            }

            // 2) inputValue() API
            if (value == null || value.isEmpty()) {
                try {
                    value = input.inputValue();
                } catch (Exception ignored) {
                }
            }

            // 3) value attribute
            if (value == null || value.isEmpty()) {
                try {
                    value = input.getAttribute("value");
                } catch (Exception ignored) {
                }
            }

            // 4) parent node text
            if (value == null || value.isEmpty()) {
                try {
                    Object parentText = input.evaluate("el => el.parentNode ? el.parentNode.textContent.trim() : ''");
                    if (parentText != null) value = parentText.toString().trim();
                } catch (Exception ignored) {
                }
            }

            // 5) textContent fallback
            if (value == null || value.isEmpty()) {
                try {
                    value = input.textContent();
                } catch (Exception ignored) {
                }
            }

            String clean = cleanNumeric(value);
            if (clean.isEmpty()) {
                logger.logWarning("Found input field but couldn't extract value from: " + selector);
            } else {
                logger.logInfo("Successfully retrieved trade-in value from " + selector + ": " + clean);
            }
            return clean;
        } catch (Exception e) {
            logger.logWarning("Could not get trade-in value from " + selector + ": " + e.getMessage());
            return "";
        }
    }

    public String getCurrentTabTradeInValue() {
        String active = getActiveTabName().toLowerCase();
        String id = "trade_in_cash";
        if (active.contains("lease")) id = "trade_in_lease";
        else if (active.contains("finance")) id = "trade_in_finance";

        // Try [2] first (multiple instances), then [1], then CSS id
        String value = getTradeInInputValue("xpath=(//input[@id='" + id + "'])[2]");
        if (value.isEmpty()) value = getTradeInInputValue("xpath=(//input[@id='" + id + "'])[1]");
        if (value.isEmpty()) value = getTradeInInputValue("css=input#" + id);
        if (value.isEmpty()) logger.logWarning("Could not determine trade-in input for active tab: " + active);
        return value;
    }

    // Python parity: _get_trade_in_from_current_tab() – tries generic presence-only locators
    private String getTradeInFromCurrentTabFallback() {
        List<String> genericSelectors = Arrays.asList(
                "xpath=//input[contains(@id,'trade_in_')]",
                "css=input[id*='trade_in_']",
                "css=.trade-in-value"
        );
        for (String sel : genericSelectors) {
            try {
                Locator el = page.locator(sel).first();
                // Python uses wait_for_presence_of_element (attached, not visible)
                el.waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.ATTACHED)
                        .setTimeout(5000));
                // Python: element.get_attribute("value") or element.text
                String value = null;
                try {
                    Object v = el.evaluate("el => el.value || ''");
                    if (v != null) value = v.toString().trim();
                } catch (Exception ignored) {
                }
                if (value == null || value.isEmpty()) {
                    try {
                        value = el.inputValue();
                    } catch (Exception ignored) {
                    }
                }
                if (value == null || value.isEmpty()) {
                    try {
                        value = el.getAttribute("value");
                    } catch (Exception ignored) {
                    }
                }
                if (value == null || value.isEmpty()) {
                    try {
                        value = el.textContent();
                    } catch (Exception ignored) {
                    }
                }
                String clean = cleanNumeric(value);
                if (!clean.isEmpty()) {
                    logger.logInfo("Got trade-in value from fallback selector " + sel + ": " + clean);
                    return clean;
                }
            } catch (Exception ignored) {
            }
        }
        return "";
    }

    public void compareValues(String tab, String actual, String expected) {
        if (actual != null && actual.equals(expected)) {
            logger.logInfo("[" + tab + "] Trade-in value matches expected: " + expected);
        } else {
            logger.logError("[" + tab + "] Trade-in value mismatch. Expected: " + expected + ", Found: " + actual);
        }
    }

    public void compareTradeInPrice() {
        try {
            // Python parity: wait.until(EC.visibility_of_element_located((By.ID, "trade_in_lease")))
            // No [2] index – Python uses just the ID directly
            String actual = "";
            for (String sel : Arrays.asList(
                    "css=input#trade_in_lease",
                    "xpath=(//input[@id='trade_in_lease'])[1]",
                    "xpath=(//input[@id='trade_in_lease'])[2]")) {
                actual = getTradeInInputValue(sel);
                if (!actual.isEmpty()) break;
            }

            // expected comes from the displayed trade-in price element (same as get_trade_in_price())
            String expected = "";
            try {
                Locator priceSpan = page.locator(TRADE_IN_PRICE_MAIN).first();
                priceSpan.waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(5000));
                expected = cleanNumeric(priceSpan.textContent());
                logger.logInfo("Clean numeric price from UI: " + expected);
            } catch (Exception ignored) {
                expected = getTradeInFromCurrentTabFallback();
            }

            logger.logInfo("Comparing Lease Input Value: " + actual + " with Expected Price: " + expected);
            compareValues("Lease", actual, expected);
        } catch (Exception e) {
            logger.logWarning("Could not compare trade-in price: " + e.getMessage());
        }
    }

    public void compareTradeInAcrossTabs() {
        List<String> tabs = Arrays.asList("Lease", "Finance", "Cash");
        Map<String, String> values = new LinkedHashMap<>();

        for (String tab : tabs) {
            try {
                // Python parity: check if tab element exists first, then JS-click it
                String tabXpath = "//span[contains(@class,'mat-button-toggle-label-content') and contains(.,'" + tab + "')]";
                Locator tabLocator = page.locator("xpath=" + tabXpath);

                if (tabLocator.count() == 0) {
                    logger.logWarning("Tab " + tab + " not found");
                    continue;
                }

                // Python parity: use JS click (scrollIntoView + click) to bypass visibility issues
                try {
                    tabLocator.first().evaluate("el => { el.scrollIntoView(true); el.click(); }");
                    page.waitForTimeout(1000);
                    logger.logInfo("Switched to " + tab + " tab");
                } catch (Exception clickErr) {
                    logger.logWarning("Could not click " + tab + " tab: " + clickErr.getMessage());
                    continue;
                }

                // Python parity: _get_trade_in_from_current_tab() – generic locators, presence only
                String value = getTradeInFromCurrentTabFallback();

                if (value != null && !value.isEmpty()) {
                    values.put(tab, value);
                    logger.logInfo("Got trade-in value from " + tab + " tab: " + value);
                } else {
                    logger.logWarning("Could not get trade-in value from " + tab + " tab");
                }
            } catch (Exception e) {
                logger.logWarning("Error processing " + tab + " tab: " + e.getMessage());
            }
        }

        if (values.isEmpty()) {
            // Not a hard failure – KBB/BB may not be present on this environment
            logger.logWarning("Could not retrieve trade-in value from any tab – continuing test.");
            return;
        }

        long uniqueValues = values.values().stream().distinct().count();
        if (uniqueValues == 1) {
            logger.logInfo("All trade-in values match: " + values);
        } else {
            logger.logWarning("Trade-in values differ across tabs: " + values);
        }
    }

    public void switchBackToMainContent() {
        // Playwright frame operations are scoped to Frame objects; page remains main context.
        logger.logInfo("Switched back to main page content");
    }
}