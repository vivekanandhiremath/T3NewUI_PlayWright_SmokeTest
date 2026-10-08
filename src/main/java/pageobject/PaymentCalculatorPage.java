package pageobject;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import utilities.CustomLogger;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PaymentCalculatorPage extends BasePage {

    // ---------- Calculator root – ALL selectors must stay inside the DIALOG instance only.
    // The page always has TWO app-payment-calculator elements:
    //   1) embedded mini-widget in the main page (no dialog class)
    //   2) the full dialog opened by clicking the calculator icon  (class contains mat-mdc-dialog-component-host)
    // We MUST target only #2 to avoid strict-mode violations and wrong-element interactions.
    private static final String CALC_ROOT_CSS = "app-payment-calculator.mat-mdc-dialog-component-host";
    private static final String CALC_ROOT_XPATH_PREFIX = "//app-payment-calculator[contains(@class,'mat-mdc-dialog-component-host')]";
    // ---------- Tabs ----------
    private static final String TAB_XPATH_TEMPLATE =
            CALC_ROOT_XPATH_PREFIX + "//span[contains(@class,'mat-button-toggle-label-content') and normalize-space()='%s']";
    private static final String ACTIVE_TAB_CSS =
            "app-payment-calculator.mat-mdc-dialog-component-host .mat-button-toggle.mat-button-toggle-checked .mat-button-toggle-label-content";
    // ---------- Months ----------
    private static final List<String> MONTH_SELECTORS = Arrays.asList(
            "css=app-payment-calculator.mat-mdc-dialog-component-host mat-button-toggle",
            "css=app-payment-calculator.mat-mdc-dialog-component-host .mat-button-toggle",
            "css=app-payment-calculator.mat-mdc-dialog-component-host .mat-chip, app-payment-calculator.mat-mdc-dialog-component-host .mat-mdc-chip",
            "css=app-payment-calculator.mat-mdc-dialog-component-host .mdc-evolution-chip, app-payment-calculator.mat-mdc-dialog-component-host .mdc-evolution-chip__action",
            "css=app-payment-calculator.mat-mdc-dialog-component-host button[role='radio'], app-payment-calculator.mat-mdc-dialog-component-host button[aria-pressed]",
            "xpath=" + CALC_ROOT_XPATH_PREFIX + "//div[contains(@class,'mat-button-toggle-group')]//*[self::span or self::button or self::div]"
    );
    private static final String NEXT_BTN_SELECTOR =
            "xpath=" + CALC_ROOT_XPATH_PREFIX + "//button[contains(@class,'next') or contains(@aria-label,'next') or .//*[contains(.,'Next')]]";
    // ---------- Incentives ----------
    private static final String INCENTIVES_LABEL_XPATH =
            CALC_ROOT_XPATH_PREFIX + "//span[normalize-space(.)='Incentives' or normalize-space(.)='Available Incentives' or " +
                    "normalize-space(.)='Offers' or normalize-space(.)='Programs']";
    private static final List<String> INCENTIVES_ROOT_SELECTORS = Arrays.asList(
            "xpath=" + CALC_ROOT_XPATH_PREFIX + "//span[normalize-space(.)='Incentives']/ancestor::*[1]",
            "xpath=" + CALC_ROOT_XPATH_PREFIX + "//span[normalize-space(.)='Available Incentives']/ancestor::*[1]",
            "xpath=" + CALC_ROOT_XPATH_PREFIX + "//div[contains(@class,'incentives')]",
            "xpath=" + CALC_ROOT_XPATH_PREFIX + "//section[contains(@class,'incentive')]",
            "css=app-payment-calculator.mat-mdc-dialog-component-host div[class*='incentive'], app-payment-calculator.mat-mdc-dialog-component-host section[class*='incentive']"
    );
    private static final List<String> INCENTIVE_ITEM_SELECTORS = Arrays.asList(
            "css=.incentive-item, .incentive, li, p, span",
            "xpath=.//li | .//p | .//span | .//*[contains(@class,'incentive')]"
    );
    private static final Set<String> INCENTIVE_HEADERS = new HashSet<>(
            Arrays.asList("incentives", "available incentives", "offers", "programs")
    );
    private static final Pattern MONTH_PATTERN = Pattern.compile("\\b(\\d{1,3})\\b");
    private final CustomLogger logger = new CustomLogger();

    public PaymentCalculatorPage(Page page) {
        super(page);
    }

    // -----------------------
    // helper context holder
    // -----------------------

    private Integer parseMonth(String text) {
        try {
            String s = safeTrim(text);
            Matcher m = MONTH_PATTERN.matcher(s);
            if (!m.find()) {
                return null;
            }
            int val = Integer.parseInt(m.group(1));
            return (val >= 1 && val <= 120) ? val : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private String safeTrim(String text) {
        return text == null ? "" : text.trim();
    }

    private String elementText(Locator element) {
        try {
            String t = safeTrim(element.textContent());
            if (!t.isEmpty()) {
                return t;
            }
            Object innerText = element.evaluate("el => el.innerText || ''");
            return safeTrim(innerText == null ? "" : String.valueOf(innerText));
        } catch (Exception ignored) {
            return "";
        }
    }

    // -----------------------
    // generic helpers
    // -----------------------

    private int safeCount(Locator locator) {
        try {
            return locator.count();
        } catch (Exception ignored) {
            return 0;
        }
    }

    private String domUid(Locator locator) {
        try {
            Object uid = locator.evaluate("el => {" +
                    "if (!el.hasAttribute('data-pc-dom-uid')) {" +
                    "  const id = 'pc-' + Date.now().toString(36) + '-' + Math.random().toString(36).slice(2);" +
                    "  el.setAttribute('data-pc-dom-uid', id);" +
                    "}" +
                    "return el.getAttribute('data-pc-dom-uid');" +
                    "}");
            return uid == null ? "" : String.valueOf(uid);
        } catch (Exception ignored) {
            return "";
        }
    }

    private boolean waitForAttached(Locator locator, double timeoutMs) {
        try {
            locator.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.ATTACHED)
                    .setTimeout(timeoutMs));
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean waitForVisible(Locator locator, double timeoutMs) {
        try {
            locator.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(timeoutMs));
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean clickLocator(Locator locator, double timeoutMs) {
        try {
            if (!waitForVisible(locator.first(), timeoutMs)) {
                return false;
            }
            Locator target = locator.first();
            target.scrollIntoViewIfNeeded();
            target.click(new Locator.ClickOptions().setTimeout(timeoutMs));
            return true;
        } catch (Exception ignored) {
            try {
                locator.first().click(new Locator.ClickOptions().setTimeout(timeoutMs).setForce(true));
                return true;
            } catch (Exception ignoredAgain) {
                return false;
            }
        }
    }

    private boolean jsClick(Locator locator) {
        try {
            locator.first().evaluate("el => { el.scrollIntoView({block:'center'}); el.click(); }");
            return true;
        } catch (Exception ignored) {
            try {
                locator.first().dispatchEvent("click");
                return true;
            } catch (Exception ignoredAgain) {
                return false;
            }
        }
    }

    private List<Locator> findMonthCandidates() {
        Map<String, Locator> unique = new LinkedHashMap<>();
        for (String selector : MONTH_SELECTORS) {
            try {
                Locator elements = page.locator(selector);
                int count = safeCount(elements);
                for (int i = 0; i < count; i++) {
                    Locator el = elements.nth(i);
                    String key = domUid(el);
                    if (key.isEmpty()) {
                        key = selector + "::" + i + "::" + elementText(el);
                    }
                    unique.putIfAbsent(key, el);
                }
            } catch (Exception ignored) {
                // Keep collecting from remaining selectors.
            }
        }
        return new ArrayList<>(unique.values());
    }

    public boolean switchToTab(String tabName) {
        return switchToTab(tabName, 4);
    }

    public boolean switchToTab(String tabName, int timeoutSeconds) {
        try {
            try {
                page.url();
            } catch (Exception browserError) {
                logger.logWarning("Browser not responsive when switching to " + tabName + ": " + browserError.getMessage());
                return false;
            }

            String mainSelector = "xpath=" + String.format(TAB_XPATH_TEMPLATE, tabName);
            Locator mainTab = page.locator(mainSelector).first();

            if (clickLocator(mainTab, timeoutSeconds * 1000.0) || jsClick(mainTab)) {
                page.waitForTimeout(300);
                return true;
            }

            List<String> altSelectors = Arrays.asList(
                    "xpath=" + CALC_ROOT_XPATH_PREFIX + "//button[normalize-space()='" + tabName + "']",
                    "xpath=" + CALC_ROOT_XPATH_PREFIX + "//*[contains(@class,'mat-button-toggle-label-content') and normalize-space()='" + tabName + "']",
                    "xpath=" + CALC_ROOT_XPATH_PREFIX + "//*[@role='tab' and (normalize-space()='" + tabName + "' or contains(.,'" + tabName + "'))]"
            );

            for (String alt : altSelectors) {
                try {
                    Locator candidate = page.locator(alt).first();
                    if (clickLocator(candidate, 1000) || jsClick(candidate)) {
                        page.waitForTimeout(300);
                        return true;
                    }
                } catch (Exception ignored) {
                    // Try next locator.
                }
            }

            try {
                page.evaluate("() => {" +
                        "const overlays = document.querySelectorAll('.cdk-overlay-backdrop, .modal-backdrop, .overlay');" +
                        "overlays.forEach(o => { o.style.display = 'none'; });" +
                        "}");
                page.waitForTimeout(100);
                if (clickLocator(mainTab, 1000)) {
                    page.waitForTimeout(300);
                    return true;
                }
            } catch (Exception ignored) {
                // Final warning below.
            }

            logger.logWarning("Could not switch to tab: " + tabName);
            return false;
        } catch (Exception e) {
            logger.logError("switchToTab " + tabName + " error", e);
            return false;
        }
    }

    // -----------------------
    // tab switching
    // -----------------------

    public Integer getSelectedMonth() {
        try {
            List<Locator> candidates = findMonthCandidates();

            // 1) aria-selected / aria-pressed / aria-checked
            for (Locator el : candidates) {
                for (String attr : Arrays.asList("aria-selected", "aria-pressed", "aria-checked")) {
                    try {
                        String val = safeTrim(el.getAttribute(attr)).toLowerCase(Locale.ROOT);
                        if ("true".equals(val) || "1".equals(val)) {
                            Integer month = parseMonth(elementText(el));
                            if (month != null) {
                                logger.logInfo("Selected month via ARIA " + attr + ": " + month);
                                return month;
                            }
                        }
                    } catch (Exception ignored) {
                        // Keep scanning.
                    }
                }
            }

            // 2) checked class on self or ancestor
            for (Locator el : candidates) {
                try {
                    String cls = safeTrim(el.getAttribute("class")).toLowerCase(Locale.ROOT);
                    if (cls.contains("mat-button-toggle-checked") || cls.contains("selected") ||
                            cls.contains("active") || cls.contains("checked") || cls.contains("mat-primary")) {
                        Integer month = parseMonth(elementText(el));
                        if (month != null) {
                            logger.logInfo("Selected month via class on self: " + month);
                            return month;
                        }
                    }

                    Locator ancestor = el.locator("xpath=.//ancestor::*[contains(@class,'mat-button-toggle-checked')][1]").first();
                    if (safeCount(ancestor) > 0) {
                        Integer month = parseMonth(elementText(ancestor));
                        if (month == null) {
                            month = parseMonth(elementText(el));
                        }
                        if (month != null) {
                            logger.logInfo("Selected month via ancestor checked class: " + month);
                            return month;
                        }
                    }
                } catch (Exception ignored) {
                    // Keep scanning.
                }
            }

            // 3) input radio checked
            try {
                Locator radios = page.locator("css=app-payment-calculator.mat-mdc-dialog-component-host input[type='radio'][checked], app-payment-calculator.mat-mdc-dialog-component-host input[type='radio'][aria-checked='true']");
                int rc = safeCount(radios);
                for (int i = 0; i < rc; i++) {
                    Locator radio = radios.nth(i);
                    try {
                        Locator label = radio.locator(
                                "xpath=./ancestor::*[contains(@class,'mat-button-toggle')][1]//*[contains(@class,'label-content') or self::span]"
                        ).first();
                        String text = safeCount(label) > 0 ? elementText(label) : elementText(radio);
                        Integer month = parseMonth(text);
                        if (month != null) {
                            logger.logInfo("Selected month via radio[checked]: " + month);
                            return month;
                        }
                    } catch (Exception ignored) {
                        // Keep scanning radios.
                    }
                }
            } catch (Exception ignored) {
                // Continue to CSS shortcut.
            }

            // 4) checked toggle content
            try {
                Locator checked = page.locator("css=" + ACTIVE_TAB_CSS);
                int cc = safeCount(checked);
                for (int i = 0; i < cc; i++) {
                    Integer month = parseMonth(elementText(checked.nth(i)));
                    if (month != null) {
                        logger.logInfo("Selected month via ACTIVE_TAB_CSS: " + month);
                        return month;
                    }
                }
            } catch (Exception ignored) {
                // Continue to fallback.
            }

            // 5) fallback first numeric candidate
            for (Locator el : candidates) {
                Integer month = parseMonth(elementText(el));
                if (month != null) {
                    logger.logInfo("Selected month via fallback: " + month);
                    return month;
                }
            }

            logger.logWarning("Current selected month: None");
            return null;
        } catch (Exception e) {
            logger.logError("getSelectedMonth error", e);
            return null;
        }
    }

    public List<Integer> logMonthsForCurrentPaymentType(String paymentType) {
        try {
            Set<Integer> uniqueMonths = new TreeSet<>();
            for (Locator el : findMonthCandidates()) {
                Integer month = parseMonth(elementText(el));
                if (month != null) {
                    uniqueMonths.add(month);
                }
            }
            List<Integer> result = new ArrayList<>(uniqueMonths);
            logger.logInfo(paymentType + " available months: " + result);
            return result;
        } catch (Exception e) {
            logger.logError("logMonthsForCurrentPaymentType error", e);
            return Collections.emptyList();
        }
    }

    // -----------------------
    // months
    // -----------------------

    public List<Integer> getAvailableMonths() {
        return logMonthsForCurrentPaymentType("Payment");
    }

    public boolean clickNextMonth() {
        try {
            if (clickLocator(page.locator(NEXT_BTN_SELECTOR).first(), 3000)) {
                page.waitForTimeout(500);
                return true;
            }
            return selectNextAvailableMonth(36);
        } catch (Exception e) {
            logger.logError("clickNextMonth failed", e);
            return false;
        }
    }

    public boolean selectNextAvailableMonth(Integer preferNotMonth) {
        try {
            List<MonthOption> parsed = new ArrayList<>();
            for (Locator el : findMonthCandidates()) {
                Integer month = parseMonth(elementText(el));
                if (month != null) {
                    parsed.add(new MonthOption(month, el));
                }
            }

            if (parsed.isEmpty()) {
                return false;
            }

            parsed.sort(Comparator.comparingInt(m -> m.month));
            Integer current = getSelectedMonth();

            if (preferNotMonth != null) {
                for (MonthOption option : parsed) {
                    if (option.month != preferNotMonth && clickLocator(option.element, 2000)) {
                        page.waitForTimeout(400);
                        logger.logInfo("Selected alternate month: " + option.month);
                        return true;
                    }
                }
            }

            if (current != null) {
                for (MonthOption option : parsed) {
                    if (option.month > current && clickLocator(option.element, 2000)) {
                        page.waitForTimeout(400);
                        logger.logInfo("Selected next month: " + option.month);
                        return true;
                    }
                }
            }

            for (MonthOption option : parsed) {
                try {
                    String aria = safeTrim(option.element.getAttribute("aria-selected")).toLowerCase(Locale.ROOT);
                    String cls = safeTrim(option.element.getAttribute("class")).toLowerCase(Locale.ROOT);
                    if (!"true".equals(aria) && !cls.contains("mat-button-toggle-checked") && clickLocator(option.element, 2000)) {
                        page.waitForTimeout(400);
                        logger.logInfo("Selected fallback month: " + option.month);
                        return true;
                    }
                } catch (Exception ignored) {
                    // Keep scanning fallback options.
                }
            }

            MonthOption first = parsed.get(0);
            boolean ok = clickLocator(first.element, 2000);
            if (ok) {
                page.waitForTimeout(400);
            }
            return ok;
        } catch (Exception e) {
            logger.logError("selectNextAvailableMonth error", e);
            return false;
        }
    }

    private List<String> collectTextsFromRoot(Locator root) {
        List<String> texts = new ArrayList<>();
        try {
            for (String selector : INCENTIVE_ITEM_SELECTORS) {
                try {
                    Locator items = root.locator(selector);
                    int count = safeCount(items);
                    for (int i = 0; i < count; i++) {
                        String text = elementText(items.nth(i));
                        if (!text.isEmpty() && !INCENTIVE_HEADERS.contains(text.toLowerCase(Locale.ROOT)) && !texts.contains(text)) {
                            texts.add(text);
                        }
                    }
                } catch (Exception ignored) {
                    // Try next selector.
                }
            }

            if (texts.isEmpty()) {
                String block = safeTrim(root.textContent());
                for (String line : block.split("\\R")) {
                    String value = safeTrim(line);
                    if (!value.isEmpty() && !INCENTIVE_HEADERS.contains(value.toLowerCase(Locale.ROOT)) && !texts.contains(value)) {
                        texts.add(value);
                    }
                }
            }
        } catch (Exception ignored) {
            // Keep best-effort collection.
        }
        return texts;
    }

    public List<String> getIncentives() {
        try {
            List<String> texts = new ArrayList<>();

            Locator labels = page.locator("xpath=" + INCENTIVES_LABEL_XPATH);
            if (safeCount(labels) > 0) {
                Locator label = labels.first();
                Locator root = safeCount(label.locator("xpath=..").first()) > 0 ? label.locator("xpath=..").first() : label;
                texts = collectTextsFromRoot(root);
            }

            if (texts.isEmpty()) {
                for (String selector : INCENTIVES_ROOT_SELECTORS) {
                    Locator container = page.locator(selector).first();
                    if (waitForAttached(container, 2000)) {
                        texts = collectTextsFromRoot(container);
                        if (!texts.isEmpty()) {
                            break;
                        }
                    }
                }
            }

            logger.logInfo("Incentives: " + texts);
            return texts;
        } catch (Exception e) {
            logger.logError("getIncentives error", e);
            return Collections.emptyList();
        }
    }

    // -----------------------
    // incentives
    // -----------------------

    private Map<String, Object> emptyTabData() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("selected_month", null);
        data.put("available_months", Collections.emptyList());
        data.put("incentives", Collections.emptyList());
        data.put("next_month_incentives", Collections.emptyList());
        return data;
    }

    /**
     * Closes the Payment Calculator dialog.
     * Matches Python: click_on_payment_calculator_close_icon → clicks (//button[@id='closeCalculator'])[2]
     * The [2] index targets the DIALOG close button (not the mini-widget button).
     * NO Escape key – it propagates globally and closes the main widget.
     */
    public void closeCalculator() {
        // Python selector: (//button[@id='closeCalculator'])[2]
        try {
            page.locator("(//button[@id='closeCalculator'])[2]").click();
            logger.logInfo("closeCalculator: closed via (//button[@id='closeCalculator'])[2]");
        } catch (Exception e) {
            logger.logWarning("closeCalculator: could not close calculator – " + e.getMessage());
        }
    }

    public Map<String, Map<String, Object>> processAllPaymentTypes() {
        Map<String, Map<String, Object>> summary = new LinkedHashMap<>();

        // Wait for the DIALOG instance of app-payment-calculator to be visible.
        // There are always 2 app-payment-calculator elements (widget + dialog);
        // the dialog one has class mat-mdc-dialog-component-host.
        try {
            page.locator(CALC_ROOT_CSS).first().waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(10_000));
            logger.logInfo("processAllPaymentTypes: calculator dialog is visible.");
        } catch (Exception e) {
            logger.logWarning("processAllPaymentTypes: calculator dialog did not become visible within 10s – " + e.getMessage());
            return summary;
        }

        for (String tab : Arrays.asList("Lease", "Finance", "Cash")) {
            try {
                try {
                    page.url();
                } catch (Exception browserError) {
                    logger.logWarning("Browser not responsive during " + tab + " processing: " + browserError.getMessage());
                    summary.put(tab, emptyTabData());
                    continue;
                }

                // Check if the tab exists in the calculator before trying to switch
                String tabXpath = String.format(TAB_XPATH_TEMPLATE, tab);
                Locator tabEl = page.locator("xpath=" + tabXpath);
                if (tabEl.count() == 0) {
                    logger.logWarning(tab + " tab not found in calculator – skipping.");
                    summary.put(tab, emptyTabData());
                    continue;
                }

                if (!switchToTab(tab)) {
                    logger.logWarning(tab + " tab could not be clicked – skipping.");
                    summary.put(tab, emptyTabData());
                    continue;
                }

                page.waitForTimeout(300);

                Integer selected = null;
                List<Integer> available = new ArrayList<>();
                List<String> nextMonthIncentives = new ArrayList<>();

                // Terms (months) are only for Lease and Finance – not Cash
                if (!"cash".equalsIgnoreCase(tab)) {
                    selected = getSelectedMonth();
                    available = logMonthsForCurrentPaymentType(tab);
                }

                List<String> incentives = getIncentives();

                if (!"cash".equalsIgnoreCase(tab)) {
                    if (selectNextAvailableMonth(36)) {
                        page.waitForTimeout(200);
                        nextMonthIncentives = getIncentives();
                    } else if (clickNextMonth()) {
                        page.waitForTimeout(200);
                        nextMonthIncentives = getIncentives();
                    }
                }

                Map<String, Object> data = new LinkedHashMap<>();
                data.put("selected_month", selected);
                data.put("available_months", available);
                data.put("incentives", incentives);
                data.put("next_month_incentives", nextMonthIncentives);

                summary.put(tab, data);
                logger.logInfo(tab + " selected: " + selected + ", incentives: " + incentives + ", next: " + nextMonthIncentives);
            } catch (Exception e) {
                logger.logError("processAllPaymentTypes " + tab + " error", e);
                summary.put(tab, emptyTabData());
            }
        }

        logger.logInfo("Payment summary: " + summary);

        // Always close the calculator before returning control to the caller
        try {
            closeCalculator();
        } catch (Exception ignored) {
        }

        return summary;
    }

    // -----------------------
    // close calculator
    // -----------------------

    public static class TradeInContext {
        public String provider;
        public String price;

        public TradeInContext(String provider, String price) {
            this.provider = provider;
            this.price = price;
        }
    }

    // -----------------------
    // orchestrator
    // -----------------------

    private static class MonthOption {
        final int month;
        final Locator element;

        MonthOption(int month, Locator element) {
            this.month = month;
            this.element = element;
        }
    }
}