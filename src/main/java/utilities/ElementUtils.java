package utilities;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class ElementUtils {

    private final Page page;
    private final CustomLogger logger = new CustomLogger();

    public ElementUtils(Page page) {
        this.page = page;
    }

    // -------------------------
    // CLICK
    // -------------------------

    public void clickOnElement(String selector) {
        try {
            Locator loc = page.locator(selector);
            loc.scrollIntoViewIfNeeded();
            loc.click(); // auto-wait replaces Selenium wait + retry logic
        } catch (Exception e) {
            logger.logInfo("⚠️ Click failed: " + e.getMessage());
        }
    }

    public void clickOnElement(Locator locator) {
        try {
            locator.scrollIntoViewIfNeeded();
            locator.click();
        } catch (Exception e) {
            logger.logInfo("⚠️ Click failed: " + e.getMessage());
        }
    }

    // -------------------------
    // TYPE
    // -------------------------

    public boolean typeIntoElement(String selector, String text) {
        try {
            Locator loc = page.locator(selector);
            loc.fill(text);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // -------------------------
    // WAIT HELPERS (Playwright-native)
    // -------------------------

    public Locator waitForVisible(String selector, int timeoutMs) {
        try {
            Locator loc = page.locator(selector);
            loc.waitFor(new Locator.WaitForOptions()
                    .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                    .setTimeout(timeoutMs));
            return loc;
        } catch (Exception e) {
            return null;
        }
    }

    public Locator waitForAttached(String selector, int timeoutMs) {
        try {
            Locator loc = page.locator(selector);
            loc.waitFor(new Locator.WaitForOptions()
                    .setState(com.microsoft.playwright.options.WaitForSelectorState.ATTACHED)
                    .setTimeout(timeoutMs));
            return loc;
        } catch (Exception e) {
            return null;
        }
    }

    // -------------------------
    // SCROLL
    // -------------------------

    public void scrollToElement(String selector) {
        try {
            page.locator(selector).scrollIntoViewIfNeeded();
        } catch (Exception ignored) {
        }
    }

    public void scrollToBottom() {
        page.evaluate("window.scrollTo(0, document.body.scrollHeight)");
    }

    public void scrollToMiddle() {
        page.evaluate("window.scrollTo(0, document.body.scrollHeight / 2)");
    }

    // -------------------------
    // ATTRIBUTE
    // -------------------------

    public String getAttribute(String selector, String attr) {
        try {
            return page.locator(selector).getAttribute(attr);
        } catch (Exception e) {
            return null;
        }
    }
}