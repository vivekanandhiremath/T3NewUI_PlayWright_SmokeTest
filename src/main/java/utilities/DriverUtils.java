package utilities;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

public class DriverUtils {

    private final boolean headless;
    private final String browserType;
    private final CustomLogger logger = new CustomLogger();
    private Playwright playwright;
    private Browser browser;

    public DriverUtils(boolean headless) {
        this(headless, ReadProperties.getBrowser());
    }

    public DriverUtils(boolean headless, String browserType) {
        this.headless = headless;
        this.browserType = browserType.toLowerCase();
    }

    public Page initializeDriver() {
        playwright = Playwright.create();
        BrowserType selectedBrowserType;

        switch (browserType) {
            case "firefox":
                logger.logInfo("Initializing Firefox browser");
                selectedBrowserType = playwright.firefox();
                break;
            case "edge":
                logger.logInfo("Initializing Edge browser");
                selectedBrowserType = playwright.webkit();
                break;
            case "safari":
                logger.logInfo("Initializing Safari browser (webkit)");
                selectedBrowserType = playwright.webkit();
                break;
            case "chrome":
            default:
                logger.logInfo("Initializing Chrome browser");
                selectedBrowserType = playwright.chromium();
                break;
        }

        // honor the headless flag passed to the constructor
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions().setHeadless(this.headless);
        browser = selectedBrowserType.launch(launchOptions);
        return browser.newPage();
    }

    public void quitDriver() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }
}
