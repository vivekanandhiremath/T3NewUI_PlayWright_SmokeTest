package utilities;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class PopupHandler {

    private final Page page;
//    private final Logger logger;

    public PopupHandler(Page page) {
        this.page = page;
//        this.logger = logger;
    }

    public void dismissAll() {

        dismissWdpu();
        dismissSearchOptics();
        dismissSpm247();
        dismissDbs();
        dismissNoThanks();
        dismissCookie();
        pressEscape();
    }

    private void dismissWdpu() {
        try {
            Locator el = page.locator("span.wdpu-close, [class*='wdpu-close']").first();
            if (el.count() > 0 && el.isVisible()) {
                el.click();
            }
        } catch (Exception ignored) {
        }
    }

    private void dismissSearchOptics() {
        try {
            Locator btn = page.locator("span.soModalClose").first();
            if (btn.count() > 0 && btn.isVisible()) {
                btn.click();
            }

            Locator iframe = page.locator("#searchOpticsIncentiveModalIFrame").first();
            if (iframe.count() > 0 && iframe.isVisible()) {
                page.evaluate("el => el.style.display='none'", iframe.elementHandle());
            }
        } catch (Exception ignored) {
        }
    }

    private void dismissSpm247() {
        try {
            page.evaluate("""
                        () => {
                            document.getElementById('spm-capture-form')?.remove();
                            document.getElementById('spm-capture-shadow')?.remove();
                        }
                    """);
        } catch (Exception ignored) {
        }
    }

    private void dismissDbs() {
        try {
            page.evaluate("""
                        () => {
                            document.querySelectorAll('[class*="dbs"], #dbs-popup')
                                .forEach(el => el.remove());
                        }
                    """);
        } catch (Exception ignored) {
        }
    }

    private void dismissNoThanks() {
        try {
            Locator btn = page.locator("button:has-text('No thanks')").first();
            if (btn.count() > 0 && btn.isVisible()) {
                btn.click();
            }
        } catch (Exception ignored) {
        }
    }

    private void dismissCookie() {
        try {
            Locator btn = page.locator("button:has-text('Accept'), button:has-text('Agree'), button:has-text('Got it')")
                    .first();
            if (btn.count() > 0 && btn.isVisible()) {
                btn.click();
            }
        } catch (Exception ignored) {
        }
    }

    private void pressEscape() {
        try {
            page.keyboard().press("Escape");
        } catch (Exception ignored) {
        }
    }
}